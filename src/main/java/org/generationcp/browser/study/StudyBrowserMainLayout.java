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
import org.generationcp.browser.util.Util;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;


/**
 * @author Mark Agarrado
 *
 */
@Configurable
public class StudyBrowserMainLayout extends HorizontalLayout implements InitializingBean, GermplasmStudyBrowserLayout {
    
    private static final long serialVersionUID = -1375083442943045398L;
    
    private StudyBrowserMain studyBrowserMain;
    private Button closeAllTabsButton;
    private VerticalLayout studyDetailsLayout;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public StudyBrowserMainLayout(StudyBrowserMain studyBrowserMain) {
        this.studyBrowserMain = studyBrowserMain;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
    }
    
	@Override
	public void instantiateComponents() {        
        closeAllTabsButton = new Button(messageSource.getMessage(Message.CLOSE_ALL_TABS));
        closeAllTabsButton.setStyleName(BaseTheme.BUTTON_LINK);
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
        closeAllTabsButton.addListener(new ClickListener() {
            
            private static final long serialVersionUID = 3037464142423787558L;

            @Override
            public void buttonClick(ClickEvent event) {
                closeAllDetailTabs();
            }
        });
	}

	@Override
	public void layoutComponents() {
		setWidth("100%");
        
    	studyDetailsLayout = new VerticalLayout();
        studyDetailsLayout.setVisible(false);
        studyDetailsLayout.addComponent(closeAllTabsButton);
        studyDetailsLayout.setComponentAlignment(closeAllTabsButton, Alignment.TOP_RIGHT);
        
        addComponent(studyDetailsLayout);
	}
    
    public void addStudyInfoTabSheet(TabSheet tabSheet) {
        studyDetailsLayout.addComponent(tabSheet);
    }
    
    public void showDetailsLayout() {
        studyDetailsLayout.setVisible(true);
    }
    
    public void hideDetailsLayout() {
        studyDetailsLayout.setVisible(false);
    }
    
    public void closeAllDetailTabs(){
        Util.closeAllTab(studyBrowserMain.getCombinedStudyTreeComponent().getTabSheetStudy());
        hideDetailsLayout();
    }
}