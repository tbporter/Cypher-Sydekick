package com.bls220.cyphersidekick.entities.ai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.bls220.cyphersidekick.MySidekick;

public class AIUtils {
	//TODO: Doesn't work yet
	public static boolean inLineOfSight(Vector2 a, Vector2 b){
		
		RayCastCallback cb = new RayCastCallback() {

			@Override
			public float reportRayFixture(Fixture arg0, Vector2 arg1,
					Vector2 arg2, float arg3) {
				
				return 0;
			}
			
		};
		
		MySidekick.getWorld().rayCast(cb, null, null);
		return true;
	}
}
