package org.generationcp.breeding.manager.listmanager;

import java.util.Arrays;
import java.util.List;

import org.generationcp.commons.constant.ColumnLabels;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Table;

import junit.framework.Assert;

public class NewGermplasmEntriesFillColumnSourceTest {
	
	private static final List<Integer> ITEMS_LIST = Arrays.asList(1, 2, 3, 4, 5);

	private static final List<Integer> GID_LIST = Arrays.asList(101, 102, 103, 104, 105);
	
	@Mock
	private Table targetTable;
	
	@InjectMocks
	private NewGermplasmEntriesFillColumnSource newEntriesSource;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}	

	@Test
	public void testPropagateUIChangesForEditableTable() {
		Mockito.doReturn(true).when(this.targetTable).isEditable();
		this.newEntriesSource.propagateUIChanges();
		
		Mockito.verify(this.targetTable).setEditable(false);
		Mockito.verify(this.targetTable).setEditable(true);
	}
	
	@Test
	public void testPropagateUIChangesForNonEditableTable() {
		Mockito.doReturn(false).when(this.targetTable).isEditable();
		this.newEntriesSource.propagateUIChanges();
		
		Mockito.verify(this.targetTable, Mockito.never()).setEditable(Matchers.anyBoolean());
	}
	
	@Test
	public void testGetGidForItemId() {
		this.newEntriesSource.setAddedItemIds(ITEMS_LIST);
		this.newEntriesSource.setAddedGids(GID_LIST);
		
		for (int i=0; i<ITEMS_LIST.size(); i++){
			final Integer gidForItemId = this.newEntriesSource.getGidForItemId(ITEMS_LIST.get(i));
			Assert.assertEquals(GID_LIST.get(i), gidForItemId);
		}
	}
	
	@Test
	public void testGetGidForItemIdWhenItemDoesntExist() {
		this.newEntriesSource.setAddedItemIds(ITEMS_LIST);
		this.newEntriesSource.setAddedGids(GID_LIST);
		Assert.assertNull(this.newEntriesSource.getGidForItemId(10));
	}
	
	@Test
	public void testSetColumnValueForItem() {
		final String column = ColumnLabels.PREFERRED_NAME.getName();
		final Item item = new PropertysetItem();
		item.addItemProperty(column, new ObjectProperty<String>(""));
		Mockito.doReturn(item).when(this.targetTable).getItem(Matchers.anyInt());
		
		final Integer itemId = ITEMS_LIST.get(0);
		final String value = "LEAFYNODE01";
		this.newEntriesSource.setColumnValueForItem(itemId, column, value);

		Assert.assertEquals(value, item.getItemProperty(column).getValue());
	}

}
