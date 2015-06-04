
package org.generationcp.breeding.manager.listmanager.listeners.test;

import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListTreeCollapseListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.TooLittleActualInvocations;

import com.vaadin.ui.Tree.CollapseEvent;

public class GermplasmListTreeCollapseListenerTest {

	private static final String LOCAL_ITEM = "LOCAL";
	private static final String CENTRAL_ITEM = "CENTRAL";
	private static GermplasmListTreeCollapseListener listener;
	private static ListSelectorComponent listSelectorComponent;
	private static CollapseEvent event;

	@Before
	public void setUp() {
		GermplasmListTreeCollapseListenerTest.listSelectorComponent = Mockito.mock(ListSelectorComponent.class);
		GermplasmListTreeCollapseListenerTest.listener =
				new GermplasmListTreeCollapseListener(GermplasmListTreeCollapseListenerTest.listSelectorComponent);
		GermplasmListTreeCollapseListenerTest.event = Mockito.mock(CollapseEvent.class);
	}

	@Test
	public void testIfAddRenameItemOptionIsHiddenAfterClickingCollapseArrowForCentral() {
		this.setUpCurrentItem(GermplasmListTreeCollapseListenerTest.CENTRAL_ITEM);
		this.triggerCollapse();
		this.assertIfToggleFolderSectionForItemSelectedIsCalled();
	}

	@Test
	public void testIfAddRenameItemOptionIsHiddenAfterClickingCollapseArrowForLocal() {
		this.setUpCurrentItem(GermplasmListTreeCollapseListenerTest.LOCAL_ITEM);
		this.triggerCollapse();
		this.assertIfToggleFolderSectionForItemSelectedIsCalled();
	}

	private void assertIfToggleFolderSectionForItemSelectedIsCalled() {
		try {
			Mockito.verify(GermplasmListTreeCollapseListenerTest.listSelectorComponent, Mockito.times(1))
					.toggleFolderSectionForItemSelected();
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expected that the toggleFolderSectionForItemSelected() is called once after invoking nodeExpand but didn't.");
		}
	}

	private void triggerCollapse() {
		GermplasmListTreeCollapseListenerTest.listener.nodeCollapse(GermplasmListTreeCollapseListenerTest.event);
	}

	private void setUpCurrentItem(String item) {
		Mockito.when(GermplasmListTreeCollapseListenerTest.event.getItemId()).thenReturn(item);
	}

}
