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
package org.generationcp.browser.cross.study.h2h.main.dialogs;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.h2h.main.SpecifyGermplasmsComponent;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.browser.germplasmlist.listeners.CloseWindowAction;
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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
 * @author Mark Agarrado
 *
 */
@Configurable
public class SelectGermplasmListDialog extends Window implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -8113004135173349534L;
    
    public final static String CLOSE_BUTTON_ID = "SelectGermplasmListDialog Close Button";
    public final static String ADD_BUTTON_ID = "SelectGermplasmListDialog Add Button";
    
    private VerticalLayout mainLayout;
    private SelectGermplasmListComponent selectGermplasmList;
    private Button cancelButton;
    private Button doneButton;
    private HorizontalLayout buttonArea;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private boolean isTestEntry;
    private boolean doCloseDialog = true;

    private Component source;

    public SelectGermplasmListDialog() {
        super();
    }
    
    public SelectGermplasmListDialog(Component source, Window parentWindow, boolean isTestEntry){
        this.source = source;
        this.isTestEntry = isTestEntry;        
    }
    
    protected void assemble() {
        initializeComponents();
        initializeValues();
        initializeLayout();
        initializeActions();
    }
    
    protected void initializeComponents() {
        mainLayout = new VerticalLayout();
        selectGermplasmList = new SelectGermplasmListComponent(null,this);
        
        buttonArea = new HorizontalLayout();
        cancelButton = new Button(); // "Cancel"
        cancelButton.setData(CLOSE_BUTTON_ID);
        doneButton = new Button(); // "Done"
        doneButton.setData(ADD_BUTTON_ID);
        doneButton.setEnabled(false);
        doneButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
    }
    
    public void setDoneButton(boolean bool){
    	doneButton.setEnabled(bool);
    }
    protected void initializeValues() {
        
    }

    protected void initializeLayout() {
        // set as modal window, other components are disabled while window is open
        setModal(true);
        // define window size, set as not resizable
        setWidth("800px");
        setHeight("540px");
        setResizable(false);
        setCaption("Select Germplasm List");
        // center window within the browser
        center();
        
        buttonArea.setMargin(false, true, false, true);
        buttonArea.setSpacing(true);
        
        buttonArea.addComponent(doneButton);
        buttonArea.addComponent(cancelButton);
        
        mainLayout.addComponent(selectGermplasmList);
        mainLayout.addComponent(buttonArea);
        mainLayout.setComponentAlignment(buttonArea, Alignment.MIDDLE_RIGHT);
        
        this.setContent(mainLayout);
    }
    
    protected void initializeActions() {
        doneButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        // only close window if calling screen used the list successfully.
        // eg. table permutations will not cause heap space error
        doneButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (doCloseDialog){
					Window window = event.getButton().getWindow();
					window.getParent().removeWindow(window);
				}
			}
		});

        cancelButton.addListener(new CloseWindowAction());
    }
    
    // called by SelectListButtonClickListener for the "Done" button
    public void populateParentList() {
        // retrieve list entries and add them to the parent ListSelect component
    	SelectGermplasmListInfoComponent listInfoComponent = selectGermplasmList.getListInfoComponent();
		doCloseDialog = ((SpecifyGermplasmsComponent)source).addGermplasmList(
    			listInfoComponent.getGermplasmListId(), listInfoComponent.getEntriesTable().size(), isTestEntry);

    	Table listEntryValues = listInfoComponent.getEntriesTable();
        // remove existing list entries if selected list has entries
        if (listEntryValues.size() == 0) {
            doneButton.setEnabled(false);
        } else {
        	doneButton.setEnabled(true);
        }
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        assemble();
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        messageSource.setCaption(cancelButton, Message.CLOSE_SCREEN_LABEL);
        messageSource.setCaption(doneButton, Message.ADD_LIST_ENTRY);
    }
}
