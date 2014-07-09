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

import java.util.Random;

/**
 * Simple implementation of the Abstract3CoordinatePRNG using 
 * <code>java.util.Random</code> as the random number generator.
 * @author Christopher Collin Hall
 */
public class Default3CoordinatePRNG extends Abstract3CoordinatePRNG {
	private final long seed;
	private final float range;
	private final float offset;
	/**
	 * Instantiate with the provided seed.
	 * @param seed Seed to use for the random number generator.
	 * @param min Minimum output value for the random numbers
	 * @param max Maximum output value for the random numbers
	 */
	public Default3CoordinatePRNG(long seed, float min, float max){
		this.seed = seed;
		range = (max - min);
		offset = min;
	}
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
	@Override
	public float valueAt(int x, int y, int z){
		Random r = new Random(seed);
		r.nextLong();
		r = new Random(r.nextLong() ^ x);
		r.nextLong();
		r = new Random(r.nextLong() ^ y);
		r.nextLong();
		r = new Random(r.nextLong() ^ z);
		r.nextLong();
		r = new Random(r.nextLong());
		r.nextLong();
		return r.nextFloat() * range + offset;
	}
	
	@Deprecated
	public static void main(String[] a){
		int size = 400;
		java.awt.image.BufferedImage bimg = new java.awt.image.BufferedImage(size,size,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		Default3CoordinatePRNG prng = new Default3CoordinatePRNG(System.currentTimeMillis(),0,1);
		for(int x = 0; x < size; x++){
			for(int y = 0; y < size; y++){
				bimg.setRGB(x, y, java.awt.Color.HSBtoRGB(0, 0, prng.valueAt(x, y, 0)));
			}
		}
		javax.swing.JOptionPane.showMessageDialog(null, new javax.swing.JLabel(new javax.swing.ImageIcon(bimg)));
		bimg = new java.awt.image.BufferedImage(size,size,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		for(int x = 0; x < size; x++){
			for(int y = 0; y < size; y++){
				bimg.setRGB(x, y, java.awt.Color.HSBtoRGB(0, 0, prng.valueAt(0, y, x)));
			}
		}
		javax.swing.JOptionPane.showMessageDialog(null, new javax.swing.JLabel(new javax.swing.ImageIcon(bimg)));
	}
	
	
}
