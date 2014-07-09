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
import hall.collin.christopher.worldgeneration.util.AltitudeColorizer;

/**
 * This is a simple map painter that colors based purely on altitude.
 * @author CCHall
 */
public class AltitudePainter extends PlanetPainter{

	private final double minAlt;
	private final double seaLevel;
	private final double maxAlt;
	/**
	 * Planet Painter that paints color using only altitude as input.
	 * @param min Lowest altitude expected (should be less than sea level)
	 * @param max Highest altitude expected (should be greater than sea level)
	 * @param seaLevel Altitude of surface of the ocean
	 */
	public AltitudePainter(double min, double max, double seaLevel){
		this.seaLevel = seaLevel;
		this.maxAlt = max;
		this.minAlt = min;
		if(seaLevel < minAlt || seaLevel > maxAlt){
			throw new IllegalArgumentException("Sea level must be between minimum and maximum altitudes");
		}
	}
	/**
	 * Planet Painter that paints color using only altitude as input.
	 */
	public AltitudePainter(){
		this.seaLevel = 0;
		this.maxAlt = 3000;
		this.minAlt = -4000;
		
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
		double alt = planet.getAltitude(longitude, latitude, precision);
		if(alt <= 0){
			alt /= (seaLevel - minAlt);
			if(alt < -1){
				alt = -1;
			}
		} else {
			alt /= (maxAlt - seaLevel);
			if(alt > 1){
				alt = 1;
			}
		}
		return AltitudeColorizer.getColor((float)alt);
	}
	
}
