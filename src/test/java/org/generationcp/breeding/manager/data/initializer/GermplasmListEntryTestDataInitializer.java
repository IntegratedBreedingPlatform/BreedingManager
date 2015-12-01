
package org.generationcp.breeding.manager.data.initializer;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;

public class GermplasmListEntryTestDataInitializer {

	public static List<GermplasmListEntry> getGermplasmListEntries() {
		final List<GermplasmListEntry> listEntries = new ArrayList<>();

		listEntries.add(new GermplasmListEntry(1, 1, 1));
		listEntries.add(new GermplasmListEntry(2, 2, 2));

		return listEntries;
	}
}
