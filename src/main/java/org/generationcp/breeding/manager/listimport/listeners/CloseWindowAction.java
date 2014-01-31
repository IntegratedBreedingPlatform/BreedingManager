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

package org.generationcp.breeding.manager.listimport.listeners;

import org.generationcp.breeding.manager.listimport.SelectGermplasmWindow;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window;

public class CloseWindowAction implements ClickListener{

    private static final long serialVersionUID = 1L;

    private Object source;
    
    public CloseWindowAction(Object source){
    	this.source = source;
    }
    
    @Override
    public void buttonClick(ClickEvent event) {
    	if(source instanceof SelectGermplasmWindow){
	    	if(event.getButton().getData().equals(SelectGermplasmWindow.DONE_BUTTON_ID)){
	    		Window window = event.getButton().getWindow();
	    		window.getParent().removeWindow(window);
	    	} else if(event.getButton().getData().equals(SelectGermplasmWindow.CANCEL_BUTTON_ID)) {
	    	    ((SelectGermplasmWindow) source).cancelButtonClickAction();
	    	} else {
	    		//	"Unhandled buttonClick event on CloseWindowAction"
	    	}
    	}
    }
}
