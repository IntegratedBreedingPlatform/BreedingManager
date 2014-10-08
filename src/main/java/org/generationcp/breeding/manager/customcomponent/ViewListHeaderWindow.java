package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ViewListHeaderWindow extends BaseSubWindow implements BreedingManagerLayout, InitializingBean, InternationalizableComponent {
	private static final long serialVersionUID = -8930840553445674785L;

	private GermplasmList germplasmList;
	private ViewListHeaderComponent listHeaderComponent;
	
	public ViewListHeaderWindow(GermplasmList germplasmList){
		super();
		this.germplasmList = germplasmList;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}
	
	public ViewListHeaderComponent getListHeaderComponent(){
		return listHeaderComponent;
	}
	
	@Override
	public void instantiateComponents() {
		setCaption("List Header Details");
		addStyleName(Reindeer.WINDOW_LIGHT);
		setResizable(false);
		setModal(true);
		setHeight("340px");
		setWidth("350px");
		
		listHeaderComponent = new ViewListHeaderComponent(germplasmList);
	}

	@Override
	public void initializeValues() {
		
	}

	@Override
	public void addListeners() {
		
	}

	@Override
	public void layoutComponents() {
		addComponent(listHeaderComponent);
	}

	@Override
	public void updateLabels() {
		
	}
	
}
