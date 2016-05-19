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

package org.generationcp.breeding.manager.germplasm.listeners;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.germplasm.GermplasmDetail;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;

public class GermplasmSelectedTabChangeListener implements TabSheet.SelectedTabChangeListener {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmSelectedTabChangeListener.class);
	private static final long serialVersionUID = -3192436611974353597L;
	private final Object source;

	public GermplasmSelectedTabChangeListener(Object source) {
		this.source = source;
	}

	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {

		if (this.source instanceof GermplasmDetail) {
			try {
				((GermplasmDetail) this.source).selectedTabChangeAction();
			} catch (InternationalizableException e) {
				GermplasmSelectedTabChangeListener.LOG.error(e.toString() + "\n" + e.getStackTrace(), e);
				e.setCaption(Message.ERROR_IN_DISPLAYING_RQUESTED_DETAIL);
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription()); // TESTED
			}
		}
	}

}
