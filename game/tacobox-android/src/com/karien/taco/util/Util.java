package com.karien.taco.util;

import com.karien.taco.mapstuff.Coord;

public class Util {
	public static Coord parseCommaCoord(String coord) {
		int comma = coord.indexOf(',');
		if (comma == -1) {
			return null;
		}
		try {
			return new Coord(
					Integer.parseInt(coord.substring(0, comma).trim()), 
					Integer.parseInt(coord.substring(comma+1).trim())
			);
		} catch (NumberFormatException x) {
			return null;
		}
	}
}
