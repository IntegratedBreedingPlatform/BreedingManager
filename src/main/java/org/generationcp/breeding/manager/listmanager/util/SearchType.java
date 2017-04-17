package org.generationcp.breeding.manager.listmanager.util;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.middleware.manager.Operation;

public enum SearchType {
	
	STARTS_WITH_KEYWORD(Message.MATCHES_STARTING_WITH, Operation.LIKE)
	, EXACT_MATCH(Message.EXACT_MATCHES, Operation.EQUAL)
	, CONTAINS_KEYWORD(Message.MATCHES_CONTAINING, Operation.LIKE);
	
	private Message label;
	private Operation operation;
	
	private SearchType(Message label, Operation operation){
		this.label = label;
		this.operation = operation;
	}

	public Message getLabel() {
		return label;
	}

	public void setLabel(Message label) {
		this.label = label;
	}
	
	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public static String getSearchKeyword(final String query, final SearchType searchType) {
		final String PERCENT = "%";
		String searchKeyword = query;
		if (STARTS_WITH_KEYWORD.equals(searchType)) {
			searchKeyword = searchKeyword + PERCENT;
		} else if (CONTAINS_KEYWORD.equals(searchType)) {
			searchKeyword = PERCENT + searchKeyword + PERCENT;
		}
		return searchKeyword;
	}
	
	

}
