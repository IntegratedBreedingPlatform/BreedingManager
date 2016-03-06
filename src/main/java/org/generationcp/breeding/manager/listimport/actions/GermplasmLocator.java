package org.generationcp.breeding.manager.listimport.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class GermplasmLocator implements Locator<ImportedGermplasm,Germplasm>{
	GermplasmDataManager germplasmDataManager;

	@Autowired
	public GermplasmLocator(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	@Override
	public List<Germplasm> locate(ImportedGermplasm key) throws NoElementFoundException{
		List<Germplasm> foundGermplasm = new ArrayList<>();

		if (key.getGid()!=null && key.getGid()>0) {
			foundGermplasm.add(germplasmDataManager.getGermplasmByGID(key.getGid()));
		} else{
			/**
			 * Original code first check if there was a match in the database and then compare is that match was equal to one
			 * leaving the case of more than 1 matches in the database to be handle the same way as no match at all.
			 * That comparison was eliminates as it is belived to be a bug.
			 * */
			// If a single match is found, multiple matches will be handled by SelectGemrplasmWindow and
			// then receiveGermplasmFromWindowAndUpdateGermplasmData()
			foundGermplasm = germplasmDataManager.getGermplasmByName(key.getDesig(), 0, 1, Operation.EQUAL);
		}

		if(CollectionUtils.isEmpty(foundGermplasm)){
			throw new NoElementFoundException();
		}

		return foundGermplasm;
	}
}
