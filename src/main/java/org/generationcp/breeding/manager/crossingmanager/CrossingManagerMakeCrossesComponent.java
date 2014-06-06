package org.generationcp.breeding.manager.crossingmanager;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossNameSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.breeding.manager.customcomponent.BreedingManagerWizardDisplay.StepChangeListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@Configurable
public class CrossingManagerMakeCrossesComponent extends VerticalLayout 
        implements InitializingBean, InternationalizableComponent, BreedingManagerLayout, StepChangeListener{
    
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(CrossingManagerMakeCrossesComponent.class);
	
    public static final String NEXT_BUTTON_ID = "next button";
    public static final String BACK_BUTTON_ID = "back button";

    private static final long serialVersionUID = 9097810121003895303L;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private ManageCrossingSettingsMain source;

    private Button backButton;
    private Button nextButton;
    
    private SelectParentsComponent selectParentsComponent;
    private MakeCrossesParentsComponent parentsComponent;
    private CrossingMethodComponent crossingMethodComponent;
    private MakeCrossesTableComponent crossesTableComponent;
    private Integer lastOpenedListId;
    
    public CrossingManagerMakeCrossesComponent(CrossingManagerMain source, Accordion accordion){
        lastOpenedListId = null;
    }
    
    public CrossingManagerMakeCrossesComponent(ManageCrossingSettingsMain manageCrossingSettingsMain){
    	this.source = manageCrossingSettingsMain;
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
        messageSource.setCaption(backButton, Message.BACK);
        messageSource.setCaption(nextButton, Message.NEXT);
    }
    
    /*
     * Action handler for Make Cross button
     */
    public void makeCrossButtonAction(List<GermplasmListEntry> femaleList, List<GermplasmListEntry> maleList, 
    		String listnameFemaleParent, String listnameMaleParent, CrossType type, boolean makeReciprocalCrosses){
        
        if (!femaleList.isEmpty() && !maleList.isEmpty()){
            // Female - Male Multiplication
            if (CrossType.MULTIPLY.equals(type)){
                crossesTableComponent.multiplyParents(femaleList, maleList,listnameFemaleParent, listnameMaleParent);
                if (makeReciprocalCrosses){
                    crossesTableComponent.multiplyParents(maleList, femaleList,listnameMaleParent,listnameFemaleParent);
                }               
                
            // Top to Bottom Crossing    
            } else if (CrossType.TOP_TO_BOTTOM.equals(type)){
                if (femaleList.size() == maleList.size()){
                    crossesTableComponent.makeTopToBottomCrosses(femaleList, maleList,listnameFemaleParent, listnameMaleParent);
                    if (makeReciprocalCrosses){
                        crossesTableComponent.makeTopToBottomCrosses(maleList, femaleList,listnameMaleParent, listnameFemaleParent);
                    }
                } else {
                    MessageNotifier.showError(getWindow(), "Error with selecting parents."
                            ,messageSource.getMessage(Message.ERROR_MALE_AND_FEMALE_PARENTS_MUST_BE_EQUAL)
                            , Notification.POSITION_CENTERED);
                }
            }
        } else {
            MessageNotifier.showError(getWindow(), "Error with selecting parents."
                    ,messageSource.getMessage(Message.AT_LEAST_ONE_FEMALE_AND_ONE_MALE_PARENT_MUST_BE_SELECTED)
                    , Notification.POSITION_CENTERED);
        }
    }
    
    public void toggleNextButton(){
		nextButton.setEnabled(isAllListsSaved());
    }
    
    private boolean isAllListsSaved(){
    	return parentsComponent.getFemaleList() != null && 
    		parentsComponent.getMaleList() != null && 
    			crossesTableComponent.getCrossList() != null;
    }
    
    public void nextButtonClickAction(){
    	if (crossesTableComponent.getCrossList() != null){
    		source.viewGermplasmListCreated(crossesTableComponent.getCrossList(), 
    				parentsComponent.getFemaleList(), parentsComponent.getMaleList());
    	}
    }
    
    public void backButtonClickAction(){
    	if (crossesTableComponent.getCrossList() != null){
    		MessageNotifier.showWarning(getWindow(), "Invalid Action", "Cannot change settings once crosses have been saved");
    		return;
    	}
    	
    	if (this.source != null){
    		this.source.backStep();
    	}
    }

    @Deprecated
    public Integer getLastOpenedListId() {
        return this.lastOpenedListId;
    }
    
    @Deprecated
    public void setLastOpenedListId(Integer lastOpenedListId) {
        this.lastOpenedListId = lastOpenedListId;
    }
    
    public void disableNextButton(){
        nextButton.setEnabled(false);
    }

    @Deprecated
    public void clearParentsListsAndCrossesTable(){
        this.crossesTableComponent.clearCrossesTable();
    }

	@Override
	public void instantiateComponents() {        
        selectParentsComponent = new SelectParentsComponent(this);
        parentsComponent = new MakeCrossesParentsComponent(this);
        crossingMethodComponent = new CrossingMethodComponent(this);
        crossesTableComponent = new MakeCrossesTableComponent(this);
        
        backButton = new Button();
        backButton.setData(BACK_BUTTON_ID);
        backButton.setWidth("80px");
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.setWidth("80px");
        nextButton.setEnabled(false);
        nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
		
	}

	@Override
	public void addListeners() {
		CrossingManagerImportButtonClickListener listener = new CrossingManagerImportButtonClickListener(this);
        backButton.addListener(listener);
        nextButton.addListener(listener);
	}

	@Override
	public void layoutComponents() {
		setWidth("950px");
        setMargin(true);
        setSpacing(true);
		
		HorizontalLayout upperLayout = new HorizontalLayout();
		upperLayout.setSpacing(true);
		upperLayout.setHeight("535px");
		upperLayout.addComponent(selectParentsComponent);
		upperLayout.addComponent(parentsComponent);
		
		HorizontalLayout lowerLayout = new HorizontalLayout();
		lowerLayout.setSpacing(true);
		lowerLayout.addComponent(crossingMethodComponent);
		lowerLayout.addComponent(crossesTableComponent);
        
        HorizontalLayout layoutButtonArea = new HorizontalLayout();
        layoutButtonArea.setMargin(true,true,false,true);
        layoutButtonArea.setSpacing(true);
        layoutButtonArea.addComponent(backButton);
        layoutButtonArea.addComponent(nextButton);
        
        addComponent(upperLayout);
        addComponent(lowerLayout);
        addComponent(layoutButtonArea);
        
        setComponentAlignment(layoutButtonArea, Alignment.MIDDLE_CENTER);
	}
		
	public void updateCrossesSeedSource(String femaleListName, String maleListName){
		crossesTableComponent.updateSeedSource(femaleListName, maleListName);
	}
	
	private boolean doUpdateTable(){
		return !getSeparatorString().equals(crossesTableComponent.getSeparator());
	}

	@Override
	public void updatePage() {
		// only make updates to the page if separator was changed
		if (doUpdateTable() && crossesTableComponent.getCrossList() == null){
			crossesTableComponent.updateSeparatorForCrossesMade();
		}
	}
	
	//SETTERS AND GETTERS
    public String getSeparatorString(){
    	CrossNameSetting crossNameSetting = getCurrentCrossingSetting().getCrossNameSetting();
    	return crossNameSetting.getSeparator();
    }

	public CrossingManagerSetting getCurrentCrossingSetting(){
		return source.getDetailComponent().getCurrentlyDefinedSetting();
	}
	
	public CrossesMadeContainer getCrossesMadeContainer(){
		return source;
	}
	
	public SelectParentsComponent getSelectParentsComponent() {
		return selectParentsComponent;
	}
	
	public MakeCrossesParentsComponent getParentsComponent() {
		return parentsComponent;
	}

	public CrossingMethodComponent getCrossingMethodComponent() {
		return crossingMethodComponent;
	}

	public MakeCrossesTableComponent getCrossesTableComponent() {
		return crossesTableComponent;
	}
	
}
