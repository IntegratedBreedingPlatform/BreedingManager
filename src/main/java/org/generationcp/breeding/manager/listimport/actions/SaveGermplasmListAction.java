
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

	public static final String INVENTORY_COMMENT = "From List Import";
	public static final String WB_ACTIVITY_NAME = "Imported a Germplasm List";
	public static final String WB_ACTIVITY_DESCRIPTION = "Imported list from file ";
	public static final Integer LIST_DATA_STATUS = 0;
	public static final Integer LIST_DATA_LRECID = 0;

	public static final int FCODE_TYPE_NAME = 0;
	public static final int FCODE_TYPE_ATTRIBUTE = 1;

	public static final String FTABLE_NAME = "NAMES";
	public static final String FTYPE_NAME = "NAME";
	public static final String FTABLE_ATTRIBUTE = "ATRIBUTS";
	public static final String FTYPE_ATTRIBUTE = "ATTRIBUTE";
	public static final String FTYPE_PASSPORT = "PASSPORT";

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
		this.gidLotMap = new HashMap<>();
		this.gidTransactionSetMap = new HashMap<>();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// do nothing
	}

	/**
	 * Saves Germplasm records and associated meta-data such as Inventory, new Attributes and new Names Save GermplasmList and
	 * GermplasmListData, ProjectActivity (Workbench) records as well
	 *
	 * @param germplasmList
	 * @param germplasmNameObjects : germplasm names imported
	 * @param newNames : list of new names to be added
	 * @param filename : name of imported file
	 * @param excludeGermplasmCreateIds : list of GIDs for which new germplasm record should not be created
	 * @param importedGermplasmList : imported germplasm from import file
	 * @param seedStorageLocationId : ID of chosen seed storage location
	 * @return id of new Germplasm List created @
	 * @throws BreedingManagerException
	 */
	public Integer saveRecords(final GermplasmList germplasmList, final List<GermplasmName> germplasmNameObjects, final List<Name> newNames,
			final String filename, final List<Integer> excludeGermplasmCreateIds, final ImportedGermplasmList importedGermplasmList,
			final Integer seedStorageLocationId) throws BreedingManagerException {

		// Retrieve seed stock variable and create UDFLDS records for new name and attribute types (if any)
		this.processVariates(importedGermplasmList);
		this.processFactors(importedGermplasmList);

		// Creates and saves germplasm record. Associate Names to created germplasm. Prepare lots to insert
		this.processGermplasmNamesAndLots(germplasmNameObjects, excludeGermplasmCreateIds, seedStorageLocationId);

		final GermplasmList list = this.saveGermplasmListRecord(germplasmList);

		// mark the existing entries of the list deleted before adding the new entries from germplasm import
		final Integer existingListId = germplasmList.getId();
		if (existingListId != null) {
			ListCommonActionsUtil.deleteExistingListEntries(existingListId, this.germplasmListManager);
		}

		final List<ImportedGermplasm> importedGermplasm = importedGermplasmList.getImportedGermplasm();
		this.saveGermplasmListDataRecords(germplasmNameObjects, list, importedGermplasm, excludeGermplasmCreateIds);

		if (!newNames.isEmpty()) {
			// save the names under the designation column.
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
		this.gidLotMap.clear();
		this.gidTransactionSetMap.clear();

		final Map<Integer, Integer> tempGidToRealGidMap = new HashMap<>();
		final Map<Integer, Germplasm> createdGermplasmMap = new HashMap<>();
		for (final GermplasmName germplasmName : germplasmNameObjects) {
			final Name name = germplasmName.getName();
			name.setNid(null);
			// Nstat = name status
			name.setNstat(Integer.valueOf(1));
			final Integer gid = germplasmName.getGermplasm().getGid();
			Integer finalGid = null;
			final Germplasm germplasm;

			// If entry was matched to existing germplasm and germplasm record should not be created
			// Check also if GID was matched to actual GID in DB to distinguish temporary GIDs (eg. an entry could have temporary,
			// unmatched GID = 10 while the actual GID 10 could be in list of matched GIDs as matched to another entry
			if (excludeGermplasmCreateIds.contains(gid) && germplasmName.isGidMatched()) {
				germplasm = this.germplasmManager.getGermplasmByGID(gid);
				germplasmName.setGermplasm(germplasm);
				name.setGermplasmId(gid);
				finalGid = gid;

				// Save new germplasm record
			} else {
				// a local GID of zero reflects no previous known GID from other systems
				germplasmName.getGermplasm().setLgid(Integer.valueOf(0));

				final Integer realGid = tempGidToRealGidMap.get(gid);
				if (createdGermplasmMap.containsKey(realGid)) {
					// we have a previous entry in the same import with the same gid
					final Germplasm createdGermplasm = createdGermplasmMap.get(realGid);
					germplasmName.setGermplasm(createdGermplasm);
					finalGid = createdGermplasm.getGid();
				} else {
					// not yet added, save new germplasm record
					germplasm = germplasmName.getGermplasm();
					germplasm.setGid(null);
					if (germplasm.getGdate().equals(Integer.valueOf(0))) {
						final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
						final Date today = new Date();
						germplasm.setGdate(Integer.valueOf(dateFormat.format(today)));
					}

					// For now have to do saving of germplasm one at a time for setting of final GIDs to duplicate entries
					finalGid = this.germplasmManager.addGermplasm(germplasm, name);

					createdGermplasmMap.put(finalGid, germplasm);
					tempGidToRealGidMap.put(gid, finalGid);
				}
			}

			// process inventory
			if (this.seedAmountScaleId != null) {
				final Lot lot = new Lot(null, this.contextUtil.getCurrentUserLocalId(), EntityType.GERMPLSM.name(), finalGid,
						seedStorageLocation, this.seedAmountScaleId, 0, 0, SaveGermplasmListAction.INVENTORY_COMMENT);
				this.gidLotMap.put(finalGid, lot);
			}
		}
	}

	/**
	 * Processes seed stock variable plus add new attribute types, if any, as UserDefinedFields records
	 *
	 * + @param importedGermplasmList + @throws BreedingManagerException
	 */
	void processVariates(final ImportedGermplasmList importedGermplasmList) throws BreedingManagerException {
		final List<UserDefinedField> existingUserDefinedFields = this.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_ATTRIBUTE);
		final List<UserDefinedField> newUserDefinedFields = new ArrayList<>();
		final Map<String, String> attributeVariates = importedGermplasmList.getImportedGermplasm().get(0).getAttributeVariates();

		for (final ImportedVariate importedVariate : importedGermplasmList.getImportedVariates()) {
			// GCP-10077: use variate name, instead of the property
			final String variate = importedVariate.getVariate();
			if (importedVariate.isSeedStockVariable()) {
				this.processSeedStockVariate(importedVariate);
			} else {
				if (attributeVariates.containsKey(variate) && !this.isUserDefinedFieldExists(existingUserDefinedFields, variate)) {
					final String fieldFormat =
							importedVariate.getScale() + "," + importedVariate.getMethod() + "," + importedVariate.getDataType();
					final UserDefinedField newUserDefinedField = this.createNewUserDefinedField(SaveGermplasmListAction.FTABLE_ATTRIBUTE,
							importedVariate.getProperty().toUpperCase(), importedVariate.getVariate(), importedVariate.getDescription(),
							fieldFormat);
					newUserDefinedFields.add(newUserDefinedField);
				}
			}
		}

		// Add new attribute types to UDFLDS table
		if (!newUserDefinedFields.isEmpty()) {
			this.germplasmManager.addUserDefinedFields(newUserDefinedFields);
		}
	}

	/**
	 * Add new name types, if any, as UserDefinedFields records
	 *
	 * + @param importedGermplasmList + @throws BreedingManagerException
	 */
	protected void processFactors(final ImportedGermplasmList importedGermplasmList) {
		final List<UserDefinedField> existingUserDefineFields = this.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_NAME);
		final List<UserDefinedField> newUserDefinedFields = new ArrayList<>();
		final Map<String, String> nameFactors = importedGermplasmList.getImportedGermplasm().get(0).getNameFactors();

		for (final ImportedFactor importedFactor : importedGermplasmList.getImportedFactors()) {
			final String factor = importedFactor.getFactor();
			if (nameFactors.containsKey(factor) && !this.isUserDefinedFieldExists(existingUserDefineFields, factor)) {
				final String fieldFormat =
						importedFactor.getScale() + "," + importedFactor.getMethod() + "," + importedFactor.getDataType();
				final UserDefinedField newUserDefinedField = this.createNewUserDefinedField(SaveGermplasmListAction.FTABLE_NAME,
						SaveGermplasmListAction.FTYPE_NAME, importedFactor.getFactor(), importedFactor.getDescription(), fieldFormat);
				newUserDefinedFields.add(newUserDefinedField);
			}
		}

		// Add new name types to UDFLDS table
		if (!newUserDefinedFields.isEmpty()) {
			this.germplasmManager.addUserDefinedFields(newUserDefinedFields);
		}
	}

	UserDefinedField createNewUserDefinedField(final String tableName, final String fieldType, final String code, final String name,
			final String fieldFormat) {
		final UserDefinedField newUserDefinedField = new UserDefinedField();
		newUserDefinedField.setFtable(tableName);
		newUserDefinedField.setFtype(fieldType);
		newUserDefinedField.setFcode(code);
		newUserDefinedField.setFname(name);
		newUserDefinedField.setFfmt(fieldFormat);
		newUserDefinedField.setFdesc("-");
		newUserDefinedField.setLfldno(0);
		newUserDefinedField.setFuid(this.contextUtil.getCurrentUserLocalId());
		newUserDefinedField.setFdate(Util.getCurrentDateAsIntegerValue());
		newUserDefinedField.setScaleid(0);

		return newUserDefinedField;
	}

	protected void processSeedStockVariate(final ImportedVariate importedVariate) throws BreedingManagerException {

		// find stock variable via name at top of column in the sheet - should be one
		final Set<StandardVariable> terms = this.ontologyDataManager.findStandardVariablesByNameOrSynonym(importedVariate.getVariate(),
				this.contextUtil.getCurrentProgramUUID());
		if (terms.size() == 1) {
			// ok to get only record with the size check
			final StandardVariable stdVariable = new ArrayList<>(terms).get(0);
			importedVariate.setScaleId(stdVariable.getId());
			this.seedAmountScaleId = importedVariate.getScaleId();
		} else {
			// sorry non-i18N message
			throw new BreedingManagerException("The BMS does not contain a Variable called " + importedVariate.getVariate()
					+ ". Please create it in the Ontology Manager or change your import sheet.");
		}

	}

	private boolean isUserDefinedFieldExists(final List<UserDefinedField> existingUserDefinedField, final String fieldCode) {
		for (final UserDefinedField userDefinedField : existingUserDefinedField) {
			if (userDefinedField.getFcode().equalsIgnoreCase(fieldCode)) {
				return true;
			}
		}
		return false;
	}

	private Integer getUserDefinedFieldId(final List<UserDefinedField> existingUserDefinedFields, final String property) {
		if (existingUserDefinedFields != null) {
			for (final UserDefinedField useerDefinedField : existingUserDefinedFields) {
				if (useerDefinedField.getFcode().equalsIgnoreCase(property.toUpperCase())) {
					return useerDefinedField.getFldno();
				}
			}
		}
		return 0;
	}

	private GermplasmList saveGermplasmListRecord(final GermplasmList germplasmList) {
		germplasmList.setUserId(this.contextUtil.getCurrentUserLocalId());

		final int newListId = this.germplasmListManager.addGermplasmList(germplasmList);
		return this.germplasmListManager.getGermplasmListById(newListId);
	}

	public List<UserDefinedField> getUserDefinedFields(final int fcodeType) {
		List<UserDefinedField> userDefinedFields = new ArrayList<>();
		if (SaveGermplasmListAction.FCODE_TYPE_ATTRIBUTE == fcodeType) {
			final List<UserDefinedField> list = this.germplasmManager.getUserDefinedFieldByFieldTableNameAndType(
					SaveGermplasmListAction.FTABLE_ATTRIBUTE, SaveGermplasmListAction.FTYPE_ATTRIBUTE);
			final List<UserDefinedField> list2 = this.germplasmManager.getUserDefinedFieldByFieldTableNameAndType(
					SaveGermplasmListAction.FTABLE_ATTRIBUTE, SaveGermplasmListAction.FTYPE_PASSPORT);
			if (list != null && !list.isEmpty()) {
				userDefinedFields.addAll(list);
			}
			if (list2 != null && !list2.isEmpty()) {
				userDefinedFields.addAll(list2);
			}
		} else if (SaveGermplasmListAction.FCODE_TYPE_NAME == fcodeType) {
			userDefinedFields = this.germplasmManager.getUserDefinedFieldByFieldTableNameAndType(SaveGermplasmListAction.FTABLE_NAME,
					SaveGermplasmListAction.FTYPE_NAME);
		}
		return userDefinedFields;
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
		final List<Name> names = new ArrayList<>();
		final List<Attribute> attrs = new ArrayList<>();
		final List<GermplasmListData> germplasmListDataList = new ArrayList<>();

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
			int curEntryId = ctr + 1;
			String entryCode = String.valueOf(curEntryId);
			// over-write with imported data if present
			if (importedGermplasm.getEntryCode() != null && importedGermplasm.getEntryCode().length() > 0) {
				entryCode = importedGermplasm.getEntryCode();
			}
			if (importedGermplasm.getEntryId() != null) {
				curEntryId = importedGermplasm.getEntryId();
			}

			// construct the list to be saved
			final GermplasmListData germplasmListData = this.buildGermplasmListData(list, gid, curEntryId,
					germplasmName.getName().getNval(), cross, importedGermplasm.getSource(), entryCode);
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

		if (!germplasmListDataList.isEmpty()) {
			// Save all list data
			final List<Integer> lrecIds = this.germplasmListManager.addGermplasmListData(germplasmListDataList);

			// loop through the lrecids and create deposit inventory transactions
			for (int i = 0; i < lrecIds.size(); i++) {
				this.createDepositInventoryTransaction(list, importedGermplasmList.get(i), importedGermplasmList.get(i).getGid(),
						lrecIds.get(i));
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
	private Map<Integer, List<Name>> getNamesMap(final List<ImportedGermplasm> importedGermplasmList,
			final List<Integer> excludeGermplasmCreateIds, final List<UserDefinedField> existingUdflds) {
		final Map<Integer, List<Name>> namesMap = new HashMap<>();
		// if there are imported germplasm present and exclusions
		if (!importedGermplasmList.isEmpty() && !excludeGermplasmCreateIds.isEmpty()) {
			// get all the name type ids present in the imported germplasm list
			final Map<String, String> nameFactors = importedGermplasmList.get(0).getNameFactors();
			final List<Integer> nameTypeIds = new ArrayList<>();
			for (final Entry<String, String> factor : nameFactors.entrySet()) {
				nameTypeIds.add(this.getUserDefinedFieldId(existingUdflds, factor.getKey()));
			}

			if (!nameTypeIds.isEmpty()) {
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
			final List<UserDefinedField> existingUserDefinedFields, final Germplasm germplasm) {
		final List<Attribute> attributes = new ArrayList<>();

		final Map<String, String> otherAttributes = importedGermplasm.getAttributeVariates();
		if (otherAttributes != null) {
			for (final Map.Entry<String, String> entry : otherAttributes.entrySet()) {
				final String code = entry.getKey();
				final String value = entry.getValue();

				if (value != null && !"".equals(value.trim())) {
					// Create New Attribute Object
					final Attribute newAttribute = new Attribute();
					newAttribute.setGermplasmId(germplasm.getGid());
					newAttribute.setTypeId(this.getUserDefinedFieldId(existingUserDefinedFields, code));
					newAttribute.setUserId(this.contextUtil.getCurrentUserLocalId());
					newAttribute.setAval(value);
					newAttribute.setLocationId(germplasm.getLocationId());
					newAttribute.setReferenceId(0);
					newAttribute.setAdate(Util.getCurrentDateAsIntegerValue());

					attributes.add(newAttribute);
				}
			}
		}

		return attributes;
	}

	/**
	 * Creates new name objects and filters the names that are already in the database
	 *
	 * @param importedGermplasm
	 * @param existingUdflds
	 * @param germplasm
	 * @param existingNames
	 * @return
	 */
	public List<Name> prepareAllNamesToAdd(final ImportedGermplasm importedGermplasm, final List<UserDefinedField> existingUdflds,
			final Germplasm germplasm, final List<Name> existingNames) {
		final List<Name> names = new ArrayList<>();
		final Map<String, String> otherNames = importedGermplasm.getNameFactors();

		if (otherNames != null) {
			for (final Map.Entry<String, String> entry : otherNames.entrySet()) {
				final String code = entry.getKey();
				final String value = entry.getValue();

				if (value != null && !"".equals(value.trim())) {
					// Create New Name Object
					final Name newName = new Name();
					newName.setGermplasmId(germplasm.getGid());
					newName.setTypeId(this.getUserDefinedFieldId(existingUdflds, code));
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
	 *
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

	public void setSeedAmountScaleId(final Integer seedAmountScaleId) {
		this.seedAmountScaleId = seedAmountScaleId;
	}

}
