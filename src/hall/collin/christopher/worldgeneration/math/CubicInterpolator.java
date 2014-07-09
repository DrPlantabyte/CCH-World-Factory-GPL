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
 * This is a convenience class that provides multidimensional cubic 
 * interpolation methods. This class is abstract and not meant to be extended
 * @author Christopher Collin Hall
 */
public abstract class CubicInterpolator {
	/**
	 * Interpolate with cubic approximation for a point X on a grid. X 
	 * must lie between the X values of the yn1 and yp1 control points.
	 * @param x x coordinate to interpolate
	 * @param yn2 Y value at f(floor(x)-1)
	 * @param yn1 Y value at f(floor(x)-0)
	 * @param yp1 Y value at f(floor(x)+1)
	 * @param yp2 Y value at f(floor(x)+2)
	 * @return A cubic-interpolated value from the given control points.
	 */
	public static double interpolate(double x, double yn2, double yn1, double yp1, double yp2){
		return interpolate1d( x,  yn2,  yn1,  yp1,  yp2);
	}
	/**
	 * Interpolate with cubic approximation for a point X on a grid. X 
	 * must lie between the X values of the yn1 and yp1 control points.
	 * @param x x coordinate to interpolate
	 * @param yn2 Y value at f(floor(x)-1)
	 * @param yn1 Y value at f(floor(x)-0)
	 * @param yp1 Y value at f(floor(x)+1)
	 * @param yp2 Y value at f(floor(x)+2)
	 * @return A cubic-interpolated value from the given control points.
	 */
	public static double interpolate1d(double x, double yn2, double yn1, double yp1, double yp2){
		double w = x - Math.floor(x);
		if(w == 0 && x != 0) return yp1; // w should be 1, but the way this is calcluated it doesn't work right
		// prevent precision-loss artifacts
		if(w < 0.00000001){
			return yn1;
		}
		if(w > 0.9999999) {
			return yp1;
		}
		// adapted from http://www.paulinternet.nl/?page=bicubic
		double A = -0.5 * yn2 + 1.5 * yn1 - 1.5 * yp1 + 0.5 * yp2;
		double B = yn2 - 2.5 * yn1 + 2 * yp1 - 0.5 * yp2;
		double C = -0.5 * yn2 + 0.5 * yp1;
		double D = yn1;
		return A * w * w * w + B * w * w + C * w + D;
	}
	/**
	 * Returns the bi-cubic interpolation of the (x,y) coordinate inide 
	 * the provided grid of control points. (x,y) is assumed to be in the 
	 * center square of the unit grid.
	 * @param x x coordinate between local16[1][y] and local16[2][y]
	 * @param y y coordinate between local16[x][1] and local16[x][2]
	 * @param local16 Array [x][y] of the 4x4 grid around the coordinate
	 * @return Returns the bi-cubic interpolation of the (x,y) coordinate.
	 */
	public static double interpolate2d(double x, double y, double[][] local16){
		double[] section = new double[4];
		for(int i = 0; i < 4; i++){
			section[i] = interpolate1d(y,local16[i][0],local16[i][1],local16[i][2],local16[i][3]);
		}
		return interpolate1d(x,section[0],section[1],section[2],section[3]);
	}
	/**
	 * Performs a tri-cubic interpolation of the (x,y,z) coordinate near 
	 * the center of the provided unit grid of surrounding control points.
	 * @param x x coordinate in the middle of the array space
	 * @param y y coordinate in the middle of the array space
	 * @param z z coordinate in the middle of the array space
	 * @param local64 Array [x][y][z] of the 4x4x4 grid around the coordinate
	 * @return Returns the tri-cubic interpolation of the given coordinate.
	 */
	public static double interpolate3d(double x, double y, double z, double[][][] local64){
		double[] section = new double[4];
		for(int i = 0; i < 4; i++){
			section[i] = interpolate2d(y,z,local64[i]);
		}
		return interpolate1d(x,section[0],section[1],section[2],section[3]);
	}
	/**
	 * Performs a quad-cubic interpolation of the (x,y,z,a) coordinate near 
	 * the center of the provided unit grid of surrounding control points.
	 * @param x coordinate in the middle of the array space
	 * @param y coordinate in the middle of the array space
	 * @param z coordinate in the middle of the array space
	 * @param a coordinate in the middle of the array space
	 * @param local256 Array [x][y][z][a] of the 4x4x4X4 grid around the coordinate
	 * @return Returns the quad-cubic interpolation of the given coordinate.
	 */
	public static double interpolate4d(double x, double y, double z, double a, double[][][][] local256){
		double[] section = new double[4];
		for(int i = 0; i < 4; i++){
			section[i] = interpolate3d(y,z,a,local256[i]);
		}
		return interpolate1d(x,section[0],section[1],section[2],section[3]);
	}
	/**
	 * Interpolate with cubic approximation for a point X on a grid. X 
	 * must lie between the X values of the yn1 and yp1 control points.
	 * @param x x coordinate to interpolate
	 * @param yn2 Y value at f(floor(x)-2)
	 * @param yn1 Y value at f(floor(x)-1)
	 * @param yp1 Y value at f(floor(x)+1)
	 * @param yp2 Y value at f(floor(x)+2)
	 * @return A cubic-interpolated value from the given control points.
	 */
	public static float interpolate1d(float x, float yn2, float yn1, float yp1, float yp2){
		// adapted from http://www.paulinternet.nl/?page=bicubic
		float A = -0.5f * yn2 + 1.5f * yn1 - 1.5f * yp1 + 0.5f * yp2;
		float B = yn2 - 2.5f * yn1 + 2f * yp1 - 0.5f * yp2;
		float C = -0.5f * yn2 + 0.5f * yp1;
		float D = yn1;
		float w = x - (float)Math.floor(x);
		return A * w * w * w + B * w * w + C * w + D;
	}
	/**
	 * Returns the bi-cubic interpolation of the (x,y) coordinate inide 
	 * the provided grid of control points. (x,y) is assumed to be in the 
	 * center square of the unit grid.
	 * @param x x coordinate between local16[1][y] and local16[2][y]
	 * @param y y coordinate between local16[x][1] and local16[x][2]
	 * @param local16 Array [x][y] of the 4x4 grid around the coordinate
	 * @return Returns the bi-cubic interpolation of the (x,y) coordinate.
	 */
	public static float interpolate2d(float x, float y, float[][] local16){
		float[] section = new float[4];
		for(int i = 0; i < 4; i++){
			section[i] = interpolate1d(y,local16[i][0],local16[i][1],local16[i][2],local16[i][3]);
		}
		return interpolate1d(x,section[0],section[1],section[2],section[3]);
	}
	/**
	 * Performs a tri-cubic interpolation of the (x,y,z) coordinate near 
	 * the center of the provided unit grid of surrounding control points.
	 * @param x x coordinate in the middle of the array space
	 * @param y y coordinate in the middle of the array space
	 * @param z z coordinate in the middle of the array space
	 * @param local64 Array [x][y][z] of the 4x4x4 grid around the coordinate
	 * @return Returns the tri-cubic interpolation of the given coordinate.
	 */
	public static float interpolate3d(float x, float y, float z, float[][][] local64){
		float[] section = new float[4];
		for(int i = 0; i < 4; i++){
			section[i] = interpolate2d(y,z,local64[i]);
		}
		return interpolate1d(x,section[0],section[1],section[2],section[3]);
	}
	/**
	 * Performs a quad-cubic interpolation of the (x,y,z,a) coordinate near 
	 * the center of the provided unit grid of surrounding control points.
	 * @param x coordinate in the middle of the array space
	 * @param y coordinate in the middle of the array space
	 * @param z coordinate in the middle of the array space
	 * @param a coordinate in the middle of the array space
	 * @param local256 Array [x][y][z][a] of the 4x4x4X4 grid around the coordinate
	 * @return Returns the quad-cubic interpolation of the given coordinate.
	 */
	public static float interpolate4d(float x, float y, float z, float a, float[][][][] local256){
		float[] section = new float[4];
		for(int i = 0; i < 4; i++){
			section[i] = interpolate3d(y,z,a,local256[i]);
		}
		return interpolate1d(x,section[0],section[1],section[2],section[3]);
	}
}
