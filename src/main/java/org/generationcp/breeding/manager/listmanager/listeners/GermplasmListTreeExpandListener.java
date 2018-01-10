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
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Layout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ExpandEvent;

public class GermplasmListTreeExpandListener implements Tree.ExpandListener {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListTreeExpandListener.class);
	private static final long serialVersionUID = -5145904396164706110L;

	private final Layout source;

	public GermplasmListTreeExpandListener(Layout source) {
		this.source = source;
	}

	@Override
	public void nodeExpand(ExpandEvent event) {
		if (this.source instanceof ListSelectorComponent) {
			if (!event.getItemId().toString().equals(ListSelectorComponent.PROGRAM_LISTS)) {
				try {
					((ListSelectorComponent) this.source).addGermplasmListNode(Integer.valueOf(event.getItemId().toString()));
				} catch (InternationalizableException e) {
					GermplasmListTreeExpandListener.LOG.error(e.getMessage(), e);
					MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
				}
			}
			ListSelectorComponent listSelectorComponent = (ListSelectorComponent) this.source;
			listSelectorComponent.setSelectedListId(event.getItemId());
			listSelectorComponent.updateButtons(event.getItemId());
			listSelectorComponent.toggleFolderSectionForItemSelected();
		}
	}
}
