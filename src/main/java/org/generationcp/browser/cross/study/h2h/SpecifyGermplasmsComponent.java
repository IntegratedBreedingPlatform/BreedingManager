package org.generationcp.browser.cross.study.h2h;

import org.generationcp.browser.cross.study.h2h.listeners.H2HComparisonQueryButtonClickListener;
import org.generationcp.browser.germplasm.dialogs.SelectAGermplasmDialog;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

@Configurable
public class SpecifyGermplasmsComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -7925696669478799303L;
    
    public static final String SELECT_TEST_ENTRY_BUTTON_ID = "SpecifyGermplasmsComponent Select Test Entry Button ID";
    public static final String SELECT_STANDARD_ENTRY_BUTTON_ID = "SpecifyGermplasmsComponent Select Standard Entry Button ID";
    
    private Label specifyTestEntryLabel;
    private Label specifyStandardEntryLabel;
    
    private TextField testEntryText;
    private TextField standardEntryText;
    
    private Button selectTestEntryButton;
    private Button selectStandardEntryButton;
    private Button nextButton;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("200px");
        setWidth("1000px");
        
        specifyTestEntryLabel = new Label("Specify a test entry:");
        addComponent(specifyTestEntryLabel, "top:20px;left:30px");
        
        testEntryText = new TextField();
        testEntryText.setWidth("200px");
        addComponent(testEntryText, "top:20px;left:150px");
        
        specifyStandardEntryLabel = new Label("Specify a standard entry:");
        addComponent(specifyStandardEntryLabel, "top:20px;left:450px");
        
        standardEntryText = new TextField();
        standardEntryText.setWidth("200px");
        addComponent(standardEntryText, "top:20px;left:600px");
        
        selectTestEntryButton = new Button("Select test entry");
        selectTestEntryButton.setData(SELECT_TEST_ENTRY_BUTTON_ID);
        selectTestEntryButton.addListener(new H2HComparisonQueryButtonClickListener(this));
        addComponent(selectTestEntryButton, "top:70px;left:170px");
        
        selectStandardEntryButton = new Button("Select standard entry");
        selectStandardEntryButton.setData(SELECT_STANDARD_ENTRY_BUTTON_ID);
        selectStandardEntryButton.addListener(new H2HComparisonQueryButtonClickListener(this));
        addComponent(selectStandardEntryButton, "top:70px;left:610px");
        
        nextButton = new Button("Next");
        addComponent(nextButton, "top:150px;left:900px");
    }

    public void selectTestEntryButtonClickAction(){
        Window parentWindow = this.getWindow();
        SelectAGermplasmDialog selectAGermplasmDialog = new SelectAGermplasmDialog(this, parentWindow, testEntryText);
        parentWindow.addWindow(selectAGermplasmDialog);
    }
    
    public void selectStandardEntryButtonClickAction(){
        Window parentWindow = this.getWindow();
        SelectAGermplasmDialog selectAGermplasmDialog = new SelectAGermplasmDialog(this, parentWindow, standardEntryText);
        parentWindow.addWindow(selectAGermplasmDialog);
    }
    
    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
        
    }
    
}
