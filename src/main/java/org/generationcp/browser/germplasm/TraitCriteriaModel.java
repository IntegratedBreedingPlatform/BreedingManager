/***************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 **************************************************************/

package org.generationcp.browser.germplasm;

public class TraitCriteriaModel{

    int traitID;
    int scaleID;
    int methodID;

    String traitName;
    String scaleName;
    String methodName;
    String criteriaValues;

    public int getTraitID() {
	return traitID;
    }

    public void setTraitID(int traitID) {
	this.traitID = traitID;
    }

    public int getScaleID() {
	return scaleID;
    }

    public void setScaleID(int scaleID) {
	this.scaleID = scaleID;
    }

    public int getMethodID() {
	return methodID;
    }

    public void setMethodID(int methodID) {
	this.methodID = methodID;
    }

    public String getTraitName() {
	return traitName;
    }

    public void setTraitName(String traitName) {
	this.traitName = traitName;
    }

    public String getScaleName() {
	return scaleName;
    }

    public void setScaleName(String scaleName) {
	this.scaleName = scaleName;
    }

    public String getMethodName() {
	return methodName;
    }

    public void setMethodName(String methodName) {
	this.methodName = methodName;
    }

    public String getCriteriaValues() {
	return criteriaValues;
    }

    public void setCriteriaValues(String criteriaValues) {
	this.criteriaValues = criteriaValues;
    }

}
