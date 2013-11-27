package com.bls220.cyphersidekick.mapstuff;

import com.badlogic.gdx.maps.MapObject;

public enum MapAction {
	appear {
		@Override
		void doit(MapObject obj) {
			obj.setVisible(true);
		}
	},
	disappear {
		@Override
		void doit(MapObject obj) {
			obj.setVisible(false);
		}
	},
	toggle {
		@Override
		void doit(MapObject obj) {
			obj.setVisible(!obj.isVisible());
		}
	};

	abstract void doit(MapObject obj);

	static MapAction getAction(String str) {
		if (str.equals(C.Appear)) {
			return appear;
		}
		if (str.equals(C.Disappear)) {
			return disappear;
		}
		if (str.equals(C.Toggle)) {
			return toggle;
		}
		throw new RuntimeException("Unknown action: " + str);
	}
}
