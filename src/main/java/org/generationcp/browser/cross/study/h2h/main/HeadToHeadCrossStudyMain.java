package org.generationcp.browser.cross.study.h2h.main;


import org.generationcp.browser.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Autowired;


import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class HeadToHeadCrossStudyMain  extends VerticalLayout implements InitializingBean, InternationalizableComponent {

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

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    private static final String GUIDE_MESSAGE = "The head-to-head comparison compares each entry in a list of test" +
            " entries with each entry in a list of standard entries. " +
            " Either or both lists might consist of only one entry. " +
            "The comparison is done pairwise on evaluation data over environments " +
            "where both entries were evaluated. The key result is the proportion of environments " +
            "where the test entry surpassed the standard entry for a particular trait.";


    @Override
    public void afterPropertiesSet() throws Exception {
    setMargin(false);
    setSpacing(true);

    titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        setTitleContent(GUIDE_MESSAGE);
        addComponent(titleLayout);

        accordion = new Accordion();
        accordion.setWidth("1000px");

        screenFour = new ResultsComponent(this);
        screenThree = new EnvironmentsAvailableComponent(this, screenFour);
        screenTwo = new TraitsAvailableComponent(this, screenThree);
        screenOne = new SpecifyGermplasmsComponent(this, screenTwo, screenFour);








        firstTab = accordion.addTab(screenOne, messageSource.getMessage(Message.SPECIFY_ENTRIES));
        secondTab = accordion.addTab(screenTwo,  messageSource.getMessage(Message.SELECT_TRAITS));
        thirdTab = accordion.addTab(screenThree, messageSource.getMessage(Message.SELECT_ENVIRONMENTS));
        fourthTab = accordion.addTab(screenFour, messageSource.getMessage(Message.DISPLAY_RESULTS));

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

        String title =  "Head to head comparison <h2>" + VERSION + "</h2>";
        mainTitle = new Label();
        mainTitle.setStyleName("gcp-window-title");
        mainTitle.setContentMode(Label.CONTENT_XHTML);
        mainTitle.setValue(title);
        titleLayout.addComponent(mainTitle);


        Label descLbl = new Label(guideMessage);
        descLbl.setWidth("300px");

        PopupView popup = new PopupView("?",descLbl);
        popup.setStyleName("gcp-popup-view");
        titleLayout.addComponent(popup);

        titleLayout.setComponentAlignment(popup, Alignment.MIDDLE_LEFT);

    }

    public void selectFirstTab(){
    	firstTab.setEnabled(true);
        this.accordion.setSelectedTab(screenOne);
        secondTab.setEnabled(false);
        thirdTab.setEnabled(false);
        fourthTab.setEnabled(false);
    }

    public void selectSecondTab(){
        secondTab.setEnabled(true);
        firstTab.setEnabled(false);
        this.accordion.setSelectedTab(screenTwo);
        thirdTab.setEnabled(false);
        fourthTab.setEnabled(false);
    }

    public void selectThirdTab(){
        firstTab.setEnabled(false);
        secondTab.setEnabled(false);
        thirdTab.setEnabled(true);
        this.accordion.setSelectedTab(screenThree);
        fourthTab.setEnabled(false);
    }

    public void selectFourthTab(){
        firstTab.setEnabled(false);
        secondTab.setEnabled(false);
        thirdTab.setEnabled(false);
        fourthTab.setEnabled(true);
        this.accordion.setSelectedTab(screenFour);
    }
}
