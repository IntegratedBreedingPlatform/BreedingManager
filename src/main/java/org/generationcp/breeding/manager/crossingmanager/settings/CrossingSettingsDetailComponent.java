package org.generationcp.breeding.manager.crossingmanager.settings;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

@Configurable
public class CrossingSettingsDetailComponent extends AbsoluteLayout 
	implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {
	
	private static final long serialVersionUID = -7733004867121978697L;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public enum Actions {
    	SAVE, CANCEL
    }
	
	private Label mandatoryLabel;
	private CrossingSettingsMethodComponent methodComponent;
	private CrossingSettingsNameComponent nameComponent;
	private CrossingSettingsOtherDetailsComponent additionalDetailsComponent;
	
	private Button saveButton;
	private Button cancelButton;
	
	
	@Override
	public void attach() {
		super.attach();
		updateLabels();
	}
	
	@Override
	public void updateLabels() {
		messageSource.setCaption(saveButton, Message.SAVE_LABEL);
		messageSource.setCaption(cancelButton, Message.CANCEL);
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
		mandatoryLabel = new Label("<i>" +messageSource.getMessage(Message.MANDATORY_FIELDS_ARE_NOTED)
				+ "</i>", Label.CONTENT_XHTML);
		
		methodComponent = new CrossingSettingsMethodComponent();
		nameComponent = new CrossingSettingsNameComponent();
		additionalDetailsComponent = new CrossingSettingsOtherDetailsComponent();
		
        saveButton = new Button();
        saveButton.setData(Actions.SAVE);
        saveButton.setWidth("80px");
        saveButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        
        cancelButton = new Button();
        cancelButton.setData(Actions.CANCEL);
        cancelButton.setWidth("80px");
        cancelButton.addStyleName(Bootstrap.Buttons.DEFAULT.styleName());
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
		addStyleName(AppConstants.CssStyles.GRAY_BORDER);
		
		addComponent(mandatoryLabel, "top:10px; left:10px");
		addComponent(methodComponent, "top:30px; left:10px");
		addComponent(nameComponent, "top:250px; left:10px");
		addComponent(additionalDetailsComponent, "top:380px; left:10px");
		
		HorizontalLayout buttonBar = new HorizontalLayout();
		buttonBar.setWidth("200px");
		buttonBar.addComponent(saveButton);
		buttonBar.addComponent(cancelButton);
		
		HorizontalLayout layout = new HorizontalLayout();
		layout.setWidth("100%");
		layout.addComponent(buttonBar);
		layout.setComponentAlignment(buttonBar, Alignment.MIDDLE_CENTER);
		
		addComponent(layout, "top:570px; left:0px");
	}

	

}
