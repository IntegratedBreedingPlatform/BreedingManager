package org.generationcp.breeding.manager.listeners;

import org.generationcp.middleware.pojos.GermplasmList;

public interface ListTreeActionsListener {

	public void updateUIForDeletedList(GermplasmList list);
	
	public void updateUIForRenamedList(GermplasmList list, String newName);
	
	public void openListDetails(GermplasmList list);
}
