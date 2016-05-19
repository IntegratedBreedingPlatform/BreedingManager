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

import org.generationcp.breeding.manager.study.StudyTreeComponent;

import com.vaadin.ui.Layout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseEvent;

public class StudyTreeCollapseListener implements Tree.CollapseListener {

	private static final long serialVersionUID = -5091664285613837786L;

	private final Layout source;

	public StudyTreeCollapseListener(Layout source) {
		this.source = source;
	}

	@Override
	public void nodeCollapse(CollapseEvent event) {
		if (this.source instanceof StudyTreeComponent) {
			((StudyTreeComponent) this.source).getStudyTree().select(event.getItemId());
			((StudyTreeComponent) this.source).getStudyTree().setValue(event.getItemId());
			((StudyTreeComponent) this.source).updateButtons(event.getItemId());
		}
	}

}
