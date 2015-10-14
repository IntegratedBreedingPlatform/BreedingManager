
package org.generationcp.breeding.manager.data.initializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;

public class ImportedGermplasmListDataInitializer {

	public static ImportedGermplasmList createImportedGermplasmList(final int noOfEntries) {
		final String filename = "SourceList.xls";
		final String name = "Import List 001";
		final String title = "Import List 001 description";
		final String type = "LST";
		final Date date = new Date();

		final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList(filename, name, title, type, date);
		importedGermplasmList.setImportedGermplasms(createListOfImportedGermplasm(noOfEntries));
		return importedGermplasmList;
	}

	public static List<ImportedGermplasm> createListOfImportedGermplasm(final int noOfEntries) {
		final List<ImportedGermplasm> importedGermplasmList = new ArrayList<ImportedGermplasm>();

		for (int i = 1; i <= noOfEntries; i++) {

			importedGermplasmList.add(createImportedGermplasm(i));
		}

		return importedGermplasmList;
	}

	public static ImportedGermplasm createImportedGermplasm(final int id) {
		final ImportedGermplasm importedGermplasm = new ImportedGermplasm();

		importedGermplasm.setAttributeVariates(createAttributeVariates(id));

		return importedGermplasm;
	}

	private static Map<String, String> createAttributeVariates(final int id) {
		final Map<String, String> attributeVariates = new HashMap<String, String>();
		attributeVariates.put("NOTE", "note value" + id);
		return attributeVariates;
	}

	public static List<GermplasmName> createGermplasmNameObjects(final int noOfEntries) {
		final List<GermplasmName> germplasmNameList = new ArrayList<GermplasmName>();

		for (int i = 1; i <= noOfEntries; i++) {

			final GermplasmName gName =
					new GermplasmName(GermplasmDataInitializer.createGermplasm(i), GermplasmDataInitializer.createGermplasmName(i));

			germplasmNameList.add(gName);
		}

		return germplasmNameList;
	}

	public static List<Integer> createListOfGemplasmIds(final int noOfIds) {
		final List<Integer> ids = new ArrayList<Integer>();

		for (int i = 1; i <= noOfIds; i++) {
			ids.add(i);
		}

		return ids;
	}
}
