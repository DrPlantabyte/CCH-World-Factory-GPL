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
package hall.collin.christopher.worldgeneration.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Altitude color look-up class.
 * @author CCHall
 */
public class AltitudeColorizer {

	/**
	 * Looks-up the color for a given altitude
	 * @param altitude Altitude, in meters. Negative values for ocean of a 
	 * given depth.
	 * @return The color, as an ARGB integer
	 */
	public static int getColor(float altitude){
		if(altitude > 1) altitude = 1;
		if(altitude < -1) altitude = -1;
		float red;
		float green;
		float blue;
		if(altitude < 0){
			// ocean colors
			red = 0;
			blue = 1;
			green = 1 + altitude;
		} else {
			if(altitude < 0.25){
				float h = 4 * altitude;
				blue = 0;
				red = 1 - h;
				green = 1;
			} else if (altitude < 0.5) {
				float h = 4 * (altitude - 0.25f);
				blue = h / 4;
				red = 0;
				green = 1 - (h / 2);
			} else if (altitude < 0.75) {
				float h = 4 * (altitude - 0.5f);
				float[] hsbvals = new float[3];
				Color.RGBtoHSB(0, 127, 63, hsbvals);
				float hue = hsbvals[0];
				float sat = hsbvals[1];
				float val = hsbvals[2];
				sat *= (1f - h);
				return Color.HSBtoRGB(hue, sat, val);
			} else {
				float h = 4 * (altitude - 0.75f);
				float[] hsbvals = new float[3];
				Color.RGBtoHSB(0, 127, 63, hsbvals);
				float hue = hsbvals[0];
				float sat = 0;
				float val = (1f - hsbvals[2]) * h + hsbvals[2];
				return Color.HSBtoRGB(hue, sat, val);
			}
		}
		
		return (new Color(red,green,blue)).getRGB();
	}
	/**
	 * @param args the command line arguments
	 */
	@Deprecated 
	public static void main(String[] args) {
		final int width = 400;
		final int height = 32;
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for(int x = 0; x < width; x++){
			float h = ((float)x / (float)width)*2 - 1f;
			for(int y = 0; y < height; y++){
				img.setRGB(x, y, getColor(h));
			}
		}
		showImage(img);
	}
	
	private static void showImage(final BufferedImage img){
		javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run(){
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(new JLabel(new ImageIcon(img)));
		frame.pack();
		frame.setVisible(true);
		}});
	}
}
