package org.generationcp.breeding.manager.application;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;
import junit.framework.Assert;
import org.generationcp.breeding.manager.listimport.GermplasmImportMain;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BreedingManagerWindowGeneratorTest {

	public static final String CAPTION = "TEST";
	public static final long PROGRAM_ID = 1l;
	public static final String LOCATION_MANAGER_PATH = "LOCATION_PATH";
	public static final String METHOD_MANAGER_PATH = "METHOD_PATH";

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@InjectMocks
	private BreedingManagerWindowGenerator breedingManagerWindowGenerator;

	@Before
	public void init() {

		final Tool locationManagerTool = new Tool();
		locationManagerTool.setPath(LOCATION_MANAGER_PATH);

		final Tool methodManagerTool = new Tool();
		methodManagerTool.setPath(METHOD_MANAGER_PATH);

		Mockito.when(workbenchDataManager.getToolWithName(BreedingManagerWindowGenerator.LOCATION_MANAGER_TOOL_NAME))
				.thenReturn(locationManagerTool);
		Mockito.when(workbenchDataManager.getToolWithName(BreedingManagerWindowGenerator.METHOD_MANAGER_TOOL_NAME))
				.thenReturn(methodManagerTool);
		Mockito.when(messageSource.getMessage(Message.IMPORT_GERMPLASM_LIST_TAB_LABEL)).thenReturn(CAPTION);
	}

	@Test
	public void testCreateGermplasmImportWindow() {

		final Window window = this.breedingManagerWindowGenerator.createGermplasmImportWindow();

		Assert.assertEquals("germplasmImportWindow", window.getDebugId());
		Assert.assertEquals(BreedingManagerWindowGenerator.GERMPLASM_IMPORT_WINDOW_NAME, window.getName());
		Assert.assertTrue(window.getContent() instanceof GermplasmImportMain);
		Assert.assertTrue(window.isImmediate());
		Assert.assertTrue(window.isResizeLazy());
		Assert.assertFalse(window.getListeners(Window.ResizeEvent.class).isEmpty());

	}

	@Test
	public void testOpenLocationManagerPopupWindow() {

		final Window parentWindow = new Window();

		final Window popupWindow = this.breedingManagerWindowGenerator.openLocationManagerPopupWindow(PROGRAM_ID, parentWindow, CAPTION);

		Assert.assertEquals("Popup window should be added to the parent window", popupWindow,
				parentWindow.getChildWindows().iterator().next());

		final Embedded embeddedBrowser = (Embedded) popupWindow.getContent().getComponentIterator().next();
		final ExternalResource externalResource = (ExternalResource) embeddedBrowser.getSource();

		Assert.assertEquals("The embedded resource should be pointed to the Location Manager URL", LOCATION_MANAGER_PATH + PROGRAM_ID,
				externalResource.getURL());

	}

	@Test
	public void testOpenMethodManagerPopupWindow() {

		final Window parentWindow = new Window();

		final Window popupWindow = this.breedingManagerWindowGenerator.openMethodManagerPopupWindow(PROGRAM_ID, parentWindow, CAPTION);

		Assert.assertEquals("Popup window should be added to the parent window", popupWindow,
				parentWindow.getChildWindows().iterator().next());

		final Embedded embeddedBrowser = (Embedded) popupWindow.getContent().getComponentIterator().next();
		final ExternalResource externalResource = (ExternalResource) embeddedBrowser.getSource();

		Assert.assertEquals("The embedded resource should be pointed to the Method Manager URL", METHOD_MANAGER_PATH + PROGRAM_ID,
				externalResource.getURL());

	}

	@Test
	public void testCreatePopupWindow() {

		final Layout popupContent = new VerticalLayout();
		final Window popupWindow = breedingManagerWindowGenerator.createPopupWindow(CAPTION, popupContent);

		Assert.assertEquals(95.0f, popupWindow.getWidth());
		Assert.assertEquals(97.0f, popupWindow.getHeight());
		Assert.assertTrue(popupWindow.isModal());
		Assert.assertFalse(popupWindow.isResizable());
		Assert.assertEquals(CAPTION, popupWindow.getCaption());
		Assert.assertEquals(popupContent, popupWindow.getContent());
		Assert.assertEquals(Reindeer.WINDOW_LIGHT, popupWindow.getStyleName());

	}

	@Test
	public void testCreatePopupWindowContent() {

		final ExternalResource externalResource = new ExternalResource("");
		final VerticalLayout content = (VerticalLayout) breedingManagerWindowGenerator.createPopupWindowContent(externalResource);

		Assert.assertEquals("layout", content.getDebugId());
		Assert.assertFalse(content.getMargin().hasTop());
		Assert.assertFalse(content.getMargin().hasBottom());
		Assert.assertFalse(content.getMargin().hasLeft());
		Assert.assertFalse(content.getMargin().hasRight());
		Assert.assertFalse(content.isSpacing());

		final Embedded embedded = (Embedded) content.getComponent(0);

		Assert.assertEquals("listInfoPage", embedded.getDebugId());
		Assert.assertEquals(Embedded.TYPE_BROWSER, embedded.getType());
		Assert.assertEquals(externalResource, embedded.getSource());

	}

	@Test
	public void testAdjustWindowContentBasedOnBrowserScreenSizeBrowserScreenSizeIsTooSmall() {

		final Window window = Mockito.mock(Window.class);
		final Layout content = Mockito.mock(Layout.class);

		final int browserWindowHeight = 768;

		Mockito.when(window.getContent()).thenReturn(content);
		Mockito.when(window.getBrowserWindowHeight()).thenReturn(browserWindowHeight);

		breedingManagerWindowGenerator.adjustWindowContentBasedOnBrowserScreenSize(window);

		// Verify that the content height is set to minimum height
		Mockito.verify(content).setHeight(BreedingManagerWindowGenerator.WINDOW_CONTENT_MINIMUM_HEIGHT, Sizeable.UNITS_PIXELS);

		// and the content size is NOT set to full
		Mockito.verify(content, Mockito.times(0)).setSizeFull();

	}

	@Test
	public void testAdjustWindowContentBasedOnBrowserScreenSizeBrowserScreenSizeIsBig() {

		final Window window = Mockito.mock(Window.class);
		final Layout content = Mockito.mock(Layout.class);

		final int browserWindowHeight = 801;

		Mockito.when(window.getContent()).thenReturn(content);
		Mockito.when(window.getBrowserWindowHeight()).thenReturn(browserWindowHeight);

		breedingManagerWindowGenerator.adjustWindowContentBasedOnBrowserScreenSize(window);

		// Verify that content size is set to full
		Mockito.verify(content).setSizeFull();

		// Verify that the content height is NOT set to minimum height
		Mockito.verify(content, Mockito.times(0))
				.setHeight(BreedingManagerWindowGenerator.WINDOW_CONTENT_MINIMUM_HEIGHT, Sizeable.UNITS_PIXELS);

	}

}
