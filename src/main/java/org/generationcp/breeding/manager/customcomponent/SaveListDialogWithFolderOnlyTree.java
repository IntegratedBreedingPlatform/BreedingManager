
package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.middleware.pojos.GermplasmList;

public class SaveListDialogWithFolderOnlyTree extends SaveListAsDialog {

	private static final long serialVersionUID = -1143414154864474334L;

	public SaveListDialogWithFolderOnlyTree(SaveListAsDialogSource source, GermplasmList germplasmList) {
		super(source, germplasmList);
	}

	@Override
	protected boolean isShowFoldersOnlyInListTree() {
		return true;
	}

}
