package org.generationcp.breeding.manager.listimport;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants.CssStyles;
import org.generationcp.breeding.manager.customfields.BreedingLocationField;
import org.generationcp.breeding.manager.customfields.BreedingMethodField;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

@Configurable
public class GermplasmFieldsComponent extends AbsoluteLayout implements
		InternationalizableComponent, InitializingBean, BreedingManagerLayout {

	private static final long serialVersionUID = -1180999883774074687L;
	
	private final static Logger LOG = LoggerFactory.getLogger(GermplasmFieldsComponent.class);
	 
	private  static final String DEFAULT_NAME_TYPE = "Line Name";
	
	private Label addGermplasmDetailsLabel;
    private Label addGermplasmDetailsMessage;
    
    private BreedingMethodField methodComponent;
    private BreedingLocationField locationComponent;
    
    private Label germplasmDateLabel;
    private Label nameTypeLabel;
  
    private ComboBox nameTypeComboBox;
    
    private DateField germplasmDateField;
    
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
    
    private Window parentWindow;
    
    private int leftIndentPixels = 130;
    

	public GermplasmFieldsComponent(Window parentWindow) {
		super();
		this.parentWindow = parentWindow;
	}
	
	public GermplasmFieldsComponent(int pixels) {
		super();
		this.leftIndentPixels = pixels;
	}

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
        messageSource.setValue(germplasmDateLabel, Message.GERMPLASM_DATE_LABEL);
        messageSource.setValue(nameTypeLabel, Message.GERMPLASM_NAME_TYPE_LABEL);
	}

	@Override
	public void instantiateComponents() {
		addGermplasmDetailsLabel = new Label(messageSource.getMessage(Message.ADD_GERMPLASM_DETAILS).toUpperCase());
		addGermplasmDetailsLabel.addStyleName(Bootstrap.Typography.H4.styleName());
		
		addGermplasmDetailsMessage = new Label();
		addGermplasmDetailsMessage.setValue("You can specify following details to apply to the imported germplasm. These details are optional.");
		
		if (parentWindow != null){
			methodComponent = new BreedingMethodField(parentWindow, 200);
		} else {
			methodComponent = new BreedingMethodField(200);
		}
		methodComponent.setCaption(messageSource.getMessage(Message.GERMPLASM_BREEDING_METHOD_LABEL));
		
		if (parentWindow != null){
			locationComponent = new BreedingLocationField(parentWindow, 200);
		} else {
			locationComponent = new BreedingLocationField(200);
		}
		locationComponent.setCaption(messageSource.getMessage(Message.GERMPLASM_LOCATION_LABEL));
		
        germplasmDateLabel = new Label();
        germplasmDateLabel.addStyleName(CssStyles.BOLD);
        germplasmDateField =  new DateField();
        germplasmDateField.setResolution(DateField.RESOLUTION_DAY);
        germplasmDateField.setDateFormat(GermplasmImportMain.DATE_FORMAT);
        
        nameTypeLabel = new Label();
        nameTypeLabel.addStyleName(CssStyles.BOLD);
        nameTypeComboBox = new ComboBox();
        nameTypeComboBox.setWidth("400px");
        nameTypeComboBox.setNullSelectionAllowed(false);
        nameTypeComboBox.setImmediate(true);
	}

	@Override
	public void initializeValues() {
		germplasmDateField.setValue(new Date());
		
        try {
			populateNameTypes();
		} catch (MiddlewareQueryException e) {
			LOG.error("Error getting name types " + e.getMessage());
			e.printStackTrace();
		}
		
	}

	@Override
	public void addListeners() {
	}

	@Override
	public void layoutComponents() {
		setHeight("270px");
		
		addComponent(addGermplasmDetailsLabel, "top:0px;left:0px");
		addComponent(addGermplasmDetailsMessage, "top:32px;left:0px");
		
		addComponent(methodComponent, "top:60px;left:0px");
		
		addComponent(locationComponent, "top:120px;left:0px");
		
		addComponent(germplasmDateLabel, "top:185px;left:0px");
		addComponent(germplasmDateField, "top:180px;left:" + getLeftIndentPixels() + "px");
		
		addComponent(nameTypeLabel, "top:220px;left:0px");
        addComponent(nameTypeComboBox, "top:215px;left:" + getLeftIndentPixels() + "px");
		
	}
	
	public ComboBox getBreedingMethodComboBox() {
		return methodComponent.getBreedingMethodComboBox();
	}

	public ComboBox getLocationComboBox() {
		return locationComponent.getBreedingLocationComboBox();
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
	
	
	public void setGermplasmBreedingMethod(String breedingMethod){
        getBreedingMethodComboBox().setNullSelectionAllowed(false);
        getBreedingMethodComboBox().addItem(breedingMethod);
        getBreedingMethodComboBox().setValue(breedingMethod);
    }
	
	public void setGermplasmDate(Date germplasmDate) throws ReadOnlyException, ConversionException, ParseException{
        germplasmDateField.setValue(germplasmDate);
	}
	
    public void setGermplasmLocation(String germplasmLocation){
        getLocationComboBox().setNullSelectionAllowed(false);
        getLocationComboBox().addItem(germplasmLocation);
        getLocationComboBox().setValue(germplasmLocation);
    }
    
    public void setGermplasmListType(String germplasmListType){
        nameTypeComboBox.setNullSelectionAllowed(false);
        nameTypeComboBox.addItem(germplasmListType);
        nameTypeComboBox.setValue(germplasmListType);
    }
    
    protected int getLeftIndentPixels(){
    	return leftIndentPixels;
    }
    
}
