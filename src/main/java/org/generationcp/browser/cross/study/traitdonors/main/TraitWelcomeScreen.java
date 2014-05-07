package org.generationcp.browser.cross.study.traitdonors.main;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.browser.cross.study.traitdonors.main.listeners.TraitDonorButtonClickListener;
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

@Configurable
public class TraitWelcomeScreen extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {
	
    private static final long serialVersionUID = -3667517088395779496L;
    
	private final static Logger LOG = LoggerFactory.getLogger(org.generationcp.browser.cross.study.adapted.main.WelcomeScreen.class);
    
    public static final String NEXT_BUTTON_ID = "WelcomeScreen Next Button ID";
    
    private TraitDonorsQueryMain mainScreen;
	private PreselectTraitFilter nextScreen;
	
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

	@Override
	public void afterPropertiesSet() throws Exception {
	   setHeight("150px");
       setWidth("1000px");
       
       introductionMessage = new Label(messageSource.getMessage(Message.TRAIT_DONORS_QUERY_INTRODUCTION_MESSAGE));
       addComponent(introductionMessage, "top:30px; left:30px;");
       
       nextButton = new Button(messageSource.getMessage(Message.NEXT));
       nextButton.setData(NEXT_BUTTON_ID);
       nextButton.addListener(new TraitDonorButtonClickListener(this));
       nextButton.setWidth("80px");
       nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
       addComponent(nextButton, "top:90px;left:900px");
	}

    
    public void nextButtonClickAction(){
    	
//    	try {
//    		// show confirm dialog first if trial envts count is > 1k
//			if (crossStudyManager.countAllTrialEnvironments() > 1000L){
//				ConfirmDialog.show(getWindow(), "", 
//						messageSource.getMessage(Message.LOAD_ENVIRONMENTS_CONFIRM), "Yes", "No", new ConfirmDialog.Listener() {
//
//					private static final long serialVersionUID = 1L;
//					@Override
//					public void onClose(ConfirmDialog dialog) {
//						if (dialog.isConfirmed()){
//							proceedToNextScreen();
//						}
//					}
//				});
//				
//				
//			} else {
//				proceedToNextScreen();
//			}
//		} catch (MiddlewareQueryException e) {
//			e.printStackTrace();
//		}
    	
    	proceedToNextScreen();
    }
    
    // FIXME : Rebecca
	private void proceedToNextScreen() {
		LOG.debug("Proceeding to Next Screen : Traits");
		this.mainScreen.selectFirstTab();
		//this.nextScreen.populateEnvironmentsTable();
		List<EnvironmentForComparison> environments = new ArrayList<EnvironmentForComparison>();
		//this.nextScreen.populateTraitsTables(environments);
		this.nextScreen.populateTraitsTables(environments);
	}
    
}

