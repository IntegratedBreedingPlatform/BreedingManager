
package org.generationcp.breeding.manager.listimport.actions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.generationcp.breeding.manager.action.SaveGermplasmListActionSource;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.data.initializer.ImportedGermplasmListDataInitializer;
import org.generationcp.breeding.manager.listimport.GermplasmFieldsComponent;
import org.generationcp.breeding.manager.listimport.GermplasmImportMain;
import org.generationcp.breeding.manager.listimport.NewDesignationForGermplasmConfirmDialog;
import org.generationcp.breeding.manager.listimport.SelectGermplasmWindow;
import org.generationcp.breeding.manager.listimport.SpecifyGermplasmDetailsComponent;
import org.generationcp.breeding.manager.listimport.listeners.ImportGermplasmEntryActionListener;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.ui.fields.BmsDateField;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.data.initializer.NameTestDataInitializer;
import org.generationcp.middleware.manager.GermplasmDataManagerUtil;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Window;

/**
 * Unit Test to verify Process Imported Germplasm Action file
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessImportedGermplasmActionTest {

	private static final String DESIGNATION = "(CML454 X CML451)-B-4-1-112";

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private SaveGermplasmListActionSource saveGermplasmListActionSource;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	private ProcessImportedGermplasmAction processImportedGermplasmAction;

	@Mock
	private GermplasmFieldsComponent germplasmFieldsComponent;

	@Mock
	private SpecifyGermplasmDetailsComponent germplasmDetailsComponent;

	@Mock
	private GermplasmDataManagerUtil germplasmDataManagerUtil;

	@Mock
	private CheckBox automaticallyAcceptSingleMatchesCheckbox;

	@Mock
	private Window parentWindow;

	final static Integer IBDB_USER_ID = 1;
	final static Integer DATE_INT_VALUE = 20151105;

	@Before
	public void setUp() throws Exception {
		this.processImportedGermplasmAction = new ProcessImportedGermplasmAction(this.germplasmDetailsComponent);
		this.processImportedGermplasmAction.setContextUtil(this.contextUtil);
		this.processImportedGermplasmAction.setGermplasmDataManager(this.germplasmDataManager);

		Mockito.doReturn(this.germplasmFieldsComponent).when(this.germplasmDetailsComponent)
				.getGermplasmFieldsComponent();
		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(123);
		Mockito.when(this.germplasmDetailsComponent.getGermplasmFieldsComponent())
				.thenReturn(this.germplasmFieldsComponent);
		Mockito.when(this.germplasmDetailsComponent.getImportedGermplasm())
				.thenReturn(ImportedGermplasmListDataInitializer.createListOfImportedGermplasm(1, false));
		Mockito.when(this.germplasmDataManager.getGermplasmByGID(Matchers.isA(Integer.class)))
				.thenReturn(GermplasmTestDataInitializer.createGermplasm(1));

		this.setUpComboBoxes();
		this.setUpBMSDateField();
		this.setUpGermplasmImportMain();
	}

	private void setUpGermplasmImportMain() {
		final GermplasmImportMain germplasmImportMain = new GermplasmImportMain(this.parentWindow, true);
		Mockito.when(this.germplasmDetailsComponent.getSource()).thenReturn(germplasmImportMain);
		Mockito.when(this.germplasmDetailsComponent.getWindow()).thenReturn(this.parentWindow);
	}

	private void setUpBMSDateField() {
		final BmsDateField bmsDateField = new BmsDateField();
		final Calendar cal = Calendar.getInstance();
		cal.set(2015, Calendar.JANUARY, 1);
		final Date testCreatedDate = cal.getTime();
		bmsDateField.setValue(testCreatedDate);
		Mockito.when(this.germplasmFieldsComponent.getGermplasmDateField()).thenReturn(bmsDateField);
	}

	private void setUpComboBoxes() {
		final ComboBox nameTypeComboBox = new ComboBox();
		nameTypeComboBox.setValue(3);
		Mockito.when(this.germplasmFieldsComponent.getNameTypeComboBox()).thenReturn(nameTypeComboBox);

		final ComboBox locationIdComboBox = new ComboBox();
		locationIdComboBox.setValue(4);
		Mockito.when(this.germplasmFieldsComponent.getLocationComboBox()).thenReturn(locationIdComboBox);

		final ComboBox methodComboBox = new ComboBox();
		methodComboBox.addItem("1");
		Mockito.doReturn(methodComboBox).when(this.germplasmFieldsComponent).getBreedingMethodComboBox();

	}

	@Test
	public void testCreateNewRecordsWithNoPedigreeConnectionWithDuplicateDesignationsInFile() {
		// Create 10 imported germplasm with first 5 entries having duplicate
		// designations within the file
		final List<ImportedGermplasm> importedGermplasm = ImportedGermplasmListDataInitializer
				.createListOfImportedGermplasm(5, false);
		importedGermplasm.addAll(ImportedGermplasmListDataInitializer.createListOfImportedGermplasm(5, false));
		Mockito.doReturn(importedGermplasm).when(this.germplasmDetailsComponent).getImportedGermplasm();

		// Method to test
		this.processImportedGermplasmAction.performFirstPedigreeAction();

		// Check that there are 10 unique germplasm (gid) to be saved, despite
		// duplicate designations
		Assert.assertEquals(10, this.processImportedGermplasmAction.getGermplasmNameObjects().size());
		final Set<Integer> uniqueGids = new HashSet<>();
		for (final GermplasmName germplasmNamePair : this.processImportedGermplasmAction.getGermplasmNameObjects()) {
			uniqueGids.add(germplasmNamePair.getGermplasm().getGid());
		}
		Assert.assertEquals(10, uniqueGids.size());
	}

	@Test
	public void testCreateNewRecordsWithPedigreeConnectionsWithDuplicateDesignationsInFile() {
		// Create 10 imported germplasm with first 5 entries having duplicate
		// designations within the file
		final List<ImportedGermplasm> importedGermplasm = ImportedGermplasmListDataInitializer
				.createListOfImportedGermplasm(5, false);
		importedGermplasm.addAll(ImportedGermplasmListDataInitializer.createListOfImportedGermplasm(5, false));
		Mockito.doReturn(importedGermplasm).when(this.germplasmDetailsComponent).getImportedGermplasm();

		// Method to test
		this.processImportedGermplasmAction.performSecondPedigreeAction();

		// Check that there are 10 unique germplasm (gid) to be saved, despite
		// duplicate designations
		Assert.assertEquals(10, this.processImportedGermplasmAction.getGermplasmNameObjects().size());
		final Set<Integer> uniqueGids = new HashSet<>();
		for (final GermplasmName germplasmNamePair : this.processImportedGermplasmAction.getGermplasmNameObjects()) {
			uniqueGids.add(germplasmNamePair.getGermplasm().getGid());
		}
		Assert.assertEquals(10, uniqueGids.size());
	}

	@Test
	public void testSelectMatchingGermplasmWheneverFoundWithDuplicateDesignationsInFile() {
		// Create 10 imported germplasm with first 5 entries having duplicate
		// designations within the file, no GIDs specified
		final boolean gidsSpecifiedInFile = false;
		final List<ImportedGermplasm> importedGermplasm = ImportedGermplasmListDataInitializer
				.createListOfImportedGermplasm(5, false, gidsSpecifiedInFile);
		importedGermplasm.addAll(
				ImportedGermplasmListDataInitializer.createListOfImportedGermplasm(5, false, gidsSpecifiedInFile));
		Mockito.doReturn(importedGermplasm).when(this.germplasmDetailsComponent).getImportedGermplasm();

		// Method to test
		this.processImportedGermplasmAction.performThirdPedigreeAction();

		// Check that there are 10 unique germplasm (gid) to be saved, despite
		// duplicate designations
		Assert.assertEquals(10, this.processImportedGermplasmAction.getGermplasmNameObjects().size());
		final Set<Integer> uniqueGids = new HashSet<>();
		for (final GermplasmName germplasmNamePair : this.processImportedGermplasmAction.getGermplasmNameObjects()) {
			uniqueGids.add(germplasmNamePair.getGermplasm().getGid());
		}
		Assert.assertEquals(10, uniqueGids.size());
	}

	@Test
	public void testCreateNewRecordsWithPedigreeConnectionsIfGidSpecified() {
		this.processImportedGermplasmAction.performSecondPedigreeAction();

		Mockito.verify(this.contextUtil).getCurrentUserLocalId();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(5)).getGermplasmFieldsComponent();
		Mockito.verify(this.germplasmFieldsComponent).getGermplasmDateField();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(3)).getImportedGermplasm();
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(2)).getLocationComboBox();
		Mockito.verify(this.germplasmFieldsComponent).getBreedingMethodComboBox();
		Mockito.verify(this.germplasmDataManager).getGermplasmByGID(Matchers.isA(Integer.class));
		Mockito.verify(this.germplasmFieldsComponent).getNameTypeComboBox();
	}

	@Test
	public void testCreateNewRecordsWithPedigreeConnectionsIfSingleDesignationMatchForAllGermplasm() {
		final List<Germplasm> germplasm = new ArrayList<>();
		germplasm.add(GermplasmTestDataInitializer.createGermplasm(1));
		Mockito.when(this.germplasmDataManager.getGermplasmByName(Matchers.anyString(), Matchers.anyInt(),
				Matchers.anyInt(), Matchers.isA(Operation.class))).thenReturn(germplasm);

		this.processImportedGermplasmAction.performSecondPedigreeAction();

		Mockito.verify(this.contextUtil).getCurrentUserLocalId();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(5)).getGermplasmFieldsComponent();
		Mockito.verify(this.germplasmFieldsComponent).getGermplasmDateField();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(3)).getImportedGermplasm();
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(2)).getLocationComboBox();
		Mockito.verify(this.germplasmFieldsComponent).getBreedingMethodComboBox();
		Mockito.verify(this.germplasmFieldsComponent).getNameTypeComboBox();
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(2)).getLocationComboBox();

		// Verify that no instance of SelectGermplasmWindow was added to list of
		// listeners, as it's only done for multiple matches
		final List<ImportGermplasmEntryActionListener> importEntryListeners = this.processImportedGermplasmAction
				.getImportEntryListeners();
		Assert.assertNotNull(importEntryListeners);
		Assert.assertTrue(importEntryListeners.isEmpty());

	}

	/**
	 * Test to verify that SelectGermplasmWindow is created for multiple matches
	 */
	@Test
	public void testCreateNewRecordsWithPedigreeConnectionsIfMultipleMatchesOnDesignation() {
		// Create 3 germplasm to be imported, with multiple designation matches
		// for 2nd germplasm
		final List<Germplasm> germplasmList = new ArrayList<>();
		germplasmList.add(GermplasmTestDataInitializer.createGermplasm(1));
		germplasmList.add(GermplasmTestDataInitializer.createGermplasm(2));
		germplasmList.add(GermplasmTestDataInitializer.createGermplasm(3));
		Mockito.when(this.germplasmDetailsComponent.getImportedGermplasm())
				.thenReturn(ImportedGermplasmListDataInitializer.createListOfImportedGermplasm(3, false));
		Mockito.when(this.germplasmDataManager.getGermplasmByName(Matchers.anyString(), Matchers.anyInt(),
				Matchers.anyInt(), Matchers.isA(Operation.class))).thenReturn(germplasmList);

		// Simulate 3 matches when searching by second germplasm's designation
		Mockito.when(this.germplasmDataManager
				.countGermplasmByName(ImportedGermplasmListDataInitializer.DESIGNATION + "-" + 2, Operation.EQUAL))
				.thenReturn(3L);
		// Hack - add an initial dummy import entry listener so that the table
		// Select Germplasm window
		// that will be added will not be populated (out of scope for this test)
		// and cause NPE
		final ArrayList<ImportGermplasmEntryActionListener> importListeners = new ArrayList<ImportGermplasmEntryActionListener>();
		importListeners.add(Mockito.mock(SelectGermplasmWindow.class));
		this.processImportedGermplasmAction.setImportEntryListener(importListeners);

		// Method to test
		this.processImportedGermplasmAction.performSecondPedigreeAction();

		// Verify that an instance of SelectGermplasmWindow was added to list of
		// listeners
		final List<ImportGermplasmEntryActionListener> importEntryListeners = this.processImportedGermplasmAction
				.getImportEntryListeners();
		Assert.assertNotNull(importEntryListeners);
		// 2 listeners including the dummy one added earlier
		Assert.assertTrue(importEntryListeners.size() == 2);
		final SelectGermplasmWindow selectGermplasmWindow = (SelectGermplasmWindow) importEntryListeners.get(1);

		// Check that the window was created for 2nd entry and that total # of
		// entries displayed equals # of imported germplasm
		Assert.assertTrue(selectGermplasmWindow.getGermplasmIndex() == 1);
		Assert.assertTrue(selectGermplasmWindow.getNoOfImportedGermplasm() == 3);
	}

	@Test
	public void testSelectMatchingGermplasmAutomaticallyAcceptSingleDesignationMatch() {
		final boolean withGidInFile = false;
		final List<ImportedGermplasm> importedGermplasm = ImportedGermplasmListDataInitializer
				.createListOfImportedGermplasm(1, false, withGidInFile);
		Mockito.when(this.germplasmDetailsComponent.getImportedGermplasm()).thenReturn(importedGermplasm);
		Mockito.when(
				this.germplasmDataManager.countGermplasmByName(importedGermplasm.get(0).getDesig(), Operation.EQUAL))
				.thenReturn(1L);
		final List<Germplasm> germplasms = new ArrayList<Germplasm>();
		final Integer gidMatched = 10;
		germplasms.add(GermplasmTestDataInitializer.createGermplasm(gidMatched));
		Mockito.doReturn(germplasms).when(this.germplasmDataManager)
				.getGermplasmByName(importedGermplasm.get(0).getDesig(), 0, 1, Operation.EQUAL);
		final boolean automaticallyAcceptSingleMatch = true;
		Mockito.doReturn(automaticallyAcceptSingleMatch).when(this.germplasmDetailsComponent)
				.automaticallyAcceptSingleMatchesCheckbox();

		// Method to test
		this.processImportedGermplasmAction.performThirdPedigreeAction();

		Mockito.verify(this.contextUtil).getCurrentUserLocalId();
		Mockito.verify(this.germplasmFieldsComponent).getGermplasmDateField();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(3)).getImportedGermplasm();
		Mockito.verify(this.germplasmFieldsComponent).getNameTypeComboBox();
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(2)).getLocationComboBox();

		// Verify Middleware interactions
		Mockito.verify(this.germplasmDataManager, Mockito.never()).getGermplasmByGID(Matchers.isA(Integer.class));
		Mockito.verify(this.germplasmDataManager, Mockito.never()).getNamesByGID(Matchers.isA(Integer.class),
				Matchers.anyInt(), (GermplasmNameType) Matchers.isNull());
		Mockito.verify(this.germplasmDataManager, Mockito.times(1)).getGermplasmByName(Matchers.isA(String.class),
				Matchers.anyInt(), Matchers.anyInt(), Matchers.isA(Operation.class));

		// Verify that no instance of SelectGermplasmWindow was added to list of
		// listeners since germplasm was automatically accepted
		final List<ImportGermplasmEntryActionListener> importEntryListeners = this.processImportedGermplasmAction
				.getImportEntryListeners();
		Assert.assertNotNull(importEntryListeners);
		Assert.assertTrue(importEntryListeners.isEmpty());
		// Verify that GID of germplasm matched by designation was added to list
		// of IDs matched and GID was updated
		final List<Integer> matchedGIDs = this.processImportedGermplasmAction.getMatchedGermplasmIds();
		Assert.assertNotNull(matchedGIDs);
		Assert.assertFalse(matchedGIDs.isEmpty());
		Assert.assertEquals("Expecting GID of germplasm matched to designation as added to list of GIDs matched.",
				gidMatched, matchedGIDs.get(0));
		Assert.assertTrue("Expecting flag for tracking if GID is matched is true.",
				this.processImportedGermplasmAction.getGermplasmNameObjects().get(0).isGidMatched());
		Assert.assertEquals("Expecting GID of germplasm to be imported was updated with matched GID.", gidMatched,
				this.processImportedGermplasmAction.getGermplasmNameObjects().get(0).getGermplasm().getGid());
	}

	@Test
	public void testSelectMatchingGermplasmDoNotAutomaticallyAcceptSingleDesignationMatch() {
		final boolean withGidInFile = false;
		final List<ImportedGermplasm> importedGermplasm = ImportedGermplasmListDataInitializer
				.createListOfImportedGermplasm(1, false, withGidInFile);
		Mockito.when(this.germplasmDetailsComponent.getImportedGermplasm()).thenReturn(importedGermplasm);
		Mockito.when(
				this.germplasmDataManager.countGermplasmByName(importedGermplasm.get(0).getDesig(), Operation.EQUAL))
				.thenReturn(1L);
		final boolean automaticallyAcceptSingleMatch = false;
		Mockito.doReturn(automaticallyAcceptSingleMatch).when(this.germplasmDetailsComponent)
				.automaticallyAcceptSingleMatchesCheckbox();

		// Hack - need to add initial listener to prevent NPE when adding new
		// listener
		this.addInitialImportEntryListener();

		// Method to test
		this.processImportedGermplasmAction.performThirdPedigreeAction();

		// Verify Middleware interactions
		Mockito.verify(this.germplasmDataManager, Mockito.never()).getGermplasmByGID(Matchers.isA(Integer.class));
		Mockito.verify(this.germplasmDataManager, Mockito.never()).getNamesByGID(Matchers.isA(Integer.class),
				Matchers.anyInt(), (GermplasmNameType) Matchers.isNull());
		Mockito.verify(this.germplasmDataManager, Mockito.never()).getGermplasmByName(Matchers.isA(String.class),
				Matchers.anyInt(), Matchers.anyInt(), Matchers.isA(Operation.class));

		// Verify that one instance of SelectGermplasmWindow was added to list
		// of listeners since user chose not to accept automatically
		final List<ImportGermplasmEntryActionListener> importEntryListeners = this.processImportedGermplasmAction
				.getImportEntryListeners();
		Assert.assertNotNull(importEntryListeners);
		// 2 import entry listeners including dummy one created earlier
		Assert.assertTrue(importEntryListeners.size() == 2);
	}

	@Test
	public void testSelectMatchingGermplasmNoDesignationMatch() {
		final boolean withGidInFile = false;
		final List<ImportedGermplasm> importedGermplasm = ImportedGermplasmListDataInitializer
				.createListOfImportedGermplasm(1, false, withGidInFile);
		Mockito.when(this.germplasmDetailsComponent.getImportedGermplasm()).thenReturn(importedGermplasm);
		Mockito.when(
				this.germplasmDataManager.countGermplasmByName(importedGermplasm.get(0).getDesig(), Operation.EQUAL))
				.thenReturn(0L);
		final boolean automaticallyAcceptSingleMatch = true;
		Mockito.doReturn(automaticallyAcceptSingleMatch).when(this.germplasmDetailsComponent)
				.automaticallyAcceptSingleMatchesCheckbox();

		// Method to test
		this.processImportedGermplasmAction.performThirdPedigreeAction();

		// Verify Middleware interactions
		Mockito.verify(this.germplasmDataManager, Mockito.never()).getGermplasmByGID(Matchers.isA(Integer.class));
		Mockito.verify(this.germplasmDataManager, Mockito.never()).getNamesByGID(Matchers.isA(Integer.class),
				Matchers.anyInt(), (GermplasmNameType) Matchers.isNull());
		Mockito.verify(this.germplasmDataManager, Mockito.never()).getGermplasmByName(Matchers.isA(String.class),
				Matchers.anyInt(), Matchers.anyInt(), Matchers.isA(Operation.class));

		// Verify that no instance of SelectGermplasmWindow was added to list of
		// listeners since no germplasm was matched
		final List<ImportGermplasmEntryActionListener> importEntryListeners = this.processImportedGermplasmAction
				.getImportEntryListeners();
		Assert.assertNotNull(importEntryListeners);
		Assert.assertTrue(importEntryListeners.isEmpty());
	}

	@Test
	public void testSelectMatchingGermplasmGIDAndDesignationInFileWereMatched() {
		final boolean withGidInFile = true;
		final List<ImportedGermplasm> importedGermplasmList = ImportedGermplasmListDataInitializer
				.createListOfImportedGermplasm(1, false, withGidInFile);
		Mockito.when(this.germplasmDetailsComponent.getImportedGermplasm()).thenReturn(importedGermplasmList);
		final ImportedGermplasm importedGermplasm = importedGermplasmList.get(0);
		Mockito.when(this.germplasmDataManager.countGermplasmByName(importedGermplasm.getDesig(), Operation.EQUAL))
				.thenReturn(0L);
		final Integer gidInFile = importedGermplasm.getGid();
		Mockito.when(this.germplasmDataManager.getGermplasmByGID(gidInFile))
				.thenReturn(GermplasmTestDataInitializer.createGermplasm(gidInFile));
		final List<Name> names = new ArrayList<>();
		names.add(NameTestDataInitializer.createName(1, 1, ProcessImportedGermplasmActionTest.DESIGNATION + "-" + 1));
		Mockito.when(this.germplasmDataManager.getNamesByGID(gidInFile, 0, null)).thenReturn(names);

		// Method to test
		this.processImportedGermplasmAction.performThirdPedigreeAction();

		// Verify Middleware interactions
		final ArgumentCaptor<Integer> gidCaptor = ArgumentCaptor.forClass(Integer.class);
		Mockito.verify(this.germplasmDataManager, Mockito.times(1)).getGermplasmByGID(gidCaptor.capture());
		Assert.assertEquals(gidInFile, gidCaptor.getValue());
		Mockito.verify(this.germplasmDataManager, Mockito.times(1)).getNamesByGID(gidCaptor.capture(), Matchers.eq(0),
				(GermplasmNameType) Matchers.isNull());
		Mockito.verify(this.germplasmDataManager, Mockito.never()).getGermplasmByName(Matchers.isA(String.class),
				Matchers.anyInt(), Matchers.anyInt(), Matchers.isA(Operation.class));

		// Verify that no instance of SelectGermplasmWindow was added to list of
		// listeners since GID and Designation in file was matched
		final List<ImportGermplasmEntryActionListener> importEntryListeners = this.processImportedGermplasmAction
				.getImportEntryListeners();
		Assert.assertNotNull(importEntryListeners);
		Assert.assertTrue(importEntryListeners.isEmpty());
	}

	@Test
	public void testSelectMatchingGermplasmDesignationInFileNotANameOfGIDSpecified() {
		final boolean withGidInFile = true;
		final List<ImportedGermplasm> importedGermplasmList = ImportedGermplasmListDataInitializer
				.createListOfImportedGermplasm(1, false, withGidInFile);
		Mockito.when(this.germplasmDetailsComponent.getImportedGermplasm()).thenReturn(importedGermplasmList);
		final ImportedGermplasm importedGermplasm = importedGermplasmList.get(0);
		Mockito.when(this.germplasmDataManager.countGermplasmByName(importedGermplasm.getDesig(), Operation.EQUAL))
				.thenReturn(0L);
		final Integer gidInFile = importedGermplasm.getGid();
		Mockito.when(this.germplasmDataManager.getGermplasmByGID(gidInFile))
				.thenReturn(GermplasmTestDataInitializer.createGermplasm(gidInFile));
		final List<Name> names = new ArrayList<>();
		names.add(NameTestDataInitializer.createName(1, 1, "Some Other Name"));
		Mockito.when(this.germplasmDataManager.getNamesByGID(gidInFile, 0, null)).thenReturn(names);

		// Hack - need to add initial listener to prevent NPE when adding new
		// listener
		this.addInitialImportEntryListener();

		// Method to test
		this.processImportedGermplasmAction.performThirdPedigreeAction();

		// Verify Middleware interactions
		final ArgumentCaptor<Integer> gidCaptor = ArgumentCaptor.forClass(Integer.class);
		Mockito.verify(this.germplasmDataManager, Mockito.times(1)).getGermplasmByGID(gidCaptor.capture());
		Assert.assertEquals(gidInFile, gidCaptor.getValue());
		Mockito.verify(this.germplasmDataManager, Mockito.times(1)).getNamesByGID(gidCaptor.capture(), Matchers.eq(0),
				(GermplasmNameType) Matchers.isNull());
		Mockito.verify(this.germplasmDataManager, Mockito.never()).getGermplasmByName(Matchers.isA(String.class),
				Matchers.anyInt(), Matchers.anyInt(), Matchers.isA(Operation.class));

		// Verify that one instance of SelectGermplasmWindow was added to list
		// of listeners
		final List<ImportGermplasmEntryActionListener> importEntryListeners = this.processImportedGermplasmAction
				.getImportEntryListeners();
		Assert.assertNotNull(importEntryListeners);
		// 2 import entry listeners including dummy one created earlier
		Assert.assertTrue(importEntryListeners.size() == 2);
		Assert.assertTrue(importEntryListeners.get(1) instanceof NewDesignationForGermplasmConfirmDialog);
		final NewDesignationForGermplasmConfirmDialog newDesignationDialog = (NewDesignationForGermplasmConfirmDialog) importEntryListeners
				.get(1);
		Assert.assertEquals(importedGermplasm.getGid(), newDesignationDialog.getGid());
		Assert.assertEquals(importedGermplasm.getDesig(), newDesignationDialog.getDesignation());
	}

	@Test
	public void testSelectMatchingGermplasmGIDInFileDoesNotExistInDB() {
		final boolean withGidInFile = true;
		final List<ImportedGermplasm> importedGermplasmList = ImportedGermplasmListDataInitializer
				.createListOfImportedGermplasm(1, false, withGidInFile);
		Mockito.when(this.germplasmDetailsComponent.getImportedGermplasm()).thenReturn(importedGermplasmList);
		final ImportedGermplasm importedGermplasm = importedGermplasmList.get(0);
		Mockito.when(this.germplasmDataManager.countGermplasmByName(importedGermplasm.getDesig(), Operation.EQUAL))
				.thenReturn(0L);
		final Integer gidInFile = importedGermplasm.getGid();
		Mockito.when(this.germplasmDataManager.getGermplasmByGID(gidInFile)).thenReturn(null);

		// Method to test
		this.processImportedGermplasmAction.performThirdPedigreeAction();

		// Verify Middleware interactions
		final ArgumentCaptor<Integer> gidCaptor = ArgumentCaptor.forClass(Integer.class);
		Mockito.verify(this.germplasmDataManager, Mockito.times(1)).getGermplasmByGID(gidCaptor.capture());
		Assert.assertEquals(gidInFile, gidCaptor.getValue());
		Mockito.verify(this.germplasmDataManager, Mockito.never()).getNamesByGID(gidCaptor.capture(), Matchers.eq(0),
				(GermplasmNameType) Matchers.isNull());
		Mockito.verify(this.germplasmDataManager, Mockito.never()).getGermplasmByName(Matchers.isA(String.class),
				Matchers.anyInt(), Matchers.anyInt(), Matchers.isA(Operation.class));

		// Verify that no instance of SelectGermplasmWindow was added to list of
		// listeners since GID and Designation in file was matched
		final List<ImportGermplasmEntryActionListener> importEntryListeners = this.processImportedGermplasmAction
				.getImportEntryListeners();
		Assert.assertNotNull(importEntryListeners);
		Assert.assertTrue(importEntryListeners.isEmpty());
		// Verify that GID in file was NOT added to list of GIDs matched
		final List<Integer> matchedGIDs = this.processImportedGermplasmAction.getMatchedGermplasmIds();
		Assert.assertNotNull(matchedGIDs);
		Assert.assertTrue(matchedGIDs.isEmpty());
		// Warning was shown that GID does not exist
		Mockito.verify(this.germplasmDetailsComponent).getWindow();
	}

	@Test
	public void testSelectMatchingGermplasmWheneverFoundIfMultipleMatchesOnDesignation() {
		final int noOfEntries = 3;
		final boolean withGidInFile = false;
		final List<ImportedGermplasm> importedGermplasmList = ImportedGermplasmListDataInitializer
				.createListOfImportedGermplasm(noOfEntries, false, withGidInFile);
		Mockito.when(this.germplasmDetailsComponent.getImportedGermplasm()).thenReturn(importedGermplasmList);

		// Simulate 3 matches when searching by second germplasm's designation
		Mockito.when(this.germplasmDataManager
				.countGermplasmByName(ImportedGermplasmListDataInitializer.DESIGNATION + "-" + 2, Operation.EQUAL))
				.thenReturn(3L);

		// Hack - need to add initial listener to prevent NPE when adding new
		// listener
		this.addInitialImportEntryListener();

		// Method to test
		this.processImportedGermplasmAction.performThirdPedigreeAction();

		Mockito.verify(this.contextUtil).getCurrentUserLocalId();
		Mockito.verify(this.germplasmFieldsComponent).getGermplasmDateField();
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(noOfEntries)).getNameTypeComboBox();
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(noOfEntries * 2)).getLocationComboBox();

		// Verify that one instance of SelectGermplasmWindow was added to list
		// of listeners
		final List<ImportGermplasmEntryActionListener> importEntryListeners = this.processImportedGermplasmAction
				.getImportEntryListeners();
		Assert.assertNotNull(importEntryListeners);
		// 2 import entry listeners including dummy one created earlier
		Assert.assertTrue(importEntryListeners.size() == 2);
		final SelectGermplasmWindow selectGermplasmWindow = (SelectGermplasmWindow) importEntryListeners.get(1);
		// Check that the window was created for 2nd entry and that total # of
		// entries displayed equals # of imported germplasm
		Assert.assertTrue(selectGermplasmWindow.getGermplasmIndex() == 1);
		Assert.assertTrue(selectGermplasmWindow.getNoOfImportedGermplasm() == noOfEntries);
	}

	@Test
	public void testUpdateGidForSingleMatchWhenAutomaticallyAcceptSingleMatchWhereMatchInDB() {
		final Integer gid = 100;
		final ImportedGermplasm importedGermplasm = ImportedGermplasmListDataInitializer.createImportedGermplasm(gid,
				true);
		importedGermplasm.setDesig("Name" + gid);
		final List<Germplasm> germplasms = new ArrayList<Germplasm>();
		germplasms.add(GermplasmTestDataInitializer.createGermplasm(gid));

		this.initializeAndSetGermplasmNameObjects();

		Mockito.doReturn(true).when(this.germplasmDetailsComponent).automaticallyAcceptSingleMatchesCheckbox();
		Mockito.doReturn(germplasms).when(this.germplasmDataManager).getGermplasmByName(importedGermplasm.getDesig(), 0,
				1, Operation.EQUAL);

		// Method to test
		final int germplasmMatchesCount = 1;
		final Integer index = 0;
		this.processImportedGermplasmAction.updateGidForSingleMatch(index, importedGermplasm, germplasmMatchesCount);

		final GermplasmName updatedGermplasmName = this.processImportedGermplasmAction.getGermplasmNameObjects().get(0);
		Assert.assertEquals("Expecting that the gid set is from the existing germplasm.", gid,
				updatedGermplasmName.getGermplasm().getGid());
		Assert.assertNotNull(this.processImportedGermplasmAction.getMatchedGermplasmIds());
		Assert.assertEquals(1, this.processImportedGermplasmAction.getMatchedGermplasmIds().size());
		Assert.assertEquals("Expecting matched germplasm to be in list of matched GIDs", gid,
				this.processImportedGermplasmAction.getMatchedGermplasmIds().get(0));
		Assert.assertTrue("Expecting flag for tracking if GID is matched is true.",
				updatedGermplasmName.isGidMatched());
	}

	@Test
	public void testUpdateGidForSingleMatchWhenAutomaticallyAcceptSingleMatchWhereMatchNotInDBAndGermplasmToReuseIsNull() {
		final Integer gid = 100;
		final ImportedGermplasm importedGermplasm = ImportedGermplasmListDataInitializer.createImportedGermplasm(gid,
				true);
		importedGermplasm.setDesig("Name" + gid);
		final List<Germplasm> germplasms = new ArrayList<Germplasm>();
		germplasms.add(GermplasmTestDataInitializer.createGermplasm(gid));

		this.initializeAndSetGermplasmNameObjects();

		Mockito.doReturn(true).when(this.germplasmDetailsComponent).automaticallyAcceptSingleMatchesCheckbox();

		// Method to test
		final int germplasmMatchesCount = 0;
		final Integer index = 0;
		this.processImportedGermplasmAction.updateGidForSingleMatch(index, importedGermplasm, germplasmMatchesCount);

		Assert.assertEquals("Expecting that the gid set is the same.", gid, importedGermplasm.getGid());
		Assert.assertEquals("Expecting the size of designationToGermplasmForReuseMap to be 2", 2,
				this.processImportedGermplasmAction.getDesignationToGermplasmForReuseMap().size());

	}

	@Test
	public void testUpdateGidForSingleMatchWhenAutomaticallyAcceptSingleMatchWhereMatchNotInDBAndGermplasmToReuseIsNotNull() {
		final Integer gid = 100;
		final ImportedGermplasm importedGermplasm = ImportedGermplasmListDataInitializer.createImportedGermplasm(gid,
				true);
		importedGermplasm.setDesig("Name" + gid);
		final List<Germplasm> germplasms = new ArrayList<Germplasm>();
		germplasms.add(GermplasmTestDataInitializer.createGermplasm(gid));

		this.initializeAndSetGermplasmNameObjects();

		Mockito.doReturn(true).when(this.germplasmDetailsComponent).automaticallyAcceptSingleMatchesCheckbox();

		final int germplasmMatchesCount = 0;
		final Integer index = 0;

		// Add the designation in the map to make sure that there will be an
		// existing germplasm to be reused.
		this.processImportedGermplasmAction.mapDesignationToGermplasmForReuse(importedGermplasm.getDesig(), index);
		Assert.assertEquals("Expecting the size of designationToGermplasmForReuseMap to be 2", 2,
				this.processImportedGermplasmAction.getDesignationToGermplasmForReuseMap().size());

		// Method to test
		this.processImportedGermplasmAction.updateGidForSingleMatch(index, importedGermplasm, germplasmMatchesCount);

		Assert.assertEquals("Expecting that the gid set is the same.", gid, importedGermplasm.getGid());
		Assert.assertEquals("Expecting the size of designationToGermplasmForReuseMap to be 2", 2,
				this.processImportedGermplasmAction.getDesignationToGermplasmForReuseMap().size());
	}

	@Test
	public void testUpdateGidForSingleMatchWhenNoDesignationMatch() {
		final Integer gid = 10;
		final ImportedGermplasm importedGermplasm = ImportedGermplasmListDataInitializer.createImportedGermplasm(gid,
				true);
		importedGermplasm.setDesig("Name" + gid);
		final GermplasmName germplasmToName = new GermplasmName(GermplasmTestDataInitializer.createGermplasm(gid),
				new Name());

		// Method to Test
		final int germplasmMatchesCount = 0;
		final int index = 0;
		this.processImportedGermplasmAction.updateGidForSingleMatch(index, importedGermplasm, germplasmMatchesCount);

		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(1)).automaticallyAcceptSingleMatchesCheckbox();
		Mockito.verify(this.germplasmDataManager, Mockito.times(0)).getGermplasmByName(importedGermplasm.getDesig(), 0,
				1, Operation.EQUAL);
		Assert.assertEquals("Expecting that the temporary gid is used when there is no designation match.", gid,
				germplasmToName.getGermplasm().getGid());
		Assert.assertFalse("Expecting flag for tracking if GID is matched is false.", germplasmToName.isGidMatched());
	}

	@Test
	public void testUpdateGidForSingleMatchDoNotAutomaticallyAcceptSingleMatch() {
		final Integer gid = 10;
		final ImportedGermplasm importedGermplasm = ImportedGermplasmListDataInitializer.createImportedGermplasm(gid,
				true);
		importedGermplasm.setDesig("Name" + gid);
		final GermplasmName germplasmToName = new GermplasmName(GermplasmTestDataInitializer.createGermplasm(gid),
				new Name());
		// Do not automatically accept single match
		Mockito.doReturn(false).when(this.germplasmDetailsComponent).automaticallyAcceptSingleMatchesCheckbox();

		// Method to test
		final int germplasmMatchesCount = 1;
		final Integer index = 0;
		this.processImportedGermplasmAction.updateGidForSingleMatch(index, importedGermplasm, germplasmMatchesCount);

		Mockito.verify(this.germplasmDataManager, Mockito.times(0)).getGermplasmByName(importedGermplasm.getDesig(), 0,
				1, Operation.EQUAL);
		Assert.assertEquals("Expecting that the temporary gid is used when single match is not automatically accepted.",
				gid, germplasmToName.getGermplasm().getGid());
		Assert.assertFalse("Expecting flag for tracking if GID is matched is false.", germplasmToName.isGidMatched());
	}

	@Test
	public void testUpdateGidForSingleMatchWhenMultipleDesignationMatches() {
		final Integer gid = 10;
		final ImportedGermplasm importedGermplasm = ImportedGermplasmListDataInitializer.createImportedGermplasm(gid,
				true);
		importedGermplasm.setDesig("Name" + gid);
		final GermplasmName germplasmToName = new GermplasmName(GermplasmTestDataInitializer.createGermplasm(gid),
				new Name());

		Mockito.doReturn(true).when(this.germplasmDetailsComponent).automaticallyAcceptSingleMatchesCheckbox();

		// Method to test
		final int germplasmMatchesCount = 3;
		final Integer index = 0;
		this.processImportedGermplasmAction.updateGidForSingleMatch(index, importedGermplasm, germplasmMatchesCount);

		Mockito.verify(this.germplasmDataManager, Mockito.times(0)).getGermplasmByName(importedGermplasm.getDesig(), 0,
				1, Operation.EQUAL);
		Assert.assertEquals(
				"Expecting that the temporary gid is used when there are multiple matched germplasm for designation.",
				gid, germplasmToName.getGermplasm().getGid());
		Assert.assertFalse("Expecting flag for tracking if GID is matched is false.", germplasmToName.isGidMatched());
	}

	@Test
	public void testIsNeedToDisplayGermplasmSelectionWindowForNoMatchWithoutAutomaticMatching() {
		Mockito.doReturn(false).when(this.germplasmDetailsComponent).automaticallyAcceptSingleMatchesCheckbox();
		Assert.assertFalse("Germplasm Selection Window should not be displayed",
				this.processImportedGermplasmAction.isNeedToDisplayGermplasmSelectionWindow(0));
	}

	@Test
	public void testIsNeedToDisplayGermplasmSelectionWindowForNoMatchWithAutomaticMatching() {
		Mockito.doReturn(true).when(this.germplasmDetailsComponent).automaticallyAcceptSingleMatchesCheckbox();
		Assert.assertFalse("Germplasm Selection Window should not be displayed",
				this.processImportedGermplasmAction.isNeedToDisplayGermplasmSelectionWindow(0));
	}

	@Test
	public void testIsNeedToDisplayGermplasmSelectionWindowForSingleMatchWithoutAutomaticMatching() {
		Mockito.doReturn(false).when(this.germplasmDetailsComponent).automaticallyAcceptSingleMatchesCheckbox();
		Assert.assertTrue("Germplasm Selection Window should be displayed",
				this.processImportedGermplasmAction.isNeedToDisplayGermplasmSelectionWindow(1));
	}

	@Test
	public void testIsNeedToDisplayGermplasmSelectionWindowForSingleMatchWithAutomaticMatching() {
		Mockito.doReturn(true).when(this.germplasmDetailsComponent).automaticallyAcceptSingleMatchesCheckbox();
		Assert.assertFalse("Germplasm Selection Window should not be displayed",
				this.processImportedGermplasmAction.isNeedToDisplayGermplasmSelectionWindow(1));
	}

	@Test
	public void testIsNeedToDisplayGermplasmSelectionWindowForMultipleMatchWithoutAutomaticMatching() {
		Mockito.doReturn(false).when(this.germplasmDetailsComponent).automaticallyAcceptSingleMatchesCheckbox();
		Assert.assertTrue("Germplasm Selection Window should be displayed",
				this.processImportedGermplasmAction.isNeedToDisplayGermplasmSelectionWindow(2));
	}

	@Test
	public void testIsNeedToDisplayGermplasmSelectionWindowForMultipleMatchWithAutomaticMatching() {
		Mockito.doReturn(true).when(this.germplasmDetailsComponent).automaticallyAcceptSingleMatchesCheckbox();
		Assert.assertTrue("Germplasm Selection Window should be displayed",
				this.processImportedGermplasmAction.isNeedToDisplayGermplasmSelectionWindow(2));
	}

	@Test
	public void testCreateGermplasmObjectWithNoSelectedLocation() {
		final Integer gid = 3;
		final Integer gnpgs = -1;
		final Integer gpid1 = 1;
		final Integer gpid2 = 2;
		final Integer ibdbUserId = 10001;
		final Integer date = 20163012;

		final Germplasm germplasm = this.processImportedGermplasmAction.createGermplasmObject(gid, gnpgs, gpid1, gpid2,
				ibdbUserId, date);

		Assert.assertEquals("The gid should be " + gid, gid, germplasm.getGid());
		Assert.assertEquals("The user id should be " + ibdbUserId, ibdbUserId, germplasm.getUserId());
		Assert.assertEquals("The location id should be " + ProcessImportedGermplasmAction.DEFAULT_LOCATION_ID,
				ProcessImportedGermplasmAction.DEFAULT_LOCATION_ID, germplasm.getLocationId());
		Assert.assertEquals("The date should be " + date, date, germplasm.getGdate());
		Assert.assertEquals("The method should be " + ProcessImportedGermplasmAction.UNKNOWN_DERIVATIVE_METHOD,
				ProcessImportedGermplasmAction.UNKNOWN_DERIVATIVE_METHOD, germplasm.getMethodId());
		Assert.assertEquals("The gnpgs should be " + gnpgs, gnpgs, germplasm.getGnpgs());
		Assert.assertEquals("The gpid1 should be " + gpid1, gpid1, germplasm.getGpid1());
		Assert.assertEquals("The gpid2 should be " + gpid2, gpid2, germplasm.getGpid2());
		Assert.assertTrue("The lgid should be 0", 0 == germplasm.getLgid());
		Assert.assertTrue("The grplace should be 0", 0 == germplasm.getGrplce());
		Assert.assertTrue("The reference id should be 0", 0 == germplasm.getReferenceId());
		Assert.assertTrue("The MGID should be 0", 0 == germplasm.getMgid());
	}

	@Test
	public void testCreateGermplasmObjectWithSelectedLocation() {
		final Integer gid = 3;
		final Integer gnpgs = -1;
		final Integer gpid1 = 1;
		final Integer gpid2 = 2;
		final String locationId = "1";
		final ComboBox locationComboBox = this.processImportedGermplasmAction.getGermplasmFieldsComponent()
				.getLocationComboBox();
		locationComboBox.addItem(locationId);
		locationComboBox.setItemCaption(locationId, "1");
		locationComboBox.setValue(locationId);
		final Germplasm germplasm = this.processImportedGermplasmAction.createGermplasmObject(gid, gnpgs, gpid1, gpid2,
				ProcessImportedGermplasmActionTest.IBDB_USER_ID, ProcessImportedGermplasmActionTest.DATE_INT_VALUE);

		Assert.assertEquals("The gid should be " + gid, gid, germplasm.getGid());
		Assert.assertEquals("The user id should be " + ProcessImportedGermplasmActionTest.IBDB_USER_ID,
				ProcessImportedGermplasmActionTest.IBDB_USER_ID, germplasm.getUserId());
		Assert.assertEquals("The location id should be " + locationId, locationId,
				germplasm.getLocationId().toString());
		Assert.assertEquals("The date should be " + ProcessImportedGermplasmActionTest.DATE_INT_VALUE,
				ProcessImportedGermplasmActionTest.DATE_INT_VALUE, germplasm.getGdate());
		Assert.assertEquals("The method should be " + ProcessImportedGermplasmAction.UNKNOWN_DERIVATIVE_METHOD,
				ProcessImportedGermplasmAction.UNKNOWN_DERIVATIVE_METHOD, germplasm.getMethodId());
		Assert.assertEquals("The gnpgs should be " + gnpgs, gnpgs, germplasm.getGnpgs());
		Assert.assertEquals("The gpid1 should be " + gpid1, gpid1, germplasm.getGpid1());
		Assert.assertEquals("The gpid2 should be " + gpid2, gpid2, germplasm.getGpid2());
		Assert.assertTrue("The lgid should be 0", 0 == germplasm.getLgid());
		Assert.assertTrue("The grplace should be 0", 0 == germplasm.getGrplce());
		Assert.assertTrue("The reference id should be 0", 0 == germplasm.getReferenceId());
		Assert.assertTrue("The MGID should be 0", 0 == germplasm.getMgid());
	}

	@Test
	public void testCreateNameObjectWithNoSelectedLocation() {
		final Name name = this.processImportedGermplasmAction.createNameObject(
				ProcessImportedGermplasmActionTest.IBDB_USER_ID, ProcessImportedGermplasmActionTest.DATE_INT_VALUE,
				ProcessImportedGermplasmActionTest.DESIGNATION);

		Assert.assertEquals("The user id should be " + ProcessImportedGermplasmActionTest.IBDB_USER_ID,
				ProcessImportedGermplasmActionTest.IBDB_USER_ID, name.getUserId());
		Assert.assertEquals("The name value should be " + ProcessImportedGermplasmActionTest.DESIGNATION,
				ProcessImportedGermplasmActionTest.DESIGNATION, name.getNval());
		Assert.assertEquals("The location id should be " + ProcessImportedGermplasmAction.DEFAULT_LOCATION_ID,
				ProcessImportedGermplasmAction.DEFAULT_LOCATION_ID, name.getLocationId());
		Assert.assertEquals("The date should be " + ProcessImportedGermplasmActionTest.DATE_INT_VALUE,
				ProcessImportedGermplasmActionTest.DATE_INT_VALUE, name.getNdate());
		Assert.assertTrue("The reference id should be 0", 0 == name.getReferenceId());
	}

	@Test
	public void testCreateNameObjectWithSelectedLocation() {
		final String locationId = "1";
		final ComboBox locationComboBox = this.processImportedGermplasmAction.getGermplasmFieldsComponent()
				.getLocationComboBox();
		locationComboBox.addItem(locationId);
		locationComboBox.setItemCaption(locationId, "1");
		locationComboBox.setValue(locationId);
		final Name name = this.processImportedGermplasmAction.createNameObject(
				ProcessImportedGermplasmActionTest.IBDB_USER_ID, ProcessImportedGermplasmActionTest.DATE_INT_VALUE,
				ProcessImportedGermplasmActionTest.DESIGNATION);

		Assert.assertEquals("The user id should be " + ProcessImportedGermplasmActionTest.IBDB_USER_ID,
				ProcessImportedGermplasmActionTest.IBDB_USER_ID, name.getUserId());
		Assert.assertEquals("The name value should be " + ProcessImportedGermplasmActionTest.DESIGNATION,
				ProcessImportedGermplasmActionTest.DESIGNATION, name.getNval());
		Assert.assertEquals("The location id should be " + locationId, locationId, name.getLocationId().toString());
		Assert.assertEquals("The date should be " + ProcessImportedGermplasmActionTest.DATE_INT_VALUE,
				ProcessImportedGermplasmActionTest.DATE_INT_VALUE, name.getNdate());
		Assert.assertTrue("The reference id should be 0", 0 == name.getReferenceId());
	}

	@Test
	public void testProcessNextItemsNoListenerRemaining() {
		this.processImportedGermplasmAction.processNextItems();

		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(1)).saveTheList();
	}

	@Test
	public void testProcessNextItemsReuseGermplasmForPedigreeOptionTwo() {
		Mockito.doReturn(ProcessImportedGermplasmAction.CREATE_NEW_RECORD_WITH_PEDIGREE_CONN)
				.when(this.germplasmDetailsComponent).getPedigreeOption();
		// Create list of germplasm-name pairs, with duplicate designations
		final int noOfEntries = 10;
		final int indexOfEntryForReuse = 0;
		final int indexOfDupeEntry = 5;
		final List<GermplasmName> germplasmNameObjects = this
				.createGermplasmNamePairsWithDuplicateDesignations(noOfEntries);
		this.processImportedGermplasmAction.setGermplasmNameObjects(germplasmNameObjects);

		// flag germplasm for reuse by entry with duplicate designation
		this.processImportedGermplasmAction.mapDesignationToGermplasmForReuse(
				germplasmNameObjects.get(indexOfEntryForReuse).getName().getNval(), indexOfEntryForReuse);
		final SelectGermplasmWindow selectGermplasmWindow = new SelectGermplasmWindow(
				this.processImportedGermplasmAction, germplasmNameObjects.get(indexOfDupeEntry).getName().getNval(),
				indexOfDupeEntry, this.parentWindow);
		final ArrayList<ImportGermplasmEntryActionListener> importListeners = new ArrayList<ImportGermplasmEntryActionListener>();
		importListeners.add(selectGermplasmWindow);
		this.processImportedGermplasmAction.setImportEntryListener(importListeners);

		// Set values of gpid1 and gpid2 to verify they were changed later
		final Germplasm germplasmOfDuplicateEntry = this.processImportedGermplasmAction.getGermplasmNameObjects()
				.get(indexOfDupeEntry).getGermplasm();
		germplasmOfDuplicateEntry.setGpid1(0);
		germplasmOfDuplicateEntry.setGpid2(0);

		// Method to test - processing of SelectGermplasmWindow
		this.processImportedGermplasmAction.processNextItems();

		// Check that GIDs remain unchanged, only pedigree connections
		final Integer gidOfEntryToReuse = this.processImportedGermplasmAction.getGermplasmNameObjects()
				.get(indexOfEntryForReuse).getGermplasm().getGid();
		Assert.assertFalse(gidOfEntryToReuse.equals(germplasmOfDuplicateEntry.getGid()));
		Assert.assertEquals(gidOfEntryToReuse, germplasmOfDuplicateEntry.getGpid1());
		Assert.assertEquals(gidOfEntryToReuse, germplasmOfDuplicateEntry.getGpid2());
	}

	@Test
	public void testProcessNextItemsReuseGermplasmForPedigreeOptionThree() {
		Mockito.doReturn(ProcessImportedGermplasmAction.SELECT_MATCHING_GERMPLASM).when(this.germplasmDetailsComponent)
				.getPedigreeOption();
		// Create list of germplasm-name pairs, with duplicate designations
		final int noOfEntries = 10;
		final int indexOfEntryForReuse = 0;
		final int indexOfDupeEntry = 5;
		final List<GermplasmName> germplasmNameObjects = this
				.createGermplasmNamePairsWithDuplicateDesignations(noOfEntries);
		this.processImportedGermplasmAction.setGermplasmNameObjects(germplasmNameObjects);

		// flag germplasm for reuse by entry with duplicate designation
		this.processImportedGermplasmAction.mapDesignationToGermplasmForReuse(
				germplasmNameObjects.get(indexOfEntryForReuse).getName().getNval(), indexOfEntryForReuse);
		final SelectGermplasmWindow selectGermplasmWindow = new SelectGermplasmWindow(
				this.processImportedGermplasmAction, germplasmNameObjects.get(indexOfDupeEntry).getName().getNval(),
				indexOfDupeEntry, this.parentWindow);
		final ArrayList<ImportGermplasmEntryActionListener> importListeners = new ArrayList<ImportGermplasmEntryActionListener>();
		importListeners.add(selectGermplasmWindow);
		this.processImportedGermplasmAction.setImportEntryListener(importListeners);

		// Verify that GIDs are unique prior to processing
		Assert.assertFalse(this.processImportedGermplasmAction.getGermplasmNameObjects().get(indexOfEntryForReuse)
				.getGermplasm().getGid().equals(this.processImportedGermplasmAction.getGermplasmNameObjects()
						.get(indexOfDupeEntry).getGermplasm().getGid()));

		// Method to test - processing of SelectGermplasmWindow
		this.processImportedGermplasmAction.processNextItems();

		// Check that germplasm for reuse was used on duplicate entry
		Assert.assertTrue(this.processImportedGermplasmAction.getGermplasmNameObjects().get(indexOfEntryForReuse)
				.getGermplasm().getGid().equals(this.processImportedGermplasmAction.getGermplasmNameObjects()
						.get(indexOfDupeEntry).getGermplasm().getGid()));

	}

	@Test
	public void testShowImportEntryListenerForSelectGermplasmWindow() {
		final SelectGermplasmWindow selectGermplasmWindow = Mockito.mock(SelectGermplasmWindow.class);

		// Call method to test
		this.processImportedGermplasmAction.showImportEntryListener(selectGermplasmWindow);

		Mockito.verify(this.parentWindow).addWindow(selectGermplasmWindow);
		Mockito.verify(selectGermplasmWindow).initializeTableValues();
	}

	@Test
	public void testAddNameToGermplasm() {
		final Integer gid = 1001;
		final Integer index = 0;
		final Name name = new Name();
		name.setNid(101);
		this.initializeAndSetGermplasmNameObjects();

		// Method to test
		this.processImportedGermplasmAction.addNameToGermplasm(name, gid, index);

		// Verify that new name was added to matched germplasm
		final List<Name> newNames = this.processImportedGermplasmAction.getNewNames();
		Assert.assertNotNull(newNames);
		Assert.assertFalse(newNames.isEmpty());
		Assert.assertEquals("Expecting new name was added to matched germplasm.", name, newNames.get(0));

		// Verify that GID of germplasm matched by designation was added to list
		// of IDs matched
		final List<Integer> matchedGIDs = this.processImportedGermplasmAction.getMatchedGermplasmIds();
		Assert.assertNotNull(matchedGIDs);
		Assert.assertFalse(matchedGIDs.isEmpty());
		Assert.assertEquals("Expecting GID of germplasm matched to designation as added to list of GIDs matched.", gid,
				matchedGIDs.get(0));
		Assert.assertTrue("Expecting flag for tracking if GID is matched is true.",
				this.processImportedGermplasmAction.getGermplasmNameObjects().get(0).isGidMatched());
	}

	@Test
	public void testReceiveGermplasmFromWindowWhenSelectMatchingGermplasmOption() {
		Mockito.doReturn(ProcessImportedGermplasmAction.SELECT_MATCHING_GERMPLASM).when(this.germplasmDetailsComponent)
				.getPedigreeOption();

		final int oldGid = 1;
		final GermplasmName germplasmToName = new GermplasmName(GermplasmTestDataInitializer.createGermplasm(oldGid),
				new Name());
		final List<GermplasmName> germplasmNameObjects = new ArrayList<>();
		germplasmNameObjects.add(germplasmToName);
		this.processImportedGermplasmAction.setGermplasmNameObjects(germplasmNameObjects);

		// Method to test
		final Integer newGid = 101;
		this.processImportedGermplasmAction.receiveGermplasmFromWindowAndUpdateGermplasmData(0,
				GermplasmTestDataInitializer.createGermplasm(newGid));

		// Verify that GID of germplasm matched by designation was added to list
		// of IDs matched
		final List<Integer> matchedGIDs = this.processImportedGermplasmAction.getMatchedGermplasmIds();
		Assert.assertNotNull(matchedGIDs);
		Assert.assertFalse(matchedGIDs.isEmpty());
		Assert.assertEquals("Expecting GID of germplasm selected was added to list of GIDs matched.", newGid,
				matchedGIDs.get(0));
		Assert.assertTrue("Expecting flag for tracking if GID is matched is true.",
				this.processImportedGermplasmAction.getGermplasmNameObjects().get(0).isGidMatched());
		// Verify that GID was copied from selected germplasm
		Assert.assertEquals("Expecting GID of germplasm selected was added to list of GIDs matched.", newGid,
				this.processImportedGermplasmAction.getGermplasmNameObjects().get(0).getGermplasm().getGid());
	}

	@Test
	public void testReceiveGermplasmFromWindowCreateNewRecordWithPedigreeConnectionOption() {
		Mockito.doReturn(ProcessImportedGermplasmAction.CREATE_NEW_RECORD_WITH_PEDIGREE_CONN)
				.when(this.germplasmDetailsComponent).getPedigreeOption();

		final Integer gid = 1;
		final Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(gid);
		germplasm.setGpid1(0);
		germplasm.setGpid2(0);
		germplasm.setGnpgs(-1);
		final GermplasmName germplasmToName = new GermplasmName(germplasm, new Name());
		final List<GermplasmName> germplasmNameObjects = new ArrayList<>();
		germplasmNameObjects.add(germplasmToName);
		this.processImportedGermplasmAction.setGermplasmNameObjects(germplasmNameObjects);

		// Method to test
		final Integer newGid = 101;
		final Germplasm selectedGermplasm = GermplasmTestDataInitializer.createGermplasm(newGid);
		selectedGermplasm.setGpid1(0);
		selectedGermplasm.setGpid2(0);
		selectedGermplasm.setGnpgs(-1);
		this.processImportedGermplasmAction.receiveGermplasmFromWindowAndUpdateGermplasmData(0, selectedGermplasm);

		// Verify that GPID1 and GPID2 of germplasm were changed but GID should
		// have been retained
		final Germplasm updatedGermplasm = this.processImportedGermplasmAction.getGermplasmNameObjects().get(0)
				.getGermplasm();
		Assert.assertEquals("Expecting GPID1 of germplasm to be imported was updated.", newGid,
				updatedGermplasm.getGpid1());
		Assert.assertEquals("Expecting GPID2 of germplasm to be imported was updated.", newGid,
				updatedGermplasm.getGpid2());
		Assert.assertEquals("Expecting GID of germplasm to be imported remained the same.", gid,
				updatedGermplasm.getGid());
		// Verify that GID of germplasm selected was not added to list of IDs
		// matched
		Assert.assertTrue(this.processImportedGermplasmAction.getMatchedGermplasmIds().isEmpty());
	}

	@Test
	public void testSearchOrAddANewGermplasmAutomaticallyAcceptSingleDesignationMatch() {
		final boolean automaticallyAcceptSingleMatch = true;
		Mockito.doReturn(automaticallyAcceptSingleMatch).when(this.germplasmDetailsComponent)
				.automaticallyAcceptSingleMatchesCheckbox();

		final int nameMatchesCount = 1;
		final String designation = "WOW 001";
		final NewDesignationForGermplasmConfirmDialog newDesignationDialog = new NewDesignationForGermplasmConfirmDialog(
				this.processImportedGermplasmAction, designation, 0, 100, 0, 0, nameMatchesCount);
		final List<Germplasm> germplasms = new ArrayList<Germplasm>();
		final Integer gidMatched = 10;
		germplasms.add(GermplasmTestDataInitializer.createGermplasm(gidMatched));
		Mockito.doReturn(germplasms).when(this.germplasmDataManager).getGermplasmByName(designation, 0, 1,
				Operation.EQUAL);

		final int oldGid = 1;
		final GermplasmName germplasmToName = new GermplasmName(GermplasmTestDataInitializer.createGermplasm(oldGid),
				new Name());
		final List<GermplasmName> germplasmNameObjects = new ArrayList<>();
		germplasmNameObjects.add(germplasmToName);
		this.processImportedGermplasmAction.setGermplasmNameObjects(germplasmNameObjects);

		// Method to test
		this.processImportedGermplasmAction.searchOrAddANewGermplasm(newDesignationDialog);

		// Verify that GID of germplasm matched by designation was added to list
		// of IDs matched and GID was updated
		final List<Integer> matchedGIDs = this.processImportedGermplasmAction.getMatchedGermplasmIds();
		Assert.assertNotNull(matchedGIDs);
		Assert.assertFalse(matchedGIDs.isEmpty());
		Assert.assertEquals("Expecting GID of germplasm matched to designation as added to list of GIDs matched.",
				gidMatched, matchedGIDs.get(0));
		Assert.assertTrue("Expecting flag for tracking if GID is matched is true.",
				this.processImportedGermplasmAction.getGermplasmNameObjects().get(0).isGidMatched());
		Assert.assertEquals("Expecting GID of germplasm to be imported was updated with matched GID.", gidMatched,
				this.processImportedGermplasmAction.getGermplasmNameObjects().get(0).getGermplasm().getGid());
		// Verify that no SelectGermplasmWindow was created
		Assert.assertTrue(this.processImportedGermplasmAction.getImportEntryListeners().isEmpty());
	}

	@Test
	public void testSearchOrAddANewGermplasmDoNotAutomaticallyAcceptSingleDesignationMatch() {
		// Sorry have to spy for preventing NPE when "showing" new
		// SelectGermplasmWindow to be added
		final ProcessImportedGermplasmAction spyAction = Mockito.spy(this.processImportedGermplasmAction);
		Mockito.doNothing().when(spyAction)
				.showImportEntryListener(Matchers.any(ImportGermplasmEntryActionListener.class));

		final boolean automaticallyAcceptSingleMatch = false;
		Mockito.doReturn(automaticallyAcceptSingleMatch).when(this.germplasmDetailsComponent)
				.automaticallyAcceptSingleMatchesCheckbox();

		final int nameMatchesCount = 1;
		final String designation = "WOW 001";
		final Integer germplasmIndex = 0;
		final NewDesignationForGermplasmConfirmDialog newDesignationDialog = new NewDesignationForGermplasmConfirmDialog(
				spyAction, designation, germplasmIndex, 100, germplasmIndex, germplasmIndex, nameMatchesCount);

		final int oldGid = 1;
		final GermplasmName germplasmToName = new GermplasmName(GermplasmTestDataInitializer.createGermplasm(oldGid),
				new Name());
		final List<GermplasmName> germplasmNameObjects = new ArrayList<>();
		germplasmNameObjects.add(germplasmToName);
		spyAction.setGermplasmNameObjects(germplasmNameObjects);
		// Check that no import listeners prior in order to verify that new one
		// was created and added in invocation of method to test
		Assert.assertTrue(spyAction.getImportEntryListeners().isEmpty());

		// Method to test
		spyAction.searchOrAddANewGermplasm(newDesignationDialog);

		Mockito.verify(this.germplasmDataManager, Mockito.never()).getGermplasmByName(designation, germplasmIndex, 1,
				Operation.EQUAL);
		// Verify that GID of germplasm selected was not added to list of IDs
		// matched
		Assert.assertTrue(spyAction.getMatchedGermplasmIds().isEmpty());
		final GermplasmName updatedGermplasmName = spyAction.getGermplasmNameObjects().get(germplasmIndex);
		Assert.assertFalse("Expecting flag for tracking if GID is matched is false.",
				updatedGermplasmName.isGidMatched());
		// Verify the new Select Germplasm Window was added and "shown"
		// immediately
		final ArgumentCaptor<ImportGermplasmEntryActionListener> listenerCaptor = ArgumentCaptor
				.forClass(ImportGermplasmEntryActionListener.class);
		Mockito.verify(spyAction).showImportEntryListener(listenerCaptor.capture());
		Assert.assertTrue("Expecting new SelectGermplasmWindow object was created.",
				listenerCaptor.getValue() instanceof SelectGermplasmWindow);
	}

	@Test
	public void testSearchOrAddANewGermplasmMultipleMatchesOnDesignation() {
		// Sorry have to spy for preventing NPE when "showing" new
		// SelectGermplasmWindow to be added
		final ProcessImportedGermplasmAction spyAction = Mockito.spy(this.processImportedGermplasmAction);
		Mockito.doNothing().when(spyAction)
				.showImportEntryListener(Matchers.any(ImportGermplasmEntryActionListener.class));

		final int nameMatchesCount = 3;
		final String designation = "WOW 001";
		final Integer germplasmIndex = 0;
		final NewDesignationForGermplasmConfirmDialog newDesignationDialog = new NewDesignationForGermplasmConfirmDialog(
				spyAction, designation, germplasmIndex, 100, germplasmIndex, germplasmIndex, nameMatchesCount);

		final int oldGid = 1;
		final GermplasmName germplasmToName = new GermplasmName(GermplasmTestDataInitializer.createGermplasm(oldGid),
				new Name());
		final List<GermplasmName> germplasmNameObjects = new ArrayList<>();
		germplasmNameObjects.add(germplasmToName);
		spyAction.setGermplasmNameObjects(germplasmNameObjects);
		// Check that no import listeners prior in order to verify that new one
		// was created and added in invocation of method to test
		Assert.assertTrue(spyAction.getImportEntryListeners().isEmpty());

		// Method to test
		spyAction.searchOrAddANewGermplasm(newDesignationDialog);

		Mockito.verify(this.germplasmDataManager, Mockito.never()).getGermplasmByName(designation, germplasmIndex, 1,
				Operation.EQUAL);
		// Verify that GID of germplasm selected was not added to list of IDs
		// matched
		Assert.assertTrue(spyAction.getMatchedGermplasmIds().isEmpty());
		final GermplasmName updatedGermplasmName = spyAction.getGermplasmNameObjects().get(germplasmIndex);
		Assert.assertFalse("Expecting flag for tracking if GID is matched is false.",
				updatedGermplasmName.isGidMatched());
		// Verify the new Select Germplasm Window was added and "shown"
		// immediately
		final ArgumentCaptor<ImportGermplasmEntryActionListener> listenerCaptor = ArgumentCaptor
				.forClass(ImportGermplasmEntryActionListener.class);
		Mockito.verify(spyAction).showImportEntryListener(listenerCaptor.capture());
		Assert.assertTrue("Expecting new SelectGermplasmWindow object was created.",
				listenerCaptor.getValue() instanceof SelectGermplasmWindow);
	}

	@Test
	public void testSetMatchedGermplasmGid() {
		final GermplasmName germplasmName = new GermplasmName(GermplasmTestDataInitializer.createGermplasm(1),
				NameTestDataInitializer.createName(GermplasmNameType.DERIVATIVE_NAME.getUserDefinedFieldID(), "name"));
		final Integer gid = 2;
		this.processImportedGermplasmAction.setMatchedGermplasmGid(gid, germplasmName);

		Assert.assertTrue("The gid should be matched.", germplasmName.isGidMatched());
		Assert.assertEquals("The gid should be " + gid, gid, germplasmName.getGermplasm().getGid());
	}

	@Test
	public void testSearchOrAddANewGermplasmNoDesignationMatch() {
		final int nameMatchesCount = 0;
		final String designation = "WOW 001";
		final NewDesignationForGermplasmConfirmDialog newDesignationDialog = new NewDesignationForGermplasmConfirmDialog(
				this.processImportedGermplasmAction, designation, 0, 100, 0, 0, nameMatchesCount);
		final List<Germplasm> germplasms = new ArrayList<Germplasm>();
		final Integer gidMatched = 10;
		germplasms.add(GermplasmTestDataInitializer.createGermplasm(gidMatched));
		Mockito.doReturn(germplasms).when(this.germplasmDataManager).getGermplasmByName(designation, 0, 1,
				Operation.EQUAL);

		final int oldGid = 1;
		final GermplasmName germplasmToName = new GermplasmName(GermplasmTestDataInitializer.createGermplasm(oldGid),
				new Name());
		final List<GermplasmName> germplasmNameObjects = new ArrayList<>();
		germplasmNameObjects.add(germplasmToName);
		this.processImportedGermplasmAction.setGermplasmNameObjects(germplasmNameObjects);

		// Method to test
		this.processImportedGermplasmAction.searchOrAddANewGermplasm(newDesignationDialog);

		// Verify that there was no matched GID
		final List<Integer> matchedGIDs = this.processImportedGermplasmAction.getMatchedGermplasmIds();
		Assert.assertNotNull(matchedGIDs);
		Assert.assertTrue(matchedGIDs.isEmpty());
		Assert.assertFalse("Expecting flag for tracking if GID is matched is false.",
				this.processImportedGermplasmAction.getGermplasmNameObjects().get(0).isGidMatched());
		// Verify that no SelectGermplasmWindow was created
		Assert.assertTrue(this.processImportedGermplasmAction.getImportEntryListeners().isEmpty());
	}

	/*
	 * Add an initial dummy import entry listener so that the table Select
	 * Germplasm window that will be added will not be populated (out of scope
	 * for this test) and cause NPE
	 */
	private void addInitialImportEntryListener() {
		final ArrayList<ImportGermplasmEntryActionListener> importListeners = new ArrayList<>();
		importListeners.add(Mockito.mock(NewDesignationForGermplasmConfirmDialog.class));
		this.processImportedGermplasmAction.setImportEntryListener(importListeners);
	}

	private List<GermplasmName> createGermplasmNamePairsWithDuplicateDesignations(final int noOfEntries) {
		final List<GermplasmName> germplasmNameObjects = ImportedGermplasmListDataInitializer
				.createGermplasmNameObjects(noOfEntries);
		final int middleIndex = noOfEntries / 2;
		for (int i = 0; i < noOfEntries / 2; i++) {
			final String newDesignation = germplasmNameObjects.get(i).getName().getNval();
			germplasmNameObjects.get(middleIndex + i).getName().setNval(newDesignation);
		}
		return germplasmNameObjects;
	}

	private void initializeAndSetGermplasmNameObjects() {
		final GermplasmName germplasmToName = new GermplasmName(new Germplasm(), new Name());
		final List<GermplasmName> germplasmNameObjects = new ArrayList<>();
		germplasmNameObjects.add(germplasmToName);
		this.processImportedGermplasmAction.setGermplasmNameObjects(germplasmNameObjects);
	}
}
