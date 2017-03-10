
package org.generationcp.breeding.manager.listimport;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.actions.ProcessImportedGermplasmAction;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.service.api.PedigreeService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

@RunWith(MockitoJUnitRunner.class)
public class SelectGermplasmWindowTest {

	private static final String ONTOLOGY_NAME = "Ontology Name";

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private ProcessImportedGermplasmAction source;

	@Mock
	private Window parentWindow;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmDataManager germplasmManager;

	@Mock
	private PedigreeService pedigreeService;

	private final String germplasmName = "Germplasm Name";
	private final int index = 2;
	private final Integer noOfImportedGermplasm = 1;

	private SelectGermplasmWindow selectGermplasmWindow;

	@Before
	public void setUp() {
		this.selectGermplasmWindow =
				new SelectGermplasmWindow(this.source, this.germplasmName, this.index, this.parentWindow, this.noOfImportedGermplasm);
		this.selectGermplasmWindow.setOntologyDataManager(this.ontologyDataManager);
		this.selectGermplasmWindow.setMessageSource(this.messageSource);
		this.selectGermplasmWindow.setGermplasmDataManager(this.germplasmManager);
		this.selectGermplasmWindow.setPedigreeService(this.pedigreeService);
	}

	@Test
	public void testInitGermplasmTable_returnsTheValueFromColumLabelDefaultName() throws MiddlewareQueryException {
		final Table germplasmTable = new Table();
		this.selectGermplasmWindow.setGermplasmTable(germplasmTable);

		final Term fromOntology = new Term();
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GERMPLASM_LOCATION.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.BREEDING_METHOD_NAME.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);

		this.selectGermplasmWindow.initGermplasmTable();

		final Table table = this.selectGermplasmWindow.getGermplasmTable();

		Assert.assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("GID", table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals("LOCATIONS", table.getColumnHeader(ColumnLabels.GERMPLASM_LOCATION.getName()));
		Assert.assertEquals("METHOD NAME", table.getColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName()));
		Assert.assertEquals("PARENTAGE", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
	}

	@Test
	public void testInitGermplasmTable_returnsTheValueFromOntology() throws MiddlewareQueryException {
		final Table germplasmTable = new Table();
		this.selectGermplasmWindow.setGermplasmTable(germplasmTable);

		final Term fromOntology = new Term();
		fromOntology.setName(SelectGermplasmWindowTest.ONTOLOGY_NAME);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GERMPLASM_LOCATION.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.BREEDING_METHOD_NAME.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);

		this.selectGermplasmWindow.initGermplasmTable();

		final Table table = this.selectGermplasmWindow.getGermplasmTable();

		Assert.assertEquals(SelectGermplasmWindowTest.ONTOLOGY_NAME, table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals(SelectGermplasmWindowTest.ONTOLOGY_NAME, table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals(SelectGermplasmWindowTest.ONTOLOGY_NAME, table.getColumnHeader(ColumnLabels.GERMPLASM_LOCATION.getName()));
		Assert.assertEquals(SelectGermplasmWindowTest.ONTOLOGY_NAME, table.getColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName()));
		Assert.assertEquals(SelectGermplasmWindowTest.ONTOLOGY_NAME, table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
	}

	@Test
	public void testInitializeGuideMessage() {
		// Initialize the select Germplasm Label
		final Label selectGermplasmLabel = new Label("", Label.CONTENT_XHTML);
		this.selectGermplasmWindow.setSelectGermplasmLabel(selectGermplasmLabel);

		Mockito.when(this.messageSource.getMessage(Message.GERMPLASM_MATCHES_LABEL,
				new Object[] {this.index + 1, this.noOfImportedGermplasm, this.germplasmName}))
				.thenReturn(Message.GERMPLASM_MATCHES_LABEL.toString());

		this.selectGermplasmWindow.initializeGuideMessage();

		Assert.assertEquals("The select germplasm label's value should be " + Message.GERMPLASM_MATCHES_LABEL.toString(),
				Message.GERMPLASM_MATCHES_LABEL.toString(), selectGermplasmLabel.getValue());
	}

	@Test
	public void testInitializeTableValues() {
		this.selectGermplasmWindow.initGermplasmTable();

		final int nameMatchCount = 3;
		Mockito.doReturn(new Long(nameMatchCount)).when(this.germplasmManager).countGermplasmByName(Matchers.anyString(),
				Matchers.any(Operation.class));
		final List<Germplasm> germplasm = new ArrayList<Germplasm>();
		germplasm.add(GermplasmTestDataInitializer.createGermplasm(1));
		germplasm.add(GermplasmTestDataInitializer.createGermplasm(2));
		germplasm.add(GermplasmTestDataInitializer.createGermplasm(3));
		Mockito.doReturn(germplasm).when(this.germplasmManager).getGermplasmByName(Matchers.anyString(), Matchers.anyInt(),
				Matchers.anyInt(), Matchers.any(Operation.class));

		// Method to test - populate germplasm table
		this.selectGermplasmWindow.initializeTableValues();

		final Table germplasmTable = this.selectGermplasmWindow.getGermplasmTable();
		Assert.assertEquals(nameMatchCount, germplasmTable.size());
	}
}
