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

import com.vaadin.ui.Layout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseEvent;

public class GermplasmListTreeCollapseListener implements Tree.CollapseListener {

	private static final long serialVersionUID = -5145904396164706110L;

	private final Layout source;

	public GermplasmListTreeCollapseListener(Layout source) {
		this.source = source;
	}

	@Override
	public void nodeCollapse(CollapseEvent event) {
		if (this.source instanceof ListSelectorComponent) {
			ListSelectorComponent listSelectorComponent = (ListSelectorComponent) this.source;
			listSelectorComponent.setSelectedListId(event.getItemId());
			listSelectorComponent.updateButtons(event.getItemId());
			listSelectorComponent.toggleFolderSectionForItemSelected();
		}
	}

}
