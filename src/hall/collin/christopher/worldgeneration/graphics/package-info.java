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

/**
 * This package contains classes used to visualize planets.
 * <p>
 * The <code>PlanetPainter</code> classes define how to color a pixel on a 
 * planet at a given coordinate. The most commonly used implemkentations of 
 * <code>PlanetPainter</code> are <code>VegetationPainter</code> and 
 * <code>BiomePainter</code> (<code>hall.collin.christopher.worldgeneration.biomes.StandardBiomeFactory.createPlanetPainter()</code>).
 * </p><p>
 * The <code>PlanetaryMapProjector</code> classes take a planet and a 
 * <code>PlanetPainter</code> instance and generate a map as a 
 * <code>BufferedImage</code>. The most familiar type of map is the 
 * <code>MercatorMapProjector</code>, which generates a rectangular map. Such 
 * a map is convenient for printing and texturing 3D objects, but it has 
 * limitations when used as a map. Specifically, it suffers from distortion that 
 * is magnified as you approach the poles. You can reduce the distortion by 
 * using either a <code>FullerDymaxionProjector</code> or a 
 * <code>SectorMapProjector</code>.
 * </p>
 * @author CCHall
 */