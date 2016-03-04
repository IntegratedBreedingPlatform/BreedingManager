package org.generationcp.breeding.manager.listimport.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.pojos.Germplasm;

public class GermplasmRegistrationContext implements NameDataProvider,GermplasmDataProvider{


	private Map<String, Germplasm> createdGermplasmMap = new HashMap<String, Germplasm>();
	private List<GermplasmName> germplasmNameObjects = new ArrayList<GermplasmName>();
	private int userId;
	private int dateIntValue;
	private int locationId;
	private int methodId;
	private ImportedGermplasm importedGermplasm;

	public NameDataProvider getNameDataProvider() {
		return this;
	}


	public void addGermplasmName(GermplasmName germplasmName) {
		germplasmNameObjects.add(germplasmName);
	}

	public Map<String, Germplasm>  getCreatedGermplasmMap() {
		return createdGermplasmMap;
	}

	public void setCreatedGermplasmMap(Map<String, Germplasm>  createdGermplasmMap) {
		this.createdGermplasmMap = createdGermplasmMap;
	}

	public GermplasmDataProvider getGermplasmDataProvider() {
		return this;
	}

	public List<GermplasmName> getGermplasmNameObjects() {
		return germplasmNameObjects;
	}


	// ------------------INTERFACE METHODS----------------------//

	@Override
	public int getGID() {
		return 0;
	}

	@Override
	public int getProgenitors() {
		return 0;
	}

	@Override
	public int getGPID1() {
		return 0;
	}

	@Override
	public int getGPID2() {
		return 0;
	}

	@Override
	public int getGrplce() {
		return 0;
	}

	@Override
	public int getMgid() {
		return 0;
	}

	@Override
	public int getMethodId() {
		return methodId;
	}

	@Override
	public int getLgid() {
		return 0;
	}

	@Override
	public int getUserId() {
		return this.userId;
	}

	@Override
	public int getDateValue() {
		return dateIntValue;
	}

	@Override
	public int getReferenceId() {
		return 0;
	}

	@Override
	public int getLocationId() {
		return locationId;
	}

	@Override
	public int getTypeId() {
		return 0;
	}

	@Override
	public String getName() {
		return importedGermplasm.getDesig();
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public void setDateValue(int dateIntValue) {
		this.dateIntValue = dateIntValue;
	}

	public void setLocationId(int location) {
		this.locationId = location;
	}

	public void setMethodId(int methodId) {
		this.methodId = methodId;
	}

	public void setImportedGermplasm(ImportedGermplasm importedGermplasm) {
		this.importedGermplasm = importedGermplasm;
	}

	public ImportedGermplasm getImportedGermplasm() {
		return importedGermplasm;
	}


}
