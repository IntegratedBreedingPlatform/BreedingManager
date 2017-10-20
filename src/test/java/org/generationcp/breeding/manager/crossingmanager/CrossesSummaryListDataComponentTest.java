
package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.BreedingManagerTable;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
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
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		this.germplasmList = this.createGermplasmList();

		this.crossesSummaryListDataComponent = Mockito.spy(new CrossesSummaryListDataComponent(this.germplasmList));
		this.crossesSummaryListDataComponent.setOntologyDataManager(this.ontologyDataManager);
		this.crossesSummaryListDataComponent.setMessageSource(this.messageSource);
		this.crossesSummaryListDataComponent.setGermplasmListManager(this.germplasmListManager);
	}

	private GermplasmList createGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(1);
		return germplasmList;
	}

	@Test
	public void testInitializeListEntriesTable_returnsTheValueFromColumLabelDefaultName() throws MiddlewareQueryException {
		BreedingManagerTable table = new BreedingManagerTable(10, 8);

		Mockito.when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn(CrossesSummaryListDataComponentTest.DUMMY_LABEL);
		Mockito.when(this.crossesSummaryListDataComponent.getListDataTable()).thenReturn(table);
		Mockito.when(this.germplasmListManager.countGermplasmListDataByListId(this.germplasmList.getId())).thenReturn(10L);

		this.crossesSummaryListDataComponent.initializeListEntriesTable();

		Assert.assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("PARENTAGE", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals("ENTRY CODE", table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals("GID", table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals("SEED SOURCE", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
		Assert.assertEquals("Female Parent", table.getColumnHeader(ColumnLabels.FEMALE_PARENT.getName()));
		Assert.assertEquals("Male Parent", table.getColumnHeader(ColumnLabels.MALE_PARENT.getName()));
		Assert.assertEquals("FGID", table.getColumnHeader(ColumnLabels.FGID.getName()));
		Assert.assertEquals("MGID", table.getColumnHeader(ColumnLabels.MGID.getName()));
		Assert.assertEquals("METHOD NAME", table.getColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName()));
	}

	@Test
	public void testInitializeListEntriesTable_returnsTheValueFromOntologyManager() throws MiddlewareQueryException {
		BreedingManagerTable table = new BreedingManagerTable(10, 8);

		Mockito.when(this.crossesSummaryListDataComponent.getListDataTable()).thenReturn(table);
		Mockito.when(this.germplasmListManager.countGermplasmListDataByListId(this.germplasmList.getId())).thenReturn(10L);

		Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.FEMALE_PARENT.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.MALE_PARENT.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.FGID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.MGID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.BREEDING_METHOD_NAME.getId())).thenReturn(fromOntology);

		this.crossesSummaryListDataComponent.initializeListEntriesTable();

		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.FEMALE_PARENT.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.MALE_PARENT.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.FGID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.MGID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName()));
	}
}
