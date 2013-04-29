package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerUploader;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmListManager;
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
import com.vaadin.ui.Window.Notification;

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
    private Component nextNextScreen;
    
    private Label crossesOptionGroupLabel;
    private OptionGroup crossesOptionGroup;

    public CrossingManagerUploader crossingManagerUploader;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    public CrossingManagerImportFileComponent(CrossingManagerMain source, Accordion accordion){
    	this.source = source;
        this.accordion = accordion;
        this.nextScreen = null;
        this.nextNextScreen = null;
    }
    
    public void setNextScreen(Component nextScreen){
        this.nextScreen = nextScreen;
    }
    
    public void setNextNextScreen(Component nextNextScreen){
        this.nextNextScreen = nextNextScreen;
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
        
        crossingManagerUploader = new CrossingManagerUploader(this, germplasmListManager);
        uploadComponents.setReceiver(crossingManagerUploader);
        uploadComponents.addListener(crossingManagerUploader);
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(new CrossingManagerImportButtonClickListener(this));
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
    	if(crossingManagerUploader.getImportedGermplasmCrosses()==null){
    		getAccordion().getApplication().getMainWindow().showNotification("You must upload a nursery template file before clicking on next.", Notification.TYPE_ERROR_MESSAGE);
    	} else if(crossesOptionGroup.getValue()==null) {
    		getAccordion().getApplication().getMainWindow().showNotification("You should select an option for specifying crosses.", Notification.TYPE_ERROR_MESSAGE);
    	} else {
    		if(crossesOptionGroup.getValue().equals(messageSource.getMessage(Message.I_HAVE_ALREADY_DEFINED_CROSSES_IN_THE_NURSERY_TEMPLATE_FILE))){
    			if(crossingManagerUploader.getImportedGermplasmCrosses().getImportedGermplasmCrosses().size()==0){
    				getAccordion().getApplication().getMainWindow().showNotification("The nursery template file you uploaded doesn't contain any data on the second sheet.", Notification.TYPE_ERROR_MESSAGE);
    			} else {
    				if(this.nextScreen != null){
    	    			this.accordion.setSelectedTab(this.nextNextScreen);
    	        	} else {
    	        		this.nextButton.setEnabled(false);
    	        	}
    			}
    		} else {
	    		if(this.nextScreen != null){
	    			this.accordion.setSelectedTab(this.nextScreen);
	        	} else {
	        		this.nextButton.setEnabled(false);
	        	}
    		}
    	}
    }
    
    public Accordion getAccordion() {
    	return accordion;
    }
    
    public Component getNextScreen() {
    	return nextScreen;
    }
    
    public Component getNextNextScreen() {
    	return nextNextScreen;
    }
    
    public CrossingManagerMain getSource() {
    	return source;
    }
    
    public void selectManuallyMakeCrosses(){
    	crossesOptionGroup.setValue(messageSource.getMessage(Message.I_WANT_TO_MANUALLY_MAKE_CROSSES));
    }
    public void selectAlreadyDefinedCrossesInNurseryTemplateFile(){
    	crossesOptionGroup.setValue(messageSource.getMessage(Message.I_HAVE_ALREADY_DEFINED_CROSSES_IN_THE_NURSERY_TEMPLATE_FILE));
    }
}
