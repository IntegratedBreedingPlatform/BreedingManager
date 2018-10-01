
package org.generationcp.breeding.manager.listmanager;

import org.generationcp.middleware.constant.ColumnLabels;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Table;

public class ListComponentFillwithSourceTest {

	private static final String GID_PROPERTY_ID = "GID_BUTTON";

	@Mock
	private ListComponent listComponent;

	@Mock
	private Table targetTable;

	private ListComponentFillWithSource fillColumnSource;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.fillColumnSource =
				new ListComponentFillWithSource(this.listComponent, this.targetTable, ListComponentFillwithSourceTest.GID_PROPERTY_ID);
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
