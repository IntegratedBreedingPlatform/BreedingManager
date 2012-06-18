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

import java.io.Serializable;

@SuppressWarnings("serial")
public class GermplasmSearchResultModel implements Serializable{

    int gid;
    String names;
    String method;
    String location;

    public Integer getGid() {
	return gid;
    }

    public void setGid(Integer gid) {
	this.gid = gid;
    }

    public String getNames() {
	return names;
    }

    public void setNames(String names) {
	this.names = names;
    }

    public String getMethod() {
	return method;
    }

    public void setMethod(String method) {
	this.method = method;
    }

    public String getLocation() {
	return location;
    }

    public void setLocation(String location) {
	this.location = location;
    }

    public void setGid(int gid) {
	this.gid = gid;
    }

}
