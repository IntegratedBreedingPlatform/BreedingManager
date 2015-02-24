package org.generationcp.breeding.manager.customfields;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.workbench.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class BreedingLocationField extends AbsoluteLayout
implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 4506866031376540836L;
	private final static Logger LOG = LoggerFactory.getLogger(BreedingLocationField.class);

	private Label captionLabel;
	private String caption;
	private ComboBox breedingLocationComboBox;
	private boolean isMandatory;
	private String DEFAULT_LOCATION = "Unknown";
	private boolean changed;
	private int leftIndentPixels = 130;
	
    private List<Location> locations;
	private CheckBox showFavoritesCheckBox;
	private Button manageFavoritesLink;

	private Window attachToWindow;
	
	private Integer locationType = 0;
	
	//flags
	private boolean displayFavoriteMethodsFilter = true;
	private boolean displayManageMethodLink = true;
	
	private BreedingLocationFieldSource source;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
    private WorkbenchDataManager workbenchDataManager;
	
	@Autowired
    private GermplasmDataManager germplasmDataManager;	
	
	@Autowired
    private LocationDataManager locationDataManager;
	
	public BreedingLocationField() {
		
	}
	
	public BreedingLocationField(BreedingLocationFieldSource source){
		this.source = source;
		this.caption = "Location: ";
		this.changed = false;
	}
	
	public BreedingLocationField(BreedingLocationFieldSource source, Window attachToWindow){
		this(source);
		this.attachToWindow = attachToWindow;
	}
	
	public BreedingLocationField(BreedingLocationFieldSource source, Window attachToWindow, int pixels){
		this(source, attachToWindow);
		this.leftIndentPixels = pixels;
	}
	
	public BreedingLocationField(BreedingLocationFieldSource source, int pixels){
		this.source = source;
		this.leftIndentPixels = pixels;
	}
	
	public BreedingLocationField(BreedingLocationFieldSource source, Window attachToWindow, int pixels, Integer locationType){
		this(source, attachToWindow);
		this.leftIndentPixels = pixels;
		this.locationType = locationType;
	}
	
	public BreedingLocationField(BreedingLocationFieldSource source, int pixels, Integer locationType){
		this.source = source;
		this.leftIndentPixels = pixels;
		this.locationType = locationType;
	}
	
	public BreedingLocationField(BreedingLocationFieldSource source, Window attachToWindow, int pixels, 
			boolean displayFavoriteMethodsFilter, boolean displayManageMethodLink, Integer locationType){
		this(source, attachToWindow);
		this.leftIndentPixels = pixels;
		this.displayFavoriteMethodsFilter = displayFavoriteMethodsFilter;
		this.displayManageMethodLink = displayManageMethodLink;
		this.locationType = locationType;
	}
	
	public BreedingLocationField(BreedingLocationFieldSource source, int pixels, boolean displayFavoriteMethodsFilter, 
			boolean displayManageMethodLink, Integer locationType){
		this.source = source;
		this.leftIndentPixels = pixels;
		this.displayFavoriteMethodsFilter = displayFavoriteMethodsFilter;
		this.displayManageMethodLink = displayManageMethodLink;
		this.locationType = locationType;
	}
	
	
	@Override
	public void instantiateComponents() {
		captionLabel = new Label(caption);
		captionLabel.addStyleName("bold");
		
		breedingLocationComboBox = new ComboBox();
		breedingLocationComboBox.setWidth("320px");
		breedingLocationComboBox.setImmediate(true);
		breedingLocationComboBox.setNullSelectionAllowed(false);
		
		if(isMandatory){
			breedingLocationComboBox.setRequired(true);
			breedingLocationComboBox.setRequiredError("Please specify the location.");
		}
		
		showFavoritesCheckBox = new CheckBox();
        showFavoritesCheckBox.setCaption(messageSource.getMessage(Message.SHOW_ONLY_FAVORITE_LOCATIONS));
        showFavoritesCheckBox.setImmediate(true);
        
        manageFavoritesLink = new Button();
        manageFavoritesLink.setStyleName(BaseTheme.BUTTON_LINK);
        manageFavoritesLink.setCaption(messageSource.getMessage(Message.MANAGE_LOCATIONS));

	}

	@Override
	public void initializeValues() {
        populateLocations();
        initPopulateFavLocations();        
	}
	
	public boolean initPopulateFavLocations(){
		boolean hasFavorite = false;
		if(BreedingManagerUtil.hasFavoriteLocation(germplasmDataManager, 0)){
        	showFavoritesCheckBox.setValue(true);
        	populateHarvestLocation(true);
        	hasFavorite = true;
        }
		return hasFavorite;
	}

	@Override
	public void addListeners() {
		
        breedingLocationComboBox.addListener(new ComboBox.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
                changed = true;
			}
        });
        
        breedingLocationComboBox.addListener(new ComboBox.ItemSetChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void containerItemSetChange(ItemSetChangeEvent event) {
                changed = true;
			}
        });
        
        showFavoritesCheckBox.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				populateHarvestLocation(((Boolean) event.getProperty().getValue()).equals(true));
			}
		});
        
        manageFavoritesLink.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				launchManageWindow();
			}
        });
        
	}

	@Override
	public void layoutComponents() {
		if(displayManageMethodLink || displayFavoriteMethodsFilter){
			setHeight("250px");
		}
		else{
			setHeight("190px");
		}
		
		addComponent(captionLabel, "top:3px; left:0;");
		addComponent(breedingLocationComboBox, "top:0; left:" + leftIndentPixels + "px");
		
		if(displayFavoriteMethodsFilter){
			addComponent(showFavoritesCheckBox, "top:30px; left:" + leftIndentPixels + "px");
		}
		
		if(displayManageMethodLink){
			int pixels = leftIndentPixels + 220;
			addComponent(manageFavoritesLink, "top:33px; left:" + pixels + "px");
		}	
	}

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
	
	public ComboBox getBreedingLocationComboBox() {
		return breedingLocationComboBox;
	}
	
	public void setbreedingLocationComboBox(ComboBox breedingLocationComboBox) {
		this.breedingLocationComboBox = breedingLocationComboBox;
	}
	
	public void setValue(String value){
		breedingLocationComboBox.select(value);
	}
	
	public String getValue(){
		return (String)breedingLocationComboBox.getValue();
	}
	
	public void validate() throws InvalidValueException {
		breedingLocationComboBox.validate();
	}
	
	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	public void populateHarvestLocation(Integer selectedLocation){
		populateHarvestLocation(showFavoritesCheckBox.getValue().equals(true));
		breedingLocationComboBox.setValue(selectedLocation);
	}
	
    private void populateHarvestLocation(boolean showOnlyFavorites) {
    	breedingLocationComboBox.removeAllItems();

        if(showOnlyFavorites){
        	try {
        		BreedingManagerUtil.populateWithFavoriteLocations(workbenchDataManager, 
						germplasmDataManager, breedingLocationComboBox, null);
        		
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
		
		try {
			
			if(locationType > 0){
				locations = locationDataManager.getLocationsByType(locationType);
			}
			else{
				locations = locationDataManager.getAllLocations();
			}
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			LOG.error("Error on getting all locations", e);
		}
		
		Integer firstId = null;
		boolean hasDefault = false;
		for(Location location : locations){
		   if(firstId == null){
		       firstId = location.getLocid();
		   }
		   breedingLocationComboBox.addItem(location.getLocid());
		   breedingLocationComboBox.setItemCaption(location.getLocid(), BreedingManagerUtil.getLocationNameDisplay(location));
		   if(DEFAULT_LOCATION.equalsIgnoreCase(location.getLname())){
		       breedingLocationComboBox.setValue(location.getLocid());
		       hasDefault = true;
		   }
         }
		if(hasDefault == false && firstId != null){
		    breedingLocationComboBox.setValue(firstId);
		}
	}
    
    private void launchManageWindow(){
		try {
			Integer wbUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
            Project project = workbenchDataManager.getLastOpenedProject(wbUserId);
            Window window = attachToWindow != null ? attachToWindow : getWindow();
			Window manageFavoriteLocationsWindow = Util.launchLocationManager(workbenchDataManager, project.getProjectId(), window, messageSource.getMessage(Message.MANAGE_LOCATIONS));
			manageFavoriteLocationsWindow.addListener(new CloseListener(){
				private static final long serialVersionUID = 1L;
				@Override
				public void windowClose(CloseEvent e) {
					source.updateAllLocationFields();
				}
			});
		} catch (MiddlewareQueryException e){
			LOG.error("Error on manageFavoriteLocations click", e);
		}
    }
    
    public void setCaption(String caption){
    	this.caption = caption;
    	if (this.captionLabel != null){
    		this.captionLabel.setValue(this.caption);
    	}
    }
    
    protected int getLeftIndentPixels(){
    	return leftIndentPixels;
    }

	public SimpleResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public WorkbenchDataManager getWorkbenchDataManager() {
		return workbenchDataManager;
	}

	public void setWorkbenchDataManager(WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	public GermplasmDataManager getGermplasmDataManager() {
		return germplasmDataManager;
	}

	public void setGermplasmDataManager(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	public LocationDataManager getLocationDataManager() {
		return locationDataManager;
	}

	public void setLocationDataManager(LocationDataManager locationDataManager) {
		this.locationDataManager = locationDataManager;
	}
        
}
