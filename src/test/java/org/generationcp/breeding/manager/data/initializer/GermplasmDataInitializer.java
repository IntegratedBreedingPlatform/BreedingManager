
package org.generationcp.breeding.manager.data.initializer;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Name;

public class GermplasmDataInitializer {

	public static Germplasm createGermplasm(final int id) {
		final Germplasm germplasm = new Germplasm();
		germplasm.setGid(id);
		germplasm.setGdate(20150101);
		germplasm.setGpid1(1);
		germplasm.setGpid2(2);
		return germplasm;
	}

	public static Name createGermplasmName(final int id) {
		final Name name = new Name();
		name.setGermplasmId(id);
		name.setNval("Name" + id);

		return name;
	}

	public static List<Name> createNameList(final int noOfEntries) {
		final List<Name> names = new ArrayList<Name>();

		for (int i = 1; i <= noOfEntries; i++) {
			names.add(createGermplasmName(i));
		}

		return names;
	}

	public static GermplasmList getGermplasmListTestData() {
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(3);
		germplasmList.setName("newName");
		germplasmList.setDescription("newDescription");
		germplasmList.setNotes("newNotes");
		germplasmList.setType("TEST_TYPE");
		return germplasmList;
	}

	public static List<GermplasmListEntry> getGermplasmListEntries() {
		final List<GermplasmListEntry> listEntries = new ArrayList<>();

		listEntries.add(new GermplasmListEntry(1, 1, 1));
		listEntries.add(new GermplasmListEntry(2, 2, 2));

		return listEntries;
	}
}
