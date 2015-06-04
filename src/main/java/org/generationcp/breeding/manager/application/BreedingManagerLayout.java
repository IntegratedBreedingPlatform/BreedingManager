
package org.generationcp.breeding.manager.application;

/**
 * Interface for standardizing Breeding Manager UI layout classes
 *
 * @author Darla Ani
 *
 */
public interface BreedingManagerLayout {

	/**
	 * Instantiate UI elements for the layout
	 */
	void instantiateComponents();

	/**
	 * Initialize values and state of the UI elements
	 */
	void initializeValues();

	/**
	 * Add action listeners to UI elements
	 */
	void addListeners();

	/**
	 * Set styling and position UI elements in the layout
	 */
	void layoutComponents();
}
