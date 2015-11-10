package org.generationcp.breeding.manager.listimport.util;

import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.breeding.manager.data.initializer.ImportedGermplasmListDataInitializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by cyrus on 10/11/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class FactorDetailsConverterTest {

	@Mock
	private Workbook workbook;

	private FactorDetailsConverter factorDetailsConverter;


	@Before
	public void setUp() throws Exception {
		factorDetailsConverter = new FactorDetailsConverter(workbook,0,GermplasmListParser.DESCRIPTION_SHEET_NO,
				GermplasmListParser.FactorHeaders.values().length, GermplasmListParser.FactorHeaders.names());
	}

	@Test
	public void testIsGermplasmNameScale() throws Exception {
		assertTrue("Should be a germplasm name scale",factorDetailsConverter.isGermplasmNameScale(FactorDetailsConverter.DBCV_SCALE));
		assertTrue("Should be a germplasm name scale",factorDetailsConverter.isGermplasmNameScale(FactorDetailsConverter.GERMPLASM_NAME));
		assertFalse("Should not be a germplasm name scale", factorDetailsConverter.isGermplasmNameScale(FactorDetailsConverter.DBID_SCALE));
	}

	@Test
	public void testIsGermplasmIdScale() throws Exception {
		assertTrue("Should be a germplasm id scale",factorDetailsConverter.isGermplasmIdScale(FactorDetailsConverter.DBID_SCALE));
		assertTrue("Should be a germplasm id scale",factorDetailsConverter.isGermplasmIdScale(FactorDetailsConverter.GERMPLASM_ID));
		assertFalse("Should not be a germplasm id scale",factorDetailsConverter.isGermplasmIdScale(FactorDetailsConverter.DBCV_SCALE));
	}

	@Test
	public void testIsStockIdScale() throws Exception {
		assertTrue("Should be a germplasm id scale",factorDetailsConverter.isStockIdScale(FactorDetailsConverter.DBCV_SCALE));
		assertTrue("Should be a germplasm id scale",factorDetailsConverter.isStockIdScale(FactorDetailsConverter.GERMPLASM_ID));
		assertFalse("Should not be a germplasm id scale",factorDetailsConverter.isStockIdScale(FactorDetailsConverter.DBID_SCALE));
	}

	@Test
	public void testIsCodeScale() throws Exception {
		assertTrue("Should be a code scale",factorDetailsConverter.isCodeScale("CODE of EntryID"));
		assertFalse("Should not be a code scale", factorDetailsConverter.isCodeScale("NOT A C*DE"));
	}

	@Test
	public void testConvertToObject() throws Exception {
		List<Map<Integer,String>> testData = ImportedGermplasmListDataInitializer.createFactorsRowValuesListParserData();

		for (Map<Integer,String> rowValues : testData) {
			factorDetailsConverter.convertToObject(rowValues);
		}


		Map<GermplasmListParser.FactorTypes, String> specialFactors = factorDetailsConverter.getSpecialFactors();

		assertTrue("Has GID",specialFactors.containsKey(GermplasmListParser.FactorTypes.GID));
		assertTrue("Has ENTRYCODE",specialFactors.containsKey(GermplasmListParser.FactorTypes.ENTRYCODE));
		assertTrue("Has ENTRY",specialFactors.containsKey(GermplasmListParser.FactorTypes.ENTRY));
		assertTrue("Has DESIG",specialFactors.containsKey(GermplasmListParser.FactorTypes.DESIG));

	}

}
