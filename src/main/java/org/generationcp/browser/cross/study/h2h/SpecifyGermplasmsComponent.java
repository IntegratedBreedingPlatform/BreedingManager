package org.generationcp.browser.cross.study.h2h;

import org.generationcp.browser.cross.study.h2h.listeners.H2HComparisonQueryButtonClickListener;
import org.generationcp.browser.germplasm.dialogs.SelectAGermplasmDialog;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

@Configurable
public class SpecifyGermplasmsComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -7925696669478799303L;
    
    public static final String NEXT_BUTTON_ID = "SpecifyGermplasmsComponent Next Button ID";
    public static final String SELECT_TEST_ENTRY_BUTTON_ID = "SpecifyGermplasmsComponent Select Test Entry Button ID";
    public static final String SELECT_STANDARD_ENTRY_BUTTON_ID = "SpecifyGermplasmsComponent Select Standard Entry Button ID";
    
    private Label specifyTestEntryLabel;
    private Label specifyStandardEntryLabel;
    private Label testEntryLabel;
    private Label standardEntryLabel;
    
    private Button selectTestEntryButton;
    private Button selectStandardEntryButton;
    private Button nextButton;
    
    private HeadToHeadComparisonMain mainScreen;
    private TraitsAvailableComponent nextScreen;
    private ResultsComponent resultsScreen;
    
    private Integer lastTestEntryGID;
    private Integer lastStandardEntryGID;
    
    public SpecifyGermplasmsComponent(HeadToHeadComparisonMain mainScreen, TraitsAvailableComponent nextScreen
            , ResultsComponent resultScreen){
        this.mainScreen = mainScreen;
        this.nextScreen = nextScreen;
        this.resultsScreen = resultScreen;
        this.lastTestEntryGID = null;
        this.lastStandardEntryGID = null;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("200px");
        setWidth("1000px");
        
        specifyTestEntryLabel = new Label("Specify a test entry:");
        addComponent(specifyTestEntryLabel, "top:20px;left:30px");
        
        testEntryLabel = new Label();
        testEntryLabel.setWidth("200px");
        testEntryLabel.setImmediate(true);
        addComponent(testEntryLabel, "top:20px;left:150px");
        
        specifyStandardEntryLabel = new Label("Specify a standard entry:");
        addComponent(specifyStandardEntryLabel, "top:20px;left:450px");
        
        standardEntryLabel = new Label();
        standardEntryLabel.setWidth("200px");
        standardEntryLabel.setImmediate(true);
        addComponent(standardEntryLabel, "top:20px;left:600px");
        
        selectTestEntryButton = new Button("Select test entry");
        selectTestEntryButton.setData(SELECT_TEST_ENTRY_BUTTON_ID);
        selectTestEntryButton.addListener(new H2HComparisonQueryButtonClickListener(this));
        addComponent(selectTestEntryButton, "top:70px;left:170px");
        
        selectStandardEntryButton = new Button("Select standard entry");
        selectStandardEntryButton.setData(SELECT_STANDARD_ENTRY_BUTTON_ID);
        selectStandardEntryButton.addListener(new H2HComparisonQueryButtonClickListener(this));
        addComponent(selectStandardEntryButton, "top:70px;left:610px");
        
        nextButton = new Button("Next");
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(new H2HComparisonQueryButtonClickListener(this));
        addComponent(nextButton, "top:150px;left:900px");
    }

    public void selectTestEntryButtonClickAction(){
        Window parentWindow = this.getWindow();
        SelectAGermplasmDialog selectAGermplasmDialog = new SelectAGermplasmDialog(this, parentWindow, testEntryLabel);
        parentWindow.addWindow(selectAGermplasmDialog);
    }
    
    public void selectStandardEntryButtonClickAction(){
        Window parentWindow = this.getWindow();
        SelectAGermplasmDialog selectAGermplasmDialog = new SelectAGermplasmDialog(this, parentWindow, standardEntryLabel);
        parentWindow.addWindow(selectAGermplasmDialog);
    }
    
    public void nextButtonClickAction(){
        if(this.testEntryLabel.getData() == null){
            MessageNotifier.showWarning(getWindow(), "Warning!", "Need to specify a test entry. Please use the Select test entry button.", Notification.POSITION_CENTERED);
            return;
        }
        
        if(this.standardEntryLabel.getData() == null){
            MessageNotifier.showWarning(getWindow(), "Warning!", "Need to specify a standard entry. Please use the Select standard entry button.", Notification.POSITION_CENTERED);
            return;
        }
        
        Integer testEntryGID = (Integer) testEntryLabel.getData();
        Integer standardEntryGID = (Integer) standardEntryLabel.getData();
        
        if(this.nextScreen != null){
            if(areCurrentGIDsDifferentFromLast(testEntryGID, standardEntryGID)){
                this.resultsScreen.setEntriesLabel((String) testEntryLabel.getValue(),(String) standardEntryLabel.getValue());
                this.nextScreen.populateTraitsAvailableTable(testEntryGID, standardEntryGID);
                this.lastTestEntryGID = testEntryGID;
                this.lastStandardEntryGID = standardEntryGID;
            }
            this.mainScreen.selectSecondTab();
        }
    }
    
    private boolean areCurrentGIDsDifferentFromLast(Integer currentTestEntryGID, Integer currentStandardEntryGID){
        if(this.lastTestEntryGID != null && this.lastStandardEntryGID != null){
            if(this.lastTestEntryGID == currentTestEntryGID && this.lastStandardEntryGID == currentStandardEntryGID){
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
        
    }
    
}
