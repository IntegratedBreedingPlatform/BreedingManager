package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ReadOnlyStatusChangeEvent;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ChooseCrossingSettingsComponent extends AbsoluteLayout implements
		InitializingBean, InternationalizableComponent, BreedingManagerLayout {
	
	public ManageCrossingSettingsMain manageCrossingSettingsMain;
	
	public enum Actions {
		COPY_SETTING, ADD_SETTING, RESET_SETTING, DELETE_SETTING
	}

	private static final long serialVersionUID = 1L;
	
	private static final ThemeResource COPY_ICON = new ThemeResource("images/copy-icon.png");
	private static final ThemeResource ADD_ICON = new ThemeResource("images/plus-icon-blue.png");
	private static final ThemeResource RESET_ICON = new ThemeResource("images/reset-icon.png");
	private static final ThemeResource TRASH_ICON = new ThemeResource("images/trash-icon-blue.png");

	private Label chooseSettingsLabel;
	private ComboBox settingsComboBox;
	private Button copySettingButton;
	private Button addSettingButton;
	private Button resetSettingButton;
	private Button deleteSettingButton;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
    WorkbenchDataManager workbenchDataManager;
	
	public ChooseCrossingSettingsComponent(ManageCrossingSettingsMain manageCrossingSettingsMain) {
		this.manageCrossingSettingsMain = manageCrossingSettingsMain;
	}

	@Override
	public void updateLabels() {
	}

	@Override
	public void layoutComponents() {
		setHeight("40px");
		setWidth("850px");
		
		addStyleName(AppConstants.CssStyles.GRAY_ROUNDED_BORDER);
		
		addComponent(chooseSettingsLabel,"top:13px; left:10px");
		addComponent(settingsComboBox, "top:10px; left:160px");
		addComponent(copySettingButton,"top:13px; left:390px");
		addComponent(addSettingButton,"top:13px; left:510px");
		addComponent(resetSettingButton,"top:13px; left:660px");
		addComponent(deleteSettingButton,"top:13px; left:750px");
		
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
		settingsComboBox.setImmediate(true);
		settingsComboBox.setNullSelectionAllowed(true);
		settingsComboBox.setTextInputAllowed(false);
		
		copySettingButton = new Button(messageSource.getMessage(Message.MAKE_A_COPY));
		copySettingButton.setData(Actions.COPY_SETTING);
		copySettingButton.setIcon(COPY_ICON);
		copySettingButton.setStyleName(Reindeer.BUTTON_LINK);
		
		addSettingButton = new Button(messageSource.getMessage(Message.ADD_NEW_SETTINGS));
		addSettingButton.setData(Actions.ADD_SETTING);
		addSettingButton.setIcon(ADD_ICON);
		addSettingButton.setStyleName(Reindeer.BUTTON_LINK);
		
		resetSettingButton = new Button(messageSource.getMessage(Message.RESET));
		resetSettingButton.setData(Actions.RESET_SETTING);
		resetSettingButton.setIcon(RESET_ICON);
		resetSettingButton.setStyleName(Reindeer.BUTTON_LINK);
		
		deleteSettingButton = new Button(messageSource.getMessage(Message.DELETE));
		deleteSettingButton.setIcon(TRASH_ICON);
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
		setSettingsComboBox(null);
	}
	
	public void setSettingsComboBox(TemplateSetting currentSetting){
		TemplateSetting templateSettingFilter = new TemplateSetting();
		settingsComboBox.removeAllItems();
		try {
			List<TemplateSetting> templateSettings = workbenchDataManager.getTemplateSettings(templateSettingFilter);
			
			for(TemplateSetting ts : templateSettings){
				settingsComboBox.addItem(ts);
				settingsComboBox.setItemCaption(ts, ts.getName());
			}
			
			if(currentSetting != null){
				 settingsComboBox.select(currentSetting);
			}
			
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addListeners() {
		
		settingsComboBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if(settingsComboBox.getValue() != null){
					manageCrossingSettingsMain.getDetailComponent().setCurrentSetting(getSelectedTemplateSetting());
					manageCrossingSettingsMain.getDetailComponent().setManageCrossingSettingsFields();
				}
				else{
				    manageCrossingSettingsMain.getDetailComponent().setCurrentSetting(null);
					manageCrossingSettingsMain.getDetailComponent().setDefaultManageCrossingSettingsFields();
				}
			}
		});
		
		resetSettingButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -432280582291837428L;

			@Override
			public void buttonClick(ClickEvent event) {
				manageCrossingSettingsMain.getDetailComponent().doResetAction();
			}
		});
		
		deleteSettingButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -432280582291837428L;

			@Override
			public void buttonClick(ClickEvent event) {
				manageCrossingSettingsMain.getDetailComponent().doDeleteAction();
			}
		});
	}
	
	public TemplateSetting getSelectedTemplateSetting(){
		return (TemplateSetting) settingsComboBox.getValue();
	}

}
