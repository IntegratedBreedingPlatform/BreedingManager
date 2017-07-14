
package org.generationcp.breeding.manager.application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.dellroad.stuff.vaadin.ContextApplication;
import org.dellroad.stuff.vaadin.SpringContextApplication;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.listimport.GermplasmImportMain;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.hibernate.util.HttpRequestAwareUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.vaadin.terminal.Terminal;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

import java.util.Set;

public class BreedingManagerApplication extends SpringContextApplication implements ApplicationContextAware {

	private static final Logger LOG = LoggerFactory.getLogger(BreedingManagerApplication.class);

	private static final long serialVersionUID = 1L;

	public static final String GERMPLASM_IMPORT_WINDOW_NAME = "germplasm-import";
	public static final String GERMPLASM_IMPORT_WINDOW_NAME_POPUP = "germplasm-import-popup";
	public static final String CROSSING_MANAGER_WINDOW_NAME = "crosses";
	public static final String NURSERY_TEMPLATE_WINDOW_NAME = "nursery-template";
	public static final String LIST_MANAGER_WINDOW_NAME = "list-manager";
	public static final String LIST_MANAGER_WITH_OPEN_LIST_WINDOW_NAME = "listmanager-";
	public static final String LIST_MANAGER_SIDEBYSIDE = "list-manager-sidebyside";
	public static final String MANAGE_SETTINGS_CROSSING_MANAGER = "crosses-settings";
	public static final String NAVIGATION_FROM_NURSERY_PREFIX = "createcrosses";
	public static final String ID_PREFIX = "-";
	public static final String REQ_PARAM_NURSERY_ID = "nurseryid";
	public static final String REQ_PARAM_LIST_ID = "germplasmlistid";
	public static final String REQ_PARAM_CROSSES_LIST_ID = "crosseslistid";
    public static final String REQ_PARAM_BREEDING_METHOD_ID = "breedingmethodid";
	public static final String PATH_TO_NURSERY = "/Fieldbook/NurseryManager/";
	public static final String PATH_TO_EDIT_NURSERY = "/Fieldbook/NurseryManager/editNursery/";

	private Window window;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	private ApplicationContext applicationContext;

	private ListManagerMain listManagerMain;
	
	private ManageCrossingSettingsMain manageCrossingSettingsMain;

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public void initSpringApplication(final ConfigurableWebApplicationContext arg0) {

		this.window = this.instantiateListManagerWindow(BreedingManagerApplication.LIST_MANAGER_WINDOW_NAME);
		this.window.setDebugId("window");
		this.setMainWindow(this.window);
		this.setTheme("gcp-default");
		this.window.setSizeUndefined();

		// Override the existing error handler that shows the stack trace
		this.setErrorHandler(this);
	}

	@Override
	public Window getWindow(final String name) {
		// dynamically create other application-level windows which is associated with specific URLs
		// these windows are the jumping on points to parts of the application
		if (super.getWindow(name) == null) {
			if (name.equals(BreedingManagerApplication.GERMPLASM_IMPORT_WINDOW_NAME)) {
				final Window germplasmImportWindow = new Window(this.messageSource.getMessage(Message.IMPORT_GERMPLASM_LIST_TAB_LABEL));
				germplasmImportWindow.setDebugId("germplasmImportWindow");
				germplasmImportWindow.setName(BreedingManagerApplication.GERMPLASM_IMPORT_WINDOW_NAME);
				germplasmImportWindow.setSizeUndefined();
				germplasmImportWindow.setContent(new GermplasmImportMain(germplasmImportWindow, false));

				// Resize the popup windows (SubWindow) when the parent window is resized
				germplasmImportWindow.addListener(new Window.ResizeListener(){

					@Override
					public void windowResized(final Window.ResizeEvent resizeEvent) {
						Set<Window> childWindows = germplasmImportWindow.getChildWindows();
						for (Window childWindow : childWindows) {
							childWindow.setWidth("95%");
							childWindow.setHeight("97%");
						}
					}
				});

				// Set immediate as true so that everytime the browser resizes,
				// it will fire the ResizeEvent Listener
				germplasmImportWindow.setImmediate(true);

				// Set ResizeLazy to true so that only one event is fired when resizing
				germplasmImportWindow.setResizeLazy(true);

				this.addWindow(germplasmImportWindow);
				return germplasmImportWindow;

			} else if (name.equals(BreedingManagerApplication.GERMPLASM_IMPORT_WINDOW_NAME_POPUP)) {
				final Window germplasmImportWindow = new Window(this.messageSource.getMessage(Message.IMPORT_GERMPLASM_LIST_TAB_LABEL));
				germplasmImportWindow.setDebugId("germplasmImportWindow");

				germplasmImportWindow.setName(BreedingManagerApplication.GERMPLASM_IMPORT_WINDOW_NAME_POPUP);
				germplasmImportWindow.setSizeUndefined();
				germplasmImportWindow.setContent(new GermplasmImportMain(germplasmImportWindow, false, true));
				this.addWindow(germplasmImportWindow);
				return germplasmImportWindow;

			} else if (name.equals(BreedingManagerApplication.LIST_MANAGER_WINDOW_NAME)) {
				final Window listManagerWindow = this.instantiateListManagerWindow(name);
				listManagerWindow.setDebugId("listManagerWindow");
				this.addWindow(listManagerWindow);

				return listManagerWindow;

			} else if (name.startsWith(BreedingManagerApplication.LIST_MANAGER_WITH_OPEN_LIST_WINDOW_NAME)) {
				String listIdPart = name.substring(name.indexOf(ID_PREFIX) + 1);
				try {
					final Integer listId = Integer.parseInt(listIdPart);
					final Window listManagerWindow = new Window(this.messageSource.getMessage(Message.LIST_MANAGER_TAB_LABEL));
					listManagerWindow.setDebugId("listManagerWindow");
					listManagerWindow.setName(name);
					listManagerWindow.setSizeFull();

					this.listManagerMain = new org.generationcp.breeding.manager.listmanager.ListManagerMain(listId);

					listManagerWindow.setContent(this.listManagerMain);
					this.addWindow(listManagerWindow);

					return listManagerWindow;
				} catch (final NumberFormatException ex) {
					return getEmptyWindowWithErrorMessage();
				}

			} else if (name.equals(BreedingManagerApplication.CROSSING_MANAGER_WINDOW_NAME)) {
				final Window manageCrossingSettings = new Window(this.messageSource.getMessage(Message.MANAGE_CROSSES));
				manageCrossingSettings.setDebugId("manageCrossingSettings");
				manageCrossingSettings.setName(BreedingManagerApplication.CROSSING_MANAGER_WINDOW_NAME);
				manageCrossingSettings.setSizeUndefined();

				this.manageCrossingSettingsMain = new ManageCrossingSettingsMain(manageCrossingSettings);
				this.manageCrossingSettingsMain.setDebugId("manageCrossingSettingsMain");

				manageCrossingSettings.setContent(this.manageCrossingSettingsMain);
				this.addWindow(manageCrossingSettings);
				return manageCrossingSettings;
			} else if (name.startsWith(NAVIGATION_FROM_NURSERY_PREFIX)) {
				final Window manageCrossingSettings = new Window(this.messageSource.getMessage(Message.MANAGE_CROSSES));
				manageCrossingSettings.setDebugId("manageCrossingSettings");
				try {
					final String[] listIdParameterValues =
							BreedingManagerUtil.getApplicationRequest().getParameterValues(BreedingManagerApplication.REQ_PARAM_LIST_ID);
					final String listIdParam = listIdParameterValues != null && listIdParameterValues.length > 0 ?
							listIdParameterValues[0] : "";
					final Integer listId = Integer.parseInt(listIdParam);

					final String[] nurseryIdParameterValues =
							BreedingManagerUtil.getApplicationRequest().getParameterValues(BreedingManagerApplication.REQ_PARAM_NURSERY_ID);
					final String nurseryId = nurseryIdParameterValues != null && nurseryIdParameterValues.length > 0 ?
							nurseryIdParameterValues[0] : "";
					final boolean errorWithListIdReqParam = listId == -1;
					final boolean errorWithNurseryIdReqParam = nurseryId.isEmpty() || !NumberUtils.isDigits(nurseryId);

					manageCrossingSettings.setSizeUndefined();

					return validateAndConstructWindow(manageCrossingSettings, listId, errorWithListIdReqParam, errorWithNurseryIdReqParam);
				} catch (final NumberFormatException nfe) {
					return getWindowWithErrorMessage(manageCrossingSettings,
							this.messageSource.getMessage(Message.ERROR_WRONG_GERMPLASM_LIST_ID));
				}
			}
		}

		return super.getWindow(name);
	}

	private Window validateAndConstructWindow(final Window manageCrossingSettings, final Integer listId, final boolean errorWithListIdReqParam,
			final boolean errorWithNurseryIdReqParam) {
		if (!errorWithListIdReqParam && !errorWithNurseryIdReqParam) {
			constructCreateCrossesWindow(manageCrossingSettings, listId);
		} else if (errorWithListIdReqParam && errorWithNurseryIdReqParam) {
			return getWindowWithErrorMessage(manageCrossingSettings,
					this.messageSource.getMessage(Message.ERROR_WRONG_GERMPLASM_LIST_ID) + " "
							+ this.messageSource.getMessage(Message.ERROR_WRONG_NURSERY_ID));
		} else if  (errorWithNurseryIdReqParam) {
			constructCreateCrossesWindow(manageCrossingSettings, listId);
			MessageNotifier.showWarning(manageCrossingSettings, this.messageSource.getMessage(Message.ERROR_WITH_REQUEST_PARAMETERS),
					this.messageSource.getMessage(Message.ERROR_WRONG_NURSERY_ID));
		} else {
			return getWindowWithErrorMessage(manageCrossingSettings,
					this.messageSource.getMessage(Message.ERROR_WRONG_GERMPLASM_LIST_ID));
		}

		return manageCrossingSettings;
	}

	private void constructCreateCrossesWindow(final Window manageCrossingSettings, final Integer listId) {
		final GermplasmList germplasmList = germplasmListManager.getGermplasmListById(listId);
		this.manageCrossingSettingsMain = new ManageCrossingSettingsMain(manageCrossingSettings, germplasmList);
		this.manageCrossingSettingsMain.setDebugId("manageCrossingSettingsMain");
		manageCrossingSettings.setContent(this.manageCrossingSettingsMain);
		this.addWindow(manageCrossingSettings);
//		this.manageCrossingSettingsMain.nextStep();
	}

	private Window getWindowWithErrorMessage(final Window manageCrossingSettings, final String description) {
		this.manageCrossingSettingsMain = new ManageCrossingSettingsMain(manageCrossingSettings);
		this.manageCrossingSettingsMain.setDebugId("manageCrossingSettingsMain");
		manageCrossingSettings.setContent(this.manageCrossingSettingsMain);
		this.addWindow(manageCrossingSettings);
//		this.manageCrossingSettingsMain.nextStep();
		MessageNotifier.showWarning(this.getWindow(manageCrossingSettings.getName()),
				this.messageSource.getMessage(Message.ERROR_WITH_REQUEST_PARAMETERS),
				description);
		return manageCrossingSettings;
	}

	protected Window getEmptyWindowWithErrorMessage() {
		final Window emptyGermplasmListDetailsWindow = new Window(this.messageSource.getMessage(Message.LIST_MANAGER_TAB_LABEL));
		emptyGermplasmListDetailsWindow.setDebugId("emptyGermplasmListDetailsWindow");
		emptyGermplasmListDetailsWindow.setSizeUndefined();
		emptyGermplasmListDetailsWindow.addComponent(new Label(this.messageSource.getMessage(Message.INVALID_LIST_ID)));
		this.addWindow(emptyGermplasmListDetailsWindow);
		return emptyGermplasmListDetailsWindow;
	}

	private Window instantiateListManagerWindow(final String name) {
		final Window listManagerWindow = new Window(this.messageSource.getMessage(Message.LIST_MANAGER_TAB_LABEL));
		listManagerWindow.setDebugId("listManagerWindow");
		listManagerWindow.setName(name);
		listManagerWindow.setSizeFull();

		this.listManagerMain = new org.generationcp.breeding.manager.listmanager.ListManagerMain();
		listManagerMain.setDebugId("listManagerMain");
		listManagerWindow.setContent(this.listManagerMain);
		listManagerWindow.setDebugId("listManagerWindow");

		return listManagerWindow;
	}

	/**
	 * Override terminalError() to handle terminal errors, to avoid showing the stack trace in the application
	 */
	@Override
	public void terminalError(final Terminal.ErrorEvent event) {
		BreedingManagerApplication.LOG.error("An unchecked exception occurred: ", event.getThrowable());
		event.getThrowable().printStackTrace();
		// Some custom behaviour.
		if (this.getMainWindow() != null) {
			MessageNotifier.showError(this.getMainWindow(), this.messageSource.getMessage(Message.ERROR_INTERNAL), // TESTED
					this.messageSource.getMessage(Message.ERROR_PLEASE_CONTACT_ADMINISTRATOR)
							+ (event.getThrowable().getLocalizedMessage() == null ? "" : "</br>"
									+ event.getThrowable().getLocalizedMessage()));
		}
	}

	@Override
	public void close() {
		super.close();

		BreedingManagerApplication.LOG.debug("Application closed");
	}

	public static BreedingManagerApplication get() {
		return ContextApplication.get(BreedingManagerApplication.class);
	}

	@Override
	protected void doOnRequestStart(final HttpServletRequest request, final HttpServletResponse response) {
		super.doOnRequestStart(request, response);
		BreedingManagerApplication.LOG.trace("Request started " + request.getRequestURI() + "?" + request.getQueryString());
		synchronized (this) {
			HttpRequestAwareUtil.onRequestStart(this.applicationContext, request, response);
		}
	}

	@Override
	protected void doOnRequestEnd(final HttpServletRequest request, final HttpServletResponse response) {
		super.doOnRequestEnd(request, response);
		BreedingManagerApplication.LOG.trace("Request ended " + request.getRequestURI() + "?" + request.getQueryString());

		synchronized (this) {
			HttpRequestAwareUtil.onRequestEnd(this.applicationContext, request, response);
		}

	}

	public ListManagerMain getListManagerMain() {
		return this.listManagerMain;
	}

	public ManageCrossingSettingsMain getManageCrossingSettingsMain() {
		return this.manageCrossingSettingsMain;
	}

	public void refreshListManagerTree() {
		if (this.listManagerMain != null) {
			this.listManagerMain.getListSelectionComponent().getListTreeComponent().refreshComponent();
		}
	}

	public void refreshCrossingManagerTree() {
		final ManageCrossingSettingsMain manageCrossSettingsMain = this.getManageCrossingSettingsMain();
		if (manageCrossSettingsMain != null) {
			manageCrossSettingsMain.getMakeCrossesComponent().getSelectParentsComponent().getListTreeComponent().refreshComponent();
		}
	}

	public void updateUIForDeletedList(final GermplasmList germplasmList) {
		if (this.getListManagerMain() != null) {
			this.getListManagerMain().updateUIForDeletedList(germplasmList);
		}

		if (this.getManageCrossingSettingsMain() != null) {
			this.getManageCrossingSettingsMain().getMakeCrossesComponent().getParentsComponent().updateUIForDeletedList(germplasmList);
			this.getManageCrossingSettingsMain().getMakeCrossesComponent().showNodeOnTree(germplasmList.getId());
		}
	}

	public void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

}
