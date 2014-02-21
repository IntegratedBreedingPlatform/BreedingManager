package org.generationcp.breeding.manager.crossingmanager.settings;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;

@Configurable
public class ManageCrossingSettingsMain extends AbsoluteLayout implements
		InitializingBean, InternationalizableComponent, BreedingManagerLayout {
	
	private static final long serialVersionUID = 1L;
	
	private ChooseCrossingSettingsComponent chooseSettingsComponent;
	private CrossingSettingsDetailComponent detailComponent;

	@Override
	public void updateLabels() {
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
		chooseSettingsComponent = new ChooseCrossingSettingsComponent();
		detailComponent = new CrossingSettingsDetailComponent();
	}

	@Override
	public void initializeValues() {
	}

	@Override
	public void addListeners() {
	}

	@Override
	public void layoutComponents() {
		setWidth("90%");
		setHeight("800px");
		
		addComponent(chooseSettingsComponent);
		addComponent(detailComponent, "top:65px;");
	}

}
