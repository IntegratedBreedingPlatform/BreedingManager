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
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
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
    private Button cancelButton;
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
    }
    
    public GermplasmImportMain getSource() {
        return source;
    }

    public void initializeUploadField(){
    	uploadComponents = new UploadField(){
			private static final long serialVersionUID = 1L;
			@Override
            public void uploadFinished(Upload.FinishedEvent event) {
                super.uploadFinished(event);
                nextButton.setEnabled(true);
            }
       };
       uploadComponents.discard();
       
       uploadComponents.setButtonCaption(messageSource.getMessage(Message.UPLOAD));
       uploadComponents.setNoFileSelectedText(messageSource.getMessage("NO_FILE_SELECTED"));
       uploadComponents.setSelectedFileText(messageSource.getMessage("SELECTED_IMPORT_FILE"));
       uploadComponents.setDeleteCaption(messageSource.getMessage("CLEAR"));
       uploadComponents.setFieldType(UploadField.FieldType.FILE);
       uploadComponents.setButtonCaption("Browse");
       
       uploadComponents.getRootLayout().setWidth("100%");
       uploadComponents.getRootLayout().setStyleName("bms-upload-container");
       addListenersForUploadField();
    }
    
    public UploadField getUploadComponent(){
    	return uploadComponents;
    }
    
	@Override
	public void instantiateComponents() {
		selectFileLabel = new Label(messageSource.getMessage(Message.SELECT_GERMPLASM_LIST_FILE));
      
		initializeUploadField();
        
        germplasmListUploader = new GermplasmListUploader();
        
        cancelButton = new Button(messageSource.getMessage(Message.CANCEL));
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.setEnabled(false);
        nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		
	}

	@Override
	public void initializeValues() {
		
	}

	public void addListenersForUploadField(){
		uploadComponents.setDeleteButtonListener(new Button.ClickListener() {
			private static final long serialVersionUID = -1357425494204377238L;

			@Override
            public void buttonClick(ClickEvent event) {
               nextButton.setEnabled(false);
            }
        });
		uploadComponents.setFileFactory(germplasmListUploader);
	}
	
	@SuppressWarnings("serial")
	@Override
	public void addListeners() {
		
		addListenersForUploadField();
		
		cancelButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (source.getGermplasmImportPopupSource() == null){
					source.reset();
				} else {
					source.getGermplasmImportPopupSource().getParentWindow().removeWindow(((Window) source.getComponentContainer()));
				}
			}
		});
		
		nextButton.addListener(new GermplasmImportButtonClickListener(this));
	}

	@Override
	public void layoutComponents() {
		addComponent(selectFileLabel, "top:20px");

		addComponent(uploadComponents, "top:50px");

		HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidth("100%");
        buttonLayout.setHeight("40px");
        buttonLayout.setSpacing(true);
        
        buttonLayout.addComponent(cancelButton);
        buttonLayout.addComponent(nextButton);
        buttonLayout.setComponentAlignment(cancelButton, Alignment.BOTTOM_RIGHT);
        buttonLayout.setComponentAlignment(nextButton, Alignment.BOTTOM_LEFT);
        
        addComponent(buttonLayout, "top:230px");
	}
	
	public GermplasmListUploader getGermplasmListUploader(){
		return this.germplasmListUploader;
	}
}
