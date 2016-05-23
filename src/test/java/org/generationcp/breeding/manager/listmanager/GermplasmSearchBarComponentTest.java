
package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.service.BreedingManagerSearchException;
import org.generationcp.breeding.manager.service.BreedingManagerServiceImpl;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

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
	public void setUp() {
		Mockito.when(this.messageSource.getMessage(Message.MATCHES_STARTING_WITH)).thenReturn("Matches starting with");
		Mockito.when(this.messageSource.getMessage(Message.EXACT_MATCHES)).thenReturn("Exact Matches");
		Mockito.when(this.messageSource.getMessage(Message.MATCHES_CONTAINING)).thenReturn("Matches containing");
		  
		this.breedingManagerService.setGermplasmDataManager(this.germplasmDataManager);
		this.germplasmSearchBarComponent = new GermplasmSearchBarComponent(this.germplasmSearchResultsComponent);
		this.germplasmSearchBarComponent.setMessageSource(this.messageSource);
		this.germplasmSearchBarComponent.setBreedingManagerService(this.breedingManagerService);
		this.germplasmSearchBarComponent.setTransactionManager(this.transactionManager);
		this.germplasmSearchBarComponent.instantiateComponents();
		
		this.germplasmSearchBarComponent.setParent(component);
		Mockito.when(this.component.getWindow()).thenReturn(new Window());
	}

	@Test
	public void testDoSearch() throws BreedingManagerSearchException {
		this.germplasmSearchBarComponent.doSearch(GermplasmSearchBarComponentTest.TEST_SEARCH_STRING);
		Mockito.verify(this.breedingManagerService).doGermplasmSearch(Matchers.anyString(), Matchers.eq(Operation.LIKE), Matchers.eq(false), Matchers.eq(false), Matchers.eq(false));	
	}
	
	@Test
	public void testDoSearchWithEmptySearchString() throws BreedingManagerSearchException {
		Mockito.when(this.breedingManagerService.doGermplasmSearch(Matchers.anyString(), Matchers.eq(Operation.LIKE), Matchers.eq(false), Matchers.eq(false), Matchers.eq(false))).thenThrow(new BreedingManagerSearchException(Message.SEARCH_QUERY_CANNOT_BE_EMPTY));
		this.germplasmSearchBarComponent.doSearch("");
		Mockito.verify(this.messageSource).getMessage(Message.UNABLE_TO_SEARCH);
	}
	
	@Test
	public void testDoSearchWithDatabaseError() throws BreedingManagerSearchException {
		Mockito.when(this.breedingManagerService.doGermplasmSearch(Matchers.anyString(), Matchers.eq(Operation.LIKE), Matchers.eq(false), Matchers.eq(false), Matchers.eq(false))).thenThrow(new BreedingManagerSearchException(Message.ERROR_DATABASE));
		this.germplasmSearchBarComponent.doSearch("");
		Mockito.verify(this.messageSource).getMessage(Message.SEARCH_RESULTS);
	}
	
	@Test
	public void testValidateIfSearchKeywordIsNotEmpty() {
		final String keyword = "";
 	 	this.germplasmSearchBarComponent.validateIfSearchKeywordIsNotEmpty(keyword);
		Mockito.verify(this.messageSource).getMessage(Message.SEARCH_KEYWORD_MUST_NOT_BE_EMPTY);
	}
}
