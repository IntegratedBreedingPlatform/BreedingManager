package org.generationcp.breeding.manager.listimport;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.UploadField;
import org.generationcp.breeding.manager.listimport.exceptions.GermplasmImportException;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.breeding.manager.listimport.util.GermplasmListUploader;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Upload;

@Configurable
public class GermplasmImportFileComponent extends AbsoluteLayout implements InitializingBean, 
		InternationalizableComponent, BreedingManagerLayout {
    
    private static final long serialVersionUID = 9097810121003895303L;
    @SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory.getLogger(GermplasmImportFileComponent.class);
    
    private GermplasmImportMain source;

    public static final String NEXT_BUTTON_ID = "next button";
    private Label selectFileLabel;
    private UploadField uploadComponents;
    private Button nextButton;
    private GermplasmListUploader germplasmListUploader;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public GermplasmImportFileComponent(GermplasmImportMain source){
        this.source = source;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
       instantiateComponents();
       initializeValues();
       addListeners();
       layoutComponents();
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(nextButton, Message.NEXT);
    }
    
    public void nextButtonClickAction() throws InternationalizableException{
    	try {
    		germplasmListUploader.validate();
    		MessageNotifier.showMessage(source.getWindow(), "Success", "File was successfully uploaded");
    		
    		source.nextStep();
			
		} catch (GermplasmImportException e) {
			MessageNotifier.showError(getWindow(), e.getCaption(), e.getMessage());
		}
    	
//    	if(germplasmListUploader.getFileIsValid()==null){
//    		source.getApplication().getMainWindow().showNotification("Please upload a valid import file before clicking on the next button", Notification.TYPE_ERROR_MESSAGE);
//    	} else if(germplasmListUploader.getFileIsValid()==false){
//    		source.getApplication().getMainWindow().showNotification("Invalid import file, please upload a valid import file before clicking on the next button", Notification.TYPE_ERROR_MESSAGE);
//    	} else {
	            //we set it here
//	            if(getGermplasmDetailsComponent() != null
//	                    && germplasmListUploader != null
//	                    && germplasmListUploader.getImportedGermplasmList() != null){
//	                    ImportedGermplasmList importedGermplasmList = germplasmListUploader.getImportedGermplasmList();
//	                    List<ImportedGermplasm> importedGermplasms = importedGermplasmList.getImportedGermplasms();
//	                    
//	                    //Clear table contents first (possible that it has some rows in it from previous uploads, and then user went back to upload screen)
//	                    getGermplasmDetailsComponent().getGermplasmDetailsTable().removeAllItems();
//	                    String source;
//	                    for(int i = 0 ; i < importedGermplasms.size() ; i++){
//	                        ImportedGermplasm importedGermplasm  = importedGermplasms.get(i);
//	                        if(importedGermplasm.getSource()==null){
//	                        	source = importedGermplasmList.getFilename()+":"+(i+1);
//	                        }else{
//	                        	source=importedGermplasm.getSource();
//	                        }
//	                        getGermplasmDetailsComponent().getGermplasmDetailsTable().addItem(new Object[]{importedGermplasm.getEntryId(), importedGermplasm.getEntryCode(),importedGermplasm.getGid(), importedGermplasm.getDesig(), importedGermplasm.getCross(), source}, new Integer(i+1));
//	                    }
//	                    getGermplasmDetailsComponent().setImportedGermplasms(importedGermplasms);
//	                    getGermplasmDetailsComponent().setGermplasmListUploader(germplasmListUploader);
//
//	                    if(germplasmListUploader.importFileIsAdvanced()){
//	                    	getGermplasmDetailsComponent().setPedigreeOptionGroupValue(3);
//	                    	getGermplasmDetailsComponent().setPedigreeOptionGroupEnabled(false);
//	                    } else {
//	                    	getGermplasmDetailsComponent().setPedigreeOptionGroupEnabled(true);
//	                    }
//	            }
//    	}
    }
    
    public GermplasmImportMain getSource() {
        return source;
    }

	@SuppressWarnings("serial")
	@Override
	public void instantiateComponents() {
		selectFileLabel = new Label(messageSource.getMessage(Message.SELECT_GERMPLASM_LIST_FILE));
      
        uploadComponents = new UploadField(){
        	 @Override
             public void uploadFinished(Upload.FinishedEvent event) {
                 super.uploadFinished(event);
                 nextButton.setEnabled(true);
             }
        };
        
        uploadComponents.setButtonCaption(messageSource.getMessage(Message.UPLOAD));
        uploadComponents.setNoFileSelectedText(messageSource.getMessage("NO_FILE_SELECTED"));
        uploadComponents.setSelectedFileText(messageSource.getMessage("SELECTED_IMPORT_FILE"));
        uploadComponents.setDeleteCaption(messageSource.getMessage("CLEAR"));
        uploadComponents.setFieldType(UploadField.FieldType.FILE);
        uploadComponents.getRootLayout().setWidth("100%");
        uploadComponents.setButtonCaption("Browse");
        
        germplasmListUploader = new GermplasmListUploader();
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.setEnabled(false);
        nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		
	}

	@Override
	public void initializeValues() {
		
	}

	@SuppressWarnings("serial")
	@Override
	public void addListeners() {
		uploadComponents.setDeleteButtonListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
               nextButton.setEnabled(false);
            }
        });
		uploadComponents.setFileFactory(germplasmListUploader);
		
		nextButton.addListener(new GermplasmImportButtonClickListener(this));
	}

	@Override
	public void layoutComponents() {
		 addComponent(selectFileLabel, "top:40px;left:30px");
		 
		 addComponent(uploadComponents, "top:60px;left:30px");
		 
		 addComponent(nextButton, "top:250px;left:700px");
	}
}
