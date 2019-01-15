package org.generationcp.breeding.manager.listimport;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.security.SecurityUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;

import com.google.common.collect.Lists;
import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmImportMainTest {

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@InjectMocks
	private final GermplasmImportMain germplasmImportMain = new GermplasmImportMain(new Window(), true, true);

	@Before
	public void setUp() {
		Mockito.when(this.messageSource.getMessage(Message.CHOOSE_IMPORT_FILE)).thenReturn("Choose Import File");
		Mockito.when(this.messageSource.getMessage(Message.SPECIFY_GERMPLASM_DETAILS)).thenReturn("Specify Germplasm Details");

		this.germplasmImportMain.instantiateComponents();
	}

	@Test
	public void testNextStep() {
		final TabSheet tabSheet = this.germplasmImportMain.getTabSheet();

		// check that state is coming from 1st step - File Upload page
		Component selectedTab = tabSheet.getSelectedTab();
		Assert.assertTrue("Germplasm Import File Upload screen should be displayed",
				this.germplasmImportMain.getWizardScreenOne().equals(selectedTab));
		Assert.assertEquals("Page height is 300px", 300.0f, tabSheet.getHeight());
		Assert.assertEquals(0, tabSheet.getHeightUnits());

		this.germplasmImportMain.nextStep();

		// Check that 2ns step - Specify Germplasm Details page is displayed and tab sheet height changed
		selectedTab = tabSheet.getSelectedTab();
		Assert.assertTrue("Specify Germplasm Details screen should be displayed",
				this.germplasmImportMain.getWizardScreenTwo().equals(selectedTab));
		Assert.assertEquals("Page height is 860px", 860.0f, tabSheet.getHeight());
		Assert.assertEquals(0, tabSheet.getHeightUnits());
	}

	@Test
	public void testBackStep() {
		final TabSheet tabSheet = this.germplasmImportMain.getTabSheet();

		// check that state is coming from 1st step - File Upload page
		Component selectedTab = tabSheet.getSelectedTab();
		Assert.assertTrue("Germplasm Import File Upload screen should be displayed",
				this.germplasmImportMain.getWizardScreenOne().equals(selectedTab));

		// Go to 2nd step then go back to 1st step
		this.germplasmImportMain.nextStep();
		this.germplasmImportMain.backStep();

		// check 1st step is displayed again
		selectedTab = tabSheet.getSelectedTab();
		Assert.assertTrue("Germplasm Import File Upload screen should be displayed",
				this.germplasmImportMain.getWizardScreenOne().equals(selectedTab));
		Assert.assertEquals("Page height is 300px", 300.0f, tabSheet.getHeight());
		Assert.assertEquals(0, tabSheet.getHeightUnits());
	}
	
}
