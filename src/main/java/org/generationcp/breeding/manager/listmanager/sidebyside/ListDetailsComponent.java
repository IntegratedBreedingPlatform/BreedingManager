package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
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
	private Integer listId;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	public ListDetailsComponent(ListManagerMain source, Integer listId){
		super();
		this.source = source;
		this.listId = listId;
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
		
		listDataComponent = new ListDataComponent(source,listId);
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

}
