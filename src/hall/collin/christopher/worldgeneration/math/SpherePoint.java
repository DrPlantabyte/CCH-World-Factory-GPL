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

import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * A 2-dimensional coordinate on the surface of a sphere, expressed in 
 * longitude and latitude.
 * @author CCHall
 */
public class SpherePoint implements Serializable, Cloneable{
	/** The longitude of the point */
	public final double longitude;
	/** The latitude of the point */
	public final double latitude;
	/**
	 * Creates a 2D (longitude, latitude) point on the surface of a 
	 * sphere.
	 * @param lon Longitude (angle around the equator, in <b>radians</b>).
	 * @param lat Latitude (angle above/below the equator, in <b>radians</b>). 
	 */
	public SpherePoint(double lon, double lat) {
		longitude = lon;
		latitude = lat;
	}
	/**
	 * Gets the longitude of the point.
	 * @return Longitude, in radians
	 */
	public double getLongitude(){
		return longitude;
	}
	/**
	 * Gets the latitude of the point.
	 * @return Latitude, in radians
	 */
	public double getLatitude(){
		return latitude;
	}
	
	/**
	 * Computes the hashCode for this point.
	 * @return A hashcode
	 */
	@Override
	public int hashCode() {
		int hash = 3;
		hash = 89 * hash + (int) (Double.doubleToLongBits(this.longitude) ^ (Double.doubleToLongBits(this.longitude) >>> 32));
		hash = 89 * hash + (int) (Double.doubleToLongBits(this.latitude) ^ (Double.doubleToLongBits(this.latitude) >>> 32));
		return hash;
	}
	/**
	 * Tests equality to another object
	 * @param obj object to test
	 * @return Returns true iff the object is another SpherePoint with 
	 * equivalent longitude and latitude coordinates
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SpherePoint other = (SpherePoint) obj;
		if (this.longitude != other.longitude) {
			return false;
		}
		if (this.latitude != other.latitude) {
			return false;
		}
		return true;
	}

	/**
	 * Makes a copy of this SpherePoint
	 * @return An identical SpherePoint instance
	 */
	@Override
	public SpherePoint clone(){
		return new SpherePoint(this.longitude, this.latitude);
	}
	/**
	 * Makes a string representing this coordinate
	 * @return "("+(float)this.longitude+","+(float)this.latitude+")"
	 */
	@Override public String toString(){
		return "("+(float)this.longitude+","+(float)this.latitude+")";
	}
	
}
