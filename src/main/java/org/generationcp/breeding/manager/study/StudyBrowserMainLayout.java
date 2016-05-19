/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.breeding.manager.study;

import org.generationcp.breeding.manager.application.GermplasmStudyBrowserLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.util.Util;
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

	private final StudyBrowserMain studyBrowserMain;
	private Button closeAllTabsButton;
	private VerticalLayout studyDetailsLayout;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public StudyBrowserMainLayout(StudyBrowserMain studyBrowserMain) {
		this.studyBrowserMain = studyBrowserMain;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		this.closeAllTabsButton = new Button(this.messageSource.getMessage(Message.CLOSE_ALL_TABS));
		this.closeAllTabsButton.setStyleName(BaseTheme.BUTTON_LINK);
		this.closeAllTabsButton.setVisible(false);
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListeners() {
		this.closeAllTabsButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 3037464142423787558L;

			@Override
			public void buttonClick(ClickEvent event) {
				StudyBrowserMainLayout.this.closeAllDetailTabs();
			}
		});
	}

	@Override
	public void layoutComponents() {
		this.setWidth("100%");

		this.studyDetailsLayout = new VerticalLayout();
		this.studyDetailsLayout.setVisible(false);
		this.studyDetailsLayout.addComponent(this.closeAllTabsButton);
		this.studyDetailsLayout.setComponentAlignment(this.closeAllTabsButton, Alignment.TOP_RIGHT);

		this.addComponent(this.studyDetailsLayout);
	}

	public void addStudyInfoTabSheet(TabSheet tabSheet) {
		this.studyDetailsLayout.addComponent(tabSheet);
	}

	public void showDetailsLayout() {
		this.studyDetailsLayout.setVisible(true);
		if (this.studyBrowserMain.getCombinedStudyTreeComponent().getTabSheetStudy().getComponentCount() > 1) {
			this.closeAllTabsButton.setVisible(true);
		}
	}

	public void hideDetailsLayout() {
		this.studyDetailsLayout.setVisible(false);
		this.closeAllTabsButton.setVisible(false);
	}

	public void closeAllDetailTabs() {
		Util.closeAllTab(this.studyBrowserMain.getCombinedStudyTreeComponent().getTabSheetStudy());
		this.hideDetailsLayout();
	}
}
