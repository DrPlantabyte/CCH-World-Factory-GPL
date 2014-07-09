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

import hall.collin.christopher.worldgeneration.math.SpherePoint;

/**
 * This class represents a node in a spherical network.
 *
 * @author CCHall
 */
public class SphereNetNode { // this is neccessary in order to have a null location
	/** The coordinate of this node */
	SpherePoint pt;
	
	
	private int filledConnections = 0;
	/** Pointers to adjacent nodes in the network. There is no limit to the 
	 * number of possible connections, but it is typically either 5 or 6 */
	private SphereNetNode[] connections = null;
	/** Default constructor, has null coordinate */
	SphereNetNode() {
		pt = null;
	}
	/**
	 * A constructor that doesn not initialize the connections array.
	 * @param p The coordinate of this node.
	 */
	public SphereNetNode(SpherePoint p) {
		pt = p;
	}

	/**
	 * Standard constructor.
	 * @param p The coordinate of this node
	 * @param numConnections The number of connections that you may assign to 
	 * this node in the future.
	 */
	public SphereNetNode(SpherePoint p, int numConnections) {
		pt = p;
		connections = new SphereNetNode[numConnections];
	}

	
	/**
	 * Returns the number of <i>possible</i> connections that this node can make 
	 * with other nodes. Unassigned connections will be null.
	 * @return The length of the connections array.
	 */
	public int getNumberConnections() {
		if (connections == null) {
			return 0;
		}
		return connections.length;
	}
	/**
	 * Adds a connection to this node, though it is better to use the 
	 * <code>addConnectionIfAbsent(target)</code> method. This is not a 
	 * bidirectional operation (will not add this node to other node's 
	 * connections).
	 * @param target Node to assign as a connection to this node.
	 */
	public synchronized void pushConnection(SphereNetNode target) {
		connections[filledConnections] = target;
		filledConnections++;
	}
	/**
	 * Adds a connection to this node if it is not already connected. This is 
	 * not a bidirectional operation (will not add this node to other node's 
	 * connections).
	 * @param target Node to assign as a connection to this node.
	 */
	public void addConnectionIfAbsent(SphereNetNode target) {
		for (int i = 0; i < filledConnections; i++) {
			if (connections[i] == target) {
				return;
			}
		}
		addConnectionIfAbsentSynchronized(target);
	}
	/**
	 * Adds a connection to this node if it is not already connected. This is 
	 * not a bidirectional operation (will not add this node to other node's 
	 * connections).
	 * @param target Node to assign as a connection to this node.
	 */
	public synchronized void addConnectionIfAbsentSynchronized(SphereNetNode target) {
		for (int i = 0; i < filledConnections; i++) {
			if (connections[i] == target) {
				return;
			}
		}
		pushConnection(target);
	}
	/**
	 * Gets the connection stored at the indicated index. See also 
	 * <code>getNumberConnections()</code>.
	 * @param index The index to look-up.
	 * @return The Node at that connection slot. If not assigned, null will be 
	 * returned.
	 */
	public SphereNetNode getConnection(int index) {
		if (connections == null) {
			return null;
		}
		return connections[index];
	}
	/**
	 * Gets the coordinate of this node.
	 * @return The coordinate of this node in spherical space (longitude, 
	 * latitude).
	 */
	public SpherePoint getCoordinate() {
		return pt;
	}
	/**
	 * Sets the coordinate of this node.
	 * @param np The coordinate of this node in spherical space (longitude, 
	 * latitude).
	 */
	public void setPoint(SpherePoint np) {
		pt = np;
	}

	/**
	 * Sets a connection in a specific slot to the target node.
	 * @param j index in connections array
	 * @param set Node to set as a connection to this node.
	 */
	public void setConnection(int j, SphereNetNode set) {
		connections[j] = set;
	}
	/**
	 * Gets the connections array.
	 * @return Array of pointers to connected nodes.
	 */
	public SphereNetNode[] getConnections(){
		return connections;
	}
}
