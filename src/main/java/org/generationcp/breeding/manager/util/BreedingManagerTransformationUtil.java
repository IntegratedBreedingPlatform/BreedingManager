package org.generationcp.breeding.manager.util;

import java.util.List;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;

/**
 * A utility that helps transform from one collection to another.
 *
 */
public class BreedingManagerTransformationUtil {

	private BreedingManagerTransformationUtil() {
		
	}
	
	/**
	 * Helps retrieve a list of germplasm id from a collection of {@link GermplasmListEntry}
	 * 
	 * @param germplasmEntryList the list of {@link GermplasmListEntry} to mine
	 * @return the resultant list of germplasm ids.
	 */
	public static ImmutableSet<Integer> getAllGidsFromGermplasmEntry(final List<GermplasmListEntry> germplasmEntryList) {
		final Function<GermplasmListEntry, Integer> getGidFunction = new Function<GermplasmListEntry, Integer>() {
			public Integer apply(GermplasmListEntry germplasmListEntry) {
				return germplasmListEntry.getGid();
			}
		};
		return FluentIterable.from(germplasmEntryList).transform(getGidFunction).toSet();
	}

}


