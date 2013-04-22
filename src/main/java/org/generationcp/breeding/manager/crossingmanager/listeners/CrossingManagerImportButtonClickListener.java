package org.generationcp.breeding.manager.crossingmanager.listeners;

import org.generationcp.breeding.manager.crossingmanager.CrossingManagerImportFileComponent;
import org.generationcp.breeding.manager.listimport.GermplasmImportFileComponent;
import org.generationcp.breeding.manager.listimport.SaveGermplasmListComponent;
import org.generationcp.breeding.manager.listimport.SpecifyGermplasmDetailsComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;


public class CrossingManagerImportButtonClickListener implements Button.ClickListener {

    private static final long serialVersionUID = 6666976205957048892L;
    private static final Logger LOG = LoggerFactory.getLogger(CrossingManagerImportButtonClickListener.class);

    private Object source;
    
    public CrossingManagerImportButtonClickListener(Object source){
        this.source = source;
    }
    
    @Override
    public void buttonClick(ClickEvent event) {
        if (event.getButton().getData().equals(CrossingManagerImportFileComponent.NEXT_BUTTON_ID) 
                && (source instanceof CrossingManagerImportFileComponent)) {
            ((CrossingManagerImportFileComponent) source).nextButtonClickAction();
        } else {
            LOG.error("GermplasmImportButtonClickListener: Error with buttonClick action. Source not identified.");
        }
    }
}
