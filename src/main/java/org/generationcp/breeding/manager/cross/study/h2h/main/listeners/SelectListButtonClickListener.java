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

package org.generationcp.breeding.manager.cross.study.h2h.main.listeners;

import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.GermplasmListTreeComponent;
import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.SelectGermplasmListDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

public class SelectListButtonClickListener implements Button.ClickListener {

	private static final Logger LOG = LoggerFactory.getLogger(SelectListButtonClickListener.class);
	private static final long serialVersionUID = 2185217915388685523L;

	private final Object source;

	public SelectListButtonClickListener(Object source) {
		this.source = source;
	}

	@Override
	public void buttonClick(ClickEvent event) {

		if (event.getButton().getData().equals(GermplasmListTreeComponent.REFRESH_BUTTON_ID) // "Refresh"
				&& this.source instanceof GermplasmListTreeComponent) {
			((GermplasmListTreeComponent) this.source).createTree();

		} else if (event.getButton().getData().equals(SelectGermplasmListDialog.ADD_BUTTON_ID)
				&& this.source instanceof SelectGermplasmListDialog) {
			((SelectGermplasmListDialog) this.source).populateParentList();

		} else {
			SelectListButtonClickListener.LOG.error("SelectListButtonClickListener: Error with buttonClick action. Source not identified.");
		}
	}

}
