package org.generationcp.breeding.manager.listimport.actions;

import org.generationcp.middleware.pojos.Germplasm;

public class GermplasmBuilderImpl implements GermplasmBuilder{

	public Germplasm build(GermplasmDataProvider provider){
		Germplasm germplasm = new Germplasm();
		germplasm.setGid(provider.getGID());
		return germplasm;
	}

}
