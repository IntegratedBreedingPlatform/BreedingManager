package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.CssLayout;

@Configurable 
public class ListDetailsComponent extends CssLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	
	private final ListManagerMain source;
	private final ListManagerDetailsLayout detailsLayout;
	private ListDataComponent listDataComponent;
	private final GermplasmList germplasmList;
	private boolean hasChanged = false;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	public ListDetailsComponent(ListManagerMain source, ListManagerDetailsLayout detailsLayout, GermplasmList germplasmList){
		super();
		this.source = source;
		this.detailsLayout = detailsLayout;
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
		listDataComponent = new ListDataComponent(source, this, germplasmList);
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
		setMargin(true,true,false,true);
		setSizeFull();
		addComponent(listDataComponent);
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
	
	public ListManagerDetailsLayout getDetailsLayout() {
	    return this.detailsLayout;
	}
	
	public ListDataComponent getListDataComponent() {
        return this.listDataComponent;
    }

}
