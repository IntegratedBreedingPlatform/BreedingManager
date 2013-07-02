package org.generationcp.breeding.manager.listimport;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

@Configurable
public class SaveGermplasmListComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent{

    private static final long serialVersionUID = -2761199444687629112L;
    private final static Logger LOG = LoggerFactory.getLogger(SaveGermplasmListComponent.class);
    
    private GermplasmImportMain source;
    
    public static final String BACK_BUTTON_ID = "back button";
    
    private Label listNameLabel;
    private Label descriptionLabel;
    private Label listTypeLabel;
    private Label listDateLabel;
    
    private TextField listNameText;
    private TextField descriptionText;
    
    private ComboBox listTypeComboBox;
    
    private DateField listDateField;
    
    private Button doneButton;
    private Button backButton;
    
    private Accordion accordion;
    private Component previousScreen;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public SaveGermplasmListComponent(GermplasmImportMain source, Accordion accordion){
        this.source = source;
        this.accordion = accordion;
    }
    
    public void setPreviousScreen(Component previousScreen){
        this.previousScreen = previousScreen;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("300px");
        setWidth("800px");
        
        listNameLabel = new Label();
        addComponent(listNameLabel, "top:30px;left:20px");
        
        listNameText = new TextField();
        listNameText.setWidth("400px");
        addComponent(listNameText, "top:10px;left:200px");
        
        descriptionLabel = new Label();
        addComponent(descriptionLabel, "top:60px;left:20px");
        
        descriptionText = new TextField();
        descriptionText.setWidth("400px");
        addComponent(descriptionText, "top:40px;left:200px");
        
        listTypeLabel = new Label();
        addComponent(listTypeLabel, "top:90px;left:20px");
        
        listTypeComboBox = new ComboBox();
        listTypeComboBox.setWidth("400px");
        addComponent(listTypeComboBox, "top:70px;left:200px");
        
        listDateLabel = new Label();
        addComponent(listDateLabel, "top:120px;left:20px");
        
        listDateField = new DateField();
        listDateField.setDateFormat("yyyy-MM-dd");
        listDateField.setResolution(DateField.RESOLUTION_DAY);
        addComponent(listDateField, "top:100px;left:200px");
        
        GermplasmImportButtonClickListener clickListener = new GermplasmImportButtonClickListener(this);
        
        backButton = new Button();
        backButton.setData(BACK_BUTTON_ID);
        backButton.addListener(clickListener);
        addComponent(backButton, "top:200px;left:600px");
        
        doneButton = new Button();
        addComponent(doneButton, "top:200px;left:670px");
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(listNameLabel, Message.LIST_NAME_LABEL);
        messageSource.setCaption(descriptionLabel, Message.LIST_DESCRIPTION_LABEL);
        messageSource.setCaption(listTypeLabel, Message.LIST_TYPE_LABEL);
        messageSource.setCaption(listDateLabel, Message.LIST_DATE_LABEL);
        messageSource.setCaption(backButton, Message.BACK);
        messageSource.setCaption(doneButton, Message.DONE);
    }

    public void backButtonClickAction(){
        if(this.previousScreen != null){
            this.accordion.setSelectedTab(previousScreen);
        } else{
            this.backButton.setEnabled(false);
        }
    }
    
    public GermplasmImportMain getSource() {
        return source;
    }    
}
