package org.generationcp.breeding.manager.listimport.actions;

import java.util.List;

import org.generationcp.middleware.pojos.Germplasm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConnectProgenitorsToExistingGermplasmProcess implements Executable<GermplasmImportationContext>{

	GermplasmLocator locator;

	@Autowired
	public ConnectProgenitorsToExistingGermplasmProcess(GermplasmLocator locator) {
		this.locator = locator;
	}

	@Override
	public GermplasmImportationContext execute(GermplasmImportationContext context) throws BMSExecutionException {
		List<Germplasm> foundGermplasmList = null;

		GermplasmDataProviderImpl provider = context.getDataProvider();
		foundGermplasmList = locator.locate(provider);
		context.setAmountMatches(foundGermplasmList.size());

		context.setMultipleMatches(foundGermplasmList.size()>1);

		if(foundGermplasmList.size()==1){
			Germplasm foundGermplasm = foundGermplasmList.get(0);
			if (provider.getProgenitors() == -1) {
				if (foundGermplasm.getGpid1() == 0) {
					provider.setFemaleParent(foundGermplasm.getGid());
				} else {
					provider.setFemaleParent(foundGermplasm.getGpid1());
				}
			}else {
				provider.setFemaleParent(foundGermplasm.getGid());
			}

			provider.setMaleParent(foundGermplasm.getGid());

		}
		return context;
	}
}
