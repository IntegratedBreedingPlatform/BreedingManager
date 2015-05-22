package org.generationcp.breeding.manager.listimport.util;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Created by cyrus on 5/7/15.
 * Unit test will only cover the observation sheet parsing
 * as we will have a separate unit test for parsing Description Sheet
 * (see the equivalent unit test for {@link org.generationcp.commons.parsing.DescriptionSheetParser})
 * But test still promises at least 50% coverage for {@link GermplasmListParser}
 */
@RunWith(MockitoJUnitRunner.class)
public class GermplasmListParserTest {
	public static final String TEST_FILE_NAME = "GermplasmImportTemplate-StockIDs-only.xls";
	public static final String OBSERVATION_NO_STOCK_ID_FILE = "GermplasmImportTemplate-StockIDs-missing-stock-id-column.xls";
	public static final String OBSERVATION_NO_STOCK_ID_VALUES_FILE = "GermplasmImportTemplate-StockIDs-missing-stock-id-values.xls";
	public static final String NO_INVENTORY_COL_FILE = "GermplasmImportTemplate-StockIDs-no-inventory-column.xls";
	public static final String DUPLICATE_STOCK_ID_FILE = "GermplasmImportTemplate-StockIDs-duplicate-stock-ids.xls";

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private InventoryDataManager inventoryDataManager;


	@InjectMocks
	private GermplasmListParser parser = spy(new GermplasmListParser());

	private Workbook defaultWorkbook;
	private Workbook noStockIDWorkbook;
	private Workbook missingStockIDValuesWorkbook;
	private Workbook noInventoryWorkbook;
	private Workbook duplicateStockIdWorkbook;

	private ImportedGermplasmList importedGermplasmList;

	@Before
	public void setUp() throws Exception {
		File workbookFile = new File(ClassLoader.getSystemClassLoader().getResource(TEST_FILE_NAME).toURI());
		File workbookFile2 = new File(ClassLoader.getSystemClassLoader().getResource(OBSERVATION_NO_STOCK_ID_FILE).toURI());
		File workbookFile3 = new File(ClassLoader.getSystemClassLoader().getResource(OBSERVATION_NO_STOCK_ID_VALUES_FILE).toURI());
		File workbookFile4 = new File(ClassLoader.getSystemClassLoader().getResource(NO_INVENTORY_COL_FILE).toURI());
		File workbookFile5 = new File(ClassLoader.getSystemClassLoader().getResource(DUPLICATE_STOCK_ID_FILE).toURI());

		assert workbookFile.exists();
		assert workbookFile2.exists();
		assert workbookFile3.exists();
		assert workbookFile4.exists();
		assert workbookFile5.exists();


		defaultWorkbook = WorkbookFactory.create(workbookFile);
		noStockIDWorkbook = WorkbookFactory.create(workbookFile2);
		missingStockIDValuesWorkbook = WorkbookFactory.create(workbookFile3);
		noInventoryWorkbook = WorkbookFactory.create(workbookFile4);
		duplicateStockIdWorkbook = WorkbookFactory.create(workbookFile5);


		when(ontologyDataManager.isSeedAmountVariable(eq("INVENTORY AMOUNT"))).thenReturn(true);
		when(ontologyDataManager.isSeedAmountVariable(not(eq("INVENTORY AMOUNT")))).thenReturn(false);
		when(germplasmDataManager.getGermplasmByGID(anyInt())).thenReturn(mock(Germplasm.class));
		when(inventoryDataManager.getSimilarStockIds(anyList())).thenReturn(new ArrayList<String>());

		Map<Integer,String> preferredNames = mock(Map.class);
		when(preferredNames.get(any())).thenReturn("TEST DESIG");
		when(germplasmDataManager.getPreferredNamesByGids(anyList())).thenReturn(preferredNames);

	}

	/**
	 * This is the default case, the template has a stock id factor
	 * @throws Exception
	 */
	@Test
	public void testTemplateWithStockIdFactor() throws Exception {
		importedGermplasmList = parser.parseWorkbook(defaultWorkbook,null);

		assertNotNull(importedGermplasmList);
		assert parser.hasStockIdFactor();
	}

	/**
	 * Test when we have no stock id column in observation
	 * @throws Exception
	 */
	@Test
	public void testTemplateWithNoStockIdInObservation() throws Exception {
		try {
			importedGermplasmList = parser.parseWorkbook(noStockIDWorkbook,null);
			fail();
		} catch (FileParsingException e) {
			assertEquals("GERMPLASM_PARSE_STOCK_COLUMN_MISSING",e.getMessage());
		}

	}

	/**
	 * Test when we have no stock id column in observation
	 * @throws Exception
	 */
	@Test
	public void testTemplateWithNoInventoryColumn() throws Exception {
		importedGermplasmList = parser.parseWorkbook(noInventoryWorkbook,null);

		assertTrue(parser.getNoInventoryWarning().contains("StockIDs can only be added for germplasm if it has existing inventory in the BMS"));
	}

	/**
	 * Test when we have stock id colum but contain missing values
	 * @throws Exception
	 */
	@Test
	public void testTemplateWithMissingStockIdValuesInObservation() throws Exception {
		try {
			importedGermplasmList = parser.parseWorkbook(missingStockIDValuesWorkbook,null);
			fail();
		} catch (FileParsingException e) {
			assertEquals("common.parser.validation.error.empty.value",e.getMessage());
		}
	}

	/**
	 * Test when we have stock id colum but contain missing values
	 * @throws Exception
	 */
	@Test
	public void testTemplateWithDuplicateIdsInObservation() throws Exception {
		try {
			importedGermplasmList = parser.parseWorkbook(duplicateStockIdWorkbook,null);
			fail();
		} catch (FileParsingException e) {
			assertEquals("GERMPLASM_PARSE_DUPLICATE_STOCK_ID",e.getMessage());
		}
	}

}