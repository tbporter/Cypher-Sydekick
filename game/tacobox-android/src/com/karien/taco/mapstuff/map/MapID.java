package com.karien.taco.mapstuff.map;

public enum MapID {
	Test, Normal, Hard, End;

	public String getPath(boolean player) {
		String suffix;
		switch (this) {
		case Test:
			suffix = "Test";
			break;
		case Normal:
			suffix = "Complex";
			break;
		case Hard:
			suffix = "Tough";
			break;
		case End:
			suffix = "End";
			break;
		default:
			throw new RuntimeException("Unknown map id: " + this);
		}
		
		return "maps/" + (player ? "light" : "dark") + suffix + ".tmx";
	}
}
