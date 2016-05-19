
package org.generationcp.breeding.manager.cross.study.adapted.main;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.adapted.main.listeners.AdaptedGermplasmButtonClickListener;
import org.generationcp.breeding.manager.cross.study.commons.EnvironmentFilter;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class WelcomeScreen extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = -3667517088395779496L;

	@SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory.getLogger(org.generationcp.breeding.manager.cross.study.adapted.main.WelcomeScreen.class);

	public static final String NEXT_BUTTON_ID = "WelcomeScreen Next Button ID";

	private final QueryForAdaptedGermplasmMain mainScreen;
	private final EnvironmentFilter nextScreen;

	private Label introductionMessage;

	private Button nextButton;

	@Autowired
	private CrossStudyDataManager crossStudyManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public WelcomeScreen(QueryForAdaptedGermplasmMain mainScreen, EnvironmentFilter nextScreen) {
		this.mainScreen = mainScreen;
		this.nextScreen = nextScreen;
	}

	@Override
	public void updateLabels() {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setHeight("150px");
		this.setWidth("1000px");

		this.introductionMessage = new Label(this.messageSource.getMessage(Message.QUERY_FOR_ADAPTED_GERMPLASM_INTRODUCTION_MESSAGE));
		this.addComponent(this.introductionMessage, "top:30px; left:30px;");

		this.nextButton = new Button(this.messageSource.getMessage(Message.NEXT));
		this.nextButton.setData(WelcomeScreen.NEXT_BUTTON_ID);
		this.nextButton.addListener(new AdaptedGermplasmButtonClickListener(this));
		this.nextButton.setWidth("80px");
		this.nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.addComponent(this.nextButton, "top:90px;left:460px");
	}

	public void nextButtonClickAction() {

		try {
			// show confirm dialog first if trial envts count is > 1k
			long trialEnvCount = this.crossStudyManager.countAllTrialEnvironments();
			if (trialEnvCount > 1000L) {
				String message = this.messageSource.getMessage(Message.LOAD_ENVIRONMENTS_CONFIRM, trialEnvCount);
				String publidDataYes = this.messageSource.getMessage(Message.LOAD_ENVIRONMENTS_INCLUDING_PUBLIC);
				String publidDataNo = this.messageSource.getMessage(Message.LOAD_ENVIRONMENTS_EXCLUDING_PUBLIC);
				ConfirmDialog confirmDialog =
						ConfirmDialog.show(this.getWindow(), "", message, publidDataYes, publidDataNo, new ConfirmDialog.Listener() {

							private static final long serialVersionUID = 1L;

							@Override
							public void onClose(ConfirmDialog dialog) {
								WelcomeScreen.this.nextScreen.setIncludePublicData(dialog.isConfirmed());
								WelcomeScreen.this.proceedToNextScreen();
							}
						});
				confirmDialog.getCancelButton().setStyleName(Reindeer.BUTTON_DEFAULT);
				confirmDialog.getOkButton().removeStyleName(Reindeer.BUTTON_DEFAULT);
				;
			} else {
				this.proceedToNextScreen();
			}
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
	}

	private void proceedToNextScreen() {
		this.mainScreen.selectFirstTab();
		this.nextScreen.populateEnvironmentsTable();
	}

}
