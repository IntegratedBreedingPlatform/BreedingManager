package org.generationcp.breeding.manager.action;

import java.util.List;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.middleware.pojos.GermplasmList;

public class SaveGermplasmListActionFactory {

	public SaveGermplasmListAction createInstance(final SaveGermplasmListActionSource source, final GermplasmList germplasmList,
			final List<GermplasmListEntry> listEntries){
		return new SaveGermplasmListAction(source, germplasmList, listEntries);
	}

}
