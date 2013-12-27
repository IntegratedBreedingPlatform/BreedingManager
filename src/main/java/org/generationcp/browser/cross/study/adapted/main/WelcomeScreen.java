package org.generationcp.browser.cross.study.adapted.main;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.adapted.main.listeners.AdaptedGermplasmButtonClickListener;
import org.generationcp.browser.cross.study.commons.EnvironmentFilter;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;

@Configurable
public class WelcomeScreen extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {
	
    private static final long serialVersionUID = -3667517088395779496L;
    
    private final static Logger LOG = LoggerFactory.getLogger(org.generationcp.browser.cross.study.adapted.main.WelcomeScreen.class);
    
    public static final String NEXT_BUTTON_ID = "WelcomeScreen Next Button ID";
    
    private QueryForAdaptedGermplasmMain mainScreen;
	private EnvironmentFilter nextScreen;
	
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	   setHeight("150px");
       setWidth("1000px");
       
       introductionMessage = new Label(messageSource.getMessage(Message.QUERY_FOR_ADAPTED_GERMPLASM_INTRODUCTION_MESSAGE));
       addComponent(introductionMessage, "top:30px; left:30px;");
       
       nextButton = new Button(messageSource.getMessage(Message.NEXT));
       nextButton.setData(NEXT_BUTTON_ID);
       nextButton.addListener(new AdaptedGermplasmButtonClickListener(this));
       nextButton.setWidth("80px");
       nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
       addComponent(nextButton, "top:90px;left:900px");
	}

    
    public void nextButtonClickAction(){
    	
    	try {
    		// show confirm dialog first if trial envts count is > 1k
			if (crossStudyManager.countAllTrialEnvironments() > 1000L){
				ConfirmDialog.show(getWindow(), "", 
						messageSource.getMessage(Message.LOAD_ENVIRONMENTS_CONFIRM), "Yes", "No", new ConfirmDialog.Listener() {

					private static final long serialVersionUID = 1L;
					@Override
					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()){
							proceedToNextScreen();
						}
					}
				});
				
				
			} else {
				proceedToNextScreen();
			}
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	private void proceedToNextScreen() {
		this.mainScreen.selectFirstTab();
		this.nextScreen.populateEnvironmentsTable();
	}
    
}

