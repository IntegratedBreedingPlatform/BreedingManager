package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class CrossingManagerSaveCrossListComponent extends VerticalLayout 
	implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	private ManageCrossingSettingsMain source;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	private AbsoluteLayout headerLayout;
	private Label headerLabel;
	private Label subHeaderLabel;
	private SaveCrossListSubComponent specifyCrossListComponent;
	private SaveCrossListSubComponent specifyFemaleParentListComponent;
	private SaveCrossListSubComponent specifyMaleParentListComponent;
	private Button backButton;
	private Button saveButton;
	
	public enum Actions {
    	SAVE, BACK
    }
	
	public CrossingManagerSaveCrossListComponent(ManageCrossingSettingsMain manageCrossingSettingsMain){
    	this.source = manageCrossingSettingsMain;
    }

	@Override
	public void updateLabels() {
		messageSource.setCaption(saveButton, Message.SAVE_LABEL);
		messageSource.setCaption(backButton, Message.BACK);
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
		setMargin(true);
		setSpacing(true);
		
		headerLayout = new AbsoluteLayout();
		headerLayout.setHeight("65px");
		
		headerLabel = new Label();
		headerLabel.setValue(messageSource.getMessage(Message.SAVE_CROSS_LIST_AND_PARENT_LISTS));
		headerLabel.setStyleName(Bootstrap.Typography.H3.styleName());
		
		subHeaderLabel = new Label();
		subHeaderLabel.setValue(messageSource.getMessage(Message.INDICATES_A_MANDATORY_FIELD));
		subHeaderLabel.addStyleName("italic");
		
		headerLayout.addComponent(headerLabel,"top:0px;left:0px");
		headerLayout.addComponent(subHeaderLabel,"top:35px;left:0px");
		
		specifyCrossListComponent = new SaveCrossListSubComponent(
				messageSource.getMessage(Message.SPECIFY_CROSS_LIST_DETAILS),
				messageSource.getMessage(Message.SAVE_CROSS_LIST_AS));
		
		specifyFemaleParentListComponent = new SaveCrossListSubComponent(
				messageSource.getMessage(Message.SPECIFY_FEMALE_PARENT_LIST_DETAILS),
				messageSource.getMessage(Message.SAVE_FEMALE_PARENT_AS));
		
		specifyMaleParentListComponent = new SaveCrossListSubComponent(
				messageSource.getMessage(Message.SPECIFY_MALE_PARENT_LIST_DETAILS),
				messageSource.getMessage(Message.SAVE_MALE_PARENT_AS));
		
		saveButton = new Button();
		saveButton.setWidth("80px");
		saveButton.setData(Actions.SAVE);
		saveButton.setCaption(messageSource.getMessage(Message.SAVE_LABEL));
        saveButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        
        backButton = new Button();
        backButton.setWidth("80px");
        backButton.setData(Actions.BACK);
        backButton.setCaption(messageSource.getMessage(Message.BACK));
        backButton.addStyleName(Bootstrap.Buttons.DEFAULT.styleName());
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
		backButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				source.backStep();
			}
			
		});
		
	}

	@Override
	public void layoutComponents() {
		
		setWidth("850px");
		setHeight("700px");
		
		addComponent(headerLayout);
		addComponent(specifyCrossListComponent);
		addComponent(specifyFemaleParentListComponent);
		addComponent(specifyMaleParentListComponent);
		
		HorizontalLayout buttonBar = new HorizontalLayout();
		buttonBar.setSpacing(true);
		buttonBar.setMargin(true);
		buttonBar.addComponent(backButton);
		buttonBar.addComponent(saveButton);
		
		HorizontalLayout layout = new HorizontalLayout();
		layout.setWidth("100%");
		layout.addComponent(buttonBar);
		layout.setComponentAlignment(buttonBar, Alignment.MIDDLE_CENTER);
		
		addComponent(layout);
	}

}
