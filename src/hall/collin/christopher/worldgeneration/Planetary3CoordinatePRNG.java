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

import hall.collin.christopher.worldgeneration.math.Abstract3CoordinatePRNG;

/**
 * This class is used for the generation of psuedorandom numbers from 
 * 3D coordinates.
 * @author CCHall
 */
public class Planetary3CoordinatePRNG extends Abstract3CoordinatePRNG{

	/** Seed used for making a unique pattern */
	private final long seed;
	/** multiplier for LCG number generation */
	private static final long lcgprng_multiplier = 25214903917L;
	/** constant for LCG number generation */
	private static final long lcgprng_adder = 11L;
	/** modulo for LCG number generation */
	private static final long lcgprng_modulo = (1L << 48);
	/** shift to strip off lower order bits */
	private static final int lcgprng_shift = 16;
	/** bitmask of random-quality bits after applying the shift operation */
	private static final long lcgprng_outputMask = 0x00000000FFFFFFFFL;
	
	public Planetary3CoordinatePRNG(long seed){
		this.seed = seed;
	}
	/**
	 * Creates a psuedorandom number that will always be the same for the same 
	 * coordinate, ranging from 0 to 1.
	 * @param x coordinate
	 * @param y coordinate
	 * @param z coordinate
	 * @return A psuedorandom number that will always be the same for the same 
	 * coordinate, ranging from -1 to 1
	 */
	@Override
	public float valueAt(int x, int y, int z) {
		return (float)((hash(x,y,z) & 0x3FFFFF)/(float)(0x1FFFFF)) - 1f;
	}
	/**
	 * Uses a Linear Congruence Generator algorithm to scramble a number
	 * @param input A number to scramble
	 * @return A seemingly random number that will always be the same for the 
	 * same input number.
	 */
	private static long hash(long input){
		return ((input * lcgprng_multiplier + lcgprng_adder) >> lcgprng_shift) & lcgprng_outputMask;
	}
	/**
	 * Uses prime numbers and a magic formula from the internet (see 
	 * <a href="http://freespace.virgin.net/hugo.elias/models/m_perlin.htm">http://freespace.virgin.net/hugo.elias/models/m_perlin.htm</a> )
	 * to combine the 3 coordinates into a single unique-ish number.
	 * @param x coordinate
	 * @param y coordinate
	 * @param z coordinate
	 * @return A psuedorandom number, which appears much more randum after being 
	 * hashed by the other hashing method.
	 */
	private long hash(int x, int y, int z){
		long n = seed + x * 2097593L + y * 57L + z * 3191L;
		n = (n<<19) ^ n;
		return hash(  (n * (n * n * 15731 + 789221) + 1376312589) );
	}
	
	@Deprecated
	public static void main(String[] args){
		Planetary3CoordinatePRNG test = new Planetary3CoordinatePRNG(System.currentTimeMillis());//"Test Seed".hashCode());
		int size = 512;
		int other = (int)(hash(size)%size);
		java.awt.image.BufferedImage ixy = new java.awt.image.BufferedImage(size,size,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.image.BufferedImage ixz = new java.awt.image.BufferedImage(size,size,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.image.BufferedImage iyz = new java.awt.image.BufferedImage(size,size,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				ixy.setRGB(i, j, java.awt.Color.HSBtoRGB(0f,0f,(float)(test.hash(i-(size/2),j-(size/2),other)%1000) / 1000f));
				ixz.setRGB(i, j, java.awt.Color.HSBtoRGB(0f,0f,(float)(test.hash(i-(size/2),other,j-(size/2))%1000) / 1000f));
				iyz.setRGB(i, j, java.awt.Color.HSBtoRGB(0f,0f,(float)(test.hash(other,i-(size/2),j-(size/2))%1000) / 1000f));
			}
		}
		javax.swing.JOptionPane.showMessageDialog(null, new javax.swing.JLabel(new javax.swing.ImageIcon(ixy)));
		javax.swing.JOptionPane.showMessageDialog(null, new javax.swing.JLabel(new javax.swing.ImageIcon(ixz)));
		javax.swing.JOptionPane.showMessageDialog(null, new javax.swing.JLabel(new javax.swing.ImageIcon(iyz)));
	}
}
