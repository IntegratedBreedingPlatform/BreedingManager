package org.generationcp.breeding.manager.listmanager.sidebyside;

import com.vaadin.ui.VerticalLayout;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.CssLayout;

@Configurable 
public class ListTabComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	
	private final ListManagerMain source;
	private final ListSelectionLayout listSelectionLayout;
	private ListComponent listComponent;
	private final GermplasmList germplasmList;
	private boolean hasChanged = false;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	public ListTabComponent(ListManagerMain source, ListSelectionLayout detailsLayout, GermplasmList germplasmList){
		super();
		this.source = source;
		this.listSelectionLayout = detailsLayout;
		this.germplasmList = germplasmList;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		listComponent = new ListComponent(source, this, germplasmList);
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void layoutComponents() {
		setMargin(true);
		setSizeFull();
		addComponent(listComponent);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
	
	public boolean hasChanged() {
	    return hasChanged;
	}
	
	public void setChanged(boolean hasChanged) {
	    this.hasChanged = hasChanged;
	}
	
	public void setListNameLabel(String name){
	    //TODO implement changing of List Name in details section for Rename List
        //lblName.setValue("<b>"+messageSource.getMessage(Message.NAME_LABEL)+":</b>&nbsp;&nbsp;"+name);
    }
	
	public ListSelectionLayout getListSelectionLayout() {
	    return this.listSelectionLayout;
	}
	
	public ListComponent getListDataComponent() {
        return this.listComponent;
    }

	public GermplasmList getGermplasmList(){
		return germplasmList;
	}
}
