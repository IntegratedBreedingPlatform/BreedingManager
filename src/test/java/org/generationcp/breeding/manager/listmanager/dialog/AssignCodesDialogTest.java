package org.generationcp.breeding.manager.listmanager.dialog;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.service.api.GermplasmNamingReferenceDataResolver;
import org.generationcp.middleware.service.api.GermplasmNamingService;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.PlatformTransactionManager;

public class AssignCodesDialogTest {

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmNamingService germplasmNamingService;

	@Mock
	private GermplasmNamingReferenceDataResolver germplasmNamingReferenceDataResolver;

	@Mock
	private PlatformTransactionManager transactionManager;

	@Mock
	private ContextUtil contextUtil;

	private AssignCodesDialog assignCodesDialog;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.assignCodesDialog = new AssignCodesDialog(false);
	}

	//TODO add tests

}
