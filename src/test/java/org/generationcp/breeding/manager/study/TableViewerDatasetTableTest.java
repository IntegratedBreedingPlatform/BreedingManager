
package org.generationcp.breeding.manager.study;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class TableViewerDatasetTableTest {

	private StudyDataManager studyDataManager;

	@InjectMocks
	private TableViewerDatasetTable tableViewerTable;

	@Before
	public void setUp() {
		this.studyDataManager = Mockito.mock(StudyDataManager.class);

		this.tableViewerTable = new TableViewerDatasetTable(this.studyDataManager, 1, 1);
	}

	@Test
	public void testRetrievalForDatasetWithLessThan100Experiments() throws MiddlewareException {
		int size = 89;
		Mockito.doReturn(new Long(size)).when(this.studyDataManager).countExperiments(Matchers.anyInt());
		Mockito.doReturn(this.createDummyExperiments(size)).when(this.studyDataManager).getExperiments(1, 0, size);

		List<Experiment> experimentsByBatch = this.tableViewerTable.getExperimentsByBatch();
		Mockito.verify(this.studyDataManager).getExperiments(1, 0, size);

		Assert.assertNotNull(experimentsByBatch);
		Assert.assertEquals("Expecting count of experiments retrieved to be equal to dataset size", size, experimentsByBatch.size());
	}

	@Test
	public void testBatchRetrievalForDatasetGreaterThan100ExperimentsWithBatchRemainder() throws MiddlewareException {
		int size = 180;
		Mockito.doReturn(new Long(size)).when(this.studyDataManager).countExperiments(Matchers.anyInt());

		this.validateBatchRetrieval(size);
	}

	@Test
	public void testBatchRetrievalForDatasetGreaterThan100ExperimentsNoBatchRemainder() throws MiddlewareException {
		int size = 300;
		Mockito.doReturn(new Long(size)).when(this.studyDataManager).countExperiments(Matchers.anyInt());

		this.validateBatchRetrieval(size);
	}

	private void validateBatchRetrieval(int size) throws MiddlewareException {
		int batchSize = TableViewerDatasetTable.BATCH_SIZE;
		int batchCount = size / batchSize;
		int remaining = size % batchSize;

		// return dummy experiments from Middleware
		List<Experiment> dummyBatchExperiments = this.createDummyExperiments(batchSize);
		for (int i = 0; i < batchCount; i++) {
			Mockito.doReturn(dummyBatchExperiments).when(this.studyDataManager).getExperiments(1, i * batchSize, batchSize);
		}
		if (remaining > 0) {
			Mockito.doReturn(this.createDummyExperiments(remaining)).when(this.studyDataManager)
					.getExperiments(1, batchSize * batchCount, remaining);
		}

		// actual method call, verify if proper Middleware calls were made
		List<Experiment> experimentsByBatch = this.tableViewerTable.getExperimentsByBatch();
		for (int i = 0; i < batchCount; i++) {
			Mockito.verify(this.studyDataManager).getExperiments(1, i * batchSize, batchSize);
		}
		if (remaining > 0) {
			Mockito.verify(this.studyDataManager).getExperiments(1, batchSize * batchCount, remaining);
		}

		Assert.assertNotNull(experimentsByBatch);
		Assert.assertEquals("Expecting count of experiments retrieved to be equal to dataset size", size, experimentsByBatch.size());
	}

	private List<Experiment> createDummyExperiments(int size) {
		List<Experiment> experiments = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			experiments.add(new Experiment());
		}
		return experiments;
	}

}
