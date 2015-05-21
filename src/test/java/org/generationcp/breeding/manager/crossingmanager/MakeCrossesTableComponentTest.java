package org.generationcp.breeding.manager.crossingmanager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Table;

public class MakeCrossesTableComponentTest {
	
	private MakeCrossesTableComponent makeCrossesTableComponent;
	
	@Mock
	private CrossingManagerMakeCrossesComponent makeCrossesMain;
	@Mock
	private OntologyDataManager ontologyDataManager;
	
	@Before
	public void setUp(){

		MockitoAnnotations.initMocks(this);
		ManagerFactory.getCurrentManagerFactoryThreadLocal().set(Mockito.mock(ManagerFactory.class));

		makeCrossesTableComponent = spy(new MakeCrossesTableComponent(makeCrossesMain));
		makeCrossesTableComponent.setOntologyDataManager(ontologyDataManager);
	}
	
	@Test
	public void testInitializeCrossesMadeTable_returnsTheValueFromColumLabelDefaultName(){
		
		Table table = new Table();
		
		when(makeCrossesTableComponent.getTableCrossesMade()).thenReturn(table);
		
		makeCrossesTableComponent.initializeCrossesMadeTable();
		
		assertEquals("#", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		assertEquals("PARENTAGE", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		assertEquals("Female Parent", table.getColumnHeader(ColumnLabels.FEMALE_PARENT.getName()));
		assertEquals("Male Parent", table.getColumnHeader(ColumnLabels.MALE_PARENT.getName()));
		assertEquals("SEED SOURCE", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}
	
	@Test
	public void testInitializeCrossesMadeTable_returnsTheValueFromOntologyManager() throws MiddlewareQueryException{
		Table table = new Table();
		
		when(makeCrossesTableComponent.getTableCrossesMade()).thenReturn(table);
		
		Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		
		when(ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.FEMALE_PARENT.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.MALE_PARENT.getId())).thenReturn(fromOntology);
		
		makeCrossesTableComponent.initializeCrossesMadeTable();
		
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.FEMALE_PARENT.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.MALE_PARENT.getName()));
	}	
}
