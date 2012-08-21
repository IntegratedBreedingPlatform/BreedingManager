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

package org.generationcp.browser.germplasm;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.listeners.GermplasmButtonClickListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

@Configurable
public class SaveGermplasmListDialog extends GridLayout implements InitializingBean, InternationalizableComponent{
	
    private static final Logger LOG = LoggerFactory.getLogger(SaveGermplasmListDialog.class);
    private static final long serialVersionUID = 1L;
    public static final Object SAVE_BUTTON_ID = "Save Germplasm List";
    public static final String CANCEL_BUTTON_ID = "Cancel Saving";
    private TextField txtGermplasmListName;
    private Label labelListName;
    private Label labelDescription;
    private Label labelHidden;
    private TextField txtDescription;
    private Label labelType;
    private TextField txtType;
    private Label labelStatus;
    private CheckBox statusHidden;
    private CheckBox statusLocked;
    private CheckBox statusFinal;
    private Window dialogWindow;
    private Window mainWindow;

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    private Button btnSave;
    private Button btnCancel;
    private TabSheet tabSheet;
    private ComboBox comboBoxType;
    private Select selectType;

    public SaveGermplasmListDialog(Window mainWindow, Window dialogWindow, TabSheet tabSheet) {
        this.dialogWindow = dialogWindow;
        this.mainWindow = mainWindow;
        this.tabSheet = tabSheet;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void afterPropertiesSet() throws Exception {
        setRows(8);
        setColumns(3);
        setSpacing(true);
        setMargin(true);

        labelListName = new Label();
        labelDescription = new Label();
        labelType = new Label();
        labelStatus = new Label();

        txtGermplasmListName = new TextField();
        txtGermplasmListName.setWidth("300px");
        
        txtDescription = new TextField();
        txtDescription.setWidth("400px");
        
        txtType = new TextField();
        txtType.setWidth("200px");
  
        selectType = new Select ();
        selectType.addItem("LST");
        selectType.setNullSelectionAllowed(false);
        selectType.select("LST");
        
        statusHidden = new CheckBox("Hidden");
        statusHidden.setValue(false);

        statusLocked = new CheckBox("Locked");
        statusLocked.setValue(false);
        
        statusFinal = new CheckBox("Final");
        statusFinal.setValue(false);
        
        HorizontalLayout hStatus = new HorizontalLayout();
        hStatus.setSpacing(true);
        
        hStatus.addComponent(statusHidden);
        hStatus.addComponent(statusLocked);
        hStatus.addComponent(statusFinal);
        
        HorizontalLayout hButton = new HorizontalLayout();
        hButton.setSpacing(true);

        btnSave = new Button();
        btnSave.setWidth("80px");
        btnSave.setData(SAVE_BUTTON_ID);
        btnSave.setDescription("Save Germplasm List ");
        btnSave.addListener(new GermplasmButtonClickListener(this));

        hButton.addComponent(btnSave);

        btnCancel = new Button();
        btnCancel.setWidth("80px");
        btnCancel.setData(CANCEL_BUTTON_ID);
        btnCancel.setDescription("Cancel Saving Germplasm List");
        btnCancel.addListener(new GermplasmButtonClickListener(this));

        hButton.addComponent(btnCancel);

        addComponent(labelListName, 1, 1);
        addComponent(txtGermplasmListName, 2, 1);
        addComponent(labelDescription, 1,2);
        addComponent(txtDescription, 2, 2);
        addComponent(labelType, 1,3);
        addComponent(selectType, 2, 3);
        addComponent(labelStatus, 1, 4);
        addComponent(hStatus, 2, 4);
        addComponent(hButton, 1, 6);
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        messageSource.setCaption(labelListName, Message.listname_label);
        messageSource.setCaption(labelDescription, Message.description_label);
        messageSource.setCaption(labelType, Message.type_label);
        messageSource.setCaption(labelStatus, Message.status_label);
        messageSource.setCaption(btnSave, Message.save_germplasm_listname_button_label);
        messageSource.setCaption(btnCancel, Message.cancel_germplasm_listname_button_label);
    }

    public void saveGermplasmListButtonClickAction() throws InternationalizableException {
        SaveGermplasmListAction saveGermplasmAction = new SaveGermplasmListAction();

        String listName = txtGermplasmListName.getValue().toString();

        if (listName.length() > 0) {
            saveGermplasmAction.addGermplasListNameAndData(listName, this.tabSheet,txtDescription.getValue().toString(),selectType.getValue().toString(),statusHidden.getValue().toString(),statusLocked.getValue().toString(),statusFinal.getValue().toString());
            closeSavingGermplasmListDialog();
        }
    }

    public void cancelGermplasmListButtonClickAction() {
        closeSavingGermplasmListDialog();
    }

    public void closeSavingGermplasmListDialog() {
        this.mainWindow.removeWindow(dialogWindow);
    }

}
