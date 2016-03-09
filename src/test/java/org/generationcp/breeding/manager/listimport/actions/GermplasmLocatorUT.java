package org.generationcp.breeding.manager.listimport.actions;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GermplasmLocatorUT {

	public static final String STANDARD_NAME = "STANDARD_NAME";

	public static final int DUMMY_GID = 5;
	@Mock
	GermplasmDataManager manager;

	GermplasmLocator locator;

	GermplasmDataProvider provider;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		locator = new GermplasmLocator(manager);

	}



	@Test
	public void locateGermplasmByGIDFirst() throws Exception {
		Germplasm expectedGermplasm = new Germplasm();
		provider = createGermplasmProvider(DUMMY_GID,null);
		when(manager.getGermplasmByGID(DUMMY_GID)).thenReturn(expectedGermplasm);

		List<Germplasm> germplasms = locator.locate(provider);

		assertThat(germplasms).containsExactly(expectedGermplasm);

		verify(manager).getGermplasmByGID(DUMMY_GID);
		verify(manager,never()).getGermplasmByName(anyString(), anyInt(), anyInt(), any(Operation.class));
	}

	@Test
	public void locateGermplasmByNameWhenGIDIsNotProvided() throws Exception {
		String name = STANDARD_NAME;
		Germplasm expectedGermplasm = new Germplasm();
		List<Germplasm> expectedList = Lists.newArrayList(expectedGermplasm);
		provider = createGermplasmProvider(0,STANDARD_NAME);
		when(manager.getGermplasmByName(name, 0, 1, Operation.EQUAL)).thenReturn(expectedList);


		List<Germplasm> germplasms = locator.locate(provider);

		assertThat(germplasms).containsExactly(expectedGermplasm);

		verify(manager,never()).getGermplasmByGID(anyInt());
		verify(manager).getGermplasmByName(name, 0, 1, Operation.EQUAL);
	}



	@Test
	public void returnEmptyListWhenGIDWasNotFound()  {

		provider = createGermplasmProvider(DUMMY_GID,null);
		when(manager.getGermplasmByGID(DUMMY_GID)).thenReturn(null);

		List<Germplasm> list = locator.locate(provider);

		assertThat(list).isEmpty();

		verify(manager).getGermplasmByGID(anyInt());
		verify(manager,never()).getGermplasmByName(anyString(), anyInt(), anyInt(), any(Operation.class));
	}

	@Test
	public void failWhenNameWasNotFound() throws Exception {
		provider = createGermplasmProvider(0,STANDARD_NAME);

		when(manager.getGermplasmByName(STANDARD_NAME, 0, 1, Operation.EQUAL)).thenReturn(new ArrayList<Germplasm>());

		List<Germplasm> list = locator.locate(provider);

		assertThat(list).isEmpty();

		verify(manager,never()).getGermplasmByGID(anyInt());
		verify(manager).getGermplasmByName(STANDARD_NAME, 0, 1, Operation.EQUAL);
	}

	public GermplasmDataProvider createGermplasmProvider(final int gid, final String name) {
		return new GermplasmDataProvider() {

			@Override
			public int getGid() {
				return gid;
			}

			@Override
			public int getProgenitors() {
				return 0;
			}

			@Override
			public int getFemaleParent() {
				return 0;
			}

			@Override
			public int getMaleParent() {
				return 0;
			}

			@Override
			public int getUserId() {
				return 0;
			}

			@Override
			public int getDateValue() {
				return 0;
			}

			@Override
			public int getGrplce() {
				return 0;
			}

			@Override
			public int getReferenceId() {
				return 0;
			}

			@Override
			public int getMgid() {
				return 0;
			}

			@Override
			public int getLocationId() {
				return 0;
			}

			@Override
			public int getMethodId() {
				return 0;
			}

			@Override
			public int getLgid() {
				return 0;
			}

			@Override
			public int getNameDateValue() {
				return 0;
			}

			@Override
			public int getTypeId() {
				return 0;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public int getNstat() {
				return 0;
			}
		};
	}
}
