package org.generationcp.breeding.manager.crossingmanager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.BreedingManagerTable;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CrossesSummaryListDataComponentTest {
	private static final String DUMMY_LABEL = "DUMMY LABEL";
	@Mock
	private OntologyDataManager ontologyDataManager;
	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	@Mock
	private GermplasmListManager germplasmListManager;
	
	private CrossesSummaryListDataComponent crossesSummaryListDataComponent;
	
	private GermplasmList germplasmList;
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		
		germplasmList = createGermplasmList();
		
		crossesSummaryListDataComponent = spy(new CrossesSummaryListDataComponent(germplasmList));
		crossesSummaryListDataComponent.setOntologyDataManager(ontologyDataManager);
		crossesSummaryListDataComponent.setMessageSource(messageSource);
		crossesSummaryListDataComponent.setGermplasmListManager(germplasmListManager);
	}
	
	private GermplasmList createGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(1);
		return germplasmList;
	}

	@Test
	public void testInitializeListEntriesTable_returnsTheValueFromColumLabelDefaultName() throws MiddlewareQueryException{
		BreedingManagerTable table = new BreedingManagerTable(10,8);
		
		when(messageSource.getMessage(Message.HASHTAG)).thenReturn(DUMMY_LABEL);
		when(crossesSummaryListDataComponent.getListDataTable()).thenReturn(table);
		when(germplasmListManager.countGermplasmListDataByListId(germplasmList.getId())).thenReturn(10L);
		
		crossesSummaryListDataComponent.initializeListEntriesTable();
		
		assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		assertEquals("PARENTAGE", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		assertEquals("ENTRY CODE", table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		assertEquals("GID", table.getColumnHeader(ColumnLabels.GID.getName()));
		assertEquals("SEED SOURCE", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
		assertEquals("Female Parent", table.getColumnHeader(ColumnLabels.FEMALE_PARENT.getName()));
		assertEquals("Male Parent", table.getColumnHeader(ColumnLabels.MALE_PARENT.getName()));
		assertEquals("FGID", table.getColumnHeader(ColumnLabels.FGID.getName()));
		assertEquals("MGID", table.getColumnHeader(ColumnLabels.MGID.getName()));
		assertEquals("METHOD NAME", table.getColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName()));
	}
	
	@Test
	public void testInitializeListEntriesTable_returnsTheValueFromOntologyManager() throws MiddlewareQueryException{
		BreedingManagerTable table = new BreedingManagerTable(10,8);
		
		when(crossesSummaryListDataComponent.getListDataTable()).thenReturn(table);
		when(germplasmListManager.countGermplasmListDataByListId(germplasmList.getId())).thenReturn(10L);
		
		Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		when(ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.FEMALE_PARENT.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.MALE_PARENT.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.FGID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.MGID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.BREEDING_METHOD_NAME.getId())).thenReturn(fromOntology);
		
		crossesSummaryListDataComponent.initializeListEntriesTable();
		
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.GID.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.FEMALE_PARENT.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.MALE_PARENT.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.FGID.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.MGID.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName()));
	}
}
