package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.breeding.manager.listimport.util.GermplasmListUploader;
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
import com.vaadin.ui.Upload;

@Configurable
public class CrossingManagerDetailsComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent{
    
    private static final long serialVersionUID = 9097810121003895303L;
    private final static Logger LOG = LoggerFactory.getLogger(CrossingManagerDetailsComponent.class);
    
    private CrossingManagerMain source;
    private Accordion accordion;
    
    private Label germplasmListNameLabel;
    private Label germplasmListDescriptionLabel;
    private Label germplasmListTypeLabel;
    private Label germplasmListDateLabel;
    private TextField germplasmListName;
    private TextField germplasmListDescription;
    private ComboBox germplasmListType;
    private DateField germplasmListDate;
    private Button backButton;
    private Button doneButton;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
        
    
    public CrossingManagerDetailsComponent(CrossingManagerMain source, Accordion accordion){
    	this.source = source;
        this.accordion = accordion;
        
    }
    
    
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("300px");
        setWidth("800px");

        germplasmListNameLabel = new Label();
        germplasmListDescriptionLabel = new Label();
        germplasmListTypeLabel = new Label();
        germplasmListDateLabel = new Label();
        germplasmListName = new TextField();
        germplasmListDescription = new TextField();
        germplasmListType = new ComboBox();
        germplasmListDate = new DateField();
        backButton = new Button();
        doneButton = new Button();
        
        germplasmListName.setWidth("450px");
        germplasmListDescription.setWidth("450px");
        germplasmListType.setWidth("450px");
        
        addComponent(germplasmListNameLabel, "top:50px; left:30px;");
        addComponent(germplasmListDescriptionLabel, "top:80px; left:30px;");
        addComponent(germplasmListTypeLabel, "top:110px; left:30px;");
        addComponent(germplasmListDateLabel, "top:140px; left:30px;");
        
        addComponent(germplasmListName, "top:30px; left:200px;");
        addComponent(germplasmListDescription, "top:60px; left:200px;");
        addComponent(germplasmListType, "top:90px; left:200px;");
        addComponent(germplasmListDate, "top:120px; left:200px;");
        
        addComponent(backButton, "top:260px; left: 625px;");
        addComponent(doneButton, "top:260px; left: 700px;");
        
        germplasmListDate.setResolution(DateField.RESOLUTION_DAY);
		germplasmListDate.setDateFormat(CrossingManagerMain.DATE_FORMAT);
        
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
    	messageSource.setCaption(germplasmListNameLabel, Message.GERMPLASM_LIST_NAME);
    	messageSource.setCaption(germplasmListDescriptionLabel, Message.GERMPLASM_LIST_DESCRIPTION);
    	messageSource.setCaption(germplasmListTypeLabel, Message.GERMPLASM_LIST_TYPE);
    	messageSource.setCaption(germplasmListDateLabel, Message.GERMPLASM_LIST_DATE);
    	messageSource.setCaption(backButton, Message.BACK);
    	messageSource.setCaption(doneButton, Message.DONE);
    }

    public CrossingManagerMain getSource() {
    	return source;
    }
}
