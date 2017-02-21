
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

		Mockito.doReturn(this.germplasmFieldsComponent).when(this.germplasmDetailsComponent).getGermplasmFieldsComponent();
		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(123);
		Mockito.when(this.germplasmDetailsComponent.getGermplasmFieldsComponent()).thenReturn(this.germplasmFieldsComponent);
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
	public void testPerformFirstPedigreeActionWithDuplicateDesignationsInFile() {
		// Create 10 imported germplasm with first 5 entries having duplicate designations within the file
		final List<ImportedGermplasm> importedGermplasm = ImportedGermplasmListDataInitializer.createListOfImportedGermplasm(5, false);
		importedGermplasm.addAll(ImportedGermplasmListDataInitializer.createListOfImportedGermplasm(5, false));
		Mockito.doReturn(importedGermplasm).when(this.germplasmDetailsComponent).getImportedGermplasm();

		// Method to test
		this.processImportedGermplasmAction.performFirstPedigreeAction();

		// Check that there are 10 unique germplasm (gid) to be saved, despite duplicate designations
		Assert.assertEquals(10, this.processImportedGermplasmAction.getGermplasmNameObjects().size());
		final Set<Integer> uniqueGids = new HashSet<>();
		for (final GermplasmName germplasmNamePair : this.processImportedGermplasmAction.getGermplasmNameObjects()) {
			uniqueGids.add(germplasmNamePair.getGermplasm().getGid());
		}
		Assert.assertEquals(10, uniqueGids.size());
	}

	@Test
	public void testPerformSecondPedigreeActionWithDuplicateDesignationsInFile() {
		// Create 10 imported germplasm with first 5 entries having duplicate designations within the file
		final List<ImportedGermplasm> importedGermplasm = ImportedGermplasmListDataInitializer.createListOfImportedGermplasm(5, false);
		importedGermplasm.addAll(ImportedGermplasmListDataInitializer.createListOfImportedGermplasm(5, false));
		Mockito.doReturn(importedGermplasm).when(this.germplasmDetailsComponent).getImportedGermplasm();

		// Method to test
		this.processImportedGermplasmAction.performSecondPedigreeAction();

		// Check that there are 10 unique germplasm (gid) to be saved, despite duplicate designations
		Assert.assertEquals(10, this.processImportedGermplasmAction.getGermplasmNameObjects().size());
		final Set<Integer> uniqueGids = new HashSet<>();
		for (final GermplasmName germplasmNamePair : this.processImportedGermplasmAction.getGermplasmNameObjects()) {
			uniqueGids.add(germplasmNamePair.getGermplasm().getGid());
		}
		Assert.assertEquals(10, uniqueGids.size());
	}

	@Test
	public void testPerformThirdPedigreeActionWithDuplicateDesignationsInFile() {
		// Create 10 imported germplasm with first 5 entries having duplicate designations within the file, no GIDs specified
		final boolean gidsSpecifiedInFile = false;
		final List<ImportedGermplasm> importedGermplasm =
				ImportedGermplasmListDataInitializer.createListOfImportedGermplasm(5, false, gidsSpecifiedInFile);
		importedGermplasm.addAll(ImportedGermplasmListDataInitializer.createListOfImportedGermplasm(5, false, gidsSpecifiedInFile));
		Mockito.doReturn(importedGermplasm).when(this.germplasmDetailsComponent).getImportedGermplasm();

		// Method to test
		this.processImportedGermplasmAction.performThirdPedigreeAction();

		// Check that there are 10 unique germplasm (gid) to be saved, despite duplicate designations
		Assert.assertEquals(10, this.processImportedGermplasmAction.getGermplasmNameObjects().size());
		final Set<Integer> uniqueGids = new HashSet<>();
		for (final GermplasmName germplasmNamePair : this.processImportedGermplasmAction.getGermplasmNameObjects()) {
			uniqueGids.add(germplasmNamePair.getGermplasm().getGid());
		}
		Assert.assertEquals(10, uniqueGids.size());
	}

	@Test
	public void testPerformSecondPedigreeActionIfGidSpecified() {
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
	public void testPerformSecondPedigreeActionIfSingleDesignationMatchForAllGermplasm() {
		final List<Germplasm> germplasm = new ArrayList<>();
		germplasm.add(GermplasmTestDataInitializer.createGermplasm(1));
		Mockito.when(this.germplasmDataManager.getGermplasmByName(Matchers.anyString(), Matchers.anyInt(), Matchers.anyInt(),
				Matchers.isA(Operation.class))).thenReturn(germplasm);

		this.processImportedGermplasmAction.performSecondPedigreeAction();

		Mockito.verify(this.contextUtil).getCurrentUserLocalId();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(5)).getGermplasmFieldsComponent();
		Mockito.verify(this.germplasmFieldsComponent).getGermplasmDateField();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(3)).getImportedGermplasm();
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(2)).getLocationComboBox();
		Mockito.verify(this.germplasmFieldsComponent).getBreedingMethodComboBox();
		Mockito.verify(this.germplasmFieldsComponent).getNameTypeComboBox();
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(2)).getLocationComboBox();

		// Verify that no instance of SelectGermplasmWindow was added to list of listeners, as it's only done for multiple matches
		final List<ImportGermplasmEntryActionListener> importEntryListeners = this.processImportedGermplasmAction.getImportEntryListeners();
		Assert.assertNotNull(importEntryListeners);
		Assert.assertTrue(importEntryListeners.isEmpty());

	}

	/**
	 * Test to verify that SelectGermplasmWindow is created for multiple matches
	 */
	@Test
	public void testPerformSecondPedigreeActionIfMultipleMatchesOnDesignation() {
		// Create 3 germplasm to be imported, with multiple designation matches for 2nd germplasm
		final List<Germplasm> germplasmList = new ArrayList<>();
		germplasmList.add(GermplasmTestDataInitializer.createGermplasm(1));
		germplasmList.add(GermplasmTestDataInitializer.createGermplasm(2));
		germplasmList.add(GermplasmTestDataInitializer.createGermplasm(3));
		Mockito.when(this.germplasmDetailsComponent.getImportedGermplasm())
				.thenReturn(ImportedGermplasmListDataInitializer.createListOfImportedGermplasm(3, false));
		Mockito.when(this.germplasmDataManager.getGermplasmByName(Matchers.anyString(), Matchers.anyInt(), Matchers.anyInt(),
				Matchers.isA(Operation.class))).thenReturn(germplasmList);

		// Simulate 3 matches when searching by second germplasm's designation
		Mockito.when(
				this.germplasmDataManager.countGermplasmByName(ImportedGermplasmListDataInitializer.DESIGNATION + "-" + 2, Operation.EQUAL))
				.thenReturn(3L);
		// Hack - add an initial dummy import entry listener so that the table Select Germplasm window
		// that will be added will not be populated (out of scope for this test) and cause NPE
		final ArrayList<ImportGermplasmEntryActionListener> importListeners = new ArrayList<ImportGermplasmEntryActionListener>();
		importListeners.add(Mockito.mock(SelectGermplasmWindow.class));
		this.processImportedGermplasmAction.setImportEntryListener(importListeners);

		// Method to test
		this.processImportedGermplasmAction.performSecondPedigreeAction();

		// Verify that an instance of SelectGermplasmWindow was added to list of listeners
		final List<ImportGermplasmEntryActionListener> importEntryListeners = this.processImportedGermplasmAction.getImportEntryListeners();
		Assert.assertNotNull(importEntryListeners);
		// 2 listeners including the dummy one added earlier
		Assert.assertTrue(importEntryListeners.size() == 2);
		final SelectGermplasmWindow selectGermplasmWindow = (SelectGermplasmWindow) importEntryListeners.get(1);

		// Check that the window was created for 2nd entry and that total # of entries displayed equals # of imported germplasm
		Assert.assertTrue(selectGermplasmWindow.getGermplasmIndex() == 1);
		Assert.assertTrue(selectGermplasmWindow.getNoOfImportedGermplasm() == 3);
	}

	@Test
	public void testPerformThirdPedigreeActionIfSingleDesignationMatchForAllGermplasm() {
		final List<Name> names = new ArrayList<>();
		names.add(NameTestDataInitializer.createName(1, 1, ProcessImportedGermplasmActionTest.DESIGNATION + "-" + 1));

		Mockito.when(this.germplasmDataManager.getNamesByGID(Matchers.isA(Integer.class), Matchers.anyInt(),
				(GermplasmNameType) Matchers.isNull())).thenReturn(names);

		this.processImportedGermplasmAction.performThirdPedigreeAction();

		Mockito.verify(this.contextUtil).getCurrentUserLocalId();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(3)).getGermplasmFieldsComponent();
		Mockito.verify(this.germplasmFieldsComponent).getGermplasmDateField();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(3)).getImportedGermplasm();
		Mockito.verify(this.germplasmDataManager).getGermplasmByGID(Matchers.isA(Integer.class));
		Mockito.verify(this.germplasmDataManager).getNamesByGID(Matchers.isA(Integer.class), Matchers.anyInt(),
				(GermplasmNameType) Matchers.isNull());
		Mockito.verify(this.germplasmFieldsComponent).getNameTypeComboBox();
		Mockito.verify(this.germplasmFieldsComponent).getLocationComboBox();

		// Verify that no instance of SelectGermplasmWindow was added to list of listeners, as it's only done for multiple matches
		final List<ImportGermplasmEntryActionListener> importEntryListeners = this.processImportedGermplasmAction.getImportEntryListeners();
		Assert.assertNotNull(importEntryListeners);
		Assert.assertTrue(importEntryListeners.isEmpty());
	}

	@Test
	public void testPerformThirdPedigreeActionIfMultipleMatchesOnDesignation() {
		final List<Name> names = new ArrayList<>();
		names.add(NameTestDataInitializer.createName(1, 1, ProcessImportedGermplasmActionTest.DESIGNATION + "-" + 1));

		Mockito.when(this.germplasmDetailsComponent.getImportedGermplasm())
				.thenReturn(ImportedGermplasmListDataInitializer.createListOfImportedGermplasm(3, false));

		Mockito.when(this.germplasmDataManager.getNamesByGID(Matchers.isA(Integer.class), Matchers.anyInt(),
				(GermplasmNameType) Matchers.isNull())).thenReturn(names);
		Mockito.when(this.germplasmDataManager.getGermplasmByGID(Matchers.anyInt())).thenReturn(null);

		// Simulate 3 matches when searching by second germplasm's designation
		Mockito.when(
				this.germplasmDataManager.countGermplasmByName(ImportedGermplasmListDataInitializer.DESIGNATION + "-" + 2, Operation.EQUAL))
				.thenReturn(3L);

		// Hack - add an initial dummy import entry listener so that the table Select Germplasm window
		// that will be added will not be populated (out of scope for this test) and cause NPE
		final ArrayList<ImportGermplasmEntryActionListener> importListeners = new ArrayList<ImportGermplasmEntryActionListener>();
		importListeners.add(Mockito.mock(SelectGermplasmWindow.class));
		this.processImportedGermplasmAction.setImportEntryListener(importListeners);

		// Method to test
		this.processImportedGermplasmAction.performThirdPedigreeAction();

		Mockito.verify(this.contextUtil).getCurrentUserLocalId();
		Mockito.verify(this.germplasmFieldsComponent).getGermplasmDateField();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(5)).getImportedGermplasm();
		Mockito.verify(this.germplasmDataManager, Mockito.times(3)).getGermplasmByGID(Matchers.isA(Integer.class));
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(3)).getNameTypeComboBox();
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(6)).getLocationComboBox();

		// Verify that one instance of SelectGermplasmWindow was added to list of listeners
		final List<ImportGermplasmEntryActionListener> importEntryListeners = this.processImportedGermplasmAction.getImportEntryListeners();
		Assert.assertNotNull(importEntryListeners);
		// 2 import entry listeners including dummy one created earlier
		Assert.assertTrue(importEntryListeners.size() == 2);
		final SelectGermplasmWindow selectGermplasmWindow = (SelectGermplasmWindow) importEntryListeners.get(1);
		// Check that the window was created for 2nd entry and that total # of entries displayed equals # of imported germplasm
		Assert.assertTrue(selectGermplasmWindow.getGermplasmIndex() == 1);
		Assert.assertTrue(selectGermplasmWindow.getNoOfImportedGermplasm() == 3);
	}

	@Test
	public void testUpdateGidWhenGermplasmIdIsExisting() {
		final int gid = 100;
		final ImportedGermplasm importedGermplasm = ImportedGermplasmListDataInitializer.createImportedGermplasm(gid, true);
		importedGermplasm.setDesig("Name" + gid);

		final int germplasmMatchesCount = 1;
		Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(0);

		Mockito.doReturn(true).when(this.germplasmDetailsComponent).automaticallyAcceptSingleMatchesCheckbox();

		final List<Germplasm> germplasms = new ArrayList<Germplasm>();
		germplasms.add(GermplasmTestDataInitializer.createGermplasm(gid));

		Mockito.doReturn(germplasms).when(this.germplasmDataManager).getGermplasmByName(importedGermplasm.getDesig(), 0, 1,
				Operation.EQUAL);

		germplasm = this.processImportedGermplasmAction.createGermplasmAndUpdateGidForSingleMatch(gid,
				ProcessImportedGermplasmActionTest.IBDB_USER_ID, ProcessImportedGermplasmActionTest.DATE_INT_VALUE, importedGermplasm,
				germplasmMatchesCount, germplasm);

		Assert.assertEquals("Expecting that the gid set is from the existing germplasm.", gid, germplasm.getGid().intValue());
	}

	@Test
	public void testUpdateGidWhenNoGermplasmIdIsExisting() {
		final int gid = 0;
		final ImportedGermplasm importedGermplasm = ImportedGermplasmListDataInitializer.createImportedGermplasm(gid, true);
		importedGermplasm.setDesig("Name" + gid);

		final int germplasmMatchesCount = 0;
		Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(0);

		germplasm = this.processImportedGermplasmAction.createGermplasmAndUpdateGidForSingleMatch(gid,
				ProcessImportedGermplasmActionTest.IBDB_USER_ID, ProcessImportedGermplasmActionTest.DATE_INT_VALUE, importedGermplasm,
				germplasmMatchesCount, germplasm);

		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(0)).automaticallyAcceptSingleMatchesCheckbox();
		Mockito.verify(this.germplasmDataManager, Mockito.times(0)).getGermplasmByName(importedGermplasm.getDesig(), 0, 1, Operation.EQUAL);
		Assert.assertEquals("Expecting that the gid is set to 0 when there is no existing germplasm.", 0, germplasm.getGid().intValue());
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

		final Germplasm germplasm = this.processImportedGermplasmAction.createGermplasmObject(gid, gnpgs, gpid1, gpid2, ibdbUserId, date);

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
		final ComboBox locationComboBox = this.processImportedGermplasmAction.getGermplasmFieldsComponent().getLocationComboBox();
		locationComboBox.addItem(locationId);
		locationComboBox.setItemCaption(locationId, "1");
		locationComboBox.setValue(locationId);
		final Germplasm germplasm = this.processImportedGermplasmAction.createGermplasmObject(gid, gnpgs, gpid1, gpid2,
				ProcessImportedGermplasmActionTest.IBDB_USER_ID, ProcessImportedGermplasmActionTest.DATE_INT_VALUE);

		Assert.assertEquals("The gid should be " + gid, gid, germplasm.getGid());
		Assert.assertEquals("The user id should be " + ProcessImportedGermplasmActionTest.IBDB_USER_ID,
				ProcessImportedGermplasmActionTest.IBDB_USER_ID, germplasm.getUserId());
		Assert.assertEquals("The location id should be " + locationId, locationId, germplasm.getLocationId().toString());
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
		final Name name = this.processImportedGermplasmAction.createNameObject(ProcessImportedGermplasmActionTest.IBDB_USER_ID,
				ProcessImportedGermplasmActionTest.DATE_INT_VALUE, ProcessImportedGermplasmActionTest.DESIGNATION);

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
		final ComboBox locationComboBox = this.processImportedGermplasmAction.getGermplasmFieldsComponent().getLocationComboBox();
		locationComboBox.addItem(locationId);
		locationComboBox.setItemCaption(locationId, "1");
		locationComboBox.setValue(locationId);
		final Name name = this.processImportedGermplasmAction.createNameObject(ProcessImportedGermplasmActionTest.IBDB_USER_ID,
				ProcessImportedGermplasmActionTest.DATE_INT_VALUE, ProcessImportedGermplasmActionTest.DESIGNATION);

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
		Mockito.doReturn("2").when(this.germplasmDetailsComponent).getPedigreeOption();
		// Create list of germplasm-name pairs, with duplicate designations
		final int noOfEntries = 10;
		final int indexOfEntryForReuse = 0;
		final int indexOfDupeEntry = 5;
		final List<GermplasmName> germplasmNameObjects = this.createGermplasmNamePairsWithDuplicateDesignations(noOfEntries);
		this.processImportedGermplasmAction.setGermplasmNameObjects(germplasmNameObjects);

		// flag germplasm for reuse by entry with duplicate designation
		this.processImportedGermplasmAction.mapDesignationToGermplasmForReuse(
				germplasmNameObjects.get(indexOfEntryForReuse).getName().getNval(), indexOfEntryForReuse);
		final SelectGermplasmWindow selectGermplasmWindow = new SelectGermplasmWindow(this.processImportedGermplasmAction,
				germplasmNameObjects.get(indexOfDupeEntry).getName().getNval(), indexOfDupeEntry, this.parentWindow);
		final ArrayList<ImportGermplasmEntryActionListener> importListeners = new ArrayList<ImportGermplasmEntryActionListener>();
		importListeners.add(selectGermplasmWindow);
		this.processImportedGermplasmAction.setImportEntryListener(importListeners);

		// Set values of gpid1 and gpid2 to verify they were changed later
		final Germplasm germplasmOfDuplicateEntry =
				this.processImportedGermplasmAction.getGermplasmNameObjects().get(indexOfDupeEntry).getGermplasm();
		germplasmOfDuplicateEntry.setGpid1(0);
		germplasmOfDuplicateEntry.setGpid2(0);

		// Method to test - processing of SelectGermplasmWindow
		this.processImportedGermplasmAction.processNextItems();

		// Check that GIDs remain unchanged, only pedigree connections
		final Integer gidOfEntryToReuse =
				this.processImportedGermplasmAction.getGermplasmNameObjects().get(indexOfEntryForReuse).getGermplasm().getGid();
		Assert.assertFalse(gidOfEntryToReuse.equals(germplasmOfDuplicateEntry.getGid()));
		Assert.assertEquals(gidOfEntryToReuse, germplasmOfDuplicateEntry.getGpid1());
		Assert.assertEquals(gidOfEntryToReuse, germplasmOfDuplicateEntry.getGpid2());
	}

	@Test
	public void testProcessNextItemsReuseGermplasmForPedigreeOptionThree() {
		Mockito.doReturn("3").when(this.germplasmDetailsComponent).getPedigreeOption();
		// Create list of germplasm-name pairs, with duplicate designations
		final int noOfEntries = 10;
		final int indexOfEntryForReuse = 0;
		final int indexOfDupeEntry = 5;
		final List<GermplasmName> germplasmNameObjects = this.createGermplasmNamePairsWithDuplicateDesignations(noOfEntries);
		this.processImportedGermplasmAction.setGermplasmNameObjects(germplasmNameObjects);

		// flag germplasm for reuse by entry with duplicate designation
		this.processImportedGermplasmAction.mapDesignationToGermplasmForReuse(
				germplasmNameObjects.get(indexOfEntryForReuse).getName().getNval(), indexOfEntryForReuse);
		final SelectGermplasmWindow selectGermplasmWindow = new SelectGermplasmWindow(this.processImportedGermplasmAction,
				germplasmNameObjects.get(indexOfDupeEntry).getName().getNval(), indexOfDupeEntry, this.parentWindow);
		final ArrayList<ImportGermplasmEntryActionListener> importListeners = new ArrayList<ImportGermplasmEntryActionListener>();
		importListeners.add(selectGermplasmWindow);
		this.processImportedGermplasmAction.setImportEntryListener(importListeners);

		// Verify that GIDs are unique prior to processing
		Assert.assertFalse(this.processImportedGermplasmAction.getGermplasmNameObjects().get(indexOfEntryForReuse).getGermplasm().getGid()
				.equals(this.processImportedGermplasmAction.getGermplasmNameObjects().get(indexOfDupeEntry).getGermplasm().getGid()));

		// Method to test - processing of SelectGermplasmWindow
		this.processImportedGermplasmAction.processNextItems();

		// Check that germplasm for reuse was used on duplicate entry
		Assert.assertTrue(this.processImportedGermplasmAction.getGermplasmNameObjects().get(indexOfEntryForReuse).getGermplasm().getGid()
				.equals(this.processImportedGermplasmAction.getGermplasmNameObjects().get(indexOfDupeEntry).getGermplasm().getGid()));

	}

	@Test
	public void testShowImportEntryListenerForSelectGermplasmWindow() {
		final SelectGermplasmWindow selectGermplasmWindow = Mockito.mock(SelectGermplasmWindow.class);

		// Call method to test
		this.processImportedGermplasmAction.showImportEntryListener(selectGermplasmWindow);

		Mockito.verify(this.parentWindow).addWindow(selectGermplasmWindow);
		Mockito.verify(selectGermplasmWindow).initializeTableValues();
	}

	private List<GermplasmName> createGermplasmNamePairsWithDuplicateDesignations(final int noOfEntries) {
		final List<GermplasmName> germplasmNameObjects = ImportedGermplasmListDataInitializer.createGermplasmNameObjects(noOfEntries);
		final int middleIndex = noOfEntries / 2;
		for (int i = 0; i < noOfEntries / 2; i++) {
			final String newDesignation = germplasmNameObjects.get(i).getName().getNval();
			germplasmNameObjects.get(middleIndex + i).getName().setNval(newDesignation);
		}
		return germplasmNameObjects;
	}
}
