package org.generationcp.breeding.manager.listimport;

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
import com.vaadin.ui.Upload;

@Configurable
public class GermplasmImportFileComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent{
    
    private static final long serialVersionUID = 9097810121003895303L;
    private final static Logger LOG = LoggerFactory.getLogger(GermplasmImportFileComponent.class);

    public static final String NEXT_BUTTON_ID = "next button";
    private Label selectFileLabel;
    private Upload uploadComponents;
    private Button nextButton;
    private Accordion accordion;
    private Component nextScreen;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public GermplasmImportFileComponent(Accordion accordion){
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
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(new GermplasmImportButtonClickListener(this));
        addComponent(nextButton, "top:250px;left:700px");
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(selectFileLabel, Message.SELECT_GERMPLASM_LIST_FILE);
        messageSource.setCaption(nextButton, Message.NEXT);
    }

    public void nextButtonClickAction() throws InternationalizableException{
        if(this.nextScreen != null){
            this.accordion.setSelectedTab(this.nextScreen);
        } else {
            this.nextButton.setEnabled(false);
        }
    }
}
