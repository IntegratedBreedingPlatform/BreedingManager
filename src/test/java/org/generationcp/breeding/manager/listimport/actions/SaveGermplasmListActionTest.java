
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
import org.generationcp.middleware.service.api.InventoryService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;

@RunWith(MockitoJUnitRunner.class)
public class SaveGermplasmListActionTest {

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

	@Before
	public void setup() {
		this.germplasmList = GermplasmListTestDataInitializer.createGermplasmList(LIST_ID);
		this.importedGermplasmList = ImportedGermplasmListDataInitializer.createImportedGermplasmList(NO_OF_ENTRIES, true);
		this.germplasmNameObjects = ImportedGermplasmListDataInitializer.createGermplasmNameObjects(NO_OF_ENTRIES);
		this.doNotCreateGermplasmsWithId = ImportedGermplasmListDataInitializer.createListOfGemplasmIds(2);
		this.newNames = GermplasmTestDataInitializer.createNameList(NO_OF_ENTRIES);

		Mockito.doReturn(PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
		Mockito.doReturn(CURRENT_LOCAL_ID).when(this.contextUtil).getCurrentUserLocalId();
		Mockito.doReturn(new ArrayList<UserDefinedField>()).when(this.germplasmManager)
				.getUserDefinedFieldByFieldTableNameAndType(FTABLE_ATTRIBUTE, FTYPE_ATTRIBUTE);
		Mockito.doReturn(new ArrayList<UserDefinedField>()).when(this.germplasmManager)
				.getUserDefinedFieldByFieldTableNameAndType(FTABLE_ATTRIBUTE, FTYPE_PASSPORT);
		Mockito.doReturn(SAVED_GERMPLASM_LIST_ID).when(this.germplasmListManager).addGermplasmList(Mockito.any(GermplasmList.class));
		Mockito.doReturn(this.germplasmList).when(this.germplasmListManager).getGermplasmListById(SAVED_GERMPLASM_LIST_ID);

		for (int i = 1; i <= NO_OF_ENTRIES; i++) {
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
		this.action.saveRecords(this.germplasmList, this.germplasmNameObjects, this.newNames, SOURCE_LIST_XLS,
				this.doNotCreateGermplasmsWithId, this.importedGermplasmList, SEED_STORAGE_LOCATION);

		try {
			Mockito.verify(this.germplasmListManager, Mockito.times(1)).deleteGermplasmListDataByListId(SAVED_GERMPLASM_LIST_ID);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting that the list entries of the existing list are marked deleted after trying to overwrite a list using germplasm import.");
		}

	}

	@Test
	public void testBlankSourceSaving() throws BreedingManagerException {
		final ArgumentCaptor<GermplasmListData> listData = ArgumentCaptor.forClass(GermplasmListData.class);

		this.action.saveRecords(this.germplasmList, this.germplasmNameObjects, this.newNames, SOURCE_LIST_XLS,
				this.doNotCreateGermplasmsWithId, this.importedGermplasmList, SEED_STORAGE_LOCATION);

		try {
			Mockito.verify(this.germplasmListManager, Mockito.atLeastOnce()).addGermplasmListData(listData.capture());
			Assert.assertEquals("Imported germplasm data with null or empty source must be saved as blank", "", listData.getValue()
					.getSeedSource());
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting that the list entries of the existing list are marked deleted after trying to overwrite a list using germplasm import.");
		}
	}

	@Test
	public void testNonBlankSourceSaving() throws BreedingManagerException {
		final ArgumentCaptor<GermplasmListData> listData = ArgumentCaptor.forClass(GermplasmListData.class);

		// provide a non null source value
		for (final ImportedGermplasm importedGermplasm : this.importedGermplasmList.getImportedGermplasms()) {
			importedGermplasm.setSource(TEST_SOURCE);
		}

		this.action.saveRecords(this.germplasmList, this.germplasmNameObjects, this.newNames, SOURCE_LIST_XLS,
				this.doNotCreateGermplasmsWithId, this.importedGermplasmList, SEED_STORAGE_LOCATION);

		try {
			Mockito.verify(this.germplasmListManager, Mockito.atLeastOnce()).addGermplasmListData(listData.capture());
			Assert.assertEquals("Imported germplasm data with non empty source must use that value", TEST_SOURCE, listData.getValue()
					.getSeedSource());
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting that the list entries of the existing list are marked deleted after trying to overwrite a list using germplasm import.");
		}
	}

	@Test
	public void testSaveRecordsWhenOverridingNewListUsingTheImportedGermplasmList() throws BreedingManagerException {
		this.germplasmList.setId(null);
		this.action.saveRecords(this.germplasmList, this.germplasmNameObjects, this.newNames, SOURCE_LIST_XLS,
				this.doNotCreateGermplasmsWithId, this.importedGermplasmList, SEED_STORAGE_LOCATION);

		try {
			Mockito.verify(this.germplasmListManager, Mockito.times(0)).deleteGermplasmListDataByListId(SAVED_GERMPLASM_LIST_ID);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that there is no existing list entries to mark as deleted for new list using germplasm import.");
		}

	}

	@Test
	public void testUpdateExportedGermplasmPreferredNameWhenImportGermplasmListHasNameFactors() {
		final String preferredNameCode = "DRVNM";
		final int noOfEntries = 10;
		final ImportedGermplasmList importedGermplasmList =
				ImportedGermplasmListDataInitializer.createImportedGermplasmList(noOfEntries, true);
		this.action.updateExportedGermplasmPreferredName(preferredNameCode, importedGermplasmList.getImportedGermplasms());

		try {
			Mockito.verify(this.germplasmManager, Mockito.times(noOfEntries))
					.updateGermplasmPrefName(Mockito.anyInt(), Mockito.anyString());
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Not all germplasm's name are updated");
		}
	}

	@Test
	public void testUpdateExportedGermplasmPreferredNameWhenImportGermplasmListHasNoNameFactors() {
		final String preferredNameCode = "DRVNM";
		final int noOfEntries = 10;
		final ImportedGermplasmList importedGermplasmList =
				ImportedGermplasmListDataInitializer.createImportedGermplasmList(noOfEntries, false);
		this.action.updateExportedGermplasmPreferredName(preferredNameCode, importedGermplasmList.getImportedGermplasms());

		try {
			Mockito.verify(this.germplasmManager, Mockito.times(0)).updateGermplasmPrefName(Mockito.anyInt(), Mockito.anyString());
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

		final Germplasm germplasm = new Germplasm();
		final List<Attribute> attributes = this.action.prepareAllAttributesToAdd(importedGermplasm, existingUdflds, germplasm);

		for (final Attribute attr : attributes) {
			Assert.assertEquals("Make sure that the attribute's location is set to 0.", attr.getLocationId().intValue(), 0);
		}
	}
}
