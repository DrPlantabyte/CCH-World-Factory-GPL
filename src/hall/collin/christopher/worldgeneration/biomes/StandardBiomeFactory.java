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

package hall.collin.christopher.worldgeneration.biomes;

import hall.collin.christopher.worldgeneration.AbstractPlanet;
import hall.collin.christopher.worldgeneration.graphics.PlanetPainter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Objects;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * This BiomeFactory generates common biomes using empirical (but not necessarily 
 * scientific standard) climate definitions. All T-scores are represented, but 
 * the ocean is represented by a single "Marine" biome rather than differentiate 
 * temperate from tropical waters.
 * @author CCHall
 */
public class StandardBiomeFactory extends BiomeFactory {

	/** T0 biome. Completely lifeless */
	public static final StandardBiomeFactory.StandardBiome MOONSCAPE = new StandardBiomeFactory.StandardBiome("Moonscape",0,new Color(127,127,127));
	
	/**Frozen T1 environment*/
	public static final StandardBiomeFactory.StandardBiome ICESHEET = new StandardBiomeFactory.StandardBiome("Icesheet",1,new Color(255,255,255));
	/**Land that is too hot for plants and therefore is inhabited only by
	 * extremophiles. (T1) */
	public static final StandardBiomeFactory.StandardBiome THERMAL_SPRINGS = new StandardBiomeFactory.StandardBiome("Thermal Springs",1,new Color(224,115,113));
	/** Super dry, salty landscape suitable only for halophiles (T1)*/
	public static final StandardBiomeFactory.StandardBiome SALT_FIELD = new StandardBiomeFactory.StandardBiome("Salt Field",1,new Color(224,170,219));
	
	/**Average temperature is below 0C (T2)*/
	public static final StandardBiomeFactory.StandardBiome TUNDRA = new StandardBiomeFactory.StandardBiome("Tundra",2,new Color(211,247,219));
	/**Average temperature is above 45C (T2)*/
	public static final StandardBiomeFactory.StandardBiome WASTELAND = new StandardBiomeFactory.StandardBiome("Wasteland",2,new Color(164,82,0));
	/** Sand dunes, like the least hospitable parts of the Sahara desert (T2). 
	 * Rain is an extremely rare event that may not happen at all for years*/
	public static final StandardBiomeFactory.StandardBiome SAND_SEA = new StandardBiomeFactory.StandardBiome("Sand Sea",2,new Color(255,255,138));
	
	/** too dry for trees, but not for grasses and shrubs */
	public static final StandardBiomeFactory.StandardBiome PLAINS = new StandardBiomeFactory.StandardBiome("Plains",3,new Color(255,229,0));
	/**Average temperature more than +20C with
 * precipitation flux* over 200cm*/
	public static final StandardBiomeFactory.StandardBiome JUNGLE = new StandardBiomeFactory.StandardBiome("Jungle",3,new Color(0,255,0));
	/**Average temperature range is from +5C to
 * +27C with precipitation flux* between 60cm and 200cm*/
	public static final StandardBiomeFactory.StandardBiome TEMPERATE_FORREST = new StandardBiomeFactory.StandardBiome("Temperate Forest",3,new Color(184,255,50));
	/** Pine/evergreen forests (also known as Taiga).
 * Average temperature range is from -5C to +5C, with a precipitation flux range of 1cm to 100cm (
 * Agricultural and Forest Meteorology. Volumes 98-99, 31 December 1999,
 * Pages 563-578 )*/
	public static final StandardBiomeFactory.StandardBiome BORIAL_FOREST = new StandardBiomeFactory.StandardBiome("Borial Forest",3,new Color(0,127,0));
	/**Has a precipitation flux* of &lt; 0*/
	public static final StandardBiomeFactory.StandardBiome DESERT = new StandardBiomeFactory.StandardBiome("Desert",3,new Color(222,210,119));
	/**Areas that are frequently submerged by tidal, flood, or rain water. This biome is not used at present (requires altitude data). */
	public static final StandardBiomeFactory.StandardBiome SWAMP = new StandardBiomeFactory.StandardBiome("Swamp",3,new Color(119,133,43));
	/**Freshwater. This biome is not used at present (requires rivers and lakes simulation). */
	public static final StandardBiomeFactory.StandardBiome SURFACE_WATER = new StandardBiomeFactory.StandardBiome("Surface Water",3,new Color(0,255,255));
	/**The ocean*/
	public static final StandardBiomeFactory.StandardBiome MARINE = new StandardBiomeFactory.StandardBiome("Marine",3,new Color(0,127,255));
	/** All of the biomes defined by this biome factory in an array */
	public static final StandardBiomeFactory.StandardBiome[] allBiomes = {MARINE,SURFACE_WATER,BORIAL_FOREST,TEMPERATE_FORREST,JUNGLE,SWAMP,PLAINS,DESERT,SAND_SEA,WASTELAND,TUNDRA,SALT_FIELD,THERMAL_SPRINGS,ICESHEET,MOONSCAPE};
	/**
	 * Used to calculate whether a point lies outside a parabolic region
	 */
	private static double boundry(double left, double right, double bottom, double top, double x) {
		if(x < left || x > right){return Double.POSITIVE_INFINITY;}
		
		double midpt = (left + right) / 2;
		
		double deltay21 = bottom - top;
		double deltaY32 = top - bottom;
		double deltaX21 = midpt - left;
		double deltaX32 = right - midpt;
		double deltaXsqr21 = midpt * midpt - left * left;
		double deltaXsqr32 = right * right - midpt * midpt;
		
		double a = (deltaY32 * deltaX21 - deltay21 * deltaX32) / (deltaXsqr32 * deltaX21 - deltaXsqr21 * deltaX32);
		double b = (deltaY32 - a * deltaXsqr32) / deltaX32;
		double c = top - (a * right * right + b * right);
		
		return a * x * x + b * x + c;
	}

	
	private BiomePlanetPainter painterInstance = null;
	/** thread-safe singleton instantiation */
	private synchronized void initPainter(){
		if(painterInstance == null){
			painterInstance = new BiomePlanetPainter();
		}
	}
	/**
	 * Gets a PlanetPainter that paints color-coded biomes.
	 * @return A PlanetPainter instance that paints the biomes by an identifying 
	 * color.
	 */
	public PlanetPainter getPlanetPainter(){
		if(painterInstance == null){
			initPainter();
		}
		return painterInstance;
	}
	/**
	 * Creates an instance of PlanetPainter that colors by biome,
	 * @return A PlanetPainter instance
	 */
	public static PlanetPainter createPlanetPainter(){
		StandardBiomeFactory sbf = new StandardBiomeFactory();
		return sbf.getPlanetPainter();
	}
	/**
	 * Default constructor.
	 */
	public StandardBiomeFactory(){
		// nothing to do
	}
	
	private static final double absoluteMaxTemp = 120; // C
	private static final double absoluteMinTemp = -90; // C
	private static final double absolutMinMoisture = -50; // (cm annual precip.) - (cm annual evap.)
	
	private static final double maxPhotosynthesisTemp = 70; // C
	private static final double minPhotosynthesisTemp = -40; // C
	private static final double algaeMinMoisture = -30; // (cm annual precip.) - (cm annual evap.)
	
	private static final double maxPlantTemp = 40;// C
	private static final double minPlantTemp = -10;// C
	private static final double plantMinMoisture = -10; // (cm annual precip.) - (cm annual evap.)
	
	private static final double moistureSaturation = 300; // (cm annual precip.) - (cm annual evap.)
	
	/**
	 * Assigns a terrestrial (aka non-aquatic) biome based only on temp. (int
	 * degrees C), moisture (cm rainfall - cm evaporation), and altitude (in
	 * meters). While river and lake biomes are not assigned here, MARINE is
	 * returned if the altitude is negative. 
	 * @param averagetemp_c Average mean temperature in degrees celcius
	 * @param cm_moisture Net annual precipitation (rainfall - evaporation) in 
	 * cm.
	 * @return A standard terrestrial biome associated with the given climate 
	 * parameters.
	 */
	public static StandardBiomeFactory.StandardBiome biomeFromTempRainfall(double averagetemp_c, double cm_moisture){
		
		if(cm_moisture < boundry(absoluteMinTemp,absoluteMaxTemp,absolutMinMoisture,moistureSaturation,averagetemp_c)){
			// T0
			return MOONSCAPE;
		} else if(cm_moisture < boundry(minPhotosynthesisTemp,maxPhotosynthesisTemp,algaeMinMoisture,moistureSaturation,averagetemp_c)){
			// T1
			if(cm_moisture < 0){
				return SALT_FIELD;
			}
			if(averagetemp_c < 0){
				return ICESHEET;
			} else {
				return THERMAL_SPRINGS;
			}
		} else if(cm_moisture < boundry(minPlantTemp,maxPlantTemp,plantMinMoisture,moistureSaturation,averagetemp_c)){
			// T2
			if(cm_moisture < 0){
				return SAND_SEA;
			}
			if(averagetemp_c < 15){
				return TUNDRA;
			} else {
				return WASTELAND;
			}
		} else if(cm_moisture >= boundry(minPlantTemp,maxPlantTemp,plantMinMoisture,moistureSaturation,averagetemp_c)){
			// T3
			if(cm_moisture < 25){
				return DESERT;
			} else if(cm_moisture < 65){
				return PLAINS;
			} else {
				if(averagetemp_c < 10){
					return BORIAL_FOREST;
				} else if(averagetemp_c > 20){
					return JUNGLE;
				} else {
					return TEMPERATE_FORREST;
				}
			}
		} else {
			// error
			return MOONSCAPE;
		} 
		
	}
	
	/**
	 * This method calculates the biome for a given location on a planet. 
	 * This BiomeFactory uses known climate data on Earth to make reasonable 
	 * estimates of a biome from temperature and precipitation. A few 
	 * non-Earthly biomes are included (such as "Moonscape") to account for 
	 * inhospitable planets. The biome definitions are focused on terrestrial 
	 * biomes important to fantasy worlds with some simplification, so all 
	 * ocean biomes are lumped together into a single "Marine" biome.
	 * @param planet The planet of interest
	 * @param longitude Longitude coordinate of the location of interest
	 * @param latitude Latitude of the location of interest
	 * @param precision Determines how fine-grained the calculation is. 
	 * @return A biome instance describing the biome at this location.
	 */
	@Override
	public StandardBiomeFactory.StandardBiome getBiome(AbstractPlanet planet, double longitude, double latitude, double precision) {
		double altitude = planet.getAltitude(longitude, latitude, precision);
		double temperature = planet.getTemperature(longitude, latitude, precision);
		double moisture = planet.getMoisture(longitude, latitude, precision);
		// ocean level check
		if(altitude < 0){
			if(temperature < -15){
				// ice cap
				return ICESHEET;
			}
			return MARINE;
		} else {
			return biomeFromTempRainfall(temperature,moisture);
		}
	}
	
	
	/**
	 * This class is a PlanetPainter implementation to make maps with 
	 * color-coded biomes.
	 */
	protected class BiomePlanetPainter extends PlanetPainter{

		
	/**
	 * Gets the base color (no hill-shading) for a single pixel on a map of the 
	 * planet at the given latitude and longitude. The color is based on the 
	 * biome and features no texturing.
	 * @param planet The planet to colorize
	 * @param longitude Longitude coordinate of the location of interest
	 * @param latitude Latitude of the location of interest
	 * @param precision Determines how fine-grained the calculation is. E.g. if 
	 * <code>precision</code> is 10km, then the planet generation implementation 
	 * will return a value that is roughly the average of a 10km radius around 
	 * the given coordinate. If making a map from a grid of data points, set 
	 * the precision to the grid spacing.
	 * @param pixelX X component of the pixel coordinate on the output map 
	 * image.
	 * @param pixelY Y component of the pixel coordinate on the output map 
	 * image.
	 * @return An ARGB pixel integer representing the color to represent the 
	 * given location on the planet (without hill-shading, just the base color).
	 */
	@Override
		public int getColor(AbstractPlanet planet, double longitude, double latitude, double precision, int pixelX, int pixelY) {
			return getBiome(planet,longitude,latitude,precision).getColorARGB();
		}
		
	}
	
	@Deprecated
	public static void main(String[] args){
		
		
		
		int size = 512;
		int offset = 200;
		int width = size + offset;
		int height = size;
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D brush = img.createGraphics();
		brush.setColor(Color.MAGENTA);
		brush.fillRect(offset, 0, size, size);
		brush.setColor(Color.BLACK);
		brush.fillRect(0, 0, offset, height);

		
		double moisture = -100;
		double temperature = -110;
		double moisterPerPx = (400 - moisture)/size;
		double tempPerPx = (150 - temperature)/size; 
		
		int ty = 12;
		for(StandardBiomeFactory.StandardBiome b : allBiomes){
			brush.setColor(new Color(b.getColorARGB()));
			brush.drawString(b.getName(), 0, ty);
			ty += 12;
		}

	
		
		double moistureReset = moisture;
		for(int x = 0; (x + offset) < img.getWidth(); x++){
			temperature += tempPerPx;
			moisture = moistureReset;
			for(int y = 0; y < img.getHeight(); y++){
				moisture += moisterPerPx;
				StandardBiomeFactory.StandardBiome biome = biomeFromTempRainfall(temperature,moisture);
					img.setRGB(x + offset, img.getHeight() - y - 1,biome.getColorARGB());
				
			}
		}

		
		javax.swing.JOptionPane.showMessageDialog(null,new JLabel(new ImageIcon(img)));
		
	}
	/**
	 * Implementation of the Biome class that adds a color-code.
	 */
	public static class StandardBiome extends Biome{

		private final int tScore;
		private final int colorARGB;
		private final String name;
		/**
		 * Constructs a StandardBiome with a name, T-score, and color-code
		 * @param name Name/label of the Biome
		 * @param tScore The T-score of the Biome. The T-score ranges from T0 to 
		 * T3, where T0 is completely devoid of life (e.g. an 
		 * asteroid). T1 is an environment hospitable only to specialized 
		 * microbes (e.g. acidic hotsprings) and T2 represents a harsh environment 
		 * where animals must be highly specialized to survive (e.g. the Sahara 
		 * Desert). T3 is like most green places on Earth, where avoiding life forms 
		 * is much harder than finding them.
		 * @param color The color-code for the Biome
		 */
		public StandardBiome(String name, int tScore, java.awt.Color color){
			colorARGB = color.getRGB();
			this.tScore = tScore;
			this.name = name;
		}
		
		/**
	 * A T-Score is the rating for how friendly the terrain is to life. 
	 * @return A number from 0 to 3. Earthly biomes should be either 2 or 3. 
	 */
	@Override
		public int getTScore() {
			return tScore;
		}

		/**
	 * Gets the name of this biome.
	 * @return The name of this biome.
	 */
	@Override
		public String getName() {
			return name;
		}
		
		/**
		 * Gets the color-code for the Biome
		 * @return An ARGB pixel int representing this biome's color-code
		 */
		public int getColorARGB() {
			return colorARGB;
		}
		/**
		 * HashCode override.
		 * @return A hash code using the name and T-score
		 */
		@Override
		public int hashCode(){
			return 57 * tScore + name.hashCode();
		}
		/**
		 * Two StandardBiomes are equal if their name and T-score are the same.
		 * @param obj Other object to test
		 * @return True or false
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final StandardBiome other = (StandardBiome) obj;
			if (this.tScore != other.tScore) {
				return false;
			}
			if (!Objects.equals(this.name, other.name)) {
				return false;
			}
			return true;
		}
	}
	
}
