
package org.generationcp.breeding.manager.cross.study.h2h.pojos;

import java.io.Serializable;

public class Result implements Serializable {

	private static final long serialVersionUID = -4278317924920918815L;

	private String traitName;
	private Integer numberOfEnvironments;
	private Integer numberOfSup;
	private Double meanStd;
	private Double meanTest;
	private Double meanDiff;
	private Double pval;

	public Result(String traitName, Integer numberOfEnvironments, Integer numberOfSup, Double meanStd, Double meanTest, Double meanDiff,
			Double pval) {
		super();
		this.traitName = traitName;
		this.numberOfEnvironments = numberOfEnvironments;
		this.numberOfSup = numberOfSup;
		this.meanStd = meanStd;
		this.meanTest = meanTest;
		this.meanDiff = meanDiff;
		this.pval = pval;
	}

	public String getTraitName() {
		return this.traitName;
	}

	public void setTraitName(String traitName) {
		this.traitName = traitName;
	}

	public Integer getNumberOfEnvironments() {
		return this.numberOfEnvironments;
	}

	public void setNumberOfEnvironments(Integer numberOfEnvironments) {
		this.numberOfEnvironments = numberOfEnvironments;
	}

	public Integer getNumberOfSup() {
		return this.numberOfSup;
	}

	public void setNumberOfSup(Integer numberOfSup) {
		this.numberOfSup = numberOfSup;
	}

	public Double getMeanStd() {
		return this.meanStd;
	}

	public void setMeanStd(Double meanStd) {
		this.meanStd = meanStd;
	}

	public Double getMeanTest() {
		return this.meanTest;
	}

	public void setMeanTest(Double meanTest) {
		this.meanTest = meanTest;
	}

	public Double getMeanDiff() {
		return this.meanDiff;
	}

	public void setMeanDiff(Double meanDiff) {
		this.meanDiff = meanDiff;
	}

	public Double getPval() {
		return this.pval;
	}

	public void setPval(Double pval) {
		this.pval = pval;
	}
}
