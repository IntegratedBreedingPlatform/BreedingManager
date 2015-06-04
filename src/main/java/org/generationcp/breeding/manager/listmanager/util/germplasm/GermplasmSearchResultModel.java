
package org.generationcp.breeding.manager.listmanager.util.germplasm;

import java.io.Serializable;

@SuppressWarnings("serial")
public class GermplasmSearchResultModel implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8757347154926736923L;
	private int gid;
	private String names;
	private String method;
	private String location;

	public Integer getGid() {
		return this.gid;
	}

	public void setGid(Integer gid) {
		this.gid = gid;
	}

	public String getNames() {
		return this.names;
	}

	public void setNames(String names) {
		this.names = names;
	}

	public String getMethod() {
		return this.method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setGid(int gid) {
		this.gid = gid;
	}

}
