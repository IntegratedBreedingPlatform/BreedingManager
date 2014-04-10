package org.generationcp.breeding.manager.customfields;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.generationcp.middleware.pojos.Method;
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
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class BreedingMethodField extends AbsoluteLayout
implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 4506866031376540836L;
	private final static Logger LOG = LoggerFactory.getLogger(BreedingMethodField.class);

	private Label captionLabel;
	private String caption;
	private ComboBox breedingMethodComboBox;
	private boolean isMandatory;
	private static String DEFAULT_METHOD = "UDM";
	private boolean changed;
	
	private Map<String, String> methodMap;
	private List<Method> methods;
	private CheckBox showFavoritesCheckBox;
	private Button manageFavoritesLink;

	private Window attachToWindow;
	
	private Label methodDescription;
	private PopupView popup;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
    private WorkbenchDataManager workbenchDataManager;
	
	@Autowired
    private GermplasmDataManager germplasmDataManager;	
	
	public BreedingMethodField(){
		this.caption = "Breeding Method: ";
		this.changed = false;
		this.attachToWindow = getWindow();
	}
	
	public BreedingMethodField(Window attachToWindow){
		this.caption = "Breeding Method: ";
		this.changed = false;
		this.attachToWindow = attachToWindow;
	}
	
	@Override
	public void instantiateComponents() {
		
		setWidth("500px");
		setHeight("250px");
		
		captionLabel = new Label(caption);
		captionLabel.addStyleName("bold");
		
		breedingMethodComboBox = new ComboBox();
		breedingMethodComboBox.setWidth("320px");
		breedingMethodComboBox.setImmediate(true);
		breedingMethodComboBox.setNullSelectionAllowed(false);
		
		if(isMandatory){
			breedingMethodComboBox.setRequired(true);
			breedingMethodComboBox.setRequiredError("Please specify the method.");
		}
		
		showFavoritesCheckBox = new CheckBox();
        showFavoritesCheckBox.setCaption(messageSource.getMessage(Message.SHOW_ONLY_FAVORITE_METHODS));
        showFavoritesCheckBox.setImmediate(true);
        
        manageFavoritesLink = new Button();
        manageFavoritesLink.setStyleName(BaseTheme.BUTTON_LINK);
        manageFavoritesLink.setCaption(messageSource.getMessage(Message.MANAGE_METHODS));

        methodDescription = new Label();
        methodDescription.setWidth("300px");
        popup = new PopupView(" ? ", methodDescription);
        popup.setStyleName("gcp-popup-view");
	}

	@Override
	public void initializeValues() {
        populateMethods();
	}

	@Override
	public void addListeners() {
		
        breedingMethodComboBox.addListener(new ComboBox.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				updateComboBoxDescription();
                changed = true;
			}
        });
        
        breedingMethodComboBox.addListener(new ComboBox.ItemSetChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void containerItemSetChange(ItemSetChangeEvent event) {
				updateComboBoxDescription();
                changed = true;
			}
        });
        
        showFavoritesCheckBox.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				populateMethods(((Boolean) event.getProperty().getValue()).equals(true));
				updateComboBoxDescription();
			}
		});
        
        showFavoritesCheckBox.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				populateMethods(((Boolean) event.getProperty().getValue()).equals(true));
				updateComboBoxDescription();
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
		addComponent(breedingMethodComboBox, "top:0; left:130px;");
		addComponent(popup, "top:0; left:455px;");
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
	
	public ComboBox getBreedingMethodComboBox() {
		return breedingMethodComboBox;
	}
	
	public void setBreedingMethodComboBox(ComboBox breedingMethodComboBox) {
		this.breedingMethodComboBox = breedingMethodComboBox;
	}
	
	public void setValue(String value){
		breedingMethodComboBox.select(value);
	}
	
	public Object getValue(){
		return breedingMethodComboBox.getValue();
	}
	
	public void validate() throws InvalidValueException {
		breedingMethodComboBox.validate();
	}
	
	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
    private void updateComboBoxDescription(){
    	Object breedingMethodComboBoxValue = breedingMethodComboBox.getValue();
    	breedingMethodComboBox.setDescription("");
    	if(breedingMethodComboBoxValue!=null){
    		//breedingMethodComboBox.setDescription(methodMap.get(breedingMethodComboBoxValue.toString()));
    		methodDescription.setValue(methodMap.get(breedingMethodComboBoxValue.toString()));
    	}
    }
    
	private Map<String, String> populateMethods() {
		
		if(methods==null){
			try {
				methods = germplasmDataManager.getAllMethods();
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
				LOG.error("Error on gettingAllMethods", e);
			}
		}
		
		methodMap = new HashMap<String, String>();
        for(Method method : methods){
            breedingMethodComboBox.addItem(method.getMid());
            breedingMethodComboBox.setItemCaption(method.getMid(), method.getMname());
            if(DEFAULT_METHOD.equalsIgnoreCase(method.getMcode())){
                breedingMethodComboBox.setValue(method.getMid());
                //breedingMethodComboBox.setDescription(method.getMdesc());
                methodDescription.setValue(method.getMdesc());
            }
            methodMap.put(method.getMid().toString(), method.getMdesc());
        }
        
        if(breedingMethodComboBox.getValue()==null && methods.get(0) != null){
        	breedingMethodComboBox.setValue(methods.get(0).getMid());
        	breedingMethodComboBox.setDescription(methods.get(0).getMdesc());
        }
		return methodMap;
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
    
    private void launchManageWindow(){
		try {
			Integer wbUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
            Project project = workbenchDataManager.getLastOpenedProject(wbUserId);
			Window manageFavoriteMethodsWindow = Util.launchMethodManager(workbenchDataManager, project.getProjectId(), attachToWindow, messageSource.getMessage(Message.MANAGE_METHODS));
			manageFavoriteMethodsWindow.addListener(new CloseListener(){
				private static final long serialVersionUID = 1L;
				@Override
				public void windowClose(CloseEvent e) {
					Object lastValue = breedingMethodComboBox.getValue();
					populateMethods(((Boolean) showFavoritesCheckBox.getValue()).equals(true));
					breedingMethodComboBox.setValue(lastValue);
				}
			});
		} catch (MiddlewareQueryException e){
			LOG.error("Error on manageFavoriteMethods click", e);
		}
    }
}
