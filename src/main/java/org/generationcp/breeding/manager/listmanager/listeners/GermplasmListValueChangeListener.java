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

import org.generationcp.breeding.manager.listmanager.dialog.AddEntryDialog;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Table;

public class GermplasmListValueChangeListener implements Table.ValueChangeListener{

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListValueChangeListener.class);
    private static final long serialVersionUID = -4521207966700882960L;

    private Object source;

    public GermplasmListValueChangeListener(Object source) {
        this.source = source;
    }

	@Override
	public void valueChange(ValueChangeEvent event) {

        if (source instanceof AddEntryDialog) {
            try {
                ((AddEntryDialog) source).resultTableValueChangeAction();
            } catch (InternationalizableException e) {  
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
            }
        }
    }


}
