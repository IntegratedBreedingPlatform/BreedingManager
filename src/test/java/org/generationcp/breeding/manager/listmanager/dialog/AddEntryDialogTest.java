
package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.generationcp.breeding.manager.customcomponent.PagedTableWithSelectAllLayoutTest;
import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchResultsComponent;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

public class AddEntryDialogTest {

	@Mock
	private GermplasmSearchResultsComponent searchResultsComponent;
	
	@Mock
	private AddEntryDialogSource dialogSource;
	
	@Mock
	private BreedingManagerService breedingManagerService;

	private PagedBreedingManagerTable table;
	private Window parentWindow;
	private AddEntryDialog addEntryDialog;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		this.parentWindow = new Window();
		this.addEntryDialog = new AddEntryDialog(this.dialogSource, this.parentWindow);

		Project testProject = new Project();
		testProject.setUniqueID(UUID.randomUUID().toString());

		Mockito.when(this.breedingManagerService.getCurrentProject()).thenReturn(testProject);
		this.addEntryDialog.setBreedingManagerService(this.breedingManagerService);
		this.addEntryDialog.setSearchResultsComponent(this.searchResultsComponent);
	}

	@Test
	public void testValidateListenerCount() throws Exception {
		AddEntryDialog dialog = Mockito.spy(this.addEntryDialog);
		dialog.setMessageSource(Mockito.mock(SimpleResourceBundleMessageSource.class));

		Mockito.doNothing().when(dialog).initializeTopPart();
		Mockito.doNothing().when(dialog).initializeBottomPart();
		Mockito.doNothing().when(dialog).initializeValues();
		Mockito.doNothing().when(dialog).addSearchResultsListeners();
		Mockito.doNothing().when(dialog).addListenerToOptionGroup();
		Mockito.doNothing().when(dialog).layoutComponents();

		dialog.afterPropertiesSet();

		Collection<?> listeners = dialog.getDoneButton().getListeners(Button.ClickEvent.class);
		Assert.assertTrue("Done button has only 1 listener", listeners.size() == 1);

		listeners = dialog.getCancelButton().getListeners(Button.ClickEvent.class);
		Assert.assertTrue("Cancel button has only 1 listener", listeners.size() == 1);

	}
	
	private void initializeTable(final int numberOfItems) {
		this.table = new PagedBreedingManagerTable(1, 20);
		this.table.setSelectable(true);
		this.table.setMultiSelect(true);
		table.addContainerProperty(ColumnLabels.GID.getName() + "_REF", Integer.class, null);

		int i = 1;
		while (i <= numberOfItems) {
			table.addItem(i);
			table.getItem(i).getItemProperty(ColumnLabels.GID.getName() + "_REF").setValue(i);
			i++;
		}

		Mockito.doReturn(table).when(this.searchResultsComponent).getMatchingGermplasmTable();
	}

	@Test
	public void testAddEntryOption1() {
		AddEntryDialog dialog = Mockito.spy(this.addEntryDialog);
		OptionGroup optionGroup = new OptionGroup();
		optionGroup.addItem(AddEntryDialog.OPTION_1_ID);
		optionGroup.select(AddEntryDialog.OPTION_1_ID);
		dialog.setOptionGroup(optionGroup);
		
		// Initialize table and select first item
		List<Integer> selectedGids = Collections.singletonList(1);
		this.initializeTable(20);
		this.table.setValue(selectedGids);
		
		ClickEvent event = Mockito.mock(ClickEvent.class);
		Button mockButton = Mockito.mock(Button.class);
		Window window = Mockito.mock(Window.class);
		Mockito.doReturn(mockButton).when(event).getButton();
		Mockito.doReturn(window).when(mockButton).getWindow();
		Mockito.doReturn(this.parentWindow).when(window).getParent();

		// Method to test
		dialog.doneButtonClickAction(event);
		
		// Verify callback function for Option 1
		Mockito.verify(this.dialogSource).finishAddingEntry(selectedGids);
	}
}
