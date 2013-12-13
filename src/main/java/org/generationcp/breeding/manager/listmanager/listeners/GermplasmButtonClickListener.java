package org.generationcp.breeding.manager.listmanager.listeners;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.generationcp.breeding.manager.listmanager.GermplasmPedigreeComponent;
import org.generationcp.breeding.manager.listmanager.GermplasmPedigreeGraphComponent;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.Reindeer;

public class GermplasmButtonClickListener implements ClickListener {

	private static final long serialVersionUID = 1L;
	private Object source;
	private Integer gid;
	
	public GermplasmButtonClickListener(Object source, Integer gid) {
		this.source = source;
		this.gid = gid;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Object eventData = event.getButton().getData();
		
		// Germplasm Pedigree Tree
		if (source instanceof GermplasmPedigreeComponent){

			GermplasmPedigreeComponent germplasmPedigreeComponent = (GermplasmPedigreeComponent) source;
			if (GermplasmPedigreeComponent.APPLY.equals(eventData)){
				germplasmPedigreeComponent.refreshPedigreeTree(); // call from component directly since UI logic only
	
			} else if (GermplasmPedigreeComponent.VIEW_PEDIGREE_GRAPH_ID.equals(eventData)){
				viewPedigreeGraphClickAction(germplasmPedigreeComponent);
			}
		} else if (source instanceof GermplasmPedigreeGraphComponent){
			GermplasmPedigreeGraphComponent graphComponent = (GermplasmPedigreeGraphComponent) source;			
			try {
				graphComponent.updatePedigreeGraphButtonClickAction(); // call from component directly since UI logic only
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
			} 
		}
	
	}
	
    public void viewPedigreeGraphClickAction(GermplasmPedigreeComponent component) throws InternationalizableException {

        try {
            Window pedigreeGraphWindow = new Window("Pedigree Graph");
            pedigreeGraphWindow.setModal(true);
            pedigreeGraphWindow.setWidth("100%");
            pedigreeGraphWindow.setHeight("620px");
            pedigreeGraphWindow.setName("Pedigree Graph");
            pedigreeGraphWindow.addStyleName(Reindeer.WINDOW_LIGHT);
            pedigreeGraphWindow.addComponent(new GermplasmPedigreeGraphComponent(this.gid, component.getGermplasmQueries()));
            component.getWindow().addWindow(pedigreeGraphWindow);
            
        } catch (Exception e) {
//            throw new InternationalizableException(e, Message.ERROR_IN_SEARCH, Message.EMPTY_STRING);
        	e.printStackTrace();
        }
    }

}
