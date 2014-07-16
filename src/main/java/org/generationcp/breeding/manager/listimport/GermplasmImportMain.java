package org.generationcp.breeding.manager.listimport;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.AppConstants.CssStyles;
import org.generationcp.breeding.manager.customcomponent.BreedingManagerWizardDisplay.StepChangeListener;
import org.generationcp.breeding.manager.listimport.util.GermplasmListUploader;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmImportMain extends VerticalLayout implements InitializingBean, 
		InternationalizableComponent, BreedingManagerLayout{
    
    private static final long serialVersionUID = -6656072296236475385L;
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final int NUMBER_OF_STEPS = 2;

    @SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory.getLogger(GermplasmImportMain.class);
    
    private static final String GUIDE_MESSAGE = "The Germplasm Import Import tool allows you to create a new list of germplasm from an import file. "
    		+ "Sample import file templates are available in the Examples folder in the documentation provided with the BMS.";
    
    private final String[] wizardStepNames = new String[NUMBER_OF_STEPS];
    
    private GermplasmImportFileComponent importFileComponent;
    private SpecifyGermplasmDetailsComponent germplasmDetailsComponent;
    private GermplasmListImportWizardDisplay wizardDisplay;
    
    private ComponentContainer parent;
    
    private TabSheet tabSheet;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
	private HorizontalLayout titleLayout;
	private Label toolTitle;
    
	private Boolean viaToolURL;
	private boolean viaPopup; 
	
    public GermplasmImportMain(ComponentContainer parent, boolean viaToolURL){
        this.parent = parent;
        this.viaToolURL = viaToolURL;
    }

    public GermplasmImportMain(ComponentContainer parent, boolean viaToolURL, boolean viaPopup){
        this.parent = parent;
        this.viaToolURL = viaToolURL;
        this.viaPopup = viaPopup;
    }
    @Override
    public void afterPropertiesSet() throws Exception {
    	instantiateComponents();
    	initializeValues();
    	addListeners();
    	layoutComponents();
    }
    
    @Override
    public void updateLabels() {
    }
    
    public GermplasmImportFileComponent getWizardScreenOne() {
        return importFileComponent;
    }
    public SpecifyGermplasmDetailsComponent getWizardScreenTwo() {
        return germplasmDetailsComponent;
    }
    
    public void setTitleContent(String guideMessage){
        titleLayout.removeAllComponents();        
        toolTitle = new Label(messageSource.getMessage(Message.IMPORT_GERMPLASM_LIST_TAB_LABEL));
        toolTitle.setStyleName(Bootstrap.Typography.H1.styleName());
        toolTitle.setContentMode(Label.CONTENT_XHTML);
        toolTitle.setWidth("300px");
        titleLayout.addComponent(toolTitle);
        
        Label descLbl = new Label(guideMessage);
        descLbl.setWidth("400px");
        
        PopupView popup = new PopupView("?",descLbl);
        popup.setStyleName(CssStyles.POPUP_VIEW);
        titleLayout.addComponent(popup);
        
        titleLayout.setComponentAlignment(popup, Alignment.MIDDLE_LEFT);
    }

	@Override
	public void instantiateComponents() {
		titleLayout = new HorizontalLayout();
		titleLayout.setSpacing(true);
		setTitleContent(GUIDE_MESSAGE);
		
		instantiateWizardDisplay();
		
		// use tab approach to display which step to display
		initializeWizardSteps();
	}

	protected void initializeWizardSteps() {
		tabSheet = new TabSheet();
		tabSheet.hideTabs(true); //tab names are not actually shown
		
		tabSheet.setHeight("1250px");
		tabSheet.setWidth("100%");
		
		tabSheet.addStyleName(AppConstants.CssStyles.TABSHEET_WHITE);
		
		this.importFileComponent =  new GermplasmImportFileComponent(this);
		this.germplasmDetailsComponent  = new SpecifyGermplasmDetailsComponent(this, viaToolURL);
		
		tabSheet.addTab(importFileComponent, wizardStepNames[0]);
		tabSheet.addTab(germplasmDetailsComponent, wizardStepNames[1]);
	}

	@Override
	public void initializeValues() {
		
	}

	@Override
	public void addListeners() {
		
	}

	@Override
	public void layoutComponents() {
		addComponent(titleLayout);
		
		addComponent(wizardDisplay);
		addComponent(tabSheet);
	}
	
	private void instantiateWizardDisplay() {
		wizardStepNames[0] = messageSource.getMessage(Message.CHOOSE_IMPORT_FILE);
		wizardStepNames[1] = messageSource.getMessage(Message.SPECIFY_GERMPLASM_DETAILS);
		wizardDisplay = new GermplasmListImportWizardDisplay(wizardStepNames);
	}

	private void showWizardStep(int step) {
		Tab tab = Util.getTabAlreadyExist(tabSheet, wizardStepNames[step]);
		if (tab != null){
			Component tabComponent = tab.getComponent();
			tabSheet.setSelectedTab(tabComponent);
			if (tabComponent instanceof StepChangeListener){
				StepChangeListener listener = (StepChangeListener) tabComponent;
				listener.updatePage();
			}
			getWindow().setScrollTop(0);
		}
	}
	
	public void nextStep(){
		int step = wizardDisplay.nextStep();
		// if from upload to specify Germplasm Details step
		if (step == 1){
			initializeSpecifyGermplasmDetailsPage();
		}
		showWizardStep(step);
	}
	
	public void backStep(){
		int step = wizardDisplay.backStep();
		showWizardStep(step);
	}
	
	public void reset(){
        this.parent.replaceComponent(this, new GermplasmImportMain(this.parent, viaToolURL, viaPopup));
    }
	
	private void initializeSpecifyGermplasmDetailsPage(){
		GermplasmListUploader germplasmListUploader = importFileComponent.getGermplasmListUploader();
        if(germplasmDetailsComponent != null && germplasmListUploader != null
                && germplasmListUploader.getImportedGermplasmList() != null){
        	
            ImportedGermplasmList importedGermplasmList = germplasmListUploader.getImportedGermplasmList();
            List<ImportedGermplasm> importedGermplasms = importedGermplasmList.getImportedGermplasms();
            
            germplasmDetailsComponent.setImportedGermplasms(importedGermplasms);
            germplasmDetailsComponent.setGermplasmListUploader(germplasmListUploader);
            
            germplasmDetailsComponent.initializeFromImportFile(importedGermplasmList);

        }
	}
}
