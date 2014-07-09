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
 * Abstract class for 3D noise generators (such as 3D perlin noise). Extending 
 * classes will use some method to generate an interpolated random value for the 
 * given coordinate.
 * @author Christopher Collin Hall
 */
public class Default3DCoordinateNoiseGenerator extends Abstract3DCoordinateNoiseGenerator{
	/**
	 * Implementation of an Abstract3CoordinatePRNG to generate random numbers
	 */
	protected final Abstract3CoordinatePRNG prng;
	/** distance between noise points */
	private final double resolution;
	/**
	 * Creates a general use, one frequency perlin noise type of interpolated 
	 * random number generator for 3D noise.
	 * @param coordprng Implementation of an Abstract3CoordinatePRNG to generate 
	 * random numbers from coordinates
	 * @param gridSpacing The noise resolution (distance between the noise control points)
	 */
	public Default3DCoordinateNoiseGenerator(Abstract3CoordinatePRNG coordprng, double gridSpacing){
		this.prng = coordprng;
		resolution = gridSpacing;
	}
	
	/**
	 * This method will generate a Perlin Noise type of value 
	 * interpolated at the provided coordinate.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @return A value interpolated from random control points, such that the 
	 * same coordinate always results in the same output value and a coordinate 
	 * very close to another will have a similar, but not necessarily the same, 
	 * value as the other coordinate.
	 * @throws ArrayIndexOutOfBoundsException May be thrown if the provided 
	 * coordinates exceed the allowable range of the underlying algorithm.
	 */
	@Override
	public double getValue(double x, double y, double z) throws ArrayIndexOutOfBoundsException{
		x /= resolution;
		y /= resolution;
		z /= resolution;
		double[][][] grid = new double[4][4][4];// [x][y][z]
		long xn1 = floor(x);
		long yn1 = floor(y);
		long zn1 = floor(z);
		
		for (int dz = -1; dz <= 2; dz++) {
			for (int dy = -1; dy <= 2; dy++) {
				for (int dx = -1; dx <= 2; dx++) {
					grid[dx + 1][dy + 1][dz + 1] = prng.doubleValueAt(xn1 + dx, yn1 + dy, zn1 + dz);
				}
			}
		}
		return CubicInterpolator.interpolate3d(x,y,z,grid);
	}

	/**
	 * Faster implementation than Math.floor(x). 
	 * @param x
	 * @return The greatest integer value less than x. If x is NaN, then 0 is 
	 * returned. If x is infinite (positive or negative), then Long.MAX_VALUE is returned.
	 */
	private static long floor(double x) {
	//	return (long)Math.floor(x);/*
		if(x < 0){
			return (long)x - 1;
		} else {
			return (long)x;
		}/* */
	}
	
	
	
	
}
