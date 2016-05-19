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

import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.SelectGermplasmEntryDialog;
import org.generationcp.breeding.manager.germplasm.GermplasmDerivativeNeighborhoodComponent;
import org.generationcp.breeding.manager.germplasm.GermplasmMaintenanceNeighborhoodComponent;
import org.generationcp.breeding.manager.germplasm.GermplasmPedigreeTreeComponent;
import org.generationcp.breeding.manager.germplasm.dialogs.SelectAGermplasmDialog;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

public class GermplasmItemClickListener implements ItemClickEvent.ItemClickListener {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmItemClickListener.class);
	private static final long serialVersionUID = -1095503156046245812L;

	private final Object sourceClass;
	private Component sourceComponent;

	public GermplasmItemClickListener(Object sourceClass) {
		this.sourceClass = sourceClass;
	}

	public GermplasmItemClickListener(Object sourceClass, Component sourceComponent) {
		this(sourceClass);
		this.sourceComponent = sourceComponent;
	}

	@Override
	public void itemClick(ItemClickEvent event) {

		if (this.sourceClass instanceof GermplasmPedigreeTreeComponent) {
			if (event.getButton() == ClickEvent.BUTTON_LEFT) {
				try {
					((GermplasmPedigreeTreeComponent) this.sourceClass).displayNewGermplasmDetailTab((Integer) event.getItemId());
				} catch (InternationalizableException e) {
					GermplasmItemClickListener.LOG.error("Error in GermplasmItemClickListener: " + e.toString() + "\n" + e.getStackTrace(),
							e);
					// TESTED
					MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
				}
			}

		} else if (this.sourceClass instanceof GermplasmDerivativeNeighborhoodComponent) {
			if (event.getButton() == ClickEvent.BUTTON_LEFT) {
				try {
					((GermplasmDerivativeNeighborhoodComponent) this.sourceClass).displayNewGermplasmDetailTab((Integer) event.getItemId());
				} catch (InternationalizableException e) {
					GermplasmItemClickListener.LOG.error("Error in GermplasmItemClickListener: " + e.toString() + "\n" + e.getStackTrace(),
							e);
					// TESTED
					MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
				}
			}

		} else if (this.sourceClass instanceof GermplasmMaintenanceNeighborhoodComponent) {
			if (event.getButton() == ClickEvent.BUTTON_LEFT) {
				try {
					((GermplasmMaintenanceNeighborhoodComponent) this.sourceClass)
							.displayNewGermplasmDetailTab((Integer) event.getItemId());
				} catch (InternationalizableException e) {
					GermplasmItemClickListener.LOG.error("Error in GermplasmItemClickListener: " + e.toString() + "\n" + e.getStackTrace(),
							e);
					// TESTED
					MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
				}
			}

		} else if (this.sourceClass instanceof SelectAGermplasmDialog) {
			if (event.getButton() == ClickEvent.BUTTON_LEFT && event.isDoubleClick()) {
				try {
					((SelectAGermplasmDialog) this.sourceClass).resultTableItemDoubleClickAction((Table) event.getSource(),
							event.getItemId(), event.getItem());
				} catch (InternationalizableException e) {
					GermplasmItemClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace(), e);
					MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
				}
			} else if (event.getButton() == ClickEvent.BUTTON_LEFT) {
				try {
					((SelectAGermplasmDialog) this.sourceClass).resultTableItemClickAction((Table) event.getSource(), event.getItemId(),
							event.getItem());
				} catch (InternationalizableException e) {
					GermplasmItemClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace(), e);
					MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
				}
			}
		} else if (this.sourceClass instanceof SelectGermplasmEntryDialog) {
			if (event.getButton() == ClickEvent.BUTTON_LEFT) {
				try {
					((SelectGermplasmEntryDialog) this.sourceClass).resultTableItemClickAction((Table) event.getSource(),
							event.getItemId(), event.getItem());
				} catch (InternationalizableException e) {
					GermplasmItemClickListener.LOG.error(e.toString() + "\n" + e.getStackTrace(), e);
					MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
				}
			}
		}
	}

}
