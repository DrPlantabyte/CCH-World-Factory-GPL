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

/**
 * A 3-dimensional point in rectangular space.
 * @author CCHall
 */
public final class Point3D {
	/** Coordinate in the first dimension */
	public final double x;
	/** Coordinate in the second dimension */
	public final double y;
	/** Coordinate in the third dimension */
	public final double z;
	/**
	 * Creates a 3D point in rectangular space.
	 * @param x Coordinate of the point
	 * @param y Coordinate of the point
	 * @param z Coordinate of the point
	 */
	public Point3D(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	/**
	 * Gets the X coordinate value.
	 * @return X value
	 */
	public double getX(){
		return x;
	}
	/**
	 * Gets the Y coordinate value.
	 * @return Y value
	 */
	public double getY(){
		return y;
	}
	/**
	 * Gets the Z coordinate value.
	 * @return Z value
	 */
	public double getZ(){
		return z;
	}
	
	/**
	 * Computes a hash code for this coordinate
	 * @return An integer hash
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
		hash = 23 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
		hash = 23 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
		return hash;
	}
	/**
	 * Tests equality to another object
	 * @param other
	 * @return Returns true iff the other object is a Point3D with equivalent 
	 * X, Y, and Z coordinates.
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (getClass() != other.getClass()) {
			return false;
		}
		final Point3D otherpt = (Point3D) other;
		if (this.x != otherpt.x) {
			return false;
		}
		if (this.y != otherpt.y) {
			return false;
		}
		if (this.z != otherpt.z) {
			return false;
		}
		return true;
	}
	
	/**
	 * Makes a string representing this coordinate
	 * @return "("+(float)this.x+","+(float)this.y+","+(float)this.z+")"
	 */
	@Override public String toString(){
		return "("+(float)this.x+","+(float)this.y+","+(float)this.z+")";
	}
}
