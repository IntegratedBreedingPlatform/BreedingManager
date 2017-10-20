package org.generationcp.breeding.manager.listmanager;

import com.vaadin.ui.Table;
import org.generationcp.middleware.constant.ColumnLabels;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Created by cyrus on 04/04/2016.
 */
public class ListDataTableFieldFactoryTest {

	ListComponent.ListDataTableFieldFactory listDataTableFieldFactoryToTest;

	@Before
	public void setUp() throws Exception {
		/*	Since ListDataTableFieldFactory is an inner class for ListComponent
		 *  there is no straight forward way to instantiate the class without re-working/refactoring ListComponent
		 *  so we retrieve it round-about via Argument captor
		 */

		// create a listComponent instance, and a mock of table
		ListComponent listComponent = new ListComponent();
		final Table mockListDataTable = Mockito.mock(Table.class);
		listComponent.setListDataTable(mockListDataTable);

		// this function call creates the actual instance of ListDataTableFieldFactory, so we need to capture it
		listComponent.makeTableEditable();

		ArgumentCaptor<ListComponent.ListDataTableFieldFactory> listDataTableFieldFactoryCaptor = ArgumentCaptor.forClass(ListComponent.ListDataTableFieldFactory.class);
		Mockito.verify(mockListDataTable).setTableFieldFactory(listDataTableFieldFactoryCaptor.capture());

		listDataTableFieldFactoryToTest = listDataTableFieldFactoryCaptor.getValue();

		// we're just making sure that the captured instance is not null
		Assert.assertNotNull(listDataTableFieldFactoryToTest);

	}

	@Test
	public void testIsNonEditableColumn() throws Exception {
		// Make sure the following columns are non editable
	    Assert.assertTrue(ColumnLabels.GID.getName() + " should be non editable",listDataTableFieldFactoryToTest.isNonEditableColumn(ColumnLabels.GID.getName()));
	    Assert.assertTrue(ColumnLabels.ENTRY_ID.getName() + " should be non editable",listDataTableFieldFactoryToTest.isNonEditableColumn(ColumnLabels.ENTRY_ID.getName()));
	    Assert.assertTrue(ColumnLabels.DESIGNATION.getName() + " should be non editable",listDataTableFieldFactoryToTest.isNonEditableColumn(ColumnLabels.DESIGNATION.getName()));
	    Assert.assertTrue(ColumnLabels.GROUP_ID.getName() + " should be non editable",listDataTableFieldFactoryToTest.isNonEditableColumn(ColumnLabels.GROUP_ID.getName()));
	}

	@Test
	public void testIsNonEditableColumnGivenEditableColumn() throws Exception {
		// isNonEditableColumn should return false if we pass an editable column

		// Make sure the following columns are non editable
		Assert.assertFalse(ColumnLabels.SEED_SOURCE.getName() + " should be non editable",listDataTableFieldFactoryToTest.isNonEditableColumn(ColumnLabels.SEED_SOURCE.getName()));
	}

}
