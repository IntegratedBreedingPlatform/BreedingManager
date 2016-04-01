
package org.generationcp.breeding.manager.listmanager;

import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.service.BreedingManagerSearchException;
import org.generationcp.breeding.manager.service.BreedingManagerServiceImpl;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;

import com.vaadin.ui.CheckBox;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmSearchBarComponentTest {

	private static final String MATCHES_STARTING_WITH = "Matches starting with";
	private static final String MATCHES_CONTAINING = "Matches containing";

	@Mock
	private GermplasmSearchResultsComponent germplasmSearchResultsComponent;

	@Mock
	private BreedingManagerServiceImpl breedingManagerService;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private PlatformTransactionManager transactionManager;

	private SimpleResourceBundleMessageSource messageSource;
	private GermplasmSearchBarComponent germplasmSearchBarComponent;

	private GermplasmSearchBarComponent spyComponent;

	private static final String NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA = "No local results were found. "
			+ "You can include public data in the search by selecting " + "\"Include public data\" checkbox.";
	private static final String NO_SEARCH_RESULTS = "No matches were found.";
	private static final String TEST_SEARCH_STRING = "1234567";
	private static final String PERCENT = "%";

	@Before
	public void setUp() {
		this.messageSource = new SimpleResourceBundleMessageSource();
		this.messageSource.setBasename("I18NMessages");
		this.breedingManagerService.setGermplasmDataManager(this.germplasmDataManager);
		this.germplasmSearchBarComponent = new GermplasmSearchBarComponent(this.germplasmSearchResultsComponent);
		this.germplasmSearchBarComponent.setMessageSource(this.messageSource);
		this.germplasmSearchBarComponent.setBreedingManagerService(this.breedingManagerService);
		this.germplasmSearchBarComponent.setTransactionManager(this.transactionManager);
		this.spyComponent = Mockito.spy(this.germplasmSearchBarComponent);
		this.spyComponent.instantiateComponents();
	}

	@Test
	public void testErrorMessages() {
		Assert.assertEquals("Error message should be \"" + GermplasmSearchBarComponentTest.NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA + "\"",
				GermplasmSearchBarComponentTest.NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA,
				this.messageSource.getMessage(Message.NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA));
		Assert.assertEquals("Error message should be \"" + GermplasmSearchBarComponentTest.NO_SEARCH_RESULTS + "\"",
				GermplasmSearchBarComponentTest.NO_SEARCH_RESULTS, this.messageSource.getMessage(Message.NO_SEARCH_RESULTS));
	}

	@Test
	public void testDoSearch() {

		final CheckBox includeParentsCheckBox = this.spyComponent.getIncludeParentsCheckBox();
		final CheckBox withInventoryOnlyCheckBox = this.spyComponent.getWithInventoryOnlyCheckBox();
		final boolean includeParents = (Boolean) includeParentsCheckBox.getValue();
		final boolean withInventoryOnly = (Boolean) withInventoryOnlyCheckBox.getValue();

		final String searchType = (String) this.spyComponent.getSearchTypeOptions().getValue();
		final String searchKeyword = this.getSearchKeyword(GermplasmSearchBarComponentTest.TEST_SEARCH_STRING, searchType);

		final boolean exactMatchesOnly = false;
		final Operation operation = exactMatchesOnly ? Operation.EQUAL : Operation.LIKE;
		final List<Germplasm> results = null;

		try {
			Mockito.when(this.germplasmDataManager.searchForGermplasm(searchKeyword, operation, includeParents, withInventoryOnly))
					.thenReturn(null);
			Mockito.doNothing().when(this.germplasmSearchResultsComponent).applyGermplasmResults(results);
			this.spyComponent.doSearch(GermplasmSearchBarComponentTest.TEST_SEARCH_STRING);
			Mockito.verify(this.breedingManagerService).doGermplasmSearch(searchKeyword, operation, includeParents, withInventoryOnly);
		} catch (final BreedingManagerSearchException e) {
			final Message errorMessage = e.getErrorMessage();
			Assert.assertEquals("Error message should be " + GermplasmSearchBarComponentTest.NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA,
					GermplasmSearchBarComponentTest.NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA, this.messageSource.getMessage(errorMessage));
		} catch (final Exception e) {
			Assert.fail("Test fails with error : " + e.getMessage());
		}
	}

	private String getSearchKeyword(final String query, final String searchType) {
		String searchKeyword = query;
		if (GermplasmSearchBarComponentTest.MATCHES_STARTING_WITH.equals(searchType)) {
			searchKeyword = searchKeyword + GermplasmSearchBarComponentTest.PERCENT;
		} else if (GermplasmSearchBarComponentTest.MATCHES_CONTAINING.equals(searchType)) {
			searchKeyword = GermplasmSearchBarComponentTest.PERCENT + searchKeyword + GermplasmSearchBarComponentTest.PERCENT;
		}
		return searchKeyword;
	}
}
