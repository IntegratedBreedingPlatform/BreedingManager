package org.generationcp.browser.cross.study.h2h;

import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class HeadToHeadComparisonMain extends VerticalLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -3488805933508882321L;
    private static final String VERSION = "1.0.0";
    
    private Accordion accordion;
    
    private HorizontalLayout titleLayout;
    
    private Label mainTitle;
    
    private SpecifyGermplasmsComponent screenOne;
    private TraitsAvailableComponent screenTwo;
    private EnvironmentsAvailableComponent screenThree;
    private ResultsComponent screenFour;

    private Tab firstTab;
    private Tab secondTab;
    private Tab thirdTab;
    private Tab fourthTab;
    
    @Override
    public void afterPropertiesSet() throws Exception {
    setMargin(false);
    setSpacing(true);
        
    titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        setTitleContent("");
        addComponent(titleLayout);
                
        accordion = new Accordion();
        accordion.setWidth("1000px");

        screenFour = new ResultsComponent();
        screenThree = new EnvironmentsAvailableComponent(this, screenFour);
        screenTwo = new TraitsAvailableComponent(this, screenThree);
        screenOne = new SpecifyGermplasmsComponent(this, screenTwo, screenFour);
        
        firstTab = accordion.addTab(screenOne, "Specify the Test and Standard Entries to Compare");
        secondTab = accordion.addTab(screenTwo, "Review Traits Available for Comparison");
        thirdTab = accordion.addTab(screenThree, "Review Environments Available for Comparison");
        fourthTab = accordion.addTab(screenFour, "View Results");
        
        secondTab.setEnabled(false);
        thirdTab.setEnabled(false);
        fourthTab.setEnabled(false);
        
        accordion.addListener(new SelectedTabChangeListener() {
            @Override
            public void selectedTabChange(SelectedTabChangeEvent event) {
                Component selected =accordion.getSelectedTab();
                Tab tab = accordion.getTab(selected);
                
                if(tab!=null && tab.equals(firstTab)){
                    secondTab.setEnabled(false);
                    thirdTab.setEnabled(false);
                    fourthTab.setEnabled(false);
                } else if(tab!=null && tab.equals(secondTab)){
                    thirdTab.setEnabled(false);
                    fourthTab.setEnabled(false);
                } else if(tab!=null && tab.equals(thirdTab)){   
                    fourthTab.setEnabled(false); 
                } 
            }
        });
        
        addComponent(accordion);
    }

    @Override
    public void updateLabels() {
    // TODO Auto-generated method stub
    }
    
    private void setTitleContent(String guideMessage){
        titleLayout.removeAllComponents();
        
        String title =  "<h1>Breeder Queries:</h1> <h1>Simple Head-to-Head Comparison Query</h1> <h2>" + VERSION + "</h2>";
        mainTitle = new Label();
        mainTitle.setStyleName("gcp-window-title");
        mainTitle.setContentMode(Label.CONTENT_XHTML);
        mainTitle.setValue(title);
        titleLayout.addComponent(mainTitle);
        
        /**
        Label descLbl = new Label(guideMessage);
        descLbl.setWidth("300px");
        
        PopupView popup = new PopupView("?",descLbl);
        popup.setStyleName("gcp-popup-view");
        titleLayout.addComponent(popup);
        
        titleLayout.setComponentAlignment(popup, Alignment.MIDDLE_LEFT);
        **/
    }
    
    public void selectFirstTab(){
        this.accordion.setSelectedTab(screenOne);
        secondTab.setEnabled(false);
        thirdTab.setEnabled(false);
        fourthTab.setEnabled(false);
    }
    
    public void selectSecondTab(){
        secondTab.setEnabled(true);
        this.accordion.setSelectedTab(screenTwo);
        thirdTab.setEnabled(false);
        fourthTab.setEnabled(false);
    }
    
    public void selectThirdTab(){
        thirdTab.setEnabled(true);
        this.accordion.setSelectedTab(screenThree);
        fourthTab.setEnabled(false);
    }
    
    public void selectFourthTab(){
        fourthTab.setEnabled(true);
        this.accordion.setSelectedTab(screenFour);
    }
}
