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
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ExpandEvent;

public class GermplasmTreeExpandListener implements Tree.ExpandListener{
    
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmTreeExpandListener.class);
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
            try{
                ((GermplasmPedigreeTreeComponent) source).pedigreeTreeExpandAction((Integer) event.getItemId());
            }catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());  // TESTED
            }
        }
    }

}
