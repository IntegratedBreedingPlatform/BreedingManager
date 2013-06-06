package org.generationcp.browser.cross.study.h2h;

import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

@Configurable
public class SpecifyGermplasmsComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -7925696669478799303L;
    
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
        addComponent(selectTestEntryButton, "top:70px;left:170px");
        
        selectStandardEntryButton = new Button("Select standard entry");
        addComponent(selectStandardEntryButton, "top:70px;left:610px");
        
        nextButton = new Button("Next");
        addComponent(nextButton, "top:150px;left:900px");
    }

    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
        
    }
}
