
package org.generationcp.breedingmanager.customcomponent.listinventory;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.listinventory.ListInventoryTable;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Table;

public class ListInventoryTableTest {

	private static final int LIST_ID = 1;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@InjectMocks
	private final ListInventoryTable listInventoryTable = new ListInventoryTable(ListInventoryTableTest.LIST_ID);

	@Before
	public void setUp() throws MiddlewareQueryException {

		MockitoAnnotations.initMocks(this);

		Mockito.doReturn("CHECK").when(this.messageSource).getMessage(Message.CHECK_ICON);
		Mockito.doReturn("#").when(this.messageSource).getMessage(Message.HASHTAG);

	}

	@Test
	public void testInstantiateComponentsHeaderNameFromOntology() throws MiddlewareQueryException {

		Mockito.doReturn(this.createTerm("DESIGNATION")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.DESIGNATION.getTermId().getId());
		Mockito.doReturn(this.createTerm("LOCATION")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.LOT_LOCATION.getTermId().getId());
		Mockito.doReturn(this.createTerm("UNITS")).when(this.ontologyDataManager).getTermById(ColumnLabels.UNITS.getTermId().getId());
		Mockito.doReturn(this.createTerm("AVAIL_INV")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.AVAILABLE_INVENTORY.getTermId().getId());
		Mockito.doReturn(this.createTerm("TOTAL")).when(this.ontologyDataManager).getTermById(ColumnLabels.TOTAL.getTermId().getId());
		Mockito.doReturn(this.createTerm("RES")).when(this.ontologyDataManager).getTermById(ColumnLabels.RESERVED.getTermId().getId());
		Mockito.doReturn(this.createTerm("NEW RES")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.NEWLY_RESERVED.getTermId().getId());
		Mockito.doReturn(this.createTerm("COMMENT")).when(this.ontologyDataManager).getTermById(ColumnLabels.COMMENT.getTermId().getId());
		Mockito.doReturn(this.createTerm("STOCKID")).when(this.ontologyDataManager).getTermById(ColumnLabels.STOCKID.getTermId().getId());
		Mockito.doReturn(this.createTerm("LOT_ID")).when(this.ontologyDataManager).getTermById(ColumnLabels.LOT_ID.getTermId().getId());

		this.listInventoryTable.instantiateComponents();

		Table table = this.listInventoryTable.getTable();
		Assert.assertEquals("CHECK", table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals("#", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("LOCATION", table.getColumnHeader(ColumnLabels.LOT_LOCATION.getName()));
		Assert.assertEquals("UNITS", table.getColumnHeader(ColumnLabels.UNITS.getName()));
		Assert.assertEquals("AVAIL_INV", table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals("TOTAL", table.getColumnHeader(ColumnLabels.TOTAL.getName()));
		Assert.assertEquals("RES", table.getColumnHeader(ColumnLabels.RESERVED.getName()));
		Assert.assertEquals("NEW RES", table.getColumnHeader(ColumnLabels.NEWLY_RESERVED.getName()));
		Assert.assertEquals("COMMENT", table.getColumnHeader(ColumnLabels.COMMENT.getName()));
		Assert.assertEquals("STOCKID", table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		Assert.assertEquals("LOT_ID", table.getColumnHeader(ColumnLabels.LOT_ID.getName()));
	}

	@Test
	public void testInstantiateComponentsHeaderNameDoesntExistFromOntology() throws MiddlewareQueryException {

		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.DESIGNATION.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.LOT_LOCATION.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.UNITS.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.AVAILABLE_INVENTORY.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.TOTAL.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.RESERVED.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.NEWLY_RESERVED.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.COMMENT.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.LOT_ID.getTermId().getId());

		this.listInventoryTable.instantiateComponents();

		Table table = this.listInventoryTable.getTable();
		Assert.assertEquals("CHECK", table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals("#", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals(ColumnLabels.DESIGNATION.getName(), table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals(ColumnLabels.LOT_LOCATION.getName(), table.getColumnHeader(ColumnLabels.LOT_LOCATION.getName()));
		Assert.assertEquals(ColumnLabels.UNITS.getName(), table.getColumnHeader(ColumnLabels.UNITS.getName()));
		Assert.assertEquals(ColumnLabels.AVAILABLE_INVENTORY.getName(), table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals(ColumnLabels.TOTAL.getName(), table.getColumnHeader(ColumnLabels.TOTAL.getName()));
		Assert.assertEquals(ColumnLabels.RESERVED.getName(), table.getColumnHeader(ColumnLabels.RESERVED.getName()));
		Assert.assertEquals(ColumnLabels.NEWLY_RESERVED.getName(), table.getColumnHeader(ColumnLabels.NEWLY_RESERVED.getName()));
		Assert.assertEquals(ColumnLabels.COMMENT.getName(), table.getColumnHeader(ColumnLabels.COMMENT.getName()));
		Assert.assertEquals(ColumnLabels.LOT_ID.getName(), table.getColumnHeader(ColumnLabels.LOT_ID.getName()));
	}

	private Term createTerm(String name) {
		Term term = new Term();
		term.setName(name);
		term.setId(0);
		return term;
	}

}
