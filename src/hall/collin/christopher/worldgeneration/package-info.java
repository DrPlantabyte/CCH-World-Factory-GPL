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
 * This package contains the best results from Christopher Collin Hall's 
 * experiments in random planetary terrain generation.
 * <p>
 * I have experimented with many, many different terrain generation algorithms. 
 * </p><p><img src="http://www.globeatnight.org/observe_finder_20N.html" align="left"/>
 * First I tried 2D Perlin Noise, which worked great for local landscapes, but 
 * it made horribly distorted maps when used on a sphere (not surprising, given 
 * the nature of 2D maps of Earth).
 * </p><p><img src="http://mathworld.wolfram.com/images/eps-gif/TruncatedIcosahedralGraph_800.gif" align="left"/>
 * I tried to get better results by replacing 
 * the 2D grid with node network connected like a sphere (inspired by Stephen 
 * Wolfram's "A New Kind of Science"). The sphere network had the nice advantage 
 * that is was relatively easy to turn nodes into triangles for presentation 
 * with the Java3D API, but creating noise frequencies by subdivison was 
 * complicated. Even more challenging was figuring out how to interpolate 
 * between nodes in a way to remove edge artifacts (caused by linear 
 * interpolation). The only algorithm that looked smooth was to use inverse 
 * distance weighted averaging (commonly used in geologic map making, usually 
 * applying a statistical factor called "Krigging", but that isn't necessary if 
 * the points are all evenly spaced). while the node network had very little 
 * distortion when mapped on a sphere, there were two glaring problems with the 
 * node network: First, The inverse-distance weighting made all peaks centered 
 * exactly on a node, giving a chicken-pox appearance to the terrain. Second, 
 * <img src="http://resources.esri.com/help/9.3/ArcGISEngine/java/Gp_ToolRef/geoprocessing/GPKAC_colin_IDW2.gif" align="right"/>
 * and more importantly, terrain generation was unacceptably slow. This was 
 * because measuring distances on a spherical surface without distortion 
 * requires the Haversine function, which relies heavily on the arcsine 
 * function, which is very slow on most (probably all) hardware. This was 
 * compounded by the fact that inverse-weight interpolation required calculating 
 * the distance from to every pixel to every node. Running on 8 threads on a 
 * 4-core processor (using look-up tables for arcsine), making a modest 
 * 1-megapixel texture map took 30 seconds to a minute (with only 2 noise 
 * frequencies). For the kind of resolution I want, it would take 10 minutes per 
 * planet. 
 * </p><p><img src="http://freespace.virgin.net/hugo.elias/models/sliced.gif" align="left"/>
 * I then stumbled into an interesting new way of making unbiased sphere 
 * maps with a 2D texture layer in this article on Spherical Landscapes by 
 * Hugo.Elias: <br/><a href="http://freespace.virgin.net/hugo.elias/models/m_landsp.htm">http://freespace.virgin.net/hugo.elias/models/m_landsp.htm</a> 
 * <br>I tried this method and it does get the look I want, but it took many, 
 * many iterations to achieve a decent resolution. This made the technique very 
 * poor for scaling (it was designed for moving points on an already-existing 3D 
 * sphere, not for scaling resolution). Ultimately, I dropped this approach, but 
 * it does a good job under certain circumstances and I recommend it over the 
 * previous techniques.
 * </p><p><img src="http://farm1.staticflickr.com/18/24013890_7f7d835efb_o.jpg" align="left"/>
 * I found that a lot of the game-dev forums would use a perlin noise 
 * library mapped on a high resolution sphere to make lumpy, planet-looking 
 * spheres, so I tried 2D perlin noise again, but this time is was wrapped onto 
 * a cube surface. The cube wrapping was a little complex, but most of the 
 * challenge was in developing a 2D cubic interpolation algorithm that could 
 * handle the corners of the cube. The result looked great when mapped onto a 
 * sphere, but I never quite mapped the cube correctly on the sphere. I think 
 * that I could have solved that, but the way I constructed my cubic 
 * interpolation algorithms made it easy for me to interpolate any number of 
 * dimensions, which gave me an idea.
 * </p><p>I remembered from Stephen Wolfram's book that a 2D slice through a 3D 
 * automata looks a lot like the 2D version of that kind of automata, so I tried 
 * using 3D Perlin Noise to make a spherical height map by converting longitude, 
 * latitude coordinates into X, Y, Z coordinates. Essentialy taking a spherical 
 * slice of the 3D noise space. The results looked great and had fast 
 * performance. Ultimately, of all the techniques that I have tried for 
 * spherical terrain generation, this technique has the least distortion, the 
 * fastest performance, and the best scalability (especially if you combine it 
 * with a psuedo-random number generator that can take a coordinate as a seed). 
 * If fact, I am realizing that 3D Perlin Noise will work great for any 
 * surface, not just spheres, with infinite scalability.
 * </p><p>
 * The algorithm used in all the planets in this library work by sampling a 3D 
 * Perlin Noise space along a 2D (spherical) surface.
 * </p>
 * @author Christopher Collin Hall (aka CCHall)
 */