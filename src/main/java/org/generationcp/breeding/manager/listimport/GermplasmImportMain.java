package org.generationcp.breeding.manager.listimport;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmImportMain extends VerticalLayout implements InitializingBean, InternationalizableComponent{
    
    private static final long serialVersionUID = -6656072296236475385L;
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    @SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory.getLogger(GermplasmImportMain.class);
    
    private final static String VERSION = "1.0.0";
    private static final String STEP_1_GUIDE_MESSAGE = "The Germplasm List Import tool facilitates the creation of a list and its list entries based on an import file.  " +
    		"The first step requires that a Germplasm List Import file be provided. " +
    		" Choose the file and press the Upload button.  Click on the Next button to proceed to the next step.";
    private static final String STEP_2_GUIDE_MESSAGE = "The second step involves specifying details for the germplasm records of list entries to be saved. " +
    		" A table of entries read from the import file is shown.  A Pedigree Option should also be chosen.";
    private static final String STEP_3_GUIDE_MESSAGE = "In this step you should specify the list details.  Defaults are provided as they are read from the import file. " +
    		" Clicking done will finish the process and save the database records as needed.  The list details and entries will then be displayed.";
    
    private GermplasmImportFileComponent wizardScreenOne;
    private SpecifyGermplasmDetailsComponent wizardScreenTwo;
    private SaveGermplasmListComponent wizardScreenThree;
    
    private Accordion accordion;
    private ComponentContainer parent;
    
    private Tab wizardTabOne;
    private Tab wizardTabTwo;
    private Tab wizardTabThree;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
	private HorizontalLayout titleLayout;
	private Label crossingManagerTitle;
    
	private Boolean viaToolURL;
	
    public GermplasmImportMain(ComponentContainer parent, boolean viaToolURL){
        this.parent = parent;
        this.viaToolURL = viaToolURL;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setMargin(false);
        setSpacing(true);
        
        titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        setTitleContent(STEP_1_GUIDE_MESSAGE);
        addComponent(titleLayout);
        
        accordion = new Accordion();
        accordion.setWidth("800px");
        
        wizardScreenOne = new GermplasmImportFileComponent(this, accordion);
        wizardScreenTwo = new SpecifyGermplasmDetailsComponent(this, accordion, viaToolURL);
        wizardScreenThree = new SaveGermplasmListComponent(this, accordion);
        
        wizardScreenOne.setNextScreen(wizardScreenTwo);
        wizardScreenTwo.setNextScreen(wizardScreenThree);
        wizardScreenTwo.setPreviousScreen(wizardScreenOne);
        wizardScreenThree.setPreviousScreen(wizardScreenTwo);
        
        wizardTabOne = accordion.addTab(wizardScreenOne, messageSource.getMessage(Message.OPEN_GERMPLASM_IMPORT_FILE)); //Open Germplasm Import File
        wizardTabTwo = accordion.addTab(wizardScreenTwo, messageSource.getMessage(Message.SPECIFY_GERMPLASM_DETAILS)); //Specify Germplasm Details
        wizardTabThree = accordion.addTab(wizardScreenThree, messageSource.getMessage(Message.SAVE_GERMPLASM_LIST)); //Save Germplasm List
        
        accordion.addListener(new SelectedTabChangeListener() {
            private static final long serialVersionUID = 738354322321641203L;

			@Override
            public void selectedTabChange(SelectedTabChangeEvent event) {
                Component selected =accordion.getSelectedTab();
                Tab tab = accordion.getTab(selected);
                if(tab!=null && tab.equals(wizardTabOne)){
                	setTitleContent(STEP_1_GUIDE_MESSAGE);
                } else if(tab!=null && tab.equals(wizardTabTwo)){
                	setTitleContent(STEP_2_GUIDE_MESSAGE);
                } else{   
                	setTitleContent(STEP_3_GUIDE_MESSAGE);
                } 
                            
            }
        });
        
        addComponent(accordion);
        
        enableTab(1);
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        
    }
    
    public GermplasmImportFileComponent getWizardScreenOne() {
        return wizardScreenOne;
    }
    public SpecifyGermplasmDetailsComponent getWizardScreenTwo() {
        return wizardScreenTwo;
    }
    public SaveGermplasmListComponent getWizardScreenThree() {
        return wizardScreenThree;
    }

    public void viewGermplasmListCreated(Integer listId){
        EmbeddedGermplasmListDetailComponent germplasmListBrowser =
            new EmbeddedGermplasmListDetailComponent(this, listId);

        this.removeComponent(this.accordion);
        this.addComponent(germplasmListBrowser);
    }
    
    public void enableAllTabs(){
    	if(this.wizardTabOne!=null)
    		this.wizardTabOne.setEnabled(true);
    	if(this.wizardTabTwo!=null)
    		this.wizardTabTwo.setEnabled(true);
    	if(this.wizardTabThree!=null)
    		this.wizardTabThree.setEnabled(true);
    }

   
    public void enableTab(int index){
    	this.enableAllTabs();
    	if(index!=1)
    		this.wizardTabOne .setEnabled(false);
    	if(index!=2)
    		this.wizardTabTwo.setEnabled(false);
    	if(index!=3)
    		this.wizardTabThree.setEnabled(false);
    }
    
    public void alsoEnableTab(int index){
    	if(index==1)
    		this.wizardTabOne .setEnabled(true);
    	if(index==2)
    		this.wizardTabTwo.setEnabled(true);
    	if(index==3)
    		this.wizardTabThree.setEnabled(true);
    }

    public void reset(){
        this.parent.replaceComponent(this, new GermplasmImportMain(this.parent, viaToolURL));
    }
    
    public void setTitleContent(String guideMessage){
        titleLayout.removeAllComponents();        
        String title =  "Germplasm Import  <h2>" + VERSION + "</h2>";
        crossingManagerTitle = new Label();
        crossingManagerTitle.setStyleName(Bootstrap.Typography.H1.styleName());
        crossingManagerTitle.setContentMode(Label.CONTENT_XHTML);
        crossingManagerTitle.setValue(title);
        crossingManagerTitle.setWidth("270px");
        titleLayout.addComponent(crossingManagerTitle);
        
        Label descLbl = new Label(guideMessage);
        descLbl.setWidth("300px");
        
        PopupView popup = new PopupView("?",descLbl);
        popup.setStyleName("gcp-popup-view");
        titleLayout.addComponent(popup);
        
        titleLayout.setComponentAlignment(popup, Alignment.MIDDLE_LEFT);
    }
}
