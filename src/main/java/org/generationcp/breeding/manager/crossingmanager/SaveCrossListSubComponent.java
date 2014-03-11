package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SaveCrossListSubComponent extends AbsoluteLayout 
		implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;

	private String headerCaption;
	private String saveAsCaption;
	
	private Label headerLabel;
	private Label saveAsLabel;
	private Label descriptionLabel;
	private Label listTypeLabel;
	private Label listDateLabel;

	private Button saveListNameButton;
	private TextArea descriptionTextArea;
	private ComboBox listTypeComboBox;
	private DateField listDtDateField;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	public SaveCrossListSubComponent(String headerCaption, String saveAsCaption){
		this.headerCaption = headerCaption;
		this.saveAsCaption = saveAsCaption;
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
		
		headerLabel = new Label();
		headerLabel.setValue(headerCaption);
		headerLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		
		saveAsLabel = new Label();
		saveAsLabel.setCaption(markAsMandatoryField(saveAsCaption));
		
		descriptionLabel = new Label();
		descriptionLabel.setCaption(markAsMandatoryField(messageSource.getMessage(Message.DESCRIPTION_LABEL)));
		
		listTypeLabel = new Label();
		listTypeLabel.setCaption(markAsMandatoryField(messageSource.getMessage(Message.LIST_TYPE)));
		
		listDateLabel = new Label();
		listDateLabel.setCaption(messageSource.getMessage(Message.LIST_DATE) + ":");
		
		saveListNameButton = new Button();
		saveListNameButton.setCaption(messageSource.getMessage(Message.CHOOSE_LOCATION));
		saveListNameButton.setStyleName(Reindeer.BUTTON_LINK);
		
		descriptionTextArea = new TextArea();
		descriptionTextArea.setWidth("260px");
		descriptionTextArea.setHeight("50px");
		
		listTypeComboBox = new ComboBox();
		
		listDtDateField = new DateField();
		
	}
	
	public String markAsMandatoryField(String label){
		return label + ": *";
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
		setHeight("150px");
		setWidth("800px");
		
		addComponent(headerLabel,"top:0px;left:0px");
		
		addComponent(saveAsLabel,"top:58px;left:0px");
		addComponent(saveListNameButton,"top:40px;left:200px");
		
		addComponent(descriptionLabel,"top:98px;left:0px");
		addComponent(descriptionTextArea,"top:80px;left:120px");
		
		addComponent(listTypeLabel,"top:98px;left:400px");
		addComponent(listTypeComboBox,"top:80px;left:480px");
		
		addComponent(listDateLabel,"top:128px;left:400px");
		addComponent(listDtDateField,"top:110px;left:480px");
	}

	@Override
	public void updateLabels() {
		
	}

}
