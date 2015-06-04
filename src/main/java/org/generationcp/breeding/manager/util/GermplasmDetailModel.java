/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.breeding.manager.util;

import org.generationcp.middleware.pojos.Bibref;

public class GermplasmDetailModel {

	private int gid;
	String germplasmPreferredName; // nval value where names.nstat=1
	private String dateNamed; // ndate value where names.nstat=1
	private String nameLocation;
	private String germplasmMethod;
	private String germplasmCreationDate;
	private String germplasmLocation;
	private String reference;
	private String germplasmUser;
	private String prefID; // nval value where names.nstat=8

	private Bibref bibRef;

	public Bibref getBibRef() {
		return this.bibRef;
	}

	public void setBibRef(Bibref bibRef) {
		this.bibRef = bibRef;
	}

	public int getGid() {
		return this.gid;
	}

	public void setGid(int gid) {
		this.gid = gid;
	}

	public String getDateNamed() {
		return this.dateNamed;
	}

	public void setDateNamed(String dateNamed) {
		this.dateNamed = dateNamed;
	}

	public String getNameLocation() {
		return this.nameLocation;
	}

	public void setNameLocation(String nameLocation) {
		this.nameLocation = nameLocation;
	}

	public String getGermplasmLocation() {
		return this.germplasmLocation;
	}

	public void setGermplasmLocation(String germplasmLocation) {
		this.germplasmLocation = germplasmLocation;
	}

	public String getReference() {
		return this.reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getGermplasmUser() {
		return this.germplasmUser;
	}

	public void setGermplasmUser(String germplasmUser) {
		this.germplasmUser = germplasmUser;
	}

	public String getGermplasmMethod() {
		return this.germplasmMethod;
	}

	public void setGermplasmMethod(String germplasmMethod) {
		this.germplasmMethod = germplasmMethod;
	}

	public String getGermplasmPreferredName() {
		return this.germplasmPreferredName;
	}

	public void setGermplasmPreferredName(String germplasmPreferredName) {
		this.germplasmPreferredName = germplasmPreferredName;
	}

	public String getGermplasmCreationDate() {
		return this.germplasmCreationDate;
	}

	public void setGermplasmCreationDate(String germplasmCreationDate) {
		this.germplasmCreationDate = germplasmCreationDate;
	}

	public String getPrefID() {
		return this.prefID;
	}

	public void setPrefID(String prefID) {
		this.prefID = prefID;
	}

}
