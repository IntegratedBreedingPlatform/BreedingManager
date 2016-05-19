
package org.generationcp.breeding.manager.application;

/**
 * Interface for standardizing Germplasm Study Browser UI layout classes
 *
 */
public interface GermplasmStudyBrowserLayout {

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
