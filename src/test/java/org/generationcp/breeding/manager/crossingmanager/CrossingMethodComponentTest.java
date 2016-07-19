package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.data.initializer.GermplasmListEntryTestDataInitializer;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CrossingMethodComponentTest {
	
	@Mock
	private GermplasmDataManager germplasmDataManager;
	
	@InjectMocks
	private CrossingMethodComponent crossingMethodComponent;
	
	@Before
	public void setUp() {
		crossingMethodComponent.setGermplasmDataManager(germplasmDataManager);
	}
	
	@Test
	public void testUpdateParentsDesignationToPreferredName() {
		final List<GermplasmListEntry> femaleList = GermplasmListEntryTestDataInitializer.getGermplasmListEntries();
		final List<GermplasmListEntry> maleList = GermplasmListEntryTestDataInitializer.getGermplasmListEntries();
		final List<Integer> gids = this.getAllGids(femaleList, maleList);
		final Map<Integer, String> gidToPreferredNameMap = this.createGidToPreferredNameMap(gids);
		Mockito.doReturn(gidToPreferredNameMap).when(this.germplasmDataManager).getPreferredNamesByGids(gids);
		crossingMethodComponent.updateParentsDesignationToPreferredName(femaleList,maleList);
		//Test that the designation of the list entries is the preferred name of the germplasm
		final List<GermplasmListEntry> parentsListEntries = new ArrayList<>();
		parentsListEntries.addAll(femaleList);
		parentsListEntries.addAll(maleList);
		for (final GermplasmListEntry germplasmListEntry : parentsListEntries) {
			final Integer gid = germplasmListEntry.getGid();
			final String designation = germplasmListEntry.getDesignation();
			final String preferredName = gidToPreferredNameMap.get(gid);
			Assert.assertEquals("The designation of the list entry should be the preferred name of the germplasm",
					preferredName,designation);
		}
	}

	private List<Integer> getAllGids(final List<GermplasmListEntry> femaleList, final List<GermplasmListEntry> maleList) {
		final Set<Integer> gids = new HashSet<>();
		for (final GermplasmListEntry germplasmListEntry : maleList) {
			final Integer gid = germplasmListEntry.getGid();
			gids.add(gid);
		}
		for (final GermplasmListEntry germplasmListEntry : femaleList) {
			final Integer gid = germplasmListEntry.getGid();
			gids.add(gid);
		}
		final List<Integer> uniqueGids = new ArrayList<>();
		uniqueGids.addAll(gids);
		return uniqueGids;
	}

	private Map<Integer, String> createGidToPreferredNameMap(final List<Integer> gids) {
		final Map<Integer, String> gidToPreferredNameMap = new HashMap<>();
		for (final Integer gid : gids) {
			gidToPreferredNameMap.put(gid, "PREFERRED-"+gid);
		}
		return gidToPreferredNameMap;
	}
	
	
}
