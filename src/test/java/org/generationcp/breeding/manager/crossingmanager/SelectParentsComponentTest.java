package org.generationcp.breeding.manager.crossingmanager;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.SaveTreeStateListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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

	@Mock
	private CrossingManagerListTreeComponent listTreeComponent;

	@Mock
	private Window window;

	@Mock
	private Component parent;
	
	private SelectParentsComponent selectParentsComponent;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		this.selectParentsComponent =  new SelectParentsComponent(this.source);
		this.selectParentsComponent.setMessageSource(this.messageSource);
		
		Mockito.doReturn("").when(this.messageSource).getMessage(Matchers.any(Message.class));
		
		this.selectParentsComponent.instantiateComponents();

		this.selectParentsComponent.setListTreeComponent(listTreeComponent);
		this.selectParentsComponent.setParent(parent);

		Mockito.when(parent.getWindow()).thenReturn(window);
	}
	
	@Test
	public void testHideDetailsTabsheet() {
		// Open 2 lists in tabsheet
		this.selectParentsComponent.createListDetailsTab(null, 1, LIST_NAME);
		this.selectParentsComponent.createListDetailsTab(null, 2, "LIST 2");
		
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
		this.selectParentsComponent.createListDetailsTab(null, 1, LIST_NAME);
		this.selectParentsComponent.createListDetailsTab(null, 1, "LIST 2");
		
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
		this.selectParentsComponent.createListDetailsTab(null, 1, LIST_NAME);
		
		// "Close All tabs" is hidden if there's only one list
		Assert.assertFalse(this.selectParentsComponent.getCloseAllTabsButton().isVisible());
		
		// Check that "Close All tabs" is visible if there are 2 or more lists open
		this.selectParentsComponent.createListDetailsTab(null,1, "LIST 2");
		Assert.assertTrue(this.selectParentsComponent.getCloseAllTabsButton().isVisible());
	}
	
	@Test
	public void testToggleTabsheetButtonClick() {
		// Open 1 list in tabsheet
		this.selectParentsComponent.addListeners();
		this.selectParentsComponent.createListDetailsTab(null,1, LIST_NAME);
		Assert.assertTrue(this.selectParentsComponent.getListDetailsTabSheet().isVisible());
		
		// Check that tabsheet is hidden upon button click
		this.selectParentsComponent.getToggleTabsheetButton().click();
		Assert.assertFalse(this.selectParentsComponent.getListDetailsTabSheet().isVisible());
		
		// Check that tabsheet is visible again after another button click
		this.selectParentsComponent.getToggleTabsheetButton().click();
		Assert.assertTrue(this.selectParentsComponent.getListDetailsTabSheet().isVisible());
	}

	@Test
	public void testOpenBrowseForListDialog() {

		this.selectParentsComponent.openBrowseForListDialog();

		Mockito.verify(listTreeComponent).showAddRenameFolderSection(false);
		Mockito.verify(listTreeComponent).reinitializeTree(false);

		final ArgumentCaptor<Window> captor = ArgumentCaptor.forClass(Window.class);
		Mockito.verify(window).addWindow(captor.capture());

		final Window window = captor.getValue();

		Assert.assertNotNull(window.getListeners(SaveTreeStateListener.class));

	}

}
