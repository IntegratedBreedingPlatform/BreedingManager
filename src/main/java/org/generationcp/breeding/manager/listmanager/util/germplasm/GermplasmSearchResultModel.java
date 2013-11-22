package org.generationcp.breeding.manager.listmanager.util.germplasm;

import java.io.Serializable;

@SuppressWarnings("serial")
public class GermplasmSearchResultModel implements Serializable{

    private int gid;
    private String names;
    private String method;
    private String location;

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

