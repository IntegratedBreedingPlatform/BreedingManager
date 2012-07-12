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

import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ExpandEvent;

public class GermplasmTreeExpandListener implements Tree.ExpandListener{

    private static final long serialVersionUID = 3215012575002448725L;
    private Tree source;
    @SuppressWarnings("unused")
    private ArrayList<Object> parameters;

    public GermplasmTreeExpandListener(Tree source) {
        this.source = source;

    }

    public GermplasmTreeExpandListener(Tree source, ArrayList<Object> parameters) {

        this(source);

        this.parameters = parameters;

    }

    @Override
    public void nodeExpand(ExpandEvent event) {

        if (source instanceof GermplasmPedigreeTreeComponent) {
            ((GermplasmPedigreeTreeComponent) source).pedigreeTreeExpandAction((Integer) event.getItemId());

        }

    }

}
