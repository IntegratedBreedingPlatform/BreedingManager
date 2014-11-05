package org.generationcp.breeding.manager.listmanager.listeners.test;

import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListTreeExpandListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.TooLittleActualInvocations;

import com.vaadin.ui.Tree.ExpandEvent;

public class GermplasmListTreeExpandListenerTest {

	private static final String LOCAL_ITEM = "LOCAL";
	private static final String CENTRAL_ITEM = "CENTRAL";
	private static GermplasmListTreeExpandListener listener;
	private static ListSelectorComponent listSelectorComponent;
	private static ExpandEvent event;

	@Before
	public void setUp() {
		GermplasmListTreeExpandListenerTest.listSelectorComponent = Mockito
				.mock(ListSelectorComponent.class);
		GermplasmListTreeExpandListenerTest.listener = new GermplasmListTreeExpandListener(
				GermplasmListTreeExpandListenerTest.listSelectorComponent);
		GermplasmListTreeExpandListenerTest.event = Mockito.mock(ExpandEvent.class);
	}

	@Test
	public void testIfAddRenameItemOptionIsHiddenAfterClickingExpandArrowForCentral() {
		setUpCurrentItem(CENTRAL_ITEM);
		triggerExpand();
		assertIfToggleFolderSectionForItemSelectedIsCalled();
	}
	
	@Test
	public void testIfAddRenameItemOptionIsHiddenAfterClickingExpandArrowForLocal() {
		setUpCurrentItem(LOCAL_ITEM);
		triggerExpand();
		assertIfToggleFolderSectionForItemSelectedIsCalled();
	}
	
	private void assertIfToggleFolderSectionForItemSelectedIsCalled() {
		try {
			Mockito.verify(GermplasmListTreeExpandListenerTest.listSelectorComponent,
					Mockito.times(1)).toggleFolderSectionForItemSelected();
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expected that the toggleFolderSectionForItemSelected() is called once after invoking nodeExpand but didn't.");
		}
	}

	private void triggerExpand() {
		GermplasmListTreeExpandListenerTest.listener
		.nodeExpand(GermplasmListTreeExpandListenerTest.event);		
	}

	private void setUpCurrentItem(String item){
		Mockito.when(GermplasmListTreeExpandListenerTest.event.getItemId()).thenReturn(item);
	}

}
