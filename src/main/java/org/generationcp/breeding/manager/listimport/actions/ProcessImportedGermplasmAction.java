package org.generationcp.breeding.manager.listimport.actions;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.listimport.GermplasmFieldsComponent;
import org.generationcp.breeding.manager.listimport.GermplasmImportMain;
import org.generationcp.breeding.manager.listimport.SelectGermplasmWindow;
import org.generationcp.breeding.manager.listimport.SpecifyGermplasmDetailsComponent;
import org.generationcp.breeding.manager.listimport.listeners.ImportGermplasmEntryActionListener;
import org.generationcp.breeding.manager.listimport.listeners.NameNotExistsForGermplasmActionListener;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmDataManagerUtil;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.workbench.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Window;

@Configurable
public class ProcessImportedGermplasmAction implements Serializable {

	private static final long serialVersionUID = -9047259985457065559L;
	private static final Logger LOG = LoggerFactory.getLogger(ProcessImportedGermplasmAction.class);
	private static final int PREFERRED_NAME_STATUS = 1;
	
	private SpecifyGermplasmDetailsComponent germplasmDetailsComponent;
	
    private List<Integer> doNotCreateGermplasmsWithId = new ArrayList<Integer>();
    private List<ImportGermplasmEntryActionListener> importEntryListeners = new ArrayList<ImportGermplasmEntryActionListener>();
    private List<GermplasmName> germplasmNameObjects = new ArrayList<GermplasmName>();
    private List<Name> newDesignationsForExistingGermplasm = new ArrayList<Name>();
    
    private Map<String,Germplasm> nameGermplasmMap = new HashMap<String,Germplasm>();
    private final Integer UNKNOWN_DERIVATIVE_METHOD = 31;
    
	@Autowired
	private WorkbenchDataManager workbenchDataManager;
	
    @Autowired
    private GermplasmDataManager germplasmDataManager;
	
	public ProcessImportedGermplasmAction(SpecifyGermplasmDetailsComponent germplasmDetailsComponent) {
		super();
		this.germplasmDetailsComponent = germplasmDetailsComponent;
		if(importEntryListeners!=null) {
			importEntryListeners.clear();
		}
	}

	
	public void processGermplasm(){
		germplasmNameObjects = new ArrayList<GermplasmName>();
        doNotCreateGermplasmsWithId = new ArrayList<Integer>();
        nameGermplasmMap = new HashMap<String, Germplasm>();
        importEntryListeners.clear();
        newDesignationsForExistingGermplasm = new ArrayList<Name>();
        
		String pedigreeOptionChosen = germplasmDetailsComponent.getPedigreeOption();
		if(pedigreeOptionChosen.equalsIgnoreCase("1") && getImportedGermplasms() != null){
            performFirstPedigreeAction();
        } else if(pedigreeOptionChosen.equalsIgnoreCase("2") && getImportedGermplasms() != null){
            performSecondPedigreeAction();
        } else if(pedigreeOptionChosen.equalsIgnoreCase("3") && getImportedGermplasms() != null){
            performThirdPedigreeAction();
        }
		if(importEntryListeners.isEmpty()) {
			saveImport();
		}
	}


	protected Integer getIBDBUserId() throws MiddlewareQueryException {
		Integer wbUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
		Project project = workbenchDataManager.getLastOpenedProject(wbUserId);
		Integer ibdbUserId = workbenchDataManager.getLocalIbdbUserId(wbUserId, project.getProjectId());
		return ibdbUserId;
	}


	protected void performFirstPedigreeAction() {
		try{
		    Integer ibdbUserId = getIBDBUserId();
		    Integer dateIntValue = getGermplasmDateValue();
		    
		    Map<String, Germplasm> createdGermplasms = new HashMap<String, Germplasm>();
		    
		    for(int i = 0 ; i < getImportedGermplasms().size(); i++){
		        ImportedGermplasm importedGermplasm  = getImportedGermplasms().get(i);
		        
		        Germplasm germplasm = createGermplasmObject(i, -1, 0, 0, ibdbUserId, dateIntValue);

		        Name name = createNameObject(ibdbUserId, dateIntValue, importedGermplasm.getDesig());
		        
		        if(!createdGermplasms.containsKey(name.getNval())){
		        	createdGermplasms.put(name.getNval(), germplasm);
		        	
		        	germplasmNameObjects.add(new GermplasmName(germplasm,name));
		        } else {
		        	germplasmNameObjects.add(new GermplasmName(createdGermplasms.get(name.getNval()),name));
		        }
		        
		    }

		}catch (MiddlewareQueryException mqe){
		    mqe.printStackTrace();
		}
	}
	
	
	protected void performSecondPedigreeAction() {
		try{
			Integer ibdbUserId = getIBDBUserId();
		    Integer dateIntValue = getGermplasmDateValue();                    
		    
		    Map<String, Germplasm> createdGermplasms = new HashMap<String, Germplasm>();

		    for(int i = 0 ; i < getImportedGermplasms().size(); i++){
		        
		        ImportedGermplasm importedGermplasm  = getImportedGermplasms().get(i);
		        int germplasmMatchesCount = (int) this.germplasmDataManager.countGermplasmByName(importedGermplasm.getDesig(), Operation.EQUAL);
		        
		        // gpid1 and gpid 2 values are default here, actual values will be set below based on matched germplasm
		        Germplasm germplasm = createGermplasmObject(i, -1, 0, 0, ibdbUserId, dateIntValue);
		        List<Germplasm> foundGermplasm = new ArrayList<Germplasm>();
		        
		        if(isGidSpecified(importedGermplasm)){
		        	foundGermplasm.add(germplasmDataManager.getGermplasmByGID(importedGermplasm.getGid())); 
		        } else {
	        		
	                if(germplasmMatchesCount==1){
	                    //If a single match is found, multiple matches will be 
	                    //   handled by SelectGemrplasmWindow and 
	                    //   then receiveGermplasmFromWindowAndUpdateGermplasmData()
	                    foundGermplasm = this.germplasmDataManager.getGermplasmByName(importedGermplasm.getDesig(), 0, 1, Operation.EQUAL);
	                } 
		                
		        }
		        
				if (foundGermplasm != null && !foundGermplasm.isEmpty() && foundGermplasm.get(0) != null){
                    updatePedigreeConnections(germplasm, foundGermplasm.get(0)); 
		        }

		        
		        Name name = createNameObject(ibdbUserId, dateIntValue, importedGermplasm.getDesig());
		        name.setNstat(PREFERRED_NAME_STATUS);
		        
		        if(!createdGermplasms.containsKey(name.getNval())){
		        	createdGermplasms.put(name.getNval(), germplasm);
		        	
		        	germplasmNameObjects.add(new GermplasmName(germplasm,name));
		        } else {
		        	
		        	germplasmNameObjects.add(new GermplasmName(createdGermplasms.get(name.getNval()),name));
		        }
		        
		        if(germplasmMatchesCount>1){
		        	displaySelectGermplasmWindowIfNecessary(importedGermplasm.getDesig(), i, germplasm);
		        }

		    }
		    
		}catch (MiddlewareQueryException mqe){
		    mqe.printStackTrace();
		}
	}

	// Set imported germplasm's gpid1 and gpid2 based on source/connecting germplasm
	protected void updatePedigreeConnections(Germplasm germplasm, Germplasm sourceGermplasm) {
		if(germplasm.getGnpgs() == -1){
			if(sourceGermplasm.getGpid1() == 0){
				germplasm.setGpid1(sourceGermplasm.getGid());
			}
			else{
				germplasm.setGpid1(sourceGermplasm.getGpid1());
			}
		}
		else{
			germplasm.setGpid1(sourceGermplasm.getGid());
		}
		
		germplasm.setGpid2(sourceGermplasm.getGid());
	}
	
	protected void performThirdPedigreeAction() {
		try{
		    Integer ibdbUserId = getIBDBUserId();
		    Integer dateIntValue = getGermplasmDateValue();     
		    
		    Map<String, Germplasm> createdGermplasms = new HashMap<String, Germplasm>();
		    
		    for(int i = 0 ; i < getImportedGermplasms().size(); i++){
		        
		        ImportedGermplasm importedGermplasm  = getImportedGermplasms().get(i);
		        int germplasmMatchesCount = (int) this.germplasmDataManager.countGermplasmByName(importedGermplasm.getDesig(), Operation.EQUAL);
		        
		        Germplasm germplasm = new Germplasm();
		        
		        boolean searchByNameOrNewGermplasmIsNeeded = true;
		        if(isGidSpecified(importedGermplasm)){
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
		        			NameNotExistsForGermplasmActionListener listener = new NameNotExistsForGermplasmActionListener(this);
		        			listener.setGid(importedGermplasm.getGid());
		        			listener.setDateIntValue(dateIntValue);
		        			listener.setIbdbUserId(ibdbUserId);
		        			listener.setGermplasmIndex(i);
		        			listener.setGermplasmName(importedGermplasm.getDesig());
		        			listener.setNameMatchesCount(germplasmMatchesCount);
		        			
		        			addImportEntryListener(listener);
		        		}
		        		
		        		searchByNameOrNewGermplasmIsNeeded = false;
		        	} else{
		        		MessageNotifier.showWarning(germplasmDetailsComponent.getWindow(), "Warning!", "GID: " + importedGermplasm.getGid() + " written on file does not exist in database."
		        				);
		        	}
		        } 
		        
		        if(germplasm == null){
		        	germplasm = new Germplasm();
		        }
		        
		        if(searchByNameOrNewGermplasmIsNeeded) {
		        	// gid at creation is temporary, will be set properly below
		        	germplasm = createGermplasmObject(i, 0, 0, 0, ibdbUserId, dateIntValue);
		        	
		        	if(germplasmMatchesCount==1 && germplasmDetailsComponent.automaticallyAcceptSingleMatchesCheckbox()){
		                //If a single match is found, multiple matches will be 
		                //   handled by SelectGemrplasmWindow and 
		                //   then receiveGermplasmFromWindowAndUpdateGermplasmData()
		                List<Germplasm> foundGermplasm = this.germplasmDataManager.getGermplasmByName(importedGermplasm.getDesig(), 0, 1, Operation.EQUAL);
		                germplasm.setGid(foundGermplasm.get(0).getGid());
		                doNotCreateGermplasmsWithId.add(foundGermplasm.get(0).getGid());
		            }
		        }

		        Name name = createNameObject(ibdbUserId, dateIntValue, importedGermplasm.getDesig());
		        
		        if(!createdGermplasms.containsKey(name.getNval())){
		        	createdGermplasms.put(name.getNval(), germplasm);
		        	
		        	germplasmNameObjects.add(new GermplasmName(germplasm,name));
		        } else {
		        	germplasmNameObjects.add(new GermplasmName(createdGermplasms.get(name.getNval()),name));
		        }
		        
		        if( ((germplasmMatchesCount>1) 
		        		|| (germplasmMatchesCount > 0 && !germplasmDetailsComponent.automaticallyAcceptSingleMatchesCheckbox())) 
		        		&& searchByNameOrNewGermplasmIsNeeded){
		        	displaySelectGermplasmWindowIfNecessary(importedGermplasm.getDesig(), i, germplasm);
		        }
		    }

		}catch (MiddlewareQueryException mqe){
		    LOG.error("Database error: " + mqe.getMessage(), mqe);
		}
	}


	protected boolean isGidSpecified(ImportedGermplasm importedGermplasm) {
		return importedGermplasm.getGid()!=null && !importedGermplasm.getGid().equals(Integer.valueOf(0));
	}


	public Name createNameObject(Integer ibdbUserId, Integer dateIntValue, String desig) {
		Name name = new Name();
		
		name.setTypeId((Integer)getGermplasmFieldsComponent().getNameTypeComboBox().getValue());
		name.setUserId(ibdbUserId);
		name.setNval(desig);
		name.setLocationId((Integer)getGermplasmFieldsComponent().getLocationComboBox().getValue());
		name.setNdate(dateIntValue);
		name.setReferenceId(0);
		
		return name;
	}


	protected Germplasm createGermplasmObject(Integer gid, Integer gnpgs, Integer gpid1, Integer gpid2, Integer ibdbUserId, Integer dateIntValue) throws MiddlewareQueryException {
		Germplasm germplasm = new Germplasm();
		
		germplasm.setGid(gid);
		germplasm.setUserId(ibdbUserId);
		germplasm.setLocationId((Integer)getGermplasmFieldsComponent().getLocationComboBox().getValue());
		germplasm.setGdate(dateIntValue);
		
		int methodId = getGermplasmMethodId(getGermplasmFieldsComponent().getBreedingMethodComboBox().getValue());
		germplasm.setMethodId(methodId);
		germplasm.setGnpgs(getGermplasmGnpgs(methodId,gnpgs));
		germplasm.setGpid1(gpid1);
		germplasm.setGpid2(gpid2);
		
		germplasm.setLgid(0);
		germplasm.setGrplce(0);
		germplasm.setReferenceId(0);
		germplasm.setMgid(0);
		
		return germplasm;
	}

	private int getGermplasmMethodId(Object methodValue){
		Integer methodId = 0;
		if(methodValue == null){
			methodId = UNKNOWN_DERIVATIVE_METHOD;
		}
		else{
			methodId = (Integer)methodValue;
		}
		return methodId;
	}
	
	private int getGermplasmGnpgs(Integer methodId, Integer prevGnpgs) throws MiddlewareQueryException {
		int gnpgs = 0;
		if(methodId == UNKNOWN_DERIVATIVE_METHOD){
			gnpgs= -1;
		}
		else{
			Method selectedMethod = germplasmDataManager.getMethodByID(methodId);
			if(selectedMethod.getMtype().equals("GEN")){
				gnpgs = 2;
			}
			else{
				gnpgs = prevGnpgs;
			}
		}
		return gnpgs;
	}


	protected Integer getGermplasmDateValue() {
		SimpleDateFormat formatter = new SimpleDateFormat(GermplasmImportMain.DATE_FORMAT);

		String sDate = "";
		Integer dateIntValue = 0;
		Date dateFieldValue = (Date) getGermplasmFieldsComponent().getGermplasmDateField().getValue();
		if(dateFieldValue!=null && !dateFieldValue.toString().equals("")){
			sDate = formatter.format(dateFieldValue);
			dateIntValue = Integer.parseInt(sDate.replace("-", ""));
		}
		return dateIntValue;
	}
	
	private SelectGermplasmWindow createSelectGermplasmWindow(String germplasmName, int i, Germplasm germplasm){
		return new SelectGermplasmWindow(this, germplasmName, i, germplasm, germplasmDetailsComponent.getViaToolURL());
	}
	
	private void displaySelectGermplasmWindowIfNecessary(String germplasmName, int i, Germplasm germplasm){
		SelectGermplasmWindow selectGermplasmWindow = createSelectGermplasmWindow(germplasmName, i, germplasm);
        addImportEntryListener(selectGermplasmWindow);
    }


	protected void addImportEntryListener(ImportGermplasmEntryActionListener listener) {
		if(importEntryListeners.isEmpty()) {
        	showImportEntryListener(listener);
        }
        importEntryListeners.add(listener);
	}
	
	 public void searchOrAddANewGermplasm(NameNotExistsForGermplasmActionListener listener) {
		 
		 int index = listener.getGermplasmIndex();
		 String desig = listener.getGermplasmName();
		 try{
			 Germplasm germplasm = createGermplasmObject(index, 0, 0, 0, 
					 listener.getIbdbUserId(), listener.getDateIntValue());
    	
			 if(listener.getNameMatchesCount()==1){
	            //If a single match is found, multiple matches will be 
	            //   handled by SelectGemrplasmWindow and 
	            //   then receiveGermplasmFromWindowAndUpdateGermplasmData()
				 List<Germplasm> foundGermplasm = this.germplasmDataManager.getGermplasmByName(desig, 0, 1, Operation.EQUAL);
		            
				 Integer gid = foundGermplasm.get(0).getGid();
				 germplasm.setGid(gid);
				 doNotCreateGermplasmsWithId.add(gid);
			 } 
	        
		 
			 if(listener.getNameMatchesCount()>1){
				 // force process the select germplasm window first for this entry before other entries
				 SelectGermplasmWindow window = createSelectGermplasmWindow(desig, index, germplasm);
				 this.importEntryListeners.add(0, window);
				 
			 } else{
				 germplasmNameObjects.get(index).setGermplasm(germplasm);
			 }
			 
			 removeCurrentListenerAndProcessNextItem(listener);
		 } catch(MiddlewareQueryException ex){
			 LOG.error("Error with getting germplasm by name = " + desig, ex);
			 return;
		 }
        
    }

	public void removeCurrentListenerAndProcessNextItem(ImportGermplasmEntryActionListener listener) {
		removeListener(listener);
		processNextItems();
	}
    
    public void closeAllImportEntryListeners(){
    	for(int i=0;i<importEntryListeners.size();i++){
    		ImportGermplasmEntryActionListener listener = importEntryListeners.get(i);
    		if (listener instanceof SelectGermplasmWindow){
    			SelectGermplasmWindow window = (SelectGermplasmWindow) listener;
    			getWindow().removeWindow(window);
    		}
    	}
    	importEntryListeners.clear();
    	germplasmDetailsComponent.closeSaveListAsDialog();
    	nameGermplasmMap.clear();
    }
    
    public void receiveGermplasmFromWindowAndUpdateGermplasmData(int index, Germplasm importedGermplasm, Germplasm selectedGermplasm) {
        String pedigreeOption = germplasmDetailsComponent.getPedigreeOption();
		if(pedigreeOption.equalsIgnoreCase("2")){
            //Update GPID 1 & 2 to values of selected germplasm, and update germplasmList using the updated germplasm
            updatePedigreeConnections(importedGermplasm, selectedGermplasm);
            
            germplasmNameObjects.get(index).setGermplasm(importedGermplasm);
            
        } else if(pedigreeOption.equalsIgnoreCase("3")){
            //Add logic here to not insert new record on DB when saved, maybe use existing GID?
            importedGermplasm.setGid(selectedGermplasm.getGid());
            doNotCreateGermplasmsWithId.add(selectedGermplasm.getGid());
            germplasmNameObjects.get(index).setGermplasm(importedGermplasm);
        }
    }


	public GermplasmFieldsComponent getGermplasmFieldsComponent() {
		return germplasmDetailsComponent.getGermplasmFieldsComponent();
	}


	public List<ImportedGermplasm> getImportedGermplasms() {
		return germplasmDetailsComponent.getImportedGermplasms();
	}
	
	public List<Integer> getMatchedGermplasmIds(){
		return doNotCreateGermplasmsWithId;
	}
	
	public List<GermplasmName> getGermplasmNameObjects(){
		return germplasmNameObjects;
	}
	
	public List<Name> getNewNames(){
		return newDesignationsForExistingGermplasm;
	}


	public Map<String, Germplasm> getNameGermplasmMap() {
		return nameGermplasmMap;
	}


	public void setNameGermplasmMap(Map<String, Germplasm> nameGermplasmMap) {
		this.nameGermplasmMap = nameGermplasmMap;
	}
	
	public void removeListener(ImportGermplasmEntryActionListener importEntryListener) {
		importEntryListeners.remove(importEntryListener);
	}

	public void processNextItems() {
		Iterator<ImportGermplasmEntryActionListener> listenersIterator = importEntryListeners.iterator();
		if(!listenersIterator.hasNext()) {
			saveImport();
			return;
		}
		ImportGermplasmEntryActionListener listener = listenersIterator.next();
		if (listener instanceof SelectGermplasmWindow){
			String germplasmName = listener.getGermplasmName();
			int germplasmIndex = listener.getGermplasmIndex();
			Germplasm germplasm = checkIfThereIsAMatch(germplasmName);
			if(germplasm != null) {
				nameGermplasmMap.get(germplasmName);
				germplasmNameObjects.get(germplasmIndex).setGermplasm(germplasm);
				removeListener(listener);
				processNextItems();
			} else {
				//If not from popup
				showImportEntryListener(listener);
			}
			
		// New Name confirm dialog
		} else {
			showImportEntryListener(listener);
		}
	}


	protected void showImportEntryListener(ImportGermplasmEntryActionListener listener) {
		if (listener instanceof SelectGermplasmWindow){
			showSelectGermplasmWindow(listener);
			
		} else if (listener instanceof NameNotExistsForGermplasmActionListener){
			NameNotExistsForGermplasmActionListener nameNotExistsListener = 
					(NameNotExistsForGermplasmActionListener) listener;
			
			ConfirmDialog.show(getWindow(), NameNotExistsForGermplasmActionListener.WINDOW_NAME
		            , nameNotExistsListener.getConfirmationMessage()
		            , NameNotExistsForGermplasmActionListener.ADD_NAME_TO_GID  //confirm option
		            , NameNotExistsForGermplasmActionListener.SEARCH_OR_CREATE_NEW  //the other option 
		            , nameNotExistsListener
			);
		}
		
	}


	protected void showSelectGermplasmWindow(ImportGermplasmEntryActionListener listener) {
		SelectGermplasmWindow window = (SelectGermplasmWindow) listener;
		getWindow().addWindow(window);
	}
	
	public Window getWindow(){
		if(germplasmDetailsComponent.getSource().getGermplasmImportPopupSource()==null){
			return germplasmDetailsComponent.getWindow();
		} else {
			return germplasmDetailsComponent.getSource().getGermplasmImportPopupSource().getParentWindow();
		}
	}
	
	public void ignoreRemainingMatches() {
		Iterator<ImportGermplasmEntryActionListener> listenersIterator = importEntryListeners.iterator();
		while(listenersIterator.hasNext()) {
			ImportGermplasmEntryActionListener listener = listenersIterator.next();
			if (listener instanceof SelectGermplasmWindow){
				String germplasmName = listener.getGermplasmName();
				int germplasmIndex = listener.getGermplasmIndex();
				Germplasm germplasm = checkIfThereIsAMatch(germplasmName);
				if(germplasm!=null) {
					germplasmNameObjects.get(germplasmIndex).setGermplasm(germplasm);
				}
				importEntryListeners.remove(listener);
			}
		}
//		importEntryListeners.clear();
		if (importEntryListeners.isEmpty()){
			saveImport();
		} else {
			processNextItems();
		}
	}

	public void saveImport() {
		germplasmDetailsComponent.popupSaveAsDialog();
	}


	public void mapGermplasmNamesToGermplasm(String germplasmName,
			Germplasm germplasm) {
		String nameInImportFile = germplasmName.toLowerCase();
		String standardizedNameInImportFile = GermplasmDataManagerUtil.standardizeName(nameInImportFile).toLowerCase();
		String nameInImportFileWithSpacesRemoved = GermplasmDataManagerUtil.removeSpaces(nameInImportFile).toLowerCase();
		
		nameGermplasmMap.put(nameInImportFile,germplasm);
		nameGermplasmMap.put(standardizedNameInImportFile,germplasm);
		nameGermplasmMap.put(nameInImportFileWithSpacesRemoved,germplasm);
	}
	
	public Germplasm checkIfThereIsAMatch(String germplasmName) {
		if(nameGermplasmMap==null || nameGermplasmMap.isEmpty()) {
			return null;
		}
		String nameInImportFile = germplasmName.toLowerCase();
		if(nameGermplasmMap.containsKey(nameInImportFile)) {
			return nameGermplasmMap.get(nameInImportFile);
		}
		String standardizedNameInImportFile = GermplasmDataManagerUtil.standardizeName(nameInImportFile).toLowerCase();
		if(nameGermplasmMap.containsKey(standardizedNameInImportFile)) {
			return nameGermplasmMap.get(standardizedNameInImportFile);
		}
		String nameInImportFileWithSpacesRemoved = GermplasmDataManagerUtil.removeSpaces(nameInImportFile).toLowerCase();
		if(nameGermplasmMap.containsKey(nameInImportFileWithSpacesRemoved)) {
			return nameGermplasmMap.get(nameInImportFileWithSpacesRemoved);
		}
		return null;
	}
	
	public void addNameToGermplasm(Name name, Integer gid){
		doNotCreateGermplasmsWithId.add(gid);
		newDesignationsForExistingGermplasm.add(name);
	}
	
	

}
