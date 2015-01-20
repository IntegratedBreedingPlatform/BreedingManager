package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.HashMap;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.xml.BreedingMethodSetting;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.workbench.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class CrossingSettingsMethodComponent extends CssLayout implements
		InternationalizableComponent, InitializingBean,
		BreedingManagerLayout {

	private static final long serialVersionUID = 8287596386088188565L;
	private static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsMethodComponent.class);

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    @Autowired
    private GermplasmDataManager germplasmDataManager;

    @Autowired
    private WorkbenchDataManager workbenchDataManager;

    private Label breedingMethodLabel;
    private Label breedingMethodDescLabel;

    private CheckBox selectMethod;

    private ComboBox breedingMethods;

    private CheckBox favoriteMethodsCheckbox;
    private Button manageFavoriteMethodsLink;

    private PopupView methodPopupView;
    private Label breedingMethodsHelpPopup;

    private HashMap<String, Integer> mapMethods;
    private List<Method> methods;
    
    @Autowired
	private BreedingManagerService breedingManagerService;
	private String programUniqueId;

	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
		initPopulateFavMethod(programUniqueId);		
	}
	
	public boolean initPopulateFavMethod(String programUUID){
		boolean hasFavorite = false;
		if(BreedingManagerUtil.hasFavoriteMethods(germplasmDataManager,programUUID)){
			favoriteMethodsCheckbox.setValue(true);
        	populateBreedingMethods(true,programUniqueId);
        	hasFavorite = true;
        }
		return hasFavorite;
	}

	@Override
	public void updateLabels() {
		breedingMethodLabel.setValue(messageSource.getMessage(Message.BREEDING_METHOD).toUpperCase());
		breedingMethodDescLabel.setValue(messageSource.getMessage(Message.BREEDING_METHOD_DESC));
	}

	@Override
	public void instantiateComponents() {

		breedingMethodLabel = new Label(messageSource.getMessage(Message.BREEDING_METHOD).toUpperCase());
		breedingMethodLabel.setStyleName(Bootstrap.Typography.H2.styleName());

		breedingMethodDescLabel = new Label(messageSource.getMessage(Message.BREEDING_METHOD_DESC));
		breedingMethodDescLabel.addStyleName("gcp-content-help-text");

		selectMethod = new CheckBox(messageSource.getMessage(Message.SELECT_A_METHOD_TO_USE_FOR_ALL_CROSSES) + ":");
		selectMethod.setImmediate(true);

        breedingMethods = new ComboBox();
        breedingMethods.setImmediate(true);
        breedingMethods.setNullSelectionAllowed(false);

        breedingMethodsHelpPopup = new Label();
        breedingMethodsHelpPopup.setEnabled(false);
        breedingMethodsHelpPopup.setWidth("500px");

        methodPopupView = new PopupView("?", breedingMethodsHelpPopup);
        methodPopupView.addStyleName(AppConstants.CssStyles.POPUP_VIEW);

        favoriteMethodsCheckbox = new CheckBox(messageSource.getMessage(Message.SHOW_ONLY_FAVORITE_METHODS));
        favoriteMethodsCheckbox.setImmediate(true);

        manageFavoriteMethodsLink = new Button();
        manageFavoriteMethodsLink.setStyleName(BaseTheme.BUTTON_LINK);
        manageFavoriteMethodsLink.setCaption(messageSource.getMessage(Message.MANAGE_METHODS));
        
        try {
			programUniqueId = breedingManagerService.getCurrentProject().getUniqueID();
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(),e);
		}
	}

	@Override
	public void initializeValues() {
		selectMethod.setValue(false);
		breedingMethods.setValue(null);
		showBreedingMethodSelection(false);

		// Retrieve breeding methods
        try {
			methods = germplasmDataManager.getMethodsByType("GEN");
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage());
		}
	}

	@Override
	public void addListeners() {

		selectMethod.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = -1282191407425721085L;

			@Override
			public void valueChange(ValueChangeEvent event) {

				final Boolean selectMethod = (Boolean) event.getProperty().getValue();

				showBreedingMethodSelection(selectMethod);

				if (selectMethod) {
					populateBreedingMethods((Boolean)favoriteMethodsCheckbox.getValue(),programUniqueId);
					breedingMethods.focus();
					enableMethodHelp(false);
				} else {
					breedingMethods.setValue(null);
				}
		    }
		});

		breedingMethods.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 6294894800193942274L;

			@Override
		    public void valueChange(ValueChangeEvent event) {
    			if (breedingMethods.size() > 0) {
            		showMethodDescription((Integer) event.getProperty().getValue());
    			}

    			final Boolean methodSelected = breedingMethods.getValue() != null;
    			enableMethodHelp(methodSelected);
		    }
		});

		favoriteMethodsCheckbox.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = -4064520391948241747L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				populateBreedingMethods(((Boolean) event.getProperty().getValue()),programUniqueId);
			}

		});

		manageFavoriteMethodsLink.addListener(new ClickListener(){
			private static final long serialVersionUID = 1525347479193533974L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					Integer wbUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
	                Project project = workbenchDataManager.getLastOpenedProject(wbUserId);
					Window manageFavoriteMethodsWindow = Util.launchMethodManager(workbenchDataManager, project.getProjectId(), getWindow(), messageSource.getMessage(Message.MANAGE_METHODS));
					manageFavoriteMethodsWindow.addListener(new CloseListener(){
						private static final long serialVersionUID = 1L;
						@Override
						public void windowClose(CloseEvent e) {
							Object lastValue = breedingMethods.getValue();
							populateBreedingMethods(((Boolean) favoriteMethodsCheckbox.getValue()).equals(true),programUniqueId);
							breedingMethods.setValue(lastValue);
						}
					});
				} catch (MiddlewareQueryException e){
					LOG.error(e.getMessage(),e);
				}
			}
        });
	}

	@Override
	public void layoutComponents() {

		addComponent(breedingMethodLabel);
		addComponent(breedingMethodDescLabel);

		final HorizontalLayout comboBoxLayout = new HorizontalLayout();
		comboBoxLayout.addComponent(selectMethod);

		final VerticalLayout comboBoxAndSettings = new VerticalLayout();

		comboBoxAndSettings.addComponent(breedingMethods);
		comboBoxAndSettings.addComponent(favoriteMethodsCheckbox);
		comboBoxAndSettings.addComponent(manageFavoriteMethodsLink);

		selectMethod.addStyleName("cs-form-label");
		breedingMethods.addStyleName("cs-form-input");
		methodPopupView.addStyleName("cs-inline-icon");

		comboBoxLayout.addComponent(comboBoxAndSettings);
		comboBoxLayout.addComponent(methodPopupView);
		addComponent(comboBoxLayout);
	}

	public void setFields(BreedingMethodSetting breedingMethodSetting) {
		if (breedingMethodSetting.isBasedOnStatusOfParentalLines()) {
			selectMethod.setValue(false);
		} else {
			selectMethod.setValue(true);
			final Integer methodId = breedingMethodSetting.getMethodId();
			
			if(!isMethodGen(methodId)){
				favoriteMethodsCheckbox.setValue(true);
				populateBreedingMethods(true,programUniqueId);
			}
			else{
				favoriteMethodsCheckbox.setValue(false);
				populateBreedingMethods(false,programUniqueId);
			}
			breedingMethods.select(methodId);
			showMethodDescription(methodId);
		}
	}

	private boolean isMethodGen(Integer methodId) {
		for(Method method : methods){
			if(method.getMid().equals(methodId)){
				return true;
			}
		}
		return false;
	}

	public void setFieldsDefaultValue() {
		selectMethod.setValue(false);
		breedingMethods.setValue(null);
	}

	/**
	 * Whether or not the user is leaving the crossing method to be determined by the status of parental lines.
	 *
	 * @return true if the method is based on the status of parental lines, false if one method has been selected for all crosses
	 */
	public Boolean isBasedOnStatusOfParentalLines() {
		// The checkbox is actually the reverse of this - false indicates based on parental lines (as this is the default)
		return !selectMethod.booleanValue();
	}

	/**
	 * Get the ID of the currently selected breeding method.
	 *
	 * @return the ID of the currently selected breeding method, or null if none are selected.
	 */
	public Integer getSelectedBreedingMethodId() {
		return (Integer) breedingMethods.getValue();
	}

	private void showMethodDescription(Integer methodId){
		if(methodId != null){
			try {				
			    final String methodDescription = germplasmDataManager.getMethodByID(methodId).getMdesc();
			    breedingMethodsHelpPopup.setValue(methodDescription);
			    breedingMethods.setDescription(methodDescription);

			} catch (MiddlewareQueryException e) {
			    LOG.error(e.getMessage(),e);
			} catch (ClassCastException e) {
				LOG.error(e.getMessage(),e);
			}
		}
	}

	private void enableMethodHelp (final Boolean enable) {
    	breedingMethodsHelpPopup.setEnabled(enable);
    	methodPopupView.setEnabled(enable);
    }

	private void showBreedingMethodSelection (final Boolean show) {
		breedingMethods.setVisible(show);
    	favoriteMethodsCheckbox.setVisible(show);
    	manageFavoriteMethodsLink.setVisible(show);
    	breedingMethodsHelpPopup.setVisible(show);
    	methodPopupView.setVisible(show);
	}

    private void populateBreedingMethods(boolean showOnlyFavorites, String programUUID) {
        breedingMethods.removeAllItems();

        mapMethods = new HashMap<String, Integer>();

        if(showOnlyFavorites){
        	try {
				BreedingManagerUtil.populateWithFavoriteMethods(workbenchDataManager, germplasmDataManager,
						breedingMethods, mapMethods, programUUID);
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR),
				"Error getting favorite methods!");
			}

        } else {
        	populateBreedingMethod();
        }
    }

    private void populateBreedingMethod(){
    	
        try {
			methods = germplasmDataManager.getMethodsByType("GEN");
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage());
		}
        
    	breedingMethods.removeAllItems();
        mapMethods = new HashMap<String, Integer>();

        for (Method m : methods) {
        	Integer methodId = m.getMid();
        	breedingMethods.addItem(methodId);
            breedingMethods.setItemCaption(methodId, m.getMname());
			mapMethods.put(m.getMname(), new Integer(methodId));
        }
    }
    
	public boolean validateInputFields(){
		if(((Boolean) selectMethod.getValue()) && breedingMethods.getValue()==null) {
			MessageNotifier.showRequiredFieldError(getWindow(), messageSource.getMessage(Message.PLEASE_CHOOSE_CROSSING_METHOD));
			return false;
		}
		return true;
	}

	public SimpleResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public GermplasmDataManager getGermplasmDataManager() {
		return germplasmDataManager;
	}

	public void setGermplasmDataManager(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	public void setBreedingManagerService(
			BreedingManagerService breedingManagerService) {
		this.breedingManagerService = breedingManagerService;
	}
}
