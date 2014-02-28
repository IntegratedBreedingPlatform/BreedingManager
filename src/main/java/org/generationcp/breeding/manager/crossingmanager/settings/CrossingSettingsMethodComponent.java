package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.HashMap;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.xml.BreedingMethodSetting;
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
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class CrossingSettingsMethodComponent extends AbsoluteLayout implements
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
	
    public enum CrossingMethodOption{
        SAME_FOR_ALL_CROSSES, BASED_ON_PARENTAL_LINES
    };
    
    private Label crossingMethodLabel;
    private Label methodDescriptionLabel;
    private TextArea crossingMethodDescriptionTextArea;
    
    private OptionGroup crossingMethodOptionGroup;
    private ComboBox crossingMethodComboBox;
    private CheckBox favoriteMethodsCheckbox;
    private Button manageFavoriteMethodsLink;
    
    private HashMap<String, Integer> mapMethods;
    private List<Method> methods;
    
	@Override
	public void afterPropertiesSet() throws Exception {
		assemble();
	}
	
	private void assemble(){
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}

	@Override
	public void updateLabels() {
	}

	@Override
	public void instantiateComponents() {
		
		crossingMethodLabel = new Label(messageSource.getMessage(Message.BREEDING_METHOD).toUpperCase());
		crossingMethodLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		
		methodDescriptionLabel = new Label("<i>" +messageSource.getMessage(Message.METHOD_DESCRIPTION_LABEL) 
				+ "</i>", Label.CONTENT_XHTML);
		methodDescriptionLabel.setWidth("200px");
		
		crossingMethodOptionGroup = new OptionGroup();
        crossingMethodOptionGroup.setImmediate(true);
        crossingMethodOptionGroup.setHeight("50px");
        
        crossingMethodDescriptionTextArea=new TextArea();
        crossingMethodDescriptionTextArea.setWordwrap(true);
        crossingMethodDescriptionTextArea.setWidth("570px");
        crossingMethodDescriptionTextArea.setRows(3);
        crossingMethodDescriptionTextArea.addStyleName("mytextarea");
        crossingMethodDescriptionTextArea.addStyleName(AppConstants.CssStyles.ITALIC);
        crossingMethodDescriptionTextArea.setReadOnly(true);
                   
        crossingMethodComboBox = new ComboBox();
        crossingMethodComboBox.setWidth("280px");
        crossingMethodComboBox.setImmediate(true);
        crossingMethodComboBox.setNullSelectionAllowed(false);

        favoriteMethodsCheckbox = new CheckBox(messageSource.getMessage(Message.SHOW_ONLY_FAVORITE_METHODS));
        favoriteMethodsCheckbox.setImmediate(true);
                
        manageFavoriteMethodsLink = new Button();
        manageFavoriteMethodsLink.setStyleName(BaseTheme.BUTTON_LINK);
        manageFavoriteMethodsLink.setCaption(messageSource.getMessage(Message.MANAGE_METHODS));
	}

	
	private boolean isSameMethodForAll() {
		return CrossingMethodOption.SAME_FOR_ALL_CROSSES.equals(crossingMethodOptionGroup.getValue());
	}

	
	@Override
	public void initializeValues() {
        crossingMethodOptionGroup.addItem(CrossingMethodOption.BASED_ON_PARENTAL_LINES);
        crossingMethodOptionGroup.setItemCaption(CrossingMethodOption.BASED_ON_PARENTAL_LINES, 
                messageSource.getMessage(Message.SET_METHOD_BASED_ON_STATUS_OF_PARENTAL_LINES));
        crossingMethodOptionGroup.addItem(CrossingMethodOption.SAME_FOR_ALL_CROSSES);
        crossingMethodOptionGroup.setItemCaption(CrossingMethodOption.SAME_FOR_ALL_CROSSES, 
                messageSource.getMessage(Message.SELECT_A_METHOD_TO_USE_FOR_ALL_CROSSES));
        crossingMethodOptionGroup.select(CrossingMethodOption.BASED_ON_PARENTAL_LINES);
        
        // retrieve crossing methods
        try {
			methods = germplasmDataManager.getMethodsByType("GEN");
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage());
		}
		
		toggleSameMethodForAllFields(false);

	}

	@Override
	public void addListeners() {
		crossingMethodOptionGroup.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = -818940519031621592L;

			@Override
            public void valueChange(ValueChangeEvent event) {
                toggleCrossingMethodOptionGroup();
            }

        });  
		
		
		crossingMethodComboBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 6294894800193942274L;

			@Override
		    public void valueChange(ValueChangeEvent event) {
    			if(crossingMethodComboBox.size() > 0){
            		showMethodDescription((Integer) event.getProperty().getValue());
    			}
		    }
		});
		
		favoriteMethodsCheckbox.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				resetMethodTextArea();
				populateBreedingMethods(((Boolean) event.getProperty().getValue()));
			}
			
		});
		
		manageFavoriteMethodsLink.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;
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
							Object lastValue = crossingMethodComboBox.getValue();
							populateBreedingMethods(((Boolean) favoriteMethodsCheckbox.getValue()).equals(true));
							crossingMethodComboBox.setValue(lastValue);
						}
					});
				} catch (MiddlewareQueryException e){
					e.printStackTrace();
					LOG.error("Error on manageFavoriteMethods click", e);
				}

			}
        	
        });

	}
	
	public void toggleCrossingMethodOptionGroup(){
		boolean sameForAllCrossesOptionSelected = isSameMethodForAll();
        
        toggleSameMethodForAllFields(sameForAllCrossesOptionSelected);
        
        if(sameForAllCrossesOptionSelected){
            crossingMethodComboBox.focus();
            populateBreedingMethods((Boolean)favoriteMethodsCheckbox.getValue());
			
        }else{
            crossingMethodComboBox.removeAllItems();
            resetMethodTextArea();
            favoriteMethodsCheckbox.setValue(false);
        }
	}
	
	public void showMethodDescription(Integer methodId){
		try {
    		Integer breedingMethodSelected = methodId;
		    String methodDescription=germplasmDataManager.getMethodByID(breedingMethodSelected).getMdesc();
		    crossingMethodDescriptionTextArea.setReadOnly(false);
		    crossingMethodDescriptionTextArea.setValue(methodDescription);
		    crossingMethodDescriptionTextArea.setReadOnly(true);

		} catch (MiddlewareQueryException e) {
		    e.printStackTrace();
		    LOG.error("Error getting method.");
		} catch (ClassCastException e) {
			e.printStackTrace();
			LOG.error("Error getting method");
		}	
	}

	@Override
	public void layoutComponents() {
		addComponent(crossingMethodLabel, "top:5px; left:0px");
        addComponent(crossingMethodOptionGroup, "top:30px;left:0px");
        addComponent(crossingMethodComboBox, "top:58px;left:300px");
        addComponent(favoriteMethodsCheckbox, "top:58px;left:620px");
        addComponent(manageFavoriteMethodsLink, "top:76px;left:640px");
        
        addComponent(methodDescriptionLabel, "top:80px;left:15px");
        addComponent(crossingMethodDescriptionTextArea, "top:95px;left:15px");

	}
	
	private void resetMethodTextArea() {
		crossingMethodDescriptionTextArea.setReadOnly(false);
		crossingMethodDescriptionTextArea.setValue("");
		crossingMethodDescriptionTextArea.setReadOnly(true);
	}
	
    private void populateBreedingMethods(boolean showOnlyFavorites) {
        crossingMethodComboBox.removeAllItems();

        mapMethods = new HashMap<String, Integer>();

        if(showOnlyFavorites){
        	try {
				BreedingManagerUtil.populateWithFavoriteMethods(workbenchDataManager, germplasmDataManager, 
						crossingMethodComboBox, mapMethods);
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
    	crossingMethodComboBox.removeAllItems();
        mapMethods = new HashMap<String, Integer>();
    
        for (Method m : methods) {
        	Integer methodId = m.getMid();
        	crossingMethodComboBox.addItem(methodId);
            crossingMethodComboBox.setItemCaption(methodId, m.getMname());
			mapMethods.put(m.getMname(), new Integer(methodId));
        }
        
    }
    
    private void toggleSameMethodForAllFields(boolean sameForAllCrosses) {
        methodDescriptionLabel.setEnabled(sameForAllCrosses);
        favoriteMethodsCheckbox.setEnabled(sameForAllCrosses);
        crossingMethodComboBox.setEnabled(sameForAllCrosses);
        manageFavoriteMethodsLink.setVisible(sameForAllCrosses);
	}

	public OptionGroup getCrossingMethodOptionGroup() {
		return crossingMethodOptionGroup;
	}

	public ComboBox getCrossingMethodComboBox() {
		return crossingMethodComboBox;
	}
    
	public boolean validateInputFields(){
		if(crossingMethodOptionGroup.getValue().equals(CrossingMethodOption.SAME_FOR_ALL_CROSSES)){
			if(crossingMethodComboBox.getValue() == null){
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.INVALID_INPUT), "No breeding method specified. Please select a breeding method."
						, Notification.POSITION_CENTERED);
				return false;
			}
		}
		return true;
	}

	public void setFields(BreedingMethodSetting breedingMethodSetting) {
		if(breedingMethodSetting.isBasedOnStatusOfParentalLines()){
			crossingMethodOptionGroup.select(CrossingMethodOption.BASED_ON_PARENTAL_LINES);
		}
		else{
			crossingMethodOptionGroup.select(CrossingMethodOption.SAME_FOR_ALL_CROSSES);
			Integer methodId = breedingMethodSetting.getMethodId();
			crossingMethodComboBox.select(methodId);
			showMethodDescription(methodId);
		}
	}

	public void setFieldsDefaultValue() {
		crossingMethodOptionGroup.select(CrossingMethodOption.BASED_ON_PARENTAL_LINES);
		toggleCrossingMethodOptionGroup();
	}
}
