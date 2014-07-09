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

package hall.collin.christopher.worldgeneration.util;

import hall.collin.christopher.worldgeneration.AbstractPlanet;
import hall.collin.christopher.worldgeneration.math.SpherePoint;
import java.awt.Color;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Function;
import javax.swing.*;

/**
 * A collection of useful GUI methods.
 * @author CCHall
 */
public abstract class GUI {
	/**
	 * Creates a modal pop-up window showing the provided image.
	 * @param bimg Image to show
	 * @param title Title of the window
	 */
	public static void showImagePopup(BufferedImage bimg, String title){
		JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(bimg)), title, JOptionPane.INFORMATION_MESSAGE);
	}
	/**
	 * Creates a non-modal pop-up window showing the provided image.
	 * @param bimg Image to show
	 * @param title Title of the window
	 */
	public static void showImagePopupNonmodal(BufferedImage bimg, String title){
		JDialog d = new javax.swing.JDialog((Frame)null, title, false);
		d.getContentPane().add(new JLabel(new ImageIcon(bimg)));
		d.pack();
		d.setVisible(true);
	}
	/**
	 * Visualizes a data map as a grey-scale image
	 * @param data a 2D array of data to visualize
	 * @param min Minimum cutoff (black)
	 * @param max Maximum cutoff (white)
	 * @return A greyscale image depicting the data
	 */
	public static BufferedImage visualize(double[][] data, double min, double max){
		int h = getMaxLength(data), w = data.length;
		final BufferedImage bimg = new BufferedImage(w, h,BufferedImage.TYPE_INT_ARGB);
		List<Callable<Boolean>> taskList = new ArrayList<>();
		for(int x = 0; x < w; x++){
			final int px = x;
			taskList.add(new Callable<Boolean>(){
				public Boolean call(){
					for(int y = 0; y < h; y++){
						double n = normalize(data[px][y],min,max);
						bimg.setRGB(px, h-y-1, colorLUT(n));
					}
					return true;
				}
			});
		}
		ForkJoinPool.commonPool().invokeAll(taskList);
		return bimg;
	}
	
	private static int getMaxLength(double[][] array2D){
		int max = 0; 
		for(int i = 0; i < array2D.length; i++){
			if(array2D[i].length > max)max = array2D[i].length;
		}
		return max;
	}
	
	private static double normalize(double x, double min, double max){
		if(x >= max) return 1;
		if(x <= min) return 0;
		return (x - min) / (max - min);
	}
	
	public static int colorLUT(double d){
		float n = (float)d;
		final float blue = (float)(240.0/360.0);
		if(n < 0.2){
			return Color.HSBtoRGB(blue, 1f, Math.max(n, 0)*5);
		} else if(n < 0.8){
			n = 1-((n - 0.2f) / 0.6f);
			return Color.HSBtoRGB(blue*n, 1f, 1f);
		} else {
			n = (n - 0.8f) / 0.2f;
			return Color.HSBtoRGB(0, 1-n, 1f);
		}
	}
	/**
	 * Maps spherical data into a cylindrical (Mercator) projection in a 2D 
	 * array of doubles.
	 * @param size The height of the 2D array (the width will be twice this)
	 * @param calculation A Function (typically a lambda) of what value to 
	 * calculate at each (longitude,latitude) coordinate.
	 * @return 
	 */
	public static double[][] mapPlanetData(final int size,Function<SpherePoint,Double> calculation){
		final double[][] data = new double[size*2][size];
		List<Callable<Object>> tasks = new ArrayList<>(size);
		for(int ix = 0; ix < (size*2); ix++){
			final int x = ix;
			double longitude = Math.PI * ((double)ix / (double)size);
			for(int iy = 0; iy < (size); iy++){
				final int y = iy;
				double latitude = Math.PI * (((double)iy / (double)size)-0.5);
				final SpherePoint pt = new SpherePoint(longitude,latitude);
				tasks.add(()->{
					data[x][y] = calculation.apply(pt);
					return null;
				});
			}
		}
		ForkJoinPool.commonPool().invokeAll(tasks);
		return data;
	}
	
	public static void doWithProgressBar(Runnable function, DoubleAdder progressTracker, double progressMax){
		final Future<?> task = ForkJoinPool.commonPool().submit(function);
		JLabel l = new JLabel("Computation in progress...");
		JProgressBar pbar = new JProgressBar();
		pbar.setMinimum(0);
		pbar.setValue(0);
		pbar.setMaximum((int)(1000*progressMax));
		pbar.setIndeterminate(false);
		pbar.setStringPainted(true);
		final JDialog d = new javax.swing.JDialog((Frame)null, "Please wait...", true);
		Thread updateThread = new Thread(new Runnable(){

			@Override
			public void run() {
				while(task.isDone() == false && task.isCancelled() == false){
					try{
						pbar.setValue((int)(1000*progressTracker.sum()));
						pbar.repaint();
						Thread.sleep(100);
					} catch(InterruptedException ex){
						break;
					}
				}
				if(d.isVisible()){
					d.setVisible(false);
					javax.swing.SwingUtilities.invokeLater(()->d.dispose());
				}
			}
		});
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		p.add(l);
		p.add(pbar);
		d.getContentPane().add(p);
		d.pack();
		d.setLocationRelativeTo(null);
		updateThread.start();
		if(task.isDone() == false && task.isCancelled() == false)d.setVisible(true);
	}

	public static File askForFile(String title) {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle(title);
		int action = jfc.showOpenDialog(null);
		if(action != JFileChooser.APPROVE_OPTION){
			return null;
		}
		return jfc.getSelectedFile();
	}
	
	public static File askForSaveFile(String title) {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle(title);
		int action = jfc.showSaveDialog(null);
		if(action != JFileChooser.APPROVE_OPTION){
			return null;
		}
		return jfc.getSelectedFile();
	}
}
