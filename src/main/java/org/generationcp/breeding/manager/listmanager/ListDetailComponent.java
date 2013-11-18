package org.generationcp.breeding.manager.listmanager;

import java.util.Date;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListButtonClickListener;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ListDetailComponent extends GridLayout implements InitializingBean, InternationalizableComponent {
	
	@SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(ListDetailComponent.class);
    private static final long serialVersionUID = 1738426765643928293L;

    private Label lblName;
    private Label lblDescription;
    private Label lblCreationDate;
    private Label lblType;
    private Label lblStatus;
    private Label lblListOwner;
    
    private Label listName;
    private Label listDescription;
    private Label listCreationDate;
    private Label listType;
    private Label listStatus;
    private Label listOwner;
    
    private Button lockButton;
    private Button unlockButton;
    private Button deleteButton;
    
    public static String LOCK_BUTTON_ID = "Lock Germplasm List";
    public static String UNLOCK_BUTTON_ID = "Unlock Germplasm List";
    public static String DELETE_BUTTON_ID = "Delete Germplasm List";
    private static String LOCK_TOOLTIP = "Click to lock or unlock this germplasm list.";
    
    private GermplasmListManager germplasmListManager;
    private int germplasmListId;

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    @Autowired
    private UserDataManager userDataManager;
    

    public GermplasmList germplasmList;
    public ListManagerTreeMenu listManagerTreeMenu;
    
    private List<UserDefinedField> userDefinedFields;
    
    private boolean usedForDetailsOnly;
    
    private static final ThemeResource ICON_LOCK = new ThemeResource("images/lock.png");
    private static final ThemeResource ICON_UNLOCK = new ThemeResource("images/unlock.png");
    
    public ListDetailComponent(GermplasmListManager germplasmListManager, int germplasmListId, boolean usedForDetailsOnly){
        this.germplasmListManager = germplasmListManager;
        this.germplasmListId = germplasmListId;
        this.usedForDetailsOnly = usedForDetailsOnly;
    }
    
    public ListDetailComponent(ListManagerTreeMenu listManagerTreeMenu, GermplasmListManager germplasmListManager, int germplasmListId
            , boolean usedForDetailsOnly){
        this.listManagerTreeMenu = listManagerTreeMenu;
        this.germplasmListManager = germplasmListManager;
        this.germplasmListId = germplasmListId;
        this.usedForDetailsOnly = usedForDetailsOnly;
    }
    
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void afterPropertiesSet() throws Exception {
	
		setRows(3);
        setColumns(8);
        setColumnExpandRatio(1, 2);
        setColumnExpandRatio(4, 2);
        setColumnExpandRatio(7, 2);
        setSpacing(true);
        setMargin(true);
        
        userDefinedFields = germplasmListManager.getGermplasmListTypes();
        
        // get GermplasmList Detail
        germplasmList = germplasmListManager.getGermplasmListById(germplasmListId);
        
        lblName = new Label( "<b>" + messageSource.getMessage(Message.NAME_LABEL) + ":</b> ", Label.CONTENT_XHTML); // "Name"
        lblDescription = new Label("<b>" + messageSource.getMessage(Message.DESCRIPTION_LABEL) + ":</b> ", Label.CONTENT_XHTML ); // "Description"
        lblCreationDate = new Label("<b>" + messageSource.getMessage(Message.CREATION_DATE_LABEL) + ":</b> ", Label.CONTENT_XHTML); // "Creation Date"
        lblType = new Label("<b>" + messageSource.getMessage(Message.TYPE_LABEL) + ":</b> ", Label.CONTENT_XHTML); // "Type"
        lblStatus = new Label("<b>" + messageSource.getMessage(Message.STATUS_LABEL) + ":</b> ", Label.CONTENT_XHTML); // "Status"
        lblListOwner = new Label("<b>" + messageSource.getMessage(Message.LIST_OWNER_LABEL) + ":</b> ", Label.CONTENT_XHTML); // "List Owner"
        
        listName = new Label(germplasmList.getName());
        listDescription = new Label(germplasmList.getDescription());
        listCreationDate = new Label(String.valueOf(germplasmList.getDate()));
        listType = new Label(getFullListTypeName(germplasmList.getType()));
        listStatus = new Label(germplasmList.getStatusString());
        listOwner= new Label(getOwnerListName(germplasmList.getUserId()));
        
        addComponent(lblName, 0, 0);
        addComponent(listName, 1, 0);
        addComponent(lblDescription, 3, 0);
        addComponent(listDescription, 4, 0);
        addComponent(lblType, 6, 0);
        addComponent(listType, 7, 0);
        
        addComponent(lblCreationDate, 0, 1);
        addComponent(listCreationDate, 1, 1);
        addComponent(lblListOwner, 3, 1);
        addComponent(listOwner, 4, 1);
        addComponent(lblStatus, 6, 1);
        
        Long projectId = (long) workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue();
        workbenchDataManager.getWorkbenchRuntimeData();
        Integer workbenchUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
        Integer IBDBUserId = workbenchDataManager.getLocalIbdbUserId(workbenchUserId, projectId);
        
      //if(germplasmList.getUserId().equals(workbenchDataManager.getWorkbenchRuntimeData().getUserId()) && germplasmList.getId()<0){
        if(!usedForDetailsOnly){
            if(germplasmList.getUserId().equals(IBDBUserId) && germplasmList.getId()<0){
                if(germplasmList.getStatus()>=100){
                    unlockButton = new Button("Click to Open List");
                    unlockButton.setData(UNLOCK_BUTTON_ID);
                    unlockButton.setIcon(ICON_LOCK);
                    unlockButton.setWidth("200px");
                    unlockButton.setDescription(LOCK_TOOLTIP);
                    unlockButton.setStyleName(Reindeer.BUTTON_LINK);
                    unlockButton.addListener(new GermplasmListButtonClickListener(this, germplasmList));
                    addComponent(unlockButton, 7, 1);
                } else if(germplasmList.getStatus()==1) {
                    lockButton = new Button("Click to Lock List");
                    lockButton.setData(LOCK_BUTTON_ID);
                    lockButton.setIcon(ICON_UNLOCK);
                    lockButton.setWidth("200px");
                    lockButton.setDescription(LOCK_TOOLTIP);
                    lockButton.setStyleName(Reindeer.BUTTON_LINK);
                    lockButton.addListener(new GermplasmListButtonClickListener(this, germplasmList));
                    addComponent(lockButton, 7, 1);
                    
                    deleteButton = new Button("Delete");
                    deleteButton.setData(DELETE_BUTTON_ID);
                    deleteButton.setWidth("80px");
                    deleteButton.addListener(new GermplasmListButtonClickListener(this, germplasmList));
                   
                    addComponent(deleteButton, 0, 2);
                }
            }
            else{
            	addComponent(listStatus, 7, 1);
            }
        }
        
	}
	
	public String getFullListTypeName(String fcode){
		String listType = "";
		
		for(UserDefinedField udf : userDefinedFields){
			if(udf.getFcode().equals(fcode)){
				listType = udf.getFname();
				break;
			}
		}
		
		return listType;
	}
	
    private String getOwnerListName(Integer userId) throws MiddlewareQueryException {
        User user=userDataManager.getUserById(userId);
        if(user != null){
            int personId=user.getPersonid();
            Person p =userDataManager.getPersonById(personId);
    
            if(p!=null){
                return p.getFirstName()+" "+p.getMiddleName() + " "+p.getLastName();
            }else{
                return user.getName();
            }
        } else {
            return "";
        }
    }
	
	@Override
	public void attach() {
	    super.attach();
	    updateLabels();
	}

	public void lockGermplasmList() {
		if(germplasmList.getStatus()<100){
            germplasmList.setStatus(germplasmList.getStatus()+100);
            try {
                germplasmListManager.updateGermplasmList(germplasmList);

                User user = (User) workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());
                ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()), 
                        workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()), 
                        "Locked a germplasm list.", 
                        "Locked list "+germplasmList.getId()+" - "+germplasmList.getName(),
                        user,
                        new Date());
                workbenchDataManager.addProjectActivity(projAct);
                
                Tab tab = Util.getTabAlreadyExist(listManagerTreeMenu.getListManagerTreeComponent().getTabSheetGermplasmList(), germplasmList.getName());
                listManagerTreeMenu.getListManagerTreeComponent().getTabSheetGermplasmList().removeTab(tab);
                
                listManagerTreeMenu.getListManagerTreeComponent().createGermplasmListInfoTab(germplasmListId);
                tab = Util.getTabAlreadyExist(listManagerTreeMenu.getListManagerTreeComponent().getTabSheetGermplasmList(), germplasmList.getName());
                listManagerTreeMenu.getListManagerTreeComponent().getTabSheetGermplasmList().setSelectedTab(tab.getComponent());
                
                //getWindow().getWindow().showNotification("Germplasm List", "Successfully Locked", Notification.TYPE_WARNING_MESSAGE);
            } catch (MiddlewareQueryException e) {
                e.printStackTrace();
            }
        }
        lockButton.detach();

        deleteButton.setEnabled(false);      
	}

	public void unlockGermplasmList() {
		if(germplasmList.getStatus()>=100){
            germplasmList.setStatus(germplasmList.getStatus()-100);
            try {
                germplasmListManager.updateGermplasmList(germplasmList);

                Tab tab = Util.getTabAlreadyExist(listManagerTreeMenu.getListManagerTreeComponent().getTabSheetGermplasmList(), germplasmList.getName());
                listManagerTreeMenu.getListManagerTreeComponent().getTabSheetGermplasmList().removeTab(tab);
                
                listManagerTreeMenu.getListManagerTreeComponent().createGermplasmListInfoTab(germplasmListId);
                tab = Util.getTabAlreadyExist(listManagerTreeMenu.getListManagerTreeComponent().getTabSheetGermplasmList(), germplasmList.getName());
                listManagerTreeMenu.getListManagerTreeComponent().getTabSheetGermplasmList().setSelectedTab(tab.getComponent());
                
                User user = (User) workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());
                ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()), 
                        workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()), 
                        "Unlocked a germplasm list.", 
                        "Unlocked list "+germplasmList.getId()+" - "+germplasmList.getName(),
                        user,
                        new Date());
                workbenchDataManager.addProjectActivity(projAct);
            } catch (MiddlewareQueryException e) {
                e.printStackTrace();
            }
        }
	}

	public void deleteGermplasmList() {
		ConfirmDialog.show(this.getWindow(), "Delete Germplasm List:", "Are you sure that you want to delete this list?", "Yes", "No", new ConfirmDialog.Listener() {
            private static final long serialVersionUID = 1L;

		    public void onClose(ConfirmDialog dialog) {
		        if (dialog.isConfirmed()) {
		            deleteGermplasmListConfirmed();
		        }
		    }
		});
	}
	
	public void deleteGermplasmListConfirmed() {
        if(germplasmList.getStatus()<100){ 
            try {
                germplasmListManager.deleteGermplasmList(germplasmList);
                
                User user = (User) workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());
                ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()), 
                        workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()), 
                        "Deleted a germplasm list.", 
                        "Deleted germplasm list with id = "+germplasmList.getId()+" and name = "+germplasmList.getName()+".",
                        user,
                        new Date());
                workbenchDataManager.addProjectActivity(projAct);
                lockButton.setEnabled(false);
                deleteButton.setEnabled(false);
                getWindow().showNotification("Germplasm List", "Successfully deleted", Notification.TYPE_WARNING_MESSAGE);
                //Close confirmation window
                
                //Re-use refresh action on GermplasmListTreeComponent
                listManagerTreeMenu.getListManagerTreeComponent().createTree();
                
                //Close tab
                Tab tab = Util.getTabAlreadyExist(listManagerTreeMenu.getListManagerTreeComponent().getTabSheetGermplasmList(), germplasmList.getName());
                listManagerTreeMenu.getListManagerTreeComponent().getTabSheetGermplasmList().removeTab(tab);
                
                
                
            } catch (MiddlewareQueryException e) {
                getWindow().showNotification("Error", "There was a problem deleting the germplasm list", Notification.TYPE_ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }     
}
