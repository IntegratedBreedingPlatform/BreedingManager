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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListButtonClickListener;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.VaadinComponentsUtil;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
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
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Select;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class ListManagerCopyToListDialog extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		Property.ValueChangeListener, AbstractSelect.NewItemHandler, BreedingManagerLayout {

	private static final Logger LOG = LoggerFactory.getLogger(ListManagerCopyToListDialog.class);
	private static final long serialVersionUID = 1L;

	static final String FOLDER_TYPE = "FOLDER";

	public static final Object SAVE_BUTTON_ID = "Save New List Entries";
	public static final String CANCEL_BUTTON_ID = "Cancel Copying New List Entries";

	private Label labelListName;
	private Label labelListNameDescription;
	private PopupView labelListNameDescriptionPopUpView;
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
	private Table listEntriesTable;
	private final String listName;
	private int newListid;
	private String listNameValue;
	private final int ibdbUserId;
	private List<GermplasmList> germplasmList;
	private Map<String, Integer> mapExistingList;
	private boolean lastAdded = false;
	private boolean existingListSelected = false;

	private Set<String> localFolderNames = new HashSet<String>();

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Resource
	private ContextUtil contextUtil;

	private ListManagerMain listManagerMain;

	public ListManagerCopyToListDialog(final Window mainWindow, final Window dialogWindow, final String listName,
			final Table listEntriesTable, final int ibdbUserId, final ListManagerMain listManagerMain) {
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
		this.labelListName.setDebugId("labelListName");
		this.labelListName.addStyleName("bold");
		
		this.labelListNameDescription = new Label(this.messageSource.getMessage(Message.LIST_NAME_LABEL_DESCRIPTION));
		this.labelListNameDescription.setDebugId("labelListNameDescription");
		this.labelListNameDescription.setWidth("250px");
		
		this.labelListNameDescriptionPopUpView = new PopupView("?", this.labelListNameDescription);
		this.labelListNameDescriptionPopUpView.setDebugId("labelListNameDescriptionPopUpView");
		this.labelListNameDescriptionPopUpView.addStyleName(AppConstants.CssStyles.POPUP_VIEW);
		
		this.labelDescription = new Label(this.messageSource.getMessage(Message.DESCRIPTION_LABEL));
		this.labelDescription.setDebugId("labelDescription");
		this.labelDescription.addStyleName("bold");

		this.labelType = new Label(this.messageSource.getMessage(Message.TYPE_LABEL));
		this.labelType.setDebugId("labelType");
		this.labelType.addStyleName("bold");

		this.comboBoxListName = new ComboBox();
		this.comboBoxListName.setDebugId("comboBoxListName");
		this.comboBoxListName.setNewItemsAllowed(true);
		this.comboBoxListName.setNewItemHandler(this);
		this.comboBoxListName.setNullSelectionAllowed(false);
		this.comboBoxListName.setImmediate(true);
		this.comboBoxListName.focus();

		this.txtDescription = new TextField();
		this.txtDescription.setDebugId("txtDescription");
		this.txtDescription.setWidth("400px");

		this.txtType = new TextField();
		this.txtType.setDebugId("txtType");
		this.txtType.setWidth("200px");

		this.selectType = new Select();
		this.selectType.setDebugId("selectType");
		this.selectType.setNullSelectionAllowed(false);

		this.btnSave = new Button(this.messageSource.getMessage(Message.SAVE_LABEL));
		this.btnSave.setDebugId("btnSave");
		this.btnSave.setWidth("80px");
		this.btnSave.setData(ListManagerCopyToListDialog.SAVE_BUTTON_ID);
		this.btnSave.setDescription("Save New Germplasm List ");
		this.btnSave.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.btnCancel = new Button(this.messageSource.getMessage(Message.CANCEL));
		this.btnCancel.setDebugId("btnCancel");
		this.btnCancel.setWidth("80px");
		this.btnCancel.setData(ListManagerCopyToListDialog.CANCEL_BUTTON_ID);
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

		final HorizontalLayout hButton = new HorizontalLayout();
		hButton.setDebugId("hButton");
		hButton.setSpacing(true);
		hButton.addComponent(this.btnCancel);
		hButton.addComponent(this.btnSave);

		final GridLayout gridLayout = new GridLayout();
		gridLayout.setDebugId("gridLayout");
		gridLayout.setRows(3);
		gridLayout.setColumns(2);
		gridLayout.setSpacing(true);
		gridLayout.addComponent(this.labelListName, 0, 0);
		
		HorizontalLayout comboBoxVerticalLayout = new HorizontalLayout();
		comboBoxVerticalLayout.setDebugId("comboBoxVerticalLayout");
		comboBoxVerticalLayout.addComponent(this.comboBoxListName);
		comboBoxVerticalLayout.addComponent(this.labelListNameDescriptionPopUpView);
		gridLayout.addComponent(comboBoxVerticalLayout, 1, 0);
		
		gridLayout.addComponent(this.labelDescription, 0, 1);
		gridLayout.addComponent(this.txtDescription, 1, 1);
		gridLayout.addComponent(this.labelType, 0, 2);
		gridLayout.addComponent(this.selectType, 1, 2);

		this.addComponent(gridLayout);
		this.addComponent(hButton);
		this.setComponentAlignment(hButton, Alignment.MIDDLE_CENTER);
	}

	void populateSelectType(final Select selectType) {
		final List<UserDefinedField> listTypes = this.germplasmListManager.getGermplasmListTypes();
		VaadinComponentsUtil.populateSelectType(selectType, listTypes);
	}

	@Override
	public void updateLabels() {
		// not needed to override for now
	}

	void populateComboBoxListName() {
		this.germplasmList = this.germplasmListManager.getAllGermplasmListsByProgramUUID(this.contextUtil.getCurrentProgramUUID());
		this.mapExistingList = new HashMap<String, Integer>();
		this.comboBoxListName.addItem("");
		for (final GermplasmList gList : this.germplasmList) {
			if (!gList.getType().equals(ListManagerCopyToListDialog.FOLDER_TYPE)) {
				this.comboBoxListName.addItem(gList.getName());
				this.mapExistingList.put(gList.getName(), new Integer(gList.getId()));
			} else {
				this.localFolderNames.add(gList.getName());
			}
		}
		this.comboBoxListName.select("");

	}

	public void saveGermplasmListButtonClickAction() {
		this.listNameValue = this.comboBoxListName.getValue().toString();
		final String description = this.txtDescription.getValue().toString();

		Boolean proceedWithSave = true;

		if (this.localFolderNames.contains(this.listNameValue)) {
			MessageNotifier.showRequiredFieldError(this.getWindow(),
					this.messageSource.getMessage(Message.ERROR_EXISTING_GERMPLASM_LIST_FOLDER_NAME));
			proceedWithSave = false;
		}

		if (proceedWithSave) {

			if (this.listNameValue.trim().length() == 0) {
				MessageNotifier.showRequiredFieldError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_SPECIFY_LIST_NAME));
			} else if (this.listNameValue.trim().length() > 50) {
				MessageNotifier.showRequiredFieldError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_LIST_NAME_TOO_LONG));
				this.comboBoxListName.setValue("");
			} else {

				if (!this.existingListSelected) {
					final GermplasmList parent = null;
					final int statusListName = 1;
					final GermplasmList listNameData = new GermplasmList(null, this.listNameValue, DateUtil.getCurrentDateAsLongValue(),
							this.selectType.getValue().toString(), this.ibdbUserId, description, parent, statusListName);

					try {
						listNameData.setProgramUUID(this.contextUtil.getCurrentProgramUUID());
						this.newListid = this.germplasmListManager.addGermplasmList(listNameData);
						this.addGermplasm();
						this.mainWindow.removeWindow(this.dialogWindow);

					} catch (final MiddlewareQueryException e) {
						ListManagerCopyToListDialog.LOG.error(e.getMessage(), e);
						MessageNotifier.showError(this.getWindow().getParent().getWindow(),
								this.messageSource.getMessage(Message.UNSUCCESSFUL),
								this.messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_COPY_TO_NEW_LIST_FAILED));
					}
				} else {

					try {
						final String listId = String.valueOf(this.mapExistingList.get(this.comboBoxListName.getValue()));
						final GermplasmList germList = this.germplasmListManager.getGermplasmListById(Integer.valueOf(listId));
						final int countOfExistingList =
								(int) this.germplasmListManager.countGermplasmListDataByListId(Integer.valueOf(listId));
						this.addGermplasmListData(germList, countOfExistingList + 1);
						this.mainWindow.removeWindow(this.dialogWindow);

						this.listManagerMain.getListSelectionComponent().getListTreeComponent().createTree();
						this.listManagerMain.getListSelectionComponent().getListTreeComponent().expandNode(ListSelectorComponent.LISTS);
						this.listManagerMain.getListSelectionComponent().getListDetailsLayout().removeTab(Integer.valueOf(listId));
						this.listManagerMain.getListSelectionComponent().getListTreeComponent()
								.treeItemClickAction(Integer.valueOf(listId));

					} catch (final MiddlewareQueryException e) {
						ListManagerCopyToListDialog.LOG.error(e.getMessage(), e);
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
			final GermplasmList germList = this.germplasmListManager.getGermplasmListById(this.newListid);
			this.addGermplasmListData(germList, 1);
			this.listManagerMain.getListSelectionComponent().getListTreeComponent().createTree();
			this.listManagerMain.getListSelectionComponent().getListTreeComponent().expandNode(ListSelectorComponent.LISTS);
			this.listManagerMain.getListSelectionComponent().getListTreeComponent().treeItemClickAction(this.newListid);
		} catch (final MiddlewareQueryException e) {
			this.germplasmListManager.deleteGermplasmListByListId(this.newListid);
			ListManagerCopyToListDialog.LOG.error(e.getMessage(), e);
			MessageNotifier.showError(this.getWindow().getParent().getWindow(), "Error with copying list entries.",
					"Copying of entries to a new list failed. " + this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}
	}

	private void addGermplasmListData(final GermplasmList germList, final int entryid) {
		final int status = 0;
		final int localRecordId = 0;
		int germplasmListDataEntryId = entryid;
		final Collection<?> selectedIds = (Collection<?>) this.listEntriesTable.getValue();
		final List<Integer> selectedGids = new ArrayList<>();
		for (final Object itemId : selectedIds) {
			final Property gidProperty = this.listEntriesTable.getItem(itemId).getItemProperty(ColumnLabels.GID.getName());
			final Button gidLinkButton = (Button) gidProperty.getValue();
			final int gid = Integer.valueOf(gidLinkButton.getCaption().toString());
			selectedGids.add(gid);
		}

		final Map<Integer, String> preferredNames = this.germplasmDataManager.getPreferredNamesByGids(selectedGids);
		for (final Object itemId : selectedIds) {
			final Property parentageProperty = this.listEntriesTable.getItem(itemId).getItemProperty(ColumnLabels.PARENTAGE.getName());
			final Property entryIdProperty = this.listEntriesTable.getItem(itemId).getItemProperty(ColumnLabels.ENTRY_ID.getName());
			final Property gidProperty = this.listEntriesTable.getItem(itemId).getItemProperty(ColumnLabels.GID.getName());
			final Property seedSourceProperty = this.listEntriesTable.getItem(itemId).getItemProperty(ColumnLabels.SEED_SOURCE.getName());

			final Button gidLinkButton = (Button) gidProperty.getValue();
			final int gid = Integer.valueOf(gidLinkButton.getCaption().toString());
			final String entryIdOfList = String.valueOf(entryIdProperty.getValue().toString());
			final String seedSource = String.valueOf(seedSourceProperty.getValue().toString());
			final String designation = preferredNames.get(gid);
			final String groupName = String.valueOf(parentageProperty.getValue().toString());

			final GermplasmListData germplasmListData = new GermplasmListData(null, germList, gid, germplasmListDataEntryId, entryIdOfList,
					seedSource, designation, groupName, status, localRecordId);
			this.germplasmListManager.addGermplasmListData(germplasmListData);

			germplasmListDataEntryId++;
		}
		if(!this.existingListSelected) {
			MessageNotifier.showMessage(this.getWindow().getParent().getWindow(), this.messageSource.getMessage(Message.SUCCESS),
					this.messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_COPY_TO_NEW_LIST_SUCCESS), 3000);
		} else {
			MessageNotifier.showMessage(this.getWindow().getParent().getWindow(), this.messageSource.getMessage(Message.SUCCESS),
					this.messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_COPY_TO_EXISTING_LIST_SUCCESS, this.listNameValue), 3000);
		}
		this.logCopyToNewListEntriesToWorkbenchProjectActivity();
	}

	private void logCopyToNewListEntriesToWorkbenchProjectActivity() {
		try {
			this.contextUtil.logProgramActivity("Copied entries into a new list.",
					"Copied entries to list " + this.newListid + " - " + this.listNameValue);
		} catch (final MiddlewareQueryException e) {
			ListManagerCopyToListDialog.LOG.error("Error with logging workbench activity.", e);
		}
	}

	public void cancelGermplasmListButtonClickAction() {
		this.mainWindow.removeWindow(this.dialogWindow);
	}

	@Override
	public void addNewItem(final String newItemCaption) {
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
	public void valueChange(final ValueChangeEvent event) {
		if (!this.lastAdded) {
			try {
				final String listNameId = String.valueOf(this.mapExistingList.get(this.comboBoxListName.getValue()));
				if (!"null".equals(listNameId)) {
					final GermplasmList gList = this.germplasmListManager.getGermplasmListById(Integer.valueOf(listNameId));
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
			} catch (final MiddlewareQueryException e) {
				ListManagerCopyToListDialog.LOG.error("Error in retrieving germplasm list.", e);
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

	/* For Test purposes */
	void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	void setComboListName(final ComboBox comboBox) {
		this.comboBoxListName = comboBox;
	}

	void setTxtDescription(final TextField txtDescription) {
		this.txtDescription = txtDescription;
	}

	void setLocalFolderNames(final Set<String> localFolderNames) {
		this.localFolderNames = localFolderNames;
	}

	void setSelectType(final Select selectType) {
		this.selectType = selectType;
	}

	Set<String> getLocalFolderNames() {
		return this.localFolderNames;
	}

	void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	void setExistingListSelected(final boolean existingListSelected) {
		this.existingListSelected = existingListSelected;
	}

	void setListEntriesTable(final Table table) {
		this.listEntriesTable = table;
	}

	void setListManagerMain(final ListManagerMain listManagerMain) {
		this.listManagerMain = listManagerMain;
	}

	void setMapExistingList(final Map<String, Integer> mapExistingList) {
		this.mapExistingList = mapExistingList;
	}

	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}
}
