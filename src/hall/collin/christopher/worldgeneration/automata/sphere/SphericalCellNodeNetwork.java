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

package hall.collin.christopher.worldgeneration.automata.sphere;

import hall.collin.christopher.worldgeneration.math.AbstractNumberGenerator;
import hall.collin.christopher.worldgeneration.math.Dodecahedron;
import hall.collin.christopher.worldgeneration.math.Icosahedron;
import hall.collin.christopher.worldgeneration.math.Point3D;
import hall.collin.christopher.worldgeneration.math.SpherePoint;
import hall.collin.christopher.worldgeneration.math.SphericalMath;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;
/*
NOTES:
For 0 subdivisions (20 cells), it takes 3 automata iterations for a node to influence the whole planet
For 1 subdivisions (80 cells), it takes 5 automata iterations for a node to influence the whole planet
For 2 subdivisions (320 cells), it takes 11 automata iterations for a node to influence the whole planet
For 3 subdivisions (1280 cells), it takes 21 automata iterations for a node to influence the whole planet
For 4 subdivisions (5120 cells), it takes 40 automata iterations for a node to influence the whole planet
For 5 subdivisions (20480 cells), it takes 87 automata iterations for a node to influence the whole planet
The general formula is approximately: I(s) = 2.78*e^(0.678*s)
	I is number of iterations for complete coverage
	s is number of subvidision operations
*/

/**
 * A class for representing a node network on a sphere.
 * @author CCHall
 */
public class SphericalCellNodeNetwork {
	
	private final SphericalMath mathHelper = SphericalMath.getInstance();
	
	
	SphereNetCell[] starting20 = new SphereNetCell[20];
	/**
	 * Creates a network with the given starting value at all nodes.
	 * @param initialValue 
	 */
	public SphericalCellNodeNetwork(Double initialValue){
		initialize20nodes(initialValue);
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
	protected List<NumberValueNetNode[]> nodeLayers = new ArrayList<>();
	/** network initialized to 20 cells (icosahedron) */
	private void initialize20nodes(Double initialValue){
	//	familyTree.clear();
		cellLayers.clear();
		nodeLayers.clear();
		Point3D[] coords = (new Icosahedron()).getVertices();
		Map<Point3D,NumberValueNetNode> vertexMapping = new HashMap<>();
		Map<Point3D,SphereNetCell> cellMapping = new HashMap<>();
		Point3D[] midPointCoords = (new Dodecahedron()).getVertices(); // logitude wil be off by a quarter turn
		for(int i = 0; i < midPointCoords.length; i++){
			Point3D p = midPointCoords[i];
			midPointCoords[i] = new Point3D(p.getZ(),p.getY(),p.getX());// rotate quarter-turn
		}
		NumberValueNetNode[] vertices = new NumberValueNetNode[12];
		for(int i = 0; i < coords.length; i++){
			vertices[i] = new NumberValueNetNode(mathHelper.point3DToLonLat(coords[i]),initialValue,5);
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
		final Map<SpherePoint, NumberValueNetNode> newPoints = new ConcurrentHashMap<>();
		
		
		// subdivide the net
		List<SphereNetCell[]> children = net.parallelStream().map(
				(final SphereNetCell n)->{
					// generate corners
					SpherePoint[] midpoints = new SpherePoint[3];
					NumberValueNetNode[] newCorners = new NumberValueNetNode[3];
					NumberValueNetNode[] oldCorners = new NumberValueNetNode[3];
					for(int i = 0; i < 3; i++){
						final int j = i; // closures...
						midpoints[i] = mathHelper.midpoint(n.node[(i+1)%3].getCoordinate(), n.node[(i+2)%3].getCoordinate());
						
						newCorners[i] = newPoints.computeIfAbsent(midpoints[j],(SpherePoint p)->{
							double v = 0.5*(((NumberValueNetNode)n.node[(j+1)%3]).getValue() + ((NumberValueNetNode)n.node[(j+2)%3]).getValue());
							NumberValueNetNode nc = new NumberValueNetNode(p, v, 6);
							return nc;
						});
						oldCorners[i] = newPoints.computeIfAbsent(n.node[j].getCoordinate(),(SpherePoint p)->{
							NumberValueNetNode nc = new NumberValueNetNode(p, ((NumberValueNetNode)n.node[j]).getValue(), n.node[j].getNumberConnections());
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
	
	
	private final double weight_self = 1.0 / (1+Math.sqrt(3));
	private final double weight_neighbor = (1 - weight_self)/2;
	
	
	/**
	 * Gets the value at a given coordinate by interpolating the nodes of the 
	 * network in the cell surrounding the point of interest.
	 * @param pt Point of interest
	 * @return The interpolated value at the point of interest.
	 */
	public double interpolateValueAt(SpherePoint pt) {
		if(cellLayers.size() == 1){
			SphereNetCell cell = getCellFor(pt, cellLayers.get(0));
			return interpolate(pt, cell);
		} else {
			// get closest nod in the first layer of the net
			SphereNetCell cell = getCellFor(pt, cellLayers.get(0));
			for(int i = 1; i < cellLayers.size(); i++){
				cell = getCellFor(pt,cell.child);
			}
			
			return interpolate(pt, cell);
		}
		
	}
	
	
	
	private double interpolate(SpherePoint pt, SphereNetCell closest) {
	//	if(true) return (closest.point[0].getValue()+closest.point[1].getValue()+closest.point[2].getValue())/3;
		// barycentric interpolation on midpoints
		return interpolate(pt, 
				((NumberValueNetNode)closest.node[0]).getValue(), closest.node[0].getCoordinate(), 
				((NumberValueNetNode)closest.node[1]).getValue(), closest.node[1].getCoordinate(), 
				((NumberValueNetNode)closest.node[2]).getValue(), closest.node[2].getCoordinate());
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
		nodeLayers.add(nodes.toArray(new NumberValueNetNode[nodes.size()]));
	}

	/**
	 * Implementation of the SphereNetNode that carries a double as a data 
	 * value.
	 */
	private static class NumberValueNetNode extends SphereNetNode{
		/**
		 * Constructor
		 * @param p coorinate
		 * @param val initial value
		 * @param numConnections Number of possible connections to other nodes 
		 * in the network.
		 */
		NumberValueNetNode(SpherePoint p, double val, int numConnections) {
			super(p,numConnections);
			value = val;
		}
		
		double value = 0;
		DoubleAdder changeBuffer = new DoubleAdder();
		
		public void addToChangeBuffer(double val){
			changeBuffer.add(val);
		}
		public void applyChangeBuffer(){
			value += changeBuffer.sumThenReset();
		}double getValue(){
			return value;
		}
		void setValue(double value){
			this.value = value;
		}
	}
	
	@Deprecated public static void main(String[] s){
		if(test2())System.exit(0);
		SphericalCellNodeNetwork net = new SphericalCellNodeNetwork(0.0);
		AbstractNumberGenerator prng =  new hall.collin.christopher.worldgeneration.math.DefaultRandomNumberGenerator();
		for(NumberValueNetNode n : net.nodeLayers.get(0)){
			n.setValue(prng.nextDouble());
		}
		int i = 0;
		while(true){
			System.out.println("\t"+i+" subdivisions");
			visualizeNetwork(net,true);
			net.subdivideNetwork();
			i++;
		}
	}
	
	@Deprecated public static boolean test2(){
		SphericalCellNodeNetwork net = new SphericalCellNodeNetwork(0.0);
		int numSubDivisions = 5;
		for(int i = 0; i < numSubDivisions; i++){
			System.out.println(net.nodeLayers.get(i).length + " nodes");
			net.subdivideNetwork();
		}
			System.out.println(net.nodeLayers.get(net.nodeLayers.size()-1).length + " nodes");
		Random r = new Random();
		List<NumberValueNetNode> nodeNet = Arrays.asList(net.nodeLayers.get(net.nodeLayers.size()-1));
		nodeNet.get(r.nextInt(nodeNet.size())).setValue(0.75);
		
		final double conductivity = 0.1;
		boolean cont = true;
		final int preIterations = 80;
		final int iterationsPerView = 1;
		int iterCount = 0;
		for (int i = 0; i < preIterations; i++) {
			nodeNet.parallelStream().forEach((NumberValueNetNode n) -> {
				// network automata
				//	double loss = 0;
				for (SphereNetNode cc : n.getConnections()) {
					NumberValueNetNode c = ((NumberValueNetNode)cc);
					if (n.getValue() > c.getValue() && c.getValue() < 0.5) {
						c.addToChangeBuffer(0.1);
					}
			//		double delta = conductivity*(n.value - c.value);
					//		loss += delta;
					//		c.addToChangeBuffer(delta);
				}
				//	n.addToChangeBuffer(-1*loss);
			});
			nodeNet.parallelStream().forEach((NumberValueNetNode n) -> {
				n.applyChangeBuffer();
			});
			iterCount++;
		}
		while(cont){
			for(int i = 0; i < iterationsPerView; i++){
			nodeNet.parallelStream().forEach((NumberValueNetNode n)->{
				// network automata
			//	double loss = 0;
				for (SphereNetNode cc : n.getConnections()){
					NumberValueNetNode c = ((NumberValueNetNode)cc);
					if(n.getValue() > c.getValue() && c.getValue() < 0.5){
						c.addToChangeBuffer(0.1);
					}
			//		double delta = conductivity*(n.value - c.value);
			//		loss += delta;
			//		c.addToChangeBuffer(delta);
				}
			//	n.addToChangeBuffer(-1*loss);
			});
			nodeNet.parallelStream().forEach((NumberValueNetNode n)->{
				n.applyChangeBuffer();
			});
			iterCount++;
			}
			System.out.println(iterCount+" terations");
			visualizeNetwork(net,false);
		}
		return true;
	}
	
	@Deprecated public static void visualizeNetwork(SphericalCellNodeNetwork net, boolean paintRefPoints){
		System.out.println(net.cellLayers.get(net.cellLayers.size()-1).length + " cells in this layer.");
		final java.awt.image.BufferedImage bimg = new java.awt.image.BufferedImage(600,300,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		List<Callable<Boolean>> rowTasks = new ArrayList<>();
		for(int y = 0; y < bimg.getHeight(); y++){
			final double latitude = 0.5 * Math.PI * (1.0 - (y/(0.5*bimg.getHeight())));
			final int py = y;
			rowTasks.add(()->{
				for(int px = 0; px < bimg.getWidth(); px++){
					final double longitude = 2*Math.PI*px/((double)bimg.getWidth());
					double value = net.interpolateValueAt(new SpherePoint(longitude,latitude));
					float hue = (float)(value - Math.floor(value));
					bimg.setRGB(px, py, java.awt.Color.HSBtoRGB((float)hue, 1, 1));
				}
				return true;
			});
		}
		ForkJoinPool.commonPool().invokeAll(rowTasks);
		if(paintRefPoints){
		Graphics2D g = bimg.createGraphics();
		// show corners
		g.setColor(Color.BLACK);
		for(SphereNetCell m : net.cellLayers.get(net.cellLayers.size()-1)){
			for(SphereNetNode nn : m.node){
				NumberValueNetNode n = (NumberValueNetNode)nn;
				int y = (int)((1-((n.getCoordinate().getLatitude() + 0.5*Math.PI)/Math.PI))*bimg.getHeight());
				int x = (int)(n.getCoordinate().getLongitude() / (2*Math.PI) * bimg.getWidth());
				if(x <= 0){
					g.fillRect(x-1, y-1, 2, 2);
					x += bimg.getWidth();
				}
				g.fillRect(x-1, y-1, 2, 2);
			}
		}
		// show centers
		g.setColor(Color.WHITE);
		for(SphereNetCell m : net.cellLayers.get(net.cellLayers.size()-1)){
				int y = (int) ((1 - ((m.midpoint.getLatitude() + 0.5 * Math.PI) / Math.PI)) * bimg.getHeight());
				int x = (int) (m.midpoint.getLongitude() / (2 * Math.PI) * bimg.getWidth());
				if(x <= 0){
					g.fillRect(x-1, y-1, 2, 2);
					x += bimg.getWidth();
				}
				g.fillRect(x - 1, y - 1, 2, 2);
			
		}
		}
		javax.swing.JOptionPane.showMessageDialog(null, new javax.swing.JLabel(new javax.swing.ImageIcon(bimg)));
	}
	
}
