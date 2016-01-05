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

package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListButtonClickListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class ListManagerCopyToNewListDialog extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		Property.ValueChangeListener, AbstractSelect.NewItemHandler, BreedingManagerLayout {

	private static final Logger LOG = LoggerFactory.getLogger(ListManagerCopyToNewListDialog.class);
	private static final long serialVersionUID = 1L;

	private static final String FOLDER_TYPE = "FOLDER";

	public static final Object SAVE_BUTTON_ID = "Save New List Entries";
	public static final String CANCEL_BUTTON_ID = "Cancel Copying New List Entries";
	public static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd";

	private Label labelListName;
	private Label labelDescription;
	private ComboBox comboBoxListName;
	private TextField txtDescription;
	private Label labelType;
	private TextField txtType;
	private final Window dialogWindow;
	private final Window mainWindow;
	private Button btnSave;
	private Button btnCancel;
	private Select selectType;
	private final Table listEntriesTable;
	private final String listName;
	private String designationOfListEntriesCopied;
	private int newListid;
	private String listNameValue;
	private final int ibdbUserId;
	private List<GermplasmList> germplasmList;
	private Map<String, Integer> mapExistingList;
	private boolean lastAdded = false;
	private boolean existingListSelected = false;

	private final Set<String> localFolderNames = new HashSet<String>();

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Resource
	private ContextUtil contextUtil;

	private final ListManagerMain listManagerMain;

	public ListManagerCopyToNewListDialog(Window mainWindow, Window dialogWindow, String listName, Table listEntriesTable, int ibdbUserId,
			ListManagerMain listManagerMain) {
		this.dialogWindow = dialogWindow;
		this.mainWindow = mainWindow;
		this.listEntriesTable = listEntriesTable;
		this.listName = listName;
		this.ibdbUserId = ibdbUserId;
		this.listManagerMain = listManagerMain;
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
		this.labelListName = new Label(this.messageSource.getMessage(Message.LIST_NAME_LABEL));
		this.labelListName.addStyleName("bold");

		this.labelDescription = new Label(this.messageSource.getMessage(Message.DESCRIPTION_LABEL));
		this.labelDescription.addStyleName("bold");

		this.labelType = new Label(this.messageSource.getMessage(Message.TYPE_LABEL));
		this.labelType.addStyleName("bold");

		this.comboBoxListName = new ComboBox();
		this.comboBoxListName.setNewItemsAllowed(true);
		this.comboBoxListName.setNewItemHandler(this);
		this.comboBoxListName.setNullSelectionAllowed(false);
		this.comboBoxListName.setImmediate(true);
		this.comboBoxListName.focus();

		this.txtDescription = new TextField();
		this.txtDescription.setWidth("400px");

		this.txtType = new TextField();
		this.txtType.setWidth("200px");

		this.selectType = new Select();
		this.selectType.setNullSelectionAllowed(false);

		this.btnSave = new Button(this.messageSource.getMessage(Message.SAVE_LABEL));
		this.btnSave.setWidth("80px");
		this.btnSave.setData(ListManagerCopyToNewListDialog.SAVE_BUTTON_ID);
		this.btnSave.setDescription("Save New Germplasm List ");
		this.btnSave.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.btnCancel = new Button(this.messageSource.getMessage(Message.CANCEL));
		this.btnCancel.setWidth("80px");
		this.btnCancel.setData(ListManagerCopyToNewListDialog.CANCEL_BUTTON_ID);
		this.btnCancel.setDescription("Cancel Saving New Germplasm List");
		this.btnCancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
	}

	@Override
	public void initializeValues() {
		this.populateComboBoxListName();
		this.populateSelectType(this.selectType);
	}

	@Override
	public void addListeners() {
		this.comboBoxListName.addListener(this);
		this.btnSave.addListener(new GermplasmListButtonClickListener(this));
		this.btnCancel.addListener(new GermplasmListButtonClickListener(this));
	}

	@Override
	public void layoutComponents() {
		this.setSpacing(true);

		HorizontalLayout hButton = new HorizontalLayout();
		hButton.setSpacing(true);
		hButton.addComponent(this.btnCancel);
		hButton.addComponent(this.btnSave);

		GridLayout gridLayout = new GridLayout();
		gridLayout.setRows(3);
		gridLayout.setColumns(2);
		gridLayout.setSpacing(true);
		gridLayout.addComponent(this.labelListName, 0, 0);
		gridLayout.addComponent(this.comboBoxListName, 1, 0);
		gridLayout.addComponent(this.labelDescription, 0, 1);
		gridLayout.addComponent(this.txtDescription, 1, 1);
		gridLayout.addComponent(this.labelType, 0, 2);
		gridLayout.addComponent(this.selectType, 1, 2);

		this.addComponent(gridLayout);
		this.addComponent(hButton);
		this.setComponentAlignment(hButton, Alignment.MIDDLE_CENTER);
	}

	private void populateSelectType(Select selectType) {
		List<UserDefinedField> listTypes;
		try {
			listTypes = this.germplasmListManager.getGermplasmListTypes();

			for (UserDefinedField listType : listTypes) {
				String typeCode = listType.getFcode();
				selectType.addItem(typeCode);
				selectType.setItemCaption(typeCode, listType.getFname());
				// set "GERMPLASMLISTS" as the default value
				if ("LST".equals(typeCode)) {
					selectType.setValue(typeCode);
				}
			}

		} catch (MiddlewareQueryException e) {
			ListManagerCopyToNewListDialog.LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public void updateLabels() {
		// not needed to override for now
	}

	private void populateComboBoxListName() {
		try {
			this.germplasmList =
					this.germplasmListManager.getAllGermplasmLists(0, (int) this.germplasmListManager.countAllGermplasmLists());
			this.mapExistingList = new HashMap<String, Integer>();
			this.comboBoxListName.addItem("");
			for (GermplasmList gList : this.germplasmList) {
				if (!gList.getName().equals(this.listName)) {
					if (!gList.getType().equals(ListManagerCopyToNewListDialog.FOLDER_TYPE)) {
						this.comboBoxListName.addItem(gList.getName());
						this.mapExistingList.put(gList.getName(), new Integer(gList.getId()));
					} else {
						this.localFolderNames.add(gList.getName());
					}
				}
			}
			this.comboBoxListName.select("");

		} catch (MiddlewareQueryException e) {
			ListManagerCopyToNewListDialog.LOG.error(e.getMessage(), e);
		}
	}

	public void saveGermplasmListButtonClickAction() {

		this.listNameValue = this.comboBoxListName.getValue().toString();
		String description = this.txtDescription.getValue().toString();

		Boolean proceedWithSave = true;

		try {
			Long matchingNamesCount = this.germplasmListManager.countGermplasmListByName(this.listNameValue, Operation.EQUAL);
			String existingListMsg = "There is already an existing germplasm list with that name";
			if (matchingNamesCount > 0) {
				MessageNotifier.showRequiredFieldError(this.getWindow(), existingListMsg);
				proceedWithSave = false;
			}

			// if list name from copy source is equal to specified value in combo box
			if (!"".equals(this.listNameValue) && this.listName.equals(this.listNameValue)) {
				MessageNotifier.showRequiredFieldError(this.getWindow(), existingListMsg);
				proceedWithSave = false;
			}

			if (this.localFolderNames.contains(this.listNameValue)) {
				MessageNotifier.showRequiredFieldError(this.getWindow(),
						"There is already an existing germplasm list folder with that name");
				proceedWithSave = false;
			}
		} catch (MiddlewareQueryException e) {
			ListManagerCopyToNewListDialog.LOG.error("Error in counting germplasm list by name.", e);
			ListManagerCopyToNewListDialog.LOG.error("\n" + e.getStackTrace());
		}

		if (proceedWithSave) {

			if (this.listNameValue.trim().length() == 0) {
				MessageNotifier.showRequiredFieldError(this.getWindow(), "Please specify a List Name before saving");
			} else if (this.listNameValue.trim().length() > 50) {
				MessageNotifier.showRequiredFieldError(this.getWindow(),
						"Listname input is too large limit the name only up to 50 characters");
				this.comboBoxListName.setValue("");
			} else {

				if (!this.existingListSelected) {
					GermplasmList parent = null;
					int statusListName = 1;
					GermplasmList listNameData = new GermplasmList(null, this.listNameValue, DateUtil.getCurrentDateAsLongValue(),
							this.selectType.getValue().toString(), this.ibdbUserId, description, parent, statusListName);

					try {
						listNameData.setProgramUUID(this.contextUtil.getCurrentProgramUUID());
						this.newListid = this.germplasmListManager.addGermplasmList(listNameData);
						this.addGermplasm();
						this.mainWindow.removeWindow(this.dialogWindow);

					} catch (MiddlewareQueryException e) {
						ListManagerCopyToNewListDialog.LOG.error(e.getMessage(), e);
						MessageNotifier.showError(this.getWindow().getParent().getWindow(),
								this.messageSource.getMessage(Message.UNSUCCESSFUL),
								this.messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_COPY_TO_NEW_LIST_FAILED));
					}
				} else {

					try {
						String listId = String.valueOf(this.mapExistingList.get(this.comboBoxListName.getValue()));
						GermplasmList germList = this.germplasmListManager.getGermplasmListById(Integer.valueOf(listId));
						int countOfExistingList = (int) this.germplasmListManager.countGermplasmListDataByListId(Integer.valueOf(listId));
						this.addGermplasmListData(germList, countOfExistingList + 1);
						this.mainWindow.removeWindow(this.dialogWindow);

						this.listManagerMain.getListSelectionComponent().getListTreeComponent().createTree();
						this.listManagerMain.getListSelectionComponent().getListTreeComponent().expandNode(ListSelectorComponent.LISTS);
						this.listManagerMain.getListSelectionComponent().getListDetailsLayout().removeTab(Integer.valueOf(listId));
						this.listManagerMain.getListSelectionComponent().getListTreeComponent()
								.treeItemClickAction(Integer.valueOf(listId));

					} catch (MiddlewareQueryException e) {
						ListManagerCopyToNewListDialog.LOG.error(e.getMessage(), e);
						MessageNotifier.showError(this.getWindow().getParent().getWindow(),
								this.messageSource.getMessage(Message.UNSUCCESSFUL),
								this.messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_COPY_TO_EXISTING_LIST_FAILED));
					}
				}
			}
		}
	}

	private void addGermplasm() {
		try {
			GermplasmList germList = this.germplasmListManager.getGermplasmListById(this.newListid);
			this.addGermplasmListData(germList, 1);
			this.listManagerMain.getListSelectionComponent().getListTreeComponent().createTree();
			this.listManagerMain.getListSelectionComponent().getListTreeComponent().expandNode(ListSelectorComponent.LISTS);
			this.listManagerMain.getListSelectionComponent().getListTreeComponent().treeItemClickAction(this.newListid);
		} catch (MiddlewareQueryException e) {
			this.germplasmListManager.deleteGermplasmListByListId(this.newListid);
			ListManagerCopyToNewListDialog.LOG.error(e.getMessage(), e);
			MessageNotifier.showError(this.getWindow().getParent().getWindow(), "Error with copying list entries.",
					"Copying of entries to a new list failed. " + this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}
	}

	private void addGermplasmListData(GermplasmList germList, int entryid) {
		int status = 0;
		int localRecordId = 0;
		int germplasmListDataEntryId = entryid;
		this.designationOfListEntriesCopied = "";
		Collection<?> selectedIds = (Collection<?>) this.listEntriesTable.getValue();
		for (final Object itemId : selectedIds) {
			Property pParentage = this.listEntriesTable.getItem(itemId).getItemProperty(ColumnLabels.PARENTAGE.getName());
			Property pEntryId = this.listEntriesTable.getItem(itemId).getItemProperty(ColumnLabels.ENTRY_ID.getName());
			Property pGid = this.listEntriesTable.getItem(itemId).getItemProperty(ColumnLabels.GID.getName());
			Property pDesignation = this.listEntriesTable.getItem(itemId).getItemProperty(ColumnLabels.DESIGNATION.getName());
			Property pSeedSource = this.listEntriesTable.getItem(itemId).getItemProperty(ColumnLabels.SEED_SOURCE.getName());

			Button pGidButton = (Button) pGid.getValue();
			int gid = Integer.valueOf(pGidButton.getCaption().toString());
			String entryIdOfList = String.valueOf(pEntryId.getValue().toString());
			String seedSource = String.valueOf(pSeedSource.getValue().toString());
			Button pDesigButton = (Button) pDesignation.getValue();
			String designation = String.valueOf(pDesigButton.getCaption().toString());
			this.designationOfListEntriesCopied += designation + ",";
			String groupName = String.valueOf(pParentage.getValue().toString());

			GermplasmListData germplasmListData = new GermplasmListData(null, germList, gid, germplasmListDataEntryId, entryIdOfList,
					seedSource, designation, groupName, status, localRecordId);
			this.germplasmListManager.addGermplasmListData(germplasmListData);

			germplasmListDataEntryId++;
		}

		this.designationOfListEntriesCopied =
				this.designationOfListEntriesCopied.substring(0, this.designationOfListEntriesCopied.length() - 1);

		MessageNotifier.showMessage(this.getWindow().getParent().getWindow(), this.messageSource.getMessage(Message.SUCCESS),
				this.messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_COPY_TO_NEW_LIST_SUCCESS), 3000);

		this.logCopyToNewListEntriesToWorkbenchProjectActivity();
	}

	private void logCopyToNewListEntriesToWorkbenchProjectActivity() {
		try {
			this.contextUtil.logProgramActivity("Copied entries into a new list.",
					"Copied entries to list " + this.newListid + " - " + this.listNameValue);
		} catch (MiddlewareQueryException e) {
			ListManagerCopyToNewListDialog.LOG.error("Error with logging workbench activity.", e);
		}
	}

	public void cancelGermplasmListButtonClickAction() {
		this.mainWindow.removeWindow(this.dialogWindow);
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

	@Override
	public void valueChange(ValueChangeEvent event) {
		if (!this.lastAdded) {
			try {
				String listNameId = String.valueOf(this.mapExistingList.get(this.comboBoxListName.getValue()));
				if (!"null".equals(listNameId)) {
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
				ListManagerCopyToNewListDialog.LOG.error("Error in retrieving germplasm list.", e);
				ListManagerCopyToNewListDialog.LOG.error("\n" + e.getStackTrace());
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
}
