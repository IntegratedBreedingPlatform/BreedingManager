package org.generationcp.breeding.manager.listimport.actions;

import org.generationcp.middleware.pojos.Germplasm;
import org.springframework.stereotype.Component;

@Component
public class GermplasmBuilder implements Builder<Germplasm,GermplasmDataProvider> {

	@Override
	public Germplasm build(GermplasmDataProvider provider){
		Germplasm germplasm = new Germplasm();

		germplasm.setGid(provider.getGID());
		germplasm.setGnpgs(provider.getProgenitors());
		germplasm.setGpid1(provider.getGPID1());
		germplasm.setGpid2(provider.getGPID2());
		germplasm.setGdate(provider.getDateValue());
		germplasm.setUserId(provider.getUserId());
		germplasm.setLocationId(provider.getLocationId());
		germplasm.setMethodId(provider.getMethodId());
		germplasm.setLgid(provider.getLgid());
		germplasm.setGrplce(provider.getGrplce());
		germplasm.setReferenceId(provider.getReferenceId());
		germplasm.setMgid(provider.getMgid());

		return germplasm;
	}
}
