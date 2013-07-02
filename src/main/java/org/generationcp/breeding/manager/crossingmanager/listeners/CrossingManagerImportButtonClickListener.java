package org.generationcp.breeding.manager.crossingmanager.listeners;

import org.generationcp.breeding.manager.crossingmanager.AdditionalDetailsCrossNameComponent;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerAdditionalDetailsComponent;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerDetailsComponent;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerImportFileComponent;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerMakeCrossesComponent;
import org.generationcp.breeding.manager.crossingmanager.EmbeddedGermplasmListDetailComponent;
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
        
        if (eventButtonData.equals(CrossingManagerImportFileComponent.NEXT_BUTTON_ID) 
                && (source instanceof CrossingManagerImportFileComponent)) {
            ((CrossingManagerImportFileComponent) source).nextButtonClickAction();
            
        } else if (CrossingManagerMakeCrossesComponent.BACK_BUTTON_ID.equals(eventButtonData) 
                && (source instanceof CrossingManagerMakeCrossesComponent)) {
            ((CrossingManagerMakeCrossesComponent) source).backButtonClickAction();
            
        } else if (CrossingManagerMakeCrossesComponent.NEXT_BUTTON_ID.equals(eventButtonData) 
            && (source instanceof CrossingManagerMakeCrossesComponent)) {
            ((CrossingManagerMakeCrossesComponent) source).nextButtonClickAction();
            
        } else if (CrossingManagerAdditionalDetailsComponent.BACK_BUTTON_ID.equals(eventButtonData) 
                && (source instanceof CrossingManagerAdditionalDetailsComponent)) {
            ((CrossingManagerAdditionalDetailsComponent) source).backButtonClickAction();
            
        } else if (CrossingManagerAdditionalDetailsComponent.NEXT_BUTTON_ID.equals(eventButtonData) 
            && (source instanceof CrossingManagerAdditionalDetailsComponent)) {
            ((CrossingManagerAdditionalDetailsComponent) source).nextButtonClickAction();
            
        } else if (CrossingManagerMakeCrossesComponent.SELECT_FEMALE_PARENT_BUTTON_ID.equals(eventButtonData)
                && (source instanceof CrossingManagerMakeCrossesComponent)) {
            ((CrossingManagerMakeCrossesComponent) source).selectFemaleParentList();
            
        } else if (CrossingManagerMakeCrossesComponent.SELECT_MALE_PARENT_BUTTON_ID.equals(eventButtonData)
                && (source instanceof CrossingManagerMakeCrossesComponent)) {
            ((CrossingManagerMakeCrossesComponent) source).selectMaleParentList();
            
        } else  if (CrossingManagerMakeCrossesComponent.MAKE_CROSS_BUTTON_ID.equals(eventButtonData) 
                && (source instanceof CrossingManagerMakeCrossesComponent)) {
            ((CrossingManagerMakeCrossesComponent) source).makeCrossButtonAction();            
            
        } else  if (AdditionalDetailsCrossNameComponent.GENERATE_BUTTON_ID.equals(eventButtonData) 
                && (source instanceof AdditionalDetailsCrossNameComponent)) {
            ((AdditionalDetailsCrossNameComponent) source).generateNextNameButtonAction();            
            
        } else if (eventButtonData.equals(CrossingManagerDetailsComponent.DONE_BUTTON_ID)
                && (source instanceof CrossingManagerDetailsComponent)) {
            ((CrossingManagerDetailsComponent) source).doneButtonClickAction();
        
        } else if (eventButtonData.equals(CrossingManagerDetailsComponent.BACK_BUTTON_ID)
                && (source instanceof CrossingManagerDetailsComponent)) {
            ((CrossingManagerDetailsComponent) source).backButtonClickAction();
        
        } else if (eventButtonData.equals(EmbeddedGermplasmListDetailComponent.MAKE_CROSSES_BUTTON_ID)
                && (source instanceof EmbeddedGermplasmListDetailComponent)) {
            ((EmbeddedGermplasmListDetailComponent) source).makeNewCrossesButtonClickAction();
        
        } else if (eventButtonData.equals(EmbeddedGermplasmListDetailComponent.EXPORT_BUTTON_ID)
                && (source instanceof EmbeddedGermplasmListDetailComponent)) {
            ((EmbeddedGermplasmListDetailComponent) source).exportToFileButtonClickAction();
        
        } else {
            LOG.error("CrossingManagerButtonClickListener: Error with buttonClick action. Source not identified.");
        }
    }
}
