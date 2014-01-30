package org.generationcp.browser.util.awhere.json.pojos;

import java.io.Serializable;

public class TenYearAverage implements Serializable {

	private static final long serialVersionUID = 7100062902807705571L;

	private String PeriodFrom;
	private String PeriodTo;
	private double Avg_MinTemp;
	private double Avg_MaxTemp;
	private double Avg_Solar;
	private double Avg_Rain;
	private double Total_GDD;
	private int Count;
	
	public TenYearAverage(String periodFrom, String periodTo,
			double avg_MinTemp, double avg_MaxTemp, double avg_Solar,
			double avg_Rain, double total_GDD, int count) {
		super();
		this.PeriodFrom = periodFrom;
		this.PeriodTo = periodTo;
		this.Avg_MinTemp = avg_MinTemp;
		this.Avg_MaxTemp = avg_MaxTemp;
		this.Avg_Solar = avg_Solar;
		this.Avg_Rain = avg_Rain;
		this.Total_GDD = total_GDD;
		this.Count = count;
	}

	public String getPeriodFrom() {
		return PeriodFrom;
	}

	public void setPeriodFrom(String periodFrom) {
		this.PeriodFrom = periodFrom;
	}

	public String getPeriodTo() {
		return PeriodTo;
	}

	public void setPeriodTo(String periodTo) {
		this.PeriodTo = periodTo;
	}

	public double getAvg_MinTemp() {
		return Avg_MinTemp;
	}

	public void setAvg_MinTemp(double avg_MinTemp) {
		this.Avg_MinTemp = avg_MinTemp;
	}

	public double getAvg_MaxTemp() {
		return Avg_MaxTemp;
	}

	public void setAvg_MaxTemp(double avg_MaxTemp) {
		this.Avg_MaxTemp = avg_MaxTemp;
	}

	public double getAvg_Solar() {
		return Avg_Solar;
	}

	public void setAvg_Solar(double avg_Solar) {
		this.Avg_Solar = avg_Solar;
	}

	public double getAvg_Rain() {
		return Avg_Rain;
	}

	public void setAvg_Rain(double avg_Rain) {
		this.Avg_Rain = avg_Rain;
	}

	public double getTotal_GDD() {
		return Total_GDD;
	}

	public void setTotal_GDD(double total_GDD) {
		this.Total_GDD = total_GDD;
	}

	public int getCount() {
		return Count;
	}

	public void setCount(int count) {
		this.Count = count;
	}

	@Override
	public String toString() {
		return "TenYearAverage [periodFrom=" + PeriodFrom + ", periodTo="
				+ PeriodTo + ", avg_MinTemp=" + Avg_MinTemp + ", avg_MaxTemp="
				+ Avg_MaxTemp + ", avg_Solar=" + Avg_Solar + ", avg_Rain="
				+ Avg_Rain + ", total_GDD=" + Total_GDD + ", count=" + Count
				+ "]";
	}
	
}
