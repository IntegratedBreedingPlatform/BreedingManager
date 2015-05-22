package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Table;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*; 

public class SelectParentsListDataComponentTest {
	
	@Mock
	private MakeCrossesParentsComponent makeCrossesParentsComponent;
    @Mock
	private SimpleResourceBundleMessageSource messageSource;
    @Mock
	private OntologyDataManager ontologyDataManager;
    
	private SelectParentsListDataComponent selectParents;
	
	private Integer germplasmListId = 1;
	private String listName = "Sample List";
	
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		selectParents = spy(new SelectParentsListDataComponent(germplasmListId, listName, makeCrossesParentsComponent));
		selectParents.setMessageSource(messageSource);
		selectParents.setOntologyDataManager(ontologyDataManager);
	}
	
	@Test
	public void testInitializeListDataTable_returnsTheValueFromColumLabelDefaultName(){
		Long count = 5L;
		selectParents.setCount(count);
		when(messageSource.getMessage(Message.CHECK_ICON)).thenReturn("TAG");
		when(messageSource.getMessage(Message.HASHTAG)).thenReturn("HASHTAG");
		
		TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();
		
		doReturn(tableWithSelectAll).when(selectParents).getListDataTableWithSelectAll();
		
		selectParents.initializeListDataTable();
		
		Table table = tableWithSelectAll.getTable();
		
		assertEquals("TAG", table.getColumnHeader(ColumnLabels.TAG.getName()));
		assertEquals("HASHTAG", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		assertEquals("AVAIL INV", table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		assertEquals("SEED RES", table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
		assertEquals("STOCKID", table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		assertEquals("GID", table.getColumnHeader(ColumnLabels.GID.getName()));
		assertEquals("ENTRY CODE", table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		assertEquals("PARENTAGE", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		assertEquals("SEED SOURCE", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}
	
	@Test
	public void testInitializeListDataTable_returnsTheValueFromOntologyManager() throws MiddlewareQueryException{
		Long count = 5L;
		selectParents.setCount(count);
		when(messageSource.getMessage(Message.CHECK_ICON)).thenReturn("TAG");
		when(messageSource.getMessage(Message.HASHTAG)).thenReturn("HASHTAG");
		
		Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		when(ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.AVAILABLE_INVENTORY.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.SEED_RESERVATION.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.STOCKID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		
		TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();
		
		doReturn(tableWithSelectAll).when(selectParents).getListDataTableWithSelectAll();
		
		selectParents.initializeListDataTable();
		
		Table table = tableWithSelectAll.getTable();
		
		assertEquals("TAG", table.getColumnHeader(ColumnLabels.TAG.getName()));
		assertEquals("HASHTAG", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.GID.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}

}
