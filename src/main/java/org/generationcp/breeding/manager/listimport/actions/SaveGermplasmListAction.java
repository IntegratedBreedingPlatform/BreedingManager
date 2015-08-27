
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
import org.generationcp.middleware.exceptions.MiddlewareException;
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
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private UserDataManager userDataManager;

	@Resource
	private ContextUtil contextUtil;

	// Lot related variables
	private Integer seedAmountScaleId;

	private Map<Integer, Lot> gidLotMap;
	private Map<Integer, List<Transaction>> gidTransactionSetMap;

	public SaveGermplasmListAction() {
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
	 * @return id of new Germplasm List created
	 * @throws MiddlewareQueryException
	 */
	public Integer saveRecords(GermplasmList germplasmList, List<GermplasmName> germplasmNameObjects, List<Name> newNames, String filename,
			List<Integer> doNotCreateGermplasmsWithId, ImportedGermplasmList importedGermplasmList, Integer seedStorageLocation)
					throws MiddlewareException {

		germplasmList.setUserId(this.contextUtil.getCurrentUserLocalId());
		germplasmList.setProgramUUID(this.contextUtil.getCurrentProgramUUID());

		// Retrieve seed stock variable and/or attribute types (or create new one) as needed
		this.processVariates(importedGermplasmList);

		// Create new udfld records as needed
		this.processFactors(importedGermplasmList);

		this.gidLotMap = new HashMap<Integer, Lot>();
		this.gidTransactionSetMap = new HashMap<Integer, List<Transaction>>();

		this.processGermplasmNamesAndLots(germplasmNameObjects, doNotCreateGermplasmsWithId, seedStorageLocation);

		List<ImportedGermplasm> importedGermplasms = importedGermplasmList.getImportedGermplasms();
		GermplasmList list = this.saveGermplasmListRecord(germplasmList);
		this.saveGermplasmListDataRecords(germplasmNameObjects, list, filename, importedGermplasms);
		this.addNewNamesToExistingGermplasm(newNames);

		this.saveInventory();

		// log project activity in Workbench
		this.contextUtil.logProgramActivity(SaveGermplasmListAction.WB_ACTIVITY_NAME,
				SaveGermplasmListAction.WB_ACTIVITY_DESCRIPTION + filename);

		return list.getId();
	}

	protected void saveInventory() throws MiddlewareQueryException {
		for (Map.Entry<Integer, Lot> item : this.gidLotMap.entrySet()) {
			Integer gid = item.getKey();
			List<Transaction> listOfTransactions = this.gidTransactionSetMap.get(gid);
			if (listOfTransactions == null || listOfTransactions.isEmpty()) {
				continue;
			}
			Lot lot = item.getValue();
			Lot existingLot =
					this.inventoryDataManager.getLotByEntityTypeAndEntityIdAndLocationIdAndScaleId(lot.getEntityType(), gid,
							lot.getLocationId(), lot.getScaleId());
			if (existingLot == null) {
				this.inventoryDataManager.addLot(lot);
			} else {
				for (Transaction transaction : listOfTransactions) {
					transaction.setLot(existingLot);
				}
			}
			this.inventoryDataManager.addTransactions(listOfTransactions);
		}
	}

	protected void processGermplasmNamesAndLots(List<GermplasmName> germplasmNameObjects, List<Integer> doNotCreateGermplasmsWithId,
			Integer seedStorageLocation) throws MiddlewareQueryException {

		Map<Integer, GermplasmName> addedGermplasmNameMap = new HashMap<Integer, GermplasmName>();

		for (GermplasmName germplasmName : germplasmNameObjects) {
			Name name = germplasmName.getName();
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
				Germplasm addedGermplasmMatch = this.getAlreadyAddedGermplasm(germplasmName, addedGermplasmNameMap);
				germplasmName.getGermplasm().setLgid(Integer.valueOf(0));
				// not yet added, create new germplasm record
				if (addedGermplasmMatch == null) {
					germplasm = germplasmName.getGermplasm();
					germplasmName.getGermplasm().setGid(null);
					if (germplasm.getGdate().equals(Integer.valueOf(0))) {
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
						Date today = new Date();
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
				Lot lot =
						new Lot(null, this.contextUtil.getCurrentUserLocalId(), EntityType.GERMPLSM.name(), gid, seedStorageLocation,
								this.seedAmountScaleId, 0, 0, SaveGermplasmListAction.INVENTORY_COMMENT);
				this.gidLotMap.put(gid, lot);
			}
		}
	}

	protected void processVariates(ImportedGermplasmList importedGermplasmList) throws MiddlewareException {
		List<UserDefinedField> existingUdflds = this.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_ATTRIBUTE);
		List<UserDefinedField> newUdflds = new ArrayList<UserDefinedField>();
		Map<String, String> attributeVariates = importedGermplasmList.getImportedGermplasms().get(0).getAttributeVariates();

		for (ImportedVariate importedVariate : importedGermplasmList.getImportedVariates()) {
			// GCP-10077: use variate name, instead of the property
			String variate = importedVariate.getVariate();
			if (importedVariate.isSeedStockVariable()) {
				this.processSeedStockVariate(importedVariate);
			} else {
				if (attributeVariates.containsKey(variate) && !this.isUdfldsExist(existingUdflds, variate)) {
					UserDefinedField newUdfld = this.createNewUserDefinedField(importedVariate);
					newUdflds.add(newUdfld);
				}
			}
		}

		// Add All UDFLDS
		this.germplasmManager.addUserDefinedFields(newUdflds);
	}

	protected void processFactors(ImportedGermplasmList importedGermplasmList) throws MiddlewareQueryException {
		List<UserDefinedField> existingUdflds = this.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_NAME);
		List<UserDefinedField> newUdflds = new ArrayList<UserDefinedField>();
		Map<String, String> nameFactors = importedGermplasmList.getImportedGermplasms().get(0).getNameFactors();

		for (ImportedFactor importedFactor : importedGermplasmList.getImportedFactors()) {
			String factor = importedFactor.getFactor();
			if (nameFactors.containsKey(factor) && !this.isUdfldsExist(existingUdflds, factor)) {
				UserDefinedField newUdfld = this.createNewUserDefinedField(importedFactor);
				newUdflds.add(newUdfld);
			}
		}

		// Add All UDFLDS
		this.germplasmManager.addUserDefinedFields(newUdflds);
	}

	private UserDefinedField createNewUserDefinedField(ImportedVariate importedVariate) throws MiddlewareQueryException {
		UserDefinedField newUdfld = new UserDefinedField();
		newUdfld.setFtable(SaveGermplasmListAction.FTABLE_ATTRIBUTE);
		newUdfld.setFtype(importedVariate.getProperty().toUpperCase());
		// GCP-10077 - use name instead of property
		newUdfld.setFcode(importedVariate.getVariate());
		newUdfld.setFname(importedVariate.getDescription());
		String fmt = importedVariate.getScale() + "," + importedVariate.getMethod() + "," + importedVariate.getDataType();
		newUdfld.setFfmt(fmt);
		newUdfld.setFdesc("-");
		newUdfld.setLfldno(0);
		newUdfld.setUser(new User(this.contextUtil.getCurrentUserLocalId()));
		newUdfld.setFdate(Util.getCurrentDateAsIntegerValue());
		newUdfld.setScaleid(0);

		return newUdfld;
	}

	private UserDefinedField createNewUserDefinedField(ImportedFactor importedFactor) throws MiddlewareQueryException {
		UserDefinedField newUdfld = new UserDefinedField();
		newUdfld.setFtable(SaveGermplasmListAction.FTABLE_NAME);
		newUdfld.setFtype(SaveGermplasmListAction.FTYPE_NAME);
		newUdfld.setFcode(importedFactor.getFactor());
		newUdfld.setFname(importedFactor.getDescription());
		String fmt = importedFactor.getScale() + "," + importedFactor.getMethod() + "," + importedFactor.getDataType();
		newUdfld.setFfmt(fmt);
		newUdfld.setFdesc("-");
		newUdfld.setLfldno(0);
		newUdfld.setUser(new User(this.contextUtil.getCurrentUserLocalId()));
		newUdfld.setFdate(Util.getCurrentDateAsIntegerValue());
		newUdfld.setScaleid(0);

		return newUdfld;
	}

	protected void processSeedStockVariate(ImportedVariate importedVariate) throws MiddlewareException {
		String trait = importedVariate.getProperty().toUpperCase();
		String scale = importedVariate.getScale().toUpperCase();
		String method = importedVariate.getMethod().toUpperCase();

		StandardVariable stdVariable = this.ontologyDataManager.findStandardVariableByTraitScaleMethodNames(
				trait, scale, method, contextUtil.getCurrentProgramUUID());
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

			Term dataType = new Term();
			dataType.setId("N".equals(importedVariate.getDataType()) ? TermId.NUMERIC_VARIABLE.getId() : TermId.CHARACTER_VARIABLE.getId());

			stdVariable = new StandardVariable(traitTerm, scaleTerm, methodTerm, dataType, null, PhenotypicType.VARIATE);
			stdVariable.setName(importedVariate.getVariate());
			stdVariable.setDescription(importedVariate.getDescription());

			this.ontologyDataManager.addStandardVariable(stdVariable,contextUtil.getCurrentProgramUUID());
		}

		if (stdVariable.getId() != 0) {
			importedVariate.setScaleId(stdVariable.getId());
			this.seedAmountScaleId = importedVariate.getScaleId();

		}
	}

	private boolean isUdfldsExist(List<UserDefinedField> existingUdflds, String fcode) {
		for (UserDefinedField udfld : existingUdflds) {
			if (udfld.getFcode().equals(fcode)) {
				return true;
			}
		}
		return false;
	}

	private Integer getUdfldID(List<UserDefinedField> existingUdflds, String property) {
		if (existingUdflds != null) {
			for (UserDefinedField udfld : existingUdflds) {
				if (udfld.getFcode().equalsIgnoreCase(property.toUpperCase())) {
					return udfld.getFldno();
				}
			}
		}
		return 0;
	}

	protected void addNewNamesToExistingGermplasm(List<Name> newNames) throws MiddlewareQueryException {
		for (Name name : newNames) {
			this.germplasmManager.addGermplasmName(name);
		}
	}

	private Germplasm getAlreadyAddedGermplasm(GermplasmName germplasmName, Map<Integer, GermplasmName> addedGermplasmNameMap) {
		Germplasm germplasm = germplasmName.getGermplasm();
		for (Integer gid : addedGermplasmNameMap.keySet()) {
			Germplasm addedGermplasm = addedGermplasmNameMap.get(gid).getGermplasm();
			if (addedGermplasm.getGpid1().equals(germplasm.getGpid1()) && addedGermplasm.getGpid2().equals(germplasm.getGpid2())
					&& addedGermplasmNameMap.get(gid).getName().getNval().equals(germplasmName.getName().getNval())) {
				return addedGermplasm;
			}
		}
		return null;
	}

	private GermplasmList saveGermplasmListRecord(GermplasmList germplasmList) throws MiddlewareQueryException {
		int newListId = this.germplasmListManager.addGermplasmList(germplasmList);
		return this.germplasmListManager.getGermplasmListById(newListId);
	}

	private List<UserDefinedField> getUserDefinedFields(int fcodeType) throws MiddlewareQueryException {
		List<UserDefinedField> udFields = new ArrayList<UserDefinedField>();
		if (SaveGermplasmListAction.FCODE_TYPE_ATTRIBUTE == fcodeType) {
			List<UserDefinedField> list =
					this.germplasmManager.getUserDefinedFieldByFieldTableNameAndType(SaveGermplasmListAction.FTABLE_ATTRIBUTE,
							SaveGermplasmListAction.FTYPE_ATTRIBUTE);
			List<UserDefinedField> list2 =
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

	private void saveGermplasmListDataRecords(List<GermplasmName> germplasmNameObjects, GermplasmList list, String filename,
			List<ImportedGermplasm> importedGermplasms) throws MiddlewareQueryException {

		List<GermplasmListData> listToSave = new ArrayList<GermplasmListData>();
		List<UserDefinedField> existingAttrUdflds = this.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_ATTRIBUTE);
		List<UserDefinedField> existingNameUdflds = this.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_NAME);
		List<Attribute> attrs = new ArrayList<Attribute>();
		List<Name> names = new ArrayList<Name>();

		int ctr = 1;
		for (GermplasmName germplasmName : germplasmNameObjects) {

			int entryId = ctr++;
			ImportedGermplasm importedGermplasm = importedGermplasms.get(entryId - 1);
			Integer gid = germplasmName.getGermplasm().getGid();

			String designation = germplasmName.getName().getNval();

			String fileNameWithoutExtension = FileUtils.getFilenameWithoutExtension(filename);
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
			GermplasmListData germplasmListData =
					this.buildGermplasmListData(list, gid, curEntryId, designation, groupName, source, entryCode);

			listToSave.add(germplasmListData);
			Integer lrecId = this.germplasmListManager.addGermplasmListData(germplasmListData);

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

	protected void createDepositInventoryTransaction(GermplasmList list, ImportedGermplasm importedGermplasm, Integer gid, Integer lrecId)
			throws MiddlewareQueryException {
		if (importedGermplasm != null && importedGermplasm.getSeedAmount() != null && importedGermplasm.getSeedAmount() > 0) {

			if (this.gidTransactionSetMap.get(gid) == null) {
				this.gidTransactionSetMap.put(gid, new ArrayList<Transaction>());
			}

			Integer intDate = DateUtil.getCurrentDateAsIntegerValue();

			Integer cropUserId = this.contextUtil.getCurrentUserLocalId();

			Transaction transaction =
					new Transaction(null, cropUserId, this.gidLotMap.get(gid), intDate, TransactionStatus.DEPOSITED.getIntValue(),
							importedGermplasm.getSeedAmount(), SaveGermplasmListAction.INVENTORY_COMMENT, 0, "LIST", list.getId(), lrecId,
							Double.valueOf(0), this.getCropPersonId(cropUserId), importedGermplasm.getInventoryId());
			if (importedGermplasm.getSeedAmount() != null) {
				this.gidTransactionSetMap.get(gid).add(transaction);
			}
		}
	}

	protected Integer getCropPersonId(Integer cropUserId) {
		User cropUser = null;
		Integer cropPersonId = 0;
		try {
			cropUser = this.userDataManager.getUserById(cropUserId);
			if (cropUser != null) {
				cropPersonId = cropUser.getPersonid();
			}
		} catch (MiddlewareQueryException e) {
			Log.error(e.getMessage(), e);
		}

		return cropPersonId;
	}

	private List<Attribute> prepareAllAttributesToAdd(ImportedGermplasm importedGermplasm, List<UserDefinedField> existingUdflds,
			Germplasm germplasm) throws MiddlewareQueryException {
		List<Attribute> attrs = new ArrayList<Attribute>();

		Map<String, String> otherAttributes = importedGermplasm.getAttributeVariates();
		if (otherAttributes != null) {
			for (Map.Entry<String, String> entry : otherAttributes.entrySet()) {
				String code = entry.getKey();
				String value = entry.getValue();

				if (value != null && !"".equals(value.trim())) {
					// Create New Attribute Object
					Attribute newAttr = new Attribute();
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

	private List<Name> prepareAllNamesToAdd(ImportedGermplasm importedGermplasm, List<UserDefinedField> existingUdflds, Germplasm germplasm)
			throws MiddlewareQueryException {
		List<Name> names = new ArrayList<Name>();

		Map<String, String> otherNames = importedGermplasm.getNameFactors();
		if (otherNames != null) {
			for (Map.Entry<String, String> entry : otherNames.entrySet()) {
				String code = entry.getKey();
				String value = entry.getValue();

				if (value != null && !"".equals(value.trim())) {
					// Create New Name Object
					Name newName = new Name();
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

	private GermplasmListData buildGermplasmListData(GermplasmList list, Integer gid, int entryId, String designation, String groupName,
			String source, String entryCode) {
		GermplasmListData germplasmListData = new GermplasmListData();
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

	public void setGermplasmListManager(GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public void setGermplasmManager(GermplasmDataManager germplasmManager) {
		this.germplasmManager = germplasmManager;
	}

	public void setInventoryDataManager(InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}

	public void setOntologyDataManager(OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

	public void setContextUtil(ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	public void setUserDataManager(UserDataManager userDataManager) {
		this.userDataManager = userDataManager;
	}

}
