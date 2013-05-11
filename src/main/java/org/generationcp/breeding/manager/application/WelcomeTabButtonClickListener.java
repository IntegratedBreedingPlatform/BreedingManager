package org.generationcp.breeding.manager.application;

import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;


public class WelcomeTabButtonClickListener implements Button.ClickListener{
    
    private static final long serialVersionUID = -5241360683750569732L;
    private static final Logger LOG = LoggerFactory.getLogger(WelcomeTabButtonClickListener.class);

    private Object source;
    
    public WelcomeTabButtonClickListener(Object source){
        this.source = source;
    }
    
    @Override
    public void buttonClick(ClickEvent event) {
        if (source instanceof WelcomeTab && event.getButton().getData().equals(WelcomeTab.IMPORT_GERMPLASM_LIST_BUTTON_ID)) {
            try {
                ((WelcomeTab) source).importGermplasmButtonClickAction();
            }catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        } else if (source instanceof WelcomeTab && event.getButton().getData().equals(WelcomeTab.IMPORT_CROSSING_MANAGER_DATA_BUTTON_ID)) {
            try {
                ((WelcomeTab) source).importCrossingManagerDataClickAction();
            }catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }

        } else if (source instanceof WelcomeTab && event.getButton().getData().equals(WelcomeTab.NURSERY_TEMPLATE_BUTTON_ID)) {
            try {
                ((WelcomeTab) source).nurseryTemplateClickAction();
            }catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        
        } else if (source instanceof WelcomeTab && event.getButton().getData().equals(WelcomeTab.SELECT_GERMPLASM_LIST_BUTTON_ID)) {
            try {
                ((WelcomeTab) source).selectGermplasmButtonClickAction();
            }catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }

        }
    }
}
