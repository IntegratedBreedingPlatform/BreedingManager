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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

@Configurable
public class SaveGermplasmListDialog extends GridLayout implements InitializingBean, InternationalizableComponent{

    private static final long serialVersionUID = 1L;
    public static final Object SAVE_BUTTON_ID = "Save Germplasm List";
    public static final String CANCEL_BUTTON_ID = "Cancel Saving";
    private TextField txtGermplasmListName;
    private Label labelEnterGermplasmListName;
    private Window dialogWindow;
    private Window mainWindow;

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    private Button btnSave;
    private Button btnCancel;
    private TabSheet tabSheet;

    public SaveGermplasmListDialog(Window mainWindow, Window dialogWindow, TabSheet tabSheet) {
        this.dialogWindow = dialogWindow;
        this.mainWindow = mainWindow;
        this.tabSheet = tabSheet;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void afterPropertiesSet() throws Exception {
        setRows(7);
        setColumns(3);
        setSpacing(true);
        setMargin(true);

        labelEnterGermplasmListName = new Label();

        txtGermplasmListName = new TextField();
        txtGermplasmListName.setWidth(300);

        HorizontalLayout hButton = new HorizontalLayout();
        hButton.setSpacing(true);

        btnSave = new Button();
        btnSave.setData(SAVE_BUTTON_ID);
        btnSave.setDescription("Save Germplasm List ");
        btnSave.addListener(new GermplasmButtonClickListener(this));

        hButton.addComponent(btnSave);

        btnCancel = new Button();
        btnCancel.setData(CANCEL_BUTTON_ID);
        btnCancel.setDescription("Cancel Saving Germplasm List");
        btnCancel.addListener(new GermplasmButtonClickListener(this));

        hButton.addComponent(btnCancel);

        addComponent(labelEnterGermplasmListName, 1, 1);
        addComponent(txtGermplasmListName, 1, 2);
        addComponent(hButton, 1, 3);
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        messageSource.setCaption(labelEnterGermplasmListName, Message.enter_germplasm_listname_label);
        messageSource.setCaption(btnSave, Message.save_germplasm_listname_button_label);
        messageSource.setCaption(btnCancel, Message.cancel_germplasm_listname_button_label);
    }

    public void saveGermplasmListButtonClickAction() throws InternationalizableException {
        SaveGermplasmListAction saveGermplasmAction = new SaveGermplasmListAction();

        String listName = txtGermplasmListName.getValue().toString();

        if (listName.length() > 0) {
            saveGermplasmAction.addGermplasListNameAndData(listName, this.tabSheet);
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
