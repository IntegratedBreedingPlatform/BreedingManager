
package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.Set;

import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.PlatformTransactionManager;

import com.google.common.collect.Sets;

public class GermplasmGroupingComponentTest {

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
	private GermplasmGroupingSource source;

	private GermplasmGroupingComponent germplasmGroupingComponent;

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
	}

	@Test
	public void testGroupGermplasm() {
		this.germplasmGroupingComponent.groupGermplasm();

		// Just basic assertion that the sepcified number of germplasm were loaded and processed via the grouping service.
		Mockito.verify(this.germplasmDataManager, Mockito.times(this.gidsToProcess.size())).getGermplasmByGID(Mockito.anyInt());
		Mockito.verify(this.germplasmGroupingService, Mockito.times(this.gidsToProcess.size())).markFixed(Mockito.any(Germplasm.class),
				Mockito.anyBoolean(), Mockito.anyBoolean());
	}

}
