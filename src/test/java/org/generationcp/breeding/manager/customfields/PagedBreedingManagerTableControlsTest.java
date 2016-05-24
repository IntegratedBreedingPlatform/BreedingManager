
package org.generationcp.breeding.manager.customfields;

import java.util.Iterator;

import junit.framework.Assert;

import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable.EntrySelectSyncHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

@RunWith(MockitoJUnitRunner.class)
public class PagedBreedingManagerTableControlsTest {

	@InjectMocks
	private PagedBreedingManagerTableControls pagedTableControls;

	@Mock
	private PagedBreedingManagerTable pagedBreedingManagerTable;

	@Mock
	private EntrySelectSyncHandler handler;

	@Test
	public void testComponentsAreAddedToControl() throws Exception {
		// check whether the components are added
		final Iterator<Component> componentsIterator = this.pagedTableControls.getComponentIterator();
		Assert.assertTrue("Components should be added to the control", componentsIterator.hasNext());
		int numberOfComponents = 0;
		while (componentsIterator.hasNext()) {
			final Component component = componentsIterator.next();
			Assert.assertTrue("Component should an instance of HorizontalLayout", component instanceof HorizontalLayout);
			numberOfComponents++;
		}
		Assert.assertEquals("There should only be 2 components added to the control", 2, numberOfComponents);

	}
}
