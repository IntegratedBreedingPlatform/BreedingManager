
package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.middleware.pojos.GermplasmList;

public class SaveCrossListAsDialog extends SaveListAsDialog {

	private static final long serialVersionUID = -4151286394925054516L;

	public SaveCrossListAsDialog(final SaveListAsDialogSource source, final GermplasmList germplasmList) {
		super(source, germplasmList);
	}

	@Override
	public void initializeValues() {
		super.initializeValues();
		this.getDetailsComponent().getListTypeField().setValue("F1");
	}

	@Override
	public String defaultListType() {
		return "F1";
	}

}
