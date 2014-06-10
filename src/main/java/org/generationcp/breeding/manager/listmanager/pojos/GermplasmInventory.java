/**
 * 
 */
package org.generationcp.breeding.manager.listmanager.pojos;

import java.io.Serializable;

/**
 * POJO for germplasm inventory
 * @author Darla Ani
 *
 */
public class GermplasmInventory implements Serializable{
	
	private static final long serialVersionUID = -3941830468899799628L;

	private Integer gid;
	private Double balance;
	private String location;
	private String scale;
	private String comment;
	
	public GermplasmInventory(Integer gid, Double balance, String location,
			String scale, String comment) {
		super();
		this.gid = gid;
		this.balance = balance;
		this.location = location;
		this.scale = scale;
		this.comment = comment;
	}

	public Integer getGid() {
		return gid;
	}

	public void setGid(Integer gid) {
		this.gid = gid;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getScale() {
		return scale;
	}

	public void setScale(String scale) {
		this.scale = scale;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gid == null) ? 0 : gid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GermplasmInventory other = (GermplasmInventory) obj;
		if (gid == null) {
			if (other.gid != null)
				return false;
		} else if (!gid.equals(other.gid))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GermplasmInventory [gid=");
		builder.append(gid);
		builder.append(", balance=");
		builder.append(balance);
		builder.append(", location=");
		builder.append(location);
		builder.append(", scale=");
		builder.append(scale);
		builder.append(", comment=");
		builder.append(comment);
		builder.append("]");
		return builder.toString();
	}

	public void addInventory(Double count){
		if (this.balance != null){
			this.balance = this.balance + count;
		} else {
			this.balance = count;
		}
	}


}
