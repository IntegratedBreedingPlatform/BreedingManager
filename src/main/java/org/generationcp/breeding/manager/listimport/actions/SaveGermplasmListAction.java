
package org.generationcp.breeding.manager.listimport.actions;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.listmanager.util.ListCommonActionsUtil;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedFactor;
import org.generationcp.commons.parsing.pojo.ImportedVariate;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.util.FileUtils;
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
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.util.Util;
import org.jfree.util.Log;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Created with IntelliJ IDEA. User: Efficio.Daniel Date: 8/20/13 Time: 1:39 PM To change this template use File | Settings | File
 * Templates.
 */
@Configurable
public class SaveGermplasmListAction implements Serializable, InitializingBean {

	private static final String INVENTORY_COMMENT = "From List Import";
	public static final String WB_ACTIVITY_NAME = "Imported a Germplasm List";
	public static final String WB_ACTIVITY_DESCRIPTION = "Imported list from file ";
	public static final Integer LIST_DATA_STATUS = 0;
	public static final Integer LIST_DATA_LRECID = 0;

	private static final int FCODE_TYPE_NAME = 0;
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
	private InventoryService inventoryService;

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
	 * @param doNotCreateGermplasmsWithId
	 * @param importedGermplasmList
	 * @param seedStorageLocation
	 * @return id of new Germplasm List created @
	 */
	public Integer saveRecords(final GermplasmList germplasmList, final List<GermplasmName> germplasmNameObjects,
			final List<Name> newNames, final String filename, final List<Integer> doNotCreateGermplasmsWithId,
			final ImportedGermplasmList importedGermplasmList, final Integer seedStorageLocation) {

		germplasmList.setUserId(this.contextUtil.getCurrentUserLocalId());
		germplasmList.setProgramUUID(this.contextUtil.getCurrentProgramUUID());

		// Retrieve seed stock variable and/or attribute types (or create new one) as needed
		this.processVariates(importedGermplasmList);

		// Create new udfld records as needed
		this.processFactors(importedGermplasmList);

		this.gidLotMap.clear();
		this.gidTransactionSetMap.clear();

		this.processGermplasmNamesAndLots(germplasmNameObjects, doNotCreateGermplasmsWithId, seedStorageLocation);

		final List<ImportedGermplasm> importedGermplasms = importedGermplasmList.getImportedGermplasms();

		final GermplasmList list = this.saveGermplasmListRecord(germplasmList);

		if (germplasmList.getId() != null) {
			ListCommonActionsUtil.deleteExistingListEntries(germplasmList, this.germplasmListManager);
		}

		this.saveGermplasmListDataRecords(germplasmNameObjects, list, filename, importedGermplasms);
		this.addNewNamesToExistingGermplasm(newNames);

		this.saveInventory();

		// log project activity in Workbench
		this.contextUtil.logProgramActivity(SaveGermplasmListAction.WB_ACTIVITY_NAME, SaveGermplasmListAction.WB_ACTIVITY_DESCRIPTION
				+ filename);

		return list.getId();
	}

	protected void saveInventory() {
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
					final Lot existingLot =
							SaveGermplasmListAction.this.inventoryService.getLotByEntityTypeAndEntityIdAndLocationIdAndScaleId(
									lot.getEntityType(), gid, lot.getLocationId(), lot.getScaleId());
					if (existingLot == null) {
						SaveGermplasmListAction.this.inventoryDataManager.addLot(lot);
					} else {
						for (final Transaction transaction : listOfTransactions) {
							transaction.setLot(existingLot);
						}
					}
					SaveGermplasmListAction.this.inventoryDataManager.addTransactions(listOfTransactions);
				}
			}
		});
	}

	protected void processGermplasmNamesAndLots(final List<GermplasmName> germplasmNameObjects,
			final List<Integer> doNotCreateGermplasmsWithId, final Integer seedStorageLocation) {

		final Map<Integer, GermplasmName> addedGermplasmNameMap = new HashMap<Integer, GermplasmName>();

		for (final GermplasmName germplasmName : germplasmNameObjects) {
			final Name name = germplasmName.getName();
			name.setNid(null);
			name.setNstat(Integer.valueOf(1));

			Integer gid = null;
			Germplasm germplasm;

			if (doNotCreateGermplasmsWithId.contains(germplasmName.getGermplasm().getGid())) {
				// If do not create new germplasm
				gid = germplasmName.getGermplasm().getGid();
				germplasm = this.germplasmManager.getGermplasmByGID(gid);
				germplasmName.setGermplasm(germplasm);
				name.setGermplasmId(gid);
			} else {
				final Germplasm addedGermplasmMatch = this.getAlreadyAddedGermplasm(germplasmName, addedGermplasmNameMap);
				germplasmName.getGermplasm().setLgid(Integer.valueOf(0));
				// not yet added, create new germplasm record
				if (addedGermplasmMatch == null) {
					germplasm = germplasmName.getGermplasm();
					germplasmName.getGermplasm().setGid(null);
					if (germplasm.getGdate().equals(Integer.valueOf(0))) {
						final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
						final Date today = new Date();
						germplasm.setGdate(Integer.valueOf(dateFormat.format(today)));
					}
					gid = this.germplasmManager.addGermplasm(germplasm, name);
					addedGermplasmNameMap.put(germplasmName.getGermplasm().getGid(), germplasmName);
					// if already addded (re-use that one)
				} else {
					germplasm = addedGermplasmMatch;
					germplasmName.setGermplasm(addedGermplasmMatch);
					gid = addedGermplasmMatch.getGid();
				}
			}

			if (this.seedAmountScaleId != null) {
				final Lot lot =
						new Lot(null, this.contextUtil.getCurrentUserLocalId(), EntityType.GERMPLSM.name(), gid, seedStorageLocation,
								this.seedAmountScaleId, 0, 0, SaveGermplasmListAction.INVENTORY_COMMENT);
				this.gidLotMap.put(gid, lot);
			}
		}
	}

	protected void processVariates(final ImportedGermplasmList importedGermplasmList) {
		final List<UserDefinedField> existingUdflds = this.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_ATTRIBUTE);
		final List<UserDefinedField> newUdflds = new ArrayList<UserDefinedField>();
		final Map<String, String> attributeVariates = importedGermplasmList.getImportedGermplasms().get(0).getAttributeVariates();

		for (final ImportedVariate importedVariate : importedGermplasmList.getImportedVariates()) {
			// GCP-10077: use variate name, instead of the property
			final String variate = importedVariate.getVariate();
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
		final Map<String, String> nameFactors = importedGermplasmList.getImportedGermplasms().get(0).getNameFactors();

		for (final ImportedFactor importedFactor : importedGermplasmList.getImportedFactors()) {
			final String factor = importedFactor.getFactor();
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
		newUdfld.setFcode(importedVariate.getVariate());
		newUdfld.setFname(importedVariate.getDescription());
		final String fmt = importedVariate.getScale() + "," + importedVariate.getMethod() + "," + importedVariate.getDataType();
		newUdfld.setFfmt(fmt);
		newUdfld.setFdesc("-");
		newUdfld.setLfldno(0);
		newUdfld.setUser(new User(this.contextUtil.getCurrentUserLocalId()));
		newUdfld.setFdate(Util.getCurrentDateAsIntegerValue());
		newUdfld.setScaleid(0);

		return newUdfld;
	}

	private UserDefinedField createNewUserDefinedField(final ImportedFactor importedFactor) {
		final UserDefinedField newUdfld = new UserDefinedField();
		newUdfld.setFtable(SaveGermplasmListAction.FTABLE_NAME);
		newUdfld.setFtype(SaveGermplasmListAction.FTYPE_NAME);
		newUdfld.setFcode(importedFactor.getFactor());
		newUdfld.setFname(importedFactor.getDescription());
		final String fmt = importedFactor.getScale() + "," + importedFactor.getMethod() + "," + importedFactor.getDataType();
		newUdfld.setFfmt(fmt);
		newUdfld.setFdesc("-");
		newUdfld.setLfldno(0);
		newUdfld.setUser(new User(this.contextUtil.getCurrentUserLocalId()));
		newUdfld.setFdate(Util.getCurrentDateAsIntegerValue());
		newUdfld.setScaleid(0);

		return newUdfld;
	}

	protected void processSeedStockVariate(final ImportedVariate importedVariate) {
		final String trait = importedVariate.getProperty().toUpperCase();
		final String scale = importedVariate.getScale().toUpperCase();
		final String method = importedVariate.getMethod().toUpperCase();

		StandardVariable stdVariable =
				this.ontologyDataManager.findStandardVariableByTraitScaleMethodNames(trait, scale, method,
						this.contextUtil.getCurrentProgramUUID());
		// create new variate if PSMR doesn't exist
		if (stdVariable == null) {

			Term traitTerm = this.ontologyDataManager.findTermByName(trait, CvId.PROPERTIES);
			if (traitTerm == null) {
				traitTerm = new Term();
				traitTerm.setName(trait);
				traitTerm.setDefinition(trait);

			}

			Term scaleTerm = this.ontologyDataManager.findTermByName(scale, CvId.SCALES);
			if (scaleTerm == null) {
				scaleTerm = new Term();
				scaleTerm.setName(scale);
				scaleTerm.setDefinition(scale);
			}

			Term methodTerm = this.ontologyDataManager.findTermByName(method, CvId.METHODS);
			if (methodTerm == null) {
				methodTerm = new Term();
				methodTerm.setName(method);
				methodTerm.setDefinition(method);
			}

			final Term dataType = new Term();
			dataType.setId("N".equals(importedVariate.getDataType()) ? TermId.NUMERIC_VARIABLE.getId() : TermId.CHARACTER_VARIABLE.getId());

			stdVariable = new StandardVariable(traitTerm, scaleTerm, methodTerm, dataType, null, PhenotypicType.VARIATE);
			stdVariable.setName(importedVariate.getVariate());
			stdVariable.setDescription(importedVariate.getDescription());

			this.ontologyDataManager.addStandardVariable(stdVariable, this.contextUtil.getCurrentProgramUUID());
		}

		if (stdVariable.getId() != 0) {
			importedVariate.setScaleId(stdVariable.getId());
			this.seedAmountScaleId = importedVariate.getScaleId();

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

	protected void addNewNamesToExistingGermplasm(final List<Name> newNames) {
		for (final Name name : newNames) {
			this.germplasmManager.addGermplasmName(name);
		}
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

	private List<UserDefinedField> getUserDefinedFields(final int fcodeType) {
		List<UserDefinedField> udFields = new ArrayList<UserDefinedField>();
		if (SaveGermplasmListAction.FCODE_TYPE_ATTRIBUTE == fcodeType) {
			final List<UserDefinedField> list =
					this.germplasmManager.getUserDefinedFieldByFieldTableNameAndType(SaveGermplasmListAction.FTABLE_ATTRIBUTE,
							SaveGermplasmListAction.FTYPE_ATTRIBUTE);
			final List<UserDefinedField> list2 =
					this.germplasmManager.getUserDefinedFieldByFieldTableNameAndType(SaveGermplasmListAction.FTABLE_ATTRIBUTE,
							SaveGermplasmListAction.FTYPE_PASSPORT);
			if (list != null && !list.isEmpty()) {
				udFields.addAll(list);
			}
			if (list2 != null && !list2.isEmpty()) {
				udFields.addAll(list2);
			}
		} else if (SaveGermplasmListAction.FCODE_TYPE_NAME == fcodeType) {
			udFields =
					this.germplasmManager.getUserDefinedFieldByFieldTableNameAndType(SaveGermplasmListAction.FTABLE_NAME,
							SaveGermplasmListAction.FTYPE_NAME);
		}
		return udFields;
	}

	private void saveGermplasmListDataRecords(final List<GermplasmName> germplasmNameObjects, final GermplasmList list,
			final String filename, final List<ImportedGermplasm> importedGermplasms) {

		final List<GermplasmListData> listToSave = new ArrayList<GermplasmListData>();
		final List<UserDefinedField> existingAttrUdflds = this.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_ATTRIBUTE);
		final List<UserDefinedField> existingNameUdflds = this.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_NAME);
		final List<Attribute> attrs = new ArrayList<Attribute>();
		final List<Name> names = new ArrayList<Name>();

		int ctr = 1;
		for (final GermplasmName germplasmName : germplasmNameObjects) {

			final int entryId = ctr++;
			final ImportedGermplasm importedGermplasm = importedGermplasms.get(entryId - 1);
			final Integer gid = germplasmName.getGermplasm().getGid();

			final String designation = germplasmName.getName().getNval();

			final String fileNameWithoutExtension = FileUtils.getFilenameWithoutExtension(filename);
			String source = fileNameWithoutExtension + ":" + entryId;
			if (importedGermplasm.getSource() != null && importedGermplasm.getSource().length() > 0) {
				source = importedGermplasm.getSource();
			}

			String groupName = "-";
			if (importedGermplasm.getCross() != null && importedGermplasm.getCross().length() > 0) {
				groupName = importedGermplasm.getCross();
			}

			String entryCode = String.valueOf(entryId);
			if (importedGermplasm.getEntryCode() != null && importedGermplasm.getEntryCode().length() > 0) {
				entryCode = importedGermplasm.getEntryCode();
			}
			int curEntryId = entryId;
			if (importedGermplasm.getEntryId() != null) {
				curEntryId = importedGermplasm.getEntryId();
			}
			final GermplasmListData germplasmListData =
					this.buildGermplasmListData(list, gid, curEntryId, designation, groupName, source, entryCode);

			listToSave.add(germplasmListData);
			final Integer lrecId = this.germplasmListManager.addGermplasmListData(germplasmListData);

			this.createDepositInventoryTransaction(list, importedGermplasm, gid, lrecId);

			if (!importedGermplasm.getAttributeVariates().isEmpty()) {
				attrs.addAll(this.prepareAllAttributesToAdd(importedGermplasm, existingAttrUdflds, germplasmName.getGermplasm()));
			}

			if (!importedGermplasm.getNameFactors().isEmpty()) {
				names.addAll(this.prepareAllNamesToAdd(importedGermplasm, existingNameUdflds, germplasmName.getGermplasm()));
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

	protected void createDepositInventoryTransaction(final GermplasmList list, final ImportedGermplasm importedGermplasm,
			final Integer gid, final Integer lrecId) {
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

	private List<Name> prepareAllNamesToAdd(final ImportedGermplasm importedGermplasm, final List<UserDefinedField> existingUdflds,
			final Germplasm germplasm) {
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

					names.add(newName);
				}
			}
		}

		return names;
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

	public void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public void setGermplasmManager(final GermplasmDataManager germplasmManager) {
		this.germplasmManager = germplasmManager;
	}

	public void setInventoryDataManager(final InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}

	public void setOntologyDataManager(final OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	public void setUserDataManager(final UserDataManager userDataManager) {
		this.userDataManager = userDataManager;
	}

}
