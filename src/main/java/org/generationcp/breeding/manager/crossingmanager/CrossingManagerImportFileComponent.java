package org.generationcp.breeding.manager.crossingmanager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerUploader;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmCross;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmCrosses;
import org.generationcp.breeding.manager.util.CrossingManagerUtil;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
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
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
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
    private Component previousScreen;
    private Component nextScreen;
    private Component nextNextScreen;
    
    private Label filenameLabel;
    
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
        assemble();
    }
    
    protected void assemble() {
        initializeComponents();
        initializeValues();
        initializeLayout();
        initializeActions();
    }
    
    protected void initializeComponents() {
        selectFileLabel = new Label();
        addComponent(selectFileLabel, "top:40px;left:30px");
        
        uploadComponents = new Upload();
        uploadComponents.setButtonCaption(messageSource.getMessage(Message.UPLOAD));
        uploadComponents.setWidth("600px");
        addComponent(uploadComponents, "top:60px;left:30px");
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.setEnabled(false);
        nextButton.addListener(new CrossingManagerImportButtonClickListener(this));
        addComponent(nextButton, "top:250px;left:700px");
        
        filenameLabel = new Label();
        addComponent(filenameLabel, "top:110px;left:30px;");
        
        crossesOptionGroupLabel = new Label();
        addComponent(crossesOptionGroupLabel, "top:156px;left:30px;");
        
        crossesOptionGroup = new OptionGroup();
        crossesOptionGroup.addItem(messageSource.getMessage(Message.I_HAVE_ALREADY_DEFINED_CROSSES_IN_THE_NURSERY_TEMPLATE_FILE));
        crossesOptionGroup.addItem(messageSource.getMessage(Message.I_WANT_TO_MANUALLY_MAKE_CROSSES));
        addComponent(crossesOptionGroup, "top:175px;left:30px;");
        
    }
    
    protected void initializeValues() {
            
    }
    
    protected void initializeLayout() {
        setHeight("300px");
        setWidth("800px");
    }
    
    protected void initializeActions() {
        crossingManagerUploader = new CrossingManagerUploader(this, germplasmListManager);
        uploadComponents.setReceiver(crossingManagerUploader);
        uploadComponents.addListener(crossingManagerUploader);
        
        uploadComponents.addListener(new FinishedListener() {
            private static final long serialVersionUID = -4478757144863256507L;

            @Override
            public void uploadFinished(FinishedEvent event) {
             if(!crossingManagerUploader.hasInvalidData()){
                ImportedGermplasmCrosses importedGermplasmCrosses = crossingManagerUploader.getImportedGermplasmCrosses();
                
                // display uploaded filename
                if (importedGermplasmCrosses != null) {
                    updateFilenameLabelValue(importedGermplasmCrosses.getFilename());
                } else {
                    updateFilenameLabelValue("");
                }
                
                // select default selected option based on file
                if(importedGermplasmCrosses==null || importedGermplasmCrosses.getImportedGermplasmCrosses().size()==0){
                    selectManuallyMakeCrosses();
                } else {
                    selectAlreadyDefinedCrossesInNurseryTemplateFile();
                }
             }else{
        	getWindow().showNotification(messageSource.getMessage(Message.INVALID_NURSERY_TEMPLATE_FILE), Notification.TYPE_ERROR_MESSAGE);
     	    	updateFilenameLabelValue("");
     	    	nextButton.setEnabled(false);
             }
            }
        });
        
        nextButton.addListener(new CrossingManagerImportButtonClickListener(this));
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
        //source.enableOnlyWizardTabOne();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(selectFileLabel, Message.SELECT_NURSERY_TEMPLATE_FILE);
        messageSource.setCaption(nextButton, Message.NEXT);
        messageSource.setCaption(crossesOptionGroupLabel,Message.SELECT_AN_OPTION_FOR_SPECIFYING_CROSSES);
        messageSource.setCaption(filenameLabel, Message.UPLOADED_FILE);
        filenameLabel.setCaption(filenameLabel.getCaption()+": ");
    }
    
    public void updateFilenameLabelValue(String filename){
    	messageSource.setCaption(filenameLabel, Message.UPLOADED_FILE);
    	filenameLabel.setCaption(filenameLabel.getCaption()+": "+filename);
    }

    public void nextButtonClickAction() throws InternationalizableException{
        source.enableWizardTabs();
        if(crossingManagerUploader.getImportedGermplasmCrosses()==null){
            getWindow().showNotification("You must upload a nursery template file before clicking on next.", Notification.TYPE_ERROR_MESSAGE);
        } else if(crossesOptionGroup.getValue()==null) {
            getWindow().showNotification("You should select an option for specifying crosses.", Notification.TYPE_ERROR_MESSAGE);
        } else {
            if(crossesOptionGroup.getValue().equals(messageSource.getMessage(Message.I_HAVE_ALREADY_DEFINED_CROSSES_IN_THE_NURSERY_TEMPLATE_FILE))){
                if(crossingManagerUploader.getImportedGermplasmCrosses().getImportedGermplasmCrosses().size()==0){
                    getWindow().showNotification("The nursery template file you uploaded doesn't contain any data on the second sheet.", Notification.TYPE_ERROR_MESSAGE);
                //pass uploaded info and Crosses (if any) to next screen
                } else {
                    CrossesMade crossesMade = new CrossesMade();
                    crossesMade.setCrossingManagerUploader(crossingManagerUploader);
                    if(this.nextNextScreen != null){
                        saveCrossesInfoToNextWizardStep(this.nextNextScreen, true);
                    } else {
                        this.nextButton.setEnabled(false);
                    }
                }
            } else {
                if(this.nextScreen != null){
                    saveCrossesInfoToNextWizardStep(this.nextScreen, false);
                    ((CrossingManagerMakeCrossesComponent)this.nextScreen).setupDefaultListFromFile();
	        	} else {
	        		this.nextButton.setEnabled(false);
	        	}
    		}
    	}
    }

    private void saveCrossesInfoToNextWizardStep(Component nextStep, boolean crossesUploaded){
        assert nextStep instanceof CrossesMadeContainer;

        CrossesMade crossesMade = new CrossesMade();
        crossesMade.setCrossingManagerUploader(crossingManagerUploader);
        if (crossesUploaded){
            crossesMade.setCrossesMap(generateCrossesMadeMap());
        }
        ((CrossesMadeContainer) nextStep).setCrossesMade(crossesMade);
        source.getWizardScreenTwo().setPreviousScreen(this);
        source.getWizardScreenThree().setPreviousScreen(this);

        source.enableWizardTabs();
        this.accordion.setSelectedTab(nextStep);
        if(nextStep instanceof CrossingManagerMakeCrossesComponent){
            source.getWizardScreenTwo().setPreviousScreen(this);
    	    this.accordion.setSelectedTab(nextStep);
            //source.enableOnlyWizardTabTwo();
            //source.enableWizardTabOne();
        } else if(nextStep instanceof CrossingManagerAdditionalDetailsComponent){
    	    System.out.println("DEBUG - Should go to additional details");
            source.getWizardScreenThree().setPreviousScreen(this);
            this.accordion.setSelectedTab(nextStep);
            //source.enableOnlyWizardTabThree();
        } else {
            System.out.println("DEBUG - I am lost");
        }
    }

    public Map<Germplasm, Name > generateCrossesMadeMap(){
        Map<Germplasm, Name> crossesMadeMap = new LinkedHashMap<Germplasm, Name>();
        List<ImportedGermplasmCross> importedGermplasmCrosses = 
                crossingManagerUploader.getImportedGermplasmCrosses().getImportedGermplasmCrosses();

        //get ID of User Defined Field for Crossing Name
        Integer crossingNameTypeId = CrossingManagerUtil.getIDForUserDefinedFieldCrossingName(
                germplasmListManager, getWindow(), messageSource);

        int ctr = 1;
        for (ImportedGermplasmCross cross : importedGermplasmCrosses){

            Germplasm germplasm = new Germplasm();
            germplasm.setGid(ctr++);
            germplasm.setGpid1(cross.getFemaleGId());
            germplasm.setGpid2(cross.getMaleGId());

            Name name = new Name();
            name.setNval(CrossingManagerUtil.generateFemaleandMaleCrossName(
                    cross.getFemaleDesignation(), cross.getMaleDesignation()));
            name.setTypeId(crossingNameTypeId);

            crossesMadeMap.put(germplasm, name);
        }

        return crossesMadeMap;
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

    public CrossingManagerUploader getCrossingManagerUploader() {
        return crossingManagerUploader;
    }
    
    public void disableNextButton(){
        nextButton.setEnabled(false);
    }
    
    public void enableNextButton(){
        nextButton.setEnabled(true);
    }
}
