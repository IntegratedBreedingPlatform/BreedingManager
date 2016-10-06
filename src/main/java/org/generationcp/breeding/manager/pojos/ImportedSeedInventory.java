package org.generationcp.breeding.manager.pojos;


public class ImportedSeedInventory {

	private Integer entry;
	private String designation;
	private Integer gid;
	private Integer lotID;
	private Integer transactionId;
	private Double reservationAmount;
	private Double withdrawalAmount;
	private Double balanceAmount;
	private String comments;

	private String transactionProcessingStatus;

	public ImportedSeedInventory(){

	}

	public ImportedSeedInventory(Integer entry, String designation, Integer gid, Integer lotID, Integer transactionId,
			Double reservationAmount, Double withdrawalAmount, Double balanceAmount, String comments) {
		this.entry = entry;
		this.designation = designation;
		this.gid = gid;
		this.lotID = lotID;
		this.transactionId = transactionId;
		this.reservationAmount = reservationAmount;
		this.withdrawalAmount = withdrawalAmount;
		this.balanceAmount = balanceAmount;
		this.comments = comments;
	}

	public Integer getEntry() {
		return entry;
	}

	public void setEntry(Integer entry) {
		this.entry = entry;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public Integer getGid() {
		return gid;
	}

	public void setGid(Integer gid) {
		this.gid = gid;
	}

	public Integer getLotID() {
		return lotID;
	}

	public void setLotID(Integer lotID) {
		this.lotID = lotID;
	}

	public Integer getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Integer transactionId) {
		this.transactionId = transactionId;
	}

	public Double getReservationAmount() {
		return reservationAmount;
	}

	public void setReservationAmount(Double reservationAmount) {
		this.reservationAmount = reservationAmount;
	}

	public Double getWithdrawalAmount() {
		return withdrawalAmount;
	}

	public void setWithdrawalAmount(Double withdrawalAmount) {
		this.withdrawalAmount = withdrawalAmount;
	}

	public Double getBalanceAmount() {
		return balanceAmount;
	}

	public void setBalanceAmount(Double balanceAmount) {
		this.balanceAmount = balanceAmount;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getTransactionProcessingStatus() {
		return transactionProcessingStatus;
	}

	public void setTransactionProcessingStatus(String transactionProcessingStatus) {
		this.transactionProcessingStatus = transactionProcessingStatus;
	}
}
