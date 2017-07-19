package org.generationcp.breeding.manager.application;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;
import org.generationcp.breeding.manager.listimport.GermplasmImportMain;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Resource;
import java.util.Set;

@Configurable
public class BreedingManagerWindowGenerator {

	public static final String GERMPLASM_IMPORT_WINDOW_NAME = "germplasm-import";

	public static final String LOCATION_MANAGER_TOOL_NAME = "locationmanager";
	public static final String LOCATION_MANAGER_DEFAULT_URL = "/ibpworkbench/content/ProgramLocations?programId=";
	public static final String METHOD_MANAGER_TOOL_NAME = "methodmanager";
	public static final String METHOD_MANAGER_DEFAULT_URL = "/ibpworkbench/content/ProgramMethods?programId=";

	public static final String POPUP_WINDOW_WIDTH = "95%";
	public static final String POPUP_WINDOW_HEIGHT = "97%";
	public static final int WINDOW_CONTENT_MINIMUM_HEIGHT = 900;
	public static final int BROWSER_WINDOW_HEIGHT_THRESHOLD = 800;

	@Resource
	private WorkbenchDataManager workbenchDataManager;

	@Resource
	private SimpleResourceBundleMessageSource messageSource;

	public Window createGermplasmImportWindow() {

		final Window germplasmImportWindow = new Window(this.messageSource.getMessage(Message.IMPORT_GERMPLASM_LIST_TAB_LABEL));
		germplasmImportWindow.setDebugId("germplasmImportWindow");
		germplasmImportWindow.setName(GERMPLASM_IMPORT_WINDOW_NAME);
		germplasmImportWindow.setSizeUndefined();
		germplasmImportWindow.setContent(new GermplasmImportMain(germplasmImportWindow, false));

		// Resize the popup windows (SubWindow) when the parent window is resized
		germplasmImportWindow.addListener(new Window.ResizeListener() {

			@Override
			public void windowResized(final Window.ResizeEvent resizeEvent) {
				final Set<Window> childWindows = germplasmImportWindow.getChildWindows();
				for (final Window childWindow : childWindows) {
					childWindow.setWidth(POPUP_WINDOW_WIDTH);
					childWindow.setHeight(POPUP_WINDOW_HEIGHT);

					adjustWindowContentBasedOnBrowserScreenSize(childWindow);
				}
			}
		});

		// Set immediate as true so that everytime the browser resizes,
		// it will fire the ResizeEvent Listener
		germplasmImportWindow.setImmediate(true);

		// Set ResizeLazy to true so that only one event is fired when resizing
		germplasmImportWindow.setResizeLazy(true);

		return germplasmImportWindow;

	}

	/**
	 * Opens and attaches a modal popup containing the location manager to a parent window
	 *
	 * @param programId    - used to load the locations for the given programId
	 * @param parentWindow - modal parentWindow will be attached to this parentWindow
	 * @return
	 */
	public Window openLocationManagerPopupWindow(final Long programId, final Window parentWindow, final String caption) {

		final Tool tool = this.workbenchDataManager.getToolWithName(BreedingManagerWindowGenerator.LOCATION_MANAGER_TOOL_NAME);

		ExternalResource listBrowserLink = null;
		if (tool == null) {
			listBrowserLink = new ExternalResource(BreedingManagerWindowGenerator.LOCATION_MANAGER_DEFAULT_URL + programId);
		} else {
			listBrowserLink = new ExternalResource(tool.getPath() + programId);
		}

		final Layout popupContent = this.createPopupWindowContent(listBrowserLink);
		final Window popupWindow = this.createPopupWindow(caption, popupContent);

		parentWindow.addWindow(popupWindow);

		return popupWindow;
	}

	/**
	 * Opens and attaches a modal popup containing the method manager to a parent window
	 *
	 * @param programId    - used to load the locations for the given programId
	 * @param parentWindow - modal window will be attached to this window
	 * @return
	 */
	public Window openMethodManagerPopupWindow(final Long programId, final Window parentWindow, final String caption) {

		final Tool tool = workbenchDataManager.getToolWithName(BreedingManagerWindowGenerator.METHOD_MANAGER_TOOL_NAME);

		ExternalResource listBrowserLink = null;
		if (tool == null) {
			listBrowserLink = new ExternalResource(BreedingManagerWindowGenerator.METHOD_MANAGER_DEFAULT_URL + programId);
		} else {
			listBrowserLink = new ExternalResource(tool.getPath() + programId);
		}

		final Layout popupContent = this.createPopupWindowContent(listBrowserLink);
		final Window popupWindow = this.createPopupWindow(caption, popupContent);

		parentWindow.addWindow(popupWindow);

		return popupWindow;
	}

	protected Window createPopupWindow(final String caption, final Layout content) {

		final Window popupWindow = new BaseSubWindow();
		popupWindow.setWidth(POPUP_WINDOW_WIDTH);
		popupWindow.setHeight(POPUP_WINDOW_HEIGHT);
		popupWindow.setModal(true);
		popupWindow.setResizable(false);
		popupWindow.center();
		popupWindow.setCaption(caption);
		popupWindow.setContent(content);
		popupWindow.addStyleName(Reindeer.WINDOW_LIGHT);
		return popupWindow;

	}

	protected Layout createPopupWindowContent(final ExternalResource listBrowserLink) {

		final VerticalLayout layout = new VerticalLayout();
		layout.setDebugId("layout");
		layout.setMargin(false);
		layout.setSpacing(false);
		layout.setHeight(WINDOW_CONTENT_MINIMUM_HEIGHT, Sizeable.UNITS_PIXELS);

		final Embedded listInfoPage = new Embedded("", listBrowserLink);
		listInfoPage.setDebugId("listInfoPage");
		listInfoPage.setType(Embedded.TYPE_BROWSER);
		listInfoPage.setSizeFull();

		layout.addComponent(listInfoPage);

		return layout;

	}

	protected void adjustWindowContentBasedOnBrowserScreenSize(Window window) {

		final int browserHeight = window.getBrowserWindowHeight();

		final Layout content = (Layout) window.getContent();

		// if the browser screen height is too small (less than or equal to 800 pixels) we should not try to
		// fit the content on the screen, as components will be cramped and difficult to use.
		if (browserHeight <= BROWSER_WINDOW_HEIGHT_THRESHOLD) {

			// So we set the content with a practical height so that the locations/methods tables are usable
			// and the components can all be seen through a scrollbar
			content.setHeight(WINDOW_CONTENT_MINIMUM_HEIGHT, Sizeable.UNITS_PIXELS);

		} else {
			// Otherwise if the screen is big, we adjust the content based on the size of its container (window)
			content.setSizeFull();
		}

	}

}
