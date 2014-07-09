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
import hall.collin.christopher.worldgeneration.SimpleRandomPlanet;
import hall.collin.christopher.worldgeneration.biomes.StandardBiomeFactory;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Fuller's Dymaxion projection maps a sphere to an un-folded icosahedron. The 
 * resulting map preserves both the size and shape of map features, but the 
 * orientation of the map is not intuitive. In this implementation, the map is 
 * displayed such that "up" is always North, "down" is always south, "left" is 
 * always West, and "right" is always East.
 * @author CCHall
 */
public class FullerDymaxionMapProjector extends PlanetaryMapProjector{
	/**
	 * Default constructor
	 */
	public FullerDymaxionMapProjector(){
		// nothing to do
	}
	/**
	 * Creates a 2D map of the planet.
	 * @param planet Planet to map
	 * @param size size of the image (typically the height of the image)
	 * @param painter A PlanetPainter instance to determine the colors on the 
	 * map.
	 * @param ptracker A progress tracker that will be incremented up to a total 
	 * value of 1 as the operation completes (can be null).
	 * @return An image of the planet's map
	 */
	@Override
	public BufferedImage createMapProjection(AbstractPlanet planet, int size, PlanetPainter painter, DoubleAdder ptracker) {
	
		double precision = 1024;
		
		final double pinc = 1.0/size;
		
		/*
/\/\/\/\/\
\         \
 \/\/\/\/\/

width = 5.5X;
height = 3(0.5X*sqrt(3)) = 1.5sqrt(3)X;
X = (height/1.5sqrt(3))
		 */
		double temp = size / (1.5 * Math.sqrt(3));
		final int width = (int)Math.round(5.5 * temp);
		final int height = size;
		
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width,height,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.image.BufferedImage shader = new java.awt.image.BufferedImage(width,height,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		Double[][] heighMap = new Double[width][height];
		
	/*	// horizontal guidelines
		final int numGuideLines = 20;
		final int gdy = height / numGuideLines;
		for(int dy = 0; dy < (height / 2); dy++){
			for(int px = 0; px < width; px++){
				if(dy == 0){
					img.setRGB(px, (height / 2), Color.BLACK.getRGB());
				} else {
					if(dy % gdy == 0 && px % 2 == 0){
						img.setRGB(px, (height / 2) + dy, Color.BLACK.getRGB());
						img.setRGB(px, (height / 2) - dy, Color.BLACK.getRGB());
					}	
				}
			}
		}
		*/
		
		
			double side =  ((double)size / 3.0) / Math.sqrt(3) * 2; // triangle side length in pixels
			double pio2 = Math.PI / 2d;
		
			double firstCenterLon = Math.PI / 5 - Math.PI; // the longitude at the peak of the left-most triangle
			double latPerPx = Math.PI / height;
			// Top row, 5 triangles, left aligned
		/*
			 *  /\  /\  /\  /\  /\
			 * /__\/__\/__\/__\/__\
			 * 
			 * */
			for (int py = 0; py < (height / 3); py++) {

				

				double latitude = pio2 - py * latPerPx;
				double triangleWidth = 2 * py / Math.sqrt(3);
				double xoffset = side * 0.5;
				double centerLon = firstCenterLon;
				double lonPerPx = 2 * Math.PI / 5 / triangleWidth;
				for (int t = 0; t < 5; t++) {
					for (int dpx = (int) (-0.5 * triangleWidth); dpx <= 0.5 * triangleWidth; dpx++) {
					if(Thread.currentThread().isInterrupted()){
						// ABORT!
						return null;
					}
						if (triangleWidth <= 0) {
							continue;
						}
						int px = (int) xoffset + dpx;
						

						double longitude = centerLon + lonPerPx * dpx;
						if (px < 0) {
							continue;
						}
						heighMap[px][height - py - 1] = planet.getAltitude(longitude, latitude, precision);
						img.setRGB(px, py, painter.getColor(planet, longitude, latitude, precision, px, py));
					}
					centerLon += 2 * Math.PI / 5;
					xoffset += side;
				}
				if(ptracker!= null) ptracker.add(pinc);
			}
			// next row: 10 triangles (treated here as a parallelagram)
		/*
			 *   __  __  __  __  __ 
			 *  \                   \
			 *   \ __  __  __  __  __\
			 *    
			 */
			int yoffset = (int) (height / 3);
			for (int dpy = 0; (dpy+yoffset) < (2*height / 3); dpy++) {
				int py = dpy + yoffset;
				
				double latitude = pio2 - py * latPerPx;
				// slope of line (in pixels) is -(sqrt(3))
				int xoffset = (int) Math.round(dpy / Math.sqrt(3));
				double lonPerPx = 2 * Math.PI / (5 * side);
				int length = (int) (5 * side);
				double longitude = firstCenterLon - (lonPerPx * (side / 2)) + (lonPerPx * xoffset);
				for (int dpx = 0; dpx < length; dpx++) {
					if(Thread.currentThread().isInterrupted()){
						// ABORT!
						return null;
					}
					int px = xoffset + dpx;
					heighMap[px][height - py - 1] = planet.getAltitude(longitude, latitude, precision);
						img.setRGB(px, py, painter.getColor(planet, longitude, latitude, precision, px, py));

					longitude += lonPerPx;
				}
				if(ptracker!= null) ptracker.add(pinc);
			}

			// last row: 5 triangles, right aligned
		/*
			 *  __  __  __  __  __ 
			 * \  /\  /\  /\  /\  /
			 *  \/  \/  \/  \/  \/ 
			 * 
			 * */
			yoffset = (int) (height * 2 / 3);
			for (int dpy = 0; (dpy+yoffset) < height; dpy++) {

				int py = dpy + yoffset;
				double latitude = pio2 - py * latPerPx;
				double triangleWidth = side - 2 * dpy / Math.sqrt(3);
				double xoffset = side;
				double centerLon = firstCenterLon + Math.PI / 5;
				double lonPerPx = 2 * Math.PI / 5 / triangleWidth;
				for (int t = 0; t < 5; t++) {
					for (int dpx = (int) (-0.5 * triangleWidth); dpx <= 0.5 * triangleWidth; dpx++) {
					if(Thread.currentThread().isInterrupted()){
						// ABORT!
						return null;
					}

						int px = (int) xoffset + dpx;
						double longitude = centerLon + lonPerPx * dpx;
						if (px < 0) {
							continue;
						}
						heighMap[px][height - py - 1] = planet.getAltitude(longitude, latitude, precision);
						img.setRGB(px, py, painter.getColor(planet, longitude, latitude, precision, px, py));
					}
					centerLon += 2 * Math.PI / 5;
					xoffset += side;
				}
				if(ptracker!= null) ptracker.add(pinc);
			}
	//	mapper.computeDirectly();
		
	
		
		
		java.awt.Graphics2D brush = img.createGraphics();
		
					if(Thread.currentThread().isInterrupted()){
						// ABORT!
						return null;
					}
		
		// calculate hill-shading
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
					if(Thread.currentThread().isInterrupted()){
						// ABORT!
						return null;
					}
				if(heighMap[x][y] == null){
					continue;
				}
				int xl = x - 1;
				if (xl < 0 || heighMap[xl][y] == null) {
					xl = x;
				}
				int xr = x + 1;
				if (xr >= width || heighMap[xr][y] == null) {
					xr = x;
				}
				int yb = y - 1;
				if (yb < 0 || heighMap[x][yb] == null) {
					yb = y;
				}
				int yt = y + 1;
				if (yt >= height || heighMap[x][yt] == null) {
					yt = y;
				}
				double slopey = heighMap[x][yb] - heighMap[x][yt];
				double slopex = heighMap[xr][y] - heighMap[xl][y];
				double slopel  = 0.7*slopey + 0.3*slopex;
				boolean light;
				if(slopel > 0){
					light = true;
				} else {
					light = false;
				}
				int alpha = (int)(Math.abs(slopel) * 0.002 * 127);
				if(alpha > 127){
					alpha = 127;
				}
				if(light){
					shader.setRGB(x, height-y-1, new java.awt.Color(0xFF, 0xFF, 0xFF, alpha).getRGB());
				} else {
					shader.setRGB(x, height-y-1, new java.awt.Color(0x00, 0x00, 0x00, alpha).getRGB());
				}
			}
		}
		
		brush.drawImage(shader, 0, 0, null);
		
		return img;
	}
	
	@Deprecated public static void main(String[] args){
		
		
		String seed = "The world is not enough";
		
		
		PlanetaryMapProjector p = new FullerDymaxionMapProjector();
		
		int size = 300;
		AbstractPlanet w = new SimpleRandomPlanet(seed);
		
		BufferedImage img = p.createMapProjection(w, size,(new StandardBiomeFactory()).getPlanetPainter());
		javax.swing.JOptionPane.showMessageDialog(null, new javax.swing.JLabel(new javax.swing.ImageIcon(img)));
	//	try {
	//		javax.imageio.ImageIO.write(img, "png", new java.io.File(seed+"-"+p.getClass().getSimpleName()+".png"));
	//	} catch (java.io.IOException ex) {
	//		java.util.logging.Logger.getLogger(MercatorMapProjector.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	//	}
	}

	
//	Map<Thread, AtomicInteger> watcherMap;
	
}
