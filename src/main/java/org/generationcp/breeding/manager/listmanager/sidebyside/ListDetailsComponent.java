package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@Configurable 
public class ListDetailsComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	
	private ListManagerMain source;
	private Label listEntriesLabel;
	private ListDataComponent listDataComponent;
	private GermplasmList germplasmList;
	private boolean hasChanged = false;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	public ListDetailsComponent(ListManagerMain source, GermplasmList germplasmList){
		super();
		this.source = source;
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
		listEntriesLabel = new Label(messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
		listEntriesLabel.setStyleName(Bootstrap.Typography.H3.styleName());
		
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
		setMargin(true);
		setSpacing(true);
		addComponent(listEntriesLabel);
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

}
