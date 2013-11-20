package com.karien.taco.mapstuff;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;

public enum MapAction {
	appear {
		@Override
		void doit(MapObject obj) {
			obj.getProperties().put(C.Visible, true);
		}
	}, disappear {
		@Override
		void doit(MapObject obj) {
			obj.getProperties().put(C.Visible, false);
		}
	}, toggle {
		@Override
		void doit(MapObject obj) {
			MapProperties p = obj.getProperties();
			p.put(C.Visible, !p.get(C.Visible, boolean.class));
		}
	};
	
	abstract void doit(MapObject obj);
	
	static MapAction getAction(String str) {
		if (str.equals(C.Appear)) {
			return appear;
		} if (str.equals(C.Disappear)) {
			return disappear;
		} if (str.equals(C.Toggle)) {
			return toggle;
		}
		throw new RuntimeException("Unknown action: " + str);
	}
}
