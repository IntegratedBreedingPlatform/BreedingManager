package org.generationcp.breeding.manager.util;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import junit.framework.Assert;

public class BreedingManagerTransformationUtilTest {

	private static final int ENTRY_ID = 3;
	private static final int GID = 2;
	private static final int LIST_ENTRY_ID = 1;

	@Test
	public void testGetAllGidsFromGermplasmEntry() throws Exception {
		final ImmutableList<GermplasmListEntry> germplasmEntryLists = ImmutableList.of(new GermplasmListEntry(LIST_ENTRY_ID, GID, ENTRY_ID));
		final ImmutableSet<Integer> allGidsFromGermplasmEntry = BreedingManagerTransformationUtil.getAllGidsFromGermplasmEntry(germplasmEntryLists);
		
		Assert.assertEquals("We should have om results in the set returned", 1, allGidsFromGermplasmEntry.size());
		Assert.assertTrue("We execpt id 2 to be part of the set", allGidsFromGermplasmEntry.contains(GID));

	}

}
