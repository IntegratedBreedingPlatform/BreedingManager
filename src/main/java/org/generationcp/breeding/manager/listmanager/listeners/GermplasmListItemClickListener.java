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

package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.generationcp.breeding.manager.listmanager.dialog.AddEntryDialog;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;

public class GermplasmListItemClickListener implements ItemClickEvent.ItemClickListener{

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListItemClickListener.class);
    private static final long serialVersionUID = -4521207966700882960L;

    private Object source;

    public GermplasmListItemClickListener(Object source) {
        this.source = source;
    }

    @Override
    public void itemClick(ItemClickEvent event) {

        if (source instanceof ListManagerTreeComponent) {
        	String item = event.getItemId().toString();
        	//System.out.println("item: " + item);
        	
            if (event.getButton() == ClickEvent.BUTTON_LEFT) {
	        	if(!item.equals("CENTRAL") && !item.equals("LOCAL")){
	        		int germplasmListId = Integer.valueOf(event.getItemId().toString());
	            
	                    try {
	                        ((ListManagerTreeComponent) source).listManagerTreeItemClickAction(germplasmListId);
	                    } catch (InternationalizableException e) {
	                        LOG.error(e.toString() + "\n" + e.getStackTrace());
	                        e.printStackTrace();
	                        MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
	                    }
	        	} else{
	        		((ListManagerTreeComponent) source).expandOrCollapseListTreeNode(item);
	        	}
	        	((ListManagerTreeComponent) source).setSelectedListId(event.getItemId());
            	((ListManagerTreeComponent) source).updateButtons(event.getItemId());
            }
        }
        
        if (source instanceof AddEntryDialog) {
            if (event.getButton() == ClickEvent.BUTTON_LEFT && event.isDoubleClick()) {
                try {
                    ((AddEntryDialog) source).resultTableItemDoubleClickAction((Table) event.getSource(), event.getItemId(), event.getItem());
                } catch (InternationalizableException e) {  
                    LOG.error(e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                    MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
                }
            } else if (event.getButton() == ClickEvent.BUTTON_LEFT) {
                try {
                    ((AddEntryDialog) source).resultTableItemClickAction((Table) event.getSource(), event.getItemId(), event.getItem());
                } catch (InternationalizableException e) {  
                    LOG.error(e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                    MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
                }
            }
        }
    }
    
    

}
