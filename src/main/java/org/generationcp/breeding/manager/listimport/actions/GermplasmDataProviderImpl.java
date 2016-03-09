package org.generationcp.breeding.manager.listimport.actions;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.commons.util.DateUtil;

public class GermplasmDataProviderImpl implements GermplasmDataProvider {

	private int userId;
	private int dateIntValue;
	private int methodId;
	private int progenitors;
	private ImportedGermplasm importedGermplasm;
	private int femaleParent = 0;
	private int maleParent = 0;
	private int locationId;

	private int typeId;
	private int nstat;
	private int dateValue;

	public int getGid() {
		boolean isNull = (importedGermplasm == null || importedGermplasm.getGid() == null);
		return isNull ?  0 : importedGermplasm.getGid();
	}

	public int getNameDateValue() {
		return DateUtil.getCurrentDateAsIntegerValue();
	}

	public int getTypeId() {
		return this.typeId;
	}

	public String getName() {
		return importedGermplasm == null ? null : importedGermplasm.getDesig();
	}

	public int getNstat() {
		return this.nstat;
	}

	public int getProgenitors() {
		return progenitors;
	}

	public int getFemaleParent() {
		return femaleParent;
	}

	public int getMaleParent() {
		return maleParent;
	}

	public int getGrplce() {
		return 0;
	}

	public int getReferenceId() {
		return 0;
	}

	public int getMgid() {
		return 0;
	}

	public int getLocationId() {
		return locationId;
	}

	public int getMethodId() {
		return methodId;
	}

	public int getLgid() {
		return 0;
	}

	public int getUserId() {
		return this.userId;
	}

	public int getDateValue() {
		return dateIntValue;
	}

	public ImportedGermplasm getImportedGermplasm() {
		return importedGermplasm;
	}

	public void setMethodId(int methodId) {
		this.methodId = methodId;
	}

	public void setImportedGermplasm(ImportedGermplasm importedGermplasm) {
		this.importedGermplasm = importedGermplasm;
	}

	public void setFemaleParent(int femaleParent) {
		this.femaleParent = femaleParent;
	}

	public void setMaleParent(int maleParent) {
		this.maleParent = maleParent;
	}

	public void setProgenitors(int progenitors) {
		this.progenitors = progenitors;
	}

	public void setLocationId(int location) {
		this.locationId = location;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public void setNstat(int nstat) {
		this.nstat = nstat;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public void setDateValue(int dateValue) {
		this.dateIntValue = dateValue;
	}
}
