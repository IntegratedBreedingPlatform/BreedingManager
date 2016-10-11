
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.service.BreedingManagerSearchException;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

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

	@InjectMocks
	private ListSearchBarComponent listSearchBarComponent;

	@Before
	public void setUp() {
		this.listSearchBarComponent.setBreedingManagerService(this.breedingManagerService);
		this.listSearchBarComponent.setMessageSource(this.messageSource);
		this.listSearchBarComponent.setParent(this.parent);
		Mockito.doReturn(new Window()).when(this.parent).getWindow();

		this.listSearchBarComponent.instantiateComponents();
	}

	@Test
	public void testDoSearch() throws Exception {
		final List<GermplasmList> germplasmLists = new ArrayList<GermplasmList>();
		germplasmLists.add(new GermplasmList());
		Mockito.doReturn(germplasmLists).when(this.breedingManagerService)
				.doGermplasmListSearch(Matchers.anyString(), Matchers.any(Operation.class));
		Mockito.doNothing().when(this.searchResultsComponent).applyGermplasmListResults(germplasmLists);

		this.listSearchBarComponent.doSearch(ListSearchBarComponentTest.DUMMY_SEARCH_STRING);
		
		Mockito.verify(this.searchResultsComponent, Mockito.times(1)).applyGermplasmListResults(germplasmLists);
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
}
