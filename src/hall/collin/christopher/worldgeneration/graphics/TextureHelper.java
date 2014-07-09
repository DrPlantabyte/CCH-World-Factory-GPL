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
package hall.collin.christopher.worldgeneration.graphics;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * A collection of methods for handling texture data.
 * @author CCHall
 */
public abstract class TextureHelper {
	/**
	 * Generates a normal map as a pixel array using the provided height map. 
	 * This method assumes that the map doesn't wrap on itself and represents a 
	 * flat surface.
	 * @param heightMap a 1 to 1 mapping of altitude at each pixel as a 2D 
	 * double array (<code>heightMap[x][y]</code>)
	 * @param gridScale This is the size of a grid unit relative to the units 
	 * used to express altitude. E.g. if altitude is in km and the grid density 
	 * is 10m resolution, then <code>gridScale</code> should be 0.01;
	 * @param ampFactor Factor to exaggerate the steepness of the texture.
	 * @param flatOcean If true, negative altitudes will be flattened out
	 * @return A 1D array of pixels representing the normal map texture
	 * (<code>int[x + y * width]</code>).
	 */
	public static BufferedImage heightMapToNormalVectorTextureFlat(double[][] heightMap ,double gridScale,  double ampFactor, boolean flatOcean){
		BufferedImage img = new BufferedImage(heightMap.length,heightMap[0].length,BufferedImage.TYPE_INT_ARGB);
		
		
		for (int y = 0; y < heightMap[0].length; y++) {
			double dx = gridScale;
			double dy = gridScale;
			int yn1 = y - 1;
			if (yn1 < 0) {
				yn1 = 0;
			}
			int yp1 = y + 1;
			if (yp1 >= heightMap[0].length) {
				yp1 = heightMap[0].length - 1;
			}
			for (int x = 0; x < heightMap.length; x++) {
				int i = x + y * heightMap.length;
				
				if(flatOcean && heightMap[x][y] < 0){
					int red = 128 + (0);
					int green = 128 + (0);
					int blue = 128 + (127);
					int px = 0xFF000000 | red << 16 | green << 8 | blue;
					img.setRGB(x, img.getHeight() - y - 1, px);
					continue;
				}

				int xn1 = x - 1;
				if (xn1 < 0) {
					xn1 = 0;
				}
				int xp1 = x + 1;
				if (xp1 >= heightMap.length) {
					xp1 = heightMap.length - 1;
				}

				
				double dzdx = (heightMap[xp1][y] - heightMap[xn1][y]);
				double dzdy = (heightMap[x][yp1] - heightMap[x][yn1]);
				// vectors
				double Ax,Ay,Az;
				double Bx,By,Bz;
				double Cx,Cy,Cz;
				// Vector A: slope vector 
				Ax = 2 * dx;
				Ay = -2 * dy; // flip the Y because image coordinates are mirrored vertically
				Az = (dzdx + dzdy)*0.5*ampFactor;
				// Vector B: vector perpendicular to direction of slope
				Bx = -Ay;
				By = Ax;
				Bz = 0;
				// Vector C: cross product AxB is perpendicular to both A and B
				Cx = Ay*Bz - Az*By;
				Cy = Az*Bx - Ax*Bz;
				Cz = Ax*By - Ay*Bx;
				// Normalizing Vector C produces the normal vector
				double M = Math.sqrt(Cx*Cx + Cy*Cy + Cz*Cz);
				Cx /= M;
				Cy /= M;
				Cz /= M;

				int nx_asPixel = (int) (127 * Cx);
				int ny_asPixel = (int) (127 * Cy);
				int nz_asPixel = (int) (127 * Cz);

				int red = 128 + (nx_asPixel);
				int green = 128 + (ny_asPixel);
				int blue = 128 + (nz_asPixel);
				int px = 0xFF000000 | red << 16 | green << 8 | blue;
				img.setRGB(x, img.getHeight() - y - 1, px);
			}
		}
		
		return img;
	}
	/**
	 * Generates a normal map as a pixel array using the provided height map. 
	 * This method assumes that the map is a UV representation of the surface of 
	 * a sphere and therefore wraps on the x axis.
	 * @param heightMap a 1 to 1 mapping of altitude at each pixel as a 2D 
	 * double array (<code>heightMap[x][y]</code>)
	 * @param gridScaleAtEquator This is the size of a grid unit relative to the units 
	 * used to express altitude, measured at the equator. E.g. if altitude is in 
	 * km and the grid density is 10m resolution at the equator, then 
	 * <code>gridScaleAtEquator</code> should be 0.01;
	 * @param ampFactor Factor to exaggerate the steepness of the texture.
	 * @param flatOcean If true, negative altitudes will be flattened out
	 * @return A buffered image representing the normal map texture (RGB -> XYZ).
	 */
	public static BufferedImage heightMapToNormalVectorTextureUV(double[][] heightMap ,
			double gridScaleAtEquator,  double ampFactor, boolean flatOcean){
		BufferedImage img = new BufferedImage(heightMap.length,heightMap[0].length,BufferedImage.TYPE_INT_ARGB);
		
		
		for (int y = 0; y < heightMap[0].length; y++) {
			double dx = gridScaleAtEquator * Math.sin(Math.PI * (double)y / (double)heightMap[0].length)
					+Float.MIN_VALUE; // prevent divide by 0 errors
		//	double dx = gridScaleAtEquator 
		//			+Float.MIN_VALUE; // prevent divide by 0 errors
			double dy = gridScaleAtEquator;
			int yn1 = y - 1;
			if (yn1 < 0) {
				yn1 = 0;
			}
			int yp1 = y + 1;
			if (yp1 >= heightMap[0].length) {
				yp1 = heightMap[0].length - 1;
			}
			for (int x = 0; x < heightMap.length; x++) {
				int i = x + y * heightMap.length;
				
				if(flatOcean && heightMap[x][y] < 0){
					int red = 128 + (0);
					int green = 128 + (0);
					int blue = 128 + (127);
					int px = 0xFF000000 | red << 16 | green << 8 | blue;
					img.setRGB(x, img.getHeight() - y - 1, px);
					continue;
				}

				int xn1 = x - 1;
				if (xn1 < 0) {
					xn1 += heightMap.length;
				}
				int xp1 = x + 1;
				if (xp1 >= heightMap.length) {
					xp1 -= heightMap.length;
				}

				
				double dzdx = (heightMap[xp1][y] - heightMap[xn1][y]);
				double dzdy = (heightMap[x][yp1] - heightMap[x][yn1]);
				// vectors
				double Ax,Ay,Az;
				double Bx,By,Bz;
				double Cx,Cy,Cz;
				// Vector A: slope vector 
				Ax = 2 * dx;
				Ay = -2 * dy; // flip the Y because image coordinates are mirrored vertically
				Az = (dzdx + dzdy)*0.5*ampFactor;
				// Vector B: vector perpendicular to direction of slope
				Bx = -Ay;
				By = Ax;
				Bz = 0;
				// Vector C: cross product AxB is perpendicular to both A and B
				Cx = Ay*Bz - Az*By;
				Cy = Az*Bx - Ax*Bz;
				Cz = Ax*By - Ay*Bx;
				// Normalizing Vector C produces the normal vector
				double M = Math.sqrt(Cx*Cx + Cy*Cy + Cz*Cz);
				Cx /= M;
				Cy /= M;
				Cz /= M;

				int nx_asPixel = (int) (127 * Cx);
				int ny_asPixel = (int) (127 * Cy);
				int nz_asPixel = (int) (127 * Cz);

				int red = 128 + (nx_asPixel);
				int green = 128 + (ny_asPixel);
				int blue = 128 + (nz_asPixel);
				int px = 0xFF000000 | red << 16 | green << 8 | blue;
				img.setRGB(x, img.getHeight() - y - 1, px);
			}
		}
		
		return img;
	}
	
	public static BufferedImage visualizeMap(double[][] heightMap){
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (int y = 0; y < heightMap[0].length; y++) {
			for (int x = 0; x < heightMap.length; x++) {
				if(heightMap[x][y] > max){
					max = heightMap[x][y];
				} else if(heightMap[x][y] < min){
					min = heightMap[x][y];
				}
			}
		}
		return visualizeMap(heightMap,min,max);
	}
	public static BufferedImage visualizeMap(double[][] heightMap, double min, double max){
		BufferedImage img = new BufferedImage(heightMap.length,heightMap[0].length,BufferedImage.TYPE_INT_ARGB);
		
		double range = (max - min);
		double offset = min;
		for (int y = 0; y < heightMap[0].length; y++) {
			for (int x = 0; x < heightMap.length; x++) {
				float val = (float)(Math.max(0,Math.min((heightMap[x][y] - offset) / range, 1)));
				img.setRGB(x, y, Color.HSBtoRGB(0, 0, val));
			}
		}
		
		return img;
	}
	public static BufferedImage visualizeMap(float[][] heightMap, float min, float max){
		BufferedImage img = new BufferedImage(heightMap.length,heightMap[0].length,BufferedImage.TYPE_INT_ARGB);
		
		float range = (max - min);
		float offset = min;
		for (int y = 0; y < heightMap[0].length; y++) {
			for (int x = 0; x < heightMap.length; x++) {
				float val = (float)(Math.min(0,Math.max((heightMap[x][y] - offset) / range, 1)));
				img.setRGB(x, y, Color.HSBtoRGB(0, 0, val));
			}
		}
		
		return img;
	}
	
	public static BufferedImage generateShadowsFromNormalMap(final BufferedImage colorMap, final BufferedImage normalMap, final float[] sunlightVector3f, final float sunlightIntensity, final float ambientIntensity){
		// port of GLSL fragment shader
		final BufferedImage out = new BufferedImage(normalMap.getWidth(), normalMap.getHeight(), BufferedImage.TYPE_INT_ARGB);
		java.util.concurrent.ForkJoinPool processor = new java.util.concurrent.ForkJoinPool();
		ArrayList<Callable<Object>> tasks = new ArrayList<>(out.getHeight()); // java Lambdas not here yet, sigh...
		
		// do some flipping to make the math work 
		sunlightVector3f[0] = -sunlightVector3f[0];
		sunlightVector3f[1] = sunlightVector3f[1];
		sunlightVector3f[2] = -sunlightVector3f[2];
		
		for (int y = 0; y < out.getHeight(); y++) {
			final int iy = y;
			tasks.add(new Callable() {
				public Object call() {
					for (int x = 0; x < out.getWidth(); x++) {
						int ix = x;

						int px = 0;

						float[] pixelColor = texture2D(colorMap, ix, iy);
						float[] pixelNormal = normalize(add(multiply(texture2D(normalMap, ix, iy), 2f), -1f));
						float diffuse =  dot(sunlightVector3f, pixelNormal);
						float[] fragColor = multiply(pixelColor, ambientIntensity + diffuse);
						fragColor[3] = 1f;

						int red = Math.min(Math.max((int) (fragColor[0] * 255),0), 255);
						int green = Math.min(Math.max((int) (fragColor[1] * 255),0), 255);
						int blue = Math.min(Math.max((int) (fragColor[2] * 255),0), 255);
						int alpha = Math.min(Math.max((int) (fragColor[3] * 255),0), 255);
						px = alpha << 24 | red << 16 | green << 8 | blue;
						out.setRGB(ix, iy, px);
					}
					return null;
				}
			});
		}
		processor.invokeAll(tasks);
		return out;
	}
	/**
	 * Acts like GLSL function of same name
	 * @param x
	 * @param y
	 * @param normalMap
	 * @return 
	 */
	private static float[] texture2D(BufferedImage normalMap,int x, int y) {
		int argb = normalMap.getRGB(x, y);
		float[] vec4 = new float[4];
		vec4[0] = ((argb >> 16) & 0xFF) / 255f;
		vec4[1] = ((argb >>  8) & 0xFF) / 255f;
		vec4[2] = ((argb      ) & 0xFF) / 255f;
		vec4[3] = ((argb >> 24) & 0xFF) / 255f;
		return vec4;
	}

	private static float[] multiply(float[] in, float f) {
		float[] out = new float[in.length];
		for(int i = 0; i < in.length; i++){
			out[i] = in[i]*f;
		}
		return out;
	}
	private static float[] add(float[] in, float f) {
		float[] out = new float[in.length];
		for(int i = 0; i < in.length; i++){
			out[i] = in[i]+f;
		}
		return out;
	}
	public static float[] normalize(float[] in) {
		float[] out = new float[in.length];
		float sum = 0;
		for(int i = 0; i < in.length; i++){
			sum += in[i]*in[i];
		}
		sum = (float)Math.sqrt(sum);
		for(int i = 0; i < in.length; i++){
			out[i] = in[i] / sum;
		}
		return out;
	}
	private static float dot(float[] a, float[] b){
		float sum = 0;
		for(int i = 0; i < a.length && i < b.length; i++){
			sum += a[i]*b[i];
		}
		return sum;
	}
	/**
	 * Creates a hill-shader layer without using any lighting calculations.
	 * @param heightMap
	 * @return 
	 */
	public static BufferedImage generateSimpleHillShader(double[][] heightMap){
		int height = heightMap[0].length;
		int width = heightMap.length;
		BufferedImage shader = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		ArrayList<java.util.concurrent.Callable<Object>> taskList = new ArrayList<>(height);
		// calculate hill-shading
		for (int y = 0; y < height; y++) {
			int fy = y;
			taskList.add(() -> {
				for (int x = 0; x < width; x++) {
				if(Thread.interrupted()){
					// ABORT!
					return false;
				}
					int xl = x - 1;
					if (xl < 0) {
						xl = 0;
					}
					int xr = x + 1;
					if (xr >= width) {
						xr = (width - 1);
					}
					int yb = fy - 1;
					if (yb < 0) {
						yb = 0;
					}
					int yt = fy + 1;
					if (yt >= height) {
						yt = (height - 1);
					}
					double slopey = heightMap[x][yb] - heightMap[x][yt];
					double slopex = heightMap[xr][fy] - heightMap[xl][fy];
					double slopel = 0.7 * slopey + 0.3 * slopex;
					boolean light = slopel > 0;
					int alpha = (int) (Math.abs(slopel) * 0.002 * 127);
					if (alpha > 127) {
						alpha = 127;
					}
					if (light) {
						shader.setRGB(x, height - fy - 1, new java.awt.Color(0xFF, 0xFF, 0xFF, alpha).getRGB());
					} else {
						shader.setRGB(x, height - fy - 1, new java.awt.Color(0x00, 0x00, 0x00, alpha).getRGB());
					}
				}
				return null;
			});
		}
		java.util.concurrent.ForkJoinPool.commonPool().invokeAll(taskList);
		return shader;
	}
}
