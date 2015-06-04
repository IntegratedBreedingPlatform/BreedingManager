
package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Table;

public class ListBuilderComponentTest {

	private static final String SEED_RES = "SEED_RES";
	private static final String AVAIL_INV = "AVAIL_INV";
	private static final String HASH = "#";
	private static final String CHECK = "CHECK";
	private static final String SEED_SOURCE = "SEED_SOURCE";
	private static final String CROSS = "CROSS";
	private static final String DESIG = "DESIG";
	private static final String ENTRY_CODE = "ENTRY_CODE";
	private static final String GID = "GID";
	private static final String STOCKID = "STOCKID";

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	private ListBuilderComponent listBuilderComponent;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		this.listBuilderComponent = Mockito.spy(new ListBuilderComponent());
		this.listBuilderComponent.setMessageSource(this.messageSource);

		Mockito.doReturn(ListBuilderComponentTest.CHECK).when(this.messageSource).getMessage(Message.CHECK_ICON);
		Mockito.doReturn(ListBuilderComponentTest.HASH).when(this.messageSource).getMessage(Message.HASHTAG);
		Mockito.doReturn(ListBuilderComponentTest.AVAIL_INV).when(this.listBuilderComponent)
				.getTermNameFromOntology(ColumnLabels.AVAILABLE_INVENTORY);
		Mockito.doReturn(ListBuilderComponentTest.SEED_RES).when(this.listBuilderComponent)
				.getTermNameFromOntology(ColumnLabels.SEED_RESERVATION);
		Mockito.doReturn(ListBuilderComponentTest.GID).when(this.listBuilderComponent).getTermNameFromOntology(ColumnLabels.GID);
		Mockito.doReturn(ListBuilderComponentTest.ENTRY_CODE).when(this.listBuilderComponent)
				.getTermNameFromOntology(ColumnLabels.ENTRY_CODE);
		Mockito.doReturn(ListBuilderComponentTest.DESIG).when(this.listBuilderComponent).getTermNameFromOntology(ColumnLabels.DESIGNATION);
		Mockito.doReturn(ListBuilderComponentTest.CROSS).when(this.listBuilderComponent).getTermNameFromOntology(ColumnLabels.PARENTAGE);
		Mockito.doReturn(ListBuilderComponentTest.SEED_SOURCE).when(this.listBuilderComponent)
				.getTermNameFromOntology(ColumnLabels.SEED_SOURCE);
		Mockito.doReturn(ListBuilderComponentTest.STOCKID).when(this.listBuilderComponent).getTermNameFromOntology(ColumnLabels.STOCKID);

	}

	@Test
	public void testAddBasicTableColumns() {

		Table table = new Table();
		this.listBuilderComponent.addBasicTableColumns(table);

		Assert.assertEquals(ListBuilderComponentTest.CHECK, table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals(ListBuilderComponentTest.HASH, table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals(ListBuilderComponentTest.AVAIL_INV, table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals(ListBuilderComponentTest.SEED_RES, table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
		Assert.assertEquals(ListBuilderComponentTest.GID, table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals(ListBuilderComponentTest.ENTRY_CODE, table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals(ListBuilderComponentTest.DESIG, table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals(ListBuilderComponentTest.CROSS, table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals(ListBuilderComponentTest.SEED_SOURCE, table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
		Assert.assertEquals(ListBuilderComponentTest.STOCKID, table.getColumnHeader(ColumnLabels.STOCKID.getName()));

	}

}
