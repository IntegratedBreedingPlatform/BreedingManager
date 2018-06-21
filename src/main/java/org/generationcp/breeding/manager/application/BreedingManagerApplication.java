
package org.generationcp.breeding.manager.application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.dellroad.stuff.vaadin.ContextApplication;
import org.dellroad.stuff.vaadin.SpringContextApplication;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
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

public class BreedingManagerApplication extends SpringContextApplication implements ApplicationContextAware {

	private static final Logger LOG = LoggerFactory.getLogger(BreedingManagerApplication.class);

	private static final long serialVersionUID = 1L;

	public static final String LIST_MANAGER_WINDOW_NAME = "list-manager";
	public static final String NAVIGATION_FROM_STUDY_PREFIX = "createcrosses";
	public static final String REQ_PARAM_STUDY_ID = "studyid";
	public static final String REQ_PARAM_LIST_ID = "germplasmlistid";
	public static final String REQ_PARAM_STUDY_TYPE = "studyType";
	public static final String REQ_PARAM_CROSSES_LIST_ID = "crosseslistid";
	public static final String[] URL_STUDY = {"/Fieldbook/TrialManager/openTrial/","#/trialSettings"};

	private Window window;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private BreedingManagerWindowGenerator breedingManagerWindowGenerator;

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

			if (name.equals(BreedingManagerWindowGenerator.GERMPLASM_IMPORT_WINDOW_NAME)) {

				Window germplasmImportWindow = breedingManagerWindowGenerator.createGermplasmImportWindow();
				this.addWindow(germplasmImportWindow);
				return germplasmImportWindow;

			} else if (name.equals(BreedingManagerApplication.LIST_MANAGER_WINDOW_NAME)) {

				final Window listManagerWindow = this.instantiateListManagerWindow(name);
				listManagerWindow.setDebugId("listManagerWindow");
				this.addWindow(listManagerWindow);

				return listManagerWindow;

			} else if (name.startsWith(NAVIGATION_FROM_STUDY_PREFIX)) {

				final Window manageCrossingSettings = new Window(this.messageSource.getMessage(Message.MANAGE_CROSSES));
				manageCrossingSettings.setDebugId("manageCrossingSettings");
				try {
					final String[] listIdParameterValues =
							BreedingManagerUtil.getApplicationRequest().getParameterValues(BreedingManagerApplication.REQ_PARAM_LIST_ID);
					final String listIdParam = listIdParameterValues != null && listIdParameterValues.length > 0 ?
							listIdParameterValues[0] : "";
					final Integer listId = Integer.parseInt(listIdParam);

					final String[] studyIdParameterValues =
							BreedingManagerUtil.getApplicationRequest().getParameterValues(BreedingManagerApplication.REQ_PARAM_STUDY_ID);
					final String studyId = studyIdParameterValues != null && studyIdParameterValues.length > 0 ?
							studyIdParameterValues[0] : "";

					final String[] studyTypeParameterValues =
						BreedingManagerUtil.getApplicationRequest().getParameterValues(BreedingManagerApplication.REQ_PARAM_STUDY_TYPE);
					final String studyTypeParam =
						studyTypeParameterValues != null && studyTypeParameterValues.length > 0 ? listIdParameterValues[0] : "";

					final boolean errorWithListIdReqParam = listId == -1;
					final boolean errorWithStudyIdReqParam = studyId.isEmpty() || !NumberUtils.isDigits(studyId);
					final boolean errorWithStudyTypeReqParam = StringUtils.isBlank(studyTypeParam);


					manageCrossingSettings.setSizeUndefined();

					return validateAndConstructWindow(manageCrossingSettings, listId, errorWithListIdReqParam, errorWithStudyIdReqParam,
						errorWithStudyTypeReqParam);
				} catch (final NumberFormatException nfe) {
					return getWindowWithErrorMessage(manageCrossingSettings,
							this.messageSource.getMessage(Message.ERROR_WRONG_GERMPLASM_LIST_ID));
				}
			}
		}

		return super.getWindow(name);
	}

	private Window validateAndConstructWindow(final Window manageCrossingSettings, final Integer listId, final boolean errorWithListIdReqParam,
			final boolean errorWithStudyIdReqParam, final boolean errorWithStudyTypeReqParam) {
		if (!errorWithListIdReqParam && !errorWithStudyIdReqParam && !errorWithStudyTypeReqParam) {
			constructCreateCrossesWindow(manageCrossingSettings, listId);
		} else if (errorWithListIdReqParam && errorWithStudyIdReqParam && errorWithStudyTypeReqParam) {
			return getWindowWithErrorMessage(manageCrossingSettings,
					this.messageSource.getMessage(Message.ERROR_WRONG_GERMPLASM_LIST_ID) + " "
							+ this.messageSource.getMessage(Message.ERROR_WRONG_STUDY_ID) + " "
						+ this.messageSource.getMessage(Message.ERROR_WRONG_STUDY_TYPE));
		} else if  (errorWithStudyIdReqParam) {
			constructCreateCrossesWindow(manageCrossingSettings, listId);
			MessageNotifier.showWarning(manageCrossingSettings, this.messageSource.getMessage(Message.ERROR_WITH_REQUEST_PARAMETERS),
					this.messageSource.getMessage(Message.ERROR_WRONG_STUDY_ID));
		} else if  (errorWithStudyTypeReqParam) {
			constructCreateCrossesWindow(manageCrossingSettings, listId);
			MessageNotifier.showWarning(manageCrossingSettings, this.messageSource.getMessage(Message.ERROR_WITH_REQUEST_PARAMETERS),
				this.messageSource.getMessage(Message.ERROR_WRONG_STUDY_TYPE));
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
	}

	private Window getWindowWithErrorMessage(final Window manageCrossingSettings, final String description) {
		this.manageCrossingSettingsMain = new ManageCrossingSettingsMain(manageCrossingSettings);
		this.manageCrossingSettingsMain.setDebugId("manageCrossingSettingsMain");
		manageCrossingSettings.setContent(this.manageCrossingSettingsMain);
		this.addWindow(manageCrossingSettings);
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
		listManagerWindow.setResizable(true);

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
