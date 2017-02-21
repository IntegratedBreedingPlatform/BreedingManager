
package org.generationcp.breeding.manager.listimport;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.actions.ProcessImportedGermplasmAction;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

@RunWith(MockitoJUnitRunner.class)
public class SelectGermplasmWindowTest {

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private ProcessImportedGermplasmAction source;

	@Mock
	private Window parentWindow;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	private final String germplasmName = "Germplasm Name";
	private final int index = 2;
	private final Integer noOfImportedGermplasm = 1;

	private SelectGermplasmWindow selectGermplasmWindow;

	@Before
	public void setUp() {
		this.selectGermplasmWindow = new SelectGermplasmWindow(this.source, this.germplasmName, this.index,
				this.parentWindow, this.noOfImportedGermplasm);
		this.selectGermplasmWindow.setOntologyDataManager(this.ontologyDataManager);
		this.selectGermplasmWindow.setMessageSource(this.messageSource);

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
		fromOntology.setName("Ontology Name");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GERMPLASM_LOCATION.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.BREEDING_METHOD_NAME.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);

		this.selectGermplasmWindow.initGermplasmTable();

		final Table table = this.selectGermplasmWindow.getGermplasmTable();

		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.GERMPLASM_LOCATION.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
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

		Assert.assertEquals("The selec germplasm label's value should be " + Message.GERMPLASM_MATCHES_LABEL.toString(),
				Message.GERMPLASM_MATCHES_LABEL.toString(), selectGermplasmLabel.getValue());
	}
}
