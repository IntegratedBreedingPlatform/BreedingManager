
package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.service.BreedingManagerSearchException;
import org.generationcp.breeding.manager.service.BreedingManagerServiceImpl;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
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
		Mockito.when(this.messageSource.getMessage(Message.SEARCH_QUERY_CANNOT_BE_EMPTY)).thenReturn("Search query cannot be empty");

		this.breedingManagerService.setGermplasmDataManager(this.germplasmDataManager);
		this.germplasmSearchBarComponent = new GermplasmSearchBarComponent(this.germplasmSearchResultsComponent);
		this.germplasmSearchBarComponent.setMessageSource(this.messageSource);
		this.germplasmSearchBarComponent.setBreedingManagerService(this.breedingManagerService);
		this.germplasmSearchBarComponent.setTransactionManager(this.transactionManager);
		this.germplasmSearchBarComponent.instantiateComponents();

		this.germplasmSearchBarComponent.setParent(this.component);
		Mockito.when(this.component.getWindow()).thenReturn(new Window());
	}

	@Test
	public void testDoSearch() throws BreedingManagerSearchException {
		this.germplasmSearchBarComponent.doSearch(GermplasmSearchBarComponentTest.TEST_SEARCH_STRING);
		Mockito.verify(this.messageSource, Mockito.times(0)).getMessage(Message.UNABLE_TO_SEARCH);
		Mockito.verify(this.messageSource, Mockito.times(0)).getMessage(Message.SEARCH_RESULTS);
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
}
