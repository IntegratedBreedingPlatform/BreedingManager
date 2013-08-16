package org.generationcp.breeding.manager.listimport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.breeding.manager.listimport.listeners.MethodValueChangeListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;

@Configurable
public class SpecifyGermplasmDetailsComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent{

    private static final long serialVersionUID = 2762965368037453497L;
    private final static Logger LOG = LoggerFactory.getLogger(SpecifyGermplasmDetailsComponent.class);
    
    private GermplasmImportMain source;
    
    public static final String NEXT_BUTTON_ID = "next button";
    public static final String BACK_BUTTON_ID = "back button";
    
    private Label breedingMethodLabel;
    private Label germplasmDateLabel;
    private Label locationLabel;
    private Label nameTypeLabel;
    private Label germplasmDetailsLabel;
    private Label pedigreeOptionsLabel;
    
    private ComboBox breedingMethodComboBox;
    private ComboBox locationComboBox;
    private ComboBox nameTypeComboBox;
    
    private DateField germplasmDateField;
    
    private Table germplasmDetailsTable;
    
    private OptionGroup pedigreeOptionGroup;
    
    private Button backButton;
    private Button nextButton;
    
    private Accordion accordion;
    private Component nextScreen;
    private Component previousScreen;

    private String DEFAULT_METHOD = "UDM";
    private String DEFAULT_LOCATION = "Unknown";

    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    public SpecifyGermplasmDetailsComponent(GermplasmImportMain source, Accordion accordion){
        this.source = source;
        this.accordion = accordion;
    }
    
    public void setNextScreen(Component nextScreen){
        this.nextScreen = nextScreen;
    }
    
    public void setPreviousScreen(Component previousScreen){
        this.previousScreen = previousScreen;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("510px");
        setWidth("800px");
        
        breedingMethodLabel = new Label();
        addComponent(breedingMethodLabel, "top:30px;left:20px");
        
        breedingMethodComboBox = new ComboBox();
        breedingMethodComboBox.setWidth("400px");
        breedingMethodComboBox.setNullSelectionAllowed(false);
        List<Method> methodList = germplasmDataManager.getAllMethods();
        Map methodMap = new HashMap();
        for(Method method : methodList){
            //method.getMcode()
            breedingMethodComboBox.addItem(method.getMid());
            breedingMethodComboBox.setItemCaption(method.getMid(), method.getMname());
            if(DEFAULT_METHOD.equalsIgnoreCase(method.getMcode())){
                breedingMethodComboBox.setValue(method.getMid());
                breedingMethodComboBox.setDescription(method.getMdesc());
            }
            methodMap.put(method.getMid().toString(), method.getMdesc());
        }
        breedingMethodComboBox.setTextInputAllowed(false);
        breedingMethodComboBox.setImmediate(true);
        breedingMethodComboBox.addListener(new MethodValueChangeListener(breedingMethodComboBox, methodMap));
        addComponent(breedingMethodComboBox, "top:10px;left:200px");
        
        germplasmDateLabel = new Label();
        addComponent(germplasmDateLabel, "top:60px;left:20px");
        
        germplasmDateField =  new DateField();
        germplasmDateField.setResolution(DateField.RESOLUTION_DAY);
        germplasmDateField.setDateFormat("yyyy-MM-dd");
        germplasmDateField.setValue(new Date());
        addComponent(germplasmDateField, "top:40px;left:200px");
        
        locationLabel = new Label();
        addComponent(locationLabel, "top:90px;left:20px");
        
        locationComboBox = new ComboBox();
        locationComboBox.setWidth("400px");
        List<Location> locationList = germplasmDataManager.getAllBreedingLocations();
        Map locationMap = new HashMap();
       for(Location location : locationList){
           //method.getMcode()
           locationComboBox.addItem(location.getLocid());
           locationComboBox.setItemCaption(location.getLocid(), location.getLname());
           if(DEFAULT_LOCATION.equalsIgnoreCase(location.getLname())){
               locationComboBox.setValue(location.getLocid());
               //locationComboBox.setDescription(location.get);
           }
           locationMap.put(location.getLocid(), location.getLname());
       }
        locationComboBox.setTextInputAllowed(false);
        locationComboBox.setImmediate(true);
        //locationComboBox.addListener(new MethodValueChangeListener(locationComboBox, locationMap));

        addComponent(locationComboBox, "top:70px;left:200px");
        
        nameTypeLabel = new Label();
        addComponent(nameTypeLabel, "top:120px;left:20px");
        
        nameTypeComboBox = new ComboBox();
        nameTypeComboBox.setWidth("400px");
        addComponent(nameTypeComboBox, "top:100px;left:200px");
        
        germplasmDetailsLabel = new Label();
        addComponent(germplasmDetailsLabel, "top:150px;left:20px");
        
        germplasmDetailsTable = new Table();
        germplasmDetailsTable.addContainerProperty(1, Integer.class, null);
        germplasmDetailsTable.addContainerProperty(2, String.class, null);
        germplasmDetailsTable.addContainerProperty(3, String.class, null);
        germplasmDetailsTable.addContainerProperty(4, String.class, null);
        germplasmDetailsTable.addContainerProperty(5, String.class, null);
        germplasmDetailsTable.setColumnHeaders(new String[]{"Entry ID", "Entry CD", "Designation", "Cross", "Source"});
        germplasmDetailsTable.setHeight("200px");
        germplasmDetailsTable.setWidth("700px");
        addComponent(germplasmDetailsTable, "top:160px;left:20px");
        
        pedigreeOptionsLabel = new Label();
        addComponent(pedigreeOptionsLabel, "top:380px;left:20px");
        
        pedigreeOptionGroup = new OptionGroup();
        pedigreeOptionGroup.addItem(1);
        pedigreeOptionGroup.addItem(2);
        pedigreeOptionGroup.addItem(3);
        pedigreeOptionGroup.setItemCaption(1, messageSource.getMessage(Message.IMPORT_PEDIGREE_OPTION_ONE));
        pedigreeOptionGroup.setItemCaption(2, messageSource.getMessage(Message.IMPORT_PEDIGREE_OPTION_TWO));
        pedigreeOptionGroup.setItemCaption(3, messageSource.getMessage(Message.IMPORT_PEDIGREE_OPTION_THREE));
        pedigreeOptionGroup.select(1);
        pedigreeOptionGroup.setNullSelectionAllowed(false);
        addComponent(pedigreeOptionGroup, "top:390px;left:20px");
        
        GermplasmImportButtonClickListener clickListener = new GermplasmImportButtonClickListener(this);
        
        backButton = new Button();
        backButton.setData(BACK_BUTTON_ID);
        backButton.addListener(clickListener);
        addComponent(backButton, "top:450px;left:600px");
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(clickListener);
        addComponent(nextButton, "top:450px;left:670px");
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(breedingMethodLabel, Message.GERMPLASM_BREEDING_METHOD_LABEL);
        messageSource.setCaption(germplasmDateLabel, Message.GERMPLASM_DATE_LABEL);
        messageSource.setCaption(locationLabel, Message.GERMPLASM_LOCATION_LABEL);
        messageSource.setCaption(nameTypeLabel, Message.GERMPLASM_NAME_TYPE_LABEL);
        messageSource.setCaption(germplasmDetailsLabel, Message.GERMPLASM_DETAILS_LABEL);
        messageSource.setCaption(pedigreeOptionsLabel, Message.PEDIGREE_OPTIONS_LABEL);
        messageSource.setCaption(backButton, Message.BACK);
        messageSource.setCaption(nextButton, Message.NEXT);
    }
    
    public void nextButtonClickAction(){
        if(this.nextScreen != null){
            this.accordion.setSelectedTab(this.nextScreen);
        } else {
            this.nextButton.setEnabled(false);
        }
    }
    
    public void backButtonClickAction(){
        if(this.previousScreen != null){
            this.accordion.setSelectedTab(previousScreen);
        } else{
            this.backButton.setEnabled(false);
        }
    }
    
    public GermplasmImportMain getSource() {
        return source;
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
    public void setGermplasmListDataTable(){
    }
}
