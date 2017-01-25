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

package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.breeding.manager.customfields.ListTreeComponent;
import org.generationcp.breeding.manager.listmanager.dialog.AddEntryDialog;
import org.generationcp.breeding.manager.listmanager.dialog.ListManagerCopyToListDialog;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;

public class GermplasmListButtonClickListener implements Button.ClickListener {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListButtonClickListener.class);
	private static final long serialVersionUID = 2185217915388685523L;

	private final Component source;

	public GermplasmListButtonClickListener(final Component source) {
		this.source = source;
	}

	@Override
	public void buttonClick(final ClickEvent event) {

		if (event.getButton().getData().equals(ListSelectorComponent.REFRESH_BUTTON_ID) // "Refresh"
				&& this.source instanceof ListTreeComponent) {
			((ListTreeComponent) this.source).refreshComponent();
		} else if (event.getButton().getData().equals(ListManagerCopyToListDialog.SAVE_BUTTON_ID)
				&& this.source instanceof ListManagerCopyToListDialog) {
			// "Save Germplasm List"
			((ListManagerCopyToListDialog) this.source).saveGermplasmListButtonClickAction();
		} else if (event.getButton().getData().equals(ListManagerCopyToListDialog.CANCEL_BUTTON_ID)
				&& this.source instanceof ListManagerCopyToListDialog) {
			// "Cancel Germplasm List"
			((ListManagerCopyToListDialog) this.source).cancelGermplasmListButtonClickAction();
		} else if (event.getButton().getData().equals(AddEntryDialog.DONE_BUTTON_ID) && this.source instanceof AddEntryDialog) {
			try {
				((AddEntryDialog) this.source).doneButtonClickAction(event);
			} catch (final InternationalizableException e) {
				GermplasmListButtonClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		} else {
			GermplasmListButtonClickListener.LOG
					.error("GermplasmListButtonClickListener: Error with buttonClick action. Source not identified.");
		}

	}

}
