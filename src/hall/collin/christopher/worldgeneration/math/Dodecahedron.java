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
public class Dodecahedron {
	
	private final Point3D[] vertices;
	/** Default constructor, makes a dodecahedron */
	public Dodecahedron(){
		vertices = new Point3D[20];
		// from wikipedia
		double phi = (1 + Math.sqrt(5)) / 2;
		double ihp = 1 / phi;
		double one = 1;
		// normalized to unit coords
		phi /= Math.sqrt(3);
		ihp /= Math.sqrt(3);
		one /= Math.sqrt(3);
		vertices[0]  = new Point3D( one, one, one);
		vertices[1]  = new Point3D( one, one,-one);
		vertices[2]  = new Point3D( one,-one, one);
		vertices[3]  = new Point3D( one,-one,-one);
		vertices[4]  = new Point3D(-one, one, one);
		vertices[5]  = new Point3D(-one, one,-one);
		vertices[6]  = new Point3D(-one,-one, one);
		vertices[7]  = new Point3D(-one,-one,-one);
		vertices[8]  = new Point3D( 0.0, ihp, phi);
		vertices[9]  = new Point3D( 0.0, ihp,-phi);
		vertices[10] = new Point3D( 0.0,-ihp, phi);
		vertices[11] = new Point3D( 0.0,-ihp,-phi);
		vertices[12] = new Point3D( ihp, phi, 0.0);
		vertices[13] = new Point3D( ihp,-phi, 0.0);
		vertices[14] = new Point3D(-ihp, phi, 0.0);
		vertices[15] = new Point3D(-ihp,-phi, 0.0);
		vertices[16] = new Point3D( phi, 0.0, ihp);
		vertices[17] = new Point3D( phi, 0.0,-ihp);
		vertices[18] = new Point3D(-phi, 0.0, ihp);
		vertices[19] = new Point3D(-phi, 0.0,-ihp);
	}
	
	/**
	 * Gets the 3D coordinates of the vertices of the Dodecahedron
	 * @return the 3D coordinates of the vertices of the Dodecahedron
	 */
	public Point3D[] getVertices(){
		return Arrays.copyOf(vertices, vertices.length);
	}
}
