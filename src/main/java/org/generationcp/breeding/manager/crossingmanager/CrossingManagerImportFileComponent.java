package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.commons.exceptions.InternationalizableException;
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
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Upload;

@Configurable
public class CrossingManagerImportFileComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent{
    
    private static final long serialVersionUID = 9097810121003895303L;
    private final static Logger LOG = LoggerFactory.getLogger(CrossingManagerImportFileComponent.class);
    
    private CrossingManagerMain source;

    public static final String NEXT_BUTTON_ID = "next button";
    private Label selectFileLabel;
    private Upload uploadComponents;
    private Button nextButton;
    private Accordion accordion;
    private Component nextScreen;
    
    private Label crossesOptionGroupLabel;
    private OptionGroup crossesOptionGroup;

    
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public CrossingManagerImportFileComponent(CrossingManagerMain source, Accordion accordion){
    	this.source = source;
        this.accordion = accordion;
        this.nextScreen = null;
    }
    
    public void setNextScreen(Component nextScreen){
        this.nextScreen = nextScreen;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("300px");
        setWidth("800px");
        
        selectFileLabel = new Label();
        addComponent(selectFileLabel, "top:40px;left:30px");
        
        uploadComponents = new Upload();
        uploadComponents.setButtonCaption(messageSource.getMessage(Message.UPLOAD));
        addComponent(uploadComponents, "top:60px;left:30px");
        
	    //GermplasmListUploader germplasmListUploader = new GermplasmListUploader(this); 
	    //uploadComponents.setReceiver(germplasmListUploader);
	    //uploadComponents.addListener(germplasmListUploader);        
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(new GermplasmImportButtonClickListener(this));
        addComponent(nextButton, "top:250px;left:700px");
        
        crossesOptionGroupLabel = new Label();
        addComponent(crossesOptionGroupLabel, "top:140px;left:30px;");
        
        crossesOptionGroup = new OptionGroup();
        crossesOptionGroup.addItem(messageSource.getMessage(Message.I_HAVE_ALREADY_DEFINED_CROSSES_IN_THE_NURSERY_TEMPLATE_FILE));
        crossesOptionGroup.addItem(messageSource.getMessage(Message.I_WANT_TO_MANUALLY_MAKE_CROSSES));
        addComponent(crossesOptionGroup, "top:155px;left:30px;");
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(selectFileLabel, Message.SELECT_NURSERY_TEMPLATE_FILE);
        messageSource.setCaption(nextButton, Message.NEXT);
        messageSource.setCaption(crossesOptionGroupLabel,Message.SELECT_AN_OPTION_FOR_SPECIFYING_CROSSES);
    }

    public void nextButtonClickAction() throws InternationalizableException{
        if(this.nextScreen != null){
            this.accordion.setSelectedTab(this.nextScreen);
        } else {
            this.nextButton.setEnabled(false);
        }
    }
    
    public Accordion getAccordion() {
    	return accordion;
    }
    
    public Component getNextScreen() {
    	return nextScreen;
    }
    
    public CrossingManagerMain getSource() {
    	return source;
    }
}
