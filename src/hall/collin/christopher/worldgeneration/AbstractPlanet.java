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

/**
 * Superclass for planets. Implementations need to be able to retrieve 
 * relevant data from arbitrary (longitude, latitude) coordinates. The 
 * AbstractPlanet class is passed into map projector classes to make maps.
 * @author CCHall
 */
public abstract class AbstractPlanet {
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
	public abstract double getRoughness(double longitude, double latitude, double precision);
	
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
	public abstract double getAltitude(double longitude, double latitude, double precision);
	
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
	public abstract double getMoisture(double longitude, double latitude, double precision);
	
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
	public abstract double getTemperature(double longitude, double latitude, double precision);
	/**
	 * Gets the size of the planet.
	 * @return The radius of the planet, in meters;
	 */
	public abstract double getRadius();
	/**
	 * This is the <code>java.lang.String.hashCode()</code> implementation, 
	 * expanded to 64 bits. This method exists to future-proof the generation 
	 * of random number seeds from Strings. If passed a string that can be 
	 * parsed as an integer, that (64-bit) integer value will be returned.<p>
	 * Forwards to 
	 * <code>hall.collin.christopher.worldgeneration.math.AbstractNumberGenerator.stringHashCode(string)</code> . 
	 * @param string A String object to serve as the seed for the hash code.
	 * @return Returns the hash code of the string using the algorithm that 
	 * became standard in Java 1.2 <code>java.lang.String.hashCode()</code>, 
	 * but expanded out to 64-bit integers. 
	 * 
	 */
	public static long stringHashCode(String string){
		return hall.collin.christopher.worldgeneration.math.AbstractNumberGenerator.stringHashCode(string);
	}
}
