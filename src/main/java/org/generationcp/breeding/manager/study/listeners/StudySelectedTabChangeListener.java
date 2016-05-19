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

package org.generationcp.breeding.manager.study.listeners;

import org.generationcp.breeding.manager.study.StudyAccordionMenu;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;

public class StudySelectedTabChangeListener implements TabSheet.SelectedTabChangeListener {

	private static final Logger LOG = LoggerFactory.getLogger(StudySelectedTabChangeListener.class);
	private static final long serialVersionUID = -1276034489275080024L;

	private final Object source;

	public StudySelectedTabChangeListener(Object source) {
		this.source = source;
	}

	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {

		if (this.source instanceof StudyAccordionMenu) {
			try {
				((StudyAccordionMenu) this.source).selectedTabChangeAction();
			} catch (InternationalizableException e) {
				StudySelectedTabChangeListener.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription()); // TESTED
			}
		}
	}

}
