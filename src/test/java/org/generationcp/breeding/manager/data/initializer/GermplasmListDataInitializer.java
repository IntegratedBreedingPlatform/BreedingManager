
package org.generationcp.breeding.manager.data.initializer;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;

public class GermplasmListDataInitializer {

	public static GermplasmList createGermplasmList(final int id) {
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(id);
		germplasmList.setName("List 001");
		germplasmList.setDescription("List 001 Description");
		germplasmList.setDate(20150101L);

		return germplasmList;
	}

	public static GermplasmList createGermplasmListWithListData(final int id, final int noOfEntries) {
		final GermplasmList germplasmList = createGermplasmList(id);
		germplasmList.setListData(createGermplasmListData(noOfEntries));

		return germplasmList;
	}

	public static List<GermplasmListData> createGermplasmListData(final Integer itemNo) {
		final List<GermplasmListData> listEntries = new ArrayList<GermplasmListData>();
		for (int i = 1; i <= itemNo; i++) {
			final GermplasmListData listEntry = new GermplasmListData();
			listEntry.setId(i);
			listEntry.setDesignation("Designation " + i);
			listEntry.setEntryCode("EntryCode " + i);
			listEntry.setEntryId(i);
			listEntry.setGroupName("GroupName " + i);
			listEntry.setStatus(1);
			listEntry.setSeedSource("SeedSource " + i);

			listEntries.add(listEntry);
		}

		return listEntries;
	}
}
