package org.generationcp.breeding.manager.listmanager.listeners;

import java.util.ArrayList;

import org.generationcp.breeding.manager.listmanager.GermplasmPedigreeTreeComponent;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ExpandEvent;

public class GermplasmTreeExpandListener implements Tree.ExpandListener{
    
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmTreeExpandListener.class);
    private static final long serialVersionUID = 3215012575002448725L;
    private Tree source;
    @SuppressWarnings("unused")
    private ArrayList<Object> parameters;

    public GermplasmTreeExpandListener(Tree source) {
        this.source = source;
    }

    public GermplasmTreeExpandListener(Tree source, ArrayList<Object> parameters) {
        this(source);
        this.parameters = parameters;
    }

    @Override
    public void nodeExpand(ExpandEvent event) {
        if (source instanceof GermplasmPedigreeTreeComponent) {
            try{
                ((GermplasmPedigreeTreeComponent) source).pedigreeTreeExpandAction((String) event.getItemId());
            }catch (InternationalizableException e){
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
                MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
            }
        }
    }

}
