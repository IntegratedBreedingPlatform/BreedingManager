package org.generationcp.breeding.manager.listimport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.breeding.manager.listimport.util.GermplasmListUploader;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
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
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SpecifyGermplasmDetailsComponent extends VerticalLayout implements InitializingBean, 
		InternationalizableComponent, BreedingManagerLayout, SaveListAsDialogSource {

    private static final long serialVersionUID = 2762965368037453497L;
    private final static Logger LOG = LoggerFactory.getLogger(SpecifyGermplasmDetailsComponent.class);
    
    public static final String NEXT_BUTTON_ID = "next button";
    public static final String BACK_BUTTON_ID = "back button";

    private GermplasmImportMain source;
    
    private GermplasmFieldsComponent germplasmFieldsComponent;
    private Table germplasmDetailsTable;
    
    private Label reviewImportDetailsLabel;
    private Label totalEntriesLabel;
    private Label selectPedigreeOptionsLabel;
    private Label pedigreeOptionsLabel;
    
    private ComboBox pedigreeOptionComboBox;
    
    private Button backButton;
    private Button nextButton;
    
    private List<ImportedGermplasm> importedGermplasms;
    private GermplasmListUploader germplasmListUploader;

    private List<Integer> doNotCreateGermplasmsWithId = new ArrayList<Integer>();
    
    private List<SelectGermplasmWindow> selectGermplasmWindows = new ArrayList<SelectGermplasmWindow>();
    private List<GermplasmName> germplasmNameObjects;

	private GermplasmList germplasmList;
	
	private SaveListAsDialog saveListAsDialog;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
        
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    private Boolean viaToolURL;
    
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
//        messageSource.setCaption(pedigreeOptionComboBox, Message.PEDIGREE_OPTIONS_LABEL);
        messageSource.setCaption(backButton, Message.BACK);
        messageSource.setCaption(nextButton, Message.FINISH);
    }
    
    
        
    public void nextButtonClickAction(){
        if (validateMethod() && validateLocation() && validatePedigreeOption()) {
        	
            	germplasmNameObjects = new ArrayList<GermplasmName>();
            	
                //germplasmList = new ArrayList<Germplasm>(); 
                //nameList = new ArrayList<Name>();
                doNotCreateGermplasmsWithId = new ArrayList<Integer>();
                
                popupSaveAsDialog();

                if(pedigreeOptionComboBox.getValue().toString().equalsIgnoreCase("1") && getImportedGermplasms() != null){
                    //meaning 1st pedigree
                    //we should create the germplasm and named pojos here
                    try{
                        WorkbenchRuntimeData data = workbenchDataManager.getWorkbenchRuntimeData();
    
                        Integer wbUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
                        Project project = workbenchDataManager.getLastOpenedProject(wbUserId);
                        Integer ibdbUserId = workbenchDataManager.getLocalIbdbUserId(wbUserId, project.getProjectId());
    
                        SimpleDateFormat formatter = new SimpleDateFormat(GermplasmImportMain.DATE_FORMAT);
                        String sDate = formatter.format(germplasmFieldsComponent.getGermplasmDateField().getValue());
     
                        Integer dateIntValue = Integer.parseInt(sDate.replace("-", ""));
                        
                        Map<String, Germplasm> createdGermplasms = new HashMap<String, Germplasm>();

                        
                        
                        for(int i = 0 ; i < getImportedGermplasms().size(); i++){
                            ImportedGermplasm importedGermplasm  = getImportedGermplasms().get(i);
                            Germplasm germplasm = new Germplasm();
                            germplasm.setGid(i);
                            germplasm.setUserId(ibdbUserId);
                            germplasm.setLocationId((Integer)germplasmFieldsComponent.getLocationComboBox().getValue());
                            germplasm.setGdate(dateIntValue);
                            germplasm.setMethodId((Integer)germplasmFieldsComponent.getBreedingMethodComboBox().getValue());
    
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
                            name.setTypeId((Integer)germplasmFieldsComponent.getNameTypeComboBox().getValue());
                            name.setUserId(ibdbUserId);
                            name.setNval(importedGermplasm.getDesig());
                            name.setLocationId((Integer)germplasmFieldsComponent.getLocationComboBox().getValue());
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
                        String sDate = formatter.format(germplasmFieldsComponent.getGermplasmDateField().getValue());
    
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
    	                        germplasm.setLocationId((Integer)germplasmFieldsComponent.getLocationComboBox().getValue());
    	                        germplasm.setGdate(dateIntValue);
    	                        germplasm.setMethodId((Integer)germplasmFieldsComponent.getBreedingMethodComboBox().getValue());
    	
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
                            name.setTypeId((Integer)germplasmFieldsComponent.getNameTypeComboBox().getValue());
                            name.setUserId(ibdbUserId);
                            name.setNval(importedGermplasm.getDesig());
                            name.setLocationId((Integer)germplasmFieldsComponent.getLocationComboBox().getValue());
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
                        String sDate = formatter.format(germplasmFieldsComponent.getGermplasmDateField().getValue());
    
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
                            				);
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
    	                        germplasm.setLocationId((Integer)germplasmFieldsComponent.getLocationComboBox().getValue());
    	                        germplasm.setGdate(dateIntValue);
    	                        germplasm.setMethodId((Integer)germplasmFieldsComponent.getBreedingMethodComboBox().getValue());
    	
    	                        germplasm.setGnpgs(0);
    	                        germplasm.setGpid1(0);
    	                        germplasm.setGpid2(0);
    	                        germplasm.setLgid(0);
    	                        germplasm.setGrplce(0);
    	                        germplasm.setReferenceId(0);
    	                        germplasm.setMgid(0);
    	                        
                            }
    
                            Name name = new Name();
                            name.setTypeId((Integer)germplasmFieldsComponent.getNameTypeComboBox().getValue());
                            name.setUserId(ibdbUserId);
                            name.setNval(importedGermplasm.getDesig());
                            name.setLocationId((Integer)germplasmFieldsComponent.getLocationComboBox().getValue());
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
        }
    }
    
    private void popupSaveAsDialog(){

		germplasmList = new GermplasmList();
		
	    SimpleDateFormat formatter = new SimpleDateFormat(GermplasmImportMain.DATE_FORMAT);
	    String sDate = formatter.format(germplasmListUploader.getListDate());
	
	    Long dataLongValue = Long.parseLong(sDate.replace("-", ""));
	    germplasmList.setName(germplasmListUploader.getListName());
	    germplasmList.setDate(dataLongValue);
	    germplasmList.setType(germplasmListUploader.getListType());
	    germplasmList.setDescription(germplasmListUploader.getListTitle());
	    germplasmList.setStatus(1);
	     
	    List<GermplasmName> germplasmNameObjects = getGermplasmNameObjects();
	    List<GermplasmName> germplasmNameObjectsToBeSaved = new ArrayList<GermplasmName>();
	     
	    for(int i = 0 ; i < germplasmNameObjects.size() ; i++){
	        if(doNotCreateGermplasmsWithId.contains(germplasmNameObjects.get(i).getGermplasm().getGid())){
	            //Get germplasm using temporarily set GID, then create map
	            Germplasm germplasmToBeUsed;
				try {
					germplasmToBeUsed = germplasmDataManager.getGermplasmByGID(germplasmNameObjects.get(i).getGermplasm().getGid());
					germplasmNameObjectsToBeSaved.add(new GermplasmName(germplasmToBeUsed, germplasmNameObjects.get(i).getName()));
				} catch (MiddlewareQueryException e) {
					e.printStackTrace();
				}
	        } else {
	        	if(germplasmNameObjects.get(i).getGermplasm().getGpid1()==0 && germplasmNameObjects.get(i).getGermplasm().getGpid2()==0)
	        		germplasmNameObjects.get(i).getGermplasm().setGnpgs(-1);
	        	 
	           	germplasmNameObjectsToBeSaved.add(new GermplasmName(germplasmNameObjects.get(i).getGermplasm(), germplasmNameObjects.get(i).getName()));
	        }
	    }
	     
	    saveListAsDialog = new SaveListAsDialog(this, germplasmList);
	    this.getWindow().addWindow(saveListAsDialog);
         
    }
    
    private boolean validatePedigreeOption() {
        return BreedingManagerUtil.validateRequiredField(getWindow(), pedigreeOptionComboBox,
                messageSource, messageSource.getMessage(Message.PEDIGREE_OPTIONS_LABEL));
    }
    
    private boolean validateLocation() {
        return BreedingManagerUtil.validateRequiredField(getWindow(), germplasmFieldsComponent.getLocationComboBox(),
                messageSource, messageSource.getMessage(Message.GERMPLASM_LOCATION_LABEL));
    }
    
    private boolean validateMethod() {
        return BreedingManagerUtil.validateRequiredField(getWindow(), germplasmFieldsComponent.getBreedingMethodComboBox(),
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
        germplasm.setLocationId((Integer)germplasmFieldsComponent.getLocationComboBox().getValue());
        germplasm.setGdate(dateIntValue);
        germplasm.setMethodId((Integer)germplasmFieldsComponent.getBreedingMethodComboBox().getValue());

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
            name.setTypeId((Integer)germplasmFieldsComponent.getNameTypeComboBox().getValue());
            name.setUserId(ibdbUserId);
            name.setNval(desig);
            name.setLocationId((Integer)germplasmFieldsComponent.getLocationComboBox().getValue());
            name.setNdate(dateIntValue);
            name.setReferenceId(0);
            germplasmDataManager.addGermplasmName(name);
    	} catch(MiddlewareQueryException ex){
    		LOG.error("Error with saving germplasm name.", ex);
    	}
    }
    
    private void updateTotalEntriesLabel(){
    	int count = germplasmDetailsTable.getItemIds().size();
		if(count == 0) {
			totalEntriesLabel.setValue(messageSource.getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL));
		} else {
			totalEntriesLabel.setValue(messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " 
	        		 + "  <b>" + count + "</b>");
        }
    }
    
    public void backButtonClickAction(){
        source.backStep();
    }
    
    public GermplasmImportMain getSource() {
        return source;
    }
    
    public void setGermplasmBreedingMethod(String breedingMethod){
    	germplasmFieldsComponent.setGermplasmBreedingMethod(breedingMethod);
    }
    
    public void setGermplasmDate(Date germplasmDate) throws ReadOnlyException, ConversionException, ParseException{
        germplasmFieldsComponent.setGermplasmDate(germplasmDate);
    }
    public void setGermplasmLocation(String germplasmLocation){
        germplasmFieldsComponent.setGermplasmLocation(germplasmLocation);
    }
    public void setGermplasmListType(String germplasmListType){
        germplasmFieldsComponent.setGermplasmListType(germplasmListType);
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
    

	@Override
	public void instantiateComponents() {
		germplasmFieldsComponent = new GermplasmFieldsComponent(200);
		
        reviewImportDetailsLabel = new Label(messageSource.getMessage(Message.GERMPLASM_DETAILS_LABEL).toUpperCase());
        reviewImportDetailsLabel.addStyleName(Bootstrap.Typography.H4.styleName());
        
        totalEntriesLabel = new Label("Total Entries: 0", Label.CONTENT_XHTML);
		
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
        
        selectPedigreeOptionsLabel = new Label(messageSource.getMessage(Message.SELECT_PEDIGREE_OPTIONS).toUpperCase());
        selectPedigreeOptionsLabel.addStyleName(Bootstrap.Typography.H4.styleName());
        
        pedigreeOptionsLabel = new Label(messageSource.getMessage(Message.PEDIGREE_OPTIONS_LABEL) + ":");
        pedigreeOptionsLabel.addStyleName(AppConstants.CssStyles.BOLD);
        pedigreeOptionsLabel.setWidth("250px");
        
        pedigreeOptionComboBox = new ComboBox();
        pedigreeOptionComboBox.setRequired(true);
        pedigreeOptionComboBox.setWidth("450px");
        pedigreeOptionComboBox.setInputPrompt("Please Choose");
        
        
        GermplasmImportButtonClickListener clickListener = new GermplasmImportButtonClickListener(this);
        
        backButton = new Button();
        backButton.setData(BACK_BUTTON_ID);
        backButton.addListener(clickListener);
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(clickListener);
        nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
        // 2nd section
        pedigreeOptionComboBox.addItem(1);
        pedigreeOptionComboBox.addItem(2);
        pedigreeOptionComboBox.addItem(3);
        pedigreeOptionComboBox.setItemCaption(1, messageSource.getMessage(Message.IMPORT_PEDIGREE_OPTION_ONE));
        pedigreeOptionComboBox.setItemCaption(2, messageSource.getMessage(Message.IMPORT_PEDIGREE_OPTION_TWO));
        pedigreeOptionComboBox.setItemCaption(3, messageSource.getMessage(Message.IMPORT_PEDIGREE_OPTION_THREE));
	}

	

	@Override
	public void addListeners() {
//		nextButton.addListener(new ClickListener(){
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void buttonClick(ClickEvent event) {
//				nextButtonClickAction();
//			}
//			
//		});
	}

	@Override
	public void layoutComponents() {
		setWidth("800px");
        
		// Review Import Details Layout
		VerticalLayout importDetailsLayout = new VerticalLayout();
		importDetailsLayout.setSpacing(true);
		importDetailsLayout.addComponent(reviewImportDetailsLabel);
		importDetailsLayout.addComponent(totalEntriesLabel);
		importDetailsLayout.addComponent(germplasmDetailsTable);
		
		
		// Pedigree Options Layout
		VerticalLayout pedigreeOptionsLayout = new VerticalLayout();
		pedigreeOptionsLayout.setSpacing(true);
		
		HorizontalLayout pedigreeControlsLayout = new HorizontalLayout();
		pedigreeControlsLayout.addComponent(pedigreeOptionsLabel);
		pedigreeControlsLayout.addComponent(pedigreeOptionComboBox);

		pedigreeOptionsLayout.addComponent(selectPedigreeOptionsLabel);
		pedigreeOptionsLayout.addComponent(pedigreeControlsLayout);
		
		
		// Buttons Layout
		HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidth("100%");
        buttonLayout.setHeight("40px");
        buttonLayout.setSpacing(true);
        
        buttonLayout.addComponent(backButton);
        buttonLayout.addComponent(nextButton);
        buttonLayout.setComponentAlignment(backButton, Alignment.BOTTOM_RIGHT);
        buttonLayout.setComponentAlignment(nextButton, Alignment.BOTTOM_LEFT);
		
        VerticalLayout spacerLayout = new VerticalLayout();
        spacerLayout.setHeight("30px");
        VerticalLayout spacerLayout2 = new VerticalLayout();
        spacerLayout2.setHeight("30px");
        
		addComponent(germplasmFieldsComponent);
		addComponent(importDetailsLayout);
		addComponent(spacerLayout);
		addComponent(pedigreeOptionsLayout);
		addComponent(spacerLayout2);
        addComponent(buttonLayout);
	}
	
	public void initializeFromImportFile(ImportedGermplasmList importedGermplasmList){
		//Clear table contents first (possible that it has some rows in it from previous uploads, and then user went back to upload screen)
		getGermplasmDetailsTable().removeAllItems();
        String source;
        for(int i = 0 ; i < importedGermplasms.size() ; i++){
            ImportedGermplasm importedGermplasm  = importedGermplasms.get(i);
            if(importedGermplasm.getSource()==null){
            	source = importedGermplasmList.getFilename()+":"+(i+1);
            }else{
            	source=importedGermplasm.getSource();
            }
            getGermplasmDetailsTable().addItem(new Object[]{importedGermplasm.getEntryId(), importedGermplasm.getEntryCode(),importedGermplasm.getGid(), importedGermplasm.getDesig(), importedGermplasm.getCross(), source}, new Integer(i+1));
        }
        updateTotalEntriesLabel();

        if(germplasmListUploader.importFileIsAdvanced()){
        	setPedigreeOptionGroupValue(3);
        	setPedigreeOptionGroupEnabled(false);
        } else {
        	setPedigreeOptionGroupEnabled(true);
        }
	}

	@Override
	public void saveList(GermplasmList list) {
			
		SaveGermplasmListAction saveGermplasmListAction = new SaveGermplasmListAction();
		
		try {
			saveGermplasmListAction.saveRecords(list, germplasmNameObjects, germplasmListUploader.getOriginalFilename(), doNotCreateGermplasmsWithId, importedGermplasms);
			MessageNotifier.showMessage(this.source.getWindow(), messageSource.getMessage(Message.SUCCESS), messageSource.getMessage(Message.GERMPLASM_LIST_SAVED_SUCCESSFULLY), 3000);
			source.backStep();
			source.reset();
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void setCurrentlySavedGermplasmList(GermplasmList list) {
		this.germplasmList = list;
	}

	@Override
	public Component getParentComponent() {
		return source;
	}
	
	public GermplasmList getGermplasmList(){
		return germplasmList;
	}
    
	public SaveListAsDialog getSaveListAsDialog(){
		return saveListAsDialog;
	}
	
}
