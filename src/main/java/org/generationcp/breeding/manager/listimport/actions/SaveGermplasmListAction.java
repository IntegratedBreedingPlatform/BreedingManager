
package org.generationcp.breeding.manager.listimport.actions;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.exception.BreedingManagerException;
import org.generationcp.breeding.manager.listmanager.util.ListCommonActionsUtil;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedFactor;
import org.generationcp.commons.parsing.pojo.ImportedVariate;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
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
import org.generationcp.middleware.util.Util;
import org.jfree.util.Log;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Configurable
public class SaveGermplasmListAction implements Serializable, InitializingBean {

	private static final String INVENTORY_COMMENT = "From List Import";
	public static final String WB_ACTIVITY_NAME = "Imported a Germplasm List";
	public static final String WB_ACTIVITY_DESCRIPTION = "Imported list from file ";
	public static final Integer LIST_DATA_STATUS = 0;
	public static final Integer LIST_DATA_LRECID = 0;

	public static final int FCODE_TYPE_NAME = 0;
	private static final int FCODE_TYPE_ATTRIBUTE = 1;

	private static final String FTABLE_NAME = "NAMES";
	private static final String FTYPE_NAME = "NAME";
	private static final String FTABLE_ATTRIBUTE = "ATRIBUTS";
	private static final String FTYPE_ATTRIBUTE = "ATTRIBUTE";
	private static final String FTYPE_PASSPORT = "PASSPORT";

	private static final long serialVersionUID = -6273933938066390358L;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private GermplasmDataManager germplasmManager;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private UserDataManager userDataManager;

	@Resource
	private ContextUtil contextUtil;

	// Lot related variables
	private Integer seedAmountScaleId;

	private final Map<Integer, Lot> gidLotMap;
	private final Map<Integer, List<Transaction>> gidTransactionSetMap;

	public SaveGermplasmListAction() {
		this.gidLotMap = new HashMap<Integer, Lot>();
		this.gidTransactionSetMap = new HashMap<Integer, List<Transaction>>();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// do nothing
	}

	/**
	 * Saves records in Germplasm, GermplasmList and GermplasmListData, ProjectActivity (Workbench).
	 * 
	 * @param germplasmList
	 * @param germplasmNameObjects
	 * @param newNames
	 * @param filename
	 * @param doNotCreateGermplasmWithId
	 * @param importedGermplasmList
	 * @param seedStorageLocation
	 * @return id of new Germplasm List created @
	 * @throws BreedingManagerException
	 */
	public Integer saveRecords(final GermplasmList germplasmList, final List<GermplasmName> germplasmNameObjects, final List<Name> newNames,
			final String filename, final List<Integer> doNotCreateGermplasmWithId, final ImportedGermplasmList importedGermplasmList,
			final Integer seedStorageLocation) throws BreedingManagerException {

		germplasmList.setUserId(this.contextUtil.getCurrentUserLocalId());
		germplasmList.setProgramUUID(this.contextUtil.getCurrentProgramUUID());

		// Retrieve seed stock variable and/or attribute types (or create new one) as needed
		this.processVariates(importedGermplasmList);

		// Create new udfld records as needed
		this.processFactors(importedGermplasmList);

		this.gidLotMap.clear();
		this.gidTransactionSetMap.clear();
		this.processGermplasmNamesAndLots(germplasmNameObjects, doNotCreateGermplasmWithId, seedStorageLocation);

		final List<ImportedGermplasm> importedGermplasm = importedGermplasmList.getImportedGermplasm();

		final GermplasmList list = this.saveGermplasmListRecord(germplasmList);

		// mark the existing entries of the list deleted before adding the new entries from germplasm import
		final Integer existingListId = germplasmList.getId();
		if (existingListId != null) {
			ListCommonActionsUtil.deleteExistingListEntries(existingListId, this.germplasmListManager);
		}

		this.saveGermplasmListDataRecords(germplasmNameObjects, list, importedGermplasm, doNotCreateGermplasmWithId);
		
		if(!newNames.isEmpty()){
			//save the names under the designation column.
			this.germplasmManager.addGermplasmName(newNames);
		}

		if (importedGermplasmList.isSetImportedNameAsPreferredName()) {
			this.updateExportedGermplasmPreferredName(importedGermplasmList.getPreferredNameCode(),
					importedGermplasmList.getImportedGermplasm());
		}

		this.saveInventory();

		// log project activity in Workbench
		this.contextUtil.logProgramActivity(SaveGermplasmListAction.WB_ACTIVITY_NAME,
				SaveGermplasmListAction.WB_ACTIVITY_DESCRIPTION + filename);

		return list.getId();
	}

	/**
	 * Update the preferred name of the imported germplasm using the selected name type from the Name Handling Dialog
	 * 
	 * @param preferredNameCode
	 * @param importedGermplasmList
	 */
	void updateExportedGermplasmPreferredName(final String preferredNameCode, final List<ImportedGermplasm> importedGermplasmList) {
		for (final ImportedGermplasm importedGermplasm : importedGermplasmList) {
			final String newPreferredName = importedGermplasm.getNameFactors().get(preferredNameCode);
			if (newPreferredName != null && newPreferredName.trim().length() > 0) {
				this.germplasmManager.updateGermplasmPrefName(importedGermplasm.getGid(), newPreferredName);
			}
		}
	}

	void saveInventory() {
		final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final org.springframework.transaction.TransactionStatus status) {
				for (final Map.Entry<Integer, Lot> item : SaveGermplasmListAction.this.gidLotMap.entrySet()) {
					final Integer gid = item.getKey();
					final List<Transaction> listOfTransactions = SaveGermplasmListAction.this.gidTransactionSetMap.get(gid);
					if (listOfTransactions == null || listOfTransactions.isEmpty()) {
						continue;
					}
					final Lot lot = item.getValue();

					SaveGermplasmListAction.this.inventoryDataManager.addLot(lot);

					SaveGermplasmListAction.this.inventoryDataManager.addTransactions(listOfTransactions);
				}
			}
		});
	}

	protected void processGermplasmNamesAndLots(final List<GermplasmName> germplasmNameObjects,
			final List<Integer> excludeGermplasmCreateIds, final Integer seedStorageLocation) {

		final Map<Integer, GermplasmName> addedGermplasmNameMap = new HashMap<Integer, GermplasmName>();

		for (final GermplasmName germplasmName : germplasmNameObjects) {
			final Name name = germplasmName.getName();
			name.setNid(null);
			// Nstat = name status
			name.setNstat(Integer.valueOf(1));
			Integer gid = null;
			final Germplasm germplasm;

			if (excludeGermplasmCreateIds.contains(germplasmName.getGermplasm().getGid())) {
				// If germplasm record should not be created
				gid = germplasmName.getGermplasm().getGid();
				germplasm = this.germplasmManager.getGermplasmByGID(gid);
				germplasmName.setGermplasm(germplasm);
				name.setGermplasmId(gid);
			} else {
				// create germplasm record
				// a local GID of zero reflects no previous known GID from other systems
				germplasmName.getGermplasm().setLgid(Integer.valueOf(0));

				final Germplasm addedGermplasmMatch = this.getAlreadyAddedGermplasm(germplasmName, addedGermplasmNameMap);
				if(addedGermplasmMatch != null){
					//we have an existing record
					germplasm = addedGermplasmMatch;
					germplasmName.setGermplasm(addedGermplasmMatch);
					gid = addedGermplasmMatch.getGid();
				}
				else {
					// not yet added, create new germplasm record
					germplasm = germplasmName.getGermplasm();
					germplasmName.getGermplasm().setGid(null);
					if (germplasm.getGdate().equals(Integer.valueOf(0))) {
						final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
						final Date today = new Date();
						germplasm.setGdate(Integer.valueOf(dateFormat.format(today)));
					}
					gid = this.germplasmManager.addGermplasm(germplasm, name);
					addedGermplasmNameMap.put(germplasmName.getGermplasm().getGid(), germplasmName);
				} 
			}
			
			// process inventory
			if (this.seedAmountScaleId != null) {
				final Lot lot = new Lot(null, this.contextUtil.getCurrentUserLocalId(), EntityType.GERMPLSM.name(), gid,
						seedStorageLocation, this.seedAmountScaleId, 0, 0, SaveGermplasmListAction.INVENTORY_COMMENT);
				this.gidLotMap.put(gid, lot);
			}
		}
	}

	protected void processVariates(final ImportedGermplasmList importedGermplasmList) throws BreedingManagerException {
		final List<UserDefinedField> existingUdflds = this.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_ATTRIBUTE);
		final List<UserDefinedField> newUdflds = new ArrayList<UserDefinedField>();
		final Map<String, String> attributeVariates = importedGermplasmList.getImportedGermplasm().get(0).getAttributeVariates();

		for (final ImportedVariate importedVariate : importedGermplasmList.getImportedVariates()) {
			// GCP-10077: use variate name, instead of the property
			final String variate = importedVariate.getName();
			if (importedVariate.isSeedStockVariable()) {
				this.processSeedStockVariate(importedVariate);
			} else {
				if (attributeVariates.containsKey(variate) && !this.isUdfldsExist(existingUdflds, variate)) {
					final UserDefinedField newUdfld = this.createNewUserDefinedField(importedVariate);
					newUdflds.add(newUdfld);
				}
			}
		}

		// Add All UDFLDS
		this.germplasmManager.addUserDefinedFields(newUdflds);
	}

	protected void processFactors(final ImportedGermplasmList importedGermplasmList) {
		final List<UserDefinedField> existingUdflds = this.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_NAME);
		final List<UserDefinedField> newUdflds = new ArrayList<UserDefinedField>();
		final Map<String, String> nameFactors = importedGermplasmList.getImportedGermplasm().get(0).getNameFactors();

		for (final ImportedFactor importedFactor : importedGermplasmList.getImportedFactors()) {
			final String factor = importedFactor.getName();
			if (nameFactors.containsKey(factor) && !this.isUdfldsExist(existingUdflds, factor)) {
				final UserDefinedField newUdfld = this.createNewUserDefinedField(importedFactor);
				newUdflds.add(newUdfld);
			}
		}

		// Add All UDFLDS
		this.germplasmManager.addUserDefinedFields(newUdflds);
	}

	private UserDefinedField createNewUserDefinedField(final ImportedVariate importedVariate) {
		final UserDefinedField newUdfld = new UserDefinedField();
		newUdfld.setFtable(SaveGermplasmListAction.FTABLE_ATTRIBUTE);
		newUdfld.setFtype(importedVariate.getProperty().toUpperCase());
		// GCP-10077 - use name instead of property
		newUdfld.setFcode(importedVariate.getName());
		newUdfld.setFname(importedVariate.getDescription());
		final String fmt = importedVariate.getScale() + "," + importedVariate.getMethod() + "," + importedVariate.getDataType();
		newUdfld.setFfmt(fmt);
		newUdfld.setFdesc("-");
		newUdfld.setLfldno(0);
		newUdfld.setFuid(this.contextUtil.getCurrentUserLocalId());
		newUdfld.setFdate(Util.getCurrentDateAsIntegerValue());
		newUdfld.setScaleid(0);

		return newUdfld;
	}

	private UserDefinedField createNewUserDefinedField(final ImportedFactor importedFactor) {
		final UserDefinedField newUdfld = new UserDefinedField();
		newUdfld.setFtable(SaveGermplasmListAction.FTABLE_NAME);
		newUdfld.setFtype(SaveGermplasmListAction.FTYPE_NAME);
		newUdfld.setFcode(importedFactor.getName());
		newUdfld.setFname(importedFactor.getDescription());
		final String fmt = importedFactor.getScale() + "," + importedFactor.getMethod() + "," + importedFactor.getDataType();
		newUdfld.setFfmt(fmt);
		newUdfld.setFdesc("-");
		newUdfld.setLfldno(0);
		newUdfld.setFuid(this.contextUtil.getCurrentUserLocalId());
		newUdfld.setFdate(Util.getCurrentDateAsIntegerValue());
		newUdfld.setScaleid(0);

		return newUdfld;
	}

	protected void processSeedStockVariate(final ImportedVariate importedVariate) throws BreedingManagerException {

		// find stock variable via name at top of column in the sheet - should be one
		final Set<StandardVariable> terms = this.ontologyDataManager.findStandardVariablesByNameOrSynonym(importedVariate.getName(),
				this.contextUtil.getCurrentProgramUUID());
		if (terms.size() == 1) {
			// ok to get only record with the size check
			final StandardVariable stdVariable = new ArrayList<>(terms).get(0);
			importedVariate.setScaleId(stdVariable.getId());
			this.seedAmountScaleId = importedVariate.getScaleId();
		} else {
			// TODO
			// sorry non-i18N message
			throw new BreedingManagerException("The BMS does not contain a Variable called " + importedVariate.getName()
					+ ". Please create it in the Ontology Manager or change your import sheet.");
		}

	}

	private boolean isUdfldsExist(final List<UserDefinedField> existingUdflds, final String fcode) {
		for (final UserDefinedField udfld : existingUdflds) {
			if (udfld.getFcode().equals(fcode)) {
				return true;
			}
		}
		return false;
	}

	private Integer getUdfldID(final List<UserDefinedField> existingUdflds, final String property) {
		if (existingUdflds != null) {
			for (final UserDefinedField udfld : existingUdflds) {
				if (udfld.getFcode().equalsIgnoreCase(property.toUpperCase())) {
					return udfld.getFldno();
				}
			}
		}
		return 0;
	}

	private Germplasm getAlreadyAddedGermplasm(final GermplasmName germplasmName, final Map<Integer, GermplasmName> addedGermplasmNameMap) {
		final Germplasm germplasm = germplasmName.getGermplasm();
		for (final Integer gid : addedGermplasmNameMap.keySet()) {
			final Germplasm addedGermplasm = addedGermplasmNameMap.get(gid).getGermplasm();
			if (addedGermplasm.getGpid1().equals(germplasm.getGpid1()) && addedGermplasm.getGpid2().equals(germplasm.getGpid2())
					&& addedGermplasmNameMap.get(gid).getName().getNval().equals(germplasmName.getName().getNval())) {
				return addedGermplasm;
			}
		}
		return null;
	}

	private GermplasmList saveGermplasmListRecord(final GermplasmList germplasmList) {
		final int newListId = this.germplasmListManager.addGermplasmList(germplasmList);
		return this.germplasmListManager.getGermplasmListById(newListId);
	}

	public List<UserDefinedField> getUserDefinedFields(final int fcodeType) {
		List<UserDefinedField> udFields = new ArrayList<UserDefinedField>();
		if (SaveGermplasmListAction.FCODE_TYPE_ATTRIBUTE == fcodeType) {
			final List<UserDefinedField> list = this.germplasmManager.getUserDefinedFieldByFieldTableNameAndType(
					SaveGermplasmListAction.FTABLE_ATTRIBUTE, SaveGermplasmListAction.FTYPE_ATTRIBUTE);
			final List<UserDefinedField> list2 = this.germplasmManager.getUserDefinedFieldByFieldTableNameAndType(
					SaveGermplasmListAction.FTABLE_ATTRIBUTE, SaveGermplasmListAction.FTYPE_PASSPORT);
			if (list != null && !list.isEmpty()) {
				udFields.addAll(list);
			}
			if (list2 != null && !list2.isEmpty()) {
				udFields.addAll(list2);
			}
		} else if (SaveGermplasmListAction.FCODE_TYPE_NAME == fcodeType) {
			udFields = this.germplasmManager.getUserDefinedFieldByFieldTableNameAndType(SaveGermplasmListAction.FTABLE_NAME,
					SaveGermplasmListAction.FTYPE_NAME);
		}
		return udFields;
	}
	
	/**
	 * Saves a list of germplasm and assocuated meta-data to the system. Also processes Inventory, new Attributes and new Names
	 * 
	 * @param germplasmNameObjects : germplasm names
	 * @param list
	 * @param importedGermplasmList : the data that is imported via spreadsheet
	 * @param excludeGermplasmCreateIds : the GIDs for which a new germplasm record should not be created
	 */
	public void saveGermplasmListDataRecords(final List<GermplasmName> germplasmNameObjects, final GermplasmList list,
			final List<ImportedGermplasm> importedGermplasmList, final List<Integer> excludeGermplasmCreateIds) {
		
		// create a map of GIDs to names, which we use to add new names to the system
		final List<UserDefinedField> existingNameUdflds = this.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_NAME);
		final Map<Integer, List<Name>> namesMap = this.getNamesMap(importedGermplasmList, excludeGermplasmCreateIds, existingNameUdflds);
		
		// set up names, attributes, and germplasmlistdata collections to collect from the imported data rows and then persist
		final List<Name> names = new ArrayList<Name>();
		final List<Attribute> attrs = new ArrayList<Attribute>();
		final List<GermplasmListData> germplasmListDataList = new ArrayList<GermplasmListData>();
		
		// iterate through the imported names to process
		for (int ctr = 0; ctr < germplasmNameObjects.size(); ctr++) {
			final GermplasmName germplasmName = germplasmNameObjects.get(ctr);
			final Integer gid = germplasmName.getGermplasm().getGid();
			final ImportedGermplasm importedGermplasm = importedGermplasmList.get(ctr);
			importedGermplasm.setGid(gid);
			
			// extract the cross name or pedigree record of the germplasm
			String cross = "-";
			if (importedGermplasm.getCross() != null && importedGermplasm.getCross().length() > 0) {
				cross = importedGermplasm.getCross();
			}
			
			// default settings for the current entry id and the entry code
			int curEntryId = ctr+1;
			String entryCode = String.valueOf(curEntryId);
			// over-write with imported data if present
			if (importedGermplasm.getEntryCode() != null && importedGermplasm.getEntryCode().length() > 0) {
				entryCode = importedGermplasm.getEntryCode();
			}
			if (importedGermplasm.getEntryId() != null) {
				curEntryId = importedGermplasm.getEntryId();
			}
			
			// construct the list to be saved
			final GermplasmListData germplasmListData =
					this.buildGermplasmListData(list, gid, curEntryId, germplasmName.getName().getNval(), cross, importedGermplasm.getSource(), entryCode);
			germplasmListDataList.add(germplasmListData);
			
			// collect new attributes to add to the system
			if (!importedGermplasm.getAttributeVariates().isEmpty()) {
				final List<UserDefinedField> existingAttrUdflds = this.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_ATTRIBUTE);
				attrs.addAll(this.prepareAllAttributesToAdd(importedGermplasm, existingAttrUdflds, germplasmName.getGermplasm()));
			}
			
			// collect new name factors to add to the system
			if (!importedGermplasm.getNameFactors().isEmpty()) {
				final Germplasm germplasm = germplasmName.getGermplasm();
				names.addAll(this.prepareAllNamesToAdd(importedGermplasm, existingNameUdflds, germplasm, namesMap.get(germplasm.getGid())));
			}

		}

		if(!germplasmListDataList.isEmpty()){
			//Save all list data
			final List<Integer> lrecIds = this.germplasmListManager.addGermplasmListData(germplasmListDataList);
			
			//loop through the lrecids and create deposit inventory transactions
			for(int i=0; i<lrecIds.size(); i++){
				this.createDepositInventoryTransaction(list, importedGermplasmList.get(i), importedGermplasmList.get(i).getGid(), lrecIds.get(i));
			}
		}
		
		if (!attrs.isEmpty()) {
			// Add All Attributes to database
			this.germplasmManager.addAttributes(attrs);
		}

		if (!names.isEmpty()) {
			// Add All Names to database
			this.germplasmManager.addGermplasmName(names);
		}
	}
	
	/**
	 * Retrieves the map of names of germplasm using germplasm ids and name type ids.
	 * 
	 * @param importedGermplasmList
	 * @param excludeGermplasmCreateIds
	 * @param existingUdflds
	 * @return
	 */
	private Map<Integer, List<Name>> getNamesMap(List<ImportedGermplasm> importedGermplasmList, List<Integer> excludeGermplasmCreateIds, List<UserDefinedField> existingUdflds) {
		Map<Integer, List<Name>> namesMap = new HashMap<Integer, List<Name>>();
		// if there are imported germplasm present and exclusions
		if(!importedGermplasmList.isEmpty() && !excludeGermplasmCreateIds.isEmpty()){
			//get all the name type ids present in the imported germplasm list
			Map<String, String> nameFactors = importedGermplasmList.get(0).getNameFactors();
			List<Integer> nameTypeIds = new ArrayList<Integer>();
			for(Entry<String, String> factor: nameFactors.entrySet()){
				nameTypeIds.add(this.getUdfldID(existingUdflds, factor.getKey()));
			}
			
			if(!nameTypeIds.isEmpty()){
				return this.germplasmManager.getNamesByGidsAndNTypeIdsInMap(excludeGermplasmCreateIds, nameTypeIds);
			}
		}
		return namesMap;
	}

	protected void createDepositInventoryTransaction(final GermplasmList list, final ImportedGermplasm importedGermplasm, final Integer gid,
			final Integer lrecId) {
		if (importedGermplasm != null && importedGermplasm.getSeedAmount() != null && importedGermplasm.getSeedAmount() > 0) {

			if (this.gidTransactionSetMap.get(gid) == null) {
				this.gidTransactionSetMap.put(gid, new ArrayList<Transaction>());
			}

			final Integer intDate = DateUtil.getCurrentDateAsIntegerValue();

			final Integer cropUserId = this.contextUtil.getCurrentUserLocalId();

			final Transaction transaction =
					new Transaction(null, cropUserId, this.gidLotMap.get(gid), intDate, TransactionStatus.DEPOSITED.getIntValue(),
							importedGermplasm.getSeedAmount(), SaveGermplasmListAction.INVENTORY_COMMENT, 0, "LIST", list.getId(), lrecId,
							Double.valueOf(0), this.getCropPersonId(cropUserId), importedGermplasm.getInventoryId());
			if (importedGermplasm.getSeedAmount() != null) {
				this.gidTransactionSetMap.get(gid).add(transaction);
			}
		}
	}

	protected Integer getCropPersonId(final Integer cropUserId) {
		User cropUser = null;
		Integer cropPersonId = 0;
		try {
			cropUser = this.userDataManager.getUserById(cropUserId);
			if (cropUser != null) {
				cropPersonId = cropUser.getPersonid();
			}
		} catch (final MiddlewareQueryException e) {
			Log.error(e.getMessage(), e);
		}

		return cropPersonId;
	}

	private List<Attribute> prepareAllAttributesToAdd(final ImportedGermplasm importedGermplasm,
			final List<UserDefinedField> existingUdflds, final Germplasm germplasm) {
		final List<Attribute> attrs = new ArrayList<Attribute>();

		final Map<String, String> otherAttributes = importedGermplasm.getAttributeVariates();
		if (otherAttributes != null) {
			for (final Map.Entry<String, String> entry : otherAttributes.entrySet()) {
				final String code = entry.getKey();
				final String value = entry.getValue();

				if (value != null && !"".equals(value.trim())) {
					// Create New Attribute Object
					final Attribute newAttr = new Attribute();
					newAttr.setGermplasmId(germplasm.getGid());
					newAttr.setTypeId(this.getUdfldID(existingUdflds, code));
					newAttr.setUserId(this.contextUtil.getCurrentUserLocalId());
					newAttr.setAval(value);
					newAttr.setLocationId(germplasm.getLocationId());
					newAttr.setReferenceId(0);
					newAttr.setAdate(Util.getCurrentDateAsIntegerValue());

					attrs.add(newAttr);
				}
			}
		}

		return attrs;
	}
	
	/**
	 * Creates new name objects and filters the names that are already in the database
	 * @param importedGermplasm
	 * @param existingUdflds
	 * @param germplasm
	 * @param existingNames
	 * @return
	 */
	public List<Name> prepareAllNamesToAdd(final ImportedGermplasm importedGermplasm, final List<UserDefinedField> existingUdflds,
			final Germplasm germplasm, final List<Name> existingNames) {
		final List<Name> names = new ArrayList<Name>();
		final Map<String, String> otherNames = importedGermplasm.getNameFactors();

		if (otherNames != null) {
			for (final Map.Entry<String, String> entry : otherNames.entrySet()) {
				final String code = entry.getKey();
				final String value = entry.getValue();

				if (value != null && !"".equals(value.trim())) {
					// Create New Name Object
					final Name newName = new Name();
					newName.setGermplasmId(germplasm.getGid());
					newName.setTypeId(this.getUdfldID(existingUdflds, code));
					newName.setUserId(this.contextUtil.getCurrentUserLocalId());
					newName.setNstat(0);
					newName.setNval(value);
					newName.setLocationId(germplasm.getLocationId());
					newName.setReferenceId(0);
					newName.setNdate(Util.getCurrentDateAsIntegerValue());

					this.filterDuplicateName(names, newName, existingNames);
				}
			}
		}

		return names;
	}
	
	/**
	 * Filters new name if it's already in the database by comparing the name values and typeids
	 * @param names
	 * @param newName
	 * @param existingNames
	 */
	private void filterDuplicateName(final List<Name> names, final Name newName, final List<Name> existingNames) {
		if (existingNames != null && !existingNames.isEmpty()) {
			for (final Name name : existingNames) {
				if (name.getTypeId().intValue() == newName.getTypeId().intValue() && name.getNval().equalsIgnoreCase(newName.getNval())) {
					return;
				}
			}
		}
		names.add(newName);
	}

	private GermplasmListData buildGermplasmListData(final GermplasmList list, final Integer gid, final int entryId,
			final String designation, final String groupName, final String source, final String entryCode) {
		final GermplasmListData germplasmListData = new GermplasmListData();
		germplasmListData.setList(list);
		germplasmListData.setGid(gid);
		germplasmListData.setEntryId(entryId);
		germplasmListData.setEntryCode(entryCode);
		germplasmListData.setSeedSource(source);
		germplasmListData.setDesignation(designation);
		germplasmListData.setStatus(SaveGermplasmListAction.LIST_DATA_STATUS);
		germplasmListData.setGroupName(groupName);
		germplasmListData.setLocalRecordId(entryId);

		return germplasmListData;
	}

	public Map<Integer, Lot> getGidLotMap() {
		return this.gidLotMap;
	}

	public Map<Integer, List<Transaction>> getGidTransactionSetMap() {
		return this.gidTransactionSetMap;
	}

}
