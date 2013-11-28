/**
 * 
 */
package com.bls220.cyphersidekick.entities;

/**
 * @author bsmith
 * 
 */
public interface Living {
	/**
	 * @return the current number of HPs
	 */
	public float getHealth();

	/**
	 * Sets the current number of HPs
	 * 
	 * @param newHealth
	 *            - the new amount of HPs
	 */
	public void setHealth(float newHealth);

	/**
	 * @return The maximum allowed number of HPs
	 */
	public float getMaxHealth();
}
