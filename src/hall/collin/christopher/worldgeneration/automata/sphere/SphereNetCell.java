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

package hall.collin.christopher.worldgeneration.automata.sphere;

import hall.collin.christopher.worldgeneration.math.Point3D;
import hall.collin.christopher.worldgeneration.math.SpherePoint;
import hall.collin.christopher.worldgeneration.math.SphericalMath;

/**
 * A cell in a spherical network. Cells are triangular, with 3 nodes as corners.
 * @author CCHall
 */
public class SphereNetCell {
	private final static SphericalMath smath = SphericalMath.getInstance();
	/** A point representing the center of the cell (this cell essentially  is a voronoi cell around that point)*/
	public SpherePoint midpoint = null;
	/** The corner nodes of this cell. In a properly implemented network, the nodes instances are shared with adjacent cells */
	public SphereNetNode[] node;
	/** Pointers to neighboring cells. Must be set externally in a network. */
	public SphereNetCell[] neighbor; // neighbors 0, 1, 2 (neighbor opposite of point with same index)
	/** pointers to descendants from this cell (usually by tesselation). */
	public SphereNetCell[] child; // children are used for tesselations, child 3 is center child
	
	
/*
Figure 1: diagram of children
         [0]
         /\
  2     /0 \     1
     p2/____\p1
      /\ 3  /\
     /1 \  /2 \
    /____\/____\
   [1]   p0    [2]
	
         0

*/		
	
	/** Default constuctor, does not have nodes or children. */
	SphereNetCell() {
		node = new SphereNetNode[3];
		node[0] = new SphereNetNode();
		node[1] = new SphereNetNode();
		node[2] = new SphereNetNode();
		neighbor = new SphereNetCell[3];
		neighbor[0] = null;
		neighbor[1] = null;
		neighbor[2] = null;
		child = new SphereNetCell[4];
		child[0] = null;
		child[1] = null;
		child[2] = null;
		child[3] = null;
	}
	/**
	 * Standard constructor, using the provided nodes for the corners
	 * @param j A node at the corner of this cell
	 * @param k A node at the corner of this cell
	 * @param l A node at the corner of this cell
	 */
	public SphereNetCell(SphereNetNode j,SphereNetNode k,SphereNetNode l) {
		node = new SphereNetNode[3];
		node[0] = (j);
		node[1] = (k);
		node[2] = (l);
		neighbor = new SphereNetCell[3];
		neighbor[0] = null;
		neighbor[1] = null;
		neighbor[2] = null;
		child = new SphereNetCell[4];
		child[0] = null;
		child[1] = null;
		child[2] = null;
		child[3] = null;
		midpoint = calculateMidpoint(j.pt,k.pt,l.pt);
	}
	/**
	 * Determines whether this cell has child cells associated with it.
	 * @return Returns true if all child elements have been initialized 
	 * (non-null), false otherwise.
	 */
	public boolean hasChildren(){
		for(int i = 0; i < child.length; i++){
			if(child[i] == null){
				return false;
			}
		}
		return true;
	}
	/**
	 * Used for sorting, the value itself doesn't mean much as it is an average 
	 * of 3 different calulations. If a point lies inside a triangle cell, it 
	 * will have the lowest value from this method.
	 * @param pt A point on a spere
	 * @return Angular distance from midpoint of this node to that point
	 */
	public double approximateDistance(SpherePoint pt){
		return (smath.angularDistance(pt, node[0].pt)+smath.angularDistance(pt, node[1].pt)+smath.angularDistance(pt, node[2].pt))/3;
	}
		
	/**
	 * Sets a neighbor for this cell.
	 * @param j The index (0-2) of the neighbor in question
	 * @param set The neighboring cell
	 */
	public void setNeighbor(int j, SphereNetCell set) {
		neighbor[j] = set;
	}

	private static SpherePoint calculateMidpoint(SpherePoint... points) {
		double x = 0, y = 0, z = 0;
		for(int i = 0; i < points.length; i++){
			Point3D p = SphericalMath.getInstance().lonLatTo3D(points[i]);
			x += p.getX();
			y += p.getY();
			z += p.getZ();
		}
		return SphericalMath.getInstance().point3DToLonLat(x, y, z);
	}


	
	
}
