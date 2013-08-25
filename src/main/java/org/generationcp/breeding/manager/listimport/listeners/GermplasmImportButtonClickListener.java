package org.generationcp.breeding.manager.listimport.listeners;

import org.generationcp.breeding.manager.listimport.EmbeddedGermplasmListDetailComponent;
import org.generationcp.breeding.manager.listimport.GermplasmImportFileComponent;
import org.generationcp.breeding.manager.listimport.SaveGermplasmListComponent;
import org.generationcp.breeding.manager.listimport.SelectGermplasmWindow;
import org.generationcp.breeding.manager.listimport.SpecifyGermplasmDetailsComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;


public class GermplasmImportButtonClickListener implements Button.ClickListener {

    private static final long serialVersionUID = 6666976205957048892L;
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmImportButtonClickListener.class);

    private Object source;
    
    public GermplasmImportButtonClickListener(Object source){
        this.source = source;
    }
    
    @Override
    public void buttonClick(ClickEvent event) {
        if (event.getButton().getData().equals(GermplasmImportFileComponent.NEXT_BUTTON_ID) 
                && (source instanceof GermplasmImportFileComponent)) {
            ((GermplasmImportFileComponent) source).nextButtonClickAction();
        } else if (event.getButton().getData().equals(SpecifyGermplasmDetailsComponent.NEXT_BUTTON_ID) 
                && (source instanceof SpecifyGermplasmDetailsComponent)) {
            ((SpecifyGermplasmDetailsComponent) source).nextButtonClickAction();
        } else if (event.getButton().getData().equals(SpecifyGermplasmDetailsComponent.BACK_BUTTON_ID) 
                && (source instanceof SpecifyGermplasmDetailsComponent)) {
            ((SpecifyGermplasmDetailsComponent) source).backButtonClickAction();
        } else if (event.getButton().getData().equals(SaveGermplasmListComponent.BACK_BUTTON_ID) 
                && (source instanceof SaveGermplasmListComponent)) {
            ((SaveGermplasmListComponent) source).backButtonClickAction();
        }else if (event.getButton().getData().equals(SaveGermplasmListComponent.DONE_BUTTON_ID)
                        && (source instanceof SaveGermplasmListComponent)) {
                    ((SaveGermplasmListComponent) source).nextButtonClickAction();
        }else if (event.getButton().getData().equals(EmbeddedGermplasmListDetailComponent.NEW_IMPORT_BUTTON_ID)
            && (source instanceof EmbeddedGermplasmListDetailComponent)) {
            ((EmbeddedGermplasmListDetailComponent) source).makeNewImportButtonClickAction();
        }else if (event.getButton().getData().equals(SelectGermplasmWindow.DONE_BUTTON_ID)
            && (source instanceof SelectGermplasmWindow)) {
            ((SelectGermplasmWindow) source).doneAction();
        }   else {
            LOG.error("GermplasmImportButtonClickListener: Error with buttonClick action. Source not identified.");
        }
    }
}
