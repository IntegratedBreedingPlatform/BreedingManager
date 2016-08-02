
package org.generationcp.breeding.manager.data.initializer;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;

public class GermplasmListDataTestDataInitializer {

	public static int NUM_OF_ENTRIES = 10;

	public static List<GermplasmListData> getGermplasmListDataList(final int listId) {
		final GermplasmList list = new GermplasmList(listId);
		final List<GermplasmListData> listDataList = new ArrayList<>();
		for (int i = 1; i <= GermplasmListDataTestDataInitializer.NUM_OF_ENTRIES; i++) {
			listDataList.add(GermplasmListDataTestDataInitializer.getGermplasmListData(list, i + 10, i + 100, i));
		}
		return listDataList;
	}

	public static GermplasmListData getGermplasmListData(final GermplasmList list, final int id, final int gid, final int entryId) {
		final GermplasmListData listData = new GermplasmListData();
		listData.setId(id);
		listData.setList(list);
		listData.setGid(gid);
		listData.setEntryId(entryId);
		listData.setEntryCode(Integer.toString(entryId));
		listData.setDesignation("LISTDATA-" + gid);
		listData.setGroupName("GRP-" + id);
		listData.setStatus(1);
		listData.setLocalRecordId(list.getId());

		final ListDataInventory listDataInventory = new ListDataInventory(id, gid);
		listDataInventory.setLotCount(id % 4);
		listDataInventory.setActualInventoryLotCount(id % 3);
		listDataInventory.setReservedLotCount(id % 2);
		listData.setInventoryInfo(listDataInventory);

		return listData;
	}
}
