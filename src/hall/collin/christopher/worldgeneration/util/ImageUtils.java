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

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

/**
 * This class provides some static methods for displaying images using 
 * Swing.
 * @author CCHall
 */
public class ImageUtils {
	/**
	 * Creates a modal pop-up window showing an image.
	 * @param img An image to show on the screen in a pop-up window.
	 */
	public static void showImage(BufferedImage img){
		JLabel ic = new JLabel(new ImageIcon(img));
		JScrollPane scroller = new JScrollPane(ic);
		JDialog popup = new JDialog();
		popup.getContentPane().setLayout(new FlowLayout());
		popup.getContentPane().add(scroller);
		popup.getContentPane().validate();
		popup.setModal(true);
		popup.pack();
		popup.setVisible(true);
	}
	/**
	 * Asks the user for a file and then saves the image to that file.
	 * @param img The image to save to file
	 * @param extension The format of the image (e.g. "png", "jpg", or "gif", 
	 * see <code>javax.imageio.ImageIO.write(...)</code>.
	 * @throws IOException thrown if the file cannot be written to (typically 
	 * caused by read/write permission conflicts).
	 */
	public static void saveImage(BufferedImage img, String extension) throws IOException{
		JFileChooser fc = new JFileChooser(".");
		int action = fc.showSaveDialog(null);
		if(action == JFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null){
			File f = fc.getSelectedFile();
			if(f.getPath().toLowerCase().endsWith("."+extension.toLowerCase()) == false){
				f = new File(f.getPath() + "." + extension);
			}
			if(f.exists()){
				int click = JOptionPane.showConfirmDialog(null, "File '" + f.getPath() + "' already exists. Replace it?");
				if(click == JOptionPane.CANCEL_OPTION){return;}
			}
			ImageIO.write(img, extension, f);
		}
	}
}
