package org.generationcp.breeding.manager.listimport.actions;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.breeding.manager.pojos.ImportedVariate;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.ims.EntityType;
import org.generationcp.middleware.pojos.ims.Lot;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;
import org.generationcp.middleware.util.Util;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Created with IntelliJ IDEA.
 * User: Efficio.Daniel
 * Date: 8/20/13
 * Time: 1:39 PM
 * To change this template use File | Settings | File Templates.
 */
@Configurable
public class SaveGermplasmListAction  implements Serializable, InitializingBean {


	private static final String INVENTORY_COMMENT = "From List Import";
	public static final String WB_ACTIVITY_NAME = "Imported a Germplasm List";
    public static final String WB_ACTIVITY_DESCRIPTION = "Imported list from file ";
    public static final Integer LIST_DATA_STATUS = 0;
    public static final Integer LIST_DATA_LRECID = 0;

    private static final long serialVersionUID = -6273933938066390358L;

    @Autowired
    private GermplasmListManager germplasmListManager;

    @Autowired
    private GermplasmDataManager germplasmManager;

    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private InventoryDataManager inventoryDataManager;
    
    @Autowired
    private OntologyDataManager ontologyDataManager;    

    private Integer wbUserId;
    private Project project;
    private Integer ibdbUserId;

    //Lot related variables
    private Integer seedAmountScaleId;

    private Map<Integer, Lot> gidLotMap;
    private Map<Integer, List<Transaction>> gidTransactionSetMap;
    

    public SaveGermplasmListAction(){
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    /**
     * Saves records in Germplasm, GermplasmList and GermplasmListData,
     * ProjectActivity (Workbench).
     *
     * @param crossesMade where crosses information is defined
     * @return id of new Germplasm List created
     * @throws MiddlewareQueryException
     */
    public Integer saveRecords(GermplasmList germplasmList, List<GermplasmName> germplasmNameObjects, List<Name> newNames, String filename
    		, List<Integer> doNotCreateGermplasmsWithId, ImportedGermplasmList importedGermplasmList, Integer seedStorageLocation)throws MiddlewareQueryException{

        retrieveIbdbUserId();
        germplasmList.setUserId(ibdbUserId);

        //Retieve seed stock variable and/or attribute types (or create new one) as needed
        processVariates(importedGermplasmList);
        
        List<Integer> germplasmIds = new ArrayList<Integer>();
        Map<Integer, GermplasmName> addedGermplasmNameMap = new HashMap<Integer, GermplasmName>();
        gidLotMap = new HashMap<Integer, Lot>();
        gidTransactionSetMap = new HashMap<Integer, List<Transaction>>();
        
        processGermplasmNamesAndLots(germplasmNameObjects, doNotCreateGermplasmsWithId, germplasmIds,
				addedGermplasmNameMap,seedStorageLocation);
        
        List<ImportedGermplasm> importedGermplasms = importedGermplasmList.getImportedGermplasms();
        GermplasmList list = saveGermplasmListRecord(germplasmList);
        saveGermplasmListDataRecords(germplasmNameObjects, germplasmIds, list, filename, importedGermplasms);
        addNewNamesToExistingGermplasm(newNames);
        
        saveInventory();
        
        // log project activity in Workbench
        addWorkbenchProjectActivity(filename);

        return list.getId();
    }

	protected void saveInventory() throws MiddlewareQueryException {
		for(Map.Entry<Integer, Lot> item: gidLotMap.entrySet()){
        	Integer gid = item.getKey();
        	Lot lot = item.getValue();
        	
        	List<Transaction> listOfTransactions = gidTransactionSetMap.get(gid);
        	
        	if(listOfTransactions!=null && listOfTransactions.size()>0){
            	inventoryDataManager.addLot(lot);
        		inventoryDataManager.addTransactions(listOfTransactions);
        	}
        }
	}

	protected void processGermplasmNamesAndLots(
			List<GermplasmName> germplasmNameObjects,
			List<Integer> doNotCreateGermplasmsWithId,
			List<Integer> germplasmIds,
			Map<Integer, GermplasmName> addedGermplasmNameMap,
			Integer seedStorageLocation)
			throws MiddlewareQueryException {
		for(GermplasmName germplasmName : germplasmNameObjects){
            Name name = germplasmName.getName();
            name.setNid(null);
            name.setNstat(Integer.valueOf(1));
            
            Integer gid = null;
            Germplasm germplasm;
            
            if(doNotCreateGermplasmsWithId.contains(germplasmName.getGermplasm().getGid()) || germplasmIds.contains(germplasmName.getGermplasm().getGid())){
                //If do not create new germplasm
            	gid = germplasmName.getGermplasm().getGid();
            	germplasm = germplasmManager.getGermplasmByGID(gid);
            	germplasmName.setGermplasm(germplasm);
                germplasmIds.add(gid);
                name.setGermplasmId(gid);
            } else {
            	Germplasm addedGermplasmMatch = getAlreadyAddedGermplasm(germplasmName, addedGermplasmNameMap);
        		germplasmName.getGermplasm().setLgid(Integer.valueOf(0));
            	//not yet added, create new germplasm record
            	if(addedGermplasmMatch==null){
            		germplasm = germplasmName.getGermplasm();
            		germplasmName.getGermplasm().setGid(null);
            		gid = germplasmManager.addGermplasm(germplasm, name);
                    germplasmIds.add(gid);
            		addedGermplasmNameMap.put(germplasmName.getGermplasm().getGid(), germplasmName);
            	//if already addded (re-use that one)
            	} else {
            		germplasm = addedGermplasmMatch;
            		germplasmName.setGermplasm(addedGermplasmMatch);
            		gid = addedGermplasmMatch.getGid();
            	}
            }
            
            if(seedAmountScaleId!=null){
            	Lot lot = new Lot(null, ibdbUserId, EntityType.GERMPLSM.name(), gid, seedStorageLocation, seedAmountScaleId, 0, 0, INVENTORY_COMMENT);
            	gidLotMap.put(gid, lot);
            }
        }
	}

	protected void processVariates(ImportedGermplasmList importedGermplasmList) throws MiddlewareQueryException {
		List<UserDefinedField> existingUdflds = getUserDefinedFields();
        List<UserDefinedField> newUdflds = new ArrayList<UserDefinedField>();
        
		for(ImportedVariate importedVariate : importedGermplasmList.getImportedVariates()){
			String property = importedVariate.getProperty();
			
	    	if(importedVariate.isSeedStockVariable()){
	    		processSeedStockVariate(importedVariate);
	    	}
	    	else{
	    		if(!isUdfldsExist(existingUdflds, property)){
	    			UserDefinedField newUdfld = createNewUserDefinedField(importedVariate);
	    			newUdflds.add(newUdfld);
	    		}
	    	}
        }
		
        //Add All UDFLDS
        germplasmManager.addUserDefinedFields(newUdflds);
	}
	
	private UserDefinedField createNewUserDefinedField(ImportedVariate importedVariate){
		UserDefinedField newUdfld = new UserDefinedField();
		newUdfld.setFtable("ATRIBUTS");
		newUdfld.setFtype("ATTRIBUTE");
		newUdfld.setFcode(importedVariate.getProperty());
		newUdfld.setFname(importedVariate.getDescription());
		String fmt = importedVariate.getScale() + "," + importedVariate.getMethod() + "," + importedVariate.getDataType();
		newUdfld.setFfmt(fmt);
		newUdfld.setFdesc("-");
		newUdfld.setLfldno(0);
		newUdfld.setUser(new User(ibdbUserId));
		newUdfld.setFdate(Util.getCurrentDate());
		newUdfld.setScaleid(0);
		
		return newUdfld;
	}

	protected void processSeedStockVariate(ImportedVariate importedVariate)
			throws MiddlewareQueryException {
		String trait = importedVariate.getProperty().toUpperCase();
		String scale = importedVariate.getScale().toUpperCase();
		String method = importedVariate.getMethod().toUpperCase();
		
		StandardVariable stdVariable = ontologyDataManager.findStandardVariableByTraitScaleMethodNames(trait, scale, method);
		// create new variate if PSMR doesn't exist
		if (stdVariable == null || stdVariable.getStoredIn().getId() != TermId.OBSERVATION_VARIATE.getId()){
			
			Term traitTerm = ontologyDataManager.findTermByName(trait, CvId.PROPERTIES);
			if (traitTerm == null){
				traitTerm = new Term();
				traitTerm.setName(trait);
				traitTerm.setDefinition(trait);
				
			}
			
			Term scaleTerm = ontologyDataManager.findTermByName(scale, CvId.SCALES);
			if (scaleTerm == null){
				scaleTerm = new Term();
				scaleTerm.setName(scale);
				scaleTerm.setDefinition(scale);
			}
			
			Term methodTerm = ontologyDataManager.findTermByName(method, CvId.METHODS);
			if (methodTerm == null){
				methodTerm = new Term();
				methodTerm.setName(method);
				methodTerm.setDefinition(method);
			}
			
			Term dataType = new Term();
			dataType.setId("N".equals(importedVariate.getDataType())? TermId.NUMERIC_VARIABLE.getId() : TermId.CHARACTER_VARIABLE.getId());
			
			Term storedIn = new Term();
			storedIn.setId(TermId.OBSERVATION_VARIATE.getId());
			
			stdVariable = new StandardVariable(traitTerm, scaleTerm, methodTerm, dataType, storedIn, null, 
					PhenotypicType.VARIATE, null, null);
			stdVariable.setName(importedVariate.getVariate());
			stdVariable.setDescription(importedVariate.getDescription());
			
			ontologyDataManager.addStandardVariable(stdVariable);
		}

		if (stdVariable.getId() != 0){
			importedVariate.setScaleId(stdVariable.getId());
			seedAmountScaleId = importedVariate.getScaleId();
			
		}
    }

	private boolean isUdfldsExist(List<UserDefinedField> existingUdflds, String property) {
		for(UserDefinedField udfld : existingUdflds){
			if(udfld.getFcode().equals(property)){
				return true;
			}
		}
		return false;
	}
	
	private Integer getUdfldID(List<UserDefinedField> existingUdflds, String property) {
		for(UserDefinedField udfld : existingUdflds){
			if(udfld.getFcode().toUpperCase().equals(property.toUpperCase())){
				return udfld.getFldno();
			}
		}
		return 0;
	}

	protected void addNewNamesToExistingGermplasm(List<Name> newNames)
			throws MiddlewareQueryException {
		for (Name name: newNames){
        	germplasmManager.addGermplasmName(name);
        }
	}

    private Germplasm getAlreadyAddedGermplasm(GermplasmName germplasmName, Map<Integer, GermplasmName> addedGermplasmNameMap){
    	Germplasm germplasm = germplasmName.getGermplasm();
    	for(Integer gid : addedGermplasmNameMap.keySet()){
    		if(addedGermplasmNameMap.get(gid).getGermplasm().getGpid1().equals(germplasm.getGpid1()) 
        			&& addedGermplasmNameMap.get(gid).getGermplasm().getGpid2().equals(germplasm.getGpid2())
        			&& addedGermplasmNameMap.get(gid).getName().getNval().equals(germplasmName.getName().getNval())
    		  ){
    			return addedGermplasmNameMap.get(gid).getGermplasm();
    		}
    	}
    	return null;
    }
    
    private GermplasmList saveGermplasmListRecord(GermplasmList germplasmList) throws MiddlewareQueryException {
        int newListId = this.germplasmListManager.addGermplasmList(germplasmList);
        GermplasmList list = this.germplasmListManager.getGermplasmListById(newListId);

        return list;
    }
    
    private List<UserDefinedField> getUserDefinedFields() throws MiddlewareQueryException{
    	List<UserDefinedField> existingUdflds = germplasmManager.getUserDefinedFieldByFieldTableNameAndType("ATRIBUTS", "ATTRIBUTE");
    	return existingUdflds;
    }

    private void saveGermplasmListDataRecords( List<GermplasmName> germplasmNameObjects,
        List<Integer> germplasmIds, GermplasmList list, String filename, List<ImportedGermplasm> importedGermplasms) throws MiddlewareQueryException {
    	
        List<GermplasmListData> listToSave = new ArrayList<GermplasmListData>();
        List<UserDefinedField> existingUdflds = getUserDefinedFields();
        List<Attribute> attrs = new ArrayList<Attribute>();
        
        int ctr = 1;
        for (GermplasmName germplasmName : germplasmNameObjects){
        	
            int entryId = ctr++;
            ImportedGermplasm importedGermplasm = importedGermplasms.get(entryId - 1);
            Integer gid = germplasmName.getGermplasm().getGid(); //germplasmIdIterator.next();
            
            String designation = germplasmName.getName().getNval();
            
            String source = filename + ":" +entryId;
            if(importedGermplasm.getSource() != null && importedGermplasm.getSource().length() > 0){
            	source = importedGermplasm.getSource();
            }

            String groupName = "-";
            if(importedGermplasm.getCross() != null && importedGermplasm.getCross().length() > 0){
            	groupName = importedGermplasm.getCross();
            }
            
            String entryCode = String.valueOf(entryId);
            if(importedGermplasm.getEntryCode() != null && importedGermplasm.getEntryCode().length() > 0){
            	entryCode = importedGermplasm.getEntryCode();
            }

            GermplasmListData germplasmListData = buildGermplasmListData(
                list, gid, entryId, designation, groupName, source, entryCode);

            listToSave.add(germplasmListData);
            Integer lrecId = germplasmListManager.addGermplasmListData(germplasmListData);
            
            createDepositInventoryTransaction(list, importedGermplasm, gid,	lrecId);
            
            if(importedGermplasm.getAttributeVariates().size() > 0){
            	attrs.addAll(prepareAllAttributesToAdd(importedGermplasm, existingUdflds, germplasmName.getGermplasm()));
            }
            
        }
        
        if(attrs.size() > 0){
            //Add All Attributes to database
            germplasmManager.addAttributes(attrs);
        }
    }

	protected void createDepositInventoryTransaction(GermplasmList list,
			ImportedGermplasm importedGermplasm, Integer gid, Integer lrecId) {
		if(importedGermplasm!=null && importedGermplasm.getSeedAmount()!=null && importedGermplasm.getSeedAmount()>0){
			
			if(gidTransactionSetMap.get(gid)==null){
				gidTransactionSetMap.put(gid, new ArrayList<Transaction>());
			}
			
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");  
			Date date = new Date(); 
			Integer intDate = Integer.valueOf(df.format(date));
			
			Transaction transaction = new Transaction(null, 
					wbUserId, gidLotMap.get(gid), intDate, TransactionStatus.DEPOSITED.getIntValue(), importedGermplasm.getSeedAmount(), 
					INVENTORY_COMMENT, 0, "LIST", list.getId(), lrecId, Double.valueOf(0), 0);
			if(importedGermplasm.getSeedAmount()!=null)
				gidTransactionSetMap.get(gid).add(transaction);
		}
	}
	
	   
    private List<Attribute> prepareAllAttributesToAdd(ImportedGermplasm importedGermplasm, 
    		List<UserDefinedField> existingUdflds, Germplasm germplasm) {
    	List<Attribute> attrs = new ArrayList<Attribute>();
    	
    	Map<String, String> otherAttributes = importedGermplasm.getAttributeVariates();
    	for(Map.Entry<String, String> entry : otherAttributes.entrySet()){
    		String property = entry.getKey();
    		String value = entry.getValue();
    		
    		if(value!= null && !value.trim().equals("")){
    			//Create New Attribute Object
                Attribute newAttr = new Attribute();
                newAttr.setGermplasmId(germplasm.getGid());
                newAttr.setTypeId(getUdfldID(existingUdflds,property));
                newAttr.setUserId(ibdbUserId);
                newAttr.setAval(value);
                newAttr.setLocationId(germplasm.getLocationId());
                newAttr.setReferenceId(0);
                newAttr.setAdate(Util.getCurrentDate());
                
                attrs.add(newAttr);
    		}
    	}
    	
    	return attrs;
	}

	private GermplasmListData buildGermplasmListData(GermplasmList list, Integer gid, int entryId,
            String designation, String groupName, String source, String entryCode) {
        GermplasmListData germplasmListData = new GermplasmListData();
        germplasmListData.setList(list);
        germplasmListData.setGid(gid);
        germplasmListData.setEntryId(entryId);
        germplasmListData.setEntryCode(entryCode);
        germplasmListData.setSeedSource(source);
        germplasmListData.setDesignation(designation);
        germplasmListData.setStatus(LIST_DATA_STATUS);
        germplasmListData.setGroupName(groupName);
        germplasmListData.setLocalRecordId(entryId);

        return germplasmListData;
    }

    private void retrieveIbdbUserId() throws MiddlewareQueryException {
        this.wbUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
        this.project = workbenchDataManager.getLastOpenedProject(wbUserId);
        this.ibdbUserId = workbenchDataManager.getLocalIbdbUserId(wbUserId, this.project.getProjectId());
    }

    /*
     * Adds a ProjectActivity record in Workbench for creating the GermplasmList through
     * Crossing Manager tool
     *
     * @param listId if of GermplasmList created
     * @throws MiddlewareQueryException
     */
    private void addWorkbenchProjectActivity(String filename) throws MiddlewareQueryException{
        User user = workbenchDataManager.getUserById(this.wbUserId);
        ProjectActivity activity = new ProjectActivity(project.getProjectId().intValue(), project,
            WB_ACTIVITY_NAME, WB_ACTIVITY_DESCRIPTION + filename, user, new Date());

        workbenchDataManager.addProjectActivity(activity);
    }
}
