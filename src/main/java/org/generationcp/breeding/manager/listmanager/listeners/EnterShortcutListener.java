/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
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

import org.generationcp.breeding.manager.listmanager.ListManagerSearchListsComponent;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;


/**
 * @author Efficio.Daniel
 *
 */
public class EnterShortcutListener extends ShortcutListener{

    private ListManagerSearchListsComponent listManagerSearchListsComponent;
    /**
     * @param shorthandCaption
     */
    public EnterShortcutListener(String shorthandCaption, ListManagerSearchListsComponent listManagerSearchListsComponent) {
        super(shorthandCaption,  ShortcutAction.KeyCode.ENTER, null);
        this.listManagerSearchListsComponent = listManagerSearchListsComponent;
    }
    
    @Override
    public void handleAction(Object sender, Object target) {
        // your code here
        this.listManagerSearchListsComponent.searchButtonClickAction();
    }

}
