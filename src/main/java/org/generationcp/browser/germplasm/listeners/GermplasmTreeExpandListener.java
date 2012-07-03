/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.browser.germplasm.listeners;

import java.util.ArrayList;

import org.generationcp.browser.germplasm.GermplasmPedigreeTreeComponent;

import com.github.peholmst.i18n4vaadin.I18N;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ExpandEvent;

public class GermplasmTreeExpandListener implements Tree.ExpandListener{

    private static final long serialVersionUID = 3215012575002448725L;
    private Layout source;
    @SuppressWarnings("unused")
    private ArrayList<Object> parameters;

    private I18N i18n;

    public GermplasmTreeExpandListener(Layout source, I18N i18n) {
        this.source = source;
        this.i18n = i18n;
    }

    public GermplasmTreeExpandListener(Layout source, ArrayList<Object> parameters, I18N i18n) {

        this(source, i18n);

        this.parameters = parameters;

    }

    @Override
    public void nodeExpand(ExpandEvent event) {

        if (source instanceof GermplasmPedigreeTreeComponent) {
            ((GermplasmPedigreeTreeComponent) source).pedigreeTreeExpandAction((Integer) event.getItemId());

        }

    }

}
