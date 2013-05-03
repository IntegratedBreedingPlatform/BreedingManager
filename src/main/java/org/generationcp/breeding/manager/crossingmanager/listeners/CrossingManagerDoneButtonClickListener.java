package org.generationcp.breeding.manager.crossingmanager.listeners;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerDetailsComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CrossingManagerDoneButtonClickListener implements Button.ClickListener {

    private static final long serialVersionUID = 6666976205957048892L;
    private static final Logger LOG = LoggerFactory.getLogger(CrossingManagerDoneButtonClickListener.class);

    private Object source;

    public CrossingManagerDoneButtonClickListener(Object source){
        this.source = source;
    }
    
    @Override
    public void buttonClick(ClickEvent event) {
        Object eventButtonData = event.getButton().getData();
        
        if (eventButtonData.equals(CrossingManagerDetailsComponent.DONE_BUTTON_ID)
                && (source instanceof CrossingManagerDetailsComponent)) {
            ((CrossingManagerDetailsComponent) source).doneButtonClickAction();
            
        } else {
            LOG.error("CrossingManagerDoneButtonClickListener: Error with buttonClick action. Source not identified.");
        }
    }
}
