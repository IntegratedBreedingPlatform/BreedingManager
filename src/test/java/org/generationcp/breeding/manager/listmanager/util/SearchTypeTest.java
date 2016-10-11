package org.generationcp.breeding.manager.listmanager.util;

import org.junit.Test;

import junit.framework.Assert;

public class SearchTypeTest {
	
	private static final String DUMMY_SEARCH_STRING = "Search String";
	
	@Test
	public void testGetSearchKeywordForStartsWithSearchType(){
		String searchKeyword = SearchType.getSearchKeyword(DUMMY_SEARCH_STRING, SearchType.STARTS_WITH_KEYWORD);
		Assert.assertEquals(DUMMY_SEARCH_STRING + "%", searchKeyword);
	}
	
	@Test
	public void testGetSearchKeywordForExactMatchSearchType(){
		String searchKeyword = SearchType.getSearchKeyword(DUMMY_SEARCH_STRING, SearchType.EXACT_MATCH);
		Assert.assertEquals(DUMMY_SEARCH_STRING, searchKeyword);
	}
	
	@Test
	public void testGetSearchKeywordForContainsSearchType(){
		String searchKeyword = SearchType.getSearchKeyword(DUMMY_SEARCH_STRING, SearchType.CONTAINS_KEYWORD);
		Assert.assertEquals("%" + DUMMY_SEARCH_STRING + "%", searchKeyword);
	}

}
