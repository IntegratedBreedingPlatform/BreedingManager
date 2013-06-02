package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Form;

@Configurable
public class CrossingManagerAdditionalDetailsComponent extends AbsoluteLayout 
        implements InitializingBean, InternationalizableComponent, CrossesMadeContainer{
    
    public static final String NEXT_BUTTON_ID = "next button";
    public static final String BACK_BUTTON_ID = "back button";
    
    private static final long serialVersionUID = 9097810121003895303L;
    private final static Logger LOG = LoggerFactory.getLogger(CrossingManagerAdditionalDetailsComponent.class);
    
    private CrossingManagerMain source;
    private Accordion accordion;
    private CrossesMade crossesMade;
    
    //Used Form to make use of fieldset HTML element to render section border
    private Form crossingMethodForm;
    private Form crossNameForm;
    private Form crossInfoForm;
    
    private AdditionalDetailsBreedingMethodComponent crossingMethodComponent;
    private AdditionalDetailsCrossNameComponent crossNameComponent;
    private AdditionalDetailsCrossInfoComponent crossInfoComponent;
    
    private Button backButton;
    private Button nextButton;

    private CrossesMadeContainerUpdateListener[] updateListeners = new CrossesMadeContainerUpdateListener[3];
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private Component nextScreen;
    private Component previousScreen;
    
    public CrossingManagerAdditionalDetailsComponent(CrossingManagerMain source, Accordion accordion){
    	this.source = source;
        this.accordion = accordion;
    }
    
    public void setNextScreen(Component nextScreen) {
		this.nextScreen = nextScreen;
	}

	public AdditionalDetailsBreedingMethodComponent getBreedingMethodComponent() {
		return crossingMethodComponent;
	}

	public AdditionalDetailsCrossNameComponent getCrossNameComponent() {
		return crossNameComponent;
	}

	public AdditionalDetailsCrossInfoComponent getCrossInfoComponent() {
		return crossInfoComponent;
	}

	public CrossingManagerMain getSource() {
    	return source;
    }

	public Accordion getAccordion() {
		return accordion;
	}
	
	@Override
	public void setCrossesMade(CrossesMade crossesMade) {
		this.crossesMade = crossesMade;
	}
	
	@Override
	public CrossesMade getCrossesMade() {
		return this.crossesMade;
	}
    
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("640px");
        setWidth("800px");
        
        crossingMethodComponent = new AdditionalDetailsBreedingMethodComponent();
        crossingMethodForm = new Form(crossingMethodComponent);
        crossingMethodForm.setHeight("210px");
        crossingMethodForm.setWidth("740px");
		
        crossNameComponent = new AdditionalDetailsCrossNameComponent();
		crossNameForm = new Form(crossNameComponent);
        crossNameForm.setHeight("260px");
        crossNameForm.setWidth("740px");
		
		crossInfoComponent = new AdditionalDetailsCrossInfoComponent();
		crossInfoForm = new Form(crossInfoComponent);
		crossInfoForm.setHeight("120px");
		crossInfoForm.setWidth("740px");
		
		CrossingManagerImportButtonClickListener listener = new CrossingManagerImportButtonClickListener(this);
		
        backButton = new Button();
        backButton.setData(BACK_BUTTON_ID);
        backButton.addListener(listener);
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(listener);
        
        // Layout Components
        addComponent(crossingMethodForm, "top:20px;left:30px");
        addComponent(crossNameForm, "top:180px;left:30px");
        addComponent(crossInfoForm, "top:410px;left:30px");
        addComponent(backButton, "top:555px;left:600px");
        addComponent(nextButton, "top:555px;left:670px");
        
        setUpdateListeners();
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
    	messageSource.setCaption(backButton, Message.BACK);
    	messageSource.setCaption(nextButton, Message.NEXT);
    	messageSource.setCaption(crossingMethodForm, Message.CROSSING_METHOD);
    	messageSource.setCaption(crossNameForm, Message.CROSS_CODE);
    	messageSource.setCaption(crossInfoForm, Message.CROSS_INFO);
    }
    
    private void setUpdateListeners(){
    	crossingMethodComponent.setCrossesMadeContainer(this);
    	crossNameComponent.setCrossesMadeContainer(this);
    	crossInfoComponent.setCrossesMadeContainer(this);
    	
    	updateListeners[0] = crossingMethodComponent;
    	updateListeners[1] = crossNameComponent;
    	updateListeners[2] = crossInfoComponent;
    }
    
    public void setPreviousScreen(Component backScreen){
        this.previousScreen = backScreen;
    }    
    
    public Component getPreviousScreen(){
        return this.previousScreen;
    }
    
    public void nextButtonClickAction(){
    	boolean allValidationsPassed = true;
    	//perform validations and update CrossesMade instance
    	for (CrossesMadeContainerUpdateListener listener : updateListeners){
    		if (listener != null){
    			if (!listener.updateCrossesMadeContainer()){
    				allValidationsPassed = false;
    				break;
    			}
    		}
    	}  
    	
    	nextScreen = source.getWizardScreenFour();
    	source.getWizardScreenFour().setPreviousScreen(this);
    	
    	if (this.nextScreen != null && allValidationsPassed){
    	    source.enableWizardTabs();
    		assert this.nextScreen instanceof CrossesMadeContainer;
    		((CrossesMadeContainer) this.nextScreen).setCrossesMade(getCrossesMade());
    		source.setCrossesMade(getCrossesMade());
    		
    		this.accordion.setSelectedTab(nextScreen);
    		if(this.getPreviousScreen() instanceof CrossingManagerMakeCrossesComponent){
    		    source.enableWizardTabs();
    		} else{
    		    source.enableOnlyWizardTabFour();
    		    source.enableWizardTabThree();
    		    source.enableWizardTabOne();
    		}
    	}
    }
    
    public void backButtonClickAction(){
        if (this.previousScreen != null){
            source.enableWizardTabs();
            this.accordion.setSelectedTab(this.previousScreen);
        }
        
    }

}
