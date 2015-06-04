/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;

/**
 * A class should implement this interface if it will be the recipient of and store information on Crosses Made from other tabs in Crossing
 * Manager
 *
 * @author Darla Ani
 *
 */
public interface CrossesMadeContainer {

	public CrossesMade getCrossesMade();

	public void setCrossesMade(CrossesMade crossesMade);

}
