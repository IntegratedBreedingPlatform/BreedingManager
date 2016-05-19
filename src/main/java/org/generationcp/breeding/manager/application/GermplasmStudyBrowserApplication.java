/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.breeding.manager.application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dellroad.stuff.vaadin.ContextApplication;
import org.dellroad.stuff.vaadin.SpringContextApplication;
import org.generationcp.breeding.manager.cross.study.adapted.main.QueryForAdaptedGermplasmMain;
import org.generationcp.breeding.manager.cross.study.h2h.HeadToHeadComparisonMain;
import org.generationcp.breeding.manager.cross.study.h2h.main.HeadToHeadCrossStudyMain;
import org.generationcp.breeding.manager.cross.study.traitdonors.main.TraitDonorsQueryMain;
import org.generationcp.breeding.manager.germplasm.GermplasmDetailsComponentTree;
import org.generationcp.breeding.manager.germplasm.GermplasmQueries;
import org.generationcp.breeding.manager.study.StudyAccordionMenu;
import org.generationcp.breeding.manager.study.StudyBrowserMain;
import org.generationcp.breeding.manager.study.StudyDetailComponent;
import org.generationcp.breeding.manager.study.StudyTreeComponent;
import org.generationcp.breeding.manager.util.awhere.AWhereFormComponent;
import org.generationcp.commons.hibernate.DynamicManagerFactoryProviderConcurrency;
import org.generationcp.commons.hibernate.util.HttpRequestAwareUtil;
import org.generationcp.commons.vaadin.actions.UpdateComponentLabelsAction;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.vaadin.terminal.Terminal;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

/**
 * The main Vaadin application class for the project.
 *
 */
@Configurable
public class GermplasmStudyBrowserApplication extends SpringContextApplication implements ApplicationContextAware {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmStudyBrowserApplication.class);

	private static final long serialVersionUID = 1L;

	public static final String STUDY_WINDOW_NAME = "study";
	public static final String STUDY_DETAILS_PREFIX = "study-";
	public static final String STUDY_BROWSER_PREFIX = "studybrowser-";
	public static final String GERMPLASM_DETAILS_PREFIX = "germplasm-";
	public static final String HEAD_TO_HEAD_CROSS_STUDY_QUERY_WINDOW_NAME = "h2h-query";
	public static final String HEAD_TO_HEAD_COMPARISON_WINDOW_NAME = "Head_to_head_comparison";
	public static final String QUERY_FOR_ADAPTED_GERMPLASM_WINDOW_NAME = "Query_For_Adapted_Germplasm";
	public static final String TRAIT_DONORS_QUERY_NAME = "Trait_Donors_Query";
	public static final String AWHERE_WINDOW_NAME = "awheretool";

	private static final String HTML_BREAK = "</br>";

	private Window window;

	@Autowired
	private DynamicManagerFactoryProviderConcurrency managerFactoryProvider;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private UpdateComponentLabelsAction messageSourceListener;

	private ApplicationContext applicationContext;

	@Autowired
	private StudyDataManager studyDataManager;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public void initSpringApplication(ConfigurableWebApplicationContext arg0) {

		// create blank root layouts for the other tabs, the content will be
		// added as the tabs are selected or as the buttons on the WelcomeTab are clicked
		this.messageSourceListener = new UpdateComponentLabelsAction(this);
		this.messageSource.addListener(this.messageSourceListener);

		this.window = this.instantiateStudyBrowserWindow();
		this.setMainWindow(this.window);
		this.setTheme("gcp-default");
		this.window.setSizeUndefined();

		// Override the existing error handler that shows the stack trace
		this.setErrorHandler(this);
	}

	@Override
	public Window getWindow(String name) {
		// dynamically create other application-level windows which is associated with specific URLs
		// these windows are the jumping on points to parts of the application
		if (super.getWindow(name) == null) {
			if (GermplasmStudyBrowserApplication.STUDY_WINDOW_NAME.equals(name)) {
				Window studyBrowserWindow = this.instantiateStudyBrowserWindow();
				this.addWindow(studyBrowserWindow);
				return studyBrowserWindow;
			} else if (name.startsWith(GermplasmStudyBrowserApplication.STUDY_BROWSER_PREFIX)) {
				String studyIdPart = name.substring(name.indexOf("-") + 1);
				int studyId = 0;

				Window studyBrowserWindow;
				String windowName = GermplasmStudyBrowserApplication.STUDY_WINDOW_NAME + studyId;

				this.removeWindow(this.getWindow(windowName));

				StudyBrowserMain studyBrowserMain = new StudyBrowserMain();
				studyBrowserWindow = this.getWindow(windowName);

				if (studyBrowserWindow == null) {
					studyBrowserWindow = new Window(this.messageSource.getMessage(Message.STUDY_BROWSER_TITLE)); // Study
					studyBrowserWindow.setName(windowName);
					studyBrowserWindow.setSizeUndefined();
					studyBrowserWindow.addComponent(studyBrowserMain);
					this.addWindow(studyBrowserWindow);
				}

				StudyTreeComponent studyTreeComponent = null;

				try {
					studyId = Integer.parseInt(studyIdPart);
				} catch (NumberFormatException e) {
					GermplasmStudyBrowserApplication.LOG.debug("Error parsing studyId", e);
					MessageNotifier.showError(this.getWindow(windowName), this.messageSource.getMessage(Message.ERROR_INTERNAL),
							this.messageSource.getMessage(Message.INVALID_PARAMETERS_SPECIFIED));
					return studyBrowserWindow;
				}

				studyTreeComponent = studyBrowserMain.getCombinedStudyTreeComponent();

				if (studyTreeComponent.studyExists(studyId)) {
					studyTreeComponent.studyTreeItemClickAction(studyId);
					studyTreeComponent.showChild(studyId);
				} else {
					MessageNotifier.showError(this.getWindow(windowName), this.messageSource.getMessage(Message.ERROR_INTERNAL),
							this.messageSource.getMessage(Message.NO_STUDIES_FOUND));
				}
				return studyBrowserWindow;

			} else if (name.startsWith(GermplasmStudyBrowserApplication.STUDY_DETAILS_PREFIX)) {
				String studyIdPart = name.substring(name.indexOf("-") + 1);
				try {
					int studyId = Integer.parseInt(studyIdPart);
					// "Study Details" + study id
					Window studyDetailsWindow = new Window(this.messageSource.getMessage(Message.STUDY_DETAILS_TEXT) + " " + studyId);
					studyDetailsWindow.setSizeUndefined();
					// TODO should disable export functions for this screen
					studyDetailsWindow.addComponent(new StudyAccordionMenu(studyId,
							new StudyDetailComponent(this.studyDataManager, studyId), this.studyDataManager, true, false));
					this.addWindow(studyDetailsWindow);
					return studyDetailsWindow;
				} catch (Exception ex) {
					GermplasmStudyBrowserApplication.LOG.error(
							this.messageSource.getMessage(Message.ERROR_IN_CREATING_STUDY_DETAILS_WINDOW) + " " + name + ex.toString()
									+ "\n" + ex.getStackTrace(), ex);
					Window emptyStudyDetailsWindow = new Window(this.messageSource.getMessage(Message.STUDY_DETAILS_TEXT));
					emptyStudyDetailsWindow.setSizeUndefined();
					emptyStudyDetailsWindow.addComponent(new Label(this.messageSource.getMessage(Message.NULL_STUDY_DETAILS) + " "
							+ studyIdPart));
					this.addWindow(emptyStudyDetailsWindow);
					return emptyStudyDetailsWindow;
				}
			} else if (name.startsWith(GermplasmStudyBrowserApplication.GERMPLASM_DETAILS_PREFIX)) {
				String gidPart = name.substring(name.indexOf("-") + 1);
				try {
					int gid = Integer.parseInt(gidPart);
					Window germplasmDetailsWindow = new Window(this.messageSource.getMessage(Message.GERMPLASM_DETAILS_TEXT) + " " + gid);
					germplasmDetailsWindow.setSizeUndefined();
					germplasmDetailsWindow.setSizeFull();

					GermplasmQueries queries = new GermplasmQueries();
					germplasmDetailsWindow.addComponent(new GermplasmDetailsComponentTree(gid, queries));

					this.addWindow(germplasmDetailsWindow);
					return germplasmDetailsWindow;
				} catch (Exception ex) {
					GermplasmStudyBrowserApplication.LOG.error(
							this.messageSource.getMessage(Message.ERROR_IN_CREATING_GERMPLASM_DETAILS_WINDOW) + " " + name + ex.toString()
									+ "\n" + ex.getStackTrace(), ex);
					Window emptyGermplasmDetailsWindow = new Window(this.messageSource.getMessage(Message.GERMPLASM_DETAILS_TEXT));
					emptyGermplasmDetailsWindow.setSizeUndefined();
					emptyGermplasmDetailsWindow.addComponent(new Label(this.messageSource.getMessage(Message.NULL_GERMPLASM_DETAILS) + " "
							+ gidPart));
					this.addWindow(emptyGermplasmDetailsWindow);
					return emptyGermplasmDetailsWindow;
				}
			} else if (GermplasmStudyBrowserApplication.HEAD_TO_HEAD_CROSS_STUDY_QUERY_WINDOW_NAME.equals(name)) {
				Window headToHeadQueryToolWindow = new Window("Cross Study: Head-to-Head Comparison");
				// Browser
				headToHeadQueryToolWindow.setName(GermplasmStudyBrowserApplication.HEAD_TO_HEAD_CROSS_STUDY_QUERY_WINDOW_NAME);
				headToHeadQueryToolWindow.setSizeUndefined();
				headToHeadQueryToolWindow.setContent(new HeadToHeadComparisonMain());
				this.addWindow(headToHeadQueryToolWindow);
				return headToHeadQueryToolWindow;
			} else if (GermplasmStudyBrowserApplication.HEAD_TO_HEAD_COMPARISON_WINDOW_NAME.equals(name)) {
				Window headToHeadQueryToolWindow = new Window("Cross Study: Head-to-Head Comparison");
				// Browser
				headToHeadQueryToolWindow.setName(GermplasmStudyBrowserApplication.HEAD_TO_HEAD_COMPARISON_WINDOW_NAME);
				headToHeadQueryToolWindow.setSizeUndefined();
				headToHeadQueryToolWindow.setContent(new HeadToHeadCrossStudyMain());
				this.addWindow(headToHeadQueryToolWindow);
				return headToHeadQueryToolWindow;
			} else if (GermplasmStudyBrowserApplication.QUERY_FOR_ADAPTED_GERMPLASM_WINDOW_NAME.equals(name)) {
				Window queryForAdaptedGermplasmToolWindow = new Window("Cross Study: Query-for-Adapted Germplasm");
				// Browser
				queryForAdaptedGermplasmToolWindow.setName(GermplasmStudyBrowserApplication.QUERY_FOR_ADAPTED_GERMPLASM_WINDOW_NAME);
				queryForAdaptedGermplasmToolWindow.setSizeUndefined();
				queryForAdaptedGermplasmToolWindow.setContent(new QueryForAdaptedGermplasmMain());
				this.addWindow(queryForAdaptedGermplasmToolWindow);
				return queryForAdaptedGermplasmToolWindow;
			} else if (GermplasmStudyBrowserApplication.TRAIT_DONORS_QUERY_NAME.equals(name)) {
				Window traitDonorsQueryToolWindow = new Window("Cross Study: Trait-Donors-Query");
				// Browser
				traitDonorsQueryToolWindow.setName(GermplasmStudyBrowserApplication.TRAIT_DONORS_QUERY_NAME);
				traitDonorsQueryToolWindow.setSizeUndefined();
				traitDonorsQueryToolWindow.setContent(new TraitDonorsQueryMain());
				this.addWindow(traitDonorsQueryToolWindow);
				return traitDonorsQueryToolWindow;
			} else if (GermplasmStudyBrowserApplication.AWHERE_WINDOW_NAME.equals(name)) {
				Window awhereWindow = new Window("AWhere Test Tool");
				awhereWindow.setName(GermplasmStudyBrowserApplication.AWHERE_WINDOW_NAME);
				awhereWindow.addComponent(new AWhereFormComponent());
				awhereWindow.setWidth("100%");
				awhereWindow.setHeight("100%");
				this.addWindow(awhereWindow);
				return awhereWindow;
			}
		}
		return super.getWindow(name);
	}

	private Window instantiateStudyBrowserWindow() {
		Window studyBrowserWindow = new Window(this.messageSource.getMessage(Message.STUDY_BROWSER_TITLE)); // Study
		// Browser
		studyBrowserWindow.setName(GermplasmStudyBrowserApplication.STUDY_WINDOW_NAME);
		studyBrowserWindow.setSizeUndefined();
		StudyBrowserMain studyBrowserMain = new StudyBrowserMain();
		studyBrowserWindow.setContent(studyBrowserMain);
		return studyBrowserWindow;
	}

	/**
	 * Override terminalError() to handle terminal errors, to avoid showing the stack trace in the application
	 */
	@Override
	public void terminalError(Terminal.ErrorEvent event) {
		GermplasmStudyBrowserApplication.LOG.error("An unchecked exception occurred: ", event.getThrowable());
		// Some custom behaviour.
		if (this.getMainWindow() != null) {
			MessageNotifier.showError(this.getMainWindow(), this.messageSource.getMessage(Message.ERROR_INTERNAL), // TESTED
					this.messageSource.getMessage(Message.ERROR_PLEASE_CONTACT_ADMINISTRATOR)
							+ (event.getThrowable().getLocalizedMessage() == null ? "" : GermplasmStudyBrowserApplication.HTML_BREAK
									+ event.getThrowable().getLocalizedMessage()));
		}
	}

	@Override
	public void close() {
		super.close();
		// implement this when we need to do something on session timeout
		this.messageSource.removeListener(this.messageSourceListener);
		GermplasmStudyBrowserApplication.LOG.debug("Application closed");
	}

	public static GermplasmStudyBrowserApplication get() {
		return ContextApplication.get(GermplasmStudyBrowserApplication.class);
	}

	@Override
	protected void doOnRequestStart(HttpServletRequest request, HttpServletResponse response) {
		GermplasmStudyBrowserApplication.LOG.trace("Request started " + request.getRequestURI() + "?" + request.getQueryString());
		synchronized (this) {
			HttpRequestAwareUtil.onRequestStart(this.applicationContext, request, response);
		}
		super.doOnRequestStart(request, response);
	}

	@Override
	protected void doOnRequestEnd(HttpServletRequest request, HttpServletResponse response) {
		super.doOnRequestEnd(request, response);

		GermplasmStudyBrowserApplication.LOG.trace("Request ended " + request.getRequestURI() + "?" + request.getQueryString());

		synchronized (this) {
			HttpRequestAwareUtil.onRequestEnd(this.applicationContext, request, response);
		}
	}
}
