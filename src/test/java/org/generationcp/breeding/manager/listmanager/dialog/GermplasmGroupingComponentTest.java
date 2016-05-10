
package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.MgidApplicationStatus;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.data.initializer.GermplasmGroupTestDataInitializer;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.data.initializer.MethodTestDataInitializer;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.service.api.GermplasmGroup;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.PlatformTransactionManager;

import com.google.common.collect.Sets;

public class GermplasmGroupingComponentTest {

	private static final String DUMMY_MESSAGE = "DUMMY MESSAGE";
	private static final int METHOD_ID = 1;
	private static final String GEN_METHOD_TYPE = "GEN";
	private static final String DER_METHOD_TYPE = "DER";

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmGroupingService germplasmGroupingService;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private PlatformTransactionManager transactionManager;

	private final Set<Integer> gidsToProcess = Sets.newHashSet(1, 2, 3);

	@Mock
	private GermplasmGroupingComponentSource source;

	private GermplasmGroupingComponent germplasmGroupingComponent;
	private GermplasmGroupTestDataInitializer germplasmGroupTestDataInitializer;
	private MethodTestDataInitializer methodTestDataInitializer;
	private GermplasmTestDataInitializer germplasmTestDataInitializer;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		this.germplasmGroupingComponent = new GermplasmGroupingComponent(this.source);

		this.germplasmGroupingComponent.setGidsToProcess(this.gidsToProcess);

		// Component init sequence
		this.germplasmGroupingComponent.afterPropertiesSet();

		this.germplasmGroupingComponent.setTransactionManager(this.transactionManager);
		this.germplasmGroupingComponent.setGermplasmDataManager(this.germplasmDataManager);
		this.germplasmGroupingComponent.setGermplasmGroupingService(this.germplasmGroupingService);
		this.germplasmGroupingComponent.setMessageSource(this.messageSource);

		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Mockito.any(Message.class));

		// init test data initializers
		this.germplasmGroupTestDataInitializer = new GermplasmGroupTestDataInitializer();
		this.methodTestDataInitializer = new MethodTestDataInitializer();
		this.germplasmTestDataInitializer = new GermplasmTestDataInitializer();
	}

	@Test
	public void testGroupGermplasm() {

		// init test data
		for (final Integer id : this.gidsToProcess) {
			final Germplasm germplasm = this.germplasmTestDataInitializer.createGermplasm(id);
			germplasm.setMethod(this.methodTestDataInitializer.createMethod(METHOD_ID, DER_METHOD_TYPE));
			Mockito.doReturn(germplasm).when(this.germplasmDataManager).getGermplasmByGID(id);

			final GermplasmGroup report = this.germplasmGroupTestDataInitializer.createGermplasmGroup(id, id);
			report.setFounder(germplasm);
			Mockito.doReturn(report).when(this.germplasmGroupingService).markFixed(germplasm, false, false);
		}

		this.germplasmGroupingComponent.groupGermplasm();

		// Just basic assertion that the specified number of germplasm were loaded and processed via the grouping service.
		Mockito.verify(this.germplasmDataManager, Mockito.times(this.gidsToProcess.size())).getGermplasmByGID(Mockito.anyInt());
		Mockito.verify(this.germplasmGroupingService, Mockito.times(this.gidsToProcess.size())).markFixed(Mockito.any(Germplasm.class),
				Mockito.anyBoolean(), Mockito.anyBoolean());
	}

	@Test
	public void testVerifyIfAllEntriesHasAppliedMGIDSuccessfullyToAllEntries() {

		final Map<Integer, GermplasmGroup> groupingResults = this.initGroupingResults();

		Assert.assertTrue("Expecting to return true for all germplasm groups since all founder germplasm has non-GENERATIVE method type.",
				this.germplasmGroupingComponent.verifyMGIDApplicationForSelected(groupingResults).equals(MgidApplicationStatus.ALL_ENTRIES));

	}

	@Test
	public void testVerifyIfAllEntriesHasAppliedMGIDSuccessfullyToSomeEntriesOnly() {

		final Map<Integer, GermplasmGroup> groupingResults = this.initGroupingResults();

		groupingResults.get(1).getFounder().setMethod(this.methodTestDataInitializer.createMethod(METHOD_ID, GEN_METHOD_TYPE));

		Assert.assertTrue("Expecting to return false since not all germplasm group's founder germplasm has non-GENERATIVE method type.",
				this.germplasmGroupingComponent.verifyMGIDApplicationForSelected(groupingResults)
						.equals(MgidApplicationStatus.SOME_ENTRIES));

	}

	@Test
	public void testVerifyIfAllEntriesHasNotAppliedMGIDToAnyEntries() {

		final Map<Integer, GermplasmGroup> groupingResults = this.initGroupingResults();

		for (final GermplasmGroup germplasmGroup : groupingResults.values()) {
			germplasmGroup.getFounder().setMethod(this.methodTestDataInitializer.createMethod(METHOD_ID, GEN_METHOD_TYPE));
		}

		Assert.assertTrue("Expecting to return false since all germplasm group's founder germplasm has GENERATIVE method type.",
				this.germplasmGroupingComponent.verifyMGIDApplicationForSelected(groupingResults).equals(MgidApplicationStatus.NO_ENTRIES));

	}

	private Map<Integer, GermplasmGroup> initGroupingResults() {
		final Map<Integer, Integer> groupIdNoOfEntriesMap = new HashMap<Integer, Integer>();
		groupIdNoOfEntriesMap.put(1, 5);
		groupIdNoOfEntriesMap.put(2, 2);
		groupIdNoOfEntriesMap.put(3, 3);

		final Map<Integer, GermplasmGroup> groupingResults =
				this.germplasmGroupTestDataInitializer.createGermplasmGroupMap(groupIdNoOfEntriesMap);

		// Set Generative method for every germplasm group founder
		for (final Map.Entry<Integer, GermplasmGroup> entry : groupingResults.entrySet()) {
			final GermplasmGroup germplasmGroup = entry.getValue();
			final Germplasm founder = germplasmGroup.getFounder();
			founder.setMethod(this.methodTestDataInitializer.createMethod(METHOD_ID, DER_METHOD_TYPE));
		}
		return groupingResults;
	}

}
