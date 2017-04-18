package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import junit.framework.Assert;

public class SelectParentsComponentTest {
	
	private static final String LIST_NAME = "LIST 1";

	@Mock
	private CrossingManagerMakeCrossesComponent source;
	
	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	
	private SelectParentsComponent selectParentsComponent;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		this.selectParentsComponent =  new SelectParentsComponent(this.source);
		this.selectParentsComponent.setMessageSource(this.messageSource);
		
		Mockito.doReturn("").when(this.messageSource).getMessage(Matchers.any(Message.class));
		
		this.selectParentsComponent.instantiateComponents();
	}
	
	@Test
	public void testHideDetailsTabsheet() {
		// Open 2 lists in tabsheet
		this.selectParentsComponent.createListDetailsTab(1, LIST_NAME);
		this.selectParentsComponent.createListDetailsTab(2, "LIST 2");
		
		// Check that "Close All tabs" and list tabsheet are visible for verifying whether they were hidden later on
		Assert.assertTrue(this.selectParentsComponent.getCloseAllTabsButton().isVisible());
		Assert.assertTrue(this.selectParentsComponent.getListDetailsTabSheet().isVisible());
		
		// Method to test
		this.selectParentsComponent.hideDetailsTabsheet();
		
		// Check that "Close All tabs" link and tabsheet are hidden and that "Browse For List" link remains visible
		Assert.assertFalse(this.selectParentsComponent.getCloseAllTabsButton().isVisible());
		Assert.assertFalse(this.selectParentsComponent.getListDetailsTabSheet().isVisible());
		Assert.assertTrue(this.selectParentsComponent.getBrowseForListsButton().isVisible());
	}
	
	@Test
	public void testCloseAllTabsButtonClick() {
		// Open 1 list in tabsheet
		this.selectParentsComponent.addListeners();
		this.selectParentsComponent.createListDetailsTab(1, LIST_NAME);
		this.selectParentsComponent.createListDetailsTab(1, "LIST 2");
		
		// Check that "Close All tabs" and list tabsheet are visible for verifying whether they were hidden later on
		Assert.assertTrue(this.selectParentsComponent.getCloseAllTabsButton().isVisible());
		Assert.assertTrue(this.selectParentsComponent.getListDetailsTabSheet().isVisible());
		
		// Method to test
		this.selectParentsComponent.getCloseAllTabsButton().click();
		
		// Check that "Close All tabs" link and tabsheet are hidden
		Assert.assertFalse(this.selectParentsComponent.getCloseAllTabsButton().isVisible());
		Assert.assertFalse(this.selectParentsComponent.getListDetailsTabSheet().isVisible());
	}
	
	@Test
	public void testCloseAllTabsButtonVisibility() {
		// Open 1 list in tabsheet
		this.selectParentsComponent.addListeners();
		this.selectParentsComponent.createListDetailsTab(1, LIST_NAME);
		
		// "Close All tabs" is hidden if there's only one list
		Assert.assertFalse(this.selectParentsComponent.getCloseAllTabsButton().isVisible());
		
		// Check that "Close All tabs" is visible if there are 2 or more lists open
		this.selectParentsComponent.createListDetailsTab(1, "LIST 2");
		Assert.assertTrue(this.selectParentsComponent.getCloseAllTabsButton().isVisible());
	}
	
	@Test
	public void testToggleTabsheetButtonClick() {
		// Open 1 list in tabsheet
		this.selectParentsComponent.addListeners();
		this.selectParentsComponent.createListDetailsTab(1, LIST_NAME);
		Assert.assertTrue(this.selectParentsComponent.getListDetailsTabSheet().isVisible());
		
		// Check that tabsheet is hidden upon button click
		this.selectParentsComponent.getToggleTabsheetButton().click();
		Assert.assertFalse(this.selectParentsComponent.getListDetailsTabSheet().isVisible());
		
		// Check that tabsheet is visible again after another button click
		this.selectParentsComponent.getToggleTabsheetButton().click();
		Assert.assertTrue(this.selectParentsComponent.getListDetailsTabSheet().isVisible());
	}

}
