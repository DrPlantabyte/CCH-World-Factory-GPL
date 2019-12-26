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
package hall.collin.christopher.worldgeneration.testapps;

import hall.collin.christopher.worldgeneration.AbstractPlanet;
import hall.collin.christopher.worldgeneration.TectonicHydrologyPlanet;
import hall.collin.christopher.worldgeneration.biomes.StandardBiomeFactory;
import hall.collin.christopher.worldgeneration.graphics.*;
import hall.collin.christopher.worldgeneration.util.GUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * This program generate a procedural planet from a seed provided by the user 
 * and exports it as images to a save file location.
 * @author cybergnome
 */
public class FantasyMapMaker1 {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		boolean tb = false;
		for(String arg : args){
			if(arg.equals("-export")){
				tb = true;
			}
		}
		final boolean sectorExport = tb;
		final JLabel l = new JLabel("Enter the name of your world:");
		final JTextField f = new JTextField(String.valueOf(System.currentTimeMillis()));
		final JLabel l2 = new JLabel("Export image size:");
		final JTextField f2 = new JTextField("512");
		final JButton doit = new JButton("Create World");
		final JFrame frame = makeFrame("Fantasy World Maker",l,f,l2,f2,doit);
		doit.addActionListener((ActionEvent ae)->{
			int size = Integer.parseInt(f2.getText());
			String seed = f.getText();
			frame.setVisible(false);
			Thread t = new Thread(()->{
			try {
				makeWorld(seed, size, sectorExport);
			} catch (InterruptedException ex) {
				System.err.println("Planet generation cnceled by user");
			}});
			t.start();
		});
		SwingUtilities.invokeLater(()->{
			frame.setVisible(true);
		});
	}
	
	private static JFrame makeFrame(String title, JComponent... items){
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		for(JComponent c : items){
			p.add(c);
		}
		JFrame frame = new JFrame(title);
		frame.getContentPane().add(p);
		frame.setLocationRelativeTo(null);
		frame.pack();
		return frame;
	}
	
	private static DoubleAdder makeProgressBar(final double finalAmount){
		final JDialog pframe = new JDialog((JFrame)null,"Computation in progress...",false);
		final DoubleAdder ptracker = new DoubleAdder();
		JPanel pp = new JPanel();
		pp.setLayout(new BoxLayout(pp,BoxLayout.Y_AXIS));
		pp.add(new JLabel("Computing..."));
		final JProgressBar pbar = new JProgressBar();
		pbar.setPreferredSize(new Dimension(300,24));
		pbar.setStringPainted(true);
		pbar.setMaximum((int)(1000*finalAmount));
		pp.add(pbar);
		
		Thread updater = new Thread(()->{
			double amount;
			do{
				amount = ptracker.sum();
				pbar.setValue((int)(1000*amount));
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					break;
				}
			} while(amount < finalAmount);
			pframe.setVisible(false);
		});
		updater.setDaemon(true);
		
		pframe.getContentPane().add(pp);
		pframe.pack();
		pframe.setLocationRelativeTo(null);
		SwingUtilities.invokeLater(()->{pframe.setVisible(true);updater.start();});
		return ptracker;
	}

	
	private static void makeWorld(String seed,int size,final boolean sectorExport) throws InterruptedException{
		final String seedClean = seed.trim().toUpperCase(Locale.US);
		if(sectorExport){
		double pmax = 63;
		DoubleAdder ptracker = makeProgressBar(pmax);
		// make planet
		TectonicHydrologyPlanet planet = TectonicHydrologyPlanet.createPlanet(seedClean, ptracker);
		
		// show preview
		PlanetPainter vp = new VegetationPainter();
		PlanetPainter bp = (new StandardBiomeFactory()).getPlanetPainter();
		MercatorMapProjector previewer = new MercatorMapProjector();
		GUI.showImagePopupNonmodal(previewer.createMapProjection(planet, 256, vp,ptracker), "Preview of planet '"+ seed + "'");
		
		// ask for destination and make folder
		File dest = GUI.askForSaveFile("Choose save destination for world");
		if(dest == null){
			System.err.println("Canceled.");
			System.exit(0);
		}
		dest.mkdir();
		// filename format: worldname_#-sector_maptype.png where # is the sector symbol
		// mape types: basic, biome, landshader, oceanshader, labels


		// make basemap with generic vegetation painter
		List<SectorMapProjector> projector = Arrays.asList(SectorMapProjector.createSectorMaps());
		projector.parallelStream().forEach((SectorMapProjector sector)->{
			sector.enableMap(true);
			sector.enableLandShader(false);
			sector.enableUnderwaterShader(false);
			sector.enableLabels(false);
			BufferedImage map = sector.createMapProjection(planet, size, vp,ptracker);
			writeImageToFile(map, createFile(dest,seed,sector.getSymbol(),"basic"));
		});
		// make basemap with color-coded biome painter
		projector.parallelStream().forEach((SectorMapProjector sector)->{
			sector.enableMap(true);
			sector.enableLandShader(false);
			sector.enableUnderwaterShader(false);
			sector.enableLabels(false);
			BufferedImage map = sector.createMapProjection(planet, size, bp,ptracker);
			writeImageToFile(map, createFile(dest,seed,sector.getSymbol(),"biome"));
		});
		// make labels
		projector.parallelStream().forEach((SectorMapProjector sector)->{
			sector.enableMap(false);
			sector.enableLandShader(false);
			sector.enableUnderwaterShader(false);
			sector.enableLabels(true);
			BufferedImage map = sector.createMapProjection(planet, size, vp,ptracker);
			writeImageToFile(map,  createFile(dest,seed,sector.getSymbol(),"labels"));
		});
		// make land hill-shading map (as transparency to go over the desired basemap)
		projector.parallelStream().forEach((SectorMapProjector sector)->{
			sector.enableMap(false);
			sector.enableLandShader(true);
			sector.enableUnderwaterShader(false);
			sector.enableLabels(false);
			BufferedImage map = sector.createMapProjection(planet, size, vp,ptracker);
			writeImageToFile(map, createFile(dest,seed,sector.getSymbol(),"landshader"));
		});
		// make ocean hill-shading map
		projector.parallelStream().forEach((SectorMapProjector sector)->{
			sector.enableMap(false);
			sector.enableLandShader(false);
			sector.enableUnderwaterShader(true);
			sector.enableLabels(false);
			BufferedImage map = sector.createMapProjection(planet, size, vp,ptracker);
			writeImageToFile(map, createFile(dest,seed,sector.getSymbol(),"oceanshader"));
		});
		// Save mercator overview
		BufferedImage overviewMap = previewer.createMapProjection(planet, size, vp,ptracker);
		writeImageToFile(overviewMap, createFile(dest,seed,"no","mercator-projection"));

		// done

		ptracker.add(0.1); // in case rounding errors reduce the total to less than expected
		System.exit(0);
		} else {
			// just show the mercator map
			double pmax = 3;
			DoubleAdder ptracker = makeProgressBar(pmax);
			// make planet
			AbstractPlanet planet = TectonicHydrologyPlanet.createPlanet(seedClean, ptracker);

			// show preview
			PlanetPainter vp = new VegetationPainter();
			PlanetPainter bp = (new StandardBiomeFactory()).getPlanetPainter();
			MercatorMapProjector previewer = new MercatorMapProjector();
			BufferedImage vimg = previewer.createMapProjection(planet, size, vp,ptracker);
			BufferedImage bimg = previewer.createMapProjection(planet, size, bp,ptracker);
			GUI.showImagePopup(vimg, "Planet '"+ seed + "'");
			File dest = GUI.askForSaveFile("Choose save destination for world map");
			if(dest == null){
				System.err.println("Canceled.");
				System.exit(0);
			}
			File vf = new File(dest.getPath() + "_vegetation.png");
			File bf = new File(dest.getPath() + "_biomes.png");
			try {
				ImageIO.write(vimg, "png", vf);
			} catch (IOException ex) {
				
				ex.printStackTrace(System.err);
				//Logger.getLogger(FantasyMapMaker1.class.getName()).log(Level.SEVERE, null, ex);
			}
			try {
				ImageIO.write(bimg, "png", bf);
			} catch (IOException ex) {
				
				ex.printStackTrace(System.err);
				//Logger.getLogger(FantasyMapMaker1.class.getName()).log(Level.SEVERE, null, ex);
			}
			System.exit(0);
		}
	}
	
	private static File createFile(File dir, String worldname, String sectorSymbol, String mapType){
		worldname = worldname.trim();
		return new File(dir.getPath()+File.separator+worldname+"_"+sectorSymbol+"-sector_"+mapType+".png");
	}
	
	private static void writeImageToFile(BufferedImage bimg, File dest){
		try {
			ImageIO.write(bimg, "png", dest);
		} catch (IOException ex) {
			ex.printStackTrace(System.err);
			//Logger.getLogger(FantasyMapMaker1.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
