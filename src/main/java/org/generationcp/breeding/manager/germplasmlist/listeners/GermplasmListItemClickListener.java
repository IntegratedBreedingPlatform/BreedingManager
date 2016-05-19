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

package org.generationcp.breeding.manager.germplasmlist.listeners;

import org.generationcp.breeding.manager.germplasm.GermplasmListComponent;
import org.generationcp.breeding.manager.germplasm.containers.ListsForGermplasmQuery;
import org.generationcp.breeding.manager.germplasmlist.ListManagerTreeComponent;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents.ClickEvent;

public class GermplasmListItemClickListener implements ItemClickEvent.ItemClickListener {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListItemClickListener.class);
	private static final long serialVersionUID = -4521207966700882960L;

	private final Object source;

	public GermplasmListItemClickListener(Object source) {
		this.source = source;
	}

	@Override
	public void itemClick(ItemClickEvent event) {

		if (this.source instanceof GermplasmListComponent) {
			int listId = Integer.valueOf(event.getItem().getItemProperty(ListsForGermplasmQuery.GERMPLASMLIST_ID).getValue().toString());
			if (event.getButton() == ClickEvent.BUTTON_LEFT) {
				try {
					((GermplasmListComponent) this.source).listItemClickAction(event, listId);
				} catch (InternationalizableException e) {
					GermplasmListItemClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
					e.printStackTrace();
					MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
				}
			}
		} else if (this.source instanceof ListManagerTreeComponent) {
			String item = event.getItemId().toString();

			if (event.getButton() == ClickEvent.BUTTON_LEFT) {
				if (!item.equals("CENTRAL") && !item.equals("LOCAL")) {
					int germplasmListId = Integer.valueOf(event.getItemId().toString());

					try {
						((ListManagerTreeComponent) this.source).listManagerTreeItemClickAction(germplasmListId);
					} catch (InternationalizableException e) {
						GermplasmListItemClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
						e.printStackTrace();
						MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
					}
				} else {
					((ListManagerTreeComponent) this.source).expandOrCollapseListTreeNode(item);
				}
				((ListManagerTreeComponent) this.source).setSelectedListId(event.getItemId());
				((ListManagerTreeComponent) this.source).updateButtons(event.getItemId());
			}
		}
	}

}
