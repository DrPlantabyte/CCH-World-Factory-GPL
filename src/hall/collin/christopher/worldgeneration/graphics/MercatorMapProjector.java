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
import java.util.ArrayList;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * A Mercator map is the simplest map projection. It is a cylindrical projection 
 * where the y axis is mapped directly to latitude and the x axis is mapped 
 * directly to longitude. This style of map greatly distorts the sizes of 
 * features, making features that are farther from the equator appear much 
 * larger.
 * @author CCHall
 */
public class MercatorMapProjector extends PlanetaryMapProjector{
	private boolean doHillshading = true;
	private boolean doOceanHillshading = false;
	private boolean doPainting = true;
	/**
	 * Sets whether or not to enable hill-shading. Hill-shading calculation 
	 * is CPU intensive and will look much better if done on the GPU instead.
	 * @param set 
	 */
	public void enableHillshading(boolean set){
		doHillshading = set;
	}
	/**
	 * Sets whether or not to enable hill-shading <b>underwater</b>. 
	 * Hill-shading calculation is CPU intensive and will look much better if 
	 * done on the GPU instead.
	 * @param set 
	 */
	public void enableOceanshading(boolean set){
		doOceanHillshading = set;
	}
	/**
	 * Sets whether or not to draw the map itself. If false, then only the 
	 * hill-shading layer will be drawn.
	 * @param set 
	 */
	public void enableMap(boolean set){
		doPainting = set;
	}
	/** default constructor */
	public MercatorMapProjector(){
		//
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
	public BufferedImage createMapProjection(final AbstractPlanet planet, int size, PlanetPainter painter, DoubleAdder ptracker) {
		final int width = size*2;
		final int height = size;
		
		double hm =1;
		if(doHillshading || doOceanHillshading)hm = 0.5;
		final double pinc = 1.0/height*hm;
		final double precision = 1024;
		final java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width,height,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		final java.awt.image.BufferedImage shader = new java.awt.image.BufferedImage(width,height,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		
		final double[][] heightMap = new double[width][height];
		
		ArrayList<java.util.concurrent.Callable<Boolean>> taskList = new ArrayList<>(height);
		
		for(int y = 0; y < height; y++){
			final double lat = -1*((double)y/(double)height - 0.5)*Math.PI;
			final int py = height - y - 1;
			final int fy = y;
			taskList.add(() -> {
			for(int x = 0; x < width; x++){
				if(Thread.interrupted()){
					// ABORT!
					return false;
				}
				double lon = ((double)x/(double)width)*2*Math.PI;
				if(doHillshading || doOceanHillshading){
					heightMap[x][py] = planet.getAltitude(lon, lat, precision);
					if((doOceanHillshading == false) && heightMap[x][py] < 0)heightMap[x][py] = 0;
					if((doHillshading == false) && heightMap[x][py] > 0)heightMap[x][py] = 0;
				}
				if(doPainting){img.setRGB(x, fy, painter.getColor(planet, lon, lat, precision, x, fy));}
			}
			if(ptracker!= null) ptracker.add(pinc);
			return true;
			});
		}
		java.util.concurrent.ForkJoinPool.commonPool().invokeAll(taskList);
		taskList.clear();
		
		
		// calculate hill-shading
		if(doHillshading || doOceanHillshading){
		for(int y = 0; y < height; y++){
			int fy = y;
			taskList.add(() -> {
			for(int x = 0; x < width; x++){
				if(Thread.interrupted()){
					// ABORT!
					return false;
				}
				int xl = x - 1;
				if(xl < 0){xl = 0;}
				int xr = x+1;
				if(xr >= width){xr = (width-1);}
				int yb = fy - 1;
				if(yb < 0){yb = 0;}
				int yt = fy+1;
				if(yt >= height){yt = (height-1);}
				double slopey = heightMap[x][yb] - heightMap[x][yt];
				double slopex = heightMap[xr][fy] - heightMap[xl][fy];
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
					shader.setRGB(x, height-fy-1, new java.awt.Color(0xFF, 0xFF, 0xFF, alpha).getRGB());
				} else {
					shader.setRGB(x, height-fy-1, new java.awt.Color(0x00, 0x00, 0x00, alpha).getRGB());
				}
			}
			if(ptracker!= null) ptracker.add(pinc);
			return true;});
		}
		
		
		java.util.concurrent.ForkJoinPool.commonPool().invokeAll(taskList);
		if(Thread.currentThread().isInterrupted()){
			// ABORT!
			return null;
		}
		
		java.awt.Graphics2D brush = img.createGraphics();
		brush.drawImage(shader, 0, 0, null);
		}
		return img;
	}
	
	@Deprecated public static void main(String[] args){
		
		String seed = "The world is not enough";
		
		PlanetaryMapProjector p = new MercatorMapProjector();
		
		int size = 300;
		AbstractPlanet w = new SimpleRandomPlanet(seed);
		
		BufferedImage img = p.createMapProjection(w, size, (new StandardBiomeFactory()).getPlanetPainter());
		javax.swing.JOptionPane.showMessageDialog(null, new javax.swing.JLabel(new javax.swing.ImageIcon(img)));
	//	try {
	//		ImageIO.write(img, "png", new java.io.File(seed+"-"+p.getClass().getSimpleName()+".png"));
	//	} catch (IOException ex) {
	//		Logger.getLogger(MercatorMapProjector.class.getName()).log(Level.SEVERE, null, ex);
	//	}
	}
	
}
