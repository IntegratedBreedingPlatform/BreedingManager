
package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.middleware.pojos.GermplasmList;

import com.vaadin.ui.Component;

public interface SaveListAsDialogSource {

	public void saveList(GermplasmList list);

	public void setCurrentlySavedGermplasmList(GermplasmList list);

	public Component getParentComponent();
	
	public void updateListUI();
}
