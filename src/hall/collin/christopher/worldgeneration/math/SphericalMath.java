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

import java.util.Arrays;

/**
 * This class contains methods for spherical geometry and trigonometry. 
 * If these methods become performance bottlenecks, extend this class and 
 * override the offending method with a faster implementation. Note that even 
 * hardware-accelerated ArcCos and ArcSin functions are relatively slow.
 * @author CCHall
 */
public class SphericalMath {
	private static final double PI = Math.PI;
	
	private static SphericalMath singleton = null;
	/**
	 * Initialization. Does not do anything.
	 */
	protected SphericalMath(){
		// defaults
	}
	/**
	 * Singleton instantiation.
	 * @return A singleton instance of this class.
	 */
	public static SphericalMath getInstance(){
		if(singleton == null){
			singleton = new SphericalMath();
		}
		return singleton;
	}
	/**
	 * Calculates the haversine of an angle. Haversine is used in spherical 
	 * trigonometry and is defined as sin(a / 2)<super>2</super>.
	 * @param a An angle, in radians
	 * @return the haversine of the angle (from 0 to 1)
	 */
	public double haversine(double a){
		double sinHalfAngle = sin(0.5 * a);
		//double h = pow(sinHalfAngle,2); // too much overhead for this performance-limiting method
		double h = sinHalfAngle * sinHalfAngle;
		if(h > 1d){
			// not possible! (mathmetically, but can happen in digital approximations)
			return 1d;
		}
		return h;
	}
	/**
	 * Calculates the haversine angle from the given value. Haversine is used in 
	 * spherical trigonometry and is defined as sin(a / 2)<super>2</super>.
	 * @param h A value in the valid range of haversine outputs (0 to 1)
	 * @return The angle whose haversine is equal to <code>h</code>.
	 */
	public double inverseHaversine(double h){
		double a = 2 * arcSin(sqrt(h));
		return a;
	}
	/**
	 * Calculates the bearing (angle from North, clock-wise) from point a to 
	 * point b, when standing at point a. Defined by 
	 * &theta;&nbsp;=atan2(sin(&Delta;long)*cos(lat<sub>2</sub>),cos(lat<sub>1</sub>)*sin(lat<sub>2</sub>) &minus; sin(lat<sub>1</sub>)*cos(lat<sub>2</sub>)*cos(&Delta;long))
	 * The reverse bearing will be equal to this value plus PI
	 * @param a
	 * @param b
	 * @return 
	 */
	public double bearing(SpherePoint a, SpherePoint b){
		double deltaLon = b.getLongitude() - a.getLongitude();
		double lat1 = a.getLatitude();
		double lat2 = b.getLatitude();
		double theta = arcTan2(sin(deltaLon)*cos(lat2),cos(lat1)*sin(lat2)-sin(lat1)*cos(lat2)*cos(deltaLon));
		return theta;
	}
	/**
	 * Calculates the angle of an arc from point a to point b (equivalent to the 
	 * distance on a unit sphere).
	 * @param a
	 * @param b
	 * @return The angle of an arc from point a to point b (unit sphere 
	 * distance).
	 */
	public double angularDistance(SpherePoint a, SpherePoint b){
		return angularDistance(a.getLongitude(), a.getLatitude(), b.getLongitude(), b.getLatitude());
	}
	/**
	 * Calculates the angle of an arc from point a to point b (equivalent to the 
	 * distance on a unit sphere).
	 * @param lon1 Longitude (in radians) of point 1
	 * @param lat1 Latitude (in radians) of point 1
	 * @param lon2 Longitude (in radians) of point 1
	 * @param lat2 Latitude (in radians) of point 1
	 * @return The angle of an arc from point a to point b (unit sphere 
	 * distance).
	 */
	public double angularDistance(double lon1, double lat1, double lon2, double lat2){
	//	return inverseHaversine(haversine(lat2 - lat1) + cos(lat1)*cos(lat2) * haversine(deltaLon));
		double p1 = haversine(lat2 - lat1);
		double p2 = cos(lat1)*cos(lat2);
		double p3 = haversine(lon2 - lon1);
		double p4 = inverseHaversine(p1 + p2 * p3);
		return p4;
	}
	/**
	 * Calculates the area of a triangle defined by three points on a unit 
	 * sphere (also known as "spherical excess").
	 * @param lon1 Longitude of point 1
	 * @param lat1 Latitude of point 1
	 * @param lon2 Longitude of point 2
	 * @param lat2 Latitude of point 2
	 * @param lon3 Longitude of point 3
	 * @param lat3 Latitude of point 3
	 * @return The "spherical excess" of the triangle, which is multiplied by 
	 * the radius squared gives you the surface area of the triangle.
	 */
	public double relativeArea(
			double lon1, double lat1,
			double lon2, double lat2,
			double lon3, double lat3){
		// According to wolfram.com:
		//  Area=R^2[(A+B+C)-pi]=R^2E, where A,B, &C are the angles of the triangle
		//  and R is the radius and E is the Spherical Excess
		
		/////////////////////////////////////////////////////////////
		// double A = angleBetween(lon1,lat1,lon2,lat2,lon3,lat3); //
		// double B = angleBetween(lon2,lat2,lon3,lat3,lon1,lat1); //
		// double C = angleBetween(lon3,lat3,lon1,lat1,lon2,lat2); //
		// double E = A+B+C-PI;                                    //
		// return E;                                               //
		/////////////////////////////////////////////////////////////
		// The above code has been inlined below to improve bit-resolution
		double b = angularDistance(lon1,lat1,lon3,lat3);
		double a = angularDistance(lon2,lat2,lon3,lat3);
		double c = angularDistance(lon2,lat2,lon1,lat1);
		// very small angles  mean it is better to take the square-root before doing division
		double numA = Math.max(haversine(a)-haversine(c-b),0); // approximation artifact protections
		double numB = Math.max(haversine(b)-haversine(a-c),0); // approximation artifact protections
		double numC = Math.max(haversine(c)-haversine(b-a),0); // approximation artifact protections
		double denA = sin(c)*sin(b);
		double denB = sin(a)*sin(c);
		double denC = sin(b)*sin(a);
		double A = (numA < denA) ? 2 * arcSin(Math.sqrt(numA)/Math.sqrt(denA)) : 0.5*PI;
		double B = (numB < denB) ? 2 * arcSin(Math.sqrt(numB)/Math.sqrt(denB)) : 0.5*PI;
		double C = (numC < denC) ? 2 * arcSin(Math.sqrt(numC)/Math.sqrt(denC)) : 0.5*PI;
		double E = A+B+C-PI; 
		return E; 
	}
	
	/**
	 * Calculates the angle on the surface of a sphere at coordinate B between 
	 * coordinates A and C. The coordinates are provided in the order BAC (in 
	 * geometry these points describe angle ABC).
	 * @param B coordinate at point B (the location where was are 
	 * measuring the angle) in angle ABC
	 * @param A coordinate at point A in angle ABC
	 * @param C coordinate at point C in angle ABC
	 * @return The angle at the first coordinate formed by drawing an arc 
	 * connecting the other two points.
	 */
	public double angleBetween(SpherePoint B, SpherePoint A, SpherePoint C){
		return angleBetween(B.getLongitude(), B.getLatitude(), 
				A.getLongitude(), A.getLatitude(), 
				C.getLongitude(), C.getLatitude());
	}
	/**
	 * Calculates the angle on the surface of a sphere at coordinate B between 
	 * coordinates A and C. The coordinates are provided in the order BAC (in 
	 * geometry these points describe angle ABC).
	 * @param originLon coordinate at point B (the location where was are 
	 * measuring the angle) in angle ABC
	 * @param originLat coordinate at point B (the location where was are 
	 * measuring the angle)
	 * @param lon1 coordinate at point A in angle ABC
	 * @param lat1 coordinate at point A in angle ABC
	 * @param lon2 coordinate at point C in angle ABC
	 * @param lat2 coordinate at point C in angle ABC
	 * @return The angle at the first coordinate formed by drawing an arc 
	 * connecting the other two points.
	 */
	public double angleBetween(double originLon, double originLat, // B
			double lon1, double lat1, // A
			double lon2, double lat2){ // C
		// Angle B  in spherical triangle ABC = inversehsin((hsin(b)-hsin(a-c))/(sin(a)sin(c)))
		double a = angularDistance(originLon,originLat,lon2,lat2);
		double b = angularDistance(lon1,lat1,lon2,lat2);
		double c = angularDistance(lon1,lat1,originLon,originLat);
		return inverseHaversine((haversine(b)-haversine(c-a))/(sin(a)*sin(c)));
	}
	/**
	 * Forces a value to lie between the two limits.
	 * @param d A value to constrain
	 * @param min Min value
	 * @param max Max value
	 * @return If d &lt; min, returns min. If d &gt; max, returns max. Otherwise 
	 * returns d
	 */
	protected double constrain(double d, double min, double max){
		if(d < min){
			return min;
		}
		if(d > max){
			return max;
		}
		return d;
	}
	/**
	 * Calculates the area of a triangle defined by three points on the sphere.
	 * @param p1 Coordinate of point 1
	 * @param p2 Coordinate of point 2
	 * @param p3 Coordinate of point 3
	 * @return The "spherical excess" of the triangle, which is multiplied by 
	 * the radius squared gives you the surface area of the triangle.
	 */
	public double relativeArea(SpherePoint p1, SpherePoint p2, SpherePoint p3){
		return relativeArea(p1.getLongitude(), p1.getLatitude(), 
				p2.getLongitude(), p2.getLatitude(),
				p3.getLongitude(), p3.getLatitude());
	}
	/**
	 * Calculates the area of a triangle defined by three points on the sphere.
	 * @param p1 Coordinate of point 1
	 * @param p2 Coordinate of point 2
	 * @param p3 Coordinate of point 3
	 * @param radius The radius of the sphere
	 * @return The surface area of the triangle.
	 */
	public double area(SpherePoint p1, SpherePoint p2, SpherePoint p3, double radius){
		return radius * radius * relativeArea(p1,p2,p3);
	}
	/**
	 * Calculates the area of a triangle defined by three points on the sphere.
	 * @param lon1 Longitude of point 1
	 * @param lat1 Latitude of point 1
	 * @param lon2 Longitude of point 2
	 * @param lat2 Latitude of point 2
	 * @param lon3 Longitude of point 3
	 * @param lat3 Latitude of point 3
	 * @param radius The radius of the sphere
	 * @return The surface area of the triangle.
	 */
	public double area(
			double lon1, double lat1,
			double lon2, double lat2,
			double lon3, double lat3,
			double radius){
		return radius * radius * relativeArea(lon1,lat1,lon2,lat2,lon3,lat3);
	}
	
	/**
	 * Calculates the shortest surface distance from point a to point b.
	 * @param a
	 * @param b
	 * @param radius The radius of the sphere
	 * @return surface distance from point a to point b.
	 */
	public double greatCircleDistance(SpherePoint a, SpherePoint b, double radius){
		return radius * angularDistance(a,b);
	}
	/**
	 * Calulates where to place a point in a great circle that intersects the 
	 * origin point with the given bearing such that the unit-sphere distance 
	 * (angle) between the two points is equal to the provided angularDistance.
	 * <p/>Defined by 
	 * lat<sub>2</sub>&nbsp;= asin(sin(lat<sub>1</sub>)*cos(d/R) + cos(lat<sub>1</sub>)*sin(d/R)*cos(&theta;))
	 * @param origin
	 * @param bearing
	 * @param angularDistance
	 * @return The point along a great circle arc that is a specified unit-sphere 
	 * distance (the arcAngle) from the origin point following the given bearing 
	 * (angle from North, clock-wise) from the origin.
	 */
	public SpherePoint followBearing(SpherePoint origin, double bearing, double angularDistance){
		double lon1 = origin.getLongitude();
		double lat1 = origin.getLatitude();
		double lat2 = arcSin( sin(lat1)*cos(angularDistance) + 
              cos(lat1)*sin(angularDistance)*cos(bearing) );
		double lon2 = lon1 + arcTan2(sin(bearing)*sin(angularDistance)*cos(lat1), 
                     cos(angularDistance)-sin(lat1)*sin(lat2));
		return new SpherePoint(lon2,lat2);
	}
	
	// overridable math invocations
	/**
	 * Same as Math.asin(...), can be overridden with faster implementation if 
	 * this method becomes a bottleneck.
	 */
	protected double arcSin(double a){
		return Math.asin(a);
	}
	/**
	 * Same as Math.acos(...), can be overridden with faster implementation if 
	 * this method becomes a bottleneck.
	 */
	protected double arcCos(double a){
		return Math.acos(a);
	}
	/**
	 * Same as Math.atan(...), can be overridden with faster implementation if 
	 * this method becomes a bottleneck.
	 */
	protected double arcTan(double a){
		return Math.atan(a);
	}
	/**
	 * Same as Math.atan2(...), can be overridden with faster implementation if 
	 * this method becomes a bottleneck.
	 */
	protected double arcTan2(double a,double b){
		return Math.atan2(a, b);
	}
	/**
	 * Same as Math.sin(...), can be overridden with faster implementation if 
	 * this method becomes a bottleneck.
	 */
	protected double sin(double a){
		return Math.sin(a);
	}
	/**
	 * Same as Math.cos(...), can be overridden with faster implementation if 
	 * this method becomes a bottleneck.
	 */
	protected double cos(double a){
		return Math.cos(a);
	}
	/**
	 * Same as Math.tan(...), can be overridden with faster implementation if 
	 * this method becomes a bottleneck.
	 */
	protected double tan(double a){
		return Math.tan(a);
	}
	/**
	 * Same as Math.sqrt(...), can be overridden with faster implementation if 
	 * this method becomes a bottleneck.
	 */
	protected double sqrt(double a){
		return Math.sqrt(a);
	}
	/**
	 * Same as Math.pow(...), can be overridden with faster implementation if 
	 * this method becomes a bottleneck.
	 */
	protected double pow(double b, double e){
		return Math.pow(b, e);
	}
	/**
	 * Same as Math.pow(...), can be overridden with faster implementation if 
	 * this method becomes a bottleneck.
	 */
	protected double pow(double b, int e){
		if(e == 2){
			return b * b;
		}
		if(e > 1){
			double r = 1;
			for(int i = 0; i < e; i++){
				r *= b;
			}
			return r;
		} else if(e == 1){
			return b;
		} else {
			double r = 1;
			for(int i = 0; i > e; i++){
				r *= b;
			}
			return 1.0 / r;
		}
	}
	/**
	 * Calculates the the closest group of points to an origin point, excluding 
	 * the origin point itself.
	 * @param origin Source point
	 * @param n Number of points to return
	 * @param coords An array of points to check
	 * @return An array of the closest points, in order of closest to 
	 * farthest.
	 */
	public SpherePoint[] closestNPoints(SpherePoint origin, int n, SpherePoint... coords){
		SpherePoint[] results = new SpherePoint[n];
		double[] dists = new double[n];
		Arrays.fill(dists, Double.MAX_VALUE);
		for(int i = 0; i < coords.length; i++){
			SpherePoint pt = coords[i];
			if(pt == origin){
				continue;
			}
			double dist = angularDistance(origin, pt);
			for(int x = 0; x < dists.length; x++){
				if(dist < dists[x]){
					// shift elements down and insert this element
					SpherePoint t = pt;
					double d = dist;
					for(int k = x; k < dists.length; k++){
						results[k] = t;
						dists[k] = d;
						if(k < (dists.length - 1)) {
							t = results[k+1];
							d = dists[k+1];
						}
					}
					break;
				}
			}
		}
		return results;
	}
	/**
	 * Calculates the the closest group of points to an origin point, excluding 
	 * the origin point itself.
	 * @param origin Source point
	 * @param n Number of points to return
	 * @param coords An array of points to check
	 * @return An array of the closest points, in order of closest to 
	 * farthest.
	 */
	public Point3D[] closestNPoints(Point3D origin, int n, Point3D... coords){
		Point3D[] results = new Point3D[n];
		double[] dists = new double[n];
		Arrays.fill(dists, Double.MAX_VALUE);
		for(int i = 0; i < coords.length; i++){
			Point3D pt = coords[i];
			if(pt == origin){
				continue;
			}
			double dist = (origin.getX() - pt.getX())*(origin.getX() - pt.getX())
					+(origin.getY() - pt.getY())*(origin.getY() - pt.getY())
					+(origin.getZ() - pt.getZ())*(origin.getZ() - pt.getZ());
			for(int x = 0; x < dists.length; x++){
				if(dist < dists[x]){
					// shift elements down and insert this element
					Point3D t = pt;
					double d = dist;
					for(int k = x; k < dists.length; k++){
						results[k] = t;
						dists[k] = d;
						if(k < (dists.length - 1)) {
							t = results[k+1];
							d = dists[k+1];
						}
					}
					break;
				}
			}
		}
		return results;
	}
	/**
	 * CAlculates the midpoint between two points on the surface of a 
	 * sphere
	 * @param a Point a
	 * @param b Point b
	 * @return The midpoint on a great circle arc from point a to point b.
	 */
	public SpherePoint midpoint(SpherePoint a, SpherePoint b){
		double lon1 = a.getLongitude();
		double lat1 = a.getLatitude();
		double lon2 = b.getLongitude();
		double lat2 = b.getLatitude();
		double d = angularDistance(a,b);
		double A = sin((0.5) * d) / sin(d);
		double B = sin(0.5 * d) / sin(d);
		double x = A * cos(lat1) * cos(lon1) + B * cos(lat2) * cos(lon2);
		double y = A * cos(lat1) * sin(lon1) + B * cos(lat2) * sin(lon2);
		double z = A * sin(lat1) + B * sin(lat2);
		double lat = arcTan2(z, sqrt(x * x + y * y));
		double lon = arcTan2(y, x);
		return new SpherePoint(lon,lat);

	}
	/**
	 * Calculates the (X,Y,Z) coordinates of a (longitude,latitude) point. It 
	 * assumes a radius of 1 (unit sphere);
	 * @param a The (longitude,latitude) coordinate to convert
	 * @return The 3D coorinate of a unit vector from the center of the 
	 * sphere to the given spherical coordinate
	 */
	public Point3D lonLatTo3D(SpherePoint a){
		double X = Math.sin(a.getLongitude())*Math.cos(a.getLatitude());
		double Y = Math.sin(a.getLatitude());
		double Z = Math.cos(a.getLongitude())*Math.cos(a.getLatitude());
		return new Point3D(X,Y,Z);
	}
	/**
	 * Calculates the (X,Y,Z) coordinates of a (longitude,latitude) point on a 
	 * sphere with the specified radius.
	 * @param a The (longitude,latitude) coordinate to convert
	 * @param r The radius of the sphere
	 * @return The 3D coordinate of a vector from the center of the 
	 * sphere to the given spherical coordinate
	 */
	public Point3D lonLatTo3D(SpherePoint a, double r) {
		double X = r*Math.sin(a.getLongitude())*Math.cos(a.getLatitude());
		double Y = r*Math.sin(a.getLatitude());
		double Z = r*Math.cos(a.getLongitude())*Math.cos(a.getLatitude());
		return new Point3D(X,Y,Z);
	}
	public SpherePoint point3DToLonLat(Point3D a){
		double x = a.getX();
		double y = a.getY();
		double z = a.getZ();
		return point3DToLonLat(x,y,z);
	}
	public SpherePoint point3DToLonLat(double x, double y, double z){
		double radius = Math.sqrt(x*x + y*y + z*z);
		double zxsq = x*x+z*z;
		double lon = (zxsq > 0) ? Math.acos(z / Math.sqrt(zxsq)) : 0;
		double lat = Math.asin(y / radius);
		
		if(x < 0){
				lon *= -1;
		}
		return new SpherePoint(lon,lat);
	}

	
	
}
