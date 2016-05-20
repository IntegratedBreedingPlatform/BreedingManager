
package org.generationcp.breeding.manager.study.generator;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;

import junit.framework.Assert;

public class DatasetCellStyleGeneratorTest {

	@Test
	public void testGetStyleIfAcceptedValue() {
		Table table = Mockito.mock(Table.class);
		Item item = Mockito.mock(Item.class);
		Property itemProperty = Mockito.mock(Property.class);
		Mockito.when(table.getItem(Matchers.anyObject())).thenReturn(item);
		Mockito.when(item.getItemProperty(Matchers.anyString())).thenReturn(itemProperty);
		Mockito.when(itemProperty.getValue()).thenReturn(true);
		DatasetCellStyleGenerator generator = new DatasetCellStyleGenerator(table);
		Assert.assertEquals("Should return accepted value since the row/col is an accepted valid value", "accepted-value",
				generator.getStyle(new Integer(1), "2"));
	}

	@Test
	public void testGetStyleIfNotAcceptedValue() {
		Table table = Mockito.mock(Table.class);
		Item item = Mockito.mock(Item.class);
		Property itemProperty = Mockito.mock(Property.class);
		Mockito.when(table.getItem(Matchers.anyObject())).thenReturn(item);
		Mockito.when(item.getItemProperty(Matchers.anyString())).thenReturn(itemProperty);
		Mockito.when(itemProperty.getValue()).thenReturn(false);
		DatasetCellStyleGenerator generator = new DatasetCellStyleGenerator(table);
		Assert.assertEquals("Should return empty value since the row/col is not an accepted valid value", "",
				generator.getStyle(new Integer(1), "2"));
	}
}
