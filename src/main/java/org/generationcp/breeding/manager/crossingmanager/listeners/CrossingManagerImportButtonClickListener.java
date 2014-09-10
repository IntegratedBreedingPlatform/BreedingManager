package org.generationcp.breeding.manager.crossingmanager.listeners;

import org.generationcp.breeding.manager.crossingmanager.AdditionalDetailsCrossNameComponent;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerMakeCrossesComponent;
import org.generationcp.breeding.manager.crossingmanager.CrossingMethodComponent;
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
        Object eventButtonData = event.getButton().getData();
        
        if (CrossingManagerMakeCrossesComponent.BACK_BUTTON_ID.equals(eventButtonData) 
                && (source instanceof CrossingManagerMakeCrossesComponent)) {
            ((CrossingManagerMakeCrossesComponent) source).backButtonClickAction();
            
        } else if (CrossingManagerMakeCrossesComponent.NEXT_BUTTON_ID.equals(eventButtonData) 
            && (source instanceof CrossingManagerMakeCrossesComponent)) {
            ((CrossingManagerMakeCrossesComponent) source).nextButtonClickAction();
            
        } else  if (CrossingMethodComponent.MAKE_CROSS_BUTTON_ID.equals(eventButtonData) 
                && (source instanceof CrossingMethodComponent)) {
            ((CrossingMethodComponent) source).makeCrossButtonAction();            
            
        } else  if (AdditionalDetailsCrossNameComponent.GENERATE_BUTTON_ID.equals(eventButtonData) 
                && (source instanceof AdditionalDetailsCrossNameComponent)) {
            ((AdditionalDetailsCrossNameComponent) source).generateNextNameButtonAction();            
            
        } else {
            LOG.error("CrossingManagerButtonClickListener: Error with buttonClick action. Source not identified.");
        }
    }
}
