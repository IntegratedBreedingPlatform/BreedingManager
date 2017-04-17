
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.util.SearchType;
import org.generationcp.breeding.manager.service.BreedingManagerSearchException;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class ListSearchBarComponentTest {

	private static final String DUMMY_SEARCH_STRING = "Search String";

	@Mock
	private BreedingManagerService breedingManagerService;

	@Mock
	private ListSearchResultsComponent searchResultsComponent;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private Component parent;

	@Mock
	private Window parentWindow;

	@InjectMocks
	private ListSearchBarComponent listSearchBarComponent;

	@Before
	public void setUp() {
		this.listSearchBarComponent.setBreedingManagerService(this.breedingManagerService);
		this.listSearchBarComponent.setMessageSource(this.messageSource);
		this.listSearchBarComponent.setParent(this.parent);

		Mockito.doReturn(this.parentWindow).when(this.parent).getWindow();
		Mockito.doReturn(new Window()).when(this.parentWindow).getParent();

		this.listSearchBarComponent.instantiateComponents();
	}

	@Test
	public void testDoSearchUsingStartsWithKeywordSearchType() throws Exception {
		// Setup mocks
		final List<GermplasmList> germplasmLists = this.setupDummyListsToReturnFromMiddleware();

		// Call method to test
		this.listSearchBarComponent.doSearch(ListSearchBarComponentTest.DUMMY_SEARCH_STRING);

		// Verify that "%" was added to end of search string and LIKE operation was used in Middleware call
		final ArgumentCaptor<String> searchStringArgument = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Operation> operationArgument = ArgumentCaptor.forClass(Operation.class);
		Mockito.verify(this.breedingManagerService, Mockito.times(1)).doGermplasmListSearch(searchStringArgument.capture(),
				operationArgument.capture());
		Assert.assertEquals(ListSearchBarComponentTest.DUMMY_SEARCH_STRING + "%", searchStringArgument.getValue());
		Assert.assertEquals(Operation.LIKE, operationArgument.getValue());

		// Verify that results were applied to searchResultsComponent
		Mockito.verify(this.searchResultsComponent, Mockito.times(1)).applyGermplasmListResults(germplasmLists);

	}

	@Test
	public void testDoSearchUsingExactMatchSearchType() throws Exception {
		// Set mocks and set search mode = "Exact match"
		this.listSearchBarComponent.setSearchType(SearchType.EXACT_MATCH);
		final List<GermplasmList> germplasmLists = this.setupDummyListsToReturnFromMiddleware();

		// Call method to test
		this.listSearchBarComponent.doSearch(ListSearchBarComponentTest.DUMMY_SEARCH_STRING);

		// Verify no wildcard character "%" was added to search string and EQUAL operation was used in Middleware call
		final ArgumentCaptor<String> searchStringArgument = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Operation> operationArgument = ArgumentCaptor.forClass(Operation.class);
		Mockito.verify(this.breedingManagerService, Mockito.times(1)).doGermplasmListSearch(searchStringArgument.capture(),
				operationArgument.capture());
		Assert.assertEquals(ListSearchBarComponentTest.DUMMY_SEARCH_STRING, searchStringArgument.getValue());
		Assert.assertEquals(Operation.EQUAL, operationArgument.getValue());

		// Verify that results were applied to searchResultsComponent
		Mockito.verify(this.searchResultsComponent, Mockito.times(1)).applyGermplasmListResults(germplasmLists);

	}

	@Test
	public void testDoSearchUsingContainsKeywordSearchType() throws Exception {
		// Set mocks and set search mode = "Contains Keyword"
		this.listSearchBarComponent.setSearchType(SearchType.CONTAINS_KEYWORD);
		final List<GermplasmList> germplasmLists = this.setupDummyListsToReturnFromMiddleware();

		// Call method to test
		this.listSearchBarComponent.doSearch(ListSearchBarComponentTest.DUMMY_SEARCH_STRING);

		// Verify that "%" was added to ends of search string and LIKE operation was used in Middleware call
		final ArgumentCaptor<String> searchStringArgument = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Operation> operationArgument = ArgumentCaptor.forClass(Operation.class);
		Mockito.verify(this.breedingManagerService, Mockito.times(1)).doGermplasmListSearch(searchStringArgument.capture(),
				operationArgument.capture());
		Assert.assertEquals("%" + ListSearchBarComponentTest.DUMMY_SEARCH_STRING + "%", searchStringArgument.getValue());
		Assert.assertEquals(Operation.LIKE, operationArgument.getValue());

		// Verify that results were applied to searchResultsComponent
		Mockito.verify(this.searchResultsComponent, Mockito.times(1)).applyGermplasmListResults(germplasmLists);

	}

	private List<GermplasmList> setupDummyListsToReturnFromMiddleware() throws BreedingManagerSearchException {
		final List<GermplasmList> germplasmLists = new ArrayList<GermplasmList>();
		germplasmLists.add(new GermplasmList());
		Mockito.doReturn(germplasmLists).when(this.breedingManagerService).doGermplasmListSearch(Matchers.anyString(),
				Matchers.any(Operation.class));
		Mockito.doNothing().when(this.searchResultsComponent).applyGermplasmListResults(germplasmLists);
		return germplasmLists;
	}

	@Test
	public void testDoSearchNoSearchResults() throws Exception {
		final List<GermplasmList> germplasmLists = new ArrayList<GermplasmList>();
		Mockito.doThrow(new BreedingManagerSearchException(Message.NO_SEARCH_RESULTS)).when(this.breedingManagerService)
				.doGermplasmListSearch(Matchers.anyString(), Matchers.any(Operation.class));
		Mockito.doNothing().when(this.searchResultsComponent).applyGermplasmListResults(germplasmLists);

		this.listSearchBarComponent.doSearch(ListSearchBarComponentTest.DUMMY_SEARCH_STRING);

		// Verify that applyGermplasmListResults still called with empty list as parameter
		Mockito.verify(this.searchResultsComponent, Mockito.times(1)).applyGermplasmListResults(germplasmLists);
	}

	@Test
	public void testDoSearchDbError() throws Exception {
		final List<GermplasmList> germplasmLists = new ArrayList<GermplasmList>();
		Mockito.doThrow(new BreedingManagerSearchException(Message.ERROR_DATABASE)).when(this.breedingManagerService)
				.doGermplasmListSearch(Matchers.anyString(), Matchers.any(Operation.class));

		this.listSearchBarComponent.doSearch(ListSearchBarComponentTest.DUMMY_SEARCH_STRING);

		// Verify that applyGermplasmListResults was never called
		Mockito.verify(this.searchResultsComponent, Mockito.times(0)).applyGermplasmListResults(germplasmLists);
	}

	@Test
	public void testDoSearchEmptyString() throws Exception {
		final List<GermplasmList> germplasmLists = new ArrayList<GermplasmList>();
		Mockito.doThrow(new BreedingManagerSearchException(Message.SEARCH_QUERY_CANNOT_BE_EMPTY)).when(this.breedingManagerService)
				.doGermplasmListSearch(Matchers.anyString(), Matchers.any(Operation.class));

		this.listSearchBarComponent.doSearch("    ");

		// Verify that applyGermplasmListResults was never called
		Mockito.verify(this.searchResultsComponent, Mockito.times(0)).applyGermplasmListResults(germplasmLists);
	}

	@Test
	public void testSearchButtonClickActionUsingDefaultSearchType() throws BreedingManagerSearchException {
		this.listSearchBarComponent.searchButtonClickAction();

		// Verify that germplasm list search was called directly (ie. no confirm dialog shown)
		Mockito.verify(this.breedingManagerService, Mockito.times(1)).doGermplasmListSearch(Matchers.anyString(),
				Matchers.any(Operation.class));
	}

	@Test
	public void testSearchButtonClickActionUsingContainsKeywordSearchType() throws BreedingManagerSearchException {
		this.listSearchBarComponent.setSearchType(SearchType.CONTAINS_KEYWORD);
		Mockito.doReturn("Some String").when(this.messageSource).getMessage(Matchers.any(Message.class));

		this.listSearchBarComponent.searchButtonClickAction();

		// Verify that germplasm list search was not called since Confirm Dialog is shown first
		Mockito.verify(this.breedingManagerService, Mockito.times(0)).doGermplasmListSearch(Matchers.anyString(),
				Matchers.any(Operation.class));
	}

}
