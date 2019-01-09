
package org.generationcp.breeding.manager.listimport;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.util.GermplasmListUploader;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Table;

@RunWith(MockitoJUnitRunner.class)
public class SpecifyGermplasmDetailsComponentTest {

	private static final String FILE_NAME = "Maize Basic-Template.2015.01.01";
	private static final String EXTENSION = "xls";
	private static final String COMPLETE_FILE_NAME =
			SpecifyGermplasmDetailsComponentTest.FILE_NAME + "." + SpecifyGermplasmDetailsComponentTest.EXTENSION;
	private static final String TEST_SOURCE_VALUE = "ListABC";

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmListUploader germplasmListUploader;

	@Mock
	private GermplasmFieldsComponent germplasmFieldsComponent;

	@InjectMocks
	private final SpecifyGermplasmDetailsComponent specifyGermplasmDetailsComponent =
			new SpecifyGermplasmDetailsComponent(Mockito.mock(GermplasmImportMain.class), false);

	@Before
	public void setUp() throws Exception {
		Mockito.doReturn("").when(this.messageSource).getMessage(Matchers.any(Message.class));
		this.specifyGermplasmDetailsComponent.instantiateComponents();
		this.specifyGermplasmDetailsComponent.setGermplasmListUploader(this.germplasmListUploader);
		this.specifyGermplasmDetailsComponent.setGermplasmFieldsComponent(this.germplasmFieldsComponent);
	}

	@Test
	public void testInitGermplasmDetailsTable_returnsTheValueFromColumLabelDefaultName() {
		final Table table = new Table();
		this.specifyGermplasmDetailsComponent.setGermplasmDetailsTable(table);

		this.specifyGermplasmDetailsComponent.initGermplasmDetailsTable();

		Assert.assertEquals("ENTRY_ID", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("ENTRY CODE", table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("PARENTAGE", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals("GID", table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals("SEED SOURCE", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}

	@Test
	public void testInitGermplasmDetailsTable_returnsTheValueFromOntologyManager() throws MiddlewareQueryException {
		final Table table = new Table();
		this.specifyGermplasmDetailsComponent.setGermplasmDetailsTable(table);

		final Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);

		this.specifyGermplasmDetailsComponent.initGermplasmDetailsTable();

		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}

	@Test
	public void testInitializeFromImportFile_BasicTemplateSourceAvailable() {
		final Table table = new Table();

		final List<ImportedGermplasm> importedGermplasms =
				this.createImportedGermplasmFromBasicTemplate(SpecifyGermplasmDetailsComponentTest.TEST_SOURCE_VALUE);

		this.setupInitializeFromImportedFileTest(table, importedGermplasms);
		Assert.assertTrue(table.getItemIds().size() == 5);
		for (int i = 1; i <= 5; i++) {
			final Integer id = new Integer(i);
			Assert.assertEquals(id, table.getItem(id).getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue());

			Assert.assertEquals(SpecifyGermplasmDetailsComponentTest.TEST_SOURCE_VALUE,
					table.getItem(id).getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue());
			Assert.assertEquals("LEAFNODE00" + i, table.getItem(id).getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue());
			Assert.assertNull(table.getItem(id).getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue());
			Assert.assertNull(table.getItem(id).getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue());
			Assert.assertNull(table.getItem(id).getItemProperty(ColumnLabels.GID.getName()).getValue());
		}
	}

	@Test
	public void testInitializeFromImportFile_BasicTemplateNoSourceValue() {
		final Table table = new Table();

		// we want to test the result of importing a germplasm list with a null / empty source value from the file
		final List<ImportedGermplasm> importedGermplasms = this.createImportedGermplasmFromBasicTemplate(null);

		this.setupInitializeFromImportedFileTest(table, importedGermplasms);
		Assert.assertTrue(table.getItemIds().size() == 5);
		for (int i = 1; i <= 5; i++) {
			final Integer id = new Integer(i);
			Assert.assertEquals("", table.getItem(id).getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue());
		}
	}

	protected void setupInitializeFromImportedFileTest(final Table table, final List<ImportedGermplasm> germplasmList) {

		final GermplasmFieldsComponent germplasmFieldsComponent = Mockito.mock(GermplasmFieldsComponent.class);
		this.specifyGermplasmDetailsComponent.setGermplasmDetailsTable(table);
		this.specifyGermplasmDetailsComponent.setGermplasmFieldsComponent(germplasmFieldsComponent);

		this.specifyGermplasmDetailsComponent.setImportedGermplasms(germplasmList);

		final GermplasmListUploader uploader = Mockito.mock(GermplasmListUploader.class);
		Mockito.doReturn(false).when(uploader).hasStockIdFactor();
		Mockito.doReturn(false).when(uploader).hasInventoryAmount();
		Mockito.doReturn(false).when(uploader).importFileIsAdvanced();
		this.specifyGermplasmDetailsComponent.setGermplasmListUploader(uploader);

		final ImportedGermplasmList importedList =
				new ImportedGermplasmList(SpecifyGermplasmDetailsComponentTest.COMPLETE_FILE_NAME, "", "", "", null);
		this.specifyGermplasmDetailsComponent.initGermplasmDetailsTable();
		this.specifyGermplasmDetailsComponent.initializeFromImportFile(importedList);

	}

	@Test
	public void testvalidateSeedLocationWithoutInventoryValue() {
		Mockito.doReturn(false).when(this.germplasmListUploader).hasInventoryAmount();
		Assert.assertTrue("Returns true when there is no inventory value from the germplasm import file.",
				this.specifyGermplasmDetailsComponent.validateSeedLocation());
	}

	@Test
	public void testvalidateSeedLocationWithInventoryValue() {
		Mockito.doReturn(true).when(this.germplasmListUploader).hasInventoryAmount();
		final ComboBox seedLocationComboBox = Mockito.mock(ComboBox.class);
		Mockito.doReturn(seedLocationComboBox).when(this.germplasmFieldsComponent).getSeedLocationComboBox();
		Mockito.doReturn(null).when(seedLocationComboBox).getValue();

		this.specifyGermplasmDetailsComponent.validateSeedLocation();

		// expecting to show an error message since seed location is required when there is an inventory value
		Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.SEED_STORAGE_LOCATION_LABEL);

	}

	private List<ImportedGermplasm> createImportedGermplasmFromBasicTemplate(final String sourceValue) {
		final List<ImportedGermplasm> testListOfGermplasm = new ArrayList<ImportedGermplasm>();
		for (int i = 1; i <= 5; i++) {
			final ImportedGermplasm germplasm = new ImportedGermplasm();
			germplasm.setEntryId(i);
			germplasm.setDesig("LEAFNODE00" + i);
			germplasm.setSource(sourceValue);
			testListOfGermplasm.add(germplasm);
		}
		return testListOfGermplasm;
	}
}
