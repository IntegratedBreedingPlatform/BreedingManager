/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.breeding.manager.util;

import java.util.ArrayList;

import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.pojos.Bibref;
//import org.generationcp.middleware.domain.dms.Study;

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
        return bibRef;
    }

    public void setBibRef(Bibref bibRef) {
        this.bibRef = bibRef;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public String getDateNamed() {
        return dateNamed;
    }

    public void setDateNamed(String dateNamed) {
        this.dateNamed = dateNamed;
    }

    public String getNameLocation() {
        return nameLocation;
    }

    public void setNameLocation(String nameLocation) {
        this.nameLocation = nameLocation;
    }

    public String getGermplasmLocation() {
        return germplasmLocation;
    }

    public void setGermplasmLocation(String germplasmLocation) {
        this.germplasmLocation = germplasmLocation;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getGermplasmUser() {
        return germplasmUser;
    }

    public void setGermplasmUser(String germplasmUser) {
        this.germplasmUser = germplasmUser;
    }

    public String getGermplasmMethod() {
        return germplasmMethod;
    }

    public void setGermplasmMethod(String germplasmMethod) {
        this.germplasmMethod = germplasmMethod;
    }

    public String getGermplasmPreferredName() {
        return germplasmPreferredName;
    }

    public void setGermplasmPreferredName(String germplasmPreferredName) {
        this.germplasmPreferredName = germplasmPreferredName;
    }

    public String getGermplasmCreationDate() {
        return germplasmCreationDate;
    }

    public void setGermplasmCreationDate(String germplasmCreationDate) {
        this.germplasmCreationDate = germplasmCreationDate;
    }

    public String getPrefID() {
        return prefID;
    }

    public void setPrefID(String prefID) {
        this.prefID = prefID;
    }

   
}
