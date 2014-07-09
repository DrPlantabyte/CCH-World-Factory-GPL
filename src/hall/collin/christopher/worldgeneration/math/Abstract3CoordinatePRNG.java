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
 * Abstract class for Psuedo-Random Number Generators (PRNGs) that generate a 
 * value based on a 3-dimensional coordinate. Used in 3D Perlin Noise 
 * generators.
 * @author Christopher Collin Hall
 */
public abstract class Abstract3CoordinatePRNG {
	/**
	 * Implementations of this method will return a psuedo-random 
	 * single-precision number using the provided coordinates. The 
	 * implementation must ensure that the same instance will always return the 
	 * same value for the same input coordinates.
	 * @param x coordinate
	 * @param y coordinate
	 * @param z coordinate
	 * @return A psuedorandom number that will always be the same for the same 
	 * coordinate
	 */
	public abstract float valueAt(int x, int y, int z);
	/**
	 * (Optional Method) Implementations of this method will return a psuedo-random 
	 * double-precision number using the provided coordinates. The 
	 * implementation must ensure that the same instance will always return the 
	 * same value for the same input coordinates.<p/>
	 * If not overridden, this method is just a wrapper of the 
	 * <code>valueAt(x,y,z)</code> method
	 * @param x coordinate
	 * @param y coordinate
	 * @param z coordinate
	 * @return A psuedorandom number that will always be the same for the same 
	 * coordinate
	 */
	public double doubleValueAt(long x, long y, long z){
		return valueAt((int)x,(int)y,(int)z);
	}
	
}
