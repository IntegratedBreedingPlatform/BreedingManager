package org.generationcp.breeding.manager.nurserytemplate.listeners;

import org.generationcp.breeding.manager.crosses.NurseryTemplateConditionsComponent;
import org.generationcp.breeding.manager.crosses.NurseryTemplateImportFileComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;


public class NurseryTemplateButtonClickListener implements Button.ClickListener {

    private static final long serialVersionUID = 6666976205957048892L;
    private static final Logger LOG = LoggerFactory.getLogger(NurseryTemplateButtonClickListener.class);

    private Object source;
    
    public NurseryTemplateButtonClickListener(Object source){
        this.source = source;
    }
    
    @Override
    public void buttonClick(ClickEvent event) {
        Object eventButtonData = event.getButton().getData();
        
        if (eventButtonData.equals(NurseryTemplateImportFileComponent.NEXT_BUTTON_ID) 
                && (source instanceof NurseryTemplateImportFileComponent)) {
            ((NurseryTemplateImportFileComponent) source).nextButtonClickAction();
        
        }else if(eventButtonData.equals(NurseryTemplateConditionsComponent.BACK_BUTTON_ID) 
            && (source instanceof NurseryTemplateConditionsComponent)) {
            ((NurseryTemplateConditionsComponent) source).backButtonClickAction();
        }else if(eventButtonData.equals(NurseryTemplateConditionsComponent.DONE_BUTTON_ID) 
            && (source instanceof NurseryTemplateConditionsComponent)) {
            ((NurseryTemplateConditionsComponent) source).doneButtonClickAction();      
        }else {
            LOG.error("CrossingManagerButtonClickListener: Error with buttonClick action. Source not identified.");
        }
    }
}
