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
	
    private List<Location> locations;
	private CheckBox showFavoritesCheckBox;
	private Button manageFavoritesLink;

	private Window attachToWindow;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
    private WorkbenchDataManager workbenchDataManager;
	
	@Autowired
    private GermplasmDataManager germplasmDataManager;	
	
	public BreedingLocationField(){
		this.caption = "Location: ";
		this.changed = false;
		this.attachToWindow = getWindow();
	}
	
	public BreedingLocationField(Window attachToWindow){
		this.caption = "Location: ";
		this.changed = false;
		this.attachToWindow = attachToWindow;
	}
	
	@Override
	public void instantiateComponents() {
		
		setWidth("500px");
		setHeight("250px");
		
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
		addComponent(captionLabel, "top:3px; left:0;");
		addComponent(breedingLocationComboBox, "top:0; left:130px;");
		addComponent(showFavoritesCheckBox, "top:25px; left:130px;");
		addComponent(manageFavoritesLink, "top:28px; left:350px;");
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
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
	
	public Object getValue(){
		return breedingLocationComboBox.getValue();
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
		
		if(locations==null){
			try {
				locations = germplasmDataManager.getAllBreedingLocations();
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
				LOG.error("Error on getting all locations", e);
			}
		}
		
		Integer firstId = null;
		boolean hasDefault = false;
		for(Location location : locations){
		   //method.getMcode()
		   if(firstId == null){
		       firstId = location.getLocid();
		   }
		   breedingLocationComboBox.addItem(location.getLocid());
		   breedingLocationComboBox.setItemCaption(location.getLocid(), location.getLname());
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
			Window manageFavoriteLocationsWindow = Util.launchLocationManager(workbenchDataManager, project.getProjectId(), attachToWindow, messageSource.getMessage(Message.MANAGE_LOCATIONS));
			manageFavoriteLocationsWindow.addListener(new CloseListener(){
				private static final long serialVersionUID = 1L;
				@Override
				public void windowClose(CloseEvent e) {
					Object lastValue = breedingLocationComboBox.getValue();
					populateHarvestLocation(((Boolean) showFavoritesCheckBox.getValue()).equals(true));
					breedingLocationComboBox.setValue(lastValue);
				}
			});
		} catch (MiddlewareQueryException e){
			LOG.error("Error on manageFavoriteLocations click", e);
		}
    }
}
