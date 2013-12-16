package org.generationcp.browser.cross.study.adapted.dialogs;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.browser.application.GermplasmStudyBrowserApplication;
import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.adapted.main.QueryForAdaptedGermplasmMain;
import org.generationcp.browser.germplasm.listeners.GermplasmButtonClickListener;
import org.generationcp.browser.germplasmlist.listeners.CloseWindowAction;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.Project;
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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Window.Notification;

@Configurable
public class SaveToListDialog extends Window implements InitializingBean, InternationalizableComponent,
		Property.ValueChangeListener, AbstractSelect.NewItemHandler {
	
    private static final Logger LOG = LoggerFactory.getLogger(SaveToListDialog.class);
    private static final long serialVersionUID = 1L;
    public static final Object SAVE_BUTTON_ID = "Save Germplasm List";
    public static final String CANCEL_BUTTON_ID = "Cancel Saving";
    
    private Label labelListName;
    private Label labelDescription;
    private TextField txtDescription;
    private Label labelType;
    private TextField txtName;
    
	private Component source;
	private Window parentWindow;
	private Map<Integer, String> germplasmsMap;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    private Button btnSave;
    private Button btnCancel;
    private ComboBox comboBoxListName;
    private Select selectType;
    private List<GermplasmList> germplasmList;
    private boolean lastAdded = false;
    private boolean existingListSelected = false;
    private Map<String, Integer> mapExistingList;
    
    private QueryForAdaptedGermplasmMain mainScreen;
	
	public SaveToListDialog(QueryForAdaptedGermplasmMain mainScreen, Component source, Window parentWindow, Map<Integer,String> germplasmsMap){
		this.mainScreen = mainScreen;
        this.source = source;
        this.parentWindow = parentWindow;
        this.germplasmsMap = germplasmsMap;
    }
	
	
	@Override
	public void updateLabels() {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
        //set as modal window, other components are disabled while window is open
        setModal(true);
        // define window size, set as not resizable
        setWidth("700px");
        setHeight("350px");
        setResizable(false);
        setCaption(messageSource.getMessage(Message.SAVE_GERMPLASM_LIST_WINDOW_LABEL));
        // center window within the browser
        center();
        
        GridLayout mainLayout = new GridLayout();
        mainLayout.setRows(8);
        mainLayout.setColumns(3);
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        
        labelListName = new Label(messageSource.getMessage(Message.LIST_NAME_LABEL));
        labelDescription = new Label(messageSource.getMessage(Message.DESCRIPTION_LABEL));
        labelType = new Label(messageSource.getMessage(Message.TYPE_LABEL));

        comboBoxListName = new ComboBox();
        populateComboBoxListName();
        comboBoxListName.setNewItemsAllowed(true);
        //comboBoxListName.setNewItemHandler(this);
        comboBoxListName.setNullSelectionAllowed(false);
        //comboBoxListName.addListener(this);
        comboBoxListName.setImmediate(true);

        txtDescription = new TextField();
        txtDescription.setWidth("400px");

        txtName = new TextField();
        txtName.setWidth("200px");

        selectType = new Select();
        populateSelectType(selectType);
        selectType.setNullSelectionAllowed(false);
        selectType.select("LST");

        HorizontalLayout hButton = new HorizontalLayout();
        hButton.setSpacing(true);
        btnSave = new Button(messageSource.getMessage(Message.SAVE_LABEL));
        btnSave.setWidth("80px");
        btnSave.setData(SAVE_BUTTON_ID);
        btnSave.setDescription("Save Germplasm List ");
        btnSave.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        btnSave.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				saveButtonClickAction();
			}
       });

        hButton.addComponent(btnSave);
        btnCancel = new Button(messageSource.getMessage(Message.CANCEL_LABEL));
        btnCancel.setWidth("80px");
        btnCancel.setData(CANCEL_BUTTON_ID);
        btnCancel.setDescription("Cancel Saving Germplasm List");
        btnCancel.addListener(new CloseWindowAction());
        hButton.addComponent(btnCancel);

        mainLayout.addComponent(labelListName, 1, 1);
        mainLayout.addComponent(comboBoxListName, 2, 1);
        mainLayout.addComponent(labelDescription, 1, 2);
        mainLayout.addComponent(txtDescription, 2, 2);
        mainLayout.addComponent(labelType, 1, 3);
        mainLayout.addComponent(selectType, 2, 3);
        mainLayout.addComponent(hButton, 1, 6);
        
        addComponent(mainLayout);			
	}
	
    private void populateComboBoxListName() throws MiddlewareQueryException {
        germplasmList = germplasmListManager.getAllGermplasmLists(0, (int) germplasmListManager.countAllGermplasmLists(), Database.LOCAL);
        mapExistingList = new HashMap<String, Integer>();
        comboBoxListName.addItem("");
        for (GermplasmList gList : germplasmList) {
            comboBoxListName.addItem(gList.getName());
            mapExistingList.put(gList.getName(), new Integer(gList.getId()));
        }
        comboBoxListName.select("");
    }

    private void populateSelectType(Select selectType) throws MiddlewareQueryException {
        List<UserDefinedField> listTypes = this.germplasmListManager.getGermplasmListTypes();
        
        for (UserDefinedField listType : listTypes) {
            String typeCode = listType.getFcode();
            selectType.addItem(typeCode);
            selectType.setItemCaption(typeCode, listType.getFname());
            //set "GERMPLASMLISTS" as the default value
            if ("LST".equals(typeCode)) {
                selectType.setValue(typeCode);
            }
        }
    }


	@Override
	public void addNewItem(String newItemCaption) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void valueChange(ValueChangeEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	public void saveButtonClickAction(){
		String listName = comboBoxListName.getValue().toString();
		String listNameId = String.valueOf(mapExistingList.get(comboBoxListName.getValue()));	
			    
		Boolean proceedWithSave = true;
		
		try {
			//Long matchingNamesCountOnLocal = germplasmListManager.countGermplasmListByName(listName, Operation.EQUAL, Database.LOCAL);
			Long matchingNamesCountOnCentral = germplasmListManager.countGermplasmListByName(listName, Operation.EQUAL, Database.CENTRAL);
			if(matchingNamesCountOnCentral>0){
				getWindow().showNotification("There is already an existing germplasm list with that name","",Notification.TYPE_ERROR_MESSAGE);
				proceedWithSave = false;
			}
			
		} catch (MiddlewareQueryException e) {
			
		}

		if(proceedWithSave){
		
			if (listName.trim().length() == 0) {
	
	            getWindow().showNotification("List Name Input Error...", "Please specify a List Name before saving",
	                    Notification.TYPE_WARNING_MESSAGE);
	
	        } else if (listName.trim().length() > 50) {
	
	            getWindow().showNotification("List Name Input Error...", "Listname input is too large limit the name only up to 50 characters",
	                    Notification.TYPE_WARNING_MESSAGE);
	            comboBoxListName.setValue("");
	
	        } else {
	        	
	        	addGermplasListNameAndData(listName, listNameId, this.germplasmsMap, txtDescription.getValue().toString(), selectType.getValue().toString());
	        	closeSavingGermplasmListDialog();
	            
	        	this.mainScreen.selectWelcomeTab();
	        	
	            // display notification message
	            MessageNotifier.showMessage(this.parentWindow, messageSource.getMessage(Message.SAVE_GERMPLASMS_TO_NEW_LIST_LABEL),
	                    messageSource.getMessage(Message.SAVE_GERMPLASMS_TO_NEW_LIST_SUCCESS));
	        }
			
		}
	}
	
	private void addGermplasListNameAndData(String listName, String listId,
			Map<Integer, String> germplasmsMap, String description, String type) {
		
		try {
            // SaveGermplasmListAction saveGermplasmAction = new
            // SaveGermplasmListAction();
            Date date = new Date();
            Format formatter = new SimpleDateFormat("yyyyMMdd");
            Long currentDate = Long.valueOf(formatter.format(date));
            Integer userId = getCurrentUserLocalId();
            GermplasmList parent = null;
            int statusListName = 1;
            String GIDListString = "";

            if ("null".equals(listId)) {
                GermplasmList listNameData = new GermplasmList(null, listName,
                        currentDate, type, userId, description, parent,
                        statusListName);

                int listid = germplasmListManager
                        .addGermplasmList(listNameData);

                GermplasmList germList = germplasmListManager
                        .getGermplasmListById(listid);

                String groupName = "-";
                String designation = "-";
                int status = 0;
                int localRecordId = 0;
                int entryid = 1;

                for (Map.Entry<Integer, String> entry : germplasmsMap.entrySet()) {
                    
                    Integer gid = entry.getKey(); 
                    designation = (entry.getValue() == null)? "-" : entry.getValue();
                    
                    String entryCode = designation;
                    String seedSource= "Browse for "+ designation;
                    
                    GermplasmListData germplasmListData = new GermplasmListData(
                            null, germList, gid, entryid, entryCode,
                            seedSource, designation, groupName, status,
                            localRecordId);

                    germplasmListManager.addGermplasmListData(germplasmListData);
                    
                    entryid++;
                    
                    GIDListString = GIDListString + ", " + Integer.toString(gid);

                }
                
            } else {

                GermplasmList germList = germplasmListManager
                        .getGermplasmListById(Integer.valueOf(listId));
                String groupName = "-";
                String designation = "-";
                int status = 0;
                int localRecordId = 0;
                int entryid = (int) germplasmListManager
                        .countGermplasmListDataByListId(Integer.valueOf(listId));

                for (Map.Entry<Integer, String> entry : germplasmsMap.entrySet()) {
                    Integer gid = entry.getKey();

                    String entryCode = (entry.getValue() == null)? "-" : entry.getValue();
                    
                    String seedSource="Browse for " + entryCode;
                    
                    // check if there is existing gid in the list
                    List<GermplasmListData> germplasmList = germplasmListManager
                            .getGermplasmListDataByListIdAndGID(
                                    Integer.valueOf(listId), gid);

                    if (germplasmList.size() < 1) {
                        ++entryid;
                        
                        // save germplasm's preferred name as designation
                        designation = entryCode;
                        
                        GermplasmListData germplasmListData = new GermplasmListData(
                                null, germList, gid, entryid, entryCode,
                                seedSource, designation, groupName, status,
                                localRecordId);

                        germplasmListManager
                                .addGermplasmListData(germplasmListData);

                    }
                    GIDListString = GIDListString + ", "
                            + Integer.toString(gid);
                }

            }

            // Save Project Activity
            GermplasmStudyBrowserApplication app = GermplasmStudyBrowserApplication
                    .get();
            GIDListString = GIDListString.substring(2); // remove ", ";

            User user = (User) workbenchDataManager
                    .getUserById(workbenchDataManager.getWorkbenchRuntimeData()
                            .getUserId());

            ProjectActivity projAct = new ProjectActivity(new Integer(
                    workbenchDataManager
                            .getLastOpenedProject(
                                    workbenchDataManager
                                            .getWorkbenchRuntimeData()
                                            .getUserId()).getProjectId()
                            .intValue()),
                    workbenchDataManager
                            .getLastOpenedProject(workbenchDataManager
                                    .getWorkbenchRuntimeData().getUserId()),
                    "Saved a germplasm list.", "Saved list - " + listName
                            + " with type - " + type, user, new Date());

            try {
                workbenchDataManager.addProjectActivity(projAct);
            } catch (MiddlewareQueryException e) {
                e.printStackTrace();
            }

        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE,
                    Message.ERROR_IN_ADDING_GERMPLASM_LIST);
        }
	}


	public void closeSavingGermplasmListDialog(){
        Window window = this.getWindow();
        window.getParent().removeWindow(window);
	}
	
    private Integer getCurrentUserLocalId() throws MiddlewareQueryException {
        Integer workbenchUserId = this.workbenchDataManager
                .getWorkbenchRuntimeData().getUserId();
        Project lastProject = this.workbenchDataManager
                .getLastOpenedProject(workbenchUserId);
        Integer localIbdbUserId = this.workbenchDataManager.getLocalIbdbUserId(workbenchUserId,
                lastProject.getProjectId());
        if (localIbdbUserId != null) {
            return localIbdbUserId;
        } else {
            return 1; // TODO: verify actual default value if no workbench_ibdb_user_map was found
        }
    }

}
