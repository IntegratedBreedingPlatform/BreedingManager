package org.generationcp.breedingmanager.customcomponent.listinventory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.listinventory.ListInventoryTable;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.vaadin.ui.Table;


public class ListInventoryTableTest {

	private static final int LIST_ID = 1;

	@Mock
	private OntologyDataManager ontologyDataManager;
	
	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	
	@InjectMocks
	private ListInventoryTable listInventoryTable = new ListInventoryTable(LIST_ID);
	
	@Before
	public void setUp() throws MiddlewareQueryException{
		
		MockitoAnnotations.initMocks(this);
		
		doReturn("CHECK").when(messageSource).getMessage(Message.CHECK_ICON);
		doReturn("#").when(messageSource).getMessage(Message.HASHTAG);
		
	}
	
	@Test
	public void testInstantiateComponentsHeaderNameFromOntology() throws MiddlewareQueryException{
		
		doReturn(createTerm("DESIGNATION")).when(ontologyDataManager).getTermById(ColumnLabels.DESIGNATION.getTermId().getId());
		doReturn(createTerm("LOCATION")).when(ontologyDataManager).getTermById(ColumnLabels.LOT_LOCATION.getTermId().getId());
		doReturn(createTerm("SCALE")).when(ontologyDataManager).getTermById(ColumnLabels.SCALE.getTermId().getId());
		doReturn(createTerm("AVAIL_INV")).when(ontologyDataManager).getTermById(ColumnLabels.AVAILABLE_INVENTORY.getTermId().getId());
		doReturn(createTerm("TOTAL")).when(ontologyDataManager).getTermById(ColumnLabels.TOTAL.getTermId().getId());
		doReturn(createTerm("RES")).when(ontologyDataManager).getTermById(ColumnLabels.RESERVED.getTermId().getId());
		doReturn(createTerm("NEW RES")).when(ontologyDataManager).getTermById(ColumnLabels.NEWLY_RESERVED.getTermId().getId());
		doReturn(createTerm("COMMENT")).when(ontologyDataManager).getTermById(ColumnLabels.COMMENT.getTermId().getId());
		doReturn(createTerm("LOT_ID")).when(ontologyDataManager).getTermById(ColumnLabels.LOT_ID.getTermId().getId());
		
		listInventoryTable.instantiateComponents();
		
		Table table = listInventoryTable.getTable();
		assertEquals("CHECK", table.getColumnHeader(ColumnLabels.TAG.getName()));
		assertEquals("#", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		assertEquals("LOCATION", table.getColumnHeader(ColumnLabels.LOT_LOCATION.getName()));
		assertEquals("SCALE", table.getColumnHeader(ColumnLabels.SCALE.getName()));
		assertEquals("AVAIL_INV", table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		assertEquals("TOTAL", table.getColumnHeader(ColumnLabels.TOTAL.getName()));
		assertEquals("RES", table.getColumnHeader(ColumnLabels.RESERVED.getName()));
		assertEquals("NEW RES", table.getColumnHeader(ColumnLabels.NEWLY_RESERVED.getName()));
		assertEquals("COMMENT", table.getColumnHeader(ColumnLabels.COMMENT.getName()));
		assertEquals("LOT_ID", table.getColumnHeader(ColumnLabels.LOT_ID.getName()));
	}
	
	@Test
	public void testInstantiateComponentsHeaderNameDoesntExistFromOntology() throws MiddlewareQueryException{
		
		doReturn(null).when(ontologyDataManager).getTermById(ColumnLabels.DESIGNATION.getTermId().getId());
		doReturn(null).when(ontologyDataManager).getTermById(ColumnLabels.LOT_LOCATION.getTermId().getId());
		doReturn(null).when(ontologyDataManager).getTermById(ColumnLabels.SCALE.getTermId().getId());
		doReturn(null).when(ontologyDataManager).getTermById(ColumnLabels.AVAILABLE_INVENTORY.getTermId().getId());
		doReturn(null).when(ontologyDataManager).getTermById(ColumnLabels.TOTAL.getTermId().getId());
		doReturn(null).when(ontologyDataManager).getTermById(ColumnLabels.RESERVED.getTermId().getId());
		doReturn(null).when(ontologyDataManager).getTermById(ColumnLabels.NEWLY_RESERVED.getTermId().getId());
		doReturn(null).when(ontologyDataManager).getTermById(ColumnLabels.COMMENT.getTermId().getId());
		doReturn(null).when(ontologyDataManager).getTermById(ColumnLabels.LOT_ID.getTermId().getId());
		
		listInventoryTable.instantiateComponents();
		
		Table table = listInventoryTable.getTable();
		assertEquals("CHECK", table.getColumnHeader(ColumnLabels.TAG.getName()));
		assertEquals("#", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		assertEquals(ColumnLabels.DESIGNATION.getName(), table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		assertEquals(ColumnLabels.LOT_LOCATION.getName(), table.getColumnHeader(ColumnLabels.LOT_LOCATION.getName()));
		assertEquals(ColumnLabels.SCALE.getName(), table.getColumnHeader(ColumnLabels.SCALE.getName()));
		assertEquals(ColumnLabels.AVAILABLE_INVENTORY.getName(), table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		assertEquals(ColumnLabels.TOTAL.getName(), table.getColumnHeader(ColumnLabels.TOTAL.getName()));
		assertEquals(ColumnLabels.RESERVED.getName(), table.getColumnHeader(ColumnLabels.RESERVED.getName()));
		assertEquals(ColumnLabels.NEWLY_RESERVED.getName(), table.getColumnHeader(ColumnLabels.NEWLY_RESERVED.getName()));
		assertEquals(ColumnLabels.COMMENT.getName(), table.getColumnHeader(ColumnLabels.COMMENT.getName()));
		assertEquals(ColumnLabels.LOT_ID.getName(), table.getColumnHeader(ColumnLabels.LOT_ID.getName()));
	}
	
	private Term createTerm(String name){
		Term term = new Term();
		term.setName(name);
		term.setId(0);
		return(term);
	}
	
}
