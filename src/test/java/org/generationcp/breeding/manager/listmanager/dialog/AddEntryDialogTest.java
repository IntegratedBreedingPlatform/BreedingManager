package org.generationcp.breeding.manager.listmanager.dialog;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Window;

public class AddEntryDialogTest {

	private AddEntryDialogSource dialogSource;
	private AddEntryDialog addEntryDialog;
	private Window parentWindow;
	
	@Before
	public void setUp() throws Exception{
		dialogSource = mock(AddEntryDialogSource.class);
		parentWindow = new Window();
		addEntryDialog = new AddEntryDialog(dialogSource, parentWindow);
	}
	
	
	@Test
	public void testValidateListenerCount() throws Exception{
		AddEntryDialog dialog = spy(addEntryDialog);
		dialog.setMessageSource(mock(SimpleResourceBundleMessageSource.class));
		
		doNothing().when(dialog).initializeTopPart();
		doNothing().when(dialog).initializeBottomPart();
		doNothing().when(dialog).initializeValues();
		doNothing().when(dialog).addSearchResultsListeners();
		doNothing().when(dialog).addListenerToOptionGroup();
		doNothing().when(dialog).layoutComponents();
		
		dialog.afterPropertiesSet();
		
		Collection<?> listeners = dialog.getDoneButton().getListeners(Button.ClickEvent.class);
		Assert.assertTrue("Done button has only 1 listener", listeners.size() == 1);
		
		listeners = dialog.getCancelButton().getListeners(Button.ClickEvent.class);
		Assert.assertTrue("Cancel button has only 1 listener", listeners.size() == 1);
		
		
	}

	@Test
	public void testAddEntryOption1() {
		AddEntryDialog dialog = spy(addEntryDialog);
		OptionGroup optionGroup = new OptionGroup();
		optionGroup.addItem(AddEntryDialog.OPTION_1_ID);
		optionGroup.select(AddEntryDialog.OPTION_1_ID);
		dialog.setOptionGroup(optionGroup);
		
		List<Integer> selectedGids = Collections.singletonList(1);
		dialog.setSelectedGids(selectedGids);
		
		ClickEvent event = mock(ClickEvent.class);
		Button mockButton = mock(Button.class);
		Window window = mock(Window.class);
		doReturn(mockButton).when(event).getButton();
		doReturn(window).when(mockButton).getWindow();
		doReturn(parentWindow).when(window).getParent();
		
		dialog.nextButtonClickAction(event);
		verify(dialogSource).finishAddingEntry(selectedGids);
	}
}
