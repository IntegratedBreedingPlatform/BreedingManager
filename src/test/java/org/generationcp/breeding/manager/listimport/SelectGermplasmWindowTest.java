
package org.generationcp.breeding.manager.listimport;

import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.actions.ProcessImportedGermplasmAction;
import org.generationcp.breeding.manager.listimport.listeners.ImportGermplasmEntryActionListener;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.service.api.PedigreeService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
	private InventoryDataManager inventoryDataManager;

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
		this.selectGermplasmWindow.setInventoryDataManager(this.inventoryDataManager);
	}

	@Test
	public void testInitGermplasmTableReturnsTheValueFromColumLabelDefaultName() throws MiddlewareQueryException {
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
	public void testInitGermplasmTableReturnsTheValueFromOntology() throws MiddlewareQueryException {
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
		Mockito.when(this.inventoryDataManager.getAvailableBalanceForGermplasms(Matchers.anyList()))
			.thenReturn(Collections.<Germplasm>emptyList());
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

	@Test
	public void testDoneActionNoCheckboxOptionSelected() {
		// Initialize UI elements
		this.selectGermplasmWindow.instantiateComponents();

		// Setup mocks
		final Integer selectedGid = 101;
		final Table table = Mockito.mock(Table.class);
		Mockito.when(table.getValue()).thenReturn(selectedGid);
		this.selectGermplasmWindow.setGermplasmTable(table);
		final Germplasm germplasm = new Germplasm();
		germplasm.setGid(selectedGid);
		Mockito.when(this.germplasmManager.getGermplasmByGID(selectedGid)).thenReturn(germplasm);

		// Method to test
		this.selectGermplasmWindow.doneAction();

		// Check that GID of selected table row was used for updating as germplasm selected
		final ArgumentCaptor<Integer> gidCaptor = ArgumentCaptor.forClass(Integer.class);
		Mockito.verify(this.germplasmManager, Mockito.times(1)).getGermplasmByGID(gidCaptor.capture());
		Assert.assertEquals("Expecting GID selected in table is used in Middleware query to get germplasm.", selectedGid,
				gidCaptor.getValue());
		final ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Germplasm> germplasmCaptor = ArgumentCaptor.forClass(Germplasm.class);
		Mockito.verify(this.source, Mockito.times(1)).receiveGermplasmFromWindowAndUpdateGermplasmData(indexCaptor.capture(),
				germplasmCaptor.capture());
		Assert.assertEquals("Expecting right import index to be used.", this.selectGermplasmWindow.getGermplasmIndex(),
				indexCaptor.getValue().intValue());
		Assert.assertEquals("Expecting retrieved germplasm to be used.", germplasm, germplasmCaptor.getValue());

		// Verify other interactions with mocks
		Mockito.verify(this.source, Mockito.never()).mapDesignationToGermplasmForReuse(Matchers.anyString(), Matchers.anyInt());
		Mockito.verify(this.source, Mockito.times(1)).removeListener(Matchers.any(ImportGermplasmEntryActionListener.class));
		Mockito.verify(this.source, Mockito.times(1)).processNextItems();
	}

	@Test
	public void testDoneActionUseSameGidOptionSelected() {
		// Initialize UI elements
		this.selectGermplasmWindow.instantiateComponents();
		this.selectGermplasmWindow.getGroupRadioBtn().select(SelectGermplasmWindow.USE_SAME_GID);

		// Setup mocks
		final Integer selectedGid = 101;
		final Table table = Mockito.mock(Table.class);
		Mockito.when(table.getValue()).thenReturn(selectedGid);
		this.selectGermplasmWindow.setGermplasmTable(table);
		final Germplasm germplasm = new Germplasm();
		germplasm.setGid(selectedGid);
		Mockito.when(this.germplasmManager.getGermplasmByGID(selectedGid)).thenReturn(germplasm);

		// Method to test
		this.selectGermplasmWindow.doneAction();

		// Check that GID of selected table row was used for updating as germplasm selected
		final ArgumentCaptor<Integer> gidCaptor = ArgumentCaptor.forClass(Integer.class);
		Mockito.verify(this.germplasmManager, Mockito.times(1)).getGermplasmByGID(gidCaptor.capture());
		Assert.assertEquals("Expecting GID selected in table is used in Middleware query to get germplasm.", selectedGid,
				gidCaptor.getValue());
		final ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Germplasm> germplasmCaptor = ArgumentCaptor.forClass(Germplasm.class);
		Mockito.verify(this.source, Mockito.times(1)).receiveGermplasmFromWindowAndUpdateGermplasmData(indexCaptor.capture(),
				germplasmCaptor.capture());
		Assert.assertEquals("Expecting right import index to be used.", this.selectGermplasmWindow.getGermplasmIndex(),
				indexCaptor.getValue().intValue());
		Assert.assertEquals("Expecting retrieved germplasm to be used.", germplasm, germplasmCaptor.getValue());

		// Check that GID is added to map for reuse
		final ArgumentCaptor<String> designationCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Integer> reusedGidCaptor = ArgumentCaptor.forClass(Integer.class);
		Mockito.verify(this.source, Mockito.times(1)).mapDesignationToGermplasmForReuse(designationCaptor.capture(),
				reusedGidCaptor.capture());
		Assert.assertEquals("Expecting correct designation added to map for reuse.", this.selectGermplasmWindow.getDesignation(),
				designationCaptor.getValue());
		Assert.assertEquals("Expecting correct import index added to map for reuse.", this.selectGermplasmWindow.getGermplasmIndex(),
				reusedGidCaptor.getValue().intValue());
	}

	@Test
	public void testDoneActionIgnoreMatchOptionSelected() {
		// Initialize UI elements
		this.selectGermplasmWindow.instantiateComponents();
		this.selectGermplasmWindow.getGroupRadioBtn().select(SelectGermplasmWindow.IGNORE_MATCHES);

		// Method to test
		this.selectGermplasmWindow.doneAction();

		// Check that no germplasm was selected
		Mockito.verify(this.germplasmManager, Mockito.never()).getGermplasmByGID(Matchers.anyInt());
		Mockito.verify(this.source, Mockito.never()).receiveGermplasmFromWindowAndUpdateGermplasmData(Matchers.anyInt(),
				Matchers.any(Germplasm.class));

		// Verify other interactions with mocks
		Mockito.verify(this.source, Mockito.never()).mapDesignationToGermplasmForReuse(Matchers.anyString(), Matchers.anyInt());
		Mockito.verify(this.source, Mockito.times(1)).removeListener(Matchers.any(ImportGermplasmEntryActionListener.class));
		Mockito.verify(this.source, Mockito.times(1)).processNextItems();
	}

	@Test
	public void testDoneActionIgnoreRemainingMatchesSelected() {
		// Initialize UI elements
		this.selectGermplasmWindow.instantiateComponents();
		this.selectGermplasmWindow.getGroupRadioBtn().select(SelectGermplasmWindow.IGNORE_MATCHES);
		this.selectGermplasmWindow.getIgnoreRemainingMatchesCheckbox().setValue(true);

		// Method to test
		this.selectGermplasmWindow.doneAction();

		// Check that no germplasm was selected for current entry and next items are not processed
		Mockito.verify(this.germplasmManager, Mockito.never()).getGermplasmByGID(Matchers.anyInt());
		Mockito.verify(this.source, Mockito.never()).receiveGermplasmFromWindowAndUpdateGermplasmData(Matchers.anyInt(),
				Matchers.any(Germplasm.class));
		Mockito.verify(this.source, Mockito.times(1)).ignoreRemainingMatches();
		Mockito.verify(this.source, Mockito.never()).processNextItems();

		// Verify other interactions with mocks
		Mockito.verify(this.source, Mockito.never()).mapDesignationToGermplasmForReuse(Matchers.anyString(), Matchers.anyInt());
		Mockito.verify(this.source, Mockito.times(1)).removeListener(Matchers.any(ImportGermplasmEntryActionListener.class));
	}
}
