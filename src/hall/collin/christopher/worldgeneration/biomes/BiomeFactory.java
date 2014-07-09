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

package hall.collin.christopher.worldgeneration.biomes;

import hall.collin.christopher.worldgeneration.AbstractPlanet;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * This is the superclass for objects that generate biomes from geography data. 
 * Since biomes are often used to color a map, it is common for BiomeFactories 
 * to also offer a PlanetPainter implementation.
 * @author Christopher Collin Hall
 */
public abstract class BiomeFactory {

	/**
	 * This method calculates the biome for a given location on a planet. 
	 * Different biome factories may make different assumptions about how a 
	 * biome should be determined and may even have different definitions of a 
	 * biome.
	 * @param planet The planet of interest
	 * @param longitude Longitude coordinate of the location of interest
	 * @param latitude Latitude of the location of interest
	 * @param precision Determines how fine-grained the calculation is. 
	 * @return A biome instance describing the biome at this location.
	 */
	public abstract Biome getBiome(AbstractPlanet planet, double longitude, double latitude, double precision);
	
	
}
