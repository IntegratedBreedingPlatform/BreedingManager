package org.generationcp.breeding.manager.listmanager.dialog;

import com.vaadin.ui.Window;
import junit.framework.Assert;
import org.generationcp.breeding.manager.listmanager.dialog.layout.AssignCodeCustomLayout;
import org.generationcp.breeding.manager.listmanager.dialog.layout.AssignCodesNamingLayout;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.GermplasmNamingReferenceDataResolver;
import org.generationcp.middleware.service.api.GermplasmNamingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.LinkedHashSet;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class AssignCodesDialogTest {

	private static final String GROUP_NAME_PREFIX_CUSTOM = "CAL";
	private static final String GROUP_NAME_PREFIX_DEFAULT = "AAA";

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

	@Mock
	private AssignCodeCustomLayout assignCodeCustomLayout;

	@Mock
	private AssignCodesNamingLayout assignCodesDefaultLayout;

	@Mock
	private Window parent;

	@InjectMocks
	private final AssignCodesDialog assignCodesDialog = new AssignCodesDialog(false);

	@Before
	public void setUp() throws Exception {

		this.assignCodesDialog.afterPropertiesSet();

		this.assignCodesDialog.setAssignCodesCustomLayout(assignCodeCustomLayout);
		this.assignCodesDialog.setAssignCodesDefaultLayout(assignCodesDefaultLayout);

		Mockito.when(assignCodesDefaultLayout.getGroupNamePrefix()).thenReturn(GROUP_NAME_PREFIX_DEFAULT);
		Mockito.when(assignCodeCustomLayout.getGroupNamePrefix()).thenReturn(GROUP_NAME_PREFIX_CUSTOM);

		this.assignCodesDialog.setParent(parent);
		this.assignCodesDialog.setGidsToProcess(this.createGidsToProcess());

	}

	@Test
	public void testAssignCodes() {

		final UserDefinedField nameType = this.createUserDefinedField();
		Mockito.when(germplasmNamingReferenceDataResolver.resolveNameType(assignCodesDialog.getLevel())).thenReturn(nameType);

		assignCodesDialog.assignCodes();

		// Make sure that the codes are assigned to all GIDs
		Mockito.verify(this.germplasmNamingService).applyGroupName(1, GROUP_NAME_PREFIX_DEFAULT, nameType, 0, 0);
		Mockito.verify(this.germplasmNamingService).applyGroupName(2, GROUP_NAME_PREFIX_DEFAULT, nameType, 0, 0);
		Mockito.verify(this.germplasmNamingService).applyGroupName(3, GROUP_NAME_PREFIX_DEFAULT, nameType, 0, 0);

		Mockito.verify(parent).addWindow(Mockito.any(AssignCodesResultsDialog.class));
		Mockito.verify(parent).removeWindow(assignCodesDialog);

	}

	@Test
	public void testGetGroupNamePrefix() {

		Assert.assertEquals(GROUP_NAME_PREFIX_CUSTOM, this.assignCodesDialog.getGroupNamePrefix(true));
		Assert.assertEquals(GROUP_NAME_PREFIX_DEFAULT, this.assignCodesDialog.getGroupNamePrefix(false));
	}

	private Set<Integer> createGidsToProcess() {

		final Set<Integer> gidsToProcess = new LinkedHashSet<>();

		gidsToProcess.add(1);
		gidsToProcess.add(2);
		gidsToProcess.add(3);

		return gidsToProcess;

	}

	private UserDefinedField createUserDefinedField() {

		final UserDefinedField userDefinedField = new UserDefinedField();
		userDefinedField.setFldno(1);
		return userDefinedField;

	}

}
