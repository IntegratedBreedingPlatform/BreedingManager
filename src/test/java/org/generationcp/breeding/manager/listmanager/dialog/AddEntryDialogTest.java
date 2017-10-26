
package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.generationcp.breeding.manager.customfields.BreedingLocationField;
import org.generationcp.breeding.manager.customfields.BreedingMethodField;
import org.generationcp.breeding.manager.customfields.ListDateField;
import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchBarComponent;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchResultsComponent;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

public class AddEntryDialogTest {

	private static final int NEW_ID = 1001;

	private final List<Integer> selectedGids = Arrays.asList(1, 2, 3, 4, 5);

	@Mock
	private GermplasmSearchResultsComponent searchResultsComponent;

	@Mock
	private GermplasmSearchBarComponent searchBarComponent;

	@Mock
	private AddEntryDialogSource dialogSource;

	@Mock
	private BreedingManagerService breedingManagerService;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private BreedingMethodField breedingMethodField;

	@Mock
	private BreedingLocationField breedingLocationField;

	@Mock
	private Window parentWindow;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Captor
	private ArgumentCaptor<List<Integer>> idsListCaptor;

	@Captor
	private ArgumentCaptor<Map<Germplasm, Name>> germplasmNameMapCaptor;

	@InjectMocks
	private AddEntryDialog addEntryDialog;

	private PagedBreedingManagerTable table;
	private OptionGroup optionGroup;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		this.addEntryDialog = new AddEntryDialog(this.dialogSource, this.parentWindow);

		final Project testProject = new Project();
		testProject.setUniqueID(UUID.randomUUID().toString());

		Mockito.when(this.breedingManagerService.getCurrentProject()).thenReturn(testProject);
		this.addEntryDialog.setBreedingManagerService(this.breedingManagerService);

		this.initializeTable(20);
		this.addEntryDialog.setSearchResultsComponent(this.searchResultsComponent);
		this.addEntryDialog.setSearchBarComponent(this.searchBarComponent);
		this.addEntryDialog.setMessageSource(this.messageSource);
	}

	@Test
	public void testValidateButtonListenersCount() throws Exception {
		// Initialize buttons
		this.addEntryDialog.initializeButtonLayout();
		this.addEntryDialog.addButtonListeners();

		// Verify listeners attached to buttons
		Collection<?> listeners = this.addEntryDialog.getDoneButton().getListeners(Button.ClickEvent.class);
		Assert.assertTrue("Done button has only 1 listener", listeners.size() == 1);

		listeners = this.addEntryDialog.getCancelButton().getListeners(Button.ClickEvent.class);
		Assert.assertTrue("Cancel button has only 1 listener", listeners.size() == 1);

	}

	private void initializeTable(final int numberOfItems) {
		this.table = new PagedBreedingManagerTable(1, 20);
		this.table.setSelectable(true);
		this.table.setMultiSelect(true);
		this.table.addContainerProperty(ColumnLabels.GID.getName() + "_REF", Integer.class, null);

		int i = 1;
		while (i <= numberOfItems) {
			this.table.addItem(i);
			this.table.getItem(i).getItemProperty(ColumnLabels.GID.getName() + "_REF").setValue(i);
			i++;
		}

		Mockito.doReturn(this.table).when(this.searchResultsComponent).getMatchingGermplasmTable();
	}

	@Test
	public void testDoneButtonClickActionOption1() {
		this.initializeOptionGroup();
		this.optionGroup.select(AddEntryDialog.OPTION_1_ID);

		// Setup mocks and setup test data
		final ClickEvent event = this.setupMockButtonEvent();
		this.table.setValue(this.selectedGids);

		// Method to test
		this.addEntryDialog.doneButtonClickAction(event);

		// Verify callback function for Option 1
		Mockito.verify(this.dialogSource).finishAddingEntry(this.selectedGids);
		Mockito.verify(this.parentWindow).removeWindow(Matchers.any(Window.class));
	}

	@Test
	public void testToggleDoneButtonStateWhenNoEntriesSelected() {
		this.addEntryDialog.initializeButtonLayout();
		this.addEntryDialog.toggleDoneButtonState();

		// Check that Done button is disabled if no entries selected on table
		Assert.assertFalse(this.addEntryDialog.getDoneButton().isEnabled());
	}

	@Test
	public void testToggleDoneButtonStateWhenEntriesSelected() {
		this.addEntryDialog.initializeButtonLayout();
		this.table.setValue(this.selectedGids);

		// Method to test
		this.addEntryDialog.toggleDoneButtonState();

		// Check that Done button is enabled since there are entries selected
		Assert.assertTrue(this.addEntryDialog.getDoneButton().isEnabled());
	}

	@Test
	public void testSaveNewGermplasmOption2() {
		this.initializeOptionGroup();
		this.optionGroup.select(AddEntryDialog.OPTION_2_ID);

		// Setup fields for populating germplasm values
		this.setupGermplasmDetailsFieldsAndMocks();
		this.table.setValue(this.selectedGids);

		// Method to test
		final Boolean addedSuccesfully = this.addEntryDialog.saveNewGermplasm();
		Assert.assertTrue(addedSuccesfully);

		// Verify key Middleware functions were performed
		Mockito.verify(this.germplasmDataManager).getGermplasms(this.idsListCaptor.capture());
		Assert.assertEquals("Expecting germplasmManager.getGermplasm was called.", this.selectedGids, this.idsListCaptor.getValue());

		Mockito.verify(this.germplasmDataManager).getPreferredNamesByGids(this.idsListCaptor.capture());
		Assert.assertEquals("Expecting germplasmManager.getPreferredNamesByGids was called.", this.selectedGids,
				this.idsListCaptor.getValue());

		Mockito.verify(this.germplasmDataManager).addGermplasm(this.germplasmNameMapCaptor.capture());
		Assert.assertNotNull(this.germplasmNameMapCaptor.getValue());
		Assert.assertEquals("Expecting " + this.selectedGids.size() + " germplasm and names was saved.", this.selectedGids.size(),
				this.germplasmNameMapCaptor.getValue().size());

		// Verify callback functions
		Mockito.verify(this.dialogSource).finishAddingEntry(this.idsListCaptor.capture());
		Assert.assertNotNull(this.idsListCaptor.getValue());
		Assert.assertEquals("Expecting " + this.selectedGids.size() + "to be added back to source table.", this.selectedGids.size(),
				this.idsListCaptor.getValue().size());
	}

	@Test
	public void testSaveNewGermplasmOption3() {
		this.initializeOptionGroup();
		this.optionGroup.select(AddEntryDialog.OPTION_3_ID);

		// Setup fields for populating germplasm values
		this.setupGermplasmDetailsFieldsAndMocks();
		this.table.setValue(this.selectedGids);

		// Method to test
		final Boolean addedSuccesfully = this.addEntryDialog.saveNewGermplasm();
		Assert.assertTrue(addedSuccesfully);

		// Verify no Middleware retrieval for existing germplasm and names
		Mockito.verify(this.germplasmDataManager, Mockito.times(0)).getGermplasms(Matchers.anyListOf(Integer.class));
		Mockito.verify(this.germplasmDataManager, Mockito.times(0)).getPreferredNamesByGids(Matchers.anyListOf(Integer.class));

		// Verify only one germplasm saved even if there are 5 selected
		Mockito.verify(this.germplasmDataManager).addGermplasm(this.germplasmNameMapCaptor.capture());
		Assert.assertNotNull(this.germplasmNameMapCaptor.getValue());
		Assert.assertEquals("Expecting only one germplasm and name pair was saved.", 1, this.germplasmNameMapCaptor.getValue().size());

		// Expecting only 1 entry to be added to source table
		Mockito.verify(this.dialogSource).finishAddingEntry(AddEntryDialogTest.NEW_ID);
	}

	@SuppressWarnings("unchecked")
	private void setupGermplasmDetailsFieldsAndMocks() {
		// Setup germplasm details fields and test values
		final ComboBox methodComboBox = new ComboBox();
		Mockito.doReturn(methodComboBox).when(this.breedingMethodField).getBreedingMethodComboBox();
		this.addEntryDialog.setBreedingMethodField(this.breedingMethodField);

		final ComboBox locationComboBox = new ComboBox();
		Mockito.doReturn(locationComboBox).when(this.breedingLocationField).getBreedingLocationComboBox();
		this.addEntryDialog.setBreedingLocationField(this.breedingLocationField);

		final ComboBox nameTypeComboBox = new ComboBox();
		this.addEntryDialog.setNameTypeComboBox(nameTypeComboBox);

		final ListDateField listDateField = new ListDateField("", true);
		listDateField.instantiateComponents();
		listDateField.setValue(DateUtil.getCurrentDate());
		this.addEntryDialog.setGermplasmDateField(listDateField);

		final TextField textField = new TextField();
		Mockito.doReturn(textField).when(this.searchBarComponent).getSearchField();
		this.addEntryDialog.setContextUtil(this.contextUtil);

		// set Middleware mocks
		this.addEntryDialog.setGermplasmDataManager(this.germplasmDataManager);
		final List<Germplasm> listOfGermplasm = new ArrayList<>();
		for (final Integer id : this.selectedGids) {
			listOfGermplasm.add(GermplasmTestDataInitializer.createGermplasm(id));
		}
		Mockito.doReturn(listOfGermplasm).when(this.germplasmDataManager).getGermplasms(this.selectedGids);

		final List<Integer> newIds = new ArrayList<>();
		for (final Integer id : this.selectedGids) {
			newIds.add(100 + id);
		}
		if (AddEntryDialog.OPTION_2_ID.equals(this.optionGroup.getValue())) {
			Mockito.doReturn(newIds).when(this.germplasmDataManager).addGermplasm(Matchers.anyMap());
		} else {
			Mockito.doReturn(Collections.singletonList(AddEntryDialogTest.NEW_ID)).when(this.germplasmDataManager)
					.addGermplasm(Matchers.anyMap());
		}

	}

	private ClickEvent setupMockButtonEvent() {
		final ClickEvent event = Mockito.mock(ClickEvent.class);
		final Button mockButton = Mockito.mock(Button.class);
		final Window mockWindow = Mockito.mock(Window.class);
		Mockito.doReturn(mockButton).when(event).getButton();
		Mockito.doReturn(mockWindow).when(mockButton).getWindow();
		Mockito.doReturn(this.parentWindow).when(mockWindow).getParent();
		return event;
	}

	private void initializeOptionGroup() {
		this.optionGroup = new OptionGroup();
		this.optionGroup.addItem(AddEntryDialog.OPTION_1_ID);
		this.optionGroup.addItem(AddEntryDialog.OPTION_2_ID);
		this.optionGroup.addItem(AddEntryDialog.OPTION_3_ID);
		this.addEntryDialog.setOptionGroup(this.optionGroup);
	}
}
