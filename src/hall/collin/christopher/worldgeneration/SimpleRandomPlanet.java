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

import hall.collin.christopher.worldgeneration.biomes.StandardBiomeFactory;
import hall.collin.christopher.worldgeneration.graphics.*;
import hall.collin.christopher.worldgeneration.math.DefaultRandomNumberGenerator;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * A simple implementation of the AbstractPlanet class that is suitable for 
 * making planetary maps that do not approximate real-world science (see 
 * <a href="http://tvtropes.org/pmwiki/pmwiki.php/Main/PatchWorkMap">TV Trope:
 * Patch Work Map</a>).
 * @author CCHall
 */
public class SimpleRandomPlanet extends AbstractPlanet{

	/** Noise layer for temperature */
	final PlanetaryScaling3DCoordinateNoiseGenerator temperatureNoise;
	/** Noise layer for altitude */
	final PlanetaryScaling3DCoordinateNoiseGenerator altitudeNoise;
	/** Noise layer for altitude */
	final PlanetaryScaling3DCoordinateNoiseGenerator altitudeNoise2;
	/** Noise layer for rainfall */
	final PlanetaryScaling3DCoordinateNoiseGenerator rainfallNoise1;
//	/** Noise layer for rainfall */
//	final PlanetaryScaling3DCoordinateNoiseGenerator rainfallNoise2;
	/** radius of the planet, in meters */
	final double radius = 6000000;
	/** Altitude above which is alpine tundra, in meters */
	final double treeline = 2000;
	/** Precision of the lowest noise frequency, in meters */
	final double minPrecision = 2300024;
	/** polar average temperature */
	final double polarTemperature = -20;
	/** equatorial average temperature */
	final double equatorialTemperature = 20;
	/** The drop in temperature (in C) per rise in altitude (in m) */
	final double temperatureDropPerMeter = -8 / 1000.0;
	/** magnitude of temperature variation */
	final double temperatureRange = 15;
	/** global average rainfall */
	final double moistureAverage = 80;
	/** magnitude of rainfall variation */
	final double moistureRange = 40;
	/** The drop in temperature (in C) per rise in altitude (in m) */
	final double moistureChangerPerMeterAltitude = -25 / 1000.0;
	/** global average altitude (make negative to have oceans larger than land) */
	final double altitudeAverage = -250;
	/** magnitude of altitude variation */
	final double altitudeRange = 2000;
	/**
	 * Creates a random fantasy world that is vaguely Earth-like.
	 * @param seed A seed (e.g. the name of the world) for the purpose of random 
	 * number generation.
	 */
	public SimpleRandomPlanet(String seed){
		DefaultRandomNumberGenerator prng = new DefaultRandomNumberGenerator(
				stringHashCode(seed)
		);
		temperatureNoise = new PlanetaryScaling3DCoordinateNoiseGenerator(
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				minPrecision,
				temperatureRange);
		altitudeNoise = new PlanetaryScaling3DCoordinateNoiseGenerator(
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				minPrecision*2,
				altitudeRange);
		altitudeNoise2 = new PlanetaryScaling3DCoordinateNoiseGenerator(
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				minPrecision*2,
				2);
		rainfallNoise1 = new PlanetaryScaling3DCoordinateNoiseGenerator(
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				minPrecision,
				2.0);
//		rainfallNoise2 = new PlanetaryScaling3DCoordinateNoiseGenerator(
//				prng.nextLong(),
//				prng.nextLong(),
//				prng.nextLong(),
//				prng.nextLong(),
//				minPrecision,
//				2.0);
	}
	
/** provided for optimization purposes */
	private double cos(double a) {
		return Math.cos(a);
	}
/** provided for optimization purposes */
	private double sin(double a) {
		return Math.sin(a);
	}
	
	@Deprecated
	public static void main(String[] args){
		String seed = (new Long(System.currentTimeMillis())).toString();
		
		final StandardBiomeFactory biomeFactory = new StandardBiomeFactory();
		
		final Map<Integer,Integer> moistureHistogram = java.util.Collections.synchronizedMap(new HashMap<Integer,Integer>());
		
		
		int size = 300;
		int legendWidth = 150;
		final int width = size*2;
		final int height = size;
		final double precision = 1024;
		java.awt.image.BufferedImage background = new java.awt.image.BufferedImage(width+legendWidth,height,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		final java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width,height,java.awt.image.BufferedImage.TYPE_INT_ARGB);
	//	java.awt.image.BufferedImage shader = new java.awt.image.BufferedImage(width,height,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.image.BufferedImage normals = new java.awt.image.BufferedImage(width,height,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.image.BufferedImage albedo = new java.awt.image.BufferedImage(width,height,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.image.BufferedImage glow = new java.awt.image.BufferedImage(width,height,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		
		final double[][] heightMap = new double[width][height];
		
		final SimpleRandomPlanet planet = new SimpleRandomPlanet(seed);
		final PlanetPainter pp =  /*new AltitudePainter();//*/biomeFactory.getPlanetPainter();
	//	final PlanetPainter pp = new VegetationPainter();
		java.util.concurrent.ForkJoinPool processor = new java.util.concurrent.ForkJoinPool();
		ArrayList<Callable<Object>> tasks = new ArrayList<>(height); // java Lambdas not here yet, sigh...
		for(int y = 0; y < height; y++){
			final int row = y;
			final double lat = ((double)y/(double)height - 0.5)*Math.PI;
tasks.add(new Callable() { // paralellization
public Object call() { // paralellization
			for(int x = 0; x < width; x++){
				double lon = ((double)x/(double)width)*2*Math.PI;
				heightMap[x][height - row - 1] = planet.getAltitude(lon, lat, precision);
				img.setRGB(x, row, pp.getColor(planet, lon, lat, precision, x, row));
				// stats
				double m = planet.getMoisture(lon, lat, precision);
				int binsize = 25;
				int bin = (int)(m / binsize)*binsize;
				if(moistureHistogram.containsKey(bin) == false){
					moistureHistogram.put(bin, 0);
				}
				moistureHistogram.put(bin, moistureHistogram.get(bin)+1);
			}
return null;}}); // paralellization
		}
processor.invokeAll(tasks); // paralellization
		
		// stats
		Integer[] bins = moistureHistogram.keySet().toArray(new Integer[0]);
		Arrays.sort(bins);
		System.out.println("Moisture histogram (precipitation, number points)");
		for(int i = 0; i < bins.length; i++){
			System.out.print(bins[i]);
			if(i < bins.length - 1){
				System.out.print("-");
				System.out.print(bins[i+1]);
			} else {
				System.out.print("+");
			}
			System.out.print("\t");
			System.out.print(moistureHistogram.get(bins[i]));
			System.out.println();
		}

		java.awt.Graphics2D brush = background.createGraphics();
		brush.setColor(java.awt.Color.BLACK);
		brush.fillRect(width, 0, legendWidth, height);
		int ty = 12;
		StandardBiomeFactory.StandardBiome[] biomes = StandardBiomeFactory.allBiomes;
		for(StandardBiomeFactory.StandardBiome b : biomes){
			brush.setColor(new java.awt.Color(b.getColorARGB()));
			brush.drawString(b.getName(), width+12, ty);
			ty += 12;
		}
		
	
		
	//	java.awt.Graphics2D texBrush = img.createGraphics();
	//	texBrush.drawImage(shader, 0, 0, null);
		brush.drawImage(img, 0, 0, null);
		
		javax.swing.JOptionPane.showMessageDialog(null, new javax.swing.JLabel(new javax.swing.ImageIcon(background)));
		//javax.swing.JOptionPane.showMessageDialog(null, new javax.swing.JLabel(new javax.swing.ImageIcon(TextureHelper.visualizeMap(heightMap))));
		//javax.swing.JOptionPane.showMessageDialog(null, new javax.swing.JLabel(new javax.swing.ImageIcon(TextureHelper.visualizeMap(deltaX(heightMap)))));
		//javax.swing.JOptionPane.showMessageDialog(null, new javax.swing.JLabel(new javax.swing.ImageIcon(TextureHelper.visualizeMap(deltaY(heightMap)))));
		normals = TextureHelper.heightMapToNormalVectorTextureUV(heightMap, (planet.radius*2*Math.PI)/normals.getWidth(), 500,true);
		
		// add shadows to 2D map
		brush.drawImage(TextureHelper.generateShadowsFromNormalMap(img, normals, TextureHelper.normalize(new float[]{1f,-2f,-1f}), 1.2f, 0.5f), 0, 0, null);
		javax.swing.JOptionPane.showMessageDialog(null, new javax.swing.JLabel(new javax.swing.ImageIcon(background)));
	//	try {
	//		ImageIO.write(normals, "PNG", new java.io.File(System.getProperty("user.home")+"/Pictures/temp.png"));
	//	} catch (IOException ex) {
	//		Logger.getLogger(SimpleRandomPlanet.class.getName()).log(Level.SEVERE, null, ex);
	//	}
			
			javax.swing.JOptionPane.showMessageDialog(null, new javax.swing.JLabel(new javax.swing.ImageIcon(normals)));
			Graphics2D ab = albedo.createGraphics();
			ab.setColor(new Color(64,64,64));
			ab.fillRect(0, 0, albedo.getWidth(), albedo.getHeight());
		
	}
	
	private static double[][] deltaX(double[][] heightMap) {
		double[][] result = new double[heightMap.length][heightMap[0].length];
		for (int x = 0; x < heightMap.length; x++) {
			int xn1 = x - 1;
			if (xn1 < 0) {
				xn1 += heightMap.length;
			}
			int xp1 = x + 1;
			if (xp1 >= heightMap.length) {
				xp1 -= heightMap.length;
			}

			for (int y = 0; y < heightMap[0].length; y++) {
				int yn1 = y - 1;
				if (yn1 < 0) {
					yn1 = 0;
				}
				int yp1 = y + 1;
				if (yp1 >= heightMap[0].length) {
					yp1 = heightMap[0].length - 1;
				}
				result[x][y] = heightMap[xp1][y] - heightMap[xn1][y];
			}
		}
		return result;
	}
	private static double[][] deltaY(double[][] heightMap) {
		double[][] result = new double[heightMap.length][heightMap[0].length];
		for (int x = 0; x < heightMap.length; x++) {
			int xn1 = x - 1;
			if (xn1 < 0) {
				xn1 += heightMap.length;
			}
			int xp1 = x + 1;
			if (xp1 >= heightMap.length) {
				xp1 -= heightMap.length;
			}

			for (int y = 0; y < heightMap[0].length; y++) {
				int yn1 = y - 1;
				if (yn1 < 0) {
					yn1 = 0;
				}
				int yp1 = y + 1;
				if (yp1 >= heightMap[0].length) {
					yp1 = heightMap[0].length - 1;
				}
				result[x][y] = heightMap[x][yp1] - heightMap[x][yn1];
			}
		}
		return result;
	}

	/**
	 * Calculates geography data at the given location, returning the roughness
	 * score at the given coordinate. Roughness is a measure of how mountainous 
	 * a location is and is generally more useful to fantasy mapping than 
	 * absolute altitude. 
	 * @param longitude Longitude coordinate of the location of interest
	 * @param latitude Latitude of the location of interest
	 * @param precision Determines how fine-grained the calculation is. E.g. if 
	 * <code>precision</code> is 10km, then the planet generation implementation 
	 * will return a value that is roughly the average of a 10km radius around 
	 * the given coordinate. If making a map from a grid of data points, set 
	 * the precision to the grid spacing.
	 * @return Returns a value from 0 to infinity at this coordinate, with a 
	 * value greater than 1 meaning mountains.
	 */
	@Override
	public double getRoughness(double longitude, double latitude, double precision) {
		double h = getAltitude(longitude, latitude, precision) - 300;
		if(h < 0) return 0;
		return Math.sqrt(h)/32.0;
	}

	/**
	 * Calculates geography data at the given location, returning the altitude
	 * at the given coordinate.
	 * @param longitude Longitude coordinate of the location of interest
	 * @param latitude Latitude of the location of interest
	 * @param precision Determines how fine-grained the calculation is. E.g. if 
	 * <code>precision</code> is 10km, then the planet generation implementation 
	 * will return a value that is roughly the average of a 10km radius around 
	 * the given coordinate. If making a map from a grid of data points, set 
	 * the precision to the grid spacing.
	 * @return The altitude at this coordinate. Note that the returned altitude 
	 * is dependant on the precision, with lower precision returning a 
	 * "smoother" representation of altitude.
	 */
	@Override
	public double getAltitude(double longitude, double latitude, double precision) {
		double x = radius * sin(longitude)*Math.cos(latitude);
		double y = radius * sin(latitude);
		double z = radius * cos(longitude)*Math.cos(latitude);
		double h = altitudeAverage + altitudeNoise.getValue(x, y, z, precision) * altitudeNoise2.getValue(x, y, z, precision);
		return h;
	}

	/**
	 * Calculates geography data at the given location, returning the water
	 * availability at the given coordinate.
	 * @param longitude Longitude coordinate of the location of interest
	 * @param latitude Latitude of the location of interest
	 * @param precision Determines how fine-grained the calculation is. E.g. if 
	 * <code>precision</code> is 10km, then the planet generation implementation 
	 * will return a value that is roughly the average of a 10km radius around 
	 * the given coordinate. If making a map from a grid of data points, set 
	 * the precision to the grid spacing.
	 * @return The moisture availability at this coordinate, roughly measured in 
	 * annual mean precipitation minus evaporation (in cm). A negative value 
	 * indicates a desert or arid environment, while a value greater than 200 is 
	 * a rainforest or jungle.
	 */
	@Override
	public double getMoisture(double longitude, double latitude, double precision) {
		double x = radius * sin(longitude)*Math.cos(latitude);
		double y = radius * sin(latitude);
		double z = radius * cos(longitude)*Math.cos(latitude);
		double rainNoise =  rainfallNoise1.getValue(x, y, z, precision);
		double h = altitudeAverage + altitudeNoise.getValue(x, y, z, precision) * altitudeNoise2.getValue(x, y, z, precision);
	//	double m = moistureAverage + moistureRange * Math.signum(rainNoise)*Math.sqrt(Math.abs(rainNoise));
		double m = moistureAverage + moistureRange * rainNoise + moistureChangerPerMeterAltitude * h;
		return m;
	}

	/**
	 * Calculates geography data at the given location, returning the 
	 * temperature at the given coordinate.
	 * @param longitude Longitude coordinate of the location of interest
	 * @param latitude Latitude of the location of interest
	 * @param precision Determines how fine-grained the calculation is. E.g. if 
	 * <code>precision</code> is 10km, then the planet generation implementation 
	 * will return a value that is roughly the average of a 10km radius around 
	 * the given coordinate. If making a map from a grid of data points, set 
	 * the precision to the grid spacing.
	 * @return The annual mean temperature (in °C) at this coordinate
	 */
	@Override
	public double getTemperature(double longitude, double latitude, double precision) {
		double x = radius * sin(longitude)*Math.cos(latitude);
		double y = radius * sin(latitude);
		double z = radius * cos(longitude)*Math.cos(latitude);
		double h = altitudeAverage + altitudeNoise.getValue(x, y, z, precision) * altitudeNoise2.getValue(x, y, z, precision);
		double t = polarTemperature + temperatureNoise.getValue(x, y, z, precision)
				+ (equatorialTemperature - polarTemperature) * cos(latitude) + temperatureDropPerMeter * h;
		return t;
	}
/**
 * Gets the planet's radius.
 * @return The radius in meters.
 */
	@Override
	public double getRadius() {
		return radius;
	}
	
}
