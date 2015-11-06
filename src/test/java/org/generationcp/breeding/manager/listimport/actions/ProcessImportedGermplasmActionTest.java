package org.generationcp.breeding.manager.listimport.actions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Window;
import org.generationcp.breeding.manager.action.SaveGermplasmListActionSource;
import org.generationcp.breeding.manager.data.initializer.GermplasmDataInitializer;
import org.generationcp.breeding.manager.data.initializer.ImportedGermplasmListDataInitializer;
import org.generationcp.breeding.manager.listimport.GermplasmFieldsComponent;
import org.generationcp.breeding.manager.listimport.GermplasmImportMain;
import org.generationcp.breeding.manager.listimport.SpecifyGermplasmDetailsComponent;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.ui.fields.BmsDateField;
import org.generationcp.middleware.manager.GermplasmDataManagerUtil;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Unit Test to verify Process Imported Germplasm Action file
 */
public class ProcessImportedGermplasmActionTest {

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private SaveGermplasmListActionSource saveGermplasmListActionSource;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@InjectMocks
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

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.processImportedGermplasmAction = Mockito.spy(new ProcessImportedGermplasmAction(germplasmDetailsComponent));
		this.processImportedGermplasmAction.setContextUtil(this.contextUtil);
		this.processImportedGermplasmAction.setGermplasmDataManager(this.germplasmDataManager);

		Mockito.doReturn(this.germplasmFieldsComponent).when(this.germplasmDetailsComponent).getGermplasmFieldsComponent();
		final ComboBox locationComboBox = new ComboBox();
		locationComboBox.addItem("1");
		Mockito.doReturn(locationComboBox).when(this.germplasmFieldsComponent).getLocationComboBox();

		final ComboBox methodComboBox = new ComboBox();
		methodComboBox.addItem("1");
		Mockito.doReturn(methodComboBox).when(this.germplasmFieldsComponent).getBreedingMethodComboBox();

		this.processImportedGermplasmAction.setGermplasmDataManager(this.germplasmDataManager);
	}

	/**
	 * Test to verify performSecondPedigreeAction method works properly if gid is specified
	 */
	@Test
	public void testPerformSecondPedigreeActionIfGidSpecified() {
		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(123);
		Mockito.when(this.germplasmDetailsComponent.getGermplasmFieldsComponent()).thenReturn(germplasmFieldsComponent);
		BmsDateField bmsDateField = new BmsDateField();
		Calendar cal = Calendar.getInstance();
		cal.set(2015, Calendar.JANUARY, 1);
		Date testCreatedDate = cal.getTime();
		bmsDateField.setValue(testCreatedDate);

		Mockito.when(germplasmFieldsComponent.getGermplasmDateField()).thenReturn(bmsDateField);

		List<ImportedGermplasm> importedGermplasms = new ArrayList<>();
		ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		importedGermplasm.setDesig("(CML454 X CML451)-B-4-1-112");
		importedGermplasm.setGid(123);
		importedGermplasms.add(importedGermplasm);

		Mockito.when(this.germplasmDetailsComponent.getImportedGermplasms()).thenReturn(importedGermplasms);

		Map<String, Integer> mapCountByNamePermutations = new HashMap<>();
		mapCountByNamePermutations.put("(CML454 X CML451)-B-4-1-112", 2);
		Mockito.when(this.germplasmDataManager.getCountByNamePermutations(Mockito.anyList())).thenReturn(mapCountByNamePermutations);

		ComboBox comboBox1 = new ComboBox();
		comboBox1.setValue(3);
		Mockito.when(this.germplasmFieldsComponent.getLocationComboBox()).thenReturn(comboBox1);

		ComboBox comboBox2 = new ComboBox();
		comboBox2.setValue(4);
		Mockito.when(this.germplasmFieldsComponent.getBreedingMethodComboBox()).thenReturn(comboBox2);

		Germplasm germplasm = new Germplasm();
		germplasm.setGid(1);
		germplasm.setGpid1(4);
		germplasm.setGpid2(5);
		Mockito.when(this.germplasmDataManager.getGermplasmByGID(Mockito.isA(Integer.class))).thenReturn(germplasm);

		ComboBox nameTypeComboBox = new ComboBox();
		nameTypeComboBox.setValue(3);

		ComboBox locationComboBox = new ComboBox();
		locationComboBox.setValue(4);

		Mockito.when(this.germplasmFieldsComponent.getNameTypeComboBox()).thenReturn(nameTypeComboBox);
		Mockito.when(this.germplasmFieldsComponent.getNameTypeComboBox()).thenReturn(locationComboBox);

		GermplasmImportMain germplasmImportMain = new GermplasmImportMain(new Window(),true);
		Mockito.when(this.germplasmDetailsComponent.getSource()).thenReturn(germplasmImportMain);
		Mockito.when(this.germplasmDetailsComponent.getWindow()).thenReturn(new Window());

		processImportedGermplasmAction.performSecondPedigreeAction();

		Mockito.verify(this.contextUtil).getCurrentUserLocalId();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(5)).getGermplasmFieldsComponent();
		Mockito.verify(this.germplasmFieldsComponent).getGermplasmDateField();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(4)).getImportedGermplasms();
		Mockito.verify(this.germplasmDataManager).getCountByNamePermutations(Mockito.anyList());
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(2)).getLocationComboBox();
		Mockito.verify(this.germplasmFieldsComponent).getBreedingMethodComboBox();
		Mockito.verify(this.germplasmDataManager).getGermplasmByGID(Mockito.isA(Integer.class));
		Mockito.verify(this.germplasmFieldsComponent).getNameTypeComboBox();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(2)).getSource();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(2)).getWindow();
	}

	/**
	 * Test to verify performSecondPedigree method works properly if germplasm match is one
	 */
	@Test
	public void testPerformSecondPedigreeActionIfGermplasmMatchCountIsOne() {

		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(123);
		Mockito.when(this.germplasmDetailsComponent.getGermplasmFieldsComponent()).thenReturn(germplasmFieldsComponent);
		BmsDateField bmsDateField = new BmsDateField();
		Calendar cal = Calendar.getInstance();
		cal.set(2015, Calendar.JANUARY, 1);
		Date testCreatedDate = cal.getTime();
		bmsDateField.setValue(testCreatedDate);

		Mockito.when(this.germplasmFieldsComponent.getGermplasmDateField()).thenReturn(bmsDateField);

		List<ImportedGermplasm> importedGermplasms = new ArrayList<>();
		ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		importedGermplasm.setDesig("(CML454 X CML451)-B-4-1-112");
		importedGermplasms.add(importedGermplasm);

		Mockito.when(this.germplasmDetailsComponent.getImportedGermplasms()).thenReturn(importedGermplasms);

		Map<String, Integer> mapCountByNamePermutations = new HashMap<>();
		mapCountByNamePermutations.put("(CML454 X CML451)-B-4-1-112", 1);
		Mockito.when(this.germplasmDataManager.getCountByNamePermutations(Mockito.anyList())).thenReturn(mapCountByNamePermutations);

		ComboBox comboBox1 = new ComboBox();
		comboBox1.setValue(3);
		Mockito.when(this.germplasmFieldsComponent.getLocationComboBox()).thenReturn(comboBox1);

		ComboBox comboBox2 = new ComboBox();
		comboBox2.setValue(4);
		Mockito.when(this.germplasmFieldsComponent.getBreedingMethodComboBox()).thenReturn(comboBox2);

		List<Germplasm> germplasms = new ArrayList<>();
		Germplasm germplasm = new Germplasm();
		germplasm.setGid(1);
		germplasm.setGpid1(2);
		germplasms.add(germplasm);
		Mockito.when(this.germplasmDataManager.getGermplasmByName(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.isA(Operation.class))).thenReturn(germplasms);

		ComboBox nameTypeComboBox = new ComboBox();
		nameTypeComboBox.setValue(3);

		ComboBox locationComboBox = new ComboBox();
		locationComboBox.setValue(4);

		Mockito.when(this.germplasmFieldsComponent.getNameTypeComboBox()).thenReturn(nameTypeComboBox);
		Mockito.when(this.germplasmFieldsComponent.getNameTypeComboBox()).thenReturn(locationComboBox);

		GermplasmImportMain germplasmImportMain = new GermplasmImportMain(new Window(),true);
		Mockito.when(this.germplasmDetailsComponent.getSource()).thenReturn(germplasmImportMain);
		Mockito.when(this.germplasmDetailsComponent.getWindow()).thenReturn(new Window());

		processImportedGermplasmAction.performSecondPedigreeAction();

		Mockito.verify(this.contextUtil).getCurrentUserLocalId();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(5)).getGermplasmFieldsComponent();
		Mockito.verify(this.germplasmFieldsComponent).getGermplasmDateField();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(4)).getImportedGermplasms();
		Mockito.verify(this.germplasmDataManager).getCountByNamePermutations(Mockito.anyList());
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(2)).getLocationComboBox();
		Mockito.verify(this.germplasmFieldsComponent).getBreedingMethodComboBox();
		Mockito.verify(this.germplasmDataManager).getGermplasmByName(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.isA(Operation.class));
		Mockito.verify(this.germplasmFieldsComponent).getNameTypeComboBox();
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(2)).getLocationComboBox();

	}

	/**
	 * Test to verify performThirdPedigreeAction works properly or not
	 */
	@Test
	public void testPerformThirdPedigreeActionIfMatchingNameFound() {
		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(123);
		Mockito.when(this.germplasmDetailsComponent.getGermplasmFieldsComponent()).thenReturn(germplasmFieldsComponent);
		BmsDateField bmsDateField = new BmsDateField();
		Calendar cal = Calendar.getInstance();
		cal.set(2015, Calendar.JANUARY, 1);
		Date testCreatedDate = cal.getTime();
		bmsDateField.setValue(testCreatedDate);

		Mockito.when(this.germplasmFieldsComponent.getGermplasmDateField()).thenReturn(bmsDateField);

		List<ImportedGermplasm> importedGermplasms = new ArrayList<>();
		ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		importedGermplasm.setDesig("(CML454 X CML451)-B-4-1-112");
		importedGermplasm.setGid(1);
		importedGermplasms.add(importedGermplasm);
		Mockito.when(this.germplasmDetailsComponent.getImportedGermplasms()).thenReturn(importedGermplasms);

		Map<String, Integer> mapCountByNamePermutations = new HashMap<>();
		mapCountByNamePermutations.put("(CML454 X CML451)-B-4-1-112", 2);
		Mockito.when(this.germplasmDataManager.getCountByNamePermutations(Mockito.anyList())).thenReturn(mapCountByNamePermutations);

		Germplasm germplasm = new Germplasm();
		germplasm.setGid(1);
		germplasm.setGpid1(4);
		germplasm.setGpid2(5);
		Mockito.when(this.germplasmDataManager.getGermplasmByGID(Mockito.isA(Integer.class))).thenReturn(germplasm);

		List<Name> names = new ArrayList<>();
		Name name = new Name();
		name.setNstat(1);
		name.setGermplasmId(1);
		name.setNid(1);
		name.setNval("(CML454 X CML451)-B-4-1-112");
		names.add(name);

		Mockito.when(this.germplasmDataManager.getNamesByGID(Mockito.isA(Integer.class), Mockito.anyInt(), (GermplasmNameType) Mockito.isNull()))
				.thenReturn(names);

		ComboBox nameTypeComboBox = new ComboBox();
		nameTypeComboBox.setValue(3);
		Mockito.when(this.germplasmFieldsComponent.getNameTypeComboBox()).thenReturn(nameTypeComboBox);

		ComboBox locationIdComboBox = new ComboBox();
		locationIdComboBox.setValue(4);
		Mockito.when(this.germplasmFieldsComponent.getLocationComboBox()).thenReturn(locationIdComboBox);

		processImportedGermplasmAction.performThirdPedigreeAction();

		Mockito.verify(this.contextUtil).getCurrentUserLocalId();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(3)).getGermplasmFieldsComponent();
		Mockito.verify(this.germplasmFieldsComponent).getGermplasmDateField();
		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(4)).getImportedGermplasms();
		Mockito.verify(this.germplasmDataManager).getCountByNamePermutations(Mockito.anyList());
		Mockito.verify(this.germplasmDataManager).getGermplasmByGID(Mockito.isA(Integer.class));
		Mockito.verify(this.germplasmDataManager)
				.getNamesByGID(Mockito.isA(Integer.class), Mockito.anyInt(), (GermplasmNameType) Mockito.isNull());
		Mockito.verify(this.germplasmFieldsComponent).getNameTypeComboBox();
		Mockito.verify(this.germplasmFieldsComponent).getLocationComboBox();
	}

	@Test
	public void testUpdateGidWhenGermplasmIdIsExisting() {
		final int gid = 100;
		final ImportedGermplasm importedGermplasm = ImportedGermplasmListDataInitializer.createImportedGermplasm(gid);
		importedGermplasm.setDesig("Name" + gid);

		final int germplasmMatchesCount = 1;
		final boolean searchByNameOrNewGermplasmIsNeeded = true;
		Germplasm germplasm = GermplasmDataInitializer.createGermplasm(0);

		Mockito.doReturn(true).when(this.germplasmDetailsComponent).automaticallyAcceptSingleMatchesCheckbox();

		final List<Germplasm> germplasms = new ArrayList<Germplasm>();
		germplasms.add(GermplasmDataInitializer.createGermplasm(gid));

		Mockito.doReturn(germplasms).when(this.germplasmDataManager)
				.getGermplasmByName(importedGermplasm.getDesig(), 0, 1, Operation.EQUAL);

		germplasm =
				this.processImportedGermplasmAction.updateGidForSingleMatch(IBDB_USER_ID, this.DATE_INT_VALUE, importedGermplasm,
						germplasmMatchesCount, germplasm, searchByNameOrNewGermplasmIsNeeded);

		Assert.assertEquals("Expecting that the gid set is from the existing germplasm.", gid, germplasm.getGid().intValue());
	}

	@Test
	public void testUpdateGidWhenNoGermplasmIdIsExisting() {
		final int gid = 0;
		final ImportedGermplasm importedGermplasm = ImportedGermplasmListDataInitializer.createImportedGermplasm(gid);
		importedGermplasm.setDesig("Name" + gid);

		final int germplasmMatchesCount = 0;
		final boolean searchByNameOrNewGermplasmIsNeeded = true;
		Germplasm germplasm = GermplasmDataInitializer.createGermplasm(0);

		germplasm =
				this.processImportedGermplasmAction.updateGidForSingleMatch(IBDB_USER_ID, this.DATE_INT_VALUE, importedGermplasm,
						germplasmMatchesCount, germplasm, searchByNameOrNewGermplasmIsNeeded);

		Mockito.verify(this.germplasmDetailsComponent, Mockito.times(0)).automaticallyAcceptSingleMatchesCheckbox();
		Mockito.verify(this.germplasmDataManager, Mockito.times(0)).getGermplasmByName(importedGermplasm.getDesig(), 0, 1, Operation.EQUAL);
		Assert.assertEquals("Expecting that the gid is set to 0 when there is no existing germplasm.", 0, germplasm.getGid().intValue());
	}
}
