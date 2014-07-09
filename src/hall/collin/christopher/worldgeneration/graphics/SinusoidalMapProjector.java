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
import java.io.IOException;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * A sinusoidal maps longitude and latitude directly to the x and y axese, but 
 * preserves the relative sizes of features. However, it greatly distorts the 
 * shape of map features.
 * @author CCHall
 */
public class SinusoidalMapProjector extends PlanetaryMapProjector{
	/** default constructor */
	public SinusoidalMapProjector(){
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
	public BufferedImage createMapProjection(AbstractPlanet planet, int size, PlanetPainter painter, DoubleAdder ptracker) {
	
		final double pinc = 1.0/size/2;
		
		int width = size*2;
		int height = size;
		double precision = 1024;
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width,height,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.image.BufferedImage shader = new java.awt.image.BufferedImage(width,height,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		
		Double[][] heighMap = new Double[width][height];
		
		for(int y = 0; y < height; y++){
			double lat = ((double)y/(double)height - 0.5)*Math.PI;
			double circumference = Math.cos(lat) * width;
			int row = (int)circumference;
			int offset = (width - row) / 2;
			for(int x = offset; x < row+offset; x++){
					if(Thread.currentThread().isInterrupted()){
						// ABORT!
						return null;
					}
				double lon = ((double)(x-offset)/circumference)*2*Math.PI;
				heighMap[x][height - y - 1] = planet.getAltitude(lon, lat, precision);
				img.setRGB(x, y, painter.getColor(planet, lon, lat, precision, x, y));
			}
			if(ptracker!= null) ptracker.add(pinc);
		}
		java.awt.Graphics2D brush = img.createGraphics();
		
		
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
			if(ptracker!= null) ptracker.add(pinc);
		}
		
		brush.drawImage(shader, 0, 0, null);
		
		return img;
	}
	
	@Deprecated public static void main(String[] args){
		
		
		String seed = "The world is not enough";
		
		PlanetaryMapProjector p = new SinusoidalMapProjector();
		
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
