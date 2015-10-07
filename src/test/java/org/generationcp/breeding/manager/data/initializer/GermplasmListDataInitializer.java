
package org.generationcp.breeding.manager.data.initializer;

import org.generationcp.middleware.pojos.GermplasmList;

public class GermplasmListDataInitializer {

	public static GermplasmList createGermplasmList(int id) {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(id);
		germplasmList.setName("List Name");
		germplasmList.setDescription("This is a sample list.");
		germplasmList.setDate(20150109L);

		return germplasmList;
	}
}
