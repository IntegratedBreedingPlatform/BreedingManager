package org.generationcp.breeding.manager.application;

import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;


public class MainApplicationSelectedTabChangeListener implements TabSheet.SelectedTabChangeListener {
    
    private static final long serialVersionUID = -4016429454543412427L;

    private static final Logger LOG = LoggerFactory.getLogger(MainApplicationSelectedTabChangeListener.class);
    
    private Object source;
    
    public MainApplicationSelectedTabChangeListener(Object source){
        this.source = source;
    }

    @Override
    public void selectedTabChange(SelectedTabChangeEvent event) {
        if (source instanceof BreedingManagerApplication) {
            
            try {
                ((BreedingManagerApplication) source).tabSheetSelectedTabChangeAction(event.getTabSheet());
            } catch (InternationalizableException e) {
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(((BreedingManagerApplication) source).getMainWindow(), 
                        e.getCaption(), e.getDescription()); 
            }

        }
    }
}
