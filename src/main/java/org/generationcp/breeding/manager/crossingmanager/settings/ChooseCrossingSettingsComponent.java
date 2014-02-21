package org.generationcp.breeding.manager.crossingmanager.settings;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ChooseCrossingSettingsComponent extends AbsoluteLayout implements
		InitializingBean, InternationalizableComponent, BreedingManagerLayout {
	
	public enum Actions {
		COPY_SETTING, ADD_SETTING, RESET_SETTING, DELETE_SETTING
	}

	private static final long serialVersionUID = 1L;

	private Label chooseSettingsLabel;
	private ComboBox settingsComboBox;
	private Button copySettingButton;
	private Button addSettingButton;
	private Button resetSettingButton;
	private Button deleteSettingButton;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Override
	public void updateLabels() {
	}

	@Override
	public void layoutComponents() {
		setHeight("40px");
		
		addStyleName(AppConstants.CssStyles.GRAY_PANEL_WITH_BORDER);
		
		addComponent(chooseSettingsLabel,"top:13px; left:20px");
		addComponent(settingsComboBox, "top:10px; left:170px");
		addComponent(copySettingButton,"top:13px; left:400px");
		addComponent(addSettingButton,"top:13px; left:520px");
		addComponent(resetSettingButton,"top:13px; left:660px");
		addComponent(deleteSettingButton,"top:13px; left:740px");
		
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		assemble();
	}

	@Override
	public void instantiateComponents() {
		chooseSettingsLabel = new Label(
				messageSource.getMessage(Message.CHOOSE_SAVED_SETTINGS));
		
		settingsComboBox = new ComboBox();
		settingsComboBox.setWidth("170px");
		
		copySettingButton = new Button(messageSource.getMessage(Message.MAKE_A_COPY));
		copySettingButton.setData(Actions.COPY_SETTING);
//		copySettingButton.setIcon(ICON_LOCK);
//		copySettingButton.setWidth("140px");
//        copySettingButton.setDescription(LOCK_TOOLTIP);
		copySettingButton.setStyleName(Reindeer.BUTTON_LINK);
		
		addSettingButton = new Button(messageSource.getMessage(Message.ADD_NEW_SETTINGS));
		addSettingButton.setData(Actions.ADD_SETTING);
		addSettingButton.setStyleName(Reindeer.BUTTON_LINK);
		
		resetSettingButton = new Button(messageSource.getMessage(Message.RESET));
		resetSettingButton.setData(Actions.RESET_SETTING);
		resetSettingButton.setStyleName(Reindeer.BUTTON_LINK);
		
		deleteSettingButton = new Button(messageSource.getMessage(Message.DELETE));
		deleteSettingButton.setData(Actions.DELETE_SETTING);
		deleteSettingButton.setStyleName(Reindeer.BUTTON_LINK);
	}

	private void assemble() {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}

	@Override
	public void initializeValues() {
		//TODO replace with real settings from DB
		settingsComboBox.addItem(1);
		settingsComboBox.setItemCaption(1, "Cross Type 1");
		settingsComboBox.addItem(2);
		settingsComboBox.setItemCaption(2, "Cross Type 2");
		
	}

	@Override
	public void addListeners() {
		// TODO Auto-generated method stub
	}
	

}
