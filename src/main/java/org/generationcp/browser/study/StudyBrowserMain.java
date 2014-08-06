/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.browser.study;

import org.generationcp.browser.application.GermplasmStudyBrowserLayout;
import org.generationcp.browser.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class StudyBrowserMain extends VerticalLayout implements InitializingBean, 
						InternationalizableComponent, GermplasmStudyBrowserLayout  {

    private static final long serialVersionUID = 1L;
    private final static String VERSION = "1.2.0";
    
    private Label applicationTitle;
    private Label headingLabel;
    private Button browseForStudy;
    private Label or;
    private Button searchForStudy;
    private Label browseStudyDescriptionLabel;
    
    private StudyTreeComponent studyTreeComponent;
    private StudySearchMainComponent searchStudyComponent;
    
    private StudyBrowserMainLayout mainLayout;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public StudyBrowserMain() {
    }
    
    @Override
    public void afterPropertiesSet() {
        instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
    }
    
	@Override
	public void instantiateComponents() {
        String title =  "Study Browser  <h2>" + VERSION + "</h2>";
        applicationTitle = new Label();
        applicationTitle.setStyleName(Bootstrap.Typography.H1.styleName());
        applicationTitle.setContentMode(Label.CONTENT_XHTML);
        applicationTitle.setValue(title);
        
    	headingLabel = new Label(messageSource.getMessage(Message.BROWSE_STUDIES));
    	headingLabel.setImmediate(true);
    	headingLabel.setWidth("300px");
    	headingLabel.setHeight("20px");
    	headingLabel.setStyleName(Bootstrap.Typography.H4.styleName());
    	headingLabel.addStyleName("bold");
    	
    	browseForStudy = new Button();
    	browseForStudy.setImmediate(true);
    	browseForStudy.setStyleName(Reindeer.BUTTON_LINK);
    	browseForStudy.setWidth("45px");
    	
    	or = new Label("or");
    	
    	searchForStudy = new Button();
    	searchForStudy.setImmediate(true);
    	searchForStudy.setStyleName(Reindeer.BUTTON_LINK);
    	searchForStudy.setWidth("40px");
    	
    	browseStudyDescriptionLabel = new Label("for a study to work with.");
    	
    	mainLayout = new StudyBrowserMainLayout(this);
    	
    	studyTreeComponent = new StudyTreeComponent(this);
        searchStudyComponent = new StudySearchMainComponent(this);
	}

	@Override
	public void initializeValues() {
        browseForStudy.setCaption(messageSource.getMessage(Message.BROWSE_LABEL) + " ");
        searchForStudy.setCaption((messageSource.getMessage(Message.SEARCH_LABEL) + " ").toLowerCase());
	}

	@Override
	public void addListeners() {
		browseForStudy.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				openBrowseForStudyWindow();
			}
		});
		
		searchForStudy.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				openSearchForStudyWindow();
			}
		});
	}

	@Override
	public void layoutComponents() {
		setSpacing(true);
		setMargin(false, true, true, true);
		
        HorizontalLayout directionLayout = new HorizontalLayout();
        directionLayout.addStyleName("study-browser-main");
        directionLayout.setHeight("16px");
        directionLayout.setSpacing(true);
        directionLayout.addComponent(browseForStudy);
        directionLayout.addComponent(or);
        directionLayout.addComponent(searchForStudy);
        directionLayout.addComponent(browseStudyDescriptionLabel);
        directionLayout.setComponentAlignment(browseForStudy, Alignment.BOTTOM_CENTER);
        directionLayout.setComponentAlignment(searchForStudy, Alignment.BOTTOM_CENTER);
        
        addComponent(applicationTitle);
        addComponent(headingLabel);
        addComponent(directionLayout);
        addComponent(mainLayout);
	}
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
    	
    }
    
	public void openBrowseForStudyWindow() {
		launchListSelectionWindow(getWindow(),studyTreeComponent,messageSource.getMessage(Message.BROWSE_STUDIES));
	}
	
	public void openSearchForStudyWindow() {
		launchListSelectionWindow(getWindow(),searchStudyComponent,messageSource.getMessage(Message.SEARCH_STUDIES));
	}
	
	private Window launchListSelectionWindow (final Window window, final Component content, final String caption) {

        final CssLayout layout = new CssLayout();
        layout.setMargin(true);
        layout.addComponent(content);
        
        final Window popupWindow = new Window();
        
        if(caption.equals(messageSource.getMessage(Message.SEARCH_STUDIES))){
        	popupWindow.setHeight("400px");
        	
        	layout.setHeight("340px");
        	layout.setWidth("780px");
        }
        else{
        	popupWindow.setHeight("550px");
        	layout.setHeight("490px");
        	layout.setWidth("100%");
        }
        popupWindow.setWidth("782px");
        
        popupWindow.setModal(true);
        popupWindow.setResizable(false);
        popupWindow.center();
        popupWindow.setCaption(caption);
        popupWindow.setContent(layout);
        popupWindow.addStyleName(Reindeer.WINDOW_LIGHT);
        
        window.addWindow(popupWindow);
        
        return popupWindow;
	}
    
    public StudyTreeComponent getCombinedStudyTreeComponent(){
    	return studyTreeComponent;
    }
    
    public StudySearchMainComponent getStudySearchComponent(){
        return searchStudyComponent;
    }
    
    public StudyBrowserMainLayout getMainLayout() {
        return mainLayout;
    }
}
