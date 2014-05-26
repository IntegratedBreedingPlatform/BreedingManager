package org.generationcp.breeding.manager.listeners;

import org.generationcp.middleware.pojos.GermplasmList;

public interface ListTreeActionsListener {

	public void updateUIForRenamedList(GermplasmList list, String newName);
	
	public void openListDetails(GermplasmList list);
	
	public void folderClicked(GermplasmList list);
}