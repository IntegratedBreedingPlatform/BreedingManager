
package org.generationcp.breeding.manager.listimport.actions;

import com.google.common.collect.Lists;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.data.initializer.ImportedGermplasmListDataInitializer;
import org.generationcp.breeding.manager.exception.BreedingManagerException;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.data.initializer.NameTestDataInitializer;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.ims.EntityType;
import org.generationcp.middleware.pojos.ims.Lot;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.util.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@RunWith(MockitoJUnitRunner.class)
public class SaveGermplasmListActionTest {

	private static final String DERIVED_NAME_CODE = "DRVNM";
	private static final String MATURITY_NAME = "Maturity Name";
	private static final String MATURITY_CODE = "MATURITY";
	private static final String DERIVED_NAME = "Derived Name";
	private static final Integer SEED_AMOUNT_SCALE_ID = 1001;
	private static final int TEST_GID = 1;
	private static final int SAVED_GERMPLASM_LIST_ID = 1;
	private static final int NO_OF_ENTRIES = 10;
	private static final Integer CURRENT_LOCAL_ID = 1;
	private static final int LIST_ID = 1;
	private static final Integer SEED_STORAGE_LOCATION = 2;
	private static final String FTABLE_ATTRIBUTE = "ATRIBUTS";
	private static final String FTYPE_ATTRIBUTE = "ATTRIBUTE";
	private static final String FTYPE_PASSPORT = "PASSPORT";
	public static final String SOURCE_LIST_XLS = "SourceList.xls";

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private GermplasmDataManager germplasmManager;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private PlatformTransactionManager transactionManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private UserService userService;

	@Captor
	private ArgumentCaptor<List<UserDefinedField>> userDefinedFieldsCaptor;

	@InjectMocks
	private SaveGermplasmListAction action;

	private GermplasmList germplasmList;
	private List<GermplasmName> germplasmNameObjects;
	private List<Name> newNames;
	private List<Integer> excludeGermplasmCreateIds;
	private ImportedGermplasmList importedGermplasmList;

	@Before
	public void setup() {
		this.germplasmList = GermplasmListTestDataInitializer.createGermplasmList(SaveGermplasmListActionTest.LIST_ID);
		this.importedGermplasmList =
				ImportedGermplasmListDataInitializer.createImportedGermplasmList(SaveGermplasmListActionTest.NO_OF_ENTRIES, true);
		this.germplasmNameObjects =
				ImportedGermplasmListDataInitializer.createGermplasmNameObjects(SaveGermplasmListActionTest.NO_OF_ENTRIES);
		this.excludeGermplasmCreateIds = ImportedGermplasmListDataInitializer.createListOfGemplasmIds(2);
		this.newNames = GermplasmTestDataInitializer.createNameList(SaveGermplasmListActionTest.NO_OF_ENTRIES);

		Mockito.doReturn(SaveGermplasmListActionTest.CURRENT_LOCAL_ID).when(this.contextUtil).getCurrentWorkbenchUserId();
		Mockito.doReturn(new ArrayList<UserDefinedField>()).when(this.germplasmManager).getUserDefinedFieldByFieldTableNameAndType(
				SaveGermplasmListActionTest.FTABLE_ATTRIBUTE, SaveGermplasmListActionTest.FTYPE_ATTRIBUTE);
		Mockito.doReturn(new ArrayList<UserDefinedField>()).when(this.germplasmManager).getUserDefinedFieldByFieldTableNameAndType(
				SaveGermplasmListActionTest.FTABLE_ATTRIBUTE, SaveGermplasmListActionTest.FTYPE_PASSPORT);
		Mockito.doReturn(SaveGermplasmListActionTest.SAVED_GERMPLASM_LIST_ID).when(this.germplasmListManager)
				.addGermplasmList(Matchers.any(GermplasmList.class));
		Mockito.doReturn(this.germplasmList).when(this.germplasmListManager)
				.getGermplasmListById(SaveGermplasmListActionTest.SAVED_GERMPLASM_LIST_ID);
		for (int i = 1; i <= SaveGermplasmListActionTest.NO_OF_ENTRIES; i++) {
			Mockito.doReturn(GermplasmTestDataInitializer.createGermplasm(i)).when(this.germplasmManager).getGermplasmByGID(i);
		}

	}

	@Test
	public void testSaveRecordsWhenOverridingExistingListUsingTheImportedGermplasmList() throws BreedingManagerException {
		this.action.saveRecords(this.germplasmList, this.germplasmNameObjects, this.newNames, SaveGermplasmListActionTest.SOURCE_LIST_XLS,
				this.excludeGermplasmCreateIds, this.importedGermplasmList, SaveGermplasmListActionTest.SEED_STORAGE_LOCATION);

		try {
			Mockito.verify(this.germplasmListManager, Mockito.times(1))
					.deleteGermplasmListDataByListId(SaveGermplasmListActionTest.SAVED_GERMPLASM_LIST_ID);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail(
					"Expecting that the list entries of the existing list are marked deleted after trying to overwrite a list using germplasm import.");
		}

	}

	@Test
	public void testGermplasmListDataSaving() throws BreedingManagerException {
		this.action.saveRecords(this.germplasmList, this.germplasmNameObjects, this.newNames, SaveGermplasmListActionTest.SOURCE_LIST_XLS,
				this.excludeGermplasmCreateIds, this.importedGermplasmList, SaveGermplasmListActionTest.SEED_STORAGE_LOCATION);
		Mockito.verify(this.germplasmListManager, Mockito.atLeastOnce()).addGermplasmListData(Matchers.anyListOf(GermplasmListData.class));
	}

	@Test
	public void testSaveRecordsWhenOverridingNewListUsingTheImportedGermplasmList() throws BreedingManagerException {
		this.germplasmList.setId(null);
		this.action.saveRecords(this.germplasmList, this.germplasmNameObjects, this.newNames, SaveGermplasmListActionTest.SOURCE_LIST_XLS,
				this.excludeGermplasmCreateIds, this.importedGermplasmList, SaveGermplasmListActionTest.SEED_STORAGE_LOCATION);

		try {
			Mockito.verify(this.germplasmListManager, Mockito.times(0))
					.deleteGermplasmListDataByListId(SaveGermplasmListActionTest.SAVED_GERMPLASM_LIST_ID);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that there is no existing list entries to mark as deleted for new list using germplasm import.");
		}

	}

	@Test
	public void testUpdateExportedGermplasmPreferredNameWhenImportGermplasmListHasNameFactors() {
		final String preferredNameCode = SaveGermplasmListActionTest.DERIVED_NAME_CODE;
		final int noOfEntries = 10;
		final ImportedGermplasmList importedGermplasmList =
				ImportedGermplasmListDataInitializer.createImportedGermplasmList(noOfEntries, true);
		this.action.updateExportedGermplasmPreferredName(preferredNameCode, importedGermplasmList.getImportedGermplasm());

		try {
			Mockito.verify(this.germplasmManager, Mockito.times(noOfEntries)).updateGermplasmPrefName(Matchers.anyInt(),
					Matchers.anyString());
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Not all germplasm's name are updated");
		}
	}

	@Test
	public void testUpdateExportedGermplasmPreferredNameWhenImportGermplasmListHasNoNameFactors() {
		final String preferredNameCode = SaveGermplasmListActionTest.DERIVED_NAME_CODE;
		final int noOfEntries = 10;
		final ImportedGermplasmList importedGermplasmList =
				ImportedGermplasmListDataInitializer.createImportedGermplasmList(noOfEntries, false);
		this.action.updateExportedGermplasmPreferredName(preferredNameCode, importedGermplasmList.getImportedGermplasm());

		try {
			Mockito.verify(this.germplasmManager, Mockito.times(0)).updateGermplasmPrefName(Matchers.anyInt(), Matchers.anyString());
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("No germplasm's will be updated.");
		}
	}

	@Test
	public void testSaveInventory() {

		final Lot testLot = new Lot();
		this.action.getGidLotMap().put(SaveGermplasmListActionTest.TEST_GID, Lists.newArrayList(testLot));
		this.action.getGidLotMapClone().put(SaveGermplasmListActionTest.TEST_GID, Lists.newArrayList(testLot));

		final List<Transaction> testGidTransactions = new ArrayList<>();
		testGidTransactions.add(new Transaction());
		this.action.getGidTransactionSetMap().put(SaveGermplasmListActionTest.TEST_GID, testGidTransactions);

		this.action.saveInventory();

		// The following methods should be called.
		Mockito.verify(this.inventoryDataManager, Mockito.times(1)).addLot(testLot);
		Mockito.verify(this.inventoryDataManager, Mockito.times(1)).addTransactions(testGidTransactions);

	}

	@Test
	public void testSaveInventoryWithEmptyTransaction() {

		final Lot testLot = new Lot();
		this.action.getGidLotMap().put(SaveGermplasmListActionTest.TEST_GID, Lists.newArrayList(testLot));

		// Create an empty Transaction list
		final List<Transaction> testGidTransactions = new ArrayList<>();
		this.action.getGidTransactionSetMap().put(SaveGermplasmListActionTest.TEST_GID, testGidTransactions);

		this.action.saveInventory();

		// The following methods should not be called because there are no items in Transaction list.
		Mockito.verify(this.inventoryDataManager, Mockito.times(0)).addLot(testLot);
		Mockito.verify(this.inventoryDataManager, Mockito.times(0)).addTransactions(testGidTransactions);

	}

	@Test
	public void prepareAllNamesToAddWhereImportedNameNotExistingInTheDB() {
		final ImportedGermplasm importedGermplasm = this.importedGermplasmList.getImportedGermplasm().get(0);
		final List<UserDefinedField> existingNameUdflds = this.action.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_NAME);
		final Germplasm germplasm = this.germplasmNameObjects.get(0).getGermplasm();
		final List<Name> names = this.action.prepareAllNamesToAdd(importedGermplasm, existingNameUdflds, germplasm, null);

		Assert.assertTrue("The imported name is not existing in the database so the names list should not be empty", !names.isEmpty());
		final Name resultName = names.get(0);
		Assert.assertEquals("The gid should be " + germplasm.getGid(), germplasm.getGid(), resultName.getGermplasmId());
		Assert.assertEquals("The typeid should be 0", 0, resultName.getTypeId().intValue());
		Assert.assertEquals("The nstat should be 0", 0, resultName.getNstat().intValue());
		Assert.assertEquals("The user id should be 1", 1, resultName.getUserId().intValue());
		Assert.assertEquals("The nval should be 'DRVNM 1'", "DRVNM 1", resultName.getNval());
		Assert.assertEquals("The ndate should be " + Util.getCurrentDateAsIntegerValue(), Util.getCurrentDateAsIntegerValue(),
				resultName.getNdate());
		Assert.assertEquals("The reference id should be 0", 0, resultName.getReferenceId().intValue());
	}

	@Test
	public void prepareAllNamesToAddWhereImportedNameExistingInTheDB() {
		final ImportedGermplasm importedGermplasm = this.importedGermplasmList.getImportedGermplasm().get(0);
		final List<UserDefinedField> existingNameUdflds = this.action.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_NAME);
		final Germplasm germplasm = this.germplasmNameObjects.get(0).getGermplasm();
		final List<Name> existingNames = NameTestDataInitializer.createNameList(1);
		final List<Name> names = this.action.prepareAllNamesToAdd(importedGermplasm, existingNameUdflds, germplasm, existingNames);

		Assert.assertTrue("The imported name is already existing in the database so the names list should be empty", names.isEmpty());
	}

	@Test
	public void testSaveGermplasmListDataRecordsWhereImportedNamesNotExistingInTheDB() {
		final List<ImportedGermplasm> importedGermplasms = this.importedGermplasmList.getImportedGermplasm();
		this.action.saveGermplasmListDataRecords(this.germplasmNameObjects, this.germplasmList, importedGermplasms,
				this.excludeGermplasmCreateIds);
		Mockito.verify(this.germplasmManager, Mockito.times(1)).addGermplasmName(Matchers.anyListOf(Name.class));
	}

	@Test
	public void testSaveGermplasmListDataRecordsWhereImportedNamesExistingInTheDB() {
		final List<ImportedGermplasm> importedGermplasms = this.importedGermplasmList.getImportedGermplasm();
		Mockito.doReturn(this.createNamesMap(importedGermplasms.size())).when(this.germplasmManager)
				.getNamesByGidsAndNTypeIdsInMap(Matchers.anyListOf(Integer.class), Matchers.anyListOf(Integer.class));
		this.action.saveGermplasmListDataRecords(this.germplasmNameObjects, this.germplasmList, importedGermplasms,
				this.excludeGermplasmCreateIds);
		Mockito.verify(this.germplasmManager, Mockito.times(0)).addGermplasmName(Matchers.anyListOf(Name.class));
	}

	@Test
	public void testGetUserDefinedFieldsForAttributeTypes() {
		this.action.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_ATTRIBUTE);

		// Verify that "Attribute" and "Passport" attribute types were retrieved from UDFLDS table
		Mockito.verify(this.germplasmManager, Mockito.times(1)).getUserDefinedFieldByFieldTableNameAndType(
				SaveGermplasmListAction.FTABLE_ATTRIBUTE, SaveGermplasmListAction.FTYPE_ATTRIBUTE);
		Mockito.verify(this.germplasmManager, Mockito.times(1)).getUserDefinedFieldByFieldTableNameAndType(
				SaveGermplasmListAction.FTABLE_ATTRIBUTE, SaveGermplasmListAction.FTYPE_PASSPORT);
		Mockito.verifyNoMoreInteractions(this.germplasmManager);
	}

	@Test
	public void testGetUserDefinedFieldsForNameTypes() {
		this.action.getUserDefinedFields(SaveGermplasmListAction.FCODE_TYPE_NAME);

		// Verify that name types were retrieved from UDFLDS table
		Mockito.verify(this.germplasmManager, Mockito.times(1))
				.getUserDefinedFieldByFieldTableNameAndType(SaveGermplasmListAction.FTABLE_NAME, SaveGermplasmListAction.FTYPE_NAME);
		Mockito.verifyNoMoreInteractions(this.germplasmManager);
	}

	@Test
	public void testProcessVariatesWhenAllAttributeTypesExistInDB() throws BreedingManagerException {
		// Setup variates that are attributes types for imported list
		final Map<String, String> attributeTypesMap = new HashMap<>();
		attributeTypesMap.put("NOTES", "note for germplasm 1");
		attributeTypesMap.put("MATURITY", "immature");
		attributeTypesMap.put("ANCEST", "long lineage");
		this.importedGermplasmList.getImportedGermplasm().get(0).setAttributeVariates(attributeTypesMap);
		ImportedGermplasmListDataInitializer.addImportedVariates(this.importedGermplasmList, "NOTES", "MATURITY", "ANCEST");

		// Mock UserDefinedFields of attribute types from DB. Matching should be case insensitive
		this.setupAttributeTypeUserDefinedFieldsMocks();

		// Call method to test
		this.action.processVariates(this.importedGermplasmList);

		// Verify that no new attribute types were added to UDFLDS table
		Mockito.verify(this.germplasmManager, Mockito.times(0)).addUserDefinedFields(Matchers.anyListOf(UserDefinedField.class));
	}

	@Test
	public void testProcessVariatesWhenNewAttributeTypesNotInDB() throws BreedingManagerException {
		// Setup variates that are attributes types for imported list. "TESTATTR" does not exist in DB yet as attribute type
		final String newAttributeType = "TESTATTR";
		final Map<String, String> attributeTypesMap = new HashMap<>();
		attributeTypesMap.put("NOTES", "note for germplasm 1");
		attributeTypesMap.put(newAttributeType, "sample value for test attribute");
		this.importedGermplasmList.getImportedGermplasm().get(0).setAttributeVariates(attributeTypesMap);
		ImportedGermplasmListDataInitializer.addImportedVariates(this.importedGermplasmList, "NOTES", newAttributeType);

		// Mock UserDefinedFields of attribute types from DB. Matching should be case insensitive
		this.setupAttributeTypeUserDefinedFieldsMocks();

		// Call method to test
		this.action.processVariates(this.importedGermplasmList);

		// Verify that new attribute type TESTATTR was added to UDFLDS table
		Mockito.verify(this.germplasmManager, Mockito.times(1)).addUserDefinedFields(this.userDefinedFieldsCaptor.capture());
		final List<UserDefinedField> newUserDefinedFields = this.userDefinedFieldsCaptor.getValue();
		Assert.assertEquals(1, newUserDefinedFields.size());
		Assert.assertEquals(SaveGermplasmListAction.FTABLE_ATTRIBUTE, newUserDefinedFields.get(0).getFtable());
		Assert.assertEquals(SaveGermplasmListAction.FTYPE_ATTRIBUTE, newUserDefinedFields.get(0).getFtype());
		Assert.assertEquals(newAttributeType, newUserDefinedFields.get(0).getFcode());
	}

	@Test
	public void testProcessGermplasmNamesAndLotsAllEntriesMatchedToExistingGermplasm() {
		// Set existing GIDs as "finalized" (or flagged as real GIDs in DB versus just a temporary GID)
		for (final GermplasmName germplasmName : this.germplasmNameObjects) {
			germplasmName.setIsGidMatched(true);
		}

		final List<Integer> matchedGids = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		// Method to test
		this.action.processGermplasmNamesAndLots(this.germplasmNameObjects, matchedGids, SaveGermplasmListActionTest.SEED_STORAGE_LOCATION);

		// Verify that no new germplasm was saved
		Mockito.verify(this.germplasmManager, Mockito.times(0)).addGermplasm(Matchers.any(Germplasm.class), Matchers.any(Name.class));
	}

	@Test
	public void testProcessGermplasmNamesAndLotsCreateNewGermplasmForAllEntries() {
		// Method to test
		this.action.processGermplasmNamesAndLots(this.germplasmNameObjects, new ArrayList<Integer>(),
				SaveGermplasmListActionTest.SEED_STORAGE_LOCATION);

		// Verify that new germplasm record was saved per each entry
		Mockito.verify(this.germplasmManager, Mockito.times(SaveGermplasmListActionTest.NO_OF_ENTRIES))
				.addGermplasm(Matchers.any(Germplasm.class), Matchers.any(Name.class));
	}

	@Test
	public void testProcessGermplasmNamesAndLotsReuseGermplasmForSomeEntries() {
		final int originalIndex1 = 0;
		final int originalIndex2 = 1;
		final int dupeIndex1 = 5;
		final int dupeIndex2 = 6;
		// Mark two entries as duplicate of previous entries
		this.germplasmNameObjects.get(dupeIndex1).getGermplasm()
				.setGid(this.germplasmNameObjects.get(originalIndex1).getGermplasm().getGid());
		this.germplasmNameObjects.get(dupeIndex2).getGermplasm()
				.setGid(this.germplasmNameObjects.get(originalIndex2).getGermplasm().getGid());

		// Method to test
		this.action.processGermplasmNamesAndLots(this.germplasmNameObjects, new ArrayList<Integer>(),
				SaveGermplasmListActionTest.SEED_STORAGE_LOCATION);

		// Verify that no unique germplasm record was created for duplicate entries
		Mockito.verify(this.germplasmManager, Mockito.times(SaveGermplasmListActionTest.NO_OF_ENTRIES - 2))
				.addGermplasm(Matchers.any(Germplasm.class), Matchers.any(Name.class));
	}

	@Test
	public void testProcessGermplasmNamesAndLotsForNewGermplasmWithInventory() {
		// Indicate that inventory is present
		this.action.setSeedAmountScaleId(SaveGermplasmListActionTest.SEED_AMOUNT_SCALE_ID);

		Mockito.when(this.germplasmManager.addGermplasm(Matchers.any(Germplasm.class), Matchers.any(Name.class))).thenReturn(101, 102, 103,
				104, 105, 106, 107, 108, 109, 110);

		// Method to test
		this.action.processGermplasmNamesAndLots(this.germplasmNameObjects, new ArrayList<Integer>(),
				SaveGermplasmListActionTest.SEED_STORAGE_LOCATION);

		final Map<Integer, List<Lot>> gidLotMap = this.action.getGidLotMap();
		Assert.assertNotNull(gidLotMap);
		Assert.assertEquals(SaveGermplasmListActionTest.NO_OF_ENTRIES, gidLotMap.size());
		for (final Entry<Integer, List<Lot>> entry : gidLotMap.entrySet()) {
			// check that the entity IDS of the lots created are for dummy GIDS of created germplasm
			final Integer gid = entry.getKey();
			final List<Lot> lots = entry.getValue();
			final Lot lot = lots.get(0);
			Assert.assertEquals(gid, lot.getEntityId());
			// Check that GID used for lots is the one generated from Middleware mock when germplasm was added
			Assert.assertTrue(gid > 100);
		}
	}

	@Test
	public void testProcessGermplasmNamesAndLotsForExistingGermplasmAddingInventory() {
		// Indicate that inventory is present
		this.action.setSeedAmountScaleId(SaveGermplasmListActionTest.SEED_AMOUNT_SCALE_ID);
		// Set existing GIDs as "finalized" (or flagged as real GIDs in DB versus just a temporary GID)
		for (final GermplasmName germplasmName : this.germplasmNameObjects) {
			germplasmName.setIsGidMatched(true);
		}

		final List<Integer> matchedGids = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		// Method to test
		this.action.processGermplasmNamesAndLots(this.germplasmNameObjects, matchedGids, SaveGermplasmListActionTest.SEED_STORAGE_LOCATION);

		final Map<Integer, List<Lot>> gidLotMap = this.action.getGidLotMap();
		Assert.assertNotNull(gidLotMap);
		Assert.assertEquals(SaveGermplasmListActionTest.NO_OF_ENTRIES, gidLotMap.size());
		for (final Entry<Integer, List<Lot>> entry : gidLotMap.entrySet()) {
			final Integer gid = entry.getKey();
			final List<Lot> lots = entry.getValue();
			final Lot lot = lots.get(0);
			Assert.assertEquals(gid, lot.getEntityId());
			Assert.assertEquals(SaveGermplasmListActionTest.SEED_AMOUNT_SCALE_ID, lot.getScaleId());
			Assert.assertEquals(EntityType.GERMPLSM.name(), lot.getEntityType());
			Assert.assertEquals(SaveGermplasmListActionTest.SEED_STORAGE_LOCATION, lot.getLocationId());
			Assert.assertEquals(SaveGermplasmListAction.INVENTORY_COMMENT, lot.getComments());
			Assert.assertEquals(SaveGermplasmListActionTest.CURRENT_LOCAL_ID, lot.getUserId());
			Assert.assertEquals(new Integer("0"), lot.getStatus());
		}
	}

	@Test
	public void testProcessGermplasmNamesAndLotsWhenTemporaryGidInMatchedGids() {
		// Set 1st entry as matched to GID 10. The 10th entry will have temporary GID = 10
		final int gidMatched = 10;
		this.germplasmNameObjects.get(0).getGermplasm().setGid(gidMatched);
		final List<Integer> matchedGids = Arrays.asList(gidMatched, 2, 3, 4, 5, 6, 7, 8, 9);
		// Set all entries to have "finalized" GID (or flagged as real GIDs in DB versus just a temporary GID) except for last entry
		for (int i = 0; i < this.germplasmNameObjects.size() - 1; i++) {
			final GermplasmName germplasmName = this.germplasmNameObjects.get(i);
			germplasmName.setIsGidMatched(true);
		}

		// Method to test
		this.action.processGermplasmNamesAndLots(this.germplasmNameObjects, matchedGids, SaveGermplasmListActionTest.SEED_STORAGE_LOCATION);

		// Verify that new germplasm record was created for 10th entry
		final ArgumentCaptor<Germplasm> germplasmCaptor = ArgumentCaptor.forClass(Germplasm.class);
		Mockito.verify(this.germplasmManager, Mockito.times(1)).addGermplasm(germplasmCaptor.capture(), Matchers.any(Name.class));

	}

	@Test
	public void testCreateNewUserDefinedField() {
		final String fieldFormat = "Text,Observed,C";

		// Test creation of new passport attribute type UDFLD
		final UserDefinedField newAttributeType =
				this.action.createNewUserDefinedField(SaveGermplasmListAction.FTABLE_ATTRIBUTE, SaveGermplasmListAction.FTYPE_PASSPORT,
						SaveGermplasmListActionTest.MATURITY_CODE, SaveGermplasmListActionTest.MATURITY_NAME, fieldFormat);
		Assert.assertEquals(SaveGermplasmListAction.FTABLE_ATTRIBUTE, newAttributeType.getFtable());
		Assert.assertEquals(SaveGermplasmListAction.FTYPE_PASSPORT, newAttributeType.getFtype());
		Assert.assertEquals(SaveGermplasmListActionTest.MATURITY_CODE, newAttributeType.getFcode());
		Assert.assertEquals(SaveGermplasmListActionTest.MATURITY_NAME, newAttributeType.getFname());
		Assert.assertEquals(fieldFormat, newAttributeType.getFfmt());

		// Test creation of new name type UDFLD
		final String nameTypeFieldFormat = SaveGermplasmListActionTest.DERIVED_NAME + "," + fieldFormat;
		final UserDefinedField newNametype =
				this.action.createNewUserDefinedField(SaveGermplasmListAction.FTABLE_NAME, SaveGermplasmListAction.FTYPE_NAME,
						SaveGermplasmListActionTest.DERIVED_NAME_CODE, SaveGermplasmListActionTest.DERIVED_NAME, nameTypeFieldFormat);
		Assert.assertEquals(SaveGermplasmListAction.FTABLE_NAME, newNametype.getFtable());
		Assert.assertEquals(SaveGermplasmListAction.FTYPE_NAME, newNametype.getFtype());
		Assert.assertEquals(SaveGermplasmListActionTest.DERIVED_NAME_CODE, newNametype.getFcode());
		Assert.assertEquals(SaveGermplasmListActionTest.DERIVED_NAME, newNametype.getFname());
		Assert.assertEquals(nameTypeFieldFormat, newNametype.getFfmt());
	}

	@Test
	public void testProcessFactorsNoNewNameTypeToSave() {
		// Mock that name types in file already exists in DB
		this.setupNameTypeUserDefinedFieldsMocks();

		// Add name factors DRVNM and TESTNAME to imported list
		this.action.processFactors(this.importedGermplasmList);

		// Verify that no new name type was saved
		Mockito.verify(this.germplasmManager, Mockito.times(0)).addUserDefinedFields(Matchers.anyListOf(UserDefinedField.class));
	}

	@Test
	public void testProcessFactorsWithNewNameTypeToSave() {
		// Add name factors DRVNM and TESTNAME to imported list
		final String newNameType = "TESTNAME";
		final Map<String, String> nameFactors = new HashMap<>();
		nameFactors.put(SaveGermplasmListActionTest.DERIVED_NAME_CODE, "DRVNM-1");
		nameFactors.put(newNameType, newNameType + "-1");
		this.importedGermplasmList.getImportedGermplasm().get(0).setNameFactors(nameFactors);
		ImportedGermplasmListDataInitializer.addNameFactors(this.importedGermplasmList, nameFactors.keySet().toArray(new String[0]));

		// Mock that DRVNM name type already exists in DB, but not TESTNAME
		this.setupNameTypeUserDefinedFieldsMocks();

		this.action.processFactors(this.importedGermplasmList);

		// Verify that TESTNAME was added as new name type to UDFLDS table
		Mockito.verify(this.germplasmManager, Mockito.times(1)).addUserDefinedFields(this.userDefinedFieldsCaptor.capture());
		final List<UserDefinedField> newUserDefinedFields = this.userDefinedFieldsCaptor.getValue();
		Assert.assertEquals(1, newUserDefinedFields.size());
		Assert.assertEquals(SaveGermplasmListAction.FTABLE_NAME, newUserDefinedFields.get(0).getFtable());
		Assert.assertEquals(SaveGermplasmListAction.FTYPE_NAME, newUserDefinedFields.get(0).getFtype());
		Assert.assertEquals(newNameType, newUserDefinedFields.get(0).getFcode());
	}

	private void setupAttributeTypeUserDefinedFieldsMocks() {
		final List<UserDefinedField> attributeTypeFields = new ArrayList<>();
		attributeTypeFields.add(new UserDefinedField(1, SaveGermplasmListAction.FTABLE_ATTRIBUTE, SaveGermplasmListAction.FTYPE_ATTRIBUTE,
				"Notes", "Notes Name", "", "", 1, 1, 1, 1));
		attributeTypeFields.add(new UserDefinedField(2, SaveGermplasmListAction.FTABLE_ATTRIBUTE, SaveGermplasmListAction.FTYPE_ATTRIBUTE,
				SaveGermplasmListActionTest.MATURITY_CODE, SaveGermplasmListActionTest.MATURITY_NAME, "", "", 1, 1, 1, 1));
		attributeTypeFields.add(new UserDefinedField(3, SaveGermplasmListAction.FTABLE_ATTRIBUTE, SaveGermplasmListAction.FTYPE_ATTRIBUTE,
				"IPSTAT", "IP Status", "", "", 1, 1, 1, 1));
		attributeTypeFields.add(new UserDefinedField(4, SaveGermplasmListAction.FTABLE_ATTRIBUTE, SaveGermplasmListAction.FTYPE_PASSPORT,
				"Ancest", "Ancestry Name", "", "", 1, 1, 1, 1));
		attributeTypeFields.add(new UserDefinedField(5, SaveGermplasmListAction.FTABLE_ATTRIBUTE, SaveGermplasmListAction.FTYPE_PASSPORT,
				"GROWER", "Grower's name", "", "", 1, 1, 1, 1));
		Mockito.doReturn(attributeTypeFields).when(this.germplasmManager).getUserDefinedFieldByFieldTableNameAndType(Matchers.anyString(),
				Matchers.anyString());
	}

	private void setupNameTypeUserDefinedFieldsMocks() {
		final List<UserDefinedField> nameTypes = new ArrayList<>();
		nameTypes.add(new UserDefinedField(1, SaveGermplasmListAction.FTABLE_NAME, SaveGermplasmListAction.FTYPE_NAME,
				SaveGermplasmListActionTest.DERIVED_NAME_CODE, SaveGermplasmListActionTest.DERIVED_NAME, "", "", 1, 1, 1, 1));
		Mockito.doReturn(nameTypes).when(this.germplasmManager)
				.getUserDefinedFieldByFieldTableNameAndType(SaveGermplasmListAction.FTABLE_NAME, SaveGermplasmListAction.FTYPE_NAME);
	}

	private Map<Integer, List<Name>> createNamesMap(final int size) {
		final Map<Integer, List<Name>> namesMap = new HashMap<Integer, List<Name>>();
		final List<Name> existingNames = NameTestDataInitializer.createNameList(size);
		for (int i = 0; i < size; i++) {
			namesMap.put(i + 1, existingNames);
		}
		return namesMap;
	}
}
