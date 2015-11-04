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
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private SaveGermplasmListActionSource saveGermplasmListActionSource;

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

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.processImportedGermplasmAction = Mockito.spy(new ProcessImportedGermplasmAction(germplasmDetailsComponent));
		this.processImportedGermplasmAction.setContextUtil(this.contextUtil);
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
		Mockito.when(this.germplasmDataManager.getMapCountByNamePermutations(Mockito.anyList())).thenReturn(mapCountByNamePermutations);

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
		Mockito.verify(this.germplasmDataManager).getMapCountByNamePermutations(Mockito.anyList());
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
		Mockito.when(this.germplasmDataManager.getMapCountByNamePermutations(Mockito.anyList())).thenReturn(mapCountByNamePermutations);

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
		Mockito.verify(this.germplasmDataManager).getMapCountByNamePermutations(Mockito.anyList());
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
		Mockito.when(this.germplasmDataManager.getMapCountByNamePermutations(Mockito.anyList())).thenReturn(mapCountByNamePermutations);

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

		Mockito.when(this.germplasmDataManager.getNamesByGID(Mockito.isA(Integer.class), Mockito.anyInt(),
				(GermplasmNameType) Mockito.isNull())).thenReturn(names);

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
		Mockito.verify(this.germplasmDataManager).getMapCountByNamePermutations(Mockito.anyList());
		Mockito.verify(this.germplasmDataManager).getGermplasmByGID(Mockito.isA(Integer.class));
		Mockito.verify(this.germplasmDataManager)
				.getNamesByGID(Mockito.isA(Integer.class), Mockito.anyInt(), (GermplasmNameType) Mockito.isNull());
		Mockito.verify(this.germplasmFieldsComponent).getNameTypeComboBox();
		Mockito.verify(this.germplasmFieldsComponent).getLocationComboBox();
	}
}
