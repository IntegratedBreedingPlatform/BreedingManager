
package org.generationcp.breeding.manager.germplasm;

import java.util.List;

import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.search.StudyResultSet;
import org.generationcp.middleware.domain.search.filter.StudyQueryFilter;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.util.MaxPedigreeLevelReachedException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 3/27/2015 Time: 3:59 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class GermplasmQueriesTest {

	private static final int TEST_TRIAL_ID_2 = 2;
	private static final String TEST_TRIAL_NAME_2 = "TRIAL 2";

	private static final int TEST_TRIAL_ID_1 = 1;
	private static final String TEST_TRIAL_NAME_1 = "TRIAL 1";

	@Mock
	private PlatformTransactionManager transactionManager;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private PedigreeDataManager pedigreeDataManager;

	@InjectMocks
	private final GermplasmQueries dut = new GermplasmQueries();

	@Test
	public void testGetPedigreeCountLabelMaxPedigreeReached() throws MiddlewareQueryException {
		Mockito.when(this.pedigreeDataManager.countPedigreeLevel(Matchers.anyInt(), Matchers.anyBoolean(), Matchers.anyBoolean()))
				.thenThrow(MaxPedigreeLevelReachedException.class);

		String label = this.dut.getPedigreeLevelCountLabel(1, true, false);

		Assert.assertEquals(GermplasmQueries.MAX_PEDIGREE_LABEL, label);
	}

	@Test
	public void testGetPedigreeCountLabelMaxNotReached() throws MiddlewareQueryException {
		int dummyPedigreeCount = 4;
		Mockito.when(this.pedigreeDataManager.countPedigreeLevel(Matchers.anyInt(), Matchers.anyBoolean(), Matchers.anyBoolean()))
				.thenReturn(dummyPedigreeCount);

		String label = this.dut.getPedigreeLevelCountLabel(1, true, false);

		Assert.assertEquals(dummyPedigreeCount + " generations", label);
	}

	@Test
	public void testGetPedigreeCountLabelMaxNotReachedOneGenerationOnly() throws MiddlewareQueryException {
		int dummyPedigreeCount = 1;
		Mockito.when(this.pedigreeDataManager.countPedigreeLevel(Matchers.anyInt(), Matchers.anyBoolean(), Matchers.anyBoolean()))
				.thenReturn(dummyPedigreeCount);

		String label = this.dut.getPedigreeLevelCountLabel(1, true, false);

		Assert.assertEquals(dummyPedigreeCount + " generation", label);
	}

	@Test
	public void testGetGermplasmStudyInfo() throws MiddlewareQueryException {

		int testGid = 1;
		StudyResultSet studyResultSet = Mockito.mock(StudyResultSet.class);
		Mockito.when(this.studyDataManager.searchStudies(Mockito.any(StudyQueryFilter.class), Mockito.anyInt())).thenReturn(studyResultSet);
		Mockito.when(studyResultSet.hasMore()).thenReturn(true).thenReturn(true).thenReturn(false);
		Mockito.when(studyResultSet.next()).thenReturn(new StudyReference(TEST_TRIAL_ID_1, TEST_TRIAL_NAME_1))
				.thenReturn(new StudyReference(TEST_TRIAL_ID_2, TEST_TRIAL_NAME_2));

		List<StudyReference> result = this.dut.getGermplasmStudyInfo(testGid);

		Assert.assertEquals(2, result.size());
		Assert.assertEquals(TEST_TRIAL_ID_1, result.get(0).getId().intValue());
		Assert.assertEquals(TEST_TRIAL_NAME_1, result.get(0).getName());
		Assert.assertEquals(TEST_TRIAL_ID_2, result.get(1).getId().intValue());
		Assert.assertEquals(TEST_TRIAL_NAME_2, result.get(1).getName());
	}

}
