
package org.generationcp.breeding.manager.pojos;

import java.util.HashMap;
import java.util.Map;

public class ImportedGermplasm {

	private Integer gid;
	private Integer entryId;
	private String desig;
	private String cross;
	private String source;
	private String entryCode;

	/** also the seed quantity */
	private Double seedAmount;

	/** This is also the stockId */
	private String inventoryId;
	private Map<String, String> attributeVariates;
	private Map<String, String> nameFactors;

	public ImportedGermplasm() {
		this.attributeVariates = new HashMap<String, String>();
		this.nameFactors = new HashMap<String, String>();
	}

	public ImportedGermplasm(Integer entryId, String desig) {
		this.entryId = entryId;
		this.desig = desig;
		this.attributeVariates = new HashMap<String, String>();
		this.nameFactors = new HashMap<String, String>();
	}

	public Integer getEntryId() {
		return this.entryId;
	}

	public void setEntryId(Integer entryId) {
		this.entryId = entryId;
	}

	public String getDesig() {
		return this.desig;
	}

	public void setDesig(String desig) {
		this.desig = desig;
	}

	public void setGid(Integer gid) {
		this.gid = gid;
	}

	public Integer getGid() {
		return this.gid;
	}

	public void setCross(String cross) {
		this.cross = cross;
	}

	public String getCross() {
		return this.cross;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		if(source == null) return "";
		else return this.source;
	}

	public void setEntryCode(String entryCode) {
		this.entryCode = entryCode;
	}

	public String getEntryCode() {
		return this.entryCode;
	}

	public void setSeedAmount(Double seedAmount) {
		this.seedAmount = seedAmount;
	}

	public Double getSeedAmount() {
		return this.seedAmount;
	}

	public Map<String, String> getAttributeVariates() {
		return this.attributeVariates;
	}

	public void setAttributeVariates(Map<String, String> variatesMap) {
		this.attributeVariates = variatesMap;
	}

	public void addAttributeVariate(String name, String value) {
		this.attributeVariates.put(name, value);
	}

	public Map<String, String> getNameFactors() {
		return this.nameFactors;
	}

	public void setNameFactors(Map<String, String> nameFactors) {
		this.nameFactors = nameFactors;
	}

	public void addNameFactor(String name, String value) {
		this.nameFactors.put(name, value);
	}

	public String getInventoryId() {
		return this.inventoryId;
	}

	public void setInventoryId(String inventoryId) {
		this.inventoryId = inventoryId;
	}

}
