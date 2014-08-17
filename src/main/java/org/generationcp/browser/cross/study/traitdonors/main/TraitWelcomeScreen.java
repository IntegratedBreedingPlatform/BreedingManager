
package org.generationcp.browser.cross.study.traitdonors.main;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.traitdonors.main.listeners.TraitDonorButtonClickListener;
import org.generationcp.browser.exception.GermplasmStudyBrowserException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;

/**
 * First accordion panel for the Trait Donors Query. This panel provides an explanation of the query
 * and navigates into the next panel.
 * 
 * @author rebecca
 *
 */
@Configurable
public class TraitWelcomeScreen extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = -3667517088395779496L;

	private final static Logger LOG = LoggerFactory.getLogger(org.generationcp.browser.cross.study.adapted.main.WelcomeScreen.class);

	public static final String NEXT_BUTTON_ID = "TraitWelcomeScreen Next Button ID";

	private final TraitDonorsQueryMain mainScreen;
	private final PreselectTraitFilter nextScreen;

	private Label introductionMessage;

	private Button nextButton;

	@Autowired
	private CrossStudyDataManager crossStudyManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public TraitWelcomeScreen(TraitDonorsQueryMain mainScreen, PreselectTraitFilter nextScreen) {
		this.mainScreen = mainScreen;
		this.nextScreen = nextScreen;
	}

	@Override
	public void updateLabels() {

	}
	
	/**
	 * Sets a message into the panel, and provides a Next button for navigation.
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.setHeight("150px");
		this.setWidth("1000px");

		this.introductionMessage = new Label(this.messageSource.getMessage(Message.TRAIT_DONORS_QUERY_INTRODUCTION_MESSAGE));
		this.addComponent(this.introductionMessage, "top:30px; left:30px;");

		this.nextButton = new Button(this.messageSource.getMessage(Message.NEXT));
		this.nextButton.setData(TraitWelcomeScreen.NEXT_BUTTON_ID);
		this.nextButton.addListener(new TraitDonorButtonClickListener(this));
		this.nextButton.setWidth("80px");
		this.nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.addComponent(this.nextButton, "top:90px;left:460px");
	}
	
	/**
	 * Proceeds to the next screen - in this case the Preselect trait filter, where traits of interest for 
	 * assessment for adaptation is made.
	 * 
	 * @throws GermplasmStudyBrowserException
	 */
	public void nextButtonClickAction() throws GermplasmStudyBrowserException {

		this.proceedToNextScreen();
	}
	
	/*
	 * Selecting the first tab in order to highlight and activate tabs. Then enter the next tab via 
	 * the method to populate the Traits into a selectable tree
	 * 
	 */
	private void proceedToNextScreen() throws GermplasmStudyBrowserException {
		TraitWelcomeScreen.LOG.debug("Proceeding to Next Screen : Traits");
		this.mainScreen.selectFirstTab();
		this.nextScreen.populateTraitsTables();
	}

}
