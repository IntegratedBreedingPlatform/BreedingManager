
package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Window;

public class AddEntryDialogTest {

	private AddEntryDialogSource dialogSource;
	private AddEntryDialog addEntryDialog;
	private Window parentWindow;
	private BreedingManagerService breedingManagerService;

	@Before
	public void setUp() throws Exception {
		this.dialogSource = Mockito.mock(AddEntryDialogSource.class);
		this.parentWindow = new Window();
		this.addEntryDialog = new AddEntryDialog(this.dialogSource, this.parentWindow);
		this.breedingManagerService = Mockito.mock(BreedingManagerService.class);

		Project testProject = new Project();
		testProject.setUniqueID(UUID.randomUUID().toString());

		Mockito.when(this.breedingManagerService.getCurrentProject()).thenReturn(testProject);
		this.addEntryDialog.setBreedingManagerService(this.breedingManagerService);
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

	@Test
	public void testAddEntryOption1() {
		AddEntryDialog dialog = Mockito.spy(this.addEntryDialog);
		OptionGroup optionGroup = new OptionGroup();
		optionGroup.addItem(AddEntryDialog.OPTION_1_ID);
		optionGroup.select(AddEntryDialog.OPTION_1_ID);
		dialog.setOptionGroup(optionGroup);

		List<Integer> selectedGids = Collections.singletonList(1);
		dialog.setSelectedGids(selectedGids);

		ClickEvent event = Mockito.mock(ClickEvent.class);
		Button mockButton = Mockito.mock(Button.class);
		Window window = Mockito.mock(Window.class);
		Mockito.doReturn(mockButton).when(event).getButton();
		Mockito.doReturn(window).when(mockButton).getWindow();
		Mockito.doReturn(this.parentWindow).when(window).getParent();

		dialog.nextButtonClickAction(event);
		Mockito.verify(this.dialogSource).finishAddingEntry(selectedGids);
	}
}
