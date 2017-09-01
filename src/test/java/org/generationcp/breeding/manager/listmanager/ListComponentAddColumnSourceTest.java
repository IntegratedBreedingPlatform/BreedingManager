
package org.generationcp.breeding.manager.listmanager;

import java.util.Arrays;
import java.util.List;

import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

import junit.framework.Assert;

public class ListComponentAddColumnSourceTest {

	private static final List<Integer> ITEMS_LIST = Arrays.asList(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);

	private static final List<Integer> GID_LIST = Arrays.asList(101, 102, 103, 104, 105, 106, 107, 108, 109, 110);

	private static final String GID_PROPERTY_ID = "GID_BUTTON";

	private static final String LISTDATA_PROPERTY_ID = "LISTDATA_ID";

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private ListTabComponent listTabComponent;

	@Mock
	private ListComponent listComponent;

	@Mock
	private Window window;

	@Mock
	private Table targetTable;

	private ListComponentAddColumnSource addColumnSource;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.addColumnSource =
				new ListComponentAddColumnSource(this.listTabComponent, this.targetTable, ListComponentAddColumnSourceTest.GID_PROPERTY_ID);
		this.addColumnSource.setOntologyDataManager(this.ontologyDataManager);

		Mockito.doReturn(ListComponentAddColumnSourceTest.ITEMS_LIST).when(this.targetTable).getItemIds();
		Mockito.doReturn(
				Arrays.asList(ListComponentAddColumnSourceTest.LISTDATA_PROPERTY_ID, ListComponentAddColumnSourceTest.GID_PROPERTY_ID))
				.when(this.targetTable).getContainerPropertyIds();
		for (int i = 0; i < ListComponentAddColumnSourceTest.ITEMS_LIST.size(); i++) {
			final Integer itemId = ListComponentAddColumnSourceTest.ITEMS_LIST.get(i);
			final Item item = new PropertysetItem();
			item.addItemProperty(ListComponentAddColumnSourceTest.LISTDATA_PROPERTY_ID, new ObjectProperty<Integer>(itemId));
			// Create a Button for GID property
			item.addItemProperty(ListComponentAddColumnSourceTest.GID_PROPERTY_ID,
					new ObjectProperty<Button>(new Button(ListComponentAddColumnSourceTest.GID_LIST.get(i).toString())));
			Mockito.doReturn(item).when(this.targetTable).getItem(itemId);
		}
		Mockito.doReturn(this.listComponent).when(this.listTabComponent).getListComponent();
		Mockito.doReturn(this.window).when(this.targetTable).getWindow();
	}

	@Test
	public void testGetItemIdsToProcess() {
		Assert.assertEquals(ListComponentAddColumnSourceTest.ITEMS_LIST, this.addColumnSource.getItemIdsToProcess());
	}

	@Test
	public void testGetGidForItemId() {
		for (int i = 0; i < ListComponentAddColumnSourceTest.ITEMS_LIST.size(); i++) {
			final Integer itemId = ListComponentAddColumnSourceTest.ITEMS_LIST.get(i);
			Assert.assertEquals(ListComponentAddColumnSourceTest.GID_LIST.get(i), this.addColumnSource.getGidForItemId(itemId));
		}
	}

	@Test
	public void testGetGidsToProcess() {
		Assert.assertEquals(ListComponentAddColumnSourceTest.GID_LIST, this.addColumnSource.getGidsToProcess());
	}

	@Test
	public void testColumnExists() {
		Assert.assertTrue(this.addColumnSource.columnExists(ListComponentAddColumnSourceTest.GID_PROPERTY_ID));
		Assert.assertFalse(this.addColumnSource.columnExists(ColumnLabels.PREFERRED_NAME.getName()));
	}

	@Test
	public void testAddColumnLabel() {
		final ColumnLabels columnLabel = ColumnLabels.PREFERRED_ID;
		this.addColumnSource.addColumn(columnLabel.getName());

		Mockito.verify(this.targetTable).addContainerProperty(columnLabel.getName(), String.class, "");
		Mockito.verify(this.targetTable).setColumnHeader(columnLabel.getName(),
				columnLabel.getTermNameFromOntology(this.ontologyDataManager));
	}

	@Test
	public void testAddColumn() {
		final String columnName = "New Attribute Type";
		this.addColumnSource.addColumn(columnName);

		Mockito.verify(this.targetTable).addContainerProperty(columnName.toUpperCase(), String.class, "");
		Mockito.verify(this.targetTable).setColumnHeader(columnName.toUpperCase(), columnName);
	}

	@Test
	public void testPropagateUIChangesForEditableTable() {
		Mockito.doReturn(true).when(this.targetTable).isEditable();
		this.addColumnSource.propagateUIChanges();

		Mockito.verify(this.listComponent).setHasUnsavedChanges(true);
		Mockito.verify(this.targetTable).setEditable(false);
		Mockito.verify(this.targetTable).setEditable(true);
	}

	@Test
	public void testPropagateUIChangesForNonEditableTable() {
		Mockito.doReturn(false).when(this.targetTable).isEditable();
		this.addColumnSource.propagateUIChanges();

		Mockito.verify(this.listComponent).setHasUnsavedChanges(true);
		Mockito.verify(this.targetTable, Mockito.never()).setEditable(Matchers.anyBoolean());
	}

	@Test
	public void testGetWindow() {
		Assert.assertEquals(this.window, this.addColumnSource.getWindow());
	}

	@Test
	public void testGetColumnsToExclude() {
		Assert.assertEquals(Arrays.asList(FillWithOption.FILL_WITH_ATTRIBUTE), this.addColumnSource.getColumnsToExclude());
	}

}
