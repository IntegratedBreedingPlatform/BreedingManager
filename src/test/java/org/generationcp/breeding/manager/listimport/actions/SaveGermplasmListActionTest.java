
package org.generationcp.breeding.manager.listimport.actions;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.data.initializer.ImportedGermplasmListDataInitializer;
import org.generationcp.breeding.manager.exception.BreedingManagerException;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
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
import org.generationcp.middleware.pojos.ims.Lot;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.util.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;

@RunWith(MockitoJUnitRunner.class)
public class SaveGermplasmListActionTest {

	private static final int TEST_GID = 1;
	private static final int SAVED_GERMPLASM_LIST_ID = 1;
	private static final int NO_OF_ENTRIES = 5;
	private static final int CURRENT_LOCAL_ID = 1;
	private static final String PROGRAM_UUID = "1234567890";
	private static final int LIST_ID = 1;
	private static final Integer SEED_STORAGE_LOCATION = 2;
	private static final String FTABLE_ATTRIBUTE = "ATRIBUTS";
	private static final String FTYPE_ATTRIBUTE = "ATTRIBUTE";
	private static final String FTYPE_PASSPORT = "PASSPORT";
	public static final String TEST_SOURCE = "TEST SOURCE";
	public static final String SOURCE_LIST_XLS = "SourceList.xls";

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private GermplasmDataManager germplasmManager;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private InventoryService inventoryService;

	@Mock
	private PlatformTransactionManager transactionManager;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private UserDataManager userDataManager;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private SaveGermplasmListAction action;

	private GermplasmList germplasmList;
	private List<GermplasmName> germplasmNameObjects;
	private List<Name> newNames;
	private List<Integer> doNotCreateGermplasmsWithId;
	private ImportedGermplasmList importedGermplasmList;

	private ImportedGermplasmListDataInitializer importedGermplasmListInitializer;

	@Before
	public void setup() {
		// initializer
		this.importedGermplasmListInitializer = new ImportedGermplasmListDataInitializer();

		this.germplasmList = GermplasmListTestDataInitializer.createGermplasmList(SaveGermplasmListActionTest.LIST_ID);
		this.importedGermplasmList =
				this.importedGermplasmListInitializer.createImportedGermplasmList(SaveGermplasmListActionTest.NO_OF_ENTRIES, true);
		this.germplasmNameObjects =
				this.importedGermplasmListInitializer.createGermplasmNameObjects(SaveGermplasmListActionTest.NO_OF_ENTRIES);
		this.doNotCreateGermplasmsWithId = this.importedGermplasmListInitializer.createListOfGemplasmIds(2);
		this.newNames = GermplasmTestDataInitializer.createNameList(SaveGermplasmListActionTest.NO_OF_ENTRIES);

		Mockito.doReturn(SaveGermplasmListActionTest.PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
		Mockito.doReturn(SaveGermplasmListActionTest.CURRENT_LOCAL_ID).when(this.contextUtil).getCurrentUserLocalId();
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
	public void testGetCropPersonId_WithNullCropUserId() throws MiddlewareQueryException {
		final Integer cropUserId = 0;
		Mockito.when(this.userDataManager.getUserById(cropUserId)).thenReturn(null);

		Assert.assertEquals("Expecting to return a blank for null userid but didn't.", this.action.getCropPersonId(cropUserId).intValue(),
				0);
	}

	@Test
	public void testGetCropPersonId_WithValidCropUserId() throws MiddlewareQueryException {
		final Integer cropUserId = 1;
		final Integer personUserId = 2;

		final User user = new User();
		user.setUserid(cropUserId);
		user.setPersonid(personUserId);

		Mockito.when(this.userDataManager.getUserById(cropUserId)).thenReturn(user);

		Assert.assertEquals("Expecting to return a person id from the userid but didn't.", this.action.getCropPersonId(cropUserId),
				personUserId);
	}

	@Test
	public void testSaveRecordsWhenOverridingExistingListUsingTheImportedGermplasmList() throws BreedingManagerException {
		this.action.saveRecords(this.germplasmList, this.germplasmNameObjects, this.newNames, SaveGermplasmListActionTest.SOURCE_LIST_XLS,
				this.doNotCreateGermplasmsWithId, this.importedGermplasmList, SaveGermplasmListActionTest.SEED_STORAGE_LOCATION);

		try {
			Mockito.verify(this.germplasmListManager, Mockito.times(1))
					.deleteGermplasmListDataByListId(SaveGermplasmListActionTest.SAVED_GERMPLASM_LIST_ID);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail(
					"Expecting that the list entries of the existing list are marked deleted after trying to overwrite a list using germplasm import.");
		}

	}

	@Test
	public void testBlankSourceSaving() throws BreedingManagerException {
		final ArgumentCaptor<GermplasmListData> listDataArgumentCaptor = ArgumentCaptor.forClass(GermplasmListData.class);

		this.action.saveRecords(this.germplasmList, this.germplasmNameObjects, this.newNames, SaveGermplasmListActionTest.SOURCE_LIST_XLS,
				this.doNotCreateGermplasmsWithId, this.importedGermplasmList, SaveGermplasmListActionTest.SEED_STORAGE_LOCATION);

		try {
			Mockito.verify(this.germplasmListManager, Mockito.atLeastOnce()).addGermplasmListData(listDataArgumentCaptor.capture());
			Assert.assertEquals("Imported germplasm data with null or empty source must be saved as blank", "",
					listDataArgumentCaptor.getValue().getSeedSource());
		} catch (final TooLittleActualInvocations e) {
			Assert.fail(
					"Expecting that the list entries of the existing list are marked deleted after trying to overwrite a list using germplasm import.");
		}
	}

	@Test
	public void testNonBlankSourceSaving() throws BreedingManagerException {
		final ArgumentCaptor<GermplasmListData> listDataArgumentCaptor = ArgumentCaptor.forClass(GermplasmListData.class);

		// provide a non null source value
		for (final ImportedGermplasm importedGermplasm : this.importedGermplasmList.getImportedGermplasms()) {
			importedGermplasm.setSource(SaveGermplasmListActionTest.TEST_SOURCE);
		}

		this.action.saveRecords(this.germplasmList, this.germplasmNameObjects, this.newNames, SaveGermplasmListActionTest.SOURCE_LIST_XLS,
				this.doNotCreateGermplasmsWithId, this.importedGermplasmList, SaveGermplasmListActionTest.SEED_STORAGE_LOCATION);

		try {
			Mockito.verify(this.germplasmListManager, Mockito.atLeastOnce()).addGermplasmListData(listDataArgumentCaptor.capture());
			Assert.assertEquals("Imported germplasm data with non empty source must use that value",
					SaveGermplasmListActionTest.TEST_SOURCE, listDataArgumentCaptor.getValue().getSeedSource());
		} catch (final TooLittleActualInvocations e) {
			Assert.fail(
					"Expecting that the list entries of the existing list are marked deleted after trying to overwrite a list using germplasm import.");
		}
	}

	@Test
	public void testSaveRecordsWhenOverridingNewListUsingTheImportedGermplasmList() throws BreedingManagerException {
		this.germplasmList.setId(null);
		this.action.saveRecords(this.germplasmList, this.germplasmNameObjects, this.newNames, SaveGermplasmListActionTest.SOURCE_LIST_XLS,
				this.doNotCreateGermplasmsWithId, this.importedGermplasmList, SaveGermplasmListActionTest.SEED_STORAGE_LOCATION);

		try {
			Mockito.verify(this.germplasmListManager, Mockito.times(0))
					.deleteGermplasmListDataByListId(SaveGermplasmListActionTest.SAVED_GERMPLASM_LIST_ID);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that there is no existing list entries to mark as deleted for new list using germplasm import.");
		}

	}

	@Test
	public void testUpdateExportedGermplasmPreferredNameWhenImportGermplasmListHasNameFactors() {
		final String preferredNameCode = "DRVNM";
		final int noOfEntries = 10;
		final ImportedGermplasmList importedGermplasmList =
				this.importedGermplasmListInitializer.createImportedGermplasmList(noOfEntries, true);
		this.action.updateExportedGermplasmPreferredName(preferredNameCode, importedGermplasmList.getImportedGermplasms());

		try {
			Mockito.verify(this.germplasmManager, Mockito.times(noOfEntries)).updateGermplasmPrefName(Matchers.anyInt(),
					Matchers.anyString());
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Not all germplasm's name are updated");
		}
	}

	@Test
	public void testUpdateExportedGermplasmPreferredNameWhenImportGermplasmListHasNoNameFactors() {
		final String preferredNameCode = "DRVNM";
		final int noOfEntries = 10;
		final ImportedGermplasmList importedGermplasmList =
				this.importedGermplasmListInitializer.createImportedGermplasmList(noOfEntries, false);
		this.action.updateExportedGermplasmPreferredName(preferredNameCode, importedGermplasmList.getImportedGermplasms());

		try {
			Mockito.verify(this.germplasmManager, Mockito.times(0)).updateGermplasmPrefName(Matchers.anyInt(), Matchers.anyString());
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("No germplasm's will be updated.");
		}
	}

	@Test
	public void testPrepareAllAttributesToAdd() {
		final ImportedGermplasm importedGermplasm = this.importedGermplasmList.getImportedGermplasms().get(0);
		final List<UserDefinedField> existingUdflds = new ArrayList<UserDefinedField>();
		final UserDefinedField udfld = new UserDefinedField(1);
		udfld.setFcode("Fcode");
		existingUdflds.add(udfld);

		final Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(1);
		final List<Attribute> attributes = this.action.prepareAllAttributesToAdd(importedGermplasm, existingUdflds, germplasm);

		for (final Attribute attr : attributes) {
			Assert.assertEquals("The attribute's germplasm id should be " + germplasm.getGid(), germplasm.getGid(), attr.getGermplasmId());
			Assert.assertEquals("The attribute's type id should be 0.", 0, attr.getTypeId().intValue());
			Assert.assertEquals("The attribute's user id should be " + SaveGermplasmListActionTest.CURRENT_LOCAL_ID,
					SaveGermplasmListActionTest.CURRENT_LOCAL_ID, attr.getUserId().intValue());
			Assert.assertEquals("The attribute's location id should be 0.", attr.getLocationId().intValue(), 0);
			Assert.assertEquals("The attribute's reference id should be 0.", attr.getReferenceId().intValue(), 0);
			Assert.assertEquals("The attribute's a date should be " + Util.getCurrentDateAsIntegerValue(),
					Util.getCurrentDateAsIntegerValue(), attr.getAdate());
		}
	}

	@Test
	public void testSaveInventory() {

		final Lot testLot = new Lot();
		this.action.getGidLotMap().put(SaveGermplasmListActionTest.TEST_GID, testLot);

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
		this.action.getGidLotMap().put(SaveGermplasmListActionTest.TEST_GID, testLot);

		// Create an empty Transaction list
		final List<Transaction> testGidTransactions = new ArrayList<>();
		this.action.getGidTransactionSetMap().put(SaveGermplasmListActionTest.TEST_GID, testGidTransactions);

		this.action.saveInventory();

		// The following methods should not be called because there are no items in Transaction list.
		Mockito.verify(this.inventoryDataManager, Mockito.times(0)).addLot(testLot);
		Mockito.verify(this.inventoryDataManager, Mockito.times(0)).addTransactions(testGidTransactions);

	}

}
