package org.generationcp.breeding.manager.listimport;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants.CssStyles;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class GermplasmFieldsComponent extends AbsoluteLayout implements
		InternationalizableComponent, InitializingBean, BreedingManagerLayout {

	private static final long serialVersionUID = -1180999883774074687L;
	
	private final static Logger LOG = LoggerFactory.getLogger(GermplasmFieldsComponent.class);
	 
	private static final String DEFAULT_METHOD = "UDM";
	private static final String DEFAULT_LOCATION = "Unknown";
	private  static final String DEFAULT_NAME_TYPE = "Line Name";
	
	private Label addGermplasmDetailsLabel;
    private Label addGermplasmDetailsMessage;
    
	private Label breedingMethodLabel;
    private Label germplasmDateLabel;
	private Label locationLabel;
    private Label nameTypeLabel;
    
    private ComboBox breedingMethodComboBox;
    private ComboBox locationComboBox;
    private ComboBox nameTypeComboBox;
    
    private DateField germplasmDateField;
    
    private List<Location> locations;
    private List<Method> methods;
    private Map<String, String> methodMap;
    
    private CheckBox showFavoriteLocationsCheckBox;
    private CheckBox showFavoriteMethodsCheckBox;
    
    private Button manageFavoriteMethodsLink;
    private Button manageFavoriteLocationsLink;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private LocationDataManager locationDataManager;
    
    @Autowired
    private GermplasmListManager germplasmListManager;

	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
        initializeValues();
        addListeners();
        layoutComponents();
	}
	
	@Override
    public void attach() {
        super.attach();
        updateLabels();
    }

	@Override
	public void updateLabels() {
		messageSource.setValue(breedingMethodLabel, Message.GERMPLASM_BREEDING_METHOD_LABEL);
        messageSource.setValue(germplasmDateLabel, Message.GERMPLASM_DATE_LABEL);
        messageSource.setValue(locationLabel, Message.GERMPLASM_LOCATION_LABEL);
        messageSource.setValue(nameTypeLabel, Message.GERMPLASM_NAME_TYPE_LABEL);
	}

	@Override
	public void instantiateComponents() {
		addGermplasmDetailsLabel = new Label(messageSource.getMessage(Message.ADD_GERMPLASM_DETAILS).toUpperCase());
		addGermplasmDetailsLabel.addStyleName(Bootstrap.Typography.H4.styleName());
		
		addGermplasmDetailsMessage = new Label();
		addGermplasmDetailsMessage.setValue("You can specify following details to apply to the imported germplasm. These details are optional.");
		
		breedingMethodLabel = new Label();
		breedingMethodLabel.addStyleName(CssStyles.BOLD);
        
        breedingMethodComboBox = new ComboBox();
        breedingMethodComboBox.setWidth("320px");
        breedingMethodComboBox.setNullSelectionAllowed(false);
        breedingMethodComboBox.setImmediate(true);
        
        showFavoriteMethodsCheckBox = new CheckBox();
        showFavoriteMethodsCheckBox.setCaption(messageSource.getMessage(Message.SHOW_ONLY_FAVORITE_METHODS));
        showFavoriteMethodsCheckBox.setImmediate(true);
        
        manageFavoriteMethodsLink = new Button();
        manageFavoriteMethodsLink.setStyleName(BaseTheme.BUTTON_LINK);
        manageFavoriteMethodsLink.setCaption(messageSource.getMessage(Message.MANAGE_METHODS));
        
        germplasmDateLabel = new Label();
        germplasmDateLabel.addStyleName(CssStyles.BOLD);
        germplasmDateField =  new DateField();
        germplasmDateField.setResolution(DateField.RESOLUTION_DAY);
        germplasmDateField.setDateFormat(GermplasmImportMain.DATE_FORMAT);
        
        locationLabel = new Label();
        locationLabel.addStyleName(CssStyles.BOLD);
        locationComboBox = new ComboBox();
        locationComboBox.setWidth("300px");
        locationComboBox.setNullSelectionAllowed(false);
        locationComboBox.setImmediate(true);

        showFavoriteLocationsCheckBox = new CheckBox();
        showFavoriteLocationsCheckBox.setCaption(messageSource.getMessage(Message.SHOW_ONLY_FAVORITE_LOCATIONS));
        showFavoriteLocationsCheckBox.setImmediate(true);
        
        manageFavoriteLocationsLink = new Button();
        manageFavoriteLocationsLink.setStyleName(BaseTheme.BUTTON_LINK);
        manageFavoriteLocationsLink.setCaption(messageSource.getMessage(Message.MANAGE_LOCATIONS));
       
        nameTypeLabel = new Label();
        nameTypeLabel.addStyleName(CssStyles.BOLD);
        nameTypeComboBox = new ComboBox();
        nameTypeComboBox.setWidth("400px");
        nameTypeComboBox.setNullSelectionAllowed(false);
        nameTypeComboBox.setImmediate(true);
	}

	@Override
	public void initializeValues() {
		try {
			methods = germplasmDataManager.getAllMethods();
		} catch (MiddlewareQueryException e) {
			LOG.error("Error getting methods " + e.getMessage());
			e.printStackTrace();
		}
        populateMethods();
        
        germplasmDateField.setValue(new Date());
        
        try {
			locations = locationDataManager.getAllLocations();
		} catch (MiddlewareQueryException e) {
			LOG.error("Error getting locations " + e.getMessage());
			e.printStackTrace();
		}
        populateHarvestLocation(false);
        
        try {
			populateNameTypes();
		} catch (MiddlewareQueryException e) {
			LOG.error("Error getting name types " + e.getMessage());
			e.printStackTrace();
		}
		
	}

	@SuppressWarnings("serial")
	@Override
	public void addListeners() {
		breedingMethodComboBox.addListener(new ComboBox.ValueChangeListener(){
			@Override
			public void valueChange(ValueChangeEvent event) {
				updateComboBoxDescription();
			}
        });
        breedingMethodComboBox.addListener(new ComboBox.ItemSetChangeListener(){
			@Override
			public void containerItemSetChange(ItemSetChangeEvent event) {
				updateComboBoxDescription();
			}
        });
        
        showFavoriteMethodsCheckBox.addListener(new Property.ValueChangeListener(){
			@Override
			public void valueChange(ValueChangeEvent event) {
				populateMethods(((Boolean) event.getProperty().getValue()).equals(true));
				updateComboBoxDescription();
			}
		});
        
        manageFavoriteMethodsLink.addListener(new ClickListener(){
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
 							Object lastValue = breedingMethodComboBox.getValue();
 							populateMethods(((Boolean) showFavoriteMethodsCheckBox.getValue()).equals(true));
 							breedingMethodComboBox.setValue(lastValue);
 						}
 					});
 				} catch (MiddlewareQueryException e){
 					LOG.error("Error on manageFavoriteMethods click", e);
 				}
 			}
         });
        
        showFavoriteLocationsCheckBox.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				populateHarvestLocation(((Boolean) event.getProperty().getValue()).equals(true));
			}
			
		});
        
        manageFavoriteLocationsLink.addListener(new ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					Integer wbUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
	                Project project = workbenchDataManager.getLastOpenedProject(wbUserId);
					Window manageFavoriteLocationsWindow = Util.launchLocationManager(workbenchDataManager, project.getProjectId(), getWindow(), messageSource.getMessage(Message.MANAGE_LOCATIONS));
					manageFavoriteLocationsWindow.addListener(new CloseListener(){
						private static final long serialVersionUID = 1L;
						@Override
						public void windowClose(CloseEvent e) {
							Object lastValue = locationComboBox.getValue();
							populateHarvestLocation(((Boolean) showFavoriteLocationsCheckBox.getValue()).equals(true));
							locationComboBox.setValue(lastValue);
						}
					});
				} catch (MiddlewareQueryException e){
					LOG.error("Error on manageFavoriteLocations click", e);
				}
			}
        	
        });
	}

	@Override
	public void layoutComponents() {
		addComponent(addGermplasmDetailsLabel, "top:0px;left:0px");
		addComponent(addGermplasmDetailsMessage, "top:32px;left:0px");
		
		addComponent(breedingMethodLabel, "top:60px;left:0px");
		addComponent(breedingMethodComboBox, "top:60px;left:200px");
		addComponent(showFavoriteMethodsCheckBox, "top:63px;left:527px");
		addComponent(manageFavoriteMethodsLink, "top:81px;left:546px");
		
		addComponent(germplasmDateLabel, "top:90px;left:0px");
		addComponent(germplasmDateField, "top:90px;left:200px");
		
		addComponent(locationLabel, "top:120px;left:0px");
		addComponent(locationComboBox, "top:120px;left:200px");
		addComponent(showFavoriteLocationsCheckBox, "top:122px;left:507px");
		addComponent(manageFavoriteLocationsLink, "top:140px;left:527px");
		
		addComponent(nameTypeLabel, "top:170px;left:0px");
        addComponent(nameTypeComboBox, "top:170px;left:200px");
		
	}
	
	public ComboBox getBreedingMethodComboBox() {
		return breedingMethodComboBox;
	}

	public ComboBox getLocationComboBox() {
		return locationComboBox;
	}

	public ComboBox getNameTypeComboBox() {
		return nameTypeComboBox;
	}

	public DateField getGermplasmDateField() {
		return germplasmDateField;
	}
	
	protected void populateNameTypes() throws MiddlewareQueryException {
		List<UserDefinedField> userDefinedFieldList = germplasmListManager.getGermplasmNameTypes();
        Integer firstId = null;
        boolean hasDefault = false;
        for(UserDefinedField userDefinedField : userDefinedFieldList){
	        if(firstId == null){
	              firstId = userDefinedField.getFldno();
	        }
            nameTypeComboBox.addItem(userDefinedField.getFldno());
            nameTypeComboBox.setItemCaption(userDefinedField.getFldno(), userDefinedField.getFname());
            if(DEFAULT_NAME_TYPE.equalsIgnoreCase(userDefinedField.getFname())){
            	nameTypeComboBox.setValue(userDefinedField.getFldno());
            	hasDefault = true;
            }
        }
        if(hasDefault == false && firstId != null){
            nameTypeComboBox.setValue(firstId);
        }
	}
	
	private void populateMethods(boolean showOnlyFavorites) {
    	breedingMethodComboBox.removeAllItems();

        if(showOnlyFavorites){
        	try {
        		
				BreedingManagerUtil.populateWithFavoriteMethods(workbenchDataManager, 
						germplasmDataManager, breedingMethodComboBox, null);
				
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR), 
						"Error getting favorite methods!");
			}
			
        } else {
        	populateMethods();
        }

    }
	
	private Map<String, String> populateMethods() {
		methodMap = new HashMap<String, String>();
        for(Method method : methods){
        	
            //method.getMcode()
            breedingMethodComboBox.addItem(method.getMid());
            breedingMethodComboBox.setItemCaption(method.getMid(), method.getMname());
            if(DEFAULT_METHOD.equalsIgnoreCase(method.getMcode())){
                breedingMethodComboBox.setValue(method.getMid());
                breedingMethodComboBox.setDescription(method.getMdesc());
            }
            methodMap.put(method.getMid().toString(), method.getMdesc());
        }
        
        if(breedingMethodComboBox.getValue()==null && methods.get(0) != null){
        	breedingMethodComboBox.setValue(methods.get(0).getMid());
        	breedingMethodComboBox.setDescription(methods.get(0).getMdesc());
        }
		return methodMap;
	}
	
	private void populateHarvestLocation(boolean showOnlyFavorites) {
    	locationComboBox.removeAllItems();

        if(showOnlyFavorites){
        	try {
        		
				BreedingManagerUtil.populateWithFavoriteLocations(workbenchDataManager, 
						germplasmDataManager, locationComboBox, null);
				
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR), 
						"Error getting favorite locations!");
			}
			
        } else {
        	populateLocations();
        }

    }

    /*
     * Fill with all locations
     */
	private void populateLocations() {
		Integer firstId = null;
		boolean hasDefault = false;
		for(Location location : locations){
		   //method.getMcode()
		   if(firstId == null){
		       firstId = location.getLocid();
		   }
		   locationComboBox.addItem(location.getLocid());
		   locationComboBox.setItemCaption(location.getLocid(), location.getLname());
		   if(DEFAULT_LOCATION.equalsIgnoreCase(location.getLname())){
		       locationComboBox.setValue(location.getLocid());
		       hasDefault = true;
		   }
         }
		if(hasDefault == false && firstId != null){
		    locationComboBox.setValue(firstId);
		}
	}
	
	private void updateComboBoxDescription(){
    	Object breedingMethodComboBoxValue = breedingMethodComboBox.getValue();
    	breedingMethodComboBox.setDescription("");
    	if(breedingMethodComboBoxValue!=null){
    		breedingMethodComboBox.setDescription(methodMap.get(breedingMethodComboBoxValue.toString()));
    	}
    }
	
	public void setGermplasmBreedingMethod(String breedingMethod){
        breedingMethodComboBox.setNullSelectionAllowed(false);
        breedingMethodComboBox.addItem(breedingMethod);
        breedingMethodComboBox.setValue(breedingMethod);
    }
	
	public void setGermplasmDate(Date germplasmDate) throws ReadOnlyException, ConversionException, ParseException{
        germplasmDateField.setValue(germplasmDate);
	}
	
    public void setGermplasmLocation(String germplasmLocation){
        locationComboBox.setNullSelectionAllowed(false);
        locationComboBox.addItem(germplasmLocation);
        locationComboBox.setValue(germplasmLocation);
    }
    
    public void setGermplasmListType(String germplasmListType){
        nameTypeComboBox.setNullSelectionAllowed(false);
        nameTypeComboBox.addItem(germplasmListType);
        nameTypeComboBox.setValue(germplasmListType);
    }

}
