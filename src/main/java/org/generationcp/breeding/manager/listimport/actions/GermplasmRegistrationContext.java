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
	private GermplasmName germplasmNameObject;
	private int userId;
	private int dateIntValue;
	private int locationId;
	private int methodId;
	private int nstat;
	private int progenitors;
	private int typeId;
	private ImportedGermplasm importedGermplasm;

	public NameDataProvider getNameDataProvider() {
		return this;
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


	public GermplasmName getGermplasmNameObject() {
		return germplasmNameObject;
	}

	public void setGermplasmNameObject(GermplasmName germplasmNameObject) {
		this.germplasmNameObject = germplasmNameObject;
	}

	// ------------------INTERFACE METHODS----------------------//

	@Override
	public int getGID() {
		return 0;
	}

	@Override
	public int getProgenitors() {
		return progenitors;
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
		return typeId;
	}

	@Override
	public String getName() {
		return importedGermplasm.getDesig();
	}

	@Override
	public int getNstat() {
		return nstat;
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
	public void setNstat(int nstat) {
		this.nstat = nstat;
	}

	public void setProgenitors(int progenitors) {
		this.progenitors = progenitors;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
}
