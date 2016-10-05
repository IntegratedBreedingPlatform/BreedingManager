
package org.generationcp.breeding.manager.listimport.actions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.generationcp.breeding.manager.action.SaveGermplasmListActionSource;
import org.generationcp.breeding.manager.data.initializer.ImportedGermplasmListDataInitializer;
import org.generationcp.breeding.manager.listimport.GermplasmFieldsComponent;
import org.generationcp.breeding.manager.listimport.GermplasmImportMain;
import org.generationcp.breeding.manager.listimport.SpecifyGermplasmDetailsComponent;
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

	final static Integer IBDB_USER_ID = 1;
	final static Integer DATE_INT_VALUE = 20151105;

	private ImportedGermplasmListDataInitializer importedGermplasmListInitializer;
	
	private NameTestDataInitializer nameTestDataInitializer;
	
	@Before
	public void setUp() throws Exception {
		// test data initializers
		this.importedGermplasmListInitializer = new ImportedGermplasmListDataInitializer();
		this.nameTestDataInitializer = new NameTestDataInitializer();
				
		this.processImportedGermplasmAction = new ProcessImportedGermplasmAction(this.germplasmDetailsComponent);
		this.processImportedGermplasmAction.setContextUtil(this.contextUtil);
		this.processImportedGermplasmAction.setGermplasmDataManager(this.germplasmDataManager);

		Mockito.doReturn(this.germplasmFieldsComponent).when(this.germplasmDetailsComponent).getGermplasmFieldsComponent();
		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(123);
		Mockito.when(this.germplasmDetailsComponent.getGermplasmFieldsComponent()).thenReturn(this.germplasmFieldsComponent);
		Mockito.when(this.germplasmDetailsComponent.getImportedGermplasm()).thenReturn(importedGermplasmListInitializer.createListOfImportedGermplasm(1, false));
		Mockito.when(this.germplasmDataManager.getGermplasmByGID(Matchers.isA(Integer.class))).thenReturn(GermplasmTestDataInitializer.createGermplasm(1));
		
		setUpComboBoxes();
		setUpBMSDateField();
		setUpGermplasmImportMain();
	}

	private void setUpGermplasmImportMain() {
		final GermplasmImportMain germplasmImportMain = new GermplasmImportMain(new Window(), true);
		Mockito.when(this.germplasmDetailsComponent.getSource()).thenReturn(germplasmImportMain);
		Mockito.when(this.germplasmDetailsComponent.getWindow()).thenReturn(new Window());
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

	/**
	 * Test to verify performSecondPedigreeAction method works properly if gid is specified
	 */
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

	/**
	 * Test to verify performSecondPedigree method works properly if germplasm match is one
	 */
	@Test
	public void testPerformSecondPedigreeActionIfGermplasmMatchCountIsOne() {
		final List<Germplasm> germplasms = new ArrayList<>();
		germplasms.add(GermplasmTestDataInitializer.createGermplasm(1));
		Mockito.when(
				this.germplasmDataManager.getGermplasmByName(Matchers.anyString(), Matchers.anyInt(), Matchers.anyInt(),
						Matchers.isA(Operation.class))).thenReturn(germplasms);

		this.processImportedGermplasmAction.performSecondPedigreeAction();

		Mockito.verify(this.contextUtil).getCurrentUserLocalId();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(5)).getGermplasmFieldsComponent();
		Mockito.verify(this.germplasmFieldsComponent).getGermplasmDateField();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(3)).getImportedGermplasm();
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(2)).getLocationComboBox();
		Mockito.verify(this.germplasmFieldsComponent).getBreedingMethodComboBox();
		Mockito.verify(this.germplasmFieldsComponent).getNameTypeComboBox();
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(2)).getLocationComboBox();

	}

	/**
	 * Test to verify performThirdPedigreeAction works properly or not
	 */
	@Test
	public void testPerformThirdPedigreeActionIfMatchingNameFound() {
		final List<Name> names = new ArrayList<>();
		names.add(this.nameTestDataInitializer.createName(1, 1, DESIGNATION));

		Mockito.when(
				this.germplasmDataManager.getNamesByGID(Matchers.isA(Integer.class), Matchers.anyInt(),
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
	}

	@Test
	public void testUpdateGidWhenGermplasmIdIsExisting() {
		final int gid = 100;
		final ImportedGermplasm importedGermplasm = this.importedGermplasmListInitializer.createImportedGermplasm(gid, true);
		importedGermplasm.setDesig("Name" + gid);

		final int germplasmMatchesCount = 1;
		final boolean searchByNameOrNewGermplasmIsNeeded = true;
		Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(0);

		Mockito.doReturn(true).when(this.germplasmDetailsComponent).automaticallyAcceptSingleMatchesCheckbox();

		final List<Germplasm> germplasms = new ArrayList<Germplasm>();
		germplasms.add(GermplasmTestDataInitializer.createGermplasm(gid));

		Mockito.doReturn(germplasms).when(this.germplasmDataManager)
				.getGermplasmByName(importedGermplasm.getDesig(), 0, 1, Operation.EQUAL);

		germplasm =
				this.processImportedGermplasmAction.updateGidForSingleMatch(ProcessImportedGermplasmActionTest.IBDB_USER_ID,
						ProcessImportedGermplasmActionTest.DATE_INT_VALUE, importedGermplasm, germplasmMatchesCount, germplasm,
						searchByNameOrNewGermplasmIsNeeded);

		Assert.assertEquals("Expecting that the gid set is from the existing germplasm.", gid, germplasm.getGid().intValue());
	}

	@Test
	public void testUpdateGidWhenNoGermplasmIdIsExisting() {
		final int gid = 0;
		final ImportedGermplasm importedGermplasm = this.importedGermplasmListInitializer.createImportedGermplasm(gid, true);
		importedGermplasm.setDesig("Name" + gid);

		final int germplasmMatchesCount = 0;
		final boolean searchByNameOrNewGermplasmIsNeeded = true;
		Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(0);

		germplasm =
				this.processImportedGermplasmAction.updateGidForSingleMatch(ProcessImportedGermplasmActionTest.IBDB_USER_ID,
						ProcessImportedGermplasmActionTest.DATE_INT_VALUE, importedGermplasm, germplasmMatchesCount, germplasm,
						searchByNameOrNewGermplasmIsNeeded);

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
}
