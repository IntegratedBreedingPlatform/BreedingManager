package org.generationcp.breeding.manager.listimport.actions;

import org.generationcp.middleware.pojos.Germplasm;

public class GermplasmBuilderImpl implements GermplasmBuilder{

	public Germplasm build(GermplasmDataProvider provider){
		Germplasm germplasm = new Germplasm();

		germplasm.setGid(provider.getGID());
		germplasm.setGnpgs(provider.getProgenitors());
		germplasm.setGpid1(provider.getGPID1());
		germplasm.setGnpgs(provider.getGPID2());
		germplasm.setUserId(provider.getDateValue());
		germplasm.setUserId(provider.getUserId());
		germplasm.setLocationId(provider.getLocationId());
		germplasm.setGdate(provider.getDateValue());
		germplasm.setMethodId(provider.getMethodId());
		germplasm.setLgid(provider.getLgid());
		germplasm.setGrplce(provider.getGrplce());
		germplasm.setReferenceId(provider.getReferenceId());
		germplasm.setMgid(provider.getMgid());

		return germplasm;
	}

}
