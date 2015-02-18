package org.generationcp.breeding.manager.listimport;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Table;

public class SpecifyGermplasmDetailsComponentTest {
	
	private SpecifyGermplasmDetailsComponent specifyGermplasmDetailsComponent;
	
	@Mock
	private GermplasmImportMain source;
	@Mock
	private OntologyDataManager ontologyDataManager;
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		specifyGermplasmDetailsComponent = spy(new SpecifyGermplasmDetailsComponent(source, false));
		specifyGermplasmDetailsComponent.setOntologyDataManager(ontologyDataManager);
	}
	
	@Test
	public void testInitGermplasmDetailsTable_returnsTheValueFromColumLabelDefaultName(){
		Table table = new Table();
		specifyGermplasmDetailsComponent.setGermplasmDetailsTable(table);
		when(specifyGermplasmDetailsComponent.getGermplasmDetailsTable()).thenReturn(table);
		
		specifyGermplasmDetailsComponent.initGermplasmDetailsTable();
		
		assertEquals("ENTRY_ID", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		assertEquals("ENTRY CODE", table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		assertEquals("PARENTAGE", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		assertEquals("GID", table.getColumnHeader(ColumnLabels.GID.getName()));
		assertEquals("SEED SOURCE", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}
	
	@Test
	public void testInitGermplasmDetailsTable_returnsTheValueFromOntologyManager() throws MiddlewareQueryException{
		Table table = new Table();
		specifyGermplasmDetailsComponent.setGermplasmDetailsTable(table);
		when(specifyGermplasmDetailsComponent.getGermplasmDetailsTable()).thenReturn(table);
		
		Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		when(ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		
		specifyGermplasmDetailsComponent.initGermplasmDetailsTable();
		
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.GID.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}
}
