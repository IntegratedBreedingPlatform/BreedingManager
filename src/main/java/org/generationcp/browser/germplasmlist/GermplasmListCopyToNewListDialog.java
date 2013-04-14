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

package org.generationcp.browser.germplasmlist;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.generationcp.browser.application.GermplasmStudyBrowserApplication;
import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasmlist.listeners.GermplasmListButtonClickListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;



@Configurable
public class GermplasmListCopyToNewListDialog extends GridLayout implements InitializingBean, InternationalizableComponent,
Property.ValueChangeListener, AbstractSelect.NewItemHandler{

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListCopyToNewListDialog.class);
	private static final long serialVersionUID = 1L;
	public static final Object SAVE_BUTTON_ID = "Save New List Entries";
	public static final String CANCEL_BUTTON_ID = "Cancel Copying New List Entries";
	private static final String ENTRY_ID = "entryId";
	private static final String GID_VALUE = "gidValue";
	private static final String ENTRY_CODE = "entryCode";
	private static final String DESIGNATION = "designation";
	private static final String GROUP_NAME = "groupName";
	private Label labelListName;
	private Label labelDescription;
	private TextField txtListName;
	private TextField txtDescription;
	private Label labelType;
	private TextField txtType;
	private Window dialogWindow;
	private Window mainWindow;
	private Button btnSave;
	private Button btnCancel;
	private Select selectType;
	private Table listEntriesTable;
	private String listName;
	private String designationOfListEntriesCopied;
	private int newListid;
	private String listNameValue;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	public GermplasmListCopyToNewListDialog(Window mainWindow, Window dialogWindow,String listName, Table listEntriesTable) {
		this.dialogWindow = dialogWindow;
		this.mainWindow = mainWindow;
		this.listEntriesTable=listEntriesTable;
		this.listName=listName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		setRows(8);
		setColumns(3);
		setSpacing(true);
		setMargin(true);

		labelListName = new Label();
		labelDescription = new Label();
		labelType = new Label();

		txtListName = new TextField();
		txtListName.setWidth("400px");

		txtDescription = new TextField();
		txtDescription.setWidth("400px");

		txtType = new TextField();
		txtType.setWidth("200px");

		selectType = new Select();
		populateSelectType(selectType);
		selectType.setNullSelectionAllowed(false);
		selectType.select("LST");

		HorizontalLayout hButton = new HorizontalLayout();
		hButton.setSpacing(true);
		btnSave = new Button();
		btnSave.setWidth("80px");
		btnSave.setData(SAVE_BUTTON_ID);
		btnSave.setDescription("Save New Germplasm List ");
		btnSave.addListener(new GermplasmListButtonClickListener(this));

		hButton.addComponent(btnSave);
		btnCancel = new Button();
		btnCancel.setWidth("80px");
		btnCancel.setData(CANCEL_BUTTON_ID);
		btnCancel.setDescription("Cancel Saving New Germplasm List");
		btnCancel.addListener(new GermplasmListButtonClickListener(this));
		hButton.addComponent(btnCancel);

		addComponent(labelListName, 1, 1);
		addComponent(txtListName, 2, 1);
		addComponent(labelDescription, 1, 2);
		addComponent(txtDescription, 2, 2);
		addComponent(labelType, 1, 3);
		addComponent(selectType, 2, 3);
		addComponent(hButton, 1, 6);
	}


	private void populateSelectType(Select selectType) {
		selectType.addItem("LST");
		selectType.addItem("HB");
		selectType.addItem("F1");
		selectType.addItem("F2");
		selectType.addItem("PN");
		selectType.addItem("OYT");
		selectType.addItem("RYT");
		selectType.addItem("FOLDER");
		selectType.addItem("EXTACQ");
		selectType.addItem("EXTREQ");
		selectType.addItem("INTREQ");
		selectType.addItem("COLLMIS");
		selectType.addItem("INTACQ");
		selectType.addItem("SI");
		selectType.addItem("SEEDSTCK");
		selectType.addItem("TRNGENC");
	}

	@Override
	public void attach() {
		super.attach();
		updateLabels();
	}

	@Override
	public void updateLabels() {
		messageSource.setCaption(labelListName, Message.LIST_NAME_LABEL);
		messageSource.setCaption(labelDescription, Message.DESCRIPTION_LABEL);
		messageSource.setCaption(labelType, Message.TYPE_LABEL);
		messageSource.setCaption(btnSave, Message.SAVE_LABEL);
		messageSource.setCaption(btnCancel, Message.CANCEL_LABEL);
	}

	public void saveGermplasmListButtonClickAction() throws InternationalizableException {

		listNameValue = txtListName.getValue().toString();
		String description=txtDescription.getValue().toString();


		if (listNameValue.trim().length() == 0) {

			getWindow().showNotification("List Name Input Error...", "Please specify a List Name before saving",
					Notification.TYPE_WARNING_MESSAGE);

		} else if (listNameValue.trim().length() > 50) {

			getWindow().showNotification("List Name Input Error...", "Listname input is too large limit the name only up to 50 characters",
					Notification.TYPE_WARNING_MESSAGE);
			txtListName.setValue("");

		} else {

			Date date = new Date();
			Format formatter = new SimpleDateFormat("yyyyMMdd");
			Long currentDate = Long.valueOf(formatter.format(date));
			int userId = 1;
			GermplasmList parent = null;
			int statusListName = 1;
			GermplasmList listNameData = new GermplasmList(null, listNameValue, currentDate, selectType.getValue().toString(), userId, description, parent, statusListName);

			try {
				newListid = germplasmListManager.addGermplasmList(listNameData);
				try{
					GermplasmList germList = germplasmListManager.getGermplasmListById(newListid);
					int status = 0;
					int localRecordId = 0;
					int entryid = 1;
					designationOfListEntriesCopied="";
					Collection<?> selectedIds = (Collection<?>)listEntriesTable.getValue();
					for (final Object itemId : selectedIds) {
						Property pEntryId = listEntriesTable.getItem(itemId).getItemProperty(ENTRY_ID);
						Property pGid= listEntriesTable.getItem(itemId).getItemProperty(GID_VALUE);
						Property pEntryCode= listEntriesTable.getItem(itemId).getItemProperty(ENTRY_CODE);
						Property pDesignation= listEntriesTable.getItem(itemId).getItemProperty(DESIGNATION);
						Property pGroupName= listEntriesTable.getItem(itemId).getItemProperty(GROUP_NAME);

						String entryIdOfList=String.valueOf(pEntryId.getValue().toString());
						int gid=Integer.valueOf(pGid.getValue().toString());
						String entryCode=String.valueOf((pEntryCode.getValue().toString()));
						String seedSource=listName+"-"+entryCode;
						String designation=String.valueOf((pDesignation.getValue().toString()));
						designationOfListEntriesCopied+=designation+",";
						String groupName=String.valueOf((pGroupName.getValue().toString()));

						GermplasmListData germplasmListData = new GermplasmListData(null, germList, gid, entryid, entryIdOfList, seedSource,
								designation, groupName, status, localRecordId);
						germplasmListManager.addGermplasmListData(germplasmListData);
						entryid++;
					}
					designationOfListEntriesCopied=designationOfListEntriesCopied.substring(0,designationOfListEntriesCopied.length()-1);
					
					MessageNotifier.showMessage(this.getWindow().getParent().getWindow(), 
							messageSource.getMessage(Message.SUCCESS), 
							messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_COPY_TO_NEW_LIST_SUCCESS));

					logCopyToNewListEntriesToWorkbenchProjectActivity();

				}catch(Exception e){
					germplasmListManager.deleteGermplasmListByListId(newListid);
					getWindow().getParent().getWindow().showNotification("Unsuccessful", "Copying of entries to a new list failed",
							Notification.TYPE_WARNING_MESSAGE);
				}
				this.mainWindow.removeWindow(dialogWindow);

			} catch (MiddlewareQueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				MessageNotifier.showMessage(this.getWindow().getParent().getWindow(), 
						messageSource.getMessage(Message.UNSUCCESSFUL), 
						messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_COPY_TO_NEW_LIST_FAILED));
			}
		}
	}

	private void logCopyToNewListEntriesToWorkbenchProjectActivity() throws MiddlewareQueryException {
		GermplasmStudyBrowserApplication app = GermplasmStudyBrowserApplication.get();

		User user = (User) workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());

		ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()), 
				workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()), 
				"Copied entries into a new list.", 
				"Copied entries " +designationOfListEntriesCopied + " to list " +newListid+  " - " + listNameValue,user,new Date());
		try {
			workbenchDataManager.addProjectActivity(projAct);
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
	}


	public void cancelGermplasmListButtonClickAction() {
		this.mainWindow.removeWindow(dialogWindow);
	}


	@Override
	public void addNewItem(String newItemCaption) {
		// TODO Auto-generated method stub

	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		// TODO Auto-generated method stub

	}

	/*
	 * Shows a notification when a selection is made.
	 */

}
