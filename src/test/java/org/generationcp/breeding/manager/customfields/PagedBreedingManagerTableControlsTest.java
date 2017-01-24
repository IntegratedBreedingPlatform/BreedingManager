
package org.generationcp.breeding.manager.customfields;

import java.util.Iterator;

import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable.EntrySelectSyncHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import junit.framework.Assert;

public class PagedBreedingManagerTableControlsTest {

	private static final int INDEX_OF_PAGE_SIZE_COMBOBOX = 1;

	private static final int PAGE_LENGTH = 20;

	private PagedBreedingManagerTableControls pagedTableControls;

	private PagedBreedingManagerTable pagedBreedingManagerTable;

	@Mock
	private EntrySelectSyncHandler handler;

	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		
		this.pagedBreedingManagerTable = new PagedBreedingManagerTable(PAGE_LENGTH, PAGE_LENGTH);
		this.pagedBreedingManagerTable.setImmediate(true);
		
		this.pagedTableControls = new PagedBreedingManagerTableControls(this.pagedBreedingManagerTable, this.handler);

	}
	
	private void addItems(final int numberOfItems){
		int i = 1;
		while (i <= numberOfItems) {
			this.pagedBreedingManagerTable.addItem(i);
			i++;
		}
	}
	
	
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
	
	@Test
	public void testPagingControlsStateWhenOnePageOnly() {
		this.addItems(5);
		
		//Hack to fire page change event so that paging buttons will be updated
		this.pagedBreedingManagerTable.previousPage();
		
		Iterator<Component> pagingControlsIterator = ((HorizontalLayout) this.pagedTableControls).getComponentIterator();
		// First Component: Page Size
		final HorizontalLayout pageSize = (HorizontalLayout) pagingControlsIterator.next();
		Assert.assertNotNull("The page size should be displayed", pageSize);
		ComboBox pageSizeComboBox = (ComboBox) pageSize.getComponent(INDEX_OF_PAGE_SIZE_COMBOBOX);
		Assert.assertEquals(String.valueOf(PAGE_LENGTH), pageSizeComboBox.getValue().toString());
		
		
		// Second Component: Page Navigation
		final HorizontalLayout pageNavigationLayout = (HorizontalLayout) pagingControlsIterator.next();
		Assert.assertNotNull("The page navigation controls should be displayed", pageNavigationLayout);
		final Iterator<Component> pageNavigationIterator = pageNavigationLayout.getComponentIterator();
		int numberOfButtons = 0;
		while (pageNavigationIterator.hasNext()) {
			final Component component = pageNavigationIterator.next();
			// verify that all buttons are disabled since we only have 1 page
			if (component instanceof Button) {
				numberOfButtons++;
				final Button button = (Button) component;
				Assert.assertFalse("The button should be disabled because there is only 1 page", button.isEnabled());
			}
		}
		Assert.assertEquals("There should be 4 buttons displayed for first, previous, next and last", 4, numberOfButtons);
	}

	@Test
	public void testPagingControlsStateWhenMoreThanOnePage() {
		// Table will span 3 pages
		this.addItems(30);
		
		//Hack to fire page change event so that paging buttons will be updated
		this.pagedBreedingManagerTable.previousPage();
		
		Iterator<Component> pagingControlsIterator = ((HorizontalLayout) this.pagedTableControls).getComponentIterator();
		// First Component: Page Size
		final HorizontalLayout pageSize = (HorizontalLayout) pagingControlsIterator.next();
		Assert.assertNotNull("The page size should be displayed", pageSize);
		ComboBox pageSizeComboBox = (ComboBox) pageSize.getComponent(INDEX_OF_PAGE_SIZE_COMBOBOX);
		Assert.assertEquals(String.valueOf(PAGE_LENGTH), pageSizeComboBox.getValue());
		
		// // Second Component: Page Navigation
		final HorizontalLayout pageNavigationLayout = (HorizontalLayout) pagingControlsIterator.next();
		Assert.assertNotNull("The page navigation controls should be displayed", pageNavigationLayout);
		final Iterator<Component> pageNavigationIterator = pageNavigationLayout.getComponentIterator();
		int numberOfButtons = 0;
		while (pageNavigationIterator.hasNext()) {
			final Component component = pageNavigationIterator.next();
			// verify that the first and previous buttons are disabled while the next and last buttons are enabled
			if (component instanceof Button) {
				numberOfButtons++;
				final Button button = (Button) component;
				// first and previous button
				if (numberOfButtons <= 2) {
					Assert.assertFalse("The button should be disabled because the current page is 1", button.isEnabled());
				} else {
					Assert.assertTrue("The button should be enabled because there are more than 1 page", button.isEnabled());
				}

			}
		}
		Assert.assertEquals("There should be 4 buttons displayed for first, previous, next and last", 4, numberOfButtons);
	}
}
