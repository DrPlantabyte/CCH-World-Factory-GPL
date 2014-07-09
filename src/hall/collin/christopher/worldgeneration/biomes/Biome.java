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

/**
 * The Biome class is used to classify the biology at a location on a 
 * planet.
 * @author CCHall
 */
public abstract class Biome {
	/**
	 * A T-Score is the rating for how friendly the terrain is to life. It 
	 * ranges from T0 to T3, where T0 is completely devoid of life (e.g. an 
	 * asteroid). T1 is an environment hospitable only to specialized 
	 * microbes (e.g. acidic hotsprings) and T2 represents a harsh environment 
	 * where animals must be highly specialized to survive (e.g. the Sahara 
	 * Desert). T3 is like most green places on Earth, where avoiding life forms 
	 * is much harder than finding them.
	 * @return A number from 0 to 3. Earthly biomes should be either 2 or 3. 
	 */
	public abstract int getTScore();
	
	/**
	 * Gets the name of this biome.
	 * @return The name of this biome.
	 */
	public abstract String getName();
	/**
	 * Returns a string containing the name and T-score of this biome.
	 * @return Returns a string based on the name of this biome
	 */
	@Override
	public String toString(){
		return getName() + " (T" + getTScore() + ")";
	}
	
}
