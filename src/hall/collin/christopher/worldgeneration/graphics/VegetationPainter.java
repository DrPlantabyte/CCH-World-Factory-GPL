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
import hall.collin.christopher.worldgeneration.math.BarycentricInterpolator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * A planet painter that uses simple temperature and moisture interpolation to
 * color the planet. It is intended to roughly approximate what the terrain 
 * would look like from afar.
 *
 * @author CCHall
 */
public class VegetationPainter extends PlanetPainter {

	private final int oceanColor =  0xFF0040FF;
	private final int seaIceColor = 0xFFD7FFFF;
	private final double[] rgb_desert = {255.0, 213.0, 0.0};
	private final double[] rgb_jungle = {0.0, 174.0, 0.0};
	private final double[] rgb_colddry = {240.0, 240.0, 255.0};
	private final double[] rgb_coldwet = {240.0, 240.0, 255.0};
	private final double[] rgb_moon = {140.0, 140.0, 140.0};

	public VegetationPainter() {
		// nothing to do
	}

	/**
	 * Gets the base color (no hill-shading) for a single pixel on a map of the 
	 * planet at the given latitude and longitude. The pixel X and Y coordinates 
	 * are provided for texturing (e.g. 
	 * <code>return textureImage.getRGB(pixelX % textureImage.getWidth(), pixelY % textureImage.getHeight());</code> )
	 * @param planet The planet to colorize
	 * @param longitude Longitude coordinate of the location of interest
	 * @param latitude Latitude of the location of interest
	 * @param precision Determines how fine-grained the calculation is. E.g. if 
	 * <code>precision</code> is 10km, then the planet generation implementation 
	 * will return a value that is roughly the average of a 10km radius around 
	 * the given coordinate. If making a map from a grid of data points, set 
	 * the precision to the grid spacing.
	 * @param pixelX X component of the pixel coordinate on the output map 
	 * image. This is useful for texturing.
	 * @param pixelY Y component of the pixel coordinate on the output map 
	 * image. This is useful for texturing.
	 * @return An ARGB pixel integer representing the color to represent the 
	 * given location on the planet (without hill-shading, just the base color).
	 */
	@Override
	public int getColor(AbstractPlanet planet, double longitude, double latitude, double precision, int pixelX, int pixelY) {
		if (planet.getAltitude(longitude, latitude, precision) < 0) {
			if(planet.getTemperature(longitude, latitude, precision) < -10) {
				return seaIceColor;
			} else {
				return oceanColor;
			}
		}
		return interpolateColor(planet.getMoisture(longitude, latitude, precision), planet.getTemperature(longitude, latitude, precision));
	}

	@Deprecated
	public static void main(String[] args) {
		int width = 600;
		int height = 600;
		double tempLow = -20;
		double tempHigh = 70;
		double moistureLow = -50;
		double moistureHigh = 200;
		BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		VegetationPainter p = new VegetationPainter();
		for (int x = 0; x < width; x++) {
			double moisture = ((double)x / width) * (moistureHigh-moistureLow) + moistureLow;
			for (int y = 0; y < height; y++) {
			double temp = ((double)(height - y - 1) / height) * (tempHigh-tempLow) + tempLow;
				bimg.setRGB(x, y, p.interpolateColor(moisture, temp));
			}
		}
		JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(bimg)));
	}

	double tempMin = -10;
	double tempMax = 10;
	double moistureMin = -5;
	double moistureMax = 150;

	private int interpolateColor(double moisture, double temperature) {

		double mw = (moisture + 5) / (moistureMax - moistureMin);
		double tw = (temperature + 5) / (tempMax - tempMin);
		double[] hot = interpolate(rgb_desert, rgb_jungle, mw);
		double[] cold = interpolate(rgb_colddry, rgb_coldwet, mw);
		double[] colors = interpolate(cold, hot, tw);
		if(temperature > 30){
			// interpolate very hot temperatures
			double weight = Math.min( (temperature - 30.0) / 40.0 ,1);
			colors = interpolate(colors, rgb_moon, weight);
		}
		if(moisture < -20){
			// interpolate very cold temperatures
			double weight = Math.min( (-20 - moisture ) / 30.0 ,1);
			colors = interpolate(colors, rgb_moon, weight);
		}
		int[] rgb = new int[3];
		for (int i = 0; i < 3; i++) {
			rgb[i] = Math.max(Math.min((int) colors[i], 0xFF), 0);
		}
		int px = 0xFF000000 | (rgb[0] << 16) | (rgb[1] << 8) | (rgb[2]);
		return px;
	}

	private double[] interpolate(double[] rgb0, double[] rgb1, double w) {
		double[] rgb = new double[3];
		for (int i = 0; i < 3; i++) {
			rgb[i] = (1 - w) * rgb0[i] + w * rgb1[i];
		}
		return rgb;
	}

}
