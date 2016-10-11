
package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.util.SearchType;
import org.generationcp.breeding.manager.service.BreedingManagerSearchException;
import org.generationcp.breeding.manager.service.BreedingManagerServiceImpl;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmSearchBarComponentTest {

	@Mock
	private GermplasmSearchResultsComponent germplasmSearchResultsComponent;

	@Mock
	private BreedingManagerServiceImpl breedingManagerService;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private PlatformTransactionManager transactionManager;

	@Mock
	private Component component;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@InjectMocks
	private GermplasmSearchBarComponent germplasmSearchBarComponent;

	private static final String TEST_SEARCH_STRING = "1234567";

	@Before
	public void setUp() throws BreedingManagerSearchException {
		Mockito.when(this.messageSource.getMessage(Message.MATCHES_STARTING_WITH)).thenReturn("Matches starting with");
		Mockito.when(this.messageSource.getMessage(Message.EXACT_MATCHES)).thenReturn("Exact Matches");
		Mockito.when(this.messageSource.getMessage(Message.MATCHES_CONTAINING)).thenReturn("Matches containing");
		Mockito.when(this.messageSource.getMessage(Message.SEARCH_QUERY_CANNOT_BE_EMPTY)).thenReturn("Search query cannot be empty");

		this.breedingManagerService.setGermplasmDataManager(this.germplasmDataManager);

		this.germplasmSearchBarComponent = new GermplasmSearchBarComponent(this.germplasmSearchResultsComponent);
		this.germplasmSearchBarComponent.setMessageSource(this.messageSource);
		this.germplasmSearchBarComponent.setBreedingManagerService(this.breedingManagerService);
		this.germplasmSearchBarComponent.setTransactionManager(this.transactionManager);
		this.germplasmSearchBarComponent.instantiateComponents();

		this.germplasmSearchBarComponent.setParent(this.component);
		Mockito.when(this.component.getWindow()).thenReturn(new Window());

		Mockito.doNothing().when(this.germplasmSearchResultsComponent).applyGermplasmResults(Matchers.any(GermplasmSearchParameter.class));
	}

	@Test
	public void testDoSearchUsingStartsWithKeywordSearchType() throws Exception {
		// Call method to test
		this.germplasmSearchBarComponent.doSearch(GermplasmSearchBarComponentTest.TEST_SEARCH_STRING);

		// Verify that "%" was added to end of search string and LIKE operation was used in Middleware call
		final ArgumentCaptor<GermplasmSearchParameter> searchArgument = ArgumentCaptor.forClass(GermplasmSearchParameter.class);
		Mockito.verify(this.germplasmSearchResultsComponent, Mockito.times(1)).applyGermplasmResults(searchArgument.capture());
		Assert.assertEquals(GermplasmSearchBarComponentTest.TEST_SEARCH_STRING + "%", searchArgument.getValue().getSearchKeyword());
		Assert.assertEquals(Operation.LIKE, searchArgument.getValue().getOperation());
	}

	@Test
	public void testDoSearchUsingExactMatchSearchType() throws Exception {
		// Set mocks and set search mode = "Exact match"
		this.germplasmSearchBarComponent.setSearchType(SearchType.EXACT_MATCH);

		// Call method to test
		this.germplasmSearchBarComponent.doSearch(GermplasmSearchBarComponentTest.TEST_SEARCH_STRING);

		// Verify no wildcard character "%" was added to search string and EQUAL operation was used in Middleware call
		final ArgumentCaptor<GermplasmSearchParameter> searchArgument = ArgumentCaptor.forClass(GermplasmSearchParameter.class);
		Mockito.verify(this.germplasmSearchResultsComponent, Mockito.times(1)).applyGermplasmResults(searchArgument.capture());
		Assert.assertEquals(GermplasmSearchBarComponentTest.TEST_SEARCH_STRING, searchArgument.getValue().getSearchKeyword());
		Assert.assertEquals(Operation.EQUAL, searchArgument.getValue().getOperation());

	}

	@Test
	public void testDoSearchUsingContainsKeywordSearchType() throws Exception {
		// Set mocks and set search mode = "Contains Keyword"
		this.germplasmSearchBarComponent.setSearchType(SearchType.CONTAINS_KEYWORD);

		// Call method to test
		this.germplasmSearchBarComponent.doSearch(GermplasmSearchBarComponentTest.TEST_SEARCH_STRING);

		// Verify that "%" was added to ends of search string and LIKE operation was used in Middleware call
		final ArgumentCaptor<GermplasmSearchParameter> searchArgument = ArgumentCaptor.forClass(GermplasmSearchParameter.class);
		Mockito.verify(this.germplasmSearchResultsComponent, Mockito.times(1)).applyGermplasmResults(searchArgument.capture());
		Assert.assertEquals("%" + GermplasmSearchBarComponentTest.TEST_SEARCH_STRING + "%", searchArgument.getValue().getSearchKeyword());
		Assert.assertEquals(Operation.LIKE, searchArgument.getValue().getOperation());

	}

	@Test
	public void testDoSearchWithEmptySearchString() throws BreedingManagerSearchException {
		Mockito.doThrow(new BreedingManagerSearchException(Message.SEARCH_QUERY_CANNOT_BE_EMPTY)).when(this.breedingManagerService)
				.validateEmptySearchString("");

		this.germplasmSearchBarComponent.doSearch("");

		Mockito.verify(this.messageSource).getMessage(Message.UNABLE_TO_SEARCH);
	}

	@Test
	public void testDoSearchWithDatabaseError() throws BreedingManagerSearchException {
		Mockito.doThrow(new BreedingManagerSearchException(Message.ERROR_DATABASE)).when(this.breedingManagerService)
				.validateEmptySearchString("");

		this.germplasmSearchBarComponent.doSearch("");

		Mockito.verify(this.messageSource).getMessage(Message.SEARCH_RESULTS);
	}

	@Test
	public void testSearchButtonClickActionUsingDefaultSearchType() throws BreedingManagerSearchException {
		this.germplasmSearchBarComponent.searchButtonClickAction();

		// Verify that germplasm search was called directly (ie. no confirm dialog shown)
		Mockito.verify(this.breedingManagerService, Mockito.times(1)).validateEmptySearchString(Matchers.anyString());
	}

	@Test
	public void testSearchButtonClickActionUsingContainsKeywordSearchType() throws BreedingManagerSearchException {
		this.germplasmSearchBarComponent.setSearchType(SearchType.CONTAINS_KEYWORD);
		Mockito.doReturn("Some String").when(this.messageSource).getMessage(Matchers.any(Message.class));

		this.germplasmSearchBarComponent.searchButtonClickAction();

		// Verify that germplasm search was not called since Confirm Dialog is shown first
		Mockito.verify(this.breedingManagerService).setGermplasmDataManager(this.germplasmDataManager);
		Mockito.verifyNoMoreInteractions(this.breedingManagerService);
	}
}
