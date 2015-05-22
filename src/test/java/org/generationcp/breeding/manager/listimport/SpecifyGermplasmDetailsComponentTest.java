package org.generationcp.breeding.manager.listimport;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.listimport.util.GermplasmListUploader;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.vaadin.ui.Table;

import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SpecifyGermplasmDetailsComponentTest {
	
	private static final String FILE_NAME = "Maize Basic-Template.2015.01.01";
	private static final String EXTENSION = "xls";
	private static final String COMPLETE_FILE_NAME = FILE_NAME + "." + EXTENSION;
	
	@Mock
	private GermplasmImportMain source;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@InjectMocks
	private SpecifyGermplasmDetailsComponent specifyGermplasmDetailsComponent = spy(new SpecifyGermplasmDetailsComponent(source, false));

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
	
	@Test
	public void testInitializeFromImportFile_BasicTemplate(){
		Table table = spy(new Table());
		specifyGermplasmDetailsComponent.setGermplasmDetailsTable(table);
		Mockito.doReturn(table).when(specifyGermplasmDetailsComponent).getGermplasmDetailsTable();
		Mockito.doNothing().when(table).setColumnCollapsed(ColumnLabels.STOCKID, true);
		Mockito.doNothing().when(table).setColumnCollapsed(ColumnLabels.AMOUNT, true);

		GermplasmListUploader uploader = Mockito.mock(GermplasmListUploader.class);
		Mockito.doReturn(false).when(uploader).hasStockIdFactor();
		Mockito.doReturn(false).when(uploader).hasInventoryAmount();
		Mockito.doReturn(false).when(uploader).hasInventoryAmountOnly();
		Mockito.doReturn(false).when(uploader).importFileIsAdvanced();
		specifyGermplasmDetailsComponent.setGermplasmListUploader(uploader);

		GermplasmFieldsComponent fieldsComponent = Mockito.mock(GermplasmFieldsComponent.class);
		Mockito.doNothing().when(fieldsComponent).refreshLayout(Matchers.anyBoolean());
		Mockito.doReturn(fieldsComponent).when(specifyGermplasmDetailsComponent).getGermplasmFieldsComponent();
		
		Mockito.doNothing().when(specifyGermplasmDetailsComponent).updateTotalEntriesLabel();
		Mockito.doNothing().when(specifyGermplasmDetailsComponent).showFirstPedigreeOption(Matchers.anyBoolean());
		Mockito.doNothing().when(specifyGermplasmDetailsComponent).toggleAcceptSingleMatchesCheckbox();
		List<ImportedGermplasm> testGermplasm = createImportedGermplasmFromBasicTemplate();
		Mockito.doReturn(testGermplasm).when(specifyGermplasmDetailsComponent).getImportedGermplasms();
		
		ImportedGermplasmList importedList = new ImportedGermplasmList(COMPLETE_FILE_NAME, "", "", "", null);
		specifyGermplasmDetailsComponent.initGermplasmDetailsTable();
		specifyGermplasmDetailsComponent.initializeFromImportFile(importedList);
		
		Assert.assertTrue(table.getItemIds().size() == 5);
		for (int i=1; i<=5; i++){
			Integer id = new Integer(i);
			Assert.assertEquals(id, table.getItem(id).getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue());
			Assert.assertEquals(FILE_NAME + ":" + i, table.getItem(id).getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue());
			Assert.assertEquals("LEAFNODE00" + i, table.getItem(id).getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue());
			Assert.assertNull(table.getItem(id).getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue());
			Assert.assertNull(table.getItem(id).getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue());
			Assert.assertNull(table.getItem(id).getItemProperty(ColumnLabels.GID.getName()).getValue());
		}
	}
	
	private List<ImportedGermplasm> createImportedGermplasmFromBasicTemplate(){
		 List<ImportedGermplasm> testListOfGermplasm = new ArrayList<ImportedGermplasm>();
		 for (int i=1; i<=5; i++){
			 ImportedGermplasm germplasm = new ImportedGermplasm();
			 germplasm.setEntryId(i);
			 germplasm.setDesig("LEAFNODE00" + i);
			 testListOfGermplasm.add(germplasm);
		 }
		 return testListOfGermplasm;
	}
}