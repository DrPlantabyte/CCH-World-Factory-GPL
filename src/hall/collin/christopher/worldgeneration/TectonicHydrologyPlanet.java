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

import hall.collin.christopher.worldgeneration.math.DefaultRandomNumberGenerator;
import hall.collin.christopher.worldgeneration.math.SpherePoint;
import hall.collin.christopher.worldgeneration.util.GUI;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * The TectonicHydrologyPlanet class simulates a planet's terrain using an 
 * algorithm that generates mountain ranges along tectonic faults and simulates 
 * the movement of water over the terrain. This planet should give rise to 
 * mountain ranges with rain-shadows.
 * @author CCHall
 */
public class TectonicHydrologyPlanet extends AbstractPlanet{

	
	private final double radius; // in meters
	private final double sunlightIntensity; // in watts per m^2
	private final double atmosphere; // in kPa at sea level
	/** precision of lowest layer of continental noise */
	private final double continentInitialPrecision = 3800000;
	/** used to bias the continent layer towards more (or less) ocean than land */
	private final double oceanBias;
	/** This is the base altitude of a continent interior */
	private final double continentHeight;
	/** This is the base altitude of a ocean interior */
	private final double oceanDepth;
	/** Height factor for mountains */
	private final double mountainHeight;
	
	/** average temperature of the equator of the planet (25C for Earth)*/
	private final double equitorialMeanAnnualTemperature;
	/** polar average temperature relative to global mean*/
	private final double polarTemperatureDelta = -42;
	/** equatorial average temperature relative to global mean*/
	private final double equatorialTemperatureDeltaFromMean = 9;
	/** The drop in temperature (in C) per rise in altitude (in m) */
	final double temperatureDropPerMeter = -10 / 1000.0;
	/** magnitude of temperature variation */
	private final double temperatureNoiseRange = 8;
	
	
	
	/** Precision of the lowest noise frequency, in meters */
	private final double minPrecision = 2300024;
	
	// Noise Layers
	/** Noise layer to determine whether a given point is land or sea */
	PlanetaryScaling3DCoordinateNoiseGenerator continentNoise;
	/** Noise layer used for mountains calculation */
	PlanetaryScaling3DCoordinateNoiseGenerator mountainRangeNoise;
	/** Noise layer used for mountains calculation */
	PlanetaryScaling3DCoordinateNoiseGenerator mountainMagnitudeNoise;
	/** Noise layer for terrain altitude */
	PlanetaryScaling3DCoordinateNoiseGenerator altitudeNoise;
	/** Noise layer for temperature */
	PlanetaryScaling3DCoordinateNoiseGenerator temperatureNoise;
	
	// hydrology sim
	/** Node network sim of water cycle */
	private HydrologyNetworkSimulation hydrologySim;
	/** Number of times to subdivide the network. Total number of nodes is 
	 * as a function of number of subdivisions is as follows (0-5 subdivs): 
	 * 12, 42, 162, 642, 2562, 10242. The number of cells as a function of 
	 * subdivisions C(s) = 20 * 4 ^ s
	 */
	private final int numNetworkSubdivisions = 5;
	/**
	 * Generates an Earth-like planet.
	 * @param seed Seed for random number generator
	 */
	protected TectonicHydrologyPlanet(String seed){
		// protected constructor
		// default values
		radius =  6.371e6;
		sunlightIntensity = 1367;
		atmosphere = 101;
		equitorialMeanAnnualTemperature = solarPowerToTemperature(sunlightIntensity);
		oceanBias = 0.38;
		continentHeight = 200;
		oceanDepth = -4000;
		mountainHeight = 1500;
		
		// init noise layers
		initNoiseLayers(seed);
		
	}
	/**
	 * Generates a planet with specified geographic parameters.
	 * @param seed Seed for random number generator
	 * @param radius_km radius of planet in kilometers (Earth is 6371 km)
	 * @param atmosphere_kPa Density of atmosphere at sea-level in kilopascals 
	 * (Earth is 101 kPa)
	 * @param ocean_fraction approximate fraction of planet covered in ocean 
	 * (actual coverage will be different, Earth is 0.7)
	 * @param solarFlux_wattsPerSqrMeter Solar intensity on the planet, before 
	 * atmospheric scattering/absorption in watts per square meter (Earth is 
	 * 1367 w/m^2)
	 */
	protected TectonicHydrologyPlanet(String seed, double radius_km, 
			double atmosphere_kPa, double ocean_fraction, double solarFlux_wattsPerSqrMeter){
		radius = radius_km * 1000;
		sunlightIntensity = solarFlux_wattsPerSqrMeter;
		atmosphere = atmosphere_kPa;
		equitorialMeanAnnualTemperature = solarPowerToTemperature(sunlightIntensity) +  5 * Math.log(atmosphere_kPa / 101.0); // Arrhenius formula for greenhous effect: ΔF = α Ln(C/C_0)
		oceanBias =  2 * (ocean_fraction - 0.5); 
		continentHeight = 200;
		oceanDepth = -4000;
		mountainHeight = 1500;
		
		// init noise layers
		initNoiseLayers(seed);
	}
	
	private double solarPowerToTemperature(double solar){
		return Math.sqrt(Math.sqrt(solar * 0.3148071235 / 0.0000000567))-273.16;
	}
	/**
	 * Creates a TectonicHydrologyPlanet with the default (Earth-like) settings. 
	 * This operation may take a long time, so a DoubleAccumulator is provided 
	 * to allow other threads to measure (and display) the progress. The 
	 * progress accumulator goes from 0 (start) to 1 (done).
	 * @param seed A string to use as the seed for random number generators.
	 * @param progressTracker Tracker for progress. During the computation, 
	 * increments will be added to to this object such that a complete operation 
	 * will sum to 1.0. This parameter can be null.
	 * @return A new TectonicHydrologyPlanet instance
	 */
	public static TectonicHydrologyPlanet createPlanet(String seed, DoubleAdder progressTracker){
		TectonicHydrologyPlanet p = new TectonicHydrologyPlanet(seed);
		
		p.initialize();
		p.hydrologySim.runSimulation(progressTracker);
		p.postInit();
		if(Thread.currentThread().isInterrupted()) return null; // aborted creation
		return p;
	}
	/**
	 * Creates a TectonicHydrologyPlanet with the default (Earth-like) settings. 
	 * This operation may take a long time, so a DoubleAccumulator is provided 
	 * to allow other threads to measure (and display) the progress. The 
	 * progress accumulator goes from 0 (start) to 1 (done).
	 * @param seed A string to use as the seed for random number generators.
	 * @param radius_km radius of planet in kilometers (Earth is 6371 km)
	 * @param atmosphere_kPa Density of atmosphere at sea-level in kilopascals 
	 * (Earth is 101 kPa)
	 * @param ocean_fraction approximate fraction of planet covered in ocean 
	 * (actual coverage will be different, Earth is 0.7)
	 * @param solarFlux_wattsPerSqrMeter Solar intensity on the planet, before 
	 * atmospheric scattering/absorption in watts per square meter (Earth is 
	 * 1367 w/m^2)
	 * @param progressTracker Tracker for progress. During the computation, 
	 * increments will be added to to this object such that a complete operation 
	 * will sum to 1.0. This parameter can be null.
	 * @return A new TectonicHydrologyPlanet instance
	 */
	public static TectonicHydrologyPlanet createPlanet(String seed,double radius_km, 
			double atmosphere_kPa, double ocean_fraction, double solarFlux_wattsPerSqrMeter, DoubleAdder progressTracker){
		TectonicHydrologyPlanet p = new TectonicHydrologyPlanet(seed, radius_km, 
			 atmosphere_kPa,  ocean_fraction,  solarFlux_wattsPerSqrMeter);
		
		p.initialize();
		p.hydrologySim.runSimulation(progressTracker);
		p.postInit();
		if(Thread.currentThread().isInterrupted()) return null; // aborted creation
		return p;
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
		double x = radius * sin(longitude)*Math.cos(latitude);
		double y = radius * sin(latitude);
		double z = radius * cos(longitude)*Math.cos(latitude);
		double m = 4*mountainRangeNoise.getValue(x, y, z, precision) - 0.5;
		double r = 1.0 / (m * m) * (mountainMagnitudeNoise.getValue(x, y, z, precision)+0.0625);
		return clamp(r,0,2);
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
		double c = clamp(getContinent(longitude, latitude, precision),-1,1);
		double scaler = 1;
		double base;
		if(c >= 0){
			// land
			base = continentHeight * sqrt(c);
		} else {
			// ocean
			base = oceanDepth * (c*c);
		//	scaler = 3;
		}
		double rough = getRoughness(longitude, latitude, precision);
		double crinkle = altitudeNoise.getValue(x, y, z, precision);
		double h = mountainHeight * (crinkle * crinkle) * rough * scaler
				+ base;
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
		return hydrologySim.cubicInterpolatePrecipitation(new SpherePoint(longitude,latitude));
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
		double h = Math.max(0, getAltitude(longitude, latitude, precision));
		double t = (equitorialMeanAnnualTemperature + polarTemperatureDelta * (1 - cos(latitude))) 
				+ temperatureNoise.getValue(x, y, z, precision)
				+ temperatureDropPerMeter * h;
		return t;
	}
	
	/**
	 * Calculates geography data at the given location, returning the 
	 * continent score at the given coordinate.
	 * @param longitude Longitude coordinate of the location of interest
	 * @param latitude Latitude of the location of interest
	 * @param precision Determines how fine-grained the calculation is. E.g. if 
	 * <code>precision</code> is 10km, then the planet generation implementation 
	 * will return a value that is roughly the average of a 10km radius around 
	 * the given coordinate. If making a map from a grid of data points, set 
	 * the precision to the grid spacing.
	 * @return A number where a positive value indicates land and a negative 
	 * value indicates ocean/sea
	 */
	protected double getContinent(double longitude, double latitude, double precision) {
		double x = radius * sin(longitude)*Math.cos(latitude);
		double y = radius * sin(latitude);
		double z = radius * cos(longitude)*Math.cos(latitude);
		return continentNoise.getValue(x, y, z, precision) - oceanBias;
	}

/**
	 * Gets the size of the planet.
	 * @return The radius of the planet, in meters;
	 */
	@Override
	public double getRadius() {
		return radius;
	}

	private void initialize() {
		// set initial values
		hydrologySim = HydrologyNetworkSimulation.createHydrologyNetwork(this, atmosphere);
	}

	private void postInit() {
		// nothing to do
	}
	/**
	 * calculates the fraction of the planet that is ocean, as a percent.
	 * @return The percentage of the planet that is ocean (0-100)
	 */
	public double percentOcean(){
		return 100 * hydrologySim.getNumberWaterSourceNodes() / (double)hydrologySim.getNumberNodes();
	}
	
	private void initNoiseLayers(String seed) {
		// init noise layers
		DefaultRandomNumberGenerator prng = new DefaultRandomNumberGenerator(
				stringHashCode(seed)
		);
		continentNoise = new PlanetaryScaling3DCoordinateNoiseGenerator(
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				continentInitialPrecision,
				1.0);
		mountainRangeNoise = new PlanetaryScaling3DCoordinateNoiseGenerator(
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				minPrecision,
				1.0);
		mountainMagnitudeNoise = new PlanetaryScaling3DCoordinateNoiseGenerator(
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				minPrecision,
				1.0);
		altitudeNoise = new PlanetaryScaling3DCoordinateNoiseGenerator(
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				minPrecision,
				1.0);
		temperatureNoise = new PlanetaryScaling3DCoordinateNoiseGenerator(
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				prng.nextLong(),
				minPrecision,
				temperatureNoiseRange);
	}
	
/** provided for optimization purposes */
	private double sqrt(double a) {
		return Math.sqrt(a);
	}
/** provided for optimization purposes */
	private double cos(double a) {
		return Math.cos(a);
	}
/** provided for optimization purposes */
	private double sin(double a) {
		return Math.sin(a);
	}
	
	private double clamp(double x, double min, double max){
		return Math.min(Math.max(x, min), max);
	}
	
	/** testing only **/
	@Deprecated public static void main(String[] args){
		//for(int t = 1; t <= 9; t++){
			//String seed = String.valueOf(System.currentTimeMillis());
			String seed = "Taerho".toUpperCase();
		
			final int mapSize = 256;
			
			DoubleAdder progressTracker = new DoubleAdder();
			final TectonicHydrologyPlanet[] parr = new TectonicHydrologyPlanet[1];
			final BufferedImage[] bimg = new BufferedImage[1];
			GUI.doWithProgressBar(()->{
				parr[0] = TectonicHydrologyPlanet.createPlanet(seed,progressTracker);
				System.out.println("Planet is "+((int)parr[0].percentOcean())+"% ocean");
				hall.collin.christopher.worldgeneration.graphics.MercatorMapProjector mp = new hall.collin.christopher.worldgeneration.graphics.MercatorMapProjector();
				mp.enableHillshading(false);
				bimg[0] = mp.createMapProjection(parr[0], mapSize, new hall.collin.christopher.worldgeneration.graphics.VegetationPainter(),progressTracker);//*/(new StandardBiomeFactory()).getPlanetPainter(),progressTracker);
			
			}, progressTracker, 2);
			
			
		/*	double[][] altMap = GUI.mapPlanetData(mapSize, (SpherePoint pt)->{
				return p.getAltitude(pt.getLongitude(), pt.getLatitude(), precision);
			});
			GUI.showImagePopupNonmodal(GUI.visualize(altMap, -100, 100),"Continent"); 

			double[][] tempMap = GUI.mapPlanetData(mapSize, (SpherePoint pt)->{
				return p.getTemperature(pt.getLongitude(), pt.getLatitude(), precision);
			});
			GUI.showImagePopupNonmodal(GUI.visualize(tempMap, -15, 50),"Temperature"); 

			double[][] dataMap = GUI.mapPlanetData(mapSize, (SpherePoint pt)->{
				return p.getMoisture(pt.getLongitude(), pt.getLati tude(), precision);
			});
			GUI.showImagePopupNonmodal(GUI.visualize(dataMap, -50, 200),"Water"); 
			*/

			GUI.showImagePopupNonmodal(bimg[0],"Planet '" + seed + "'"); 
		//}
	}

	

}
