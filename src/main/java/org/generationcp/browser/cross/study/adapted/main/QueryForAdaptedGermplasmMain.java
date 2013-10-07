package org.generationcp.browser.cross.study.adapted.main;

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
public class QueryForAdaptedGermplasmMain extends VerticalLayout implements InitializingBean, InternationalizableComponent {
	 	private static final long serialVersionUID = -3488805933508882321L;
	    private static final String VERSION = "1.0.0-BETA";

	    private Accordion accordion;

	    private HorizontalLayout titleLayout;

	    private Label mainTitle;

	    private SpecifyAndWeighEnvironments screenOne;
	    private SetUpTraitFilter screenTwo;
	    private ResultsComponent screenThree;

	    private Tab firstTab;
	    private Tab secondTab;
	    private Tab thirdTab;
	    
	    @Autowired
	    private SimpleResourceBundleMessageSource messageSource;
	    
	    private static final String GUIDE_MESSAGE = "Find germplasms suitable for specific environmental conditions. " +
	    					"(Filter trial environments by the required environmental conditions, and find the germplasm " + 
	    					"with the best performance of some important trait(s) in those environments). ";
	    
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

	        screenThree = new ResultsComponent(this);
	        screenTwo = new SetUpTraitFilter(this, screenThree);
	        screenOne = new SpecifyAndWeighEnvironments(this, screenTwo, screenThree);

	        firstTab = accordion.addTab(screenOne, messageSource.getMessage(Message.SPECIFY_WEIGH_ENVIRONMENT));
	        secondTab = accordion.addTab(screenTwo,  messageSource.getMessage(Message.SETUP_TRAIT_FILTER));
	        thirdTab = accordion.addTab(screenThree, messageSource.getMessage(Message.DISPLAY_RESULTS));
	        
	        
	        firstTab.setEnabled(true);
	        secondTab.setEnabled(true);
	        thirdTab.setEnabled(false);

	        accordion.addListener(new SelectedTabChangeListener() {
	            @Override
	            public void selectedTabChange(SelectedTabChangeEvent event) {
	                Component selected =accordion.getSelectedTab();
	                Tab tab = accordion.getTab(selected);

	                if(tab!=null && tab.equals(firstTab)){
	                    secondTab.setEnabled(true);
	                    thirdTab.setEnabled(false);
	                } else if(tab!=null && tab.equals(secondTab)){
	                	firstTab.setEnabled(true);
	                	thirdTab.setEnabled(true);
	                } else if(tab!=null && tab.equals(thirdTab)){
	                	firstTab.setEnabled(false);
	                	thirdTab.setEnabled(true);
	                }
	            }
	        });
			
	        addComponent(accordion);
	    }
	    
	    private void setTitleContent(String guideMessage){
	        titleLayout.removeAllComponents();

	        String title =  "Query for Adapted Germplasm <h2>" + VERSION + "</h2>";
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

		@Override
		public void updateLabels() {
			// TODO Auto-generated method stub
			
		}
		
		public void selectFirstTabAndReset(){
	    	firstTab.setEnabled(true);
	        this.accordion.setSelectedTab(screenOne);
	        secondTab.setEnabled(true);
	        thirdTab.setEnabled(false);
	    }
	    
	    public void selectFirstTab(){
	    	firstTab.setEnabled(true);
	        this.accordion.setSelectedTab(screenOne);
	        secondTab.setEnabled(true);
	        thirdTab.setEnabled(false);
	    }

	    public void selectSecondTab(){
	    	firstTab.setEnabled(true);
	    	secondTab.setEnabled(true);
	        this.accordion.setSelectedTab(screenTwo);
	        thirdTab.setEnabled(true);
	    }

	    public void selectThirdTab(){
	        firstTab.setEnabled(false);
	        secondTab.setEnabled(true);
	        thirdTab.setEnabled(true);
	        this.accordion.setSelectedTab(screenThree);
	    }

}
