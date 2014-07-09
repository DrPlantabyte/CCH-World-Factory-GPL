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

import hall.collin.christopher.worldgeneration.util.ImageUtils;
import hall.collin.christopher.worldgeneration.biomes.StandardBiomeFactory;
import hall.collin.christopher.worldgeneration.graphics.*;
import hall.collin.christopher.worldgeneration.math.DefaultRandomNumberGenerator;
import java.io.IOException;

/**
 * A planet implementation that is a barren, lifeless rock.
 * @author CCHall
 */
public class RandomMoon extends AbstractPlanet{

	
	final double radius;
	
	
	/** Noise layer for altitude */
	final PlanetaryScaling3DCoordinateNoiseGenerator altitudeLayer;
	/** Noise layer for altitude */
	final PlanetaryScaling3DCoordinateNoiseGenerator tectonicLayer;
	
	/** Precision of the lowest noise frequency, in meters */
	final double minPrecision = 2300024;
	/** magnitude of altitude variation */
	final double altitudeRange = 2000;
	
	private final double tectFactor = 2.0;
	/**
	 * Creates a new moon from a text seed and given radius (in meters).
	 * @param seed Usually the name of the moon.
	 * @param radius The radius of the moon, in <b>meters</b>.
	 */
	public RandomMoon(String seed, double radius){
		DefaultRandomNumberGenerator prng = new DefaultRandomNumberGenerator(
				stringHashCode(seed)
		);
		this.radius = radius;
		altitudeLayer = new PlanetaryScaling3DCoordinateNoiseGenerator(
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				minPrecision,
				altitudeRange);
		tectonicLayer = new PlanetaryScaling3DCoordinateNoiseGenerator(
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				minPrecision,
				tectFactor);
	}
	
	
	
	private double getTectonicFactor(double x, double y, double z){
		double t = tectonicLayer.getValue(x, y, z, 4096);
		double tectonic = 1 / (t*t);
		if(tectonic > 10){tectonic = 10;}
		return tectonic;
	}
	
	
/** provided for optimization purposes */
	private double cos(double a) {
		return Math.cos(a);
	}
/** provided for optimization purposes */
	private double sin(double a) {
		return Math.sin(a);
	}
	
	@Deprecated
	public static void main(String[] args){
		String seed = (new Long(System.currentTimeMillis())).toString();
		
		String newseed = javax.swing.JOptionPane.showInputDialog(seed);
		if(newseed != null){seed = newseed;}
		
		int size = 155;
		
		AbstractPlanet planet = new RandomMoon(seed, 2e6);
		PlanetaryMapProjector projector = 
			//	new MercatorMapProjector();
			//	new SinusoidalMapProjector();
				new FullerDymaxionMapProjector();
		
		java.awt.image.BufferedImage img = projector.createMapProjection(planet, size, (new StandardBiomeFactory()).getPlanetPainter());
		ImageUtils.showImage(img);
		try{ImageUtils.saveImage(img, "png");}catch(java.io.IOException iox){System.err.println(iox);}
		System.exit(0);
	}

	/**
	 * Calculates geography data at the given location, returning the roughness
	 * score at the given coordinate. Roughness is a measure of how mountainous 
	 * a location is and is generally more useful to fantasy mapping than 
	 * absolute altitude. 
	 * @param longitude Longitude coordinate of the location of interest
	 * @param latitude Latitude of the location of interest
	 * @param precision Determines how fine-grained the calculation is. E.g. if 
	 * <code>precision</code> is 10km, then the planet generation implementation 
	 * will return a value that is roughly the average of a 10km radius around 
	 * the given coordinate. If making a map from a grid of data points, set 
	 * the precision to the grid spacing.
	 * @return Returns a value from 0 to infinity at this coordinate, with a 
	 * value greater than 1 meaning mountains.
	 */
	@Override
	public double getRoughness(double longitude, double latitude, double precision) {
		double x = radius * sin(longitude)*cos(latitude);
		double y = radius * sin(latitude);
		double z = radius * cos(longitude)*cos(latitude);
		return getTectonicFactor(x,y,z)/5;
	}

	/**
	 * Calculates geography data at the given location, returning the altitude
	 * at the given coordinate.
	 * @param longitude Longitude coordinate of the location of interest
	 * @param latitude Latitude of the location of interest
	 * @param precision Determines how fine-grained the calculation is. E.g. if 
	 * <code>precision</code> is 10km, then the planet generation implementation 
	 * will return a value that is roughly the average of a 10km radius around 
	 * the given coordinate. If making a map from a grid of data points, set 
	 * the precision to the grid spacing.
	 * @return The altitude at this coordinate. Note that the returned altitude 
	 * is dependant on the precision, with lower precision returning a 
	 * "smoother" representation of altitude.
	 */
	@Override
	public double getAltitude(double longitude, double latitude, double precision) {
		double x = radius * sin(longitude)*cos(latitude);
		double y = radius * sin(latitude);
		double z = radius * cos(longitude)*cos(latitude);
		double h = altitudeLayer.getValue(x, y, z, precision) * getTectonicFactor(x,y,z)+radius;
		return h;
	}

	/**
	 * Calculates geography data at the given location, returning the water
	 * availability at the given coordinate.
	 * @param longitude Longitude coordinate of the location of interest
	 * @param latitude Latitude of the location of interest
	 * @param precision Determines how fine-grained the calculation is. E.g. if 
	 * <code>precision</code> is 10km, then the planet generation implementation 
	 * will return a value that is roughly the average of a 10km radius around 
	 * the given coordinate. If making a map from a grid of data points, set 
	 * the precision to the grid spacing.
	 * @return The moisture availability at this coordinate, roughly measured in 
	 * annual mean precipitation minus evaporation (in cm). A negative value 
	 * indicates a desert or arid environment, while a value greater than 200 is 
	 * a rainforest or jungle.
	 */
	@Override
	public double getMoisture(double longitude, double latitude, double precision) {
		return Double.NEGATIVE_INFINITY;
	}

	/**
	 * Calculates geography data at the given location, returning the 
	 * temperature at the given coordinate.
	 * @param longitude Longitude coordinate of the location of interest
	 * @param latitude Latitude of the location of interest
	 * @param precision Determines how fine-grained the calculation is. E.g. if 
	 * <code>precision</code> is 10km, then the planet generation implementation 
	 * will return a value that is roughly the average of a 10km radius around 
	 * the given coordinate. If making a map from a grid of data points, set 
	 * the precision to the grid spacing.
	 * @return The annual mean temperature (in °C) at this coordinate
	 */
	@Override
	public double getTemperature(double longitude, double latitude, double precision) {
		return 3-273.15; // 3°K
	}

	/**
	 * Gets the size of the planet.
	 * @return The radius of the planet, in meters;
	 */
	@Override
	public double getRadius() {
		return radius;
	}
}
