package org.generationcp.breeding.manager.listmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.service.BreedingManagerSearchException;
import org.generationcp.breeding.manager.service.BreedingManagerServiceImpl;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
	
	private SimpleResourceBundleMessageSource messageSource;	
	private GermplasmSearchBarComponent germplasmSearchBarComponent;
	
	private GermplasmSearchBarComponent spyComponent;
	
	private static final String NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA = "No local results were found. " +
				"You can include public data in the search by selecting " +
				"\"Include public data\" checkbox.";
	private static final String NO_SEARCH_RESULTS = "No matches were found.";
	private static final String TEST_SEARCH_STRING = "1234567";
	private static final String PERCENT = "%";
	
	
	@Before
	public void setUp() {
		messageSource = new SimpleResourceBundleMessageSource();
		messageSource.setBasename("I18NMessages");
		breedingManagerService.setGermplasmDataManager(germplasmDataManager);
		germplasmSearchBarComponent = new GermplasmSearchBarComponent(germplasmSearchResultsComponent);
		germplasmSearchBarComponent.setMessageSource(messageSource);
		germplasmSearchBarComponent.setBreedingManagerService(breedingManagerService);
		spyComponent = spy(germplasmSearchBarComponent);
		spyComponent.instantiateComponents();
	}
	
	@Test
	public void testErrorMessages() {
		assertEquals("Error message should be \""+NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA + "\"",
				NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA,
				messageSource.getMessage(Message.NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA));
		assertEquals("Error message should be \""+NO_SEARCH_RESULTS + "\"",
				NO_SEARCH_RESULTS,
				messageSource.getMessage(Message.NO_SEARCH_RESULTS));
	}
	
	@Test
	public void testDoSearch() {
		
		CheckBox includeParentsCheckBox = spyComponent.getIncludeParentsCheckBox();
		CheckBox withInventoryOnlyCheckBox = spyComponent.getWithInventoryOnlyCheckBox(); 
		boolean includeParents = (Boolean) includeParentsCheckBox.getValue();
		boolean withInventoryOnly = (Boolean) withInventoryOnlyCheckBox.getValue();
		
		String searchType = (String)spyComponent.getSearchTypeOptions().getValue();
		String searchKeyword = getSearchKeyword(TEST_SEARCH_STRING,searchType);
		
		boolean exactMatchesOnly = false;
        Operation operation = exactMatchesOnly ? Operation.EQUAL : Operation.LIKE;
        List<Germplasm> results = null;
        
        try {
        	when(germplasmDataManager.searchForGermplasm(searchKeyword, operation, 
        			includeParents,withInventoryOnly)).thenReturn(null);
			doNothing().when(germplasmSearchResultsComponent).applyGermplasmResults(results);
			spyComponent.doSearch(TEST_SEARCH_STRING);
			verify(breedingManagerService).doGermplasmSearch(searchKeyword, 
					operation, includeParents,withInventoryOnly);
		} catch (BreedingManagerSearchException e) {
			Message errorMessage = e.getErrorMessage();
			assertEquals("Error message should be "+NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA,
				NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA,
				messageSource.getMessage(errorMessage));
		} catch(Exception e) {
			fail("Test fails with error : "+ e.getMessage());
		}
	}

	private String getSearchKeyword(String query, String searchType) {
		String searchKeyword = query;
		if(MATCHES_STARTING_WITH.equals(searchType)) {
        	searchKeyword = searchKeyword+PERCENT;
        } else if(MATCHES_CONTAINING.equals(searchType)) {
        	searchKeyword = PERCENT+searchKeyword+PERCENT;
        }
        return searchKeyword;
	}
}
