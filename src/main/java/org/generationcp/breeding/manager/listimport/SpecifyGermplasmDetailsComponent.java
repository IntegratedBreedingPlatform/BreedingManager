package org.generationcp.breeding.manager.listimport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.breeding.manager.listimport.util.GermplasmListUploader;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmDataManagerUtil;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
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
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

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
    
    private ComboBox breedingMethodComboBox;
    private ComboBox locationComboBox;
    private ComboBox nameTypeComboBox;
    
    private DateField germplasmDateField;
    
    private Table germplasmDetailsTable;
    
    private ComboBox pedigreeOptionComboBox;
    
    private Button backButton;
    private Button nextButton;
    
    private String DEFAULT_METHOD = "UDM";
    private String DEFAULT_LOCATION = "Unknown";
    private String DEFAULT_NAME_TYPE = "Line Name";
    private List<ImportedGermplasm> importedGermplasms;
    private GermplasmListUploader germplasmListUploader;

    private List<Integer> doNotCreateGermplasmsWithId = new ArrayList<Integer>();
    
    private List<SelectGermplasmWindow> selectGermplasmWindows = new ArrayList<SelectGermplasmWindow>();
    private List<GermplasmName> germplasmNameObjects;
    
    private List<Location> locations;
    private List<Method> methods;
    
    private CheckBox showFavoriteLocationsCheckBox;
    private CheckBox showFavoriteMethodsCheckBox;
    
    private Button manageFavoriteMethodsLink;
    private Button manageFavoriteLocationsLink;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    @Autowired
    private GermplasmDataManager germplasmDataManager;

    @Autowired
     private GermplasmListManager germplasmListManager;
    @Autowired
     private WorkbenchDataManager workbenchDataManager;
    @Autowired
     private LocationDataManager locationDataManager;
    
    private Boolean viaToolURL;
    private Map<String, String> methodMap;
    
    public SpecifyGermplasmDetailsComponent(GermplasmImportMain source, Boolean viaToolURL){
        this.source = source;
        this.viaToolURL = viaToolURL;
    }

    public Table getGermplasmDetailsTable(){
        return germplasmDetailsTable;
    }
    
    public List<ImportedGermplasm> getImportedGermplasms() {
        return importedGermplasms;
    }

    public void setImportedGermplasms(List<ImportedGermplasm> importedGermplasms) {
        this.importedGermplasms = importedGermplasms;
    }

    public GermplasmListUploader getGermplasmListUploader() {
        return germplasmListUploader;
    }

    public void setGermplasmListUploader(GermplasmListUploader germplasmListUploader) {
        this.germplasmListUploader = germplasmListUploader;
    }

    public void displaySelectGermplasmWindow(String germplasmName, int i, Germplasm germplasm){
        SelectGermplasmWindow selectGermplasmWindow = new SelectGermplasmWindow(this, germplasmName, i, germplasm, viaToolURL);
        selectGermplasmWindow.addStyleName(Reindeer.WINDOW_LIGHT);
        selectGermplasmWindows.add(selectGermplasmWindow);
        this.getWindow().addWindow(selectGermplasmWindow);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("510px");
        setWidth("800px");
        
        breedingMethodLabel = new Label();
        addComponent(breedingMethodLabel, "top:30px;left:20px");
        
        breedingMethodComboBox = new ComboBox();
        breedingMethodComboBox.setWidth("320px");
        breedingMethodComboBox.setNullSelectionAllowed(false);
        breedingMethodComboBox.addListener(new ComboBox.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				updateComboBoxDescription();
			}
        });
        breedingMethodComboBox.addListener(new ComboBox.ItemSetChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void containerItemSetChange(ItemSetChangeEvent event) {
				updateComboBoxDescription();
			}
        });
        
        methods = germplasmDataManager.getAllMethods();
        populateMethods();
        
        breedingMethodComboBox.setImmediate(true);
        addComponent(breedingMethodComboBox, "top:10px;left:220px");
        
        showFavoriteMethodsCheckBox = new CheckBox();
        showFavoriteMethodsCheckBox.setCaption(messageSource.getMessage(Message.SHOW_ONLY_FAVORITE_METHODS));
        showFavoriteMethodsCheckBox.setImmediate(true);
        showFavoriteMethodsCheckBox.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				populateMethods(((Boolean) event.getProperty().getValue()).equals(true));
				updateComboBoxDescription();
			}
			
		});
        addComponent(showFavoriteMethodsCheckBox, "top:13px;left:547px");
        
        manageFavoriteMethodsLink = new Button();
        manageFavoriteMethodsLink.setStyleName(BaseTheme.BUTTON_LINK);
        manageFavoriteMethodsLink.setCaption(messageSource.getMessage(Message.MANAGE_METHODS));
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
        addComponent(manageFavoriteMethodsLink, "top:31px;left:566px");
        
        germplasmDateLabel = new Label();
        addComponent(germplasmDateLabel, "top:60px;left:20px");
        
        germplasmDateField =  new DateField();
        germplasmDateField.setResolution(DateField.RESOLUTION_DAY);
        germplasmDateField.setDateFormat(GermplasmImportMain.DATE_FORMAT);
        germplasmDateField.setValue(new Date());
        addComponent(germplasmDateField, "top:40px;left:220px");
        
        locationLabel = new Label();
        addComponent(locationLabel, "top:90px;left:20px");
        
        locationComboBox = new ComboBox();
        locationComboBox.setWidth("300px");
        locationComboBox.setNullSelectionAllowed(false);
        locations = locationDataManager.getAllLocations();
        populateHarvestLocation(false);
        locationComboBox.setImmediate(true);
        
        addComponent(locationComboBox, "top:70px;left:220px");
        
        
        showFavoriteLocationsCheckBox = new CheckBox();
        showFavoriteLocationsCheckBox.setCaption(messageSource.getMessage(Message.SHOW_ONLY_FAVORITE_LOCATIONS));
        showFavoriteLocationsCheckBox.setImmediate(true);
        showFavoriteLocationsCheckBox.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				populateHarvestLocation(((Boolean) event.getProperty().getValue()).equals(true));
			}
			
		});
        addComponent(showFavoriteLocationsCheckBox, "top:72px;left:527px");
        
        manageFavoriteLocationsLink = new Button();
        manageFavoriteLocationsLink.setStyleName(BaseTheme.BUTTON_LINK);
        manageFavoriteLocationsLink.setCaption(messageSource.getMessage(Message.MANAGE_LOCATIONS));
        manageFavoriteLocationsLink.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;
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
        addComponent(manageFavoriteLocationsLink, "top:90px;left:547px");
        
        nameTypeLabel = new Label();
        addComponent(nameTypeLabel, "top:140px;left:20px");
        
        nameTypeComboBox = new ComboBox();
        nameTypeComboBox.setWidth("400px");
        nameTypeComboBox.setNullSelectionAllowed(false);
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

        nameTypeComboBox.setImmediate(true);

        addComponent(nameTypeComboBox, "top:120px;left:220px");
        
        germplasmDetailsLabel = new Label();
        addComponent(germplasmDetailsLabel, "top:170px;left:20px");
        
        germplasmDetailsTable = new Table();
        germplasmDetailsTable.addContainerProperty(1, Integer.class, null);
        germplasmDetailsTable.addContainerProperty(2, String.class, null);
        germplasmDetailsTable.addContainerProperty(3, Integer.class, null);
        germplasmDetailsTable.addContainerProperty(4, String.class, null);
        germplasmDetailsTable.addContainerProperty(5, String.class, null);
        germplasmDetailsTable.addContainerProperty(6, String.class, null);
        germplasmDetailsTable.setColumnHeaders(new String[]{"Entry ID", "Entry Code","GID","Designation", "Cross", "Source"});
        germplasmDetailsTable.setHeight("200px");
        germplasmDetailsTable.setWidth("700px");
        addComponent(germplasmDetailsTable, "top:175px;left:20px");
        
        pedigreeOptionComboBox = new ComboBox();
        pedigreeOptionComboBox.setRequired(true);
        pedigreeOptionComboBox.setWidth("410px");
        pedigreeOptionComboBox.addItem(1);
        pedigreeOptionComboBox.addItem(2);
        pedigreeOptionComboBox.addItem(3);
        pedigreeOptionComboBox.setItemCaption(1, messageSource.getMessage(Message.IMPORT_PEDIGREE_OPTION_ONE));
        pedigreeOptionComboBox.setItemCaption(2, messageSource.getMessage(Message.IMPORT_PEDIGREE_OPTION_TWO));
        pedigreeOptionComboBox.setItemCaption(3, messageSource.getMessage(Message.IMPORT_PEDIGREE_OPTION_THREE));
        pedigreeOptionComboBox.setNullSelectionAllowed(true);
        addComponent(pedigreeOptionComboBox, "top:410px;left:20px");
        
        GermplasmImportButtonClickListener clickListener = new GermplasmImportButtonClickListener(this);
        
        backButton = new Button();
        backButton.setData(BACK_BUTTON_ID);
        backButton.addListener(clickListener);
        addComponent(backButton, "top:450px;left:600px");
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(clickListener);
        nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        addComponent(nextButton, "top:450px;left:670px");
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
        messageSource.setCaption(pedigreeOptionComboBox, Message.PEDIGREE_OPTIONS_LABEL);
        messageSource.setCaption(backButton, Message.BACK);
        messageSource.setCaption(nextButton, Message.NEXT);
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
        
    public void nextButtonClickAction(){
        if (validateMethod() && validateLocation() && validatePedigreeOption()) {
            	germplasmNameObjects = new ArrayList<GermplasmName>();
            	
                //germplasmList = new ArrayList<Germplasm>(); 
                //nameList = new ArrayList<Name>();
                doNotCreateGermplasmsWithId = new ArrayList<Integer>();
                
                if(pedigreeOptionComboBox.getValue().toString().equalsIgnoreCase("1") && getImportedGermplasms() != null){
                    //meaning 1st pedigree
                    //we should create the germplasm and named pojos here
                    try{
                        WorkbenchRuntimeData data = workbenchDataManager.getWorkbenchRuntimeData();
    
                        Integer wbUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
                        Project project = workbenchDataManager.getLastOpenedProject(wbUserId);
                        Integer ibdbUserId = workbenchDataManager.getLocalIbdbUserId(wbUserId, project.getProjectId());
    
                        SimpleDateFormat formatter = new SimpleDateFormat(GermplasmImportMain.DATE_FORMAT);
                        String sDate = formatter.format(germplasmDateField.getValue());
    
                        Integer dateIntValue = Integer.parseInt(sDate.replace("-", ""));
                        
                        Map<String, Germplasm> createdGermplasms = new HashMap<String, Germplasm>();
                        
                        for(int i = 0 ; i < getImportedGermplasms().size(); i++){
                            ImportedGermplasm importedGermplasm  = getImportedGermplasms().get(i);
                            Germplasm germplasm = new Germplasm();
                            germplasm.setGid(i);
                            germplasm.setUserId(ibdbUserId);
                            germplasm.setLocationId((Integer)locationComboBox.getValue());
                            germplasm.setGdate(dateIntValue);
                            germplasm.setMethodId((Integer)breedingMethodComboBox.getValue());
    
                            germplasm.setGnpgs(-1);
                            germplasm.setGpid1(0);
                            germplasm.setGpid2(0);
                            germplasm.setLgid(0);
                            germplasm.setGrplce(0);
                            germplasm.setReferenceId(0);
                            germplasm.setMgid(0);
    
                            Name name = new Name();
                            //name.setNid();
                            //name.setGermplasmId();
                            name.setTypeId((Integer)nameTypeComboBox.getValue());
                            name.setUserId(ibdbUserId);
                            name.setNval(importedGermplasm.getDesig());
                            name.setLocationId((Integer)locationComboBox.getValue());
                            name.setNdate(dateIntValue);
                            name.setReferenceId(0);
                            
                            if(!createdGermplasms.containsKey(name.getNval())){
                            	//germplasmList.add(germplasm);
                            	//nameList.add(name);
                            	createdGermplasms.put(name.getNval(), germplasm);
                            	
                            	germplasmNameObjects.add(new GermplasmName(germplasm,name));
                            } else {
                            	//germplasmList.add(createdGermplasms.get(name.getNval()));
                            	//nameList.add(name);
                            	
                            	germplasmNameObjects.add(new GermplasmName(createdGermplasms.get(name.getNval()),name));
                            }
                            
                            
                        }
                        //logFirstPedigreeUploadedToWorkbenchProjectActivity();
    
                    }catch (MiddlewareQueryException mqe){
                        mqe.printStackTrace();
                    }
                    
                } else if(pedigreeOptionComboBox.getValue().toString().equalsIgnoreCase("2") && getImportedGermplasms() != null){
                    //meaning 2nd pedigree
                    try{
                        WorkbenchRuntimeData data = workbenchDataManager.getWorkbenchRuntimeData();
                        
                        Integer wbUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
                        Project project = workbenchDataManager.getLastOpenedProject(wbUserId);
                        Integer ibdbUserId = workbenchDataManager.getLocalIbdbUserId(wbUserId, project.getProjectId());                    
                        
                        SimpleDateFormat formatter = new SimpleDateFormat(GermplasmImportMain.DATE_FORMAT);
                        String sDate = formatter.format(germplasmDateField.getValue());
    
                        Integer dateIntValue = Integer.parseInt(sDate.replace("-", ""));
                        
                        Map<String, Germplasm> createdGermplasms = new HashMap<String, Germplasm>();
                        
                        for(int i = 0 ; i < getImportedGermplasms().size(); i++){
                            
                            ImportedGermplasm importedGermplasm  = getImportedGermplasms().get(i);
                            int germplasmMatchesCount = (int) this.germplasmDataManager.countGermplasmByName(importedGermplasm.getDesig(), Operation.EQUAL);
                            
                            Germplasm germplasm = new Germplasm();
                            
                            if(importedGermplasm.getGid()!=null){
                            	germplasm = germplasmDataManager.getGermplasmByGID(importedGermplasm.getGid()); 
                            } else {
    	                        germplasm.setGid(i);
    	                        germplasm.setUserId(ibdbUserId);
    	                        germplasm.setLocationId((Integer)locationComboBox.getValue());
    	                        germplasm.setGdate(dateIntValue);
    	                        germplasm.setMethodId((Integer)breedingMethodComboBox.getValue());
    	
    	                        germplasm.setGnpgs(-1);
    	                        if(germplasmMatchesCount==1){
    	                            //If a single match is found, multiple matches will be 
    	                            //   handled by SelectGemrplasmWindow and 
    	                            //   then receiveGermplasmFromWindowAndUpdateGermplasmData()
    	                            List<Germplasm> foundGermplasm = this.germplasmDataManager.getGermplasmByName(importedGermplasm.getDesig(), 0, 1, Operation.EQUAL);
    	                            if(foundGermplasm.get(0).getGnpgs()<2){
    	                            	germplasm.setGpid1(foundGermplasm.get(0).getGpid1());
    	                            } else {
    	                            	germplasm.setGpid1(foundGermplasm.get(0).getGid());                            	
    	                            }
    	                            germplasm.setGpid2(foundGermplasm.get(0).getGid()); 
    	                        } else {
    	                            //If no matches are found
    	                            germplasm.setGpid1(0); 
    	                            germplasm.setGpid2(0);
    	                        }
    	                        
    	                        germplasm.setUserId(ibdbUserId); 
    	                        germplasm.setLgid(0);
    	                        germplasm.setGrplce(0);
    	                        germplasm.setReferenceId(0);
    	                        germplasm.setMgid(0);
    	                        
                            }
                            
                            
    
                            Name name = new Name();
                            //name.setNid();
                            //name.setGermplasmId();
                            name.setTypeId((Integer)nameTypeComboBox.getValue());
                            name.setUserId(ibdbUserId);
                            name.setNval(importedGermplasm.getDesig());
                            name.setLocationId((Integer)locationComboBox.getValue());
                            name.setNdate(dateIntValue);
                            name.setReferenceId(0);
                            
                            if(!createdGermplasms.containsKey(name.getNval())){
                            	//germplasmList.add(germplasm);
                            	//nameList.add(name);
                            	createdGermplasms.put(name.getNval(), germplasm);
                            	
                            	germplasmNameObjects.add(new GermplasmName(germplasm,name));
                            } else {
                            	//germplasmList.add(createdGermplasms.get(name.getNval()));
                            	//nameList.add(name);
                            	
                            	germplasmNameObjects.add(new GermplasmName(createdGermplasms.get(name.getNval()),name));
                            }
    
                            if(germplasmMatchesCount>1 && importedGermplasm.getGid()==null){
                                displaySelectGermplasmWindow(importedGermplasm.getDesig(), i, germplasm);
                            }
    
                        }
                        //logFirstPedigreeUploadedToWorkbenchProjectActivity();
                    }catch (MiddlewareQueryException mqe){
                        mqe.printStackTrace();
                    }
                   
                } else if(pedigreeOptionComboBox.getValue().toString().equalsIgnoreCase("3") && getImportedGermplasms() != null){
                    //meaning 3rd pedigree
                    try{
                        WorkbenchRuntimeData data = workbenchDataManager.getWorkbenchRuntimeData();
                        
                        Integer wbUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
                        Project project = workbenchDataManager.getLastOpenedProject(wbUserId);
                        Integer ibdbUserId = workbenchDataManager.getLocalIbdbUserId(wbUserId, project.getProjectId());
                        
                        SimpleDateFormat formatter = new SimpleDateFormat(GermplasmImportMain.DATE_FORMAT);
                        String sDate = formatter.format(germplasmDateField.getValue());
    
                        Integer dateIntValue = Integer.parseInt(sDate.replace("-", ""));
                        
                        Map<String, Germplasm> createdGermplasms = new HashMap<String, Germplasm>();
                        
                        for(int i = 0 ; i < getImportedGermplasms().size(); i++){
                            
                            ImportedGermplasm importedGermplasm  = getImportedGermplasms().get(i);
                            int germplasmMatchesCount = (int) this.germplasmDataManager.countGermplasmByName(importedGermplasm.getDesig(), Operation.EQUAL);
                            
                            Germplasm germplasm = new Germplasm();
                            
                            boolean searchByNameOrNewGermplasmIsNeeded = true;
                            if(importedGermplasm.getGid()!=null && !importedGermplasm.getGid().equals(Integer.valueOf(0))){
                            	germplasm = germplasmDataManager.getGermplasmByGID(importedGermplasm.getGid());
                            	
                            	if(germplasm != null){
                            		List<Name> names = germplasmDataManager.getNamesByGID(importedGermplasm.getGid(), 0, null);
                            		boolean thereIsMatchingName = false;
                            		for(Name name : names){
                            			String nameInDb = name.getNval().toLowerCase();
                            			String nameInImportFile = importedGermplasm.getDesig().toLowerCase();
                            			String standardizedNameInImportFile = GermplasmDataManagerUtil.standardizeName(nameInImportFile).toLowerCase();
                            			String nameInImportFileWithSpacesRemoved = GermplasmDataManagerUtil.removeSpaces(nameInImportFile).toLowerCase();
                            			
                            			if(nameInDb.equals(nameInImportFile)
                            					|| nameInDb.equals(standardizedNameInImportFile)
                            					|| nameInDb.equals(nameInImportFileWithSpacesRemoved)){
                            				thereIsMatchingName = true;
                            			} 
                            		}
                            		
                            		if(thereIsMatchingName){
                            			doNotCreateGermplasmsWithId.add(importedGermplasm.getGid());
                            		} else{
                            			final Integer gidFinal = importedGermplasm.getGid(); 
                            			final String desigFinal = importedGermplasm.getDesig();
                            			final Integer finalIbdbUserId = ibdbUserId;
                            			final Integer finalDateIntValue = dateIntValue;
                            			final int index = i;
                            			final int finalGermplasmMatchesCount = germplasmMatchesCount;
                            			final Map<String, Germplasm> finalCreatedGermplasms = createdGermplasms;
                            			ConfirmDialog.show(source.getWindow(), "New Name" 
                            		            ,"The name \"" + importedGermplasm.getDesig() + "\" is not recorded as a name of GID " + importedGermplasm.getGid() + "."
                            		            + " Do you want to add the name to the GID or search/create another germplasm record?"
                            		            , "Add name to GID"  //confirm option
                            		            , "Search/create another germplasm record"  //the other option 
                            		            , new ConfirmDialog.Listener() {
                            						private static final long serialVersionUID = 1L;
                            						public void onClose(ConfirmDialog dialog) {
                            		                    if (dialog.isConfirmed()) {
                            		                    	addGermplasmName(desigFinal, gidFinal, finalIbdbUserId, finalDateIntValue);
                            		                    	doNotCreateGermplasmsWithId.add(gidFinal);
                            		                    } else{
                            		                    	searchOrAddANewGermplasm(finalGermplasmMatchesCount, finalIbdbUserId
                            		                    			, finalDateIntValue, desigFinal, index, finalCreatedGermplasms);
                            		                    }
                            		                }
                            		            }
                            			);
                            		}
                            		searchByNameOrNewGermplasmIsNeeded = false;
                            	} else{
                            		MessageNotifier.showWarning(getWindow(), "Warning!", "GID: " + importedGermplasm.getGid() + " written on file does not exist in database."
                            				, Notification.POSITION_CENTERED);
                            	}
                            } 
                            
                            if(germplasm == null){
                            	germplasm = new Germplasm();
                            }
                            
                            if(searchByNameOrNewGermplasmIsNeeded) {
                            	if(germplasmMatchesCount==1){
    	                            //If a single match is found, multiple matches will be 
    	                            //   handled by SelectGemrplasmWindow and 
    	                            //   then receiveGermplasmFromWindowAndUpdateGermplasmData()
    	                            List<Germplasm> foundGermplasm = this.germplasmDataManager.getGermplasmByName(importedGermplasm.getDesig(), 0, 1, Operation.EQUAL);
    	                            germplasm.setGid(foundGermplasm.get(0).getGid());
    	                            doNotCreateGermplasmsWithId.add(foundGermplasm.get(0).getGid());
    	                        } else {
    	                            //If no matches found
    	                            germplasm.setGid(i);
    	                        }
    	                        
    	                        germplasm.setUserId(ibdbUserId);
    	                        germplasm.setLocationId((Integer)locationComboBox.getValue());
    	                        germplasm.setGdate(dateIntValue);
    	                        germplasm.setMethodId((Integer)breedingMethodComboBox.getValue());
    	
    	                        germplasm.setGnpgs(0);
    	                        germplasm.setGpid1(0);
    	                        germplasm.setGpid2(0);
    	                        germplasm.setLgid(0);
    	                        germplasm.setGrplce(0);
    	                        germplasm.setReferenceId(0);
    	                        germplasm.setMgid(0);
    	                        
                            }
    
                            Name name = new Name();
                            name.setTypeId((Integer)nameTypeComboBox.getValue());
                            name.setUserId(ibdbUserId);
                            name.setNval(importedGermplasm.getDesig());
                            name.setLocationId((Integer)locationComboBox.getValue());
                            name.setNdate(dateIntValue);
                            name.setReferenceId(0);
                            
                            if(!createdGermplasms.containsKey(name.getNval())){
                            	//germplasmList.add(germplasm);
                            	//nameList.add(name);
                            	createdGermplasms.put(name.getNval(), germplasm);
                            	
                            	germplasmNameObjects.add(new GermplasmName(germplasm,name));
                            } else {
                            	//germplasmList.add(createdGermplasms.get(name.getNval()));
                            	//nameList.add(name);
                            	
                            	germplasmNameObjects.add(new GermplasmName(createdGermplasms.get(name.getNval()),name));
                            }
                            
                            if(germplasmMatchesCount>1 && searchByNameOrNewGermplasmIsNeeded){
                                displaySelectGermplasmWindow(importedGermplasm.getDesig(), i, germplasm);
                            }
                        }
                        //logFirstPedigreeUploadedToWorkbenchProjectActivity();
    
                    }catch (MiddlewareQueryException mqe){
                        LOG.error("Database error: " + mqe.getMessage(), mqe);
                    }
                    
                }
    
//               if(nextScreen instanceof SaveGermplasmListComponent){
//                   //((SaveGermplasmListComponent) nextScreen).setGermplasmList(germplasmList);
//                   ((SaveGermplasmListComponent) nextScreen).setDoNotCreateGermplasmsWithId(doNotCreateGermplasmsWithId);
//                   //((SaveGermplasmListComponent) nextScreen).setNameList(nameList);
//                   ((SaveGermplasmListComponent) nextScreen).setFilename(germplasmListUploader.getOriginalFilename());
//                   
//                    //for 909
//                   ((SaveGermplasmListComponent) nextScreen).setListDetails(germplasmListUploader.getListName(), germplasmListUploader.getListTitle(), germplasmListUploader.getListDate(), germplasmListUploader.getListType());
//                   
//    			   try {
//    				   Method breedingMethod = germplasmDataManager.getMethodByID((Integer) breedingMethodComboBox.getValue());
//    				   ((SaveGermplasmListComponent) nextScreen).setBreedingMethod(breedingMethod);
//    			   } catch (MiddlewareQueryException e) {
//    				   e.printStackTrace();
//    			   }
//                  
//               	}
                source.nextStep();
        }
    }
    
    private boolean validatePedigreeOption() {
        return BreedingManagerUtil.validateRequiredField(getWindow(), pedigreeOptionComboBox,
                messageSource, pedigreeOptionComboBox.getCaption());
    }
    
    private boolean validateLocation() {
        return BreedingManagerUtil.validateRequiredField(getWindow(), locationComboBox,
                messageSource, messageSource.getMessage(Message.GERMPLASM_LOCATION_LABEL));
    }
    
    private boolean validateMethod() {
        return BreedingManagerUtil.validateRequiredField(getWindow(), breedingMethodComboBox,
                messageSource, messageSource.getMessage(Message.GERMPLASM_BREEDING_METHOD_LABEL));
    }
  
    private void searchOrAddANewGermplasm(int germplasmMatchesCount, Integer ibdbUserId, Integer dateIntValue, String desig, int index
    		, Map<String, Germplasm> createdGermplasms){
    	Germplasm germplasm = new Germplasm();
    	if(germplasmMatchesCount==1){
            //If a single match is found, multiple matches will be 
            //   handled by SelectGemrplasmWindow and 
            //   then receiveGermplasmFromWindowAndUpdateGermplasmData()
    		try{
	            List<Germplasm> foundGermplasm = this.germplasmDataManager.getGermplasmByName(desig, 0, 1, Operation.EQUAL);
	            germplasm.setGid(foundGermplasm.get(0).getGid());
	            doNotCreateGermplasmsWithId.add(foundGermplasm.get(0).getGid());
    		} catch(MiddlewareQueryException ex){
    			LOG.error("Error with getting germplasm by name = " + desig, ex);
    			return;
    		}
        } else {
            //If no matches found
            germplasm.setGid(index);
        }
        
        germplasm.setUserId(ibdbUserId);
        germplasm.setLocationId((Integer)locationComboBox.getValue());
        germplasm.setGdate(dateIntValue);
        germplasm.setMethodId((Integer)breedingMethodComboBox.getValue());

        germplasm.setGnpgs(0);
        germplasm.setGpid1(0);
        germplasm.setGpid2(0);
        germplasm.setLgid(0);
        germplasm.setGrplce(0);
        germplasm.setReferenceId(0);
        germplasm.setMgid(0);
        
        if(germplasmMatchesCount>1){
            displaySelectGermplasmWindow(desig, index, germplasm);
        } else{
        	germplasmNameObjects.get(index).setGermplasm(germplasm);
        }
        
    }
    
    private void addGermplasmName(String desig, Integer gid, Integer ibdbUserId, Integer dateIntValue){
    	try{
    		Name name = new Name();
    		name.setNid(null);
    		name.setNstat(Integer.valueOf(0));
    		name.setGermplasmId(gid);
            name.setTypeId((Integer)nameTypeComboBox.getValue());
            name.setUserId(ibdbUserId);
            name.setNval(desig);
            name.setLocationId((Integer)locationComboBox.getValue());
            name.setNdate(dateIntValue);
            name.setReferenceId(0);
            germplasmDataManager.addGermplasmName(name);
    	} catch(MiddlewareQueryException ex){
    		LOG.error("Error with saving germplasm name.", ex);
    	}
    }
    
    public void backButtonClickAction(){
        source.backStep();
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

    /*
     * Called by the listener of the "Done" button on the select germplasm window
     */
    public void receiveGermplasmFromWindowAndUpdateGermplasmData(int index, Germplasm importedGermplasm, Germplasm selectedGermplasm) {
        if(pedigreeOptionComboBox.getValue().toString().equalsIgnoreCase("2")){
            //Update GPID 1 & 2 to values of selected germplasm, and update germplasmList using the updated germplasm
            
            if(selectedGermplasm.getGnpgs()<2){
            	importedGermplasm.setGpid1(selectedGermplasm.getGpid1());
            } else {
            	importedGermplasm.setGpid1(selectedGermplasm.getGid());                            	
            }
            importedGermplasm.setGpid2(selectedGermplasm.getGid());
            
            //germplasmList.set(index, importedGermplasm);
            germplasmNameObjects.get(index).setGermplasm(importedGermplasm);
            
        } else if(pedigreeOptionComboBox.getValue().toString().equalsIgnoreCase("3")){
            //Add logic here to not insert new record on DB when saved, maybe use existing GID?
            importedGermplasm.setGid(selectedGermplasm.getGid());
            doNotCreateGermplasmsWithId.add(selectedGermplasm.getGid());
            germplasmNameObjects.get(index).setGermplasm(importedGermplasm);
        }
    }
    
    public void setPedigreeOptionGroupValue(Integer value){
    	pedigreeOptionComboBox.setValue(value);
    }
    
    public Integer getPedigreeOptionGroupValue(){
    	return (Integer) pedigreeOptionComboBox.getValue();
    }
    
    public void setPedigreeOptionGroupEnabled(Boolean value){
    	pedigreeOptionComboBox.setEnabled(value);
    }

    public List<SelectGermplasmWindow> getSelectGermplasmWindows(){
    	return selectGermplasmWindows;
    }
    
    public void closeAllSelectGermplasmWindows(){
    	for(int i=0;i<selectGermplasmWindows.size();i++){
    		SelectGermplasmWindow selectGermplasmWindow = selectGermplasmWindows.get(i);
    		try {
    			selectGermplasmWindow.getParent().removeWindow(selectGermplasmWindow);
    		} catch(NullPointerException e) {
    			selectGermplasmWindows.remove(selectGermplasmWindow);
    		}
    	}
    	selectGermplasmWindows.clear();
    }
    
    public List<GermplasmName> getGermplasmNameObjects(){
    	return germplasmNameObjects;
    }
    
    private void updateComboBoxDescription(){
    	Object breedingMethodComboBoxValue = breedingMethodComboBox.getValue();
    	breedingMethodComboBox.setDescription("");
    	if(breedingMethodComboBoxValue!=null){
    		breedingMethodComboBox.setDescription(methodMap.get(breedingMethodComboBoxValue.toString()));
    	}
    }
    
}
