package org.generationcp.breeding.manager.listimport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.breeding.manager.listimport.listeners.MethodValueChangeListener;
import org.generationcp.breeding.manager.listimport.util.GermplasmListUploader;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.*;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

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
    private String DEFAULT_NAME_TYPE = "Line Name";
    private List<ImportedGermplasm> importedGermplasms;
    private GermplasmListUploader germplasmListUploader;

    private List<Germplasm> germplasmList = new ArrayList();
    private List<Name> nameList = new ArrayList();    
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    @Autowired
    private GermplasmDataManager germplasmDataManager;

    @Autowired
     private GermplasmListManager germplasmListManager;
    @Autowired
     private WorkbenchDataManager workbenchDataManager;
        
    public SpecifyGermplasmDetailsComponent(GermplasmImportMain source, Accordion accordion){
        this.source = source;
        this.accordion = accordion;
    }

    public Table getGermplasmDetailsTable(){
        return germplasmDetailsTable;
    }
    
    public void setNextScreen(Component nextScreen){
        this.nextScreen = nextScreen;
    }
    
    public void setPreviousScreen(Component previousScreen){
        this.previousScreen = previousScreen;
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
        SelectGermplasmWindow selectGermplasmWindow = new SelectGermplasmWindow(this, germplasmName, i, germplasm);
        this.getWindow().addWindow(selectGermplasmWindow);
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
        breedingMethodComboBox.setImmediate(true);
        breedingMethodComboBox.addListener(new MethodValueChangeListener(breedingMethodComboBox, methodMap));
        addComponent(breedingMethodComboBox, "top:10px;left:200px");
        
        germplasmDateLabel = new Label();
        addComponent(germplasmDateLabel, "top:60px;left:20px");
        
        germplasmDateField =  new DateField();
        germplasmDateField.setResolution(DateField.RESOLUTION_DAY);
        germplasmDateField.setDateFormat(GermplasmImportMain.DATE_FORMAT);
        germplasmDateField.setValue(new Date());
        addComponent(germplasmDateField, "top:40px;left:200px");
        
        locationLabel = new Label();
        addComponent(locationLabel, "top:90px;left:20px");
        
        locationComboBox = new ComboBox();
        locationComboBox.setWidth("400px");
        locationComboBox.setNullSelectionAllowed(false);
        List<Location> locationList = germplasmDataManager.getAllBreedingLocations();
        Map locationMap = new HashMap();
        Integer firstId = null;
        boolean hasDefault = false;
       for(Location location : locationList){
           //method.getMcode()
           if(firstId == null){
               firstId = location.getLocid();
           }
           locationComboBox.addItem(location.getLocid());
           locationComboBox.setItemCaption(location.getLocid(), location.getLname());
           if(DEFAULT_LOCATION.equalsIgnoreCase(location.getLname())){
               locationComboBox.setValue(location.getLocid());
               hasDefault = true;
               //locationComboBox.setDescription(location.get);
           }
           locationMap.put(location.getLocid(), location.getLname());
       }
        if(hasDefault == false && firstId != null){
            locationComboBox.setValue(firstId);
        }
        locationComboBox.setImmediate(true);
        //locationComboBox.addListener(new MethodValueChangeListener(locationComboBox, locationMap));

        addComponent(locationComboBox, "top:70px;left:200px");
        
        nameTypeLabel = new Label();
        addComponent(nameTypeLabel, "top:120px;left:20px");
        
        nameTypeComboBox = new ComboBox();
        nameTypeComboBox.setWidth("400px");
        nameTypeComboBox.setNullSelectionAllowed(false);
        List<UserDefinedField> userDefinedFieldList = germplasmListManager.getGermplasmNameTypes();
         firstId = null;
         hasDefault = false;
        for(UserDefinedField userDefinedField : userDefinedFieldList){
                  //method.getMcode()
                    if(firstId == null){
                          firstId = userDefinedField.getFldno();
                      }
            nameTypeComboBox.addItem(userDefinedField.getFldno());
            nameTypeComboBox.setItemCaption(userDefinedField.getFldno(), userDefinedField.getFname());
                  if(DEFAULT_NAME_TYPE.equalsIgnoreCase(userDefinedField.getFname())){
                      nameTypeComboBox.setValue(userDefinedField.getFldno());
                      hasDefault = true;
                      //locationComboBox.setDescription(location.get);
                  }
              }
        if(hasDefault == false && firstId != null){
                    locationComboBox.setValue(firstId);
                }

        nameTypeComboBox.setImmediate(true);

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
            
            if(pedigreeOptionGroup.getValue().toString().equalsIgnoreCase("1") && getImportedGermplasms() != null){
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
                    for(int i = 0 ; i < getImportedGermplasms().size(); i++){
                        ImportedGermplasm importedGermplasm  = getImportedGermplasms().get(i);
                        Germplasm germplasm = new Germplasm();
                        germplasm.setGid(i);
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
                        germplasmList.add(germplasm);

                        Name name = new Name();
                        //name.setNid();
                        //name.setGermplasmId();
                        name.setTypeId((Integer)nameTypeComboBox.getValue());
                        name.setUserId(ibdbUserId);
                        name.setNval(importedGermplasm.getDesig());
                        name.setLocationId((Integer)locationComboBox.getValue());
                        name.setNdate(dateIntValue);
                        name.setReferenceId(0);
                        nameList.add(name);
                    }
                    //logFirstPedigreeUploadedToWorkbenchProjectActivity();

                }catch (MiddlewareQueryException mqe){
                    mqe.printStackTrace();
                }
                
            } else if(pedigreeOptionGroup.getValue().toString().equalsIgnoreCase("2") && getImportedGermplasms() != null){
                //meaning 2nd pedigree
                try{
                    WorkbenchRuntimeData data = workbenchDataManager.getWorkbenchRuntimeData();
                    
                    SimpleDateFormat formatter = new SimpleDateFormat(GermplasmImportMain.DATE_FORMAT);
                    String sDate = formatter.format(germplasmDateField.getValue());

                    Integer dateIntValue = Integer.parseInt(sDate.replace("-", ""));
                    
                    for(int i = 0 ; i < getImportedGermplasms().size(); i++){
                        
                        ImportedGermplasm importedGermplasm  = getImportedGermplasms().get(i);
                        int germplasmMatchesCount = (int) this.germplasmDataManager.countGermplasmByName(importedGermplasm.getDesig(), Operation.EQUAL);
                        
                        Germplasm germplasm = new Germplasm();
                        germplasm.setGid(i);
                        germplasm.setUserId(data.getUserId());
                        germplasm.setLocationId((Integer)locationComboBox.getValue());
                        germplasm.setGdate(dateIntValue);
                        germplasm.setMethodId((Integer)breedingMethodComboBox.getValue());

                        germplasm.setGnpgs(-1);
                        if(germplasmMatchesCount==1){
                            //If a single match is found, multiple matches will be 
                            //   handled by SelectGemrplasmWindow and 
                            //   then receiveGermplasmFromWindowAndUpdateGermplasmData()
                            List<Germplasm> foundGermplasm = this.germplasmDataManager.getGermplasmByName(importedGermplasm.getDesig(), 0, 1, Operation.EQUAL);
                            germplasm.setGpid1(foundGermplasm.get(0).getGpid1()); 
                            germplasm.setGpid2(foundGermplasm.get(0).getGid()); 
                        } else {
                            //If no matches are found
                            germplasm.setGpid1(0); 
                            germplasm.setGpid2(0);
                        }
                        
                        germplasm.setUserId(data.getUserId()); 
                        germplasm.setLgid(0);
                        germplasm.setGrplce(0);
                        germplasm.setReferenceId(0);
                        germplasm.setMgid(0);
                        germplasmList.add(germplasm);

                        Name name = new Name();
                        //name.setNid();
                        //name.setGermplasmId();
                        name.setTypeId((Integer)nameTypeComboBox.getValue());
                        name.setUserId(data.getUserId());
                        name.setNval(importedGermplasm.getDesig());
                        name.setLocationId((Integer)locationComboBox.getValue());
                        name.setNdate(dateIntValue);
                        name.setReferenceId(0);
                        nameList.add(name);
                        
                        if(germplasmMatchesCount>1){
                            displaySelectGermplasmWindow(importedGermplasm.getDesig(), i, germplasm);
                        }

                    }
                    //logFirstPedigreeUploadedToWorkbenchProjectActivity();
                }catch (MiddlewareQueryException mqe){
                    mqe.printStackTrace();
                }
               
            } else if(pedigreeOptionGroup.getValue().toString().equalsIgnoreCase("3") && getImportedGermplasms() != null){
                //meaning 3rd pedigree
                try{
                    WorkbenchRuntimeData data = workbenchDataManager.getWorkbenchRuntimeData();

                    SimpleDateFormat formatter = new SimpleDateFormat(GermplasmImportMain.DATE_FORMAT);
                    String sDate = formatter.format(germplasmDateField.getValue());

                    Integer dateIntValue = Integer.parseInt(sDate.replace("-", ""));
                    for(int i = 0 ; i < getImportedGermplasms().size(); i++){
                        
                        ImportedGermplasm importedGermplasm  = getImportedGermplasms().get(i);
                        int germplasmMatchesCount = (int) this.germplasmDataManager.countGermplasmByName(importedGermplasm.getDesig(), Operation.EQUAL);
                        
                        Germplasm germplasm = new Germplasm();
                        
                        if(germplasmMatchesCount==1){
                            //If a single match is found, multiple matches will be 
                            //   handled by SelectGemrplasmWindow and 
                            //   then receiveGermplasmFromWindowAndUpdateGermplasmData()
                            List<Germplasm> foundGermplasm = this.germplasmDataManager.getGermplasmByName(importedGermplasm.getDesig(), 0, 1, Operation.EQUAL);
                            germplasm.setGid(foundGermplasm.get(0).getGid());
                        } else {
                            //If no matches found
                            germplasm.setGid(i);
                        }
                        
                        germplasm.setUserId(data.getUserId());
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
                        germplasmList.add(germplasm);

                        Name name = new Name();
                        //name.setNid();
                        //name.setGermplasmId();
                        name.setTypeId((Integer)nameTypeComboBox.getValue());
                        name.setUserId(data.getUserId());
                        name.setNval(importedGermplasm.getDesig());
                        name.setLocationId((Integer)locationComboBox.getValue());
                        name.setNdate(dateIntValue);
                        name.setReferenceId(0);
                        nameList.add(name);
                        
                        if(germplasmMatchesCount>1){
                            displaySelectGermplasmWindow(importedGermplasm.getDesig(), i, germplasm);
                        }
                    }
                    //logFirstPedigreeUploadedToWorkbenchProjectActivity();

                }catch (MiddlewareQueryException mqe){
                    mqe.printStackTrace();
                }
                
            }

           if(nextScreen instanceof SaveGermplasmListComponent){
               ((SaveGermplasmListComponent) nextScreen).setGermplasmList(germplasmList);
               ((SaveGermplasmListComponent) nextScreen).setNameList(nameList);
               ((SaveGermplasmListComponent) nextScreen).setFilename(germplasmListUploader.getOriginalFilename());
                //for 909
               ((SaveGermplasmListComponent) nextScreen).setListDetails(germplasmListUploader.getListName(), germplasmListUploader.getListTitle(), germplasmListUploader.getListDate());
           }
           
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

    /*
     * Called by the listener of the "Done" button on the select germplasm window
     */
    public void receiveGermplasmFromWindowAndUpdateGermplasmData(int index, Germplasm importedGermplasm, Germplasm selectedGermplasm) {
        if(pedigreeOptionGroup.getValue().toString().equalsIgnoreCase("2")){
            //Update GPID 1 & 2 to values of selected germplasm, and update germplasmList using the updated germplasm
            importedGermplasm.setGpid1(selectedGermplasm.getGpid1()); 
            importedGermplasm.setGpid2(selectedGermplasm.getGid());            
            germplasmList.set(index, importedGermplasm);
        } else if(pedigreeOptionGroup.getValue().toString().equalsIgnoreCase("3")){
            //Add logic here to not insert new record on DB when saved, maybe use existing GID?
            importedGermplasm.setGid(selectedGermplasm.getGid());
        }
    }
}
