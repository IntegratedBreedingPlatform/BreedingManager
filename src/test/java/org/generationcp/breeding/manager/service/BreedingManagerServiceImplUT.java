package org.generationcp.breeding.manager.service;

import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class BreedingManagerServiceImplUT {

	public static final String DUMMY_STRING = "DUMMY_STRING";
	public static final String EMPTY_STRING = "";
	public static final int EXPECTED_SIZE = 1;

	BreedingManagerServiceImpl target;

	@Mock
	private GermplasmDataManager germplasmDataManagerMock;

	@Mock
	private ContextUtil contextUtilMock;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		target = new BreedingManagerServiceImpl();
		target.setGermplasmDataManager(germplasmDataManagerMock);


	}

	@Test
	public void doGermplasmSearchFailsWithErrorInSearchWhenManagerFails(){
		List<GermplasmList> list = Lists.newArrayList();

		when(germplasmDataManagerMock.searchForGermplasm(DUMMY_STRING, Operation.EQUAL,true,true,true)).thenThrow(new MiddlewareQueryException(DUMMY_STRING));
		try {
			target.doGermplasmSearch(DUMMY_STRING, Operation.EQUAL,true,true,true);
		} catch (BreedingManagerSearchException e) {
			assertThat(e.getErrorMessage()).isEqualsToByComparingFields(Message.ERROR_IN_SEARCH);
		}

	}

	@Test
	public void doGermplasmSearchFailsWithNoSearchWhenSearchStringIsEmpty(){
		List<Germplasm> list = Lists.newArrayList();
		when(germplasmDataManagerMock.searchForGermplasm(EMPTY_STRING, Operation.EQUAL,true,true,true)).thenReturn(list);
		try {
			target.doGermplasmSearch(EMPTY_STRING, Operation.EQUAL,true,true,true);
		} catch (BreedingManagerSearchException e) {
			assertThat(e.getErrorMessage()).isEqualsToByComparingFields(Message.NO_SEARCH_RESULTS);
		}

	}

	@Test
	public void doGermplasmSearchReturnAListOfGermplasmWhenSearchStringIsFound() throws BreedingManagerSearchException {
		Germplasm elem1 = new Germplasm();
		List<Germplasm> list = Lists.newArrayList(elem1);
		when(germplasmDataManagerMock.searchForGermplasm(EMPTY_STRING, Operation.EQUAL,true,true,true)).thenReturn(list);

		List<Germplasm> germplasms = target.doGermplasmSearch(DUMMY_STRING, Operation.EQUAL, true, true, true);

		assertThat(germplasms).hasSize(EXPECTED_SIZE);
		assertThat(germplasms).contains(elem1);
	}
}
