
package org.generationcp.breeding.manager.cross.study.h2h.main.dialogs;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.study.StudyAccordionMenu;
import org.generationcp.breeding.manager.study.StudyDetailComponent;
import org.generationcp.breeding.manager.util.CloseWindowAction;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

@Configurable
public class StudyInfoDialog extends BaseSubWindow implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = -7651767452229107837L;

	private final static Logger LOG = LoggerFactory.getLogger(FilterLocationDialog.class);

	public static final String CLOSE_SCREEN_BUTTON_ID = "StudyInfoDialog Close Button ID";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private Button cancelButton;
	private final Integer studyId;
	@Autowired
	private StudyDataManager studyDataManager;

	private final boolean h2hCall;

	public StudyInfoDialog(Component source, Window parentWindow, Integer studyId, boolean h2hCall) {
		this.studyId = studyId;
		this.h2hCall = h2hCall;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// set as modal window, other components are disabled while window is open
		this.setModal(true);
		// define window size, set as not resizable
		this.setWidth("1100px");
		this.setHeight("650px");
		this.setResizable(false);
		// center window within the browser
		this.center();

		AbsoluteLayout mainLayout = new AbsoluteLayout();
		mainLayout.setMargin(true);
		mainLayout.setWidth("1100px");
		mainLayout.setHeight("550px");

		try {
			Study study = this.studyDataManager.getStudy(this.studyId);
			this.setCaption("Study Information: " + study.getName());
			// don't show study details if study record is a Folder ("F")
			Accordion accordion =
					new StudyAccordionMenu(this.studyId, new StudyDetailComponent(this.studyDataManager, this.studyId),
							this.studyDataManager, false, this.h2hCall);
			accordion.setWidth("93%");
			accordion.setHeight("490px");
			mainLayout.addComponent(accordion, "top:10px;left:5px");
		} catch (NumberFormatException e) {
			StudyInfoDialog.LOG.error(e.toString() + "\n" + e.getStackTrace());
			e.printStackTrace();
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
					this.messageSource.getMessage(Message.ERROR_IN_NUMBER_FORMAT));
		} catch (MiddlewareQueryException e) {
			StudyInfoDialog.LOG.error(e.toString() + "\n" + e.getStackTrace());
			e.printStackTrace();
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID));
		}

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);

		this.cancelButton = new Button("Close");
		this.cancelButton.setData(StudyInfoDialog.CLOSE_SCREEN_BUTTON_ID);
		this.cancelButton.addListener(new CloseWindowAction());

		buttonLayout.addComponent(this.cancelButton);
		mainLayout.addComponent(buttonLayout, "top:520px;left:950px");

		this.addComponent(mainLayout);
	}

	@Override
	public void updateLabels() {

	}
}
