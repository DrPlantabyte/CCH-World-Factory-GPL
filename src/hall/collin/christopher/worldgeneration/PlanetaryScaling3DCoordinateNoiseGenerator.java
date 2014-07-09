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

import hall.collin.christopher.worldgeneration.math.CubicInterpolator;
import hall.collin.christopher.worldgeneration.math.XorShiftRandomNumberGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This classes provides an interface to sample 3-dimensional Perlin 
 * Noise with arbitrary special resolution and cubic interpolation.
 * @author CCHall
 */
public class PlanetaryScaling3DCoordinateNoiseGenerator  {

	
	private final XorShiftRandomNumberGenerator seedGenerator;
	private final List<Planetary3CoordinatePRNG> layers = new ArrayList<>();
	private final List<Double> magnitudes = new ArrayList<>();
	private final List<Double> units = new ArrayList<>();
	private final double initialUnitSize;
	private final double unitScaleFactor;
	private final double initialMagnitude;
	private final double magnitudeScaleFactor;
	/**
	 * Creates an instance of the PlanetaryScaling3DCoordinateNoiseGenerator 
	 * with the given seeds and default settings. Note that this noise generator 
	 * needs 4 seeds, which shouldn't be 0's.
	 * @param seed1 A seed for random number generation
	 * @param seed2 A seed for random number generation
	 * @param seed3 A seed for random number generation
	 * @param seed4 A seed for random number generation
	 * @param initialUnitSize This is teh spacial resolution of the lowest noise 
	 * frequency.
	 * @param initialMagnitude This is the initial range of noise for the first 
	 * frequency.
	 */
	public PlanetaryScaling3DCoordinateNoiseGenerator(long seed1, long seed2, long seed3, long seed4, double initialUnitSize, double initialMagnitude){
		seedGenerator = new XorShiftRandomNumberGenerator(seed1, seed2, seed3, seed4, false);
		this.initialMagnitude = initialMagnitude;
		this.initialUnitSize = initialUnitSize;
		this.unitScaleFactor = 0.5;
		this.magnitudeScaleFactor = 0.5;
	}
	
	
	
	private final Lock seedLock = new ReentrantLock();
	/** ensures that all of the layers have been generated */
	private void checkLayers(int depth){
		if(layers.size() > depth){return;}
		seedLock.lock();
		try{
			while(layers.size() <= depth){
				layers.add(new Planetary3CoordinatePRNG(seedGenerator.nextLong()));
				units.add(Math.pow(unitScaleFactor, units.size())*initialUnitSize);
				magnitudes.add(Math.pow(magnitudeScaleFactor, magnitudes.size())*initialMagnitude);
			}
		} finally {
			seedLock.unlock();
		}
	}
	/**
	 * Generates a Perlin Noise type interpolated random number, using nested 
	 * fractals to generate noise down to the specified precision. 
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param precision Spacial resolution. 
	 * @return A Perlin Noise type interpolated random number. 
	 */
	public double getValue(double x, double y, double z, double precision) {
		int depth = (int)(Math.log(precision/initialUnitSize)/Math.log(unitScaleFactor))+1;
		if(depth < 1){
			depth = 1;
		}
		checkLayers(depth);
		
		double sum = 0;
		for(int i = 0; i < depth; i++){
			sum += interpolateValue(x,y,z,i) * magnitudes.get(i);
		}
		return sum;
	}
	
	private double interpolateValue(double x, double y, double z, int depth){
		double unitSize = units.get(depth);
		double xp = x/unitSize;
		double yp = y/unitSize;
		double zp = z/unitSize;
		int x0 = floor(xp);
		int y0 = floor(yp);
		int z0 = floor(zp);
		double[][][] local64 = new double[4][4][4];
		for (int dx = -1; dx < 3; dx++) {
			for (int dy = -1; dy < 3; dy++) {
				for (int dz = -1; dz < 3; dz++) {
					local64[dx + 1][dy + 1][dz + 1] = layers.get(depth).valueAt(x0 + dx, y0 + dy, z0 + dz);
				}
			}
		}
		return CubicInterpolator.interpolate3d(xp-x0, yp-y0, zp-z0, local64);
	}
	
	/**
	 * Custom implementation of <code>Math.floor()</code> that is a 
	 * little quicker.
	 * @param a A number
	 * @return The largest integer less than <code>a</code>.
	 */
	protected static int floor(double a){
		if(a < 0){
			return ((int)a)-1;
		}else{
			return (int) a;
		}
	}
	/**
	 * Custom implementation of <code>Math.floor()</code> that is a 
	 * little quicker.
	 * @param a A number
	 * @return The largest integer less than <code>a</code>.
	 */
	protected static int floor(float a){
		if(a < 0){
			return ((int)a)-1;
		}else{
			return (int) a;
		}
	}
	
	
	
}
