
package org.generationcp.breeding.manager.listmanager;

import org.generationcp.commons.constant.ColumnLabels;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Table;

public class ListBuilderFillWithSourceTest {

	private static final String GID_PROPERTY_ID = "GID_BUTTON";

	@Mock
	private ListBuilderComponent listBuilderComponent;

	@Mock
	private Table targetTable;

	private ListBuilderFillWithSource fillColumnSource;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.fillColumnSource =
				new ListBuilderFillWithSource(this.listBuilderComponent, this.targetTable, ListBuilderFillWithSourceTest.GID_PROPERTY_ID);
	}

	@Test
	public void testAddColumnLabel() {
		this.fillColumnSource.addColumn(ColumnLabels.GERMPLASM_LOCATION.getName());
		Mockito.verifyZeroInteractions(this.targetTable);
	}

	@Test
	public void testAddColumn() {
		this.fillColumnSource.addColumn("Attribute Type");
		Mockito.verifyZeroInteractions(this.targetTable);
	}

}
