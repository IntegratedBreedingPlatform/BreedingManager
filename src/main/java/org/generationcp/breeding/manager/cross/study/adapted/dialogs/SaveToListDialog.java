
package org.generationcp.breeding.manager.cross.study.adapted.dialogs;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.adapted.main.QueryForAdaptedGermplasmMain;
import org.generationcp.breeding.manager.cross.study.traitdonors.main.TraitDonorsQueryMain;
import org.generationcp.breeding.manager.germplasmlist.dialogs.SelectLocationFolderDialog;
import org.generationcp.breeding.manager.germplasmlist.dialogs.SelectLocationFolderDialogSource;
import org.generationcp.breeding.manager.germplasmlist.util.GermplasmListTreeUtil;
import org.generationcp.breeding.manager.util.CloseWindowAction;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
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
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SaveToListDialog extends BaseSubWindow
		implements InitializingBean, InternationalizableComponent, SelectLocationFolderDialogSource {

	private static final Logger LOG = LoggerFactory.getLogger(SaveToListDialog.class);
	private static final long serialVersionUID = 1L;
	public static final Object SAVE_BUTTON_ID = "Save Germplasm List";
	public static final String CANCEL_BUTTON_ID = "Cancel Saving";
	public static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd";
	private static final String ONE_HUNDRED_PX = "100px";

	private Label saveInFolderLabel;
	private Label folderToSaveListTo;
	private Label labelListName;
	private Label labelDescription;
	private TextField txtDescription;
	private Label labelType;
	private TextField txtName;

	private final Window parentWindow;
	private final Map<Integer, String> germplasmsMap;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Resource
	private ContextUtil contextUtil;

	private Button btnSave;
	private Button btnCancel;
	private Button changeLocationFolderButton;
	private ComboBox comboBoxListName;
	private Select selectType;
	private List<GermplasmList> germplasmList;
	private Map<String, Integer> mapExistingList;

	private QueryForAdaptedGermplasmMain mainScreen;
	private GermplasmList lastSelectedFolder;

	public SaveToListDialog(QueryForAdaptedGermplasmMain mainScreen, Component source, Window parentWindow,
			Map<Integer, String> germplasmsMap) {
		this.mainScreen = mainScreen;
		this.parentWindow = parentWindow;
		this.germplasmsMap = germplasmsMap;
	}

	public SaveToListDialog(TraitDonorsQueryMain mainScreen2, Component source, Window parentWindow, Map<Integer, String> germplasmsMap) {
		this.parentWindow = parentWindow;
		this.germplasmsMap = germplasmsMap;
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		// set as modal window, other components are disabled while window is open
		this.setModal(true);
		// define window size, set as not resizable
		this.setWidth("560px");
		this.setHeight("293px");
		this.setResizable(false);
		this.setCaption(this.messageSource.getMessage(Message.SAVE_GERMPLASM_LIST_WINDOW_LABEL));
		this.addStyleName(Reindeer.WINDOW_LIGHT);
		// center window within the browser
		this.center();

		AbsoluteLayout mainLayout = new AbsoluteLayout();
		mainLayout.setWidth("550px");
		mainLayout.setHeight("240px");
		mainLayout.addStyleName("white-bg");

		this.labelListName = new Label(this.messageSource.getMessage(Message.LIST_NAME_LABEL));
		this.labelListName.setWidth(SaveToListDialog.ONE_HUNDRED_PX);
		this.labelDescription = new Label(this.messageSource.getMessage(Message.DESCRIPTION_LABEL));
		this.labelDescription.setWidth(SaveToListDialog.ONE_HUNDRED_PX);
		this.labelType = new Label(this.messageSource.getMessage(Message.TYPE_LABEL));
		this.labelType.setWidth(SaveToListDialog.ONE_HUNDRED_PX);

		this.comboBoxListName = new ComboBox();
		this.populateComboBoxListName();
		this.comboBoxListName.setNewItemsAllowed(true);
		this.comboBoxListName.setNullSelectionAllowed(false);
		this.comboBoxListName.setImmediate(true);
		this.comboBoxListName.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 2852245342851718316L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				String listName = (String) SaveToListDialog.this.comboBoxListName.getValue();
				Integer listId = SaveToListDialog.this.mapExistingList.get(listName);

				if (listId != null) {
					try {
						GermplasmList list = SaveToListDialog.this.germplasmListManager.getGermplasmListById(listId);
						SaveToListDialog.this.txtDescription.setValue(list.getDescription());
						SaveToListDialog.this.txtDescription.setEnabled(false);
						SaveToListDialog.this.selectType.setValue(list.getType());
						SaveToListDialog.this.selectType.setEnabled(false);
						SaveToListDialog.this.setSelectedFolder(list.getParent());
						SaveToListDialog.this.changeLocationFolderButton.setVisible(false);
					} catch (MiddlewareQueryException ex) {
						SaveToListDialog.LOG.error("Error with retrieving list with id: " + listId, ex);
					}
				}
			}
		});

		this.comboBoxListName.setNewItemHandler(new AbstractSelect.NewItemHandler() {

			private static final long serialVersionUID = 2934507069390997826L;

			@Override
			public void addNewItem(String newItemCaption) {
				SaveToListDialog.this.txtDescription.setValue("");
				SaveToListDialog.this.txtDescription.setEnabled(true);
				SaveToListDialog.this.selectType.setValue("LST");
				SaveToListDialog.this.selectType.setEnabled(true);
				SaveToListDialog.this.setSelectedFolder(SaveToListDialog.this.lastSelectedFolder);
				SaveToListDialog.this.changeLocationFolderButton.setVisible(true);
				SaveToListDialog.this.comboBoxListName.addItem(newItemCaption);
				SaveToListDialog.this.comboBoxListName.setValue(newItemCaption);
			}
		});

		this.txtDescription = new TextField();
		this.txtDescription.setWidth("400px");

		this.txtName = new TextField();
		this.txtName.setWidth("200px");

		this.selectType = new Select();
		this.populateSelectType(this.selectType);
		this.selectType.setNullSelectionAllowed(false);
		this.selectType.select("LST");

		HorizontalLayout hButton = new HorizontalLayout();
		hButton.setSpacing(true);
		this.btnSave = new Button(this.messageSource.getMessage(Message.SAVE_LABEL));
		this.btnSave.setWidth("80px");
		this.btnSave.setData(SaveToListDialog.SAVE_BUTTON_ID);
		this.btnSave.setDescription("Save Germplasm List ");
		this.btnSave.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.btnSave.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				SaveToListDialog.this.saveButtonClickAction();
			}
		});

		hButton.addComponent(this.btnSave);
		this.btnCancel = new Button(this.messageSource.getMessage(Message.CANCEL_LABEL));
		this.btnCancel.setWidth("80px");
		this.btnCancel.setData(SaveToListDialog.CANCEL_BUTTON_ID);
		this.btnCancel.setDescription("Cancel Saving Germplasm List");
		this.btnCancel.addListener(new CloseWindowAction());
		hButton.addComponent(this.btnCancel);

		this.saveInFolderLabel = new Label(this.messageSource.getMessage(Message.SAVE_IN_WITH_COLON));
		this.saveInFolderLabel.setWidth(SaveToListDialog.ONE_HUNDRED_PX);

		this.folderToSaveListTo = new Label("Lists");
		this.folderToSaveListTo.setData(null);
		this.folderToSaveListTo.addStyleName("not-bold");
		this.folderToSaveListTo.setWidth("300px");

		this.changeLocationFolderButton = new Button(this.messageSource.getMessage(Message.CHANGE_LOCATION));
		this.changeLocationFolderButton.addStyleName(BaseTheme.BUTTON_LINK);
		this.changeLocationFolderButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 415799611820196717L;

			@Override
			public void buttonClick(ClickEvent event) {
				SaveToListDialog.this.displaySelectFolderDialog();
			}
		});

		mainLayout.addComponent(this.saveInFolderLabel, "top:30px;left:20px");
		mainLayout.addComponent(this.folderToSaveListTo, "top:30px;left:110px");
		mainLayout.addComponent(this.changeLocationFolderButton, "top:28px;left:415px");
		mainLayout.addComponent(this.labelListName, "top:60px;left:20px");
		mainLayout.addComponent(this.comboBoxListName, "top:58px;left:110px");
		mainLayout.addComponent(this.labelDescription, "top:90px;left:20px");
		mainLayout.addComponent(this.txtDescription, "top:88px;left:110px");
		mainLayout.addComponent(this.labelType, "top:120px;left:20px");
		mainLayout.addComponent(this.selectType, "top:118px;left:110px");
		mainLayout.addComponent(hButton, "top:170px;left:150px");

		this.setContent(mainLayout);
	}

	protected void populateComboBoxListName() throws MiddlewareQueryException {
		this.germplasmList = this.germplasmListManager.getAllGermplasmLists(0, (int) this.germplasmListManager.countAllGermplasmLists());
		this.mapExistingList = new HashMap<String, Integer>();
		this.comboBoxListName.addItem("");
		for (GermplasmList gList : this.germplasmList) {
			if (!"FOLDER".equals(gList.getType())
					&& (gList.getProgramUUID() == null || gList.getProgramUUID().equals(this.contextUtil.getCurrentProgramUUID()))) {
				this.comboBoxListName.addItem(gList.getName());
				this.mapExistingList.put(gList.getName(), new Integer(gList.getId()));
			}
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

	public void saveButtonClickAction() {
		String listName = this.comboBoxListName.getValue().toString();

		try {
			this.validateListNameToSave(listName);
		} catch (InvalidValueException e) {
			SaveToListDialog.LOG.error(e.getMessage(), e);
			MessageNotifier.showRequiredFieldError(this.parentWindow, e.getMessage());
			this.comboBoxListName.setValue("");
			return;
		} catch (MiddlewareQueryException e) {
			SaveToListDialog.LOG.error(e.getMessage(), e);
		}

		// proceed with the saving of germplasm list
		String listNameId = String.valueOf(this.mapExistingList.get(this.comboBoxListName.getValue()));
		this.addGermplasListNameAndData(listName, listNameId, this.germplasmsMap, this.txtDescription.getValue().toString(),
				this.selectType.getValue().toString());
		this.closeSavingGermplasmListDialog();

		this.mainScreen.selectWelcomeTab();

		// display notification message
		MessageNotifier.showMessage(this.parentWindow, this.messageSource.getMessage(Message.SAVE_GERMPLASMS_TO_NEW_LIST_LABEL),
				this.messageSource.getMessage(Message.SAVE_GERMPLASMS_TO_NEW_LIST_SUCCESS));
	}

	protected void validateListNameToSave(String listName) throws MiddlewareQueryException {

		Long matchingNamesCount = this.germplasmListManager.countGermplasmListByName(listName, Operation.EQUAL);
		if (matchingNamesCount > 0) {
			throw new InvalidValueException("There is already an existing germplasm list with that name");
		}

		if (listName.trim().length() == 0) {
			throw new InvalidValueException("Please specify a List Name before saving");
		}

		if (listName.trim().length() > 50) {
			throw new InvalidValueException("Listname input is too large limit the name only up to 50 characters");
		}
	}

	private void addGermplasListNameAndData(String listName, String listId, Map<Integer, String> germplasmsMap, String description,
			String type) {

		try {
			Integer userId = this.contextUtil.getCurrentUserLocalId();
			GermplasmList parent = (GermplasmList) this.folderToSaveListTo.getData();
			int statusListName = 1;
			String gidListString = "";

			if ("null".equals(listId)) {
				GermplasmList listNameData = new GermplasmList(null, listName, DateUtil.getCurrentDateAsLongValue(), type, userId,
						description, parent, statusListName);
				listNameData.setProgramUUID(this.contextUtil.getCurrentProgramUUID());

				int listid = this.germplasmListManager.addGermplasmList(listNameData);

				GermplasmList germList = this.germplasmListManager.getGermplasmListById(listid);

				String groupName = "-";
				String designation = "-";
				int status = 0;
				int localRecordId = 0;
				int entryid = 1;

				for (Map.Entry<Integer, String> entry : germplasmsMap.entrySet()) {

					Integer gid = entry.getKey();
					designation = entry.getValue() == null ? "-" : entry.getValue();

					String entryCode = String.valueOf(entryid);
					String seedSource = "Browse for " + designation;

					GermplasmListData germplasmListData = new GermplasmListData(null, germList, gid, entryid, entryCode, seedSource,
							designation, groupName, status, localRecordId);

					this.germplasmListManager.addGermplasmListData(germplasmListData);

					entryid++;

					gidListString = gidListString + ", " + Integer.toString(gid);

				}

			} else {

				GermplasmList germList = this.germplasmListManager.getGermplasmListById(Integer.valueOf(listId));
				String groupName = "-";
				String designation = "-";
				int status = 0;
				int localRecordId = 0;
				int entryid = (int) this.germplasmListManager.countGermplasmListDataByListId(Integer.valueOf(listId));

				for (Map.Entry<Integer, String> entry : germplasmsMap.entrySet()) {
					Integer gid = entry.getKey();

					String entryCode = entry.getValue() == null ? "-" : entry.getValue();

					String seedSource = "Browse for " + entryCode;

					// check if there is existing gid in the list
					List<GermplasmListData> existingList =
							this.germplasmListManager.getGermplasmListDataByListIdAndGID(Integer.valueOf(listId), gid);

					if (existingList.isEmpty()) {
						++entryid;

						// save germplasm's preferred name as designation
						designation = entryCode;

						GermplasmListData germplasmListData = new GermplasmListData(null, germList, gid, entryid, entryCode, seedSource,
								designation, groupName, status, localRecordId);

						this.germplasmListManager.addGermplasmListData(germplasmListData);

					}
					gidListString = gidListString + ", " + Integer.toString(gid);
				}

			}

			// Save Project Activity
			this.contextUtil.logProgramActivity("Saved a germplasm list.", "Saved list - " + listName + " with type - " + type);

		} catch (MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_ADDING_GERMPLASM_LIST);
		}
	}

	public void closeSavingGermplasmListDialog() {
		Window window = this.getWindow();
		window.getParent().removeWindow(window);
	}

	@Override
	public void setSelectedFolder(GermplasmList folder) {
		try {
			Deque<GermplasmList> parentFolders = new ArrayDeque<GermplasmList>();
			GermplasmListTreeUtil.traverseParentsOfList(this.germplasmListManager, folder, parentFolders);

			StringBuilder locationFolderString = new StringBuilder();
			locationFolderString.append("Lists");

			while (!parentFolders.isEmpty()) {
				locationFolderString.append(" > ");
				GermplasmList parentFolder = parentFolders.pop();
				locationFolderString.append(parentFolder.getName());
			}

			if (folder != null) {
				locationFolderString.append(" > ");
				locationFolderString.append(folder.getName());
			}

			if (folder != null && folder.getName().length() >= 36) {
				this.folderToSaveListTo.setValue(folder.getName().substring(0, 47));
			} else if (locationFolderString.length() > 43) {
				int lengthOfFolderName = folder.getName().length();
				this.folderToSaveListTo
						.setValue(locationFolderString.substring(0, 43 - lengthOfFolderName - 6) + "... > " + folder.getName());
			} else {
				this.folderToSaveListTo.setValue(locationFolderString.toString());
			}

			this.folderToSaveListTo.setDescription(locationFolderString.toString());
			this.folderToSaveListTo.setData(folder);
			this.lastSelectedFolder = folder;
		} catch (MiddlewareQueryException ex) {
			SaveToListDialog.LOG.error("Error with traversing parents of list: " + folder.getId(), ex);
		}
	}

	private void displaySelectFolderDialog() {
		GermplasmList selectedFolder = (GermplasmList) this.folderToSaveListTo.getData();
		SelectLocationFolderDialog selectFolderDialog = null;
		if (selectedFolder != null) {
			selectFolderDialog = new SelectLocationFolderDialog(this, selectedFolder.getId());
		} else {
			selectFolderDialog = new SelectLocationFolderDialog(this, null);
		}
		this.getWindow().getParent().addWindow(selectFolderDialog);
	}

	public void setGermplasmListManager(GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public void setComboboxListName(ComboBox combobox) {
		this.comboBoxListName = combobox;
	}
}
