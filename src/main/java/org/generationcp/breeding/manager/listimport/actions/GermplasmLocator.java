package org.generationcp.breeding.manager.listimport.actions;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class GermplasmLocator implements Locator<GermplasmDataProvider,Germplasm>{
	GermplasmDataManager germplasmDataManager;

	@Autowired
	public GermplasmLocator(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	@Override
	public List<Germplasm> locate(GermplasmDataProvider key) {
		List<Germplasm> foundGermplasm;

		if (key.getGid() > 0) {
			Germplasm germplasm = germplasmDataManager.getGermplasmByGID(key.getGid());
			foundGermplasm = new ArrayList<>();
			if(germplasm!=null){
				foundGermplasm.add(germplasm);
			}
		} else{
			// If a single match is found, multiple matches will be handled by SelectGemrplasmWindow and
			// then receiveGermplasmFromWindowAndUpdateGermplasmData()
			foundGermplasm = germplasmDataManager.getGermplasmByName(key.getName(), 0, 1, Operation.EQUAL);
		}
		//Avoid NullPointerException
		return foundGermplasm == null ? new ArrayList<Germplasm>() :foundGermplasm;
	}
}
