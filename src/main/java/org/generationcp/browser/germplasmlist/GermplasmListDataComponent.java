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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.generationcp.browser.application.GermplasmStudyBrowserApplication;
import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasmlist.listeners.GermplasmListButtonClickListener;
import org.generationcp.browser.germplasmlist.util.GermplasmListExporter;
import org.generationcp.browser.germplasmlist.util.GermplasmListExporterException;
import org.generationcp.browser.study.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class GermplasmListDataComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListDataComponent.class);
	private static final long serialVersionUID = -6487623269938610915L;

	private static final String GID = "gid";
	private static final String ENTRY_ID = "entryId";
	private static final String ENTRY_CODE = "entryCode";
	private static final String SEED_SOURCE = "seedSource";
	private static final String DESIGNATION = "designation";
	private static final String GROUP_NAME = "groupName";
	private static final String STATUS = "status";

	public final static String SORTING_BUTTON_ID = "GermplasmListDataComponent Save Sorting Button";
	public static final String  DELETE_LIST_ENTRIES_BUTTON_ID="Delete list entries";
	public final static String EXPORT_BUTTON_ID = "GermplasmListDataComponent Export List Button";

	private Table listDataTable;
	private Button selectAllButton;
	private Button saveSortingButton;
	private Button exportListButton;

	private int germplasmListId;
	private String listName;
	private List<GermplasmListData> listDatas;
	private Button deleteListEntriesButton;
	private String listEntriesDeleted="";
	private int userId;

	static final Action ACTION_DELETE = new Action("Delete selected entries");
	static final Action[] ACTIONS_TABLE_CONTEXT_MENU = new Action[] { ACTION_DELETE };

	private boolean fromUrl;    //this is true if this component is created by accessing the Germplasm List Details page directly from the URL

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private GermplasmListManager germplasmListManager;

	public GermplasmListDataComponent(int germplasmListId,String listName,int userId, boolean fromUrl){
		this.germplasmListId = germplasmListId;
		this.fromUrl = fromUrl;
		this.listName=listName;
		this.userId=userId;
	}

	@Override
	public void afterPropertiesSet() throws Exception{
		listDatas = new ArrayList<GermplasmListData>();
		long listDataCount = this.germplasmListManager.countGermplasmListDataByListId(germplasmListId);

		if (listDataCount == 0) {
			addComponent(new Label(messageSource.getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL))); // "No Germplasm List Data retrieved."
		} else {

			// create the Vaadin Table to display the Germplasm List Data
			listDataTable = new Table("");

			listDataTable.setSelectable(true);
			listDataTable.setMultiSelect(true);
			listDataTable.setColumnCollapsingAllowed(true);
			listDataTable.setColumnReorderingAllowed(true);
			listDataTable.setPageLength(15); // number of rows to display in the Table
			listDataTable.setSizeFull(); // to make scrollbars appear on the Table component
			
			listDataTable.addActionHandler(new Action.Handler() {
				public Action[] getActions(Object target, Object sender) {
						return ACTIONS_TABLE_CONTEXT_MENU;
				}

				public void handleAction(Action action, Object sender, Object target) {
					if (ACTION_DELETE == action) {
						deleteListButtonClickAction();
					}
				}
			});

			//make GID as link only if the page wasn't directly accessed from the URL
			if (!fromUrl) {
				listDataTable.addContainerProperty(GID, Button.class, null);
			} else {
				listDataTable.addContainerProperty(GID, Integer.class, null);
			}

			listDataTable.addContainerProperty(ENTRY_ID, Integer.class, null);
			listDataTable.addContainerProperty(ENTRY_CODE, String.class, null);
			listDataTable.addContainerProperty(SEED_SOURCE, String.class, null);
			listDataTable.addContainerProperty(DESIGNATION, String.class, null);
			listDataTable.addContainerProperty(GROUP_NAME, String.class, null);
			listDataTable.addContainerProperty(STATUS, String.class, null);

			messageSource.setColumnHeader(listDataTable, GID, Message.LISTDATA_GID_HEADER);
			messageSource.setColumnHeader(listDataTable, ENTRY_ID, Message.LISTDATA_ENTRY_ID_HEADER);
			messageSource.setColumnHeader(listDataTable, ENTRY_CODE, Message.LISTDATA_ENTRY_CODE_HEADER);
			messageSource.setColumnHeader(listDataTable, SEED_SOURCE, Message.LISTDATA_SEEDSOURCE_HEADER);
			messageSource.setColumnHeader(listDataTable, DESIGNATION, Message.LISTDATA_DESIGNATION_HEADER);
			messageSource.setColumnHeader(listDataTable, GROUP_NAME, Message.LISTDATA_GROUPNAME_HEADER);
			messageSource.setColumnHeader(listDataTable, STATUS, Message.LISTDATA_STATUS_HEADER);

			populateTable();

			setSpacing(true);
			addComponent(listDataTable);

			HorizontalLayout buttonArea = new HorizontalLayout();
			buttonArea.setSpacing(true);

			selectAllButton = new Button("Select All",new Button.ClickListener() {
				public void buttonClick(Button.ClickEvent event) {
					listDataTable.setValue(listDataTable.getItemIds());
				}
			});
			buttonArea.addComponent(selectAllButton);

			exportListButton = new Button("Export List", new GermplasmListButtonClickListener(this));
			exportListButton.setData(EXPORT_BUTTON_ID);
			buttonArea.addComponent(exportListButton);

			// Show "Save Sorting" button only when Germplasm List open is a local IBDB record (negative ID).
			// and when not accessed directly from URL or popup window
			if (germplasmListId < 0
					&& !fromUrl) {
				saveSortingButton = new Button("Save Sorting", new GermplasmListButtonClickListener(this));
				saveSortingButton.setData(SORTING_BUTTON_ID);
				buttonArea.addComponent(saveSortingButton);

				deleteListEntriesButton = new Button("Delete List Entries", new GermplasmListButtonClickListener(this));
				deleteListEntriesButton.setData(DELETE_LIST_ENTRIES_BUTTON_ID);
				deleteListEntriesButton.setDescription("Delete list entries");
				buttonArea.addComponent(deleteListEntriesButton);
			}

			addComponent(buttonArea);



		}
	}

	private void populateTable() throws MiddlewareQueryException {
		long listDataCount = this.germplasmListManager.countGermplasmListDataByListId(germplasmListId);
		listDatas = this.germplasmListManager.getGermplasmListDataByListId(germplasmListId, 0, (int) listDataCount);
		for (GermplasmListData data : listDatas) {
			Object gidObject;

			if (!fromUrl) {
				// make GID as link only if the page wasn't directly accessed from the URL
				String gid = String.format("%s", data.getGid().toString());
				Button gidButton = new Button(gid, new GidLinkButtonClickListener(gid));
				gidButton.setStyleName(BaseTheme.BUTTON_LINK);
				gidButton.setDescription("Click to view Germplasm information");
				gidObject = gidButton;
				//item.addItemProperty(columnId, new ObjectProperty<Button>(gidButton));
			} else {
				gidObject = data.getGid();
			}

			listDataTable.addItem(new Object[] {
					gidObject, data.getEntryId(), data.getEntryCode(), data.getSeedSource(),
					data.getDesignation(), data.getGroupName(), data.getStatusString()
			}, data.getId());
		}

		listDataTable.sort(new Object[]{"entryId"}, new boolean[]{true});
	}



	@Override
	public void attach() {
		super.attach();
		updateLabels();
	}

	@Override
	public void updateLabels() {
	}

	// called by GermplasmListButtonClickListener
	public void saveSortingAction() throws InternationalizableException {
		int entryId = 1;
		//re-assign "Entry ID" field based on table's sorting
		for (Iterator<?> i = listDataTable.getItemIds().iterator(); i.hasNext();) {
			//iterate through the table elements' IDs
			int listDataId = (Integer) i.next();

			//update table item's entryId
			Item item = listDataTable.getItem(listDataId);
			item.getItemProperty(ENTRY_ID).setValue(entryId);

			//then find the corresponding ListData and assign a new entryId to it
			for (GermplasmListData listData : listDatas) {
				if (listData.getId().equals(listDataId)) {
					listData.setEntryId(entryId);
					break;
				}
			}
			entryId += 1;
		}
		//save the list of Germplasm List Data to the database
		try {
			germplasmListManager.updateGermplasmListData(listDatas);
			listDataTable.requestRepaint();
			MessageNotifier.showMessage(this.getWindow(), 
					messageSource.getMessage(Message.SUCCESS), 
					messageSource.getMessage(Message.SAVE_GERMPLASMLIST_DATA_SORTING_SUCCESS));
		} catch (MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_SAVING_GERMPLASMLIST_DATA_SORTING);
		}

	}

	//called by GermplasmListButtonClickListener
	public void exportListAction() throws InternationalizableException {

		String tempFileName = System.getProperty( "user.home" ) + "/temp.xls";

		GermplasmListExporter listExporter = new GermplasmListExporter(germplasmListManager, germplasmListId);

		try {
			listExporter.exportGermplasmListExcel(tempFileName);
			FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), this.getApplication());
			fileDownloadResource.setFilename("export.xls");

			Window downloadWindow = new Window();
			downloadWindow.setWidth(0);
			downloadWindow.setHeight(0);
			downloadWindow.open(fileDownloadResource);
			this.getWindow().addWindow(downloadWindow);

			//            File tempFile = new File(tempFileName);
			//            tempFile.delete();
		} catch (GermplasmListExporterException e) {
			MessageNotifier.showError(this.getApplication().getWindow(GermplasmStudyBrowserApplication.GERMPLASMLIST_WINDOW_NAME), e.getMessage(), "");
		} 
	}

	public void deleteListButtonClickAction()  throws InternationalizableException {

		ConfirmDialog.show(this.getWindow(), "Delete List Entries:", "Are you want to delete the list entries?",
				"Ok", "Cancel", new ConfirmDialog.Listener() {

			public void onClose(ConfirmDialog dialog) {
				if (dialog.isConfirmed()) {
					// Confirmed to continue
					try {
						if(workbenchDataManager.getWorkbenchRuntimeData().getUserId() == userId) {
							Collection<?> selectedIds = (Collection<?>)listDataTable.getValue();
							listEntriesDeleted="";
							for (final Object itemId : selectedIds) {
								Property p = listDataTable.getItem(itemId).getItemProperty(ENTRY_ID);
								try {
									int entryId=Integer.valueOf(p.getValue().toString());
									listEntriesDeleted+=String.valueOf(entryId)+",";
									germplasmListManager.deleteGermplasmListDataByListIdEntryId(germplasmListId,entryId);
								} catch (MiddlewareQueryException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							listEntriesDeleted=listEntriesDeleted.substring(0,listEntriesDeleted.length()-1);
							listDataTable.removeAllItems();
							try {
								populateTable();
								logDeletedListEntriesToWorkbenchProjectActivity();
							} catch (MiddlewareQueryException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							showMessageInvalidDeletingListEntries();
						}
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MiddlewareQueryException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					// User did not confirm
				}
			}

		});
	}

	private void logDeletedListEntriesToWorkbenchProjectActivity() throws MiddlewareQueryException {
		GermplasmStudyBrowserApplication app = GermplasmStudyBrowserApplication.get();

		User user = (User) workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());

		ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()), 
				workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()), 
				"Deleted list entries.", 
				"Deleted the following list entries " + listEntriesDeleted + " from the list id " + germplasmListId + " - " + listName,user,new Date());
		try {
			workbenchDataManager.addProjectActivity(projAct);
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
	}

	private void showMessageInvalidDeletingListEntries(){
		MessageNotifier.showMessage(this.getWindow(), 
				messageSource.getMessage(Message.INVALID_DELETING_LIST_ENTRIES), 
				messageSource.getMessage(Message.INVALID_USER_DELETING_LIST_ENTRIES) + " "+listName);
	}



}
