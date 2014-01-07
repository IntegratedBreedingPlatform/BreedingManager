package org.generationcp.breeding.manager.listimport;

import java.util.Iterator;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.breeding.manager.listimport.util.GermplasmListUploader;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
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
import com.vaadin.ui.Window.Notification;

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
    private Label filenameLabel;
    
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
        
        filenameLabel = new Label();
        addComponent(filenameLabel, "top:110px;left:35px;");
        
        germplasmListUploader = new GermplasmListUploader(this);
        uploadComponents.setReceiver(germplasmListUploader);
        uploadComponents.addListener(germplasmListUploader);        
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(new GermplasmImportButtonClickListener(this));
        nextButton.setEnabled(false);
        nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        addComponent(nextButton, "top:250px;left:700px");
        
    }

    public void enableNextButton(){
    	this.nextButton.setEnabled(true);
    }
    
    public void disableNextButton(){
    	this.nextButton.setEnabled(false);
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
        messageSource.setCaption(filenameLabel, Message.UPLOADED_FILE);
        filenameLabel.setCaption(filenameLabel.getCaption()+": ");
    }
    
    public void updateFilenameLabelValue(String filename){
        messageSource.setCaption(filenameLabel, Message.UPLOADED_FILE);
        filenameLabel.setCaption(filenameLabel.getCaption()+": "+filename);
    }    

    public void nextButtonClickAction() throws InternationalizableException{
    	if(germplasmListUploader.getFileIsValid()==null){
    		source.getApplication().getMainWindow().showNotification("Please upload a valid import file before clicking on the next button", Notification.TYPE_ERROR_MESSAGE);
    	} else if(germplasmListUploader.getFileIsValid()==false){
    		source.getApplication().getMainWindow().showNotification("Invalid import file, please upload a valid import file before clicking on the next button", Notification.TYPE_ERROR_MESSAGE);
    	} else {
	        if(this.nextScreen != null){
	        	source.enableAllTabs();
	            this.accordion.setSelectedTab(this.nextScreen);
	            source.enableTab(2);
	            source.alsoEnableTab(1);
	            //we set it here
	            if(getGermplasmDetailsComponent() != null
	                    && germplasmListUploader != null
	                    && germplasmListUploader.getImportedGermplasmList() != null){
	                    ImportedGermplasmList importedGermplasmList = germplasmListUploader.getImportedGermplasmList();
	                    List<ImportedGermplasm> importedGermplasms = importedGermplasmList.getImportedGermplasms();
	                    
	                    //Clear table contents first (possible that it has some rows in it from previous uploads, and then user went back to upload screen)
	                    getGermplasmDetailsComponent().getGermplasmDetailsTable().removeAllItems();
	                    String source;
	                    for(int i = 0 ; i < importedGermplasms.size() ; i++){
	                        ImportedGermplasm importedGermplasm  = importedGermplasms.get(i);
	                        if(importedGermplasm.getSource()==null){
	                        	source = importedGermplasmList.getFilename()+":"+(i+1);
	                        }else{
	                        	source=importedGermplasm.getSource();
	                        }
	                        getGermplasmDetailsComponent().getGermplasmDetailsTable().addItem(new Object[]{importedGermplasm.getEntryId(), importedGermplasm.getEntryCode(),importedGermplasm.getGid(), importedGermplasm.getDesig(), importedGermplasm.getCross(), source}, new Integer(i+1));
	                    }
	                    getGermplasmDetailsComponent().setImportedGermplasms(importedGermplasms);
	                    getGermplasmDetailsComponent().setGermplasmListUploader(germplasmListUploader);

	                    if(germplasmListUploader.importFileIsAdvanced()){
	                    	//getGermplasmDetailsComponent().setPedigreeOptionGroupValue(3);
	                    	getGermplasmDetailsComponent().setPedigreeOptionGroupEnabled(false);
	                    } else {
	                    	//getGermplasmDetailsComponent().setPedigreeOptionGroupValue(1);
	                    	getGermplasmDetailsComponent().setPedigreeOptionGroupEnabled(true);
	                    }
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
    
    public GermplasmImportMain getSource() {
        return source;
    }
}
