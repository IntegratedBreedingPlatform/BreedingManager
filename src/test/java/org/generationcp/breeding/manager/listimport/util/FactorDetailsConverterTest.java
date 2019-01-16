
package org.generationcp.breeding.manager.listimport.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.breeding.manager.data.initializer.ImportedGermplasmListDataInitializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by cyrus on 10/11/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class FactorDetailsConverterTest {

	@Mock
	private Workbook workbook;

	private FactorDetailsConverter factorDetailsConverter;

	private ImportedGermplasmListDataInitializer importedGermplasmListInitializer;

	@Before
	public void setUp() throws Exception {
		this.factorDetailsConverter =
				new FactorDetailsConverter(this.workbook, 0, GermplasmListParser.DESCRIPTION_SHEET_NO,
						GermplasmListParser.FactorHeaders.values().length, GermplasmListParser.FactorHeaders.names());

		// initializer
		this.importedGermplasmListInitializer = new ImportedGermplasmListDataInitializer();
	}

	@Test
	public void testIsGermplasmNameScale() throws Exception {
		assertTrue("Should be a germplasm name scale", this.factorDetailsConverter.isGermplasmNameScale(FactorDetailsConverter.DBCV_SCALE));
		assertTrue("Should be a germplasm name scale",
				this.factorDetailsConverter.isGermplasmNameScale(FactorDetailsConverter.GERMPLASM_NAME));
		assertFalse("Should not be a germplasm name scale",
				this.factorDetailsConverter.isGermplasmNameScale(FactorDetailsConverter.DBID_SCALE));
	}

	@Test
	public void testIsGermplasmIdScale() throws Exception {
		assertTrue("Should be a germplasm id scale", this.factorDetailsConverter.isGermplasmIdScale(FactorDetailsConverter.DBID_SCALE));
		assertTrue("Should be a germplasm id scale", this.factorDetailsConverter.isGermplasmIdScale(FactorDetailsConverter.GERMPLASM_ID));
		assertFalse("Should not be a germplasm id scale", this.factorDetailsConverter.isGermplasmIdScale(FactorDetailsConverter.DBCV_SCALE));
	}

	@Test
	public void testIsStockIdScale() throws Exception {
		assertTrue("Should be a germplasm id scale", this.factorDetailsConverter.isStockIdScale(FactorDetailsConverter.DBCV_SCALE));
		assertTrue("Should be a germplasm id scale", this.factorDetailsConverter.isStockIdScale(FactorDetailsConverter.GERMPLASM_ID));
		assertFalse("Should not be a germplasm id scale", this.factorDetailsConverter.isStockIdScale(FactorDetailsConverter.DBID_SCALE));
	}

	@Test
	public void testIsCodeScale() throws Exception {
		assertTrue("Should be a code scale", this.factorDetailsConverter.isCodeScale("CODE of EntryID"));
		assertFalse("Should not be a code scale", this.factorDetailsConverter.isCodeScale("NOT A C*DE"));
	}

	@Test
	public void testConvertToObject() throws Exception {
		final List<Map<Integer, String>> testData = this.importedGermplasmListInitializer.createFactorsRowValuesListParserData();

		for (final Map<Integer, String> rowValues : testData) {
			this.factorDetailsConverter.convertToObject(rowValues);
		}

		final Map<GermplasmListParser.FactorTypes, String> specialFactors = this.factorDetailsConverter.getSpecialFactors();

		assertTrue("Has GID", specialFactors.containsKey(GermplasmListParser.FactorTypes.GID));
		assertTrue("Has ENTRYCODE", specialFactors.containsKey(GermplasmListParser.FactorTypes.ENTRYCODE));
		assertTrue("Has ENTRY", specialFactors.containsKey(GermplasmListParser.FactorTypes.ENTRY));
		assertTrue("Has DESIG", specialFactors.containsKey(GermplasmListParser.FactorTypes.DESIG));

	}

}
