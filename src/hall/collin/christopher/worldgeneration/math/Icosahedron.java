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

package hall.collin.christopher.worldgeneration.math;

import java.util.Arrays;

/**
 * Class for getting constants related to geometry
 * @author CCHall
 */
public class Icosahedron {
	
	private final Point3D[] vertices;
	/** Default constructor, makes an icosahedron */
	public Icosahedron(){
		vertices = new Point3D[12];
		// from wikipedia
		double phi = (1 + Math.sqrt(5)) / 2;
		double one = 1;
		// normalized to unit coords
		phi /= Math.sqrt(3);
		one /= Math.sqrt(3);
		vertices[0]  = new Point3D( 0.0, one, phi);
		vertices[1]  = new Point3D( 0.0, one,-phi);
		vertices[2]  = new Point3D( 0.0,-one, phi);
		vertices[3]  = new Point3D( 0.0,-one,-phi);
		vertices[4]  = new Point3D( one, phi, 0.0);
		vertices[5]  = new Point3D( one,-phi, 0.0);
		vertices[6]  = new Point3D(-one, phi, 0.0);
		vertices[7]  = new Point3D(-one,-phi, 0.0);
		vertices[8]  = new Point3D( phi, 0.0, one);
		vertices[9]  = new Point3D( phi, 0.0,-one);
		vertices[10]  = new Point3D(-phi, 0.0, one);
		vertices[11]  = new Point3D(-phi, 0.0,-one);
	}
	
	/**
	 * Gets the coordinates of the vertices of the icosahedron.
	 * @return the coordinates of the vertices of an icosahedron.
	 */
	public Point3D[] getVertices(){
		return Arrays.copyOf(vertices, vertices.length);
	}
}
