// BV Ue2 SS2021 Vorgabe
//
// Copyright (C) 2021 by Klaus Jung
// All rights reserved.
// Date: 2021-03-24

package bv_ss21;


public class GeometricTransform {

	public enum InterpolationType { 
		NEAREST("Nearest Neighbour"), 
		BILINEAR("Bilinear");
		
		private final String name;       
	    private InterpolationType(String s) { name = s; }
	    public String toString() { return this.name; }
	};
	
	public void perspective(RasterImage src, RasterImage dst, double angle, double perspectiveDistortion, InterpolationType interpolation) {
		switch(interpolation) {
		case NEAREST:
			perspectiveNearestNeighbour(src, dst, angle, perspectiveDistortion);
			break;
		case BILINEAR:
			perspectiveBilinear(src, dst, angle, perspectiveDistortion);
			break;
		default:
			break;	
		}
		
	}

	/**
	 * @param src source image
	 * @param dst destination Image
	 * @param angle rotation angle in degrees
	 * @param perspectiveDistortion amount of the perspective distortion 
	 */
	public void perspectiveNearestNeighbour(RasterImage src, RasterImage dst, double angle, double perspectiveDistortion) {
		// TODO: implement the geometric transformation using nearest neighbour image rendering
		
		// NOTE: angle contains the angle in degrees, whereas Math trigonometric functions need the angle in radians
			/*
			 * Bogenlänge (radians) des geg. Winkels prop. dem radius 
			 * Kreis mit 5cm radius -> winkel von 1 rad 5cm langer bogen
			 * Vollkreis (360°) = 2pi*r
			 */


		//Schleife über Ziel-Rasterbild
		for(int y_dest = 0; y_dest < dst.height; y_dest++) {
			for(int x_dest = 0; x_dest < dst.width; x_dest++) {
				int pos_dest = y_dest * dst.width + x_dest;
				
				//neuen Mittelpunkt für neues Bild damit Rotation mittig vom Bild um y-Achse und nicht am linken Rand
				int x_dest_n = x_dest - (dst.width/2);
				int y_dest_n = y_dest - (dst.height/2);
				
				//angle -> radians:
				double radians = Math.toRadians(angle);
				
				double x_source_n = x_dest_n / (Math.cos(radians)- perspectiveDistortion * Math.sin(radians) * x_dest_n);
				double y_source_n = y_dest_n * (perspectiveDistortion * Math.sin(radians)* x_source_n +1);

				//Rücktransformation/-translation:
				double x_source = (int) Math.round( x_source_n + (src.width/2));
				double y_source = (int) Math.round(y_source_n + (src.height/2));
				
				//Nearest Neighbor:
				int pos_source = (int)y_source * src.width + (int)x_source;
				
				if(x_source >= 0 && x_source < src.width && y_source >= 0 && y_source < src.height) {
					dst.argb[pos_dest] = src.argb[pos_source];
				} else {
					dst.argb[pos_dest] = 0xFFFFFFFF; //alles um Ränder herum soll weiß sein 
				}
				
			}
		}
	}


	/**
	 * @param src source image
	 * @param dst destination Image
	 * @param angle rotation angle in degrees
	 * @param perspectiveDistortion amount of the perspective distortion 
	 */
	public void perspectiveBilinear(RasterImage src, RasterImage dst, double angle, double perspectiveDistortion) {
		// TODO: implement the geometric transformation using bilinear interpolation
		
		// NOTE: angle contains the angle in degrees, whereas Math trigonometric functions need the angle in radians
		
		//Schleife über Ziel-Rasterbild
		for(int y_dest = 0; y_dest < dst.height; y_dest++) {
			for(int x_dest = 0; x_dest < dst.width; x_dest++) {
				int pos_dest = y_dest * dst.width + x_dest;
				
				//neuen Mittelpunkt für neues Bild damit Rotation mittig vom Bild um y-Achse und nicht am linken Rand
				int x_dest_n = x_dest - (dst.width/2);
				int y_dest_n = y_dest - (dst.height/2);
				
				//angle -> radians:
				double radians = Math.toRadians(angle);
				
				double x_source_n = x_dest_n / (Math.cos(radians)- perspectiveDistortion * Math.sin(radians) * x_dest_n);
				double y_source_n = y_dest_n * (perspectiveDistortion * Math.sin(radians)* x_source_n +1);

				//Rücktransformation/-translation:
				double x_source = x_source_n + (src.width/2);
				double y_source = y_source_n + (src.height/2);
				
				//abrunden (auch zum berechnen von Abständen h und v):
				int x = (int) Math.floor(x_source);
				int y = (int) Math.floor(y_source);
				
				int pos_source = y * src.width + x;
				
				if(x >= 0 && x < src.width && y >= 0 && y < src.height) {

					//Abstände Pixelposition zu umgebenden Pixeln:
					double h = x_source - x;
					double v = y_source - y;
					
					//RGB-Werte lesen 1. Nachbar:A
					int rgb1 = src.argb[pos_source];
					if(y == 0) 
						rgb1 = 0xFFFFFFFF;
					
					double r1 = (rgb1 >> 16) & 0xff;
					double g1 = (rgb1 >> 8) & 0xff;
					double b1 = (rgb1) & 0xff;
					
					//2.Nachbar:B
					int rgb2; // = src.argb[pos_source+1];
					if(pos_source+1 < src.argb.length) {
						rgb2 = src.argb[pos_source +1];
					
					if(y == 0) 
						rgb2 = 0xFFFFFFFF;
					}
					 else 
						rgb2 = 0xFFFFFFFF;
					
					double r2 = (rgb2 >> 16) & 0xff;
					double g2 = (rgb2 >> 8) & 0xff;
					double b2 = (rgb2) & 0xff;
					
					//3.Nachbar:C
					int rgb3;// = src.argb[pos_source + src.width];
					if(pos_source + src.width < src.argb.length) 
						rgb3 = src.argb[pos_source + src.width];
					 else 
						rgb3 = 0xFFFFFFFF;
					
					double r3 = (rgb3 >> 16) & 0xff;
					double g3 = (rgb3 >> 8) & 0xff;
					double b3 = (rgb3) & 0xff;
					
					//4.Nachbar:D
					int rgb4;// = src.argb[pos_source + src.width +1];
					if(pos_source + src.width +1 < src.argb.length) 
						rgb4 = src.argb[pos_source + src.width +1];
					 else 
						rgb4 = 0xFFFFFFFF;
					
					double r4 = (rgb4 >> 16) & 0xff;
					double g4 = (rgb4 >> 8) & 0xff;
					double b4 = (rgb4) & 0xff;
					
					//gemischte r,g,b Werte (neues Pixel)(Formel aus Skript GdM):
					int r = (int) (r1*(1-h)*(1-v) + r2*h*(1-v) + r3*(1-h)*v + r4*h*v);
					int g = (int) (g1*(1-h)*(1-v) + g2*h*(1-v) + g3*(1-h)*v + g4*h*v);
					int b = (int) (b1*(1-h)*(1-v) + b2*h*(1-v) + b3*(1-h)*v + b4*h*v);	
					
					
					dst.argb[pos_dest] = (0xff << 24) | (r << 16) | (g << 8) | b;
				} else {
					dst.argb[pos_dest] = 0xFFFFFFFF;
				}
			}
		}
	}
}
