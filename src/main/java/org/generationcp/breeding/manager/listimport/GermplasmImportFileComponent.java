package org.generationcp.breeding.manager.listimport;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.breeding.manager.listimport.util.GermplasmListUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.*;
//import com.vaadin.ui.AbsoluteLayout;
//import com.vaadin.ui.Accordion;
//import com.vaadin.ui.Button;
//import com.vaadin.ui.Component;
//import com.vaadin.ui.Label;
//import com.vaadin.ui.Upload;

import java.util.Iterator;
import java.util.List;

@Configurable
public class GermplasmImportFileComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {
    
    private static final long serialVersionUID = 9097810121003895303L;
    private final static Logger LOG = LoggerFactory.getLogger(GermplasmImportFileComponent.class);
    
    private GermplasmImportMain source;

    public static final String NEXT_BUTTON_ID = "next button";
    private Label selectFileLabel;
    private Upload uploadComponents;
    private Button nextButton;
    private Accordion accordion;
    private Component nextScreen;
    private GermplasmListUploader germplasmListUploader;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public GermplasmImportFileComponent(GermplasmImportMain source, Accordion accordion){
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
        
        germplasmListUploader = new GermplasmListUploader(this);
        uploadComponents.setReceiver(germplasmListUploader);
        uploadComponents.addListener(germplasmListUploader);        
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(new GermplasmImportButtonClickListener(this));
        addComponent(nextButton, "top:250px;left:700px");
    }


    private SpecifyGermplasmDetailsComponent getGermplasmDetailsComponent (){
        if(this.accordion != null){
            Iterator<Component> componentIterator = this.accordion.getComponentIterator();
            while(componentIterator.hasNext()){
                Component component = componentIterator.next();
                if(component instanceof SpecifyGermplasmDetailsComponent)
                    return (SpecifyGermplasmDetailsComponent) component;
            }

        }
        return null;
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
            //we set it here
            if(getGermplasmDetailsComponent() != null
                    && germplasmListUploader != null
                    && germplasmListUploader.getImportedGermplasmList() != null){
                    ImportedGermplasmList importedGermplasmList = germplasmListUploader.getImportedGermplasmList();
                    List<ImportedGermplasm> importedGermplasms = importedGermplasmList.getImportedGermplasms();
                    for(int i = 0 ; i < importedGermplasms.size() ; i++){
                        ImportedGermplasm importedGermplasm  = importedGermplasms.get(i);
                        String source = importedGermplasmList.getFilename()+":"+(i+1);
                        getGermplasmDetailsComponent().getGermplasmDetailsTable().addItem(new Object[]{importedGermplasm.getEntryId(), "", importedGermplasm.getDesig(), "", source}, new Integer(i+1));
                    }
                    getGermplasmDetailsComponent().setImportedGermplasms(importedGermplasms);
                    getGermplasmDetailsComponent().setGermplasmListUploader(germplasmListUploader);
            }
        }
    }
    
    public Accordion getAccordion() {
        return accordion;
    }
    
    public Component getNextScreen() {
        return nextScreen;
    }
    
    public GermplasmImportMain getSource() {
        return source;
    }
}
