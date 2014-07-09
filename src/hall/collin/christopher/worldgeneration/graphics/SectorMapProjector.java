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

package hall.collin.christopher.worldgeneration.graphics;

import hall.collin.christopher.worldgeneration.AbstractPlanet;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * The SectorMapProjector divides the sphere into 12 pentagonal sectors.
 * <p/>
 * <table>
 * <hr><td>Sector Index</td><td>Sector Location</td>    <td>Sector Label</td></hr>
 * <tr><td>0</td>           <td>North Pole</td>         <td>α</td></tr>
 * <tr><td>1</td>           <td>Northern Hemisphere</td><td>β</td></tr>
 * <tr><td>2</td>           <td>Northern Hemisphere</td><td>γ</td></tr>
 * <tr><td>3</td>           <td>Northern Hemisphere</td><td>δ</td></tr>
 * <tr><td>4</td>           <td>Northern Hemisphere</td><td>ε</td></tr>
 * <tr><td>5</td>           <td>Northern Hemisphere</td><td>ζ</td></tr>
 * <tr><td>6</td>           <td>Southern Hemisphere</td><td>η</td></tr>
 * <tr><td>7</td>           <td>Southern Hemisphere</td><td>θ</td></tr>
 * <tr><td>8</td>           <td>Southern Hemisphere</td><td>ι</td></tr>
 * <tr><td>9</td>           <td>Southern Hemisphere</td><td>κ</td></tr>
 * <tr><td>10</td>           <td>Southern Hemisphere</td><td>λ</td></tr>
 * <tr><td>11</td>           <td>South Pole</td>         <td>μ</td></tr>
 * </table>
 * @author CCHall
 */
public class SectorMapProjector extends PlanetaryMapProjector{
// TODO: SVG export (define a pattern of an embedded png images with a gaussian blur filter set to 0.67 deviation, then fill pentagon with the pattern)
	protected final int sector;
	/** lon,lat coordinates of the corners */
	protected final Point2D[] cornerCoords = new Point2D.Double[5];
	/** lon,lat coordinates of the edge midpoints */
	protected final Point2D[] edgeMidpointCoords = new Point2D.Double[5];
	/** lon,lat coordinates of the center. Interpolation will use center point, closest corner, and closest edge midpoint */
	protected final Point2D centerCoord;
	/** relative x,y pixel coordinates of the pentagon corners (0,0 is <b>bottom</b> left corner and 1,1 is top right)*/
	protected final Point2D[] relPixelCornerCoords = new Point2D.Double[5];
	/** relative x,y pixel coordinates of the pentagon edge midpoints (0,0 is <b>bottom</b> left corner and 1,1 is top right)*/
	protected final Point2D[] relPixelEdgeCoords = new Point2D.Double[5];
	/** relative x,y pixel coordinates of the center of the pentagon */
	protected final Point2D relCenterCoord;

	/** Angle on sphere from center of pentagon to corner */
	protected final double radialAngle = Math.PI / (2.0 + 2.0*Math.cos(Math.PI / 5.0) + Math.sqrt((5.0 - Math.sqrt(5.0))/2.0));
	/** Angle on sphere from corner of pentagon to opposite edge */
	protected final double pentagonAngle = radialAngle * (1 + Math.cos(Math.PI / 5.0));
	/** Angle on sphere from corner of pentagon to neighboring corner */
	protected final double edgeAngle = radialAngle * Math.sqrt((5.0 - Math.sqrt(5.0))/2.0);
	/** Angle on sphere from center of pentagon to edge midpoint */
	protected final double inscribeAngle = radialAngle * Math.cos(Math.PI / 5.0);

	/**
	 * Array of symbols such that using the sector number as the index in the 
	 * array gives you the label (Greek lowercase letter) for that sector.
	 */
	public static final String[] SECTOR_LABELS = {"α","β","γ","δ","ε","ζ","η","θ","ι","κ","λ","μ"};
	
	private boolean drawLabels = true;
	private boolean drawMap = true;
	private boolean drawLandHillShading = true;
	private boolean drawOceanHillShading = true;
	/**
	 * Sets whether or not to draw labels, such as scale bars, on the image.
	 * @param drawLabels If true, labels will be drawn.
	 */
	public void enableLabels(boolean drawLabels){
		this.drawLabels = drawLabels;
	}
	/**
	 * Sets whether or not to draw the map itself (disable to see only labels).
	 * @param drawMap If true, map will be drawn.
	 */
	public void enableMap(boolean drawMap){
		this.drawMap = drawMap;
	}
	/**
	 * Sets whether or not to draw hill-shading on land.
	 * @param drawShading If true, hill-shading will be drawn.
	 */
	public void enableLandShader(boolean drawShading){
		this.drawLandHillShading = drawShading;
	}
	/**
	 * Sets whether or not to draw hill-shading on underwater terrain.
	 * @param drawShading If true, hill-shading will be drawn.
	 */
	public void enableUnderwaterShader(boolean drawShading){
		this.drawOceanHillShading = drawShading;
	}
	/**
	 * initializes a sector
	 * @param sectorIndex The index of the sector (0-11 inclusive)
	 */
	protected SectorMapProjector(int sectorIndex){
		sector = sectorIndex;
		
		final double piOver5 = Math.PI / 5.0;
		final double pi2Over5 = piOver5 * 2.0;
		final double piOver10 = Math.PI / 10.0;
		double latNorthPole = Math.PI / 2.0;
		double latAlphaEdge = latNorthPole - inscribeAngle;
		double latAlphaCorner = latNorthPole - radialAngle;
		double latNorthHemisphereCenter = latNorthPole - (2 * inscribeAngle);
		double latNorthHemisphereCorner = latNorthPole - radialAngle - edgeAngle;
		double latSouthPole = Math.PI / -2.0;
		double latMuEdge = latSouthPole + inscribeAngle;
		double latMuCorner = latSouthPole + radialAngle;
		double latSouthHemisphereCenter = latSouthPole + (2 * inscribeAngle);
		double latSouthHemisphereCorner = latSouthPole + radialAngle + edgeAngle;
		
		double middleLon;
		boolean pointedUp;
		
		if (sector == 0) { // north pole
			centerCoord = new Point2D.Double(0, latNorthPole);
			for (int i = 0; i < 5; i++) {
				cornerCoords[i] = new Point2D.Double((i * 2) * piOver5, latAlphaCorner);
				edgeMidpointCoords[i] = new Point2D.Double((i * 2 + 1) * piOver5, latAlphaEdge);
			}
			pointedUp = true;
		} else if (sector == 11) { // south pole
			centerCoord = new Point2D.Double(0, -Math.PI / 2);
			for (int i = 0; i < 5; i++) {
				cornerCoords[i] = new Point2D.Double((i * 2) * piOver5, latMuCorner);
				edgeMidpointCoords[i] = new Point2D.Double((i * 2 + 1) * piOver5, latMuEdge);
			}
			pointedUp = false;
		} else if (sector >= 1 && sector <= 5) { // northern hemisphere
			middleLon = Math.PI + pi2Over5 * (sector - 1);
			if (middleLon > (2 * Math.PI)) {
				middleLon -= 2 * Math.PI;
			}
			centerCoord = new Point2D.Double(middleLon, latNorthHemisphereCenter);
			cornerCoords[0] = new Point2D.Double(middleLon, latSouthHemisphereCorner);
			cornerCoords[1] = new Point2D.Double(middleLon + piOver5, latNorthHemisphereCorner);
			cornerCoords[2] = new Point2D.Double(middleLon + piOver5, latAlphaCorner);
			cornerCoords[3] = new Point2D.Double(middleLon - piOver5, latAlphaCorner);
			cornerCoords[4] = new Point2D.Double(middleLon - piOver5, latNorthHemisphereCorner);
			edgeMidpointCoords[0] = new Point2D.Double(middleLon + piOver10, 0);
			edgeMidpointCoords[1] = new Point2D.Double(middleLon + piOver5, latAlphaCorner - (0.5 * edgeAngle));
			edgeMidpointCoords[2] = new Point2D.Double(middleLon, latAlphaEdge);
			edgeMidpointCoords[3] = new Point2D.Double(middleLon - piOver5, latAlphaCorner - (0.5 * edgeAngle));
			edgeMidpointCoords[4] = new Point2D.Double(middleLon - piOver10, 0);
			pointedUp = false;
		} else if (sector >= 6 && sector <= 10) { // southern hemisphere
			middleLon = pi2Over5 * (sector - 6);
			if (middleLon > (2 * Math.PI)) {
				middleLon -= 2 * Math.PI;
			}
			centerCoord = new Point2D.Double(middleLon, latSouthHemisphereCenter);
			cornerCoords[0] = new Point2D.Double(middleLon, latNorthHemisphereCorner);
			cornerCoords[1] = new Point2D.Double(middleLon - piOver5, latSouthHemisphereCorner);
			cornerCoords[2] = new Point2D.Double(middleLon - piOver5, latMuCorner);
			cornerCoords[3] = new Point2D.Double(middleLon + piOver5, latMuCorner);
			cornerCoords[4] = new Point2D.Double(middleLon + piOver5, latSouthHemisphereCorner);
			edgeMidpointCoords[0] = new Point2D.Double(middleLon - piOver10, 0);
			edgeMidpointCoords[1] = new Point2D.Double(middleLon - piOver5, latMuCorner + (0.5 * edgeAngle));
			edgeMidpointCoords[2] = new Point2D.Double(middleLon, latMuEdge);
			edgeMidpointCoords[3] = new Point2D.Double(middleLon + piOver5, latMuCorner + (0.5 * edgeAngle));
			edgeMidpointCoords[4] = new Point2D.Double(middleLon + piOver10, 0);
			pointedUp = true;
		} else {
			// impossible sector number
			throw new IllegalArgumentException("Sector index " + sector + " is not allowed. Only sectors 0-11 are allowed");
		}
		
		if(pointedUp){
			// pentagon pointed up
			for(int i = 0; i < 5; i++){
				relPixelCornerCoords[i] = new Point2D.Double((-Math.sin((i*2)*piOver5)+1)*0.5,(Math.cos((i*2)*piOver5)+1)*0.5);
			}
		} else {
			// pentagon pointed down
			double pi = Math.PI;
			for(int i = 0; i < 5; i++){
				relPixelCornerCoords[i] = new Point2D.Double((-Math.sin((i*2)*piOver5+pi)+1)*0.5,(Math.cos((i*2)*piOver5+pi)+1)*0.5);
			}
		}
		for(int i = 0; i < 5; i++){
			int i2 = (i+1)%5;
			relPixelEdgeCoords[i] = new Point2D.Double((relPixelCornerCoords[i].getX()+relPixelCornerCoords[i2].getX())/2,
			(relPixelCornerCoords[i].getY()+relPixelCornerCoords[i2].getY())/2);
		}
		// set relative center
		double cx = 0, cy = 0;
		for(int i = 0; i < 5; i++){
			cx += relPixelCornerCoords[i].getX();
			cy += relPixelCornerCoords[i].getY();
		}
		relCenterCoord = new Point2D.Double(cx/5.0, cy/5.0);
	}
	
	public static SectorMapProjector[] createSectorMaps(){
		SectorMapProjector[] maps = new SectorMapProjector[12];
		for(int i = 0; i < 12; i++){
			maps[i] = new SectorMapProjector(i);
		}
		return maps;
	}
	/**
	 * Computes the relative pixel coordinates from absolute image coordinates, 
	 * flipping the y axis as necessary.
	 * @param x x coordinate of the pixel in the image
	 * @param y y coordinate of the pixel in the image
	 * @param width width of the image/region of interest
	 * @param height height of the image/region of interest
	 * @param xOffset offset from left
	 * @param yOffset offset from top
	 * @return A Point2D that can be passed into the method of this class that 
	 * take relative pixel coordinates.
	 */
	protected static Point2D pixelCoordinateToRelativeCoordinate(int x, int y, int width, int height, int xOffset, int yOffset){
		double relX = (double)(x - xOffset)/(double)width;
		double relY = (double)(y - yOffset)/(double)width;
		return new Point2D.Double(relX, 1-relY);
	}
	
	/**
	 * Creates a 2D map of one sector of the planet. Each sector is 1/12 the 
	 * area of the whole planet
	 * @param planet Planet to map
	 * @param size width and height of the image (square image)
	 * @param painter A PlanetPainter instance to determine the colors on the 
	 * map.
	 * @param ptracker A progress tracker that will be incremented up to a total 
	 * value of 1 as the operation completes (can be null).
	 * @return An image of the planet's map
	 */
	@Override
	public BufferedImage createMapProjection(AbstractPlanet planet, final int size, PlanetPainter painter, DoubleAdder ptracker) {
		final BufferedImage bimg = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
		final double radius = planet.getRadius();
		final double metersPerPixel = (radialAngle*radius)/(0.5*size); // used for scale-bar
		final double precision = 2*metersPerPixel;
		
		double s = 1.0;
		if(drawMap) s += 1;
		if(drawLandHillShading || drawOceanHillShading) s += 1;
		if(drawLabels) s += 1;
		final double partial = 1 / s;
		
		ArrayList<java.util.concurrent.Callable<Boolean>> taskList = new ArrayList<>(size);
		
		if(drawMap){
			for(int fy = 0; fy < size; fy++){
				final int y = fy;
				taskList.add(()->{
				for(int x = 0; x < size; x++){
					if(Thread.interrupted()){
						// ABORT!
						return false;
					}
					Point2D relCoord = pixelCoordinateToRelativeCoordinate(
							x,y,
							bimg.getWidth(),bimg.getHeight(),
							0,0
					);
					if(isInMapArea(relCoord)){
						Point2D lonLat = barycentricLookup(relCoord);
						bimg.setRGB(x, y, painter.getColor(planet, lonLat.getX(), lonLat.getY(), precision, x, y));
					}
				}
				if(ptracker!= null) ptracker.add(partial / size);
				return true;
				});
			}
			
			ForkJoinPool.commonPool().invokeAll(taskList);
			taskList.clear();
			if(Thread.currentThread().isInterrupted()){
				// ABORT!
				return null;
			}
			
		}
		if(drawLandHillShading || drawOceanHillShading){
			// draw shaders
			double[][] heightMap = new double[size][size];
			for(int fy = 0; fy < size; fy++){
				final int y = fy;
				taskList.add(()->{
				for(int x = 0; x < size; x++){
					if(Thread.currentThread().isInterrupted()){
						// ABORT!
						return null;
					}
					Point2D relCoord = pixelCoordinateToRelativeCoordinate(
							x,y,
							bimg.getWidth(),bimg.getHeight(),
							0,0
					);
					Point2D lonLat = barycentricLookup(relCoord);
					double h = planet.getAltitude(lonLat.getX(), lonLat.getY(), precision);
					if(drawLandHillShading == false && h >= 0){
						heightMap[x][size-y-1] = 0; // flip Y because the image origin is in upper left corner instead of bottom left
					} else if(drawOceanHillShading == false && h < 0){
						heightMap[x][size-y-1] = 0;
					} else {
						heightMap[x][size-y-1] = h;
					}
				}
				if(ptracker!= null) ptracker.add(partial / size);
				return true;
				});
			}
			BufferedImage shader = TextureHelper.generateSimpleHillShader(heightMap);
			for(int y = 0; y < size; y++){
				if(Thread.currentThread().isInterrupted()){
					// ABORT!
					return null;
				}
				for(int x = 0; x < size; x++){
					Point2D relCoord = pixelCoordinateToRelativeCoordinate(
							x,y,
							bimg.getWidth(),bimg.getHeight(),
							0,0
					);
					if(isInMapArea(relCoord) == false){
						shader.setRGB(x, y, 0x00000000);
					}
				}
			}
			ForkJoinPool.commonPool().invokeAll(taskList);
			taskList.clear();
			if(Thread.currentThread().isInterrupted()){
				// ABORT!
				return null;
			}
			bimg.createGraphics().drawImage(shader, 0, 0, null);
		}
		
		if(Thread.currentThread().isInterrupted()){
			// ABORT!
			return null;
		}
		if(drawLabels){
			drawLabels(planet,size,painter,bimg);
			if(ptracker!= null) ptracker.add(partial);
		}
		
		
		return bimg;
	}
	/**
	 * Draws extra labels on the image.
	 * @param planet
	 * @param size
	 * @param painter
	 * @param baseMap 
	 */
	protected void drawLabels(AbstractPlanet planet, int size, PlanetPainter painter, BufferedImage baseMap) {
		boolean pointedUp = (this.sector == 0 || (this.sector > 5 && this.sector < 11));
		
		final double radius = planet.getRadius();
		final double metersPerPixel = (radialAngle * radius) / (0.5 * size); // used for scale-bar
		final double kmPerPixel = metersPerPixel / 1000.0;
		double maxScaleKm = 0.45 * kmPerPixel * size;
		double scaleKm = Math.pow(10, Math.floor(Math.log10(maxScaleKm)));
		// decorations
		Graphics2D g = baseMap.createGraphics();

		// make scale-bar
		int scalebarPixelSize = (int) (scaleKm / kmPerPixel);
		int xOffset = 2, yOffset = 2, height = 8;
		if (sector == 0 || (sector >= 6 && sector <= 10)) {
			yOffset += baseMap.getHeight() - (2 * height + 2);
		}
		g.setColor(Color.WHITE);
		g.fillRect(xOffset - 1, yOffset - 1, scalebarPixelSize  + 2,  height + 2);
		g.setColor(Color.BLACK);
		g.fillRect(xOffset, yOffset, scalebarPixelSize , height);
		g.setFont(Font.decode("Tahoma PLAIN 8"));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.WHITE);
		g.drawString(" " + ((float)scaleKm) + "km", xOffset , yOffset + height - 1);
		
		if(sector == 0){
			drawNeighborSectorLabels(pointedUp,g,size,Arrays.copyOfRange(SECTOR_LABELS, 1, 6));
		} else if(sector < 6){
			String[] labels = new String[5];
			labels[0] = SECTOR_LABELS[0];
			labels[1] = SECTOR_LABELS[(sector+3)%5+1];
			labels[4] = SECTOR_LABELS[(sector)%5+1];
			labels[2] = SECTOR_LABELS[(sector+1)%5+6];
			labels[3] = SECTOR_LABELS[(sector+2)%5+6];
			drawNeighborSectorLabels(pointedUp,g,size,labels);
		} else if(sector < 11){
			String[] labels = new String[5];
			labels[0] = SECTOR_LABELS[11];
			labels[1] = SECTOR_LABELS[(sector)%5+6];
			labels[4] = SECTOR_LABELS[(sector+3)%5+6];
			labels[2] = SECTOR_LABELS[(sector+2)%5+1];
			labels[3] = SECTOR_LABELS[(sector+1)%5+1];
			drawNeighborSectorLabels(pointedUp,g,size,labels);
		} else {
			drawNeighborSectorLabels(pointedUp,g,size,SECTOR_LABELS[6],SECTOR_LABELS[10],SECTOR_LABELS[9],SECTOR_LABELS[8],SECTOR_LABELS[7]);
		}
	}
	
	
	
	private void drawNeighborSectorLabels(boolean pointIsUp, Graphics2D g, int imgSize, String... labels){
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.BLACK);
		g.setFont(Font.decode("Arial BOLD 12"));
		double rotation = 2 * Math.PI / 5;
		double angle;
		double factor = 0.9;
		float xOffset = 0.0f, yOffset = 6.0f;
		if(pointIsUp){
			angle = Math.PI;
		} else {
			angle = 0;
		}
		for(int i = 0; i < 5; i++){
			float x = (float)(imgSize - (Math.sin(angle) * (factor*imgSize / 2) + (imgSize / 2)))+xOffset;
			float y = (float)(imgSize - (Math.cos(angle) * (factor*imgSize / 2) + (imgSize / 2)))+yOffset;
			g.drawString(labels[i], x, y);
			angle += rotation;
		}
	}
	
	private String[] reverseArray(String[] in){
		String[] out = new String[in.length];
		for(int i = 0; i < in.length; i++){
			out[out.length - i - 1] = in[i];
		}
		return out;
	}
	/**
	 * Determines whether or not a given point is within the pentagon of the 
	 * sector map.
	 * @param relativePixelCoordinate Coordinates of the pixel to look-up, 
	 * normalize such that the <b>bottom-left</b> corner of the image is 
	 * <b>(0,0)</b> and the <b>top-right</b> corner is <b>(1,1)</b>
	 * @return True if the coordinate lies within the pentagon, false otherwise
	 */
	protected boolean isInMapArea(Point2D relativePixelCoordinate){
		for(int i = 0; i < 5; i++){
			int i2 = (i+1)%5;
			double dy = (relPixelCornerCoords[i2].getY() - relPixelCornerCoords[i].getY());
			double dx = (relPixelCornerCoords[i2].getX() - relPixelCornerCoords[i].getX());
			double slope = dy/dx;
			double yValOnLine = slope * (relativePixelCoordinate.getX() - relPixelCornerCoords[i].getX()) 
					+ relPixelCornerCoords[i].getY();
			if(dx > 0 && relativePixelCoordinate.getY() < yValOnLine){
				return false;
			} else if (dx < 0 && relativePixelCoordinate.getY() > yValOnLine){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Performs a barycentric interpolation to get the lon,lat coordinate 
	 * corresponding to a given pixel (given as relative pixel coordinate).
	 * @param relativePixelCoordinate Coordinates of the pixel to look-up, 
	 * normalize such that the <b>bottom</b>-left corner of the image is (0,0) 
	 * and the top-right corner is (1,1)
	 * @return A Point2D object holding the interpolated (longitude,latitude) 
	 * spherical coordinate
	 */
	protected Point2D.Double barycentricLookup(Point2D relativePixelCoordinate){
		
		
		
		int closestCorner = 0;
		int closestEdgeMidpoint = 0;
		
		double d1 = Double.MAX_VALUE;
		double d2 = Double.MAX_VALUE;
		for(int i = 0; i < 5; i++){
			if(relativePixelCoordinate.distanceSq(relPixelCornerCoords[i]) < d1){
				closestCorner = i;
				d1 = relativePixelCoordinate.distanceSq(relPixelCornerCoords[i]);
			}
			if(relativePixelCoordinate.distanceSq(relPixelEdgeCoords[i]) < d2){
				closestEdgeMidpoint = i;
				d2 = relativePixelCoordinate.distanceSq(relPixelEdgeCoords[i]);
			}
		}
		
		// weight each corner by the area formed by the test point and other two corners of the triangle
		double[] lons = new double[3];
		lons[0] = centerCoord.getX();
		lons[1] = cornerCoords[closestCorner].getX();
		lons[2] = edgeMidpointCoords[closestEdgeMidpoint].getX();
		double[] lats = new double[3];
		lats[0] = centerCoord.getY();
		lats[1] = cornerCoords[closestCorner].getY();
		lats[2] = edgeMidpointCoords[closestEdgeMidpoint].getY();
		double[] areas = new double[3];
		areas[0] = triangleArea(relativePixelCoordinate.getX(), relativePixelCoordinate.getY(),
				relPixelCornerCoords[closestCorner].getX(), relPixelCornerCoords[closestCorner].getY(), 
				relPixelEdgeCoords[closestEdgeMidpoint].getX(), relPixelEdgeCoords[closestEdgeMidpoint].getY()
				);
		areas[1] = triangleArea(relativePixelCoordinate.getX(), relativePixelCoordinate.getY(),
				relCenterCoord.getX(), relCenterCoord.getY(), 
				relPixelEdgeCoords[closestEdgeMidpoint].getX(), relPixelEdgeCoords[closestEdgeMidpoint].getY()
				);
		areas[2] = triangleArea(relativePixelCoordinate.getX(), relativePixelCoordinate.getY(),
				relPixelCornerCoords[closestCorner].getX(), relPixelCornerCoords[closestCorner].getY(), 
				relCenterCoord.getX(), relCenterCoord.getY()
				);
		double sum = areas[0]+areas[1]+areas[2];
		double lon = 0;
		double lat = 0;
		if (sector == 0 || sector == 11) {
			// special edge-case: polar sectors
			for (int i = 0; i < 3; i++) {
				lon += lons[i] * areas[i] / sum;
				lat += lats[i] * areas[i] / sum;
			}
			if (relativePixelCoordinate.getX() == relCenterCoord.getX()) {
				if (relativePixelCoordinate.getY() > relCenterCoord.getY()) {
					lon = 0;
				} else {
					lon = Math.PI;
				}
			} else {
				if (this.sector == 0) {
					lon = Math.atan2(relativePixelCoordinate.getY() - relCenterCoord.getY(), relativePixelCoordinate.getX() - relCenterCoord.getX()) - 0.5 * Math.PI;
				} else if (this.sector == 11) {
					lon = -Math.atan2(relativePixelCoordinate.getY() - relCenterCoord.getY(), relativePixelCoordinate.getX() - relCenterCoord.getX()) + 0.5 * Math.PI;
				}
			}
		} else {
			for (int i = 0; i < 3; i++) {
				lon += lons[i] * areas[i] / sum;
				lat += lats[i] * areas[i] / sum;
			}
		}
		return new Point2D.Double(lon, lat);
	}
	/**
	 * returns the area of a triangle from 3 x,y coordinates
	 * @param x1 coordinate of a corner of the triangle
	 * @param y1 coordinate of a corner of the triangle
	 * @param x2 coordinate of a corner of the triangle
	 * @param y2 coordinate of a corner of the triangle
	 * @param x3 coordinate of a corner of the triangle
	 * @param y3 coordinate of a corner of the triangle
	 * @return The area of the triangle
	 */
	protected double triangleArea(double x1, double y1, double x2, double y2, double x3, double y3){
		double dx12 = x2-x1;
		double dx13 = x3-x1;
		double dy12 = y2-y1;
		double dy13 = y3-y1;
		double m12 = Math.sqrt(dx12*dx12+dy12*dy12);
		double m13 = Math.sqrt(dx13*dx13+dy13*dy13);
		double dotProduct = dx12 * dx13 + dy12 * dy13;
		double d = (m12*m13);
		if(d == 0) return 0; // two points are right on top of each other
		double angle = acos(dotProduct / d);
		return 0.5*(m12 * Math.sin(angle) * m13);
	}
	/**
	 * Provided for optimization purposes.
	 * @param x 
	 * @return Returns Math.acos(x)
	 */
	protected double acos(double x){
		return Math.acos(x);
	}
	
	
	/**
	 * Gets the symbol for this sector.
	 * @return A greek letter designating this sector
	 */
	public String getSymbol() {
		return SECTOR_LABELS[this.sector];
	}
	/**
	 * Gets the sector number for this sector.
	 * @return A number from 0 to 11 (inclusive).
	 */
	public int getSectorNumber(){
		return sector;
	}
	
	
	@Deprecated public static void main(String[] args){
		final int size = 256;
		AbstractPlanet p = new hall.collin.christopher.worldgeneration.SimpleRandomPlanet("Testing...");
		SectorMapProjector[] sectorMaps = SectorMapProjector.createSectorMaps();
		PlanetPainter pp = new VegetationPainter();
		for(int i = 0; i < sectorMaps.length; i++){
			SectorMapProjector s = sectorMaps[i];
			s.enableUnderwaterShader(false);
			s.enableLandShader(false);
			hall.collin.christopher.worldgeneration.util.GUI.showImagePopupNonmodal(s.createMapProjection(p, size, pp),"Sector "+SECTOR_LABELS[i]);
		}
	}
}
