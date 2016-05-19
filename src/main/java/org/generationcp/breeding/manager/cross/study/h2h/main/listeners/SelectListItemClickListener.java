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
import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.SelectGermplasmListTreeComponent;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents.ClickEvent;

public class SelectListItemClickListener implements ItemClickEvent.ItemClickListener {

	private static final Logger LOG = LoggerFactory.getLogger(SelectListItemClickListener.class);
	private static final long serialVersionUID = -4521207966700882960L;

	private final Object source;

	public SelectListItemClickListener(Object source) {
		this.source = source;
	}

	@Override
	public void itemClick(ItemClickEvent event) {

		if (this.source instanceof SelectGermplasmListTreeComponent) {
			int germplasmListId = Integer.valueOf(event.getItemId().toString());
			if (event.getButton() == ClickEvent.BUTTON_LEFT) {
				try {
					((SelectGermplasmListTreeComponent) this.source).displayGermplasmListDetails(germplasmListId);
				} catch (InternationalizableException e) {
					SelectListItemClickListener.LOG.error(e.getMessage(), e);
					MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
				}
			}
		} else if (this.source instanceof GermplasmListTreeComponent) {
			String item = event.getItemId().toString();

			if (!item.equals(GermplasmListTreeComponent.ROOT_FOLDER_NAME)) {
				int germplasmListId = Integer.valueOf(event.getItemId().toString());
				if (event.getButton() == ClickEvent.BUTTON_LEFT) {
					try {
						((GermplasmListTreeComponent) this.source).listManagerTreeItemClickAction(germplasmListId);
					} catch (InternationalizableException e) {
						SelectListItemClickListener.LOG.error(e.getMessage(), e);
						MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
					}
				}
			} else {
				((GermplasmListTreeComponent) this.source).expandOrCollapseListTreeNode(item);
			}
		}
	}

}
