package org.generationcp.breeding.manager.crossingmanager.settings;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Label;

@Configurable
public class ManageCrossingSettingsMain extends AbsoluteLayout implements
		InitializingBean, InternationalizableComponent, BreedingManagerLayout {
	
	private static final long serialVersionUID = 1L;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	private Label manageCrossingSettingsLabel;
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
		manageCrossingSettingsLabel = new Label(messageSource.getMessage(Message.MANAGE_SAVED_CROSSING_SETTINGS));
		manageCrossingSettingsLabel.setStyleName(Bootstrap.Typography.H1.styleName());
		
		chooseSettingsComponent = new ChooseCrossingSettingsComponent(this);
		detailComponent = new CrossingSettingsDetailComponent(this);
	}

	@Override
	public void initializeValues() {
	}

	@Override
	public void addListeners() {
	}

	@Override
	public void layoutComponents() {
		setWidth("1000px");
		setHeight("800px");
		
		addComponent(manageCrossingSettingsLabel);
		addComponent(chooseSettingsComponent, "top:45px");
		addComponent(detailComponent, "top:100px;");
	}

	public ChooseCrossingSettingsComponent getChooseSettingsComponent() {
		return chooseSettingsComponent;
	}

	public CrossingSettingsDetailComponent getDetailComponent() {
		return detailComponent;
	}
	
	

}
