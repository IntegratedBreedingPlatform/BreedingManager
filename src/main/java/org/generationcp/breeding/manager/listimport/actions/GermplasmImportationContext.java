package org.generationcp.breeding.manager.listimport.actions;

import java.util.HashMap;
import java.util.Map;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.middleware.pojos.Germplasm;

public class GermplasmImportationContext {


	private Map<String, Germplasm> createdGermplasmMap = new HashMap<String, Germplasm>();
	private GermplasmName germplasmNameObject;
	private boolean multipleMatches;
	private int amountMatches;
	private Germplasm germplasm;
	private GermplasmDataProviderImpl provider;


	public GermplasmImportationContext(GermplasmDataProviderImpl provider) {

		this.provider = provider;

	}

	public Map<String, Germplasm>  getCreatedGermplasmMap() {
		return createdGermplasmMap;
	}

	public void setCreatedGermplasmMap(Map<String, Germplasm>  createdGermplasmMap) {
		this.createdGermplasmMap = createdGermplasmMap;
	}

	public GermplasmName getGermplasmNameObject() {
		return germplasmNameObject;
	}

	public void setGermplasmNameObject(GermplasmName germplasmNameObject) {
		this.germplasmNameObject = germplasmNameObject;
	}

	public void setMultipleMatches(boolean multipleMatches) {
		this.multipleMatches = multipleMatches;
	}

	public boolean isMultipleMatches() {
		return multipleMatches;
	}

	public void setAmountMatches(int amountMatches) {
		this.amountMatches = amountMatches;
	}

	public int getAmountMatches() {
		return amountMatches;
	}

	public Germplasm getGermplasm() {
		return germplasm;
	}

	public void setGermplasm(Germplasm germplasm) {
		this.germplasm = germplasm;
	}

	public GermplasmDataProviderImpl getDataProvider() {
		return provider;
	}


}
