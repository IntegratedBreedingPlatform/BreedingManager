package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.VerticalLayout;

@Configurable 
public class ListTabComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	
	private final ListManagerMain source;
	private final ListSelectionLayout listSelectionLayout;
	private ListComponent listComponent;
	private final GermplasmList germplasmList;
	
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
		if(germplasmList != null){
			listSelectionLayout.addUpdateListStatusForChanges(listComponent, false);
		}
	}

	@Override
	public void initializeValues() {
		// do nothing
	}

	@Override
	public void addListeners() {
		// do nothing
	}

	@Override
	public void layoutComponents() {
		setMargin(true);
		addComponent(listComponent);
	}

	@Override
	public void updateLabels() {
		// do nothing
	}
	
	public void setListNameLabel(String name){
	    //TODO implement changing of List Name in details section for Rename List
    }
	
	public ListSelectionLayout getListSelectionLayout() {
	    return this.listSelectionLayout;
	}
	
	public ListComponent getListComponent() {
        return this.listComponent;
    }

	public GermplasmList getGermplasmList(){
		return germplasmList;
	}
}
