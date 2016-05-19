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

import com.vaadin.ui.Layout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ExpandEvent;

public class SelectListTreeExpandListener implements Tree.ExpandListener {

	private static final Logger LOG = LoggerFactory.getLogger(SelectListTreeExpandListener.class);
	private static final long serialVersionUID = -5145904396164706110L;

	private final Layout source;

	public SelectListTreeExpandListener(Layout source) {
		this.source = source;
	}

	@Override
	public void nodeExpand(ExpandEvent event) {
		try {
			if (this.source instanceof SelectGermplasmListTreeComponent) {
				((SelectGermplasmListTreeComponent) this.source).addGermplasmListNode(Integer.valueOf(event.getItemId().toString()));

			} else if (this.source instanceof GermplasmListTreeComponent
					&& !event.getItemId().toString().equals(GermplasmListTreeComponent.ROOT_FOLDER_NAME)) {
				((GermplasmListTreeComponent) this.source).addGermplasmListNode(Integer.valueOf(event.getItemId().toString()));
			}
		} catch (InternationalizableException e) {
			SelectListTreeExpandListener.LOG.error(e.getMessage(), e);
			MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
		}
	}

}
