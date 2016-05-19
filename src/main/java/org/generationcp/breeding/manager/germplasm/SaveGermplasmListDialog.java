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

package org.generationcp.breeding.manager.germplasm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.germplasm.listeners.GermplasmButtonClickListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

@Configurable
public class SaveGermplasmListDialog extends GridLayout implements InitializingBean, InternationalizableComponent,
		Property.ValueChangeListener, AbstractSelect.NewItemHandler {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(SaveGermplasmListDialog.class);
	private static final long serialVersionUID = 1L;
	public static final Object SAVE_BUTTON_ID = "Save Germplasm List";
	public static final String CANCEL_BUTTON_ID = "Cancel Saving";
	private Label labelListName;
	private Label labelDescription;
	private TextField txtDescription;
	private Label labelType;
	private TextField txtType;
	private final Window dialogWindow;
	private final Window mainWindow;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	private Button btnSave;
	private Button btnCancel;
	private final TabSheet tabSheet;
	private ComboBox comboBoxListName;
	private Select selectType;
	private List<GermplasmList> germplasmList;
	private boolean lastAdded = false;
	private boolean existingListSelected = false;
	private Map<String, Integer> mapExistingList;

	public SaveGermplasmListDialog(Window mainWindow, Window dialogWindow, TabSheet tabSheet) {
		this.dialogWindow = dialogWindow;
		this.mainWindow = mainWindow;
		this.tabSheet = tabSheet;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setRows(8);
		this.setColumns(3);
		this.setSpacing(true);
		this.setMargin(true);

		this.labelListName = new Label();
		this.labelDescription = new Label();
		this.labelType = new Label();

		this.comboBoxListName = new ComboBox();
		this.populateComboBoxListName();
		this.comboBoxListName.setNewItemsAllowed(true);
		this.comboBoxListName.setNewItemHandler(this);
		this.comboBoxListName.setNullSelectionAllowed(false);
		this.comboBoxListName.addListener(this);
		this.comboBoxListName.setImmediate(true);

		this.txtDescription = new TextField();
		this.txtDescription.setWidth("400px");

		this.txtType = new TextField();
		this.txtType.setWidth("200px");

		this.selectType = new Select();
		this.populateSelectType(this.selectType);
		this.selectType.setNullSelectionAllowed(false);
		this.selectType.select("LST");

		HorizontalLayout hButton = new HorizontalLayout();
		hButton.setSpacing(true);
		this.btnSave = new Button();
		this.btnSave.setWidth("80px");
		this.btnSave.setData(SaveGermplasmListDialog.SAVE_BUTTON_ID);
		this.btnSave.setDescription("Save Germplasm List ");
		this.btnSave.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.btnSave.addListener(new GermplasmButtonClickListener(this));

		hButton.addComponent(this.btnSave);
		this.btnCancel = new Button();
		this.btnCancel.setWidth("80px");
		this.btnCancel.setData(SaveGermplasmListDialog.CANCEL_BUTTON_ID);
		this.btnCancel.setDescription("Cancel Saving Germplasm List");
		this.btnCancel.addListener(new GermplasmButtonClickListener(this));
		hButton.addComponent(this.btnCancel);

		this.addComponent(this.labelListName, 1, 1);
		this.addComponent(this.comboBoxListName, 2, 1);
		this.addComponent(this.labelDescription, 1, 2);
		this.addComponent(this.txtDescription, 2, 2);
		this.addComponent(this.labelType, 1, 3);
		this.addComponent(this.selectType, 2, 3);
		this.addComponent(hButton, 1, 6);
	}

	private void populateComboBoxListName() throws MiddlewareQueryException {
		this.germplasmList =
				this.germplasmListManager.getAllGermplasmLists(0, (int) this.germplasmListManager.countAllGermplasmLists(), Database.LOCAL);
		this.mapExistingList = new HashMap<String, Integer>();
		this.comboBoxListName.addItem("");
		for (GermplasmList gList : this.germplasmList) {
			this.comboBoxListName.addItem(gList.getName());
			this.mapExistingList.put(gList.getName(), new Integer(gList.getId()));
		}
		this.comboBoxListName.select("");
	}

	private void populateSelectType(Select selectType) throws MiddlewareQueryException {
		List<UserDefinedField> listTypes = this.germplasmListManager.getGermplasmListTypes();

		for (UserDefinedField listType : listTypes) {
			String typeCode = listType.getFcode();
			selectType.addItem(typeCode);
			selectType.setItemCaption(typeCode, listType.getFname());
			// set "GERMPLASMLISTS" as the default value
			if ("LST".equals(typeCode)) {
				selectType.setValue(typeCode);
			}
		}
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this.labelListName, Message.LIST_NAME_LABEL);
		this.messageSource.setCaption(this.labelDescription, Message.DESCRIPTION_LABEL);
		this.messageSource.setCaption(this.labelType, Message.TYPE_LABEL);
		this.messageSource.setCaption(this.btnSave, Message.SAVE_LABEL);
		this.messageSource.setCaption(this.btnCancel, Message.CANCEL_LABEL);
	}

	public void saveGermplasmListButtonClickAction() throws InternationalizableException {

		SaveGermplasmListAction saveGermplasmAction = new SaveGermplasmListAction();
		String listName = this.comboBoxListName.getValue().toString();
		String listNameId = String.valueOf(this.mapExistingList.get(this.comboBoxListName.getValue()));

		if (listName.trim().length() == 0) {
			this.getWindow().showNotification("List Name Input Error...", "Please specify a List Name before saving",
					Notification.TYPE_WARNING_MESSAGE);
		} else if (listName.trim().length() > 50) {
			this.getWindow().showNotification("List Name Input Error...",
					"Listname input is too large limit the name only up to 50 characters", Notification.TYPE_WARNING_MESSAGE);
			this.comboBoxListName.setValue("");
		} else {
			saveGermplasmAction.addGermplasListNameAndData(listName, listNameId, this.tabSheet, this.txtDescription.getValue().toString(),
					this.selectType.getValue().toString());
			this.closeSavingGermplasmListDialog();
			// display notification message
			MessageNotifier.showMessage(this.mainWindow, this.messageSource.getMessage(Message.SAVE_GERMPLASMS_TO_NEW_LIST_LABEL),
					this.messageSource.getMessage(Message.SAVE_GERMPLASMS_TO_NEW_LIST_SUCCESS));
		}
	}

	public void cancelGermplasmListButtonClickAction() {
		this.closeSavingGermplasmListDialog();
	}

	public void closeSavingGermplasmListDialog() {
		this.mainWindow.removeWindow(this.dialogWindow);
	}

	/*
	 * Shows a notification when a selection is made.
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		if (!this.lastAdded) {
			try {
				String listNameId = String.valueOf(this.mapExistingList.get(this.comboBoxListName.getValue()));
				if (listNameId != "null") {
					GermplasmList gList = this.germplasmListManager.getGermplasmListById(Integer.valueOf(listNameId));
					this.txtDescription.setValue(gList.getDescription());
					this.txtDescription.setEnabled(false);
					this.selectType.select(gList.getType());
					this.selectType.setEnabled(false);
					this.existingListSelected = true;
				} else {
					this.txtDescription.setValue("");
					this.txtDescription.setEnabled(true);
					this.selectType.select("LST");
					this.selectType.setEnabled(true);
				}
			} catch (MiddlewareQueryException e) {
				MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
						this.messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LIST_BY_ID));
			}
		} else {
			if (this.existingListSelected) {
				this.txtDescription.setValue("");
				this.existingListSelected = false;
			}
			this.txtDescription.setEnabled(true);
			this.selectType.select("LST");
			this.selectType.setEnabled(true);
		}
		this.lastAdded = false;
	}

	@Override
	public void addNewItem(String newItemCaption) {
		if (!this.comboBoxListName.containsId(newItemCaption)) {
			if (this.comboBoxListName.containsId("")) {
				this.comboBoxListName.removeItem("");
			}
			this.lastAdded = true;
			this.comboBoxListName.addItem(newItemCaption);
			this.comboBoxListName.setValue(newItemCaption);
		}

	}
}
