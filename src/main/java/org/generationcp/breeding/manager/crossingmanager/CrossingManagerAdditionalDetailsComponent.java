package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
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
import com.vaadin.ui.Form;

@Configurable
public class CrossingManagerAdditionalDetailsComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent{
    
    public static final String NEXT_BUTTON_ID = "next button";
    public static final String BACK_BUTTON_ID = "back button";
    
    private static final long serialVersionUID = 9097810121003895303L;
    private final static Logger LOG = LoggerFactory.getLogger(CrossingManagerAdditionalDetailsComponent.class);
    
    private CrossingManagerMain source;
    private Accordion accordion;
    
    //Used Form to make use of fieldset HTML element to render section border
    private Form breedingMethodForm;
    private Form crossNameForm;
    private Form crossInfoForm;
    
    private Button backButton;
    private Button nextButton;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public CrossingManagerAdditionalDetailsComponent(CrossingManagerMain source, Accordion accordion){
    	this.source = source;
        this.accordion = accordion;
    }
    
    
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("640px");
        setWidth("800px");
        
        // Breeding Method section
        breedingMethodForm = new Form(new AdditionalDetailsBreedingMethodComponent());
        breedingMethodForm.setHeight("210px"); // make form size bigger than layout component layout
        breedingMethodForm.setWidth("740px");
        breedingMethodForm.setCaption(messageSource.getMessage(Message.BREEDING_METHOD));
		addComponent(breedingMethodForm, "top:30px;left:30px");
		
		// Cross Name section
        crossNameForm = new Form(new AdditionalDetailsCrossNameComponent());
        crossNameForm.setHeight("240px");  // make form size bigger than component layout
        crossNameForm.setWidth("740px");
        crossNameForm.setCaption(messageSource.getMessage(Message.CROSS_NAME));
		addComponent(crossNameForm, "top:200px;left:30px");
		
		// Cross Info section
		crossInfoForm = new Form(new AdditionalDetailsCrossInfoComponent());
		crossInfoForm.setHeight("120px");  // make form size bigger than layout component layout
		crossInfoForm.setWidth("740px");
		crossInfoForm.setCaption(messageSource.getMessage(Message.CROSS_INFO));
		addComponent(crossInfoForm, "top:440px;left:30px");
		
        backButton = new Button();
        backButton.setData(BACK_BUTTON_ID);
        addComponent(backButton, "top:585px;left:600px");
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        addComponent(nextButton, "top:585px;left:670px");
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
    }

    public CrossingManagerMain getSource() {
    	return source;
    }


	public Accordion getAccordion() {
		return accordion;
	}
}
