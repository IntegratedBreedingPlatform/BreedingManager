package org.generationcp.breeding.manager.util;

import java.util.List;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;

public class BreedingManagerTransformationUtil {

	private BreedingManagerTransformationUtil() {
		
	}
	
	public static ImmutableSet<Integer> getAllGidsFromGermplasmEntry(final List<GermplasmListEntry> germplasmEntryList) {
		final Function<GermplasmListEntry, Integer> getGidFunction = new Function<GermplasmListEntry, Integer>() {
			public Integer apply(GermplasmListEntry germplasmListEntry) {
				return germplasmListEntry.getGid();
			}
		};
		return FluentIterable.from(germplasmEntryList).transform(getGidFunction).toSet();
	}

}


