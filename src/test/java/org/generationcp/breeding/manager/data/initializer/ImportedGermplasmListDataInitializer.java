
package org.generationcp.breeding.manager.data.initializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedFactor;
import org.generationcp.commons.parsing.pojo.ImportedVariate;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;

public class ImportedGermplasmListDataInitializer {

	public ImportedGermplasmListDataInitializer() {
		// do nothing
	}

	public ImportedGermplasmList createImportedGermplasmList(final int noOfEntries, final boolean withNameFactors) {
		final String filename = "SourceList.xls";
		final String name = "Import List 001";
		final String title = "Import List 001 description";
		final String type = "LST";
		final Date date = new Date();

		final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList(filename, name, title, type, date);
		importedGermplasmList.setImportedGermplasm(this.createListOfImportedGermplasm(noOfEntries, withNameFactors));
		importedGermplasmList.setImportedFactors(this.createImportedFactors(withNameFactors));
		return importedGermplasmList;
	}

	public List<ImportedFactor> createImportedFactors(final boolean withNameFactors) {
		final List<ImportedFactor> importedFactors = new ArrayList<ImportedFactor>();

		importedFactors.add(new ImportedFactor("ENTRY", "The germplasm entry number", "GERMPLASM ENTRY", "NUMBER", "ENUMERATED", "C", ""));
		importedFactors.add(new ImportedFactor("DESIGNATION", "The name of the germplasm", "GERMPLASM ID", "DBCV", "ASSIGNED", "C", ""));
		if (withNameFactors) {
			importedFactors.add(new ImportedFactor("DRVNM", "Derivative Name", "GERMPLASM ID", "NAME", "ASSIGNED", "C", ""));
		}

		return importedFactors;
	}

	public List<ImportedGermplasm> createListOfImportedGermplasm(final int noOfEntries, final boolean withNameFactors) {
		final List<ImportedGermplasm> importedGermplasmList = new ArrayList<ImportedGermplasm>();

		for (int i = 1; i <= noOfEntries; i++) {

			importedGermplasmList.add(this.createImportedGermplasm(i, withNameFactors));
		}

		return importedGermplasmList;
	}

	public ImportedGermplasm createImportedGermplasm(final int id, final boolean withNameFactors) {
		final ImportedGermplasm importedGermplasm = new ImportedGermplasm();

		if (withNameFactors) {
			importedGermplasm.setNameFactors(this.createNameFactors(id, withNameFactors));
		}

		importedGermplasm.setAttributeVariates(this.createAttributeVariates(id));

		return importedGermplasm;
	}

	private Map<String, String> createNameFactors(final int id, final boolean withNameFactors) {
		final Map<String, String> nameFactors = new HashMap<String, String>();
		nameFactors.put("DRVNM", "DRVNM " + id);
		return nameFactors;
	}

	private Map<String, String> createAttributeVariates(final int id) {
		final Map<String, String> attributeVariates = new HashMap<String, String>();
		attributeVariates.put("NOTE", "note value" + id);
		return attributeVariates;
	}

	public List<GermplasmName> createGermplasmNameObjects(final int noOfEntries) {
		final List<GermplasmName> germplasmNameList = new ArrayList<GermplasmName>();

		for (int i = 1; i <= noOfEntries; i++) {

			final GermplasmName gName =
					new GermplasmName(GermplasmTestDataInitializer.createGermplasm(i), GermplasmTestDataInitializer.createGermplasmName(i));

			germplasmNameList.add(gName);
		}

		return germplasmNameList;
	}

	public List<Integer> createListOfGemplasmIds(final int noOfIds) {
		final List<Integer> ids = new ArrayList<Integer>();

		for (int i = 1; i <= noOfIds; i++) {
			ids.add(i);
		}

		return ids;
	}

	public Map<ListEntryLotDetails, Double> createReservations(final int noOfEntries) {
		final Map<ListEntryLotDetails, Double> reservations = new HashMap<>();
		for (Integer i = 0; i < noOfEntries; i++) {
			reservations.put(ListInventoryDataInitializer.createLotDetail(i, 1), i.doubleValue());
		}
		return reservations;
	}

	public List<Map<Integer, String>> createFactorsRowValuesListParserData() {
		final List<Map<Integer, String>> testData = new ArrayList<>();

		final String[][] rawData =
				{ {"ENTRY_NO", "Germplasm entry - enumerated (number)", "GERMPLASM ENTRY", "NUMBER", "ENUMERATED"},
						{"GID", "Germplasm identifier - assigned (DBID)", "GERMPLASM ID", "GERMPLASM ID", "ASSIGNED"},
						{"ENTRY_CODE", "Germplasm ID - Assigned (Code)", "GERMPLASM ENTRY", "CODE OF ENTRY_CODE", "ASSIGNED"},
						{"DESIGNATION", "Germplasm identifier - assigned (DBCV)", "GERMPLASM ID", "GERMPLASM NAME", "ASSIGNED"},
						{"CROSS", "The pedigree string of the germplasm", "CROSS HISTORY", "TEXT", "ASSIGNED"},
						{"SEED_SOURCE", "Seed source - Selected (Code)", "SEED SOURCE", "CODE OF SEED_SOURCE", "SELECTED"}};

		for (final String[] rowValue : rawData) {
			final Map<Integer, String> map = new HashMap<>();
			for (int i = 0; i < rowValue.length; i++) {
				map.put(i, rowValue[i]);
			}

			testData.add(map);
		}

		return testData;
	}
	
	public void addImportedVariates(final ImportedGermplasmList importedGermplasmList, final String... variates){
		for (final String variateName: variates){
			importedGermplasmList.addImportedVariate(new ImportedVariate(variateName, "Test Description", "ATTRIBUTE", "Scale 1", "Method 1", "C"));
		}
	}
}
