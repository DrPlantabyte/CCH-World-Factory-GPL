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
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Superclass for classes that make images from planets. 
 * @author CCHall
 */
public abstract class PlanetaryMapProjector {
	/**
	 * Creates a 2D map of the planet. 
	 * <p>
	 * <b>PlanetPainter implementations should check for and abort on thread 
	 * interruption.</b>
	 * @param planet Planet to map
	 * @param size size of the image (typically the height of the image)
	 * @param painter A PlanetPainter instance to determine the colors on the 
	 * map.
	 * @param progressTracker This object is used to track the progress of the 
	 * image creation process. Implementations must incrementally add values to 
	 * this object such that the sum total is 1 when the operation is complete. 
	 * Also, implementations must allow this parameter to be null.
	 * @return An image of the planet's map
	 */
	public abstract BufferedImage createMapProjection(AbstractPlanet planet, int size, PlanetPainter painter, DoubleAdder progressTracker);
	
	/**
	 * Creates a 2D map of the planet
	 * @param planet Planet to map
	 * @param size size of the image (typically the height of the image)
	 * @param painter A PlanetPainter instance to determine the colors on the 
	 * map.
	 * @return An image of the planet's map
	 */
	public BufferedImage createMapProjection(AbstractPlanet planet, int size, PlanetPainter painter){
		return createMapProjection(planet,size,painter,null);
	}
}
