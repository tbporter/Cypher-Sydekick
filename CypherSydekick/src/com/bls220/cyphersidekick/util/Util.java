package com.bls220.cyphersidekick.util;

import com.bls220.cyphersidekick.mapstuff.Coord;

public class Util {
	public static Coord parseCommaCoord(String coord) {
		int comma = coord.indexOf(',');
		if (comma == -1) {
			return null;
		}
		try {
			return new Coord(
					Integer.parseInt(coord.substring(0, comma).trim()),
					Integer.parseInt(coord.substring(comma + 1).trim()));
		} catch (NumberFormatException x) {
			return null;
		}
	}
}
