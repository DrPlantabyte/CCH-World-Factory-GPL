/*
CCH World Factory - GPL

Copyright (C) 2014 Christopher Collin Hall
email: explosivegnome@yahoo.com

CCH World Factory - GPL is distributed under the GNU General Public 
License (GPL) version 3. A non-GPL branch of the CCH World Factory 
also exists. For non-GPL licensing options, contact the copyright 
holder, Christopher Collin Hall (explosivegnome@yahoo.com). 

CCH World Factory - GPL is free software: you can redistribute it 
and/or modify it under the terms of the GNU General Public License 
as published by the Free Software Foundation, either version 3 of 
the License, or (at your option) any later version.

CCH World Factory - GPL is distributed in the hope that it will be 
useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with CCH World Factory - GPL.  If not, see 
<http://www.gnu.org/licenses/>.

*/
/*
 * Copyright 2014 - Christopher Collin Hall ( explosivegnome@yahoo.com )
 * All rights reserved.
 */
package hall.collin.christopher.worldgeneration;

import hall.collin.christopher.worldgeneration.automata.sphere.SphereNetCell;
import hall.collin.christopher.worldgeneration.automata.sphere.SphereNetNode;
import hall.collin.christopher.worldgeneration.graphics.AltitudePainter;
import hall.collin.christopher.worldgeneration.graphics.MercatorMapProjector;
import hall.collin.christopher.worldgeneration.math.CubicInterpolator;
import hall.collin.christopher.worldgeneration.math.Dodecahedron;
import hall.collin.christopher.worldgeneration.math.Icosahedron;
import hall.collin.christopher.worldgeneration.math.Point3D;
import hall.collin.christopher.worldgeneration.math.SpherePoint;
import hall.collin.christopher.worldgeneration.math.SphericalMath;
import hall.collin.christopher.worldgeneration.util.GUI;
import static hall.collin.christopher.worldgeneration.util.GUI.colorLUT;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * This class runs a water diffusion simulation via a node-network 
 * arranged to emulate the surface of a sphere.
 * @author CCHall
 */
public class HydrologyNetworkSimulation {
	
	private final SphericalMath mathHelper = SphericalMath.getInstance();
	
	private SphereNetCell[] starting20 = new SphereNetCell[20];
	private long numWaterSourceNodes = -1;
	/**
	 * Creates a network with the given starting value at all nodes.
	 * @param initialValue 
	 */
	protected HydrologyNetworkSimulation(){
		initialize20nodes();
	}
	
	
	private static final int numSubDivisions = 5;
	private static final int numIterations = 1600;
	private static final double initialPrecipitation = 0;
	private static final double initialWater = 0;
	private static final double minConductivity = 0.05;
	
	private static final double oceanRefillValue = 0.0;
	private static final double oceanInitialPrecipitate = 500;
	
	/**
	 * Creates a node-network around the planet and then initializes the 
	 * values to the starting point of the simulation (but does not 
	 * run the simulation).
	 * @param p Planet whose weather you want to simulate
	 * @param atmosphericPressure_kPa The planet's atmospheric pressure at 
	 * sea-level, in kPa.
	 * @return An instance of HydrologyNetworkSimulation that is ready to 
	 * simulate
	 */
	public static HydrologyNetworkSimulation createHydrologyNetwork(AbstractPlanet p, double atmosphericPressure_kPa){
		final HydrologyNetworkSimulation sim = new HydrologyNetworkSimulation();
		for(int i = 0; i < numSubDivisions; i++){
			sim.subdivideNetwork();
		}
		HydrologyNetNode[] net = sim.getNodeNetwork();
		final double precision = Math.sqrt((4*Math.PI*p.getRadius()*p.getRadius()) / (double)net.length);
		Arrays.asList(net).parallelStream().forEach((HydrologyNetNode n)->{
			// set initial values
			n.precipitate = initialPrecipitation;
			n.cloud = initialWater;
			SpherePoint coord = n.getCoordinate();
			double altitude = p.getAltitude(coord.getLongitude(), coord.getLatitude(), precision);
			double roughness = p.getRoughness(coord.getLongitude(), coord.getLatitude(), precision);
			double temperature = p.getTemperature(coord.getLongitude(), coord.getLatitude(), precision);
			n.conductivity = sim.calculateConductivity(roughness);
			n.evaporativity = sim.calculateEvaporation(temperature, atmosphericPressure_kPa);
			n.precipitivity = sim.calculatePreciptivity(roughness);
			n.runoff = sim.calculateRunOff(altitude, numIterations);
			n.isWaterSource = (altitude <= 0);
		});
		// remove source status of costal nodes (avoids interpolation artifact)
		List<HydrologyNetNode> costalNodes = Collections.synchronizedList(new ArrayList<>());
		Arrays.asList(net).parallelStream().forEach((HydrologyNetNode n)->{
			if(n.isWaterSource){
				for(SphereNetNode c : n.getConnections()){
					if(!((HydrologyNetNode)c).isWaterSource){
						costalNodes.add(n);
						break;
					}
				}
			}
		});
		costalNodes.parallelStream().forEach((n) -> {
			n.isWaterSource = false;
		});
		// set parameters specific to watersource nodes
		final LongAdder waterSourceCounter = new LongAdder();
		Arrays.asList(net).parallelStream().filter((HydrologyNetNode n)->n.isWaterSource).forEach((HydrologyNetNode n)->{
			n.runoff = -1*oceanRefillValue;
			n.precipitate = oceanInitialPrecipitate;
			waterSourceCounter.increment();
		});
		sim.numWaterSourceNodes = waterSourceCounter.sum();
		return sim;
	}
	
	private double calculateEvaporation(double annualMeanTemperature_C, double pressure_KPa){
		double p = (100 / pressure_KPa);
		double t = Math.max((0.01 * annualMeanTemperature_C) ,0.05);
		double w = 0.5*(0.10 + t);
		return Math.min(p*w, 0.5);
	}
	private double calculateRunOff(double altitude_m, int simulationDuration){
		double h = Math.max(altitude_m, 0);
		return h * 0.00002+0.005;
	}
	private double calculateConductivity(double terrainRoughness){
		terrainRoughness = Math.max(Math.min(terrainRoughness, 2),0);
		return Math.max(0.1*(2.0 - terrainRoughness),minConductivity);
	}
	private double calculatePreciptivity(double terrainRoughness){
		return 0.17;
	}
	
	private void computeHydrology(HydrologyNetNode node, DoubleAdder runoffBuffer){
		double dP, dW;
		dP = node.precipitivity * node.cloud - node.evaporativity * node.precipitate - node.runoff;
		dW = node.evaporativity * node.precipitate - node.precipitivity * node.cloud;
		for(int n = 0; n < node.getNumberConnections(); n++){
			double dWn = node.conductivity * node.cloud /  node.getNumberConnections();
			((HydrologyNetNode)node.getConnection(n)).addToWaterChangeBuffer(dWn);
			dW -= dWn;
		}
		node.addToPrecipitationChangeBuffer(dP);
		node.addToWaterChangeBuffer(dW);
		runoffBuffer.add(node.runoff); // used to make the water cycle zero-sum
	}
	
	private HydrologyNetNode[] getNodeNetwork(){
		return nodeLayers.get(nodeLayers.size()-1);
	}
	/**
	 * Runs the water diffusion simulation.
	 * @param progressTracker An instance of <code>java.util.concurrent.atomic.DoubleAdder</code>. 
	 * As the simulation progresses, incremental values will be added to this 
	 * object such that a total value of 1 is added at the time of completion 
	 * for the simulation. This is useful for showing a progress bar. Can be null.
	 */
	public void runSimulation(DoubleAdder progressTracker){
		clearCaches();
		final double progressIncrement = 1.0 / numIterations;
		List<HydrologyNetNode> net = Arrays.asList(getNodeNetwork());
		for(int i = 0; i < numIterations; i++){
			if(Thread.currentThread().isInterrupted()){
				// ABORT CALCULATION
				break;
			}
			iterateSimulation(net);
			if(progressTracker != null)progressTracker.add(progressIncrement);
		}
	}
	/**
	 * Performs a single iteration of the simulation over the whole network.
	 * @param net the network over which to iterate.
	 */
	protected void iterateSimulation(List<HydrologyNetNode> net){
		DoubleAdder runoff = new DoubleAdder();
		// water sim
		net.parallelStream().forEach((HydrologyNetNode n)->{
				computeHydrology(n,runoff);
			});
		// make zero-sum
		double runoffRedist = runoff.sum() / numWaterSourceNodes;
		net.parallelStream().filter((HydrologyNetNode n)->n.isWaterSource).forEach((HydrologyNetNode n)->{
			n.precipitationChangeBuffer.add(runoffRedist);
		});
		// apply changes
		net.parallelStream().forEach((HydrologyNetNode n)->{
			n.applyPrecipitationChangeBuffer();
			n.applyWaterChangeBuffer();
		});
		
	}
	/**
	 * Gets the closest cell to the given coordinate.
	 * @param pt coordinate of interest
	 * @param net Array of cells to check
	 * @return The cell in which the point of interest lands
	 */
	public static SphereNetCell getClosest(final SpherePoint pt, SphereNetCell[] net){
		double closestDist = Double.MAX_VALUE;
		int index = -1;
		for(int i = 0; i < net.length; i++){
			double d = net[i].approximateDistance(pt);
			if(d < closestDist){
				closestDist = d;
				index = i;
			}
		}
		return net[index];
		
	}
	/** Each element in this list is a network made by subdividing the previous network */
	protected List<SphereNetCell[]> cellLayers = new ArrayList<>();
	/** Each element in this list is a network made by subdividing the previous network */
	protected List<HydrologyNetNode[]> nodeLayers = new ArrayList<>();
	/** network initialized to 20 cells (icosahedron) */
	private void initialize20nodes(){
	//	familyTree.clear();
		cellLayers.clear();
		nodeLayers.clear();
		Point3D[] coords = (new Icosahedron()).getVertices();
		Map<Point3D,HydrologyNetNode> vertexMapping = new HashMap<>();
		Map<Point3D,SphereNetCell> cellMapping = new HashMap<>();
		Point3D[] midPointCoords = (new Dodecahedron()).getVertices(); // logitude wil be off by a quarter turn
		for(int i = 0; i < midPointCoords.length; i++){
			Point3D p = midPointCoords[i];
			midPointCoords[i] = new Point3D(p.getZ(),p.getY(),p.getX());// rotate quarter-turn
		}
		HydrologyNetNode[] vertices = new HydrologyNetNode[12];
		for(int i = 0; i < coords.length; i++){
			vertices[i] = new HydrologyNetNode(mathHelper.point3DToLonLat(coords[i]),5);
			vertexMapping.put(coords[i], vertices[i]);
		}
		
		// connect them
		for(int i = 0; i < 12; i++){
			Point3D[] closest5 = mathHelper.closestNPoints(coords[i], 5, coords);
			for(int j = 0; j < closest5.length; j++){
				vertices[i].setConnection(j,vertexMapping.get(closest5[j]));
			}
		}
		for(int i = 0; i < 20; i++){
			Point3D[] closest3 = mathHelper.closestNPoints(midPointCoords[i], 3, coords);
			starting20[i] = new SphereNetCell(vertexMapping.get(closest3[0]),vertexMapping.get(closest3[1]),vertexMapping.get(closest3[2]));
			cellMapping.put(midPointCoords[i], starting20[i]);
		}
		for(int i = 0; i < 20; i++){
			Point3D[] closest3 = mathHelper.closestNPoints(midPointCoords[i], 3, midPointCoords);
			for(int j = 0; j < closest3.length; j++){
				starting20[i].setNeighbor(j,cellMapping.get(closest3[j]));
			}
		}
		cellLayers.add(starting20);
		addAllCorners(starting20);
	}
	
	/** 
	 * Generates a new network by subdividing the lastest network by tesselation.
	 */
	public void subdivideNetwork(){
		List<SphereNetCell>net = Arrays.asList(cellLayers.get(cellLayers.size()-1));
		SphereNetCell[] next = new SphereNetCell[net.size()*4];
		final Map<SpherePoint, HydrologyNetNode> newPoints = new ConcurrentHashMap<>();
		
		
		// subdivide the net
		List<SphereNetCell[]> children = net.parallelStream().map(
				(final SphereNetCell n)->{
					// generate corners
					SpherePoint[] midpoints = new SpherePoint[3];
					HydrologyNetNode[] newCorners = new HydrologyNetNode[3];
					HydrologyNetNode[] oldCorners = new HydrologyNetNode[3];
					for(int i = 0; i < 3; i++){
						final int j = i; // closures...
						midpoints[i] = mathHelper.midpoint(n.node[(i+1)%3].getCoordinate(), n.node[(i+2)%3].getCoordinate());
						
						newCorners[i] = newPoints.computeIfAbsent(midpoints[j],(SpherePoint p)->{
							double v = 0.5*(((HydrologyNetNode)n.node[(j+1)%3]).getWaterValue() + ((HydrologyNetNode)n.node[(j+2)%3]).getWaterValue());
							HydrologyNetNode nc = new HydrologyNetNode(p, 6);
							return nc;
						});
						oldCorners[i] = newPoints.computeIfAbsent(n.node[j].getCoordinate(),(SpherePoint p)->{
							HydrologyNetNode nc = new HydrologyNetNode(p, n.node[j].getNumberConnections());
							return nc;
						});
					}
					// make the triangles
					n.child[3] = new SphereNetCell(newCorners[0],newCorners[1],newCorners[2]);
					for(int i = 0; i < 3; i++){
						n.child[i] = new SphereNetCell(oldCorners[i],newCorners[(i+1)%3],newCorners[(i+2)%3]);
					}
					// connect the triangles
					for(int i = 0; i < 3; i++){
						n.child[3].neighbor[i] = n.child[i];
						n.child[i].neighbor[i] = n.child[3];
					}
					// interconnect corners
					for(int i = 0; i < 3; i++){
						oldCorners[i].addConnectionIfAbsent(newCorners[(i+1)%3]);
						oldCorners[i].addConnectionIfAbsent(newCorners[(i+2)%3]);
						newCorners[i].addConnectionIfAbsent(newCorners[(i+1)%3]);
						newCorners[i].addConnectionIfAbsent(newCorners[(i+2)%3]);
						newCorners[i].addConnectionIfAbsent(oldCorners[(i+1)%3]);
						newCorners[i].addConnectionIfAbsent(oldCorners[(i+2)%3]);
					}
					return n.child;
				}
		).collect(Collectors.toList());
		// interconnect the child nets
		net.parallelStream().forEach((SphereNetCell n)->{
			// connect children to neighbor's children
			for(int i = 0; i < 3; i++){
				int b = 0; // neaighbor orientation
				for(b = 0; b < 3; b++ ){
					if(n.neighbor[i].neighbor[b] == n) break;
				}
				n.child[(i+1)%3].neighbor[i] = n.neighbor[i].child[(b+2)%3];
				n.child[(i+2)%3].neighbor[i] = n.neighbor[i].child[(b+1)%3];
				
				//interconnect corners
				n.child[3].node[i].addConnectionIfAbsent(n.child[3].node[(i+1)%3]);
				n.child[3].node[i].addConnectionIfAbsent(n.child[3].node[(i+2)%3]);
			}
		});
		
		// copy the children into a single network
		for(int i = 0; i < children.size(); i++){
			System.arraycopy(children.get(i), 0, next, i*4, 4);
		}
		cellLayers.add(next);
		addAllCorners(next);
	}
	
	
	
	/**
	 * Interpolates a value at a coordinate (within this cell) based on the 
	 * values at the corners of this triangle.
	 * @param at The coordinate at which to generate an interpolated value.
	 * @param v1 The value at corner 1
	 * @param p1 The coordinate of corner 1
	 * @param v2 The value at corner 2
	 * @param p2 The coordinate of corner 2
	 * @param v3 The value at corner 3
	 * @param p3 The coordinate of corner 3
	 * @return The barycentric interpolation at the requested coordinate.
	 */
	protected static double interpolate(SpherePoint at, double v1, SpherePoint p1, double v2, SpherePoint p2, double v3, SpherePoint p3){
		double a1 = SphericalMath.getInstance().relativeArea(at, p2, p3);
		double a2 = SphericalMath.getInstance().relativeArea(p1, at, p3);
		double a3 = SphericalMath.getInstance().relativeArea(p1, p2, at);
		double inverseSum = 1.0/(a1+a2+a3);
		double w1 = inverseSum * a1;
		double w2 = inverseSum * a2;
		double w3 = inverseSum * a3;
		return w1*v1+w2*v2+w3*v3;
	}
	
	
	
	
	/**
	 * Gets the value at a given coordinate by interpolating the nodes of the 
	 * network in the cell surrounding the point of interest.
	 * @param pt Point of interest
	 * @return The interpolated value at the point of interest.
	 */
	public double interpolateCloudValueAt(SpherePoint pt) {
		if(cellLayers.size() == 1){
			SphereNetCell cell = getCellFor(pt, cellLayers.get(0));
			return interpolateCloudWater(pt, cell);
		} else {
			// get closest nod in the first layer of the net
			SphereNetCell cell = getCellFor(pt, cellLayers.get(0));
			for(int i = 1; i < cellLayers.size(); i++){
				cell = getCellFor(pt,cell.child);
			}
			
			return interpolateCloudWater(pt, cell);
		}
		
	}
	
	private static final double GRID_SPACING = 0.015;
	private static final double GRID_MULTIPLIER = 1.0 / GRID_SPACING;
	private static final int GRID_SIZE = (int)(2*GRID_MULTIPLIER)+6;
	private static final int GRID_OFFSET = (int)(GRID_MULTIPLIER)+3;
	private Double[][][] precititationInterpolationCache = new Double[GRID_SIZE][GRID_SIZE][GRID_SIZE];
	/**
	 * Like interpolatePrecipitationValueAt(coordinate), but less prone to 
	 * interpolation artifacts.
	 * @param coordinate The coordinate to interpolate the value at.
	 * @return The amount of precipitation at the specified location.
	 */
	public double cubicInterpolatePrecipitation(SpherePoint coordinate){
		Point3D temp  = SphericalMath.getInstance().lonLatTo3D(coordinate);
		double[] xyz = new double[3];
		xyz[0] = temp.getX() * GRID_MULTIPLIER;
		xyz[1] = temp.getY() * GRID_MULTIPLIER;
		xyz[2] = temp.getZ() * GRID_MULTIPLIER;
		int[] intxyz = new int[3];
		for(int i = 0; i < 3; i++)intxyz[i] = (int)Math.floor(xyz[i]);
		double[][][] local64 = new double[4][4][4];
		for (int dx = -1; dx < 3; dx++) {
			for (int dy = -1; dy < 3; dy++) {
				for (int dz = -1; dz < 3; dz++) {
					Double v = precititationInterpolationCache[intxyz[0]+dx+GRID_OFFSET][intxyz[1]+dy+GRID_OFFSET][intxyz[2]+dz+GRID_OFFSET];
					if(v == null){
						SpherePoint p = SphericalMath.getInstance().point3DToLonLat(intxyz[0]+dx, intxyz[1]+dy, intxyz[2]+dz);
						v = this.interpolatePrecipitationValueAt(p);
						precititationInterpolationCache[intxyz[0]+dx+GRID_OFFSET][intxyz[1]+dy+GRID_OFFSET][intxyz[2]+dz+GRID_OFFSET] = v; // note: primitive double assignment is not atomic, but Object assignment is
					}
					local64[dx + 1][dy + 1][dz + 1] = v;
				}
			}
		}
		return CubicInterpolator.interpolate3d(xyz[0],xyz[1],xyz[2], local64);
	}
	/**
	 * Like interpolateCloudValueAt(coordinate), but less prone to 
	 * interpolation artifacts.
	 * @param coordinate The coordinate to interpolate the value at.
	 * @return The amount of moisture in the air at the specified location.
	 */
	public double cubicInterpolateCloudWater(SpherePoint coordinate){
		Point3D temp  = SphericalMath.getInstance().lonLatTo3D(coordinate);
		double[] xyz = new double[3];
		xyz[0] = temp.getX() * GRID_MULTIPLIER;
		xyz[1] = temp.getY() * GRID_MULTIPLIER;
		xyz[2] = temp.getZ() * GRID_MULTIPLIER;
		double[] intxyz = new double[3];
		for(int i = 0; i < 3; i++)intxyz[i] = Math.floor(xyz[i]);
		double[][][] local64 = new double[4][4][4];
		for (int dx = -1; dx < 3; dx++) {
			for (int dy = -1; dy < 3; dy++) {
				for (int dz = -1; dz < 3; dz++) {
					SpherePoint p = SphericalMath.getInstance().point3DToLonLat(intxyz[0]+dx, intxyz[1]+dy, intxyz[2]+dz);
					local64[dx + 1][dy + 1][dz + 1] = this.interpolatePrecipitationValueAt(p);
				}
			}
		}
		return CubicInterpolator.interpolate3d(xyz[0],xyz[1],xyz[2], local64);
	}
	
	private double interpolateCloudWater(SpherePoint pt, SphereNetCell closest) {
	//	if(true) return (closest.point[0].getValue()+closest.point[1].getValue()+closest.point[2].getValue())/3;
		// barycentric interpolation on midpoints
		return interpolate(pt, 
				((HydrologyNetNode)closest.node[0]).getWaterValue(), closest.node[0].getCoordinate(), 
				((HydrologyNetNode)closest.node[1]).getWaterValue(), closest.node[1].getCoordinate(), 
				((HydrologyNetNode)closest.node[2]).getWaterValue(), closest.node[2].getCoordinate());
	}
	/**
	 * Gets the value at a given coordinate by interpolating the nodes of the 
	 * network in the cell surrounding the point of interest.
	 * @param pt Point of interest
	 * @return The interpolated value at the point of interest.
	 */
	public double interpolatePrecipitationValueAt(SpherePoint pt) {
		if(cellLayers.size() == 1){
			SphereNetCell cell = getCellFor(pt, cellLayers.get(0));
			return interpolatePrecipitation(pt, cell);
		} else {
			// get closest nod in the first layer of the net
			SphereNetCell cell = getCellFor(pt, cellLayers.get(0));
			for(int i = 1; i < cellLayers.size(); i++){
				if(cell == null){
					throw new NullPointerException("Could not find cell in layer "+i+" near coordinate "+pt.toString());
				}
				cell = getCellFor(pt,cell.child);
			}
			
			return interpolatePrecipitation(pt, cell);
		}
		
	}
	
	
	
	private double interpolatePrecipitation(SpherePoint pt, SphereNetCell closest) {
	//	if(true) return (closest.point[0].getValue()+closest.point[1].getValue()+closest.point[2].getValue())/3;
		// barycentric interpolation on midpoints
		return interpolate(pt, 
				((HydrologyNetNode)closest.node[0]).getPrecipitationValue(), closest.node[0].getCoordinate(), 
				((HydrologyNetNode)closest.node[1]).getPrecipitationValue(), closest.node[1].getCoordinate(), 
				((HydrologyNetNode)closest.node[2]).getPrecipitationValue(), closest.node[2].getCoordinate());
	}
	
	

	private SphereNetCell getCellFor(SpherePoint pt, SphereNetCell[] net) {
		double dist = Double.MAX_VALUE;
		SphereNetCell closest = null;
		for(SphereNetCell cell : net) {
			double d = mathHelper.angularDistance(pt, cell.midpoint);
			if(d < dist){
				dist = d;
				closest = cell;
			}
		}
		return closest;
	}	

	private void addAllCorners(SphereNetCell[] cellNet) {
		HashSet<SphereNetNode> nodes = new HashSet<>();
		for(SphereNetCell t : cellNet){
			nodes.add(t.node[0]);
			nodes.add(t.node[1]);
			nodes.add(t.node[2]);
		}
		nodeLayers.add(nodes.toArray(new HydrologyNetNode[nodes.size()]));
	}
	/**
	 * Returns the number of nodes marked as water sources (ocean).
	 * @return  Returns the number of nodes marked as water sources (ocean).
	 */
	public long getNumberWaterSourceNodes() {
		return this.numWaterSourceNodes;
	}
	/**
	 * Returns the total size of the hydrology node network.
	 * @return The total number of nodes.
	 */
	public long getNumberNodes(){
		return getNodeNetwork().length;
	}
	/** empties caches used by interpolation methods */
	protected final void clearCaches() {
		for(Double[][] d : precititationInterpolationCache){
			for(Double[] dd : d){
				Arrays.fill(dd, null);
			}
		}
	}
	
	

	/**
	 * Implementation of the SphereNetNode that carries a hydrology data.
	 * Ocean nodes are designated as water source nodes and they produce water. 
	 * Other nodes carry water. 
	 * On each iteration, a node passes some water to neighboring nodes, drops 
	 * some water as precipitation, retakes some of the dropped water as 
	 * evaporation, looses some of the dropped water as run-off, and passes some 
	 * water to connected nodes.
	 */
	protected static class HydrologyNetNode extends SphereNetNode{
		/**
		 * Constructor
		 * @param p coorinate
		 * @param val initial value
		 * @param numConnections Number of possible connections to other nodes 
		 * in the network.
		 */
		HydrologyNetNode(SpherePoint p, int numConnections) {
			super(p,numConnections);
		}
		
		boolean isWaterSource = false;
		/** water held by this node */
		double cloud = 0; // TODO: make atomic if simulation uses direct assignment
		/** water change buffer */
		DoubleAdder waterChangeBuffer = new DoubleAdder();
		/** Water dropped on this node as precipitation */
		double precipitate = 0;
		/** precipitant change buffer */
		DoubleAdder precipitationChangeBuffer = new DoubleAdder();
		/** factor describing how much of this node's water it gives to neighbors per iteration */
		double conductivity = 1;
		/** factor for what fraction of this nodes water falls as precipitation */
		double precipitivity = 0;
		/** How much water moves from precipitant to water buffer each iteration */
		double evaporativity = 0;
		/** What fraction of dropped water disappears into rivers and groundwater */
		double runoff = 0;
		
		public void addToWaterChangeBuffer(double val){
			waterChangeBuffer.add(val);
		}
		public void applyWaterChangeBuffer(){
			cloud += waterChangeBuffer.sumThenReset();
		}
		public double getWaterValue(){
			return cloud;
		}
		public void setWaterValue(double value){
			this.cloud = value;
		}
		
		public void addToPrecipitationChangeBuffer(double val){
			precipitationChangeBuffer.add(val);
		}
		public void applyPrecipitationChangeBuffer(){
			precipitate += precipitationChangeBuffer.sumThenReset();
		}
		public double getPrecipitationValue(){
			return precipitate;
		}
		public void setPrecipitationValue(double value){
			this.precipitate = value;
		}
	}
	
	@Deprecated public static void main(String[] args){
		final AbstractPlanet p = new SimpleRandomPlanet("#"+System.currentTimeMillis());
		final int size = 128;
		final double precision = p.getRadius() * Math.PI / size / 4;
		MercatorMapProjector mmp = new MercatorMapProjector();
		BufferedImage mapProjection = mmp.createMapProjection(p, size, new AltitudePainter());
		GUI.showImagePopupNonmodal(mapProjection,"Map");
	//	double[][] altmap = new double[size*2][size];
		double[][] roughmap = new double[size*2][size];
	//	double[][] tempmap = new double[size*2][size];
		for(int x = 0; x < size*2; x++){
			double longitude = (double)x / (double)(size*2) * 2 * Math.PI;
			for(int y = 0; y < size; y++){
				double latitude = ((double)y / (double)size - 0.5) * Math.PI;
	//			altmap[x][y] = p.getAltitude(longitude, latitude, precision);
				roughmap[x][y] = p.getRoughness(longitude, latitude, precision);
	//			tempmap[x][y] = p.getTemperature(longitude, latitude, precision);
			}
		}
	//	GUI.showImagePopupNonmodal(GUI.visualize(altmap,0,3000),"Altitude");
		GUI.showImagePopupNonmodal(GUI.visualize(roughmap,0,1),"Roughness");
	//	GUI.showImagePopupNonmodal(GUI.visualize(tempmap,0,30),"Temperature");
		
		HydrologyNetworkSimulation sim = HydrologyNetworkSimulation.createHydrologyNetwork(p, 101.0);
		List<HydrologyNetNode> net = Arrays.asList(sim.nodeLayers.get(sim.nodeLayers.size()-1));
		double legendMin = -50;
		double legendMax = 150;
//		double[][] legend = new double[21][10];
//		for(int lx = 0; lx < legend.length; lx++){
//			Arrays.fill(legend[lx], legendMin+lx*(legendMax-legendMin)/legend.length);
//		}
//		GUI.showImagePopup(GUI.visualize(legend,legendMin,legendMax),"Legend");
		final int stepSize = 10;
		for(int i = 0; i <= 1000; i+= stepSize){
			if (i != 0) {
				for (int d = 0; d < stepSize; d++) {
					sim.iterateSimulation(net);
				}
			};
			System.out.println("iteration #"+i);
			GUI.showImagePopup(GUI.visualize(mapData(size,sim),legendMin,legendMax),"Simulation");
		}
	}
	
	@Deprecated private static double[][] mapData(int size, HydrologyNetworkSimulation sim){
		double[][] data = new double[size*2][size*2];
		List<Callable<Boolean>> taskList = new ArrayList<>();
		for(int x = 0; x < size*2; x++){
			final double longitude = (2 * Math.PI)*(x / ((double)size*2));
			final int px = x;
			taskList.add(new Callable<Boolean>(){
				public Boolean call(){
					for(int y = 0; y < size; y++){
						double latitude = (2 * y / ((double)size) - 1) * 0.5 * Math.PI;
						data[px][y] = sim.interpolatePrecipitationValueAt(new SpherePoint(longitude,latitude));
						data[px][y+size] = sim.interpolateCloudValueAt(new SpherePoint(longitude,latitude));
					}
					return true;
				}
			});
		}
		ForkJoinPool.commonPool().invokeAll(taskList);
		return data;
	}
}
