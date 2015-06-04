
package org.generationcp.breeding.manager.listimport;

import org.generationcp.middleware.pojos.GermplasmList;

import com.vaadin.ui.Window;

public interface GermplasmImportPopupSource {

	public void openSavedGermplasmList(GermplasmList germplasmList);

	public void refreshListTreeAfterListImport();

	public Window getParentWindow();

}
