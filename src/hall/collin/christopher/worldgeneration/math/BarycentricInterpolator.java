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

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * Interpolator class for 2D interpolation of a triangle from reference 
 * data at the corners.
 * @author CCHall
 */
public class BarycentricInterpolator {
	/**
	 * Interpolates the value at a coordinate within a triangle using 3 
	 * reference points
	 * @param x Coordinate to interpolate
	 * @param y Coordinate to interpolate
	 * @param x1 X coordinate of reference point 1
	 * @param y1 Y coordinate of reference point 1
	 * @param v1 Value at reference coordinate point 1
	 * @param x2 X coordinate of reference point 2
	 * @param y2 Y coordinate of reference point 2
	 * @param v2 Value at reference coordinate point 2
	 * @param x3 X coordinate of reference point 3
	 * @param y3 Y coordinate of reference point 3
	 * @param v3 Value at reference coordinate point 3
	 * @return The barycentric interpolation of the value at the given 
	 * coordinate.
	 */
	public static double interpolateTriangle(double x, double y, 
			double x1, double y1, double v1, 
			double x2, double y2, double v2, 
			double x3, double y3, double v3){
		if((x-x1)*(x-x1)+(y-y1)*(y-y1) < Float.MIN_VALUE)return v1;
		if((x-x2)*(x-x2)+(y-y2)*(y-y2) < Float.MIN_VALUE)return v2;
		if((x-x3)*(x-x3)+(y-y3)*(y-y3) < Float.MIN_VALUE)return v3;
		double a1,a2,a3,aTotal;
		a1 = triangleArea(x ,y ,x2,y2,x3,y3);
		a2 = triangleArea(x1,y1,x ,y ,x3,y3);
		a3 = triangleArea(x1,y1,x2,y2,x ,y );
		aTotal = a1+a2+a3;
		return v1*(a1/aTotal) + v2*(a2/aTotal) + v3*(a3/aTotal);
	}
	
	/**
	 * returns the area of a triangle from 3 x,y coordinates
	 * @param x1 coordinate of a corner of the triangle
	 * @param y1 coordinate of a corner of the triangle
	 * @param x2 coordinate of a corner of the triangle
	 * @param y2 coordinate of a corner of the triangle
	 * @param x3 coordinate of a corner of the triangle
	 * @param y3 coordinate of a corner of the triangle
	 * @return The area of the triangle
	 */
	public static double triangleArea(double x1, double y1, double x2, double y2, double x3, double y3){
		double dx12 = x2-x1;
		double dx13 = x3-x1;
		double dy12 = y2-y1;
		double dy13 = y3-y1;
		double m12 = Math.sqrt(dx12*dx12+dy12*dy12);
		double m13 = Math.sqrt(dx13*dx13+dy13*dy13);
		double dotProduct = dx12 * dx13 + dy12 * dy13;
		double cos = dotProduct / (m12*m13);
		if(cos >= 1) return 0;
		if(cos <= -1) return 0;
		double angle = acos(cos);
		return 0.5*(m12 * sin(angle) * m13);
	}
	
	/** implemented here in case of optimization requirements */
	private static double acos(double x){
		return Math.acos(x);
	}
	/** implemented here in case of optimization requirements */
	private static double sin(double x){
		return Math.sin(x);
	}
	/**
	 * Interpolates the value at a coordinate within a triangle using 
	 * barycentric interpolation of the data at the corners.
	 * @param x coordinate of the location to interpolate
	 * @param y coordinate of the location to interpolate
	 * @param p1 coordinate (X,Y) and value (Z) of a corner of the triangle.
	 * @param p2 coordinate (X,Y) and value (Z) of a corner of the triangle.
	 * @param p3 coordinate (X,Y) and value (Z) of a corner of the triangle.
	 * @return 
	 */
	public static double interpolateTriangle(double x, double y, Point3D p1, Point3D p2, Point3D p3) {
		return interpolateTriangle(x,y,p1.getX(),p1.getY(),p1.getZ(),p2.getX(),p2.getY(),p2.getZ(),p3.getX(),p3.getY(),p3.getZ());
	}
	
	
}
