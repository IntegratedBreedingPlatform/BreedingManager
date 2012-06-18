/***************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the 
 * GNU General Public License (http://bit.ly/8Ztv8M) and the 
 * provisions of Part F of the Generation Challenge Programme 
 * Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 **************************************************************/
package org.generationcp.browser.germplasm.listeners;

import java.util.ArrayList;

import org.generationcp.browser.germplasm.GermplasmDetail;
import org.generationcp.middleware.exceptions.QueryException;

import com.vaadin.ui.Layout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ExpandEvent;

public class GermplasmTreeExpandListener implements Tree.ExpandListener{

    private static final long serialVersionUID = 3215012575002448725L;
    private Layout source;
    private ArrayList<Object> parameters;
    
    public GermplasmTreeExpandListener(Layout source){
	this.source = source;
    }

    public GermplasmTreeExpandListener(Layout source, ArrayList<Object> parameters) {
	this.source = source;
	this.parameters = parameters;
    }

    @Override
    public void nodeExpand(ExpandEvent event) {
	
	if (source instanceof GermplasmDetail) {
	    ((GermplasmDetail) source).pedigreeTreeExpandAction((Integer) event.getItemId());

	}
	    
    }

}
