package org.generationcp.breeding.manager.listmanager;

import java.util.Date;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.dialog.AddEditListNotes;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.ListCommonActionsUtil;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
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

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Window;
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
    private Label lblListNotes;

    private Label listStatus;
    private Label listNotes;
    
    private Button lockButton;
    private Button unlockButton;
    private Button deleteButton;
    private Button addEditViewButton;
    
    public static String LOCK_BUTTON_ID = "Lock Germplasm List";
    public static String UNLOCK_BUTTON_ID = "Unlock Germplasm List";
    public static String DELETE_BUTTON_ID = "Delete Germplasm List";
    public static String VIEW_NOTES_BUTTON_ID = "View Notes Germplasm List";
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
    
    private Integer workbenchUserId;
    private Integer iBDBUserId;
    
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
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		addStyleName("overflow_x_auto");
			
		setRows(4);
        setColumns(3);
        setColumnExpandRatio(0, 2);
        setColumnExpandRatio(2, 2);
        setRowExpandRatio(2, 2);
        setRowExpandRatio(3, 2);
        setSpacing(true);
        
        userDefinedFields = germplasmListManager.getGermplasmListTypes();
        
        // get GermplasmList Detail
        germplasmList = germplasmListManager.getGermplasmListById(germplasmListId);
        
        lblName = createCaptionAndValueLbl(Message.NAME_LABEL, germplasmList.getName()); // "Name"
        lblDescription = createCaptionAndValueLbl(Message.DESCRIPTION_LABEL, germplasmList.getDescription());  // "Description"
        lblCreationDate = createCaptionAndValueLbl(Message.CREATION_DATE_LABEL, String.valueOf(germplasmList.getDate()));  // "Creation Date"
        lblType = createCaptionAndValueLbl(Message.TYPE_LABEL, germplasmList.getType()); // "Type"
        lblListOwner = createCaptionAndValueLbl(Message.LIST_OWNER_LABEL, getOwnerListName(germplasmList.getUserId()));  // "List Owner"
        
        listNotes= new Label(getNotes(germplasmList.getNotes()),Label.CONTENT_TEXT);
        
        retrieveUserInfo();
        layoutComponents();
	}

	private void layoutComponents() {
		addComponent(lblName, 0, 0);
        addComponent(lblDescription, 2, 0);
        
        addComponent(lblType, 0, 1);
        renderNotesLabel();
        addComponent(listNotes, 2, 2, 2, 3);
        
        addComponent(lblCreationDate, 0, 2);        
        addComponent(lblListOwner, 0, 3);
	}

	private void retrieveUserInfo() throws MiddlewareQueryException {
		Long projectId = (long) workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue();
        workbenchDataManager.getWorkbenchRuntimeData();
        workbenchUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
        iBDBUserId = workbenchDataManager.getLocalIbdbUserId(workbenchUserId, projectId);
	}

	public Label createCaptionAndValueLbl(Message caption,String value) {
		StringBuffer sb = new StringBuffer();
		sb.append("<b>"); //make the caption bold
		sb.append(messageSource.getMessage(caption));
		sb.append(":</b>");
		sb.append("&nbsp;&nbsp;");
		sb.append(value);
		return new Label(sb.toString(), Label.CONTENT_XHTML);
	}

	/*
	 * Display "Notes" label and Add / Edit Notes button 
	 * (button only for unlocked local lists)
	 */
	private void renderNotesLabel() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setWidth("400px");
		
		HorizontalLayout content = new HorizontalLayout();
		content.setWidth("150px");
		
		lblListNotes = createCaptionAndValueLbl(Message.NOTES, "");  // "Notes"
		lblListNotes.setWidth("50px");
		content.addComponent(lblListNotes);

		boolean isUnlocked = isUnlockedList();
		if(!usedForDetailsOnly){
			boolean hasNotes = germplasmList.getNotes() != null && germplasmList.getNotes().trim().length() > 0;
			boolean isLocal = germplasmList.getId()<0;
			if(hasNotes || isLocal) {
            	addEditViewButton = new Button();
            	addEditViewButton.addStyleName(Reindeer.BUTTON_LINK);
            	addEditViewButton.setWidth("100px");
            	addEditViewButton.setData(VIEW_NOTES_BUTTON_ID);
            		
            	boolean userIsListOwner = userIsListOwner();
				if(!hasNotes && isUnlocked && isLocal && userIsListOwner){
        			addEditViewButton.setCaption(messageSource.getMessage(Message.ADD_NOTES));
        			
        		} else if (hasNotes){
					Message caption = (isUnlocked && isLocal && userIsListOwner) ? Message.EDIT_NOTES : Message.VIEW_NOTES;
					addEditViewButton.setCaption(messageSource.getMessage(caption));
				}
    			addEditViewButton.addListener(new Button.ClickListener(){
    				
    				private static final long serialVersionUID = 1L;
    				
    				@Override
    				public void buttonClick(ClickEvent event) {
    					addEditListNotes(event.getButton().getCaption());
    				}
    				
    			});
    			content.addComponent(addEditViewButton);
    		}
        }
		layout.addComponent(content);
		addComponent(layout, 2, 1);
	}

	private boolean userIsListOwner() {
		return germplasmList.getUserId().equals(iBDBUserId);
	}
	
	/*
	 * Render value for status as lock button / unlock button / label 
	 * depending on list status
	 */
	private void renderStatusField(HorizontalLayout layout) {
		lblStatus = new Label("<b>" + messageSource.getMessage(Message.STATUS_LABEL) + ":</b>&nbsp;&nbsp;", 
				Label.CONTENT_XHTML); 
		layout.addComponent(lblStatus);
		if(!usedForDetailsOnly){
			// local list
            if(userIsListOwner() && germplasmList.getId()<0){
                if(germplasmList.getStatus()>=100){
                    unlockButton = new Button("Click to Open List");
                    unlockButton.setData(UNLOCK_BUTTON_ID);
                    unlockButton.setIcon(ICON_LOCK);
                    unlockButton.setWidth("140px");
                    unlockButton.setDescription(LOCK_TOOLTIP);
                    unlockButton.setStyleName(Reindeer.BUTTON_LINK);
                    unlockButton.addListener(new GermplasmListButtonClickListener(this, germplasmList));
                    layout.addComponent(unlockButton);
                    
                } else if(isUnlockedList()) {
                    lockButton = new Button("Click to Lock List");
                    lockButton.setData(LOCK_BUTTON_ID);
                    lockButton.setIcon(ICON_UNLOCK);
                    lockButton.setWidth("140px");
                    lockButton.setDescription(LOCK_TOOLTIP);
                    lockButton.setStyleName(Reindeer.BUTTON_LINK);
                    lockButton.addListener(new GermplasmListButtonClickListener(this, germplasmList));
                    layout.addComponent(lockButton);
                }
                
            // central lists  or if not list owner  
            } else{
            	String statusString = germplasmList.getStatusString();
				listStatus = new Label(statusString);
				
				//compute length of label
				int baseWidth = 25;
				int finalWidth = baseWidth + (5 * statusString.length());
            	listStatus.setWidth(finalWidth + "px");
 
            	layout.addComponent(listStatus);
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
    
    public String getDescription(String desc){
    	String processedDesc = desc;
    	int endIndex = 30; // TODO Calculate the maximum no of character for 2 lines of text 
    	
    	if(desc != null && desc.length() > endIndex){
    		processedDesc = processedDesc.substring(0, endIndex) + "...";
    	}
    	
    	return processedDesc;
    }
    
    public String getNotes(String notes){
    	String processedNotes = notes;
    	int endIndex = 110; // TODO Calculate the maximum no of character for 2 lines of text 
    	
    	if(notes != null && notes.length() > endIndex){
    		processedNotes = processedNotes.substring(0, endIndex) + "...";
    	}
    	
    	return processedNotes;
    }
	
	@Override
	public void attach() {
	    super.attach();
	    updateLabels();
	}
	
	public void addEditListNotes(String title){
		Window parentWindow = this.getWindow();
		AddEditListNotes addEditListNotes = new AddEditListNotes(this, germplasmListManager, germplasmListId, title);
		addEditListNotes.addStyleName(Reindeer.WINDOW_LIGHT);
		parentWindow.addWindow(addEditListNotes);
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
                
                recreateTab();
                
                //getWindow().getWindow().showNotification("Germplasm List", "Successfully Locked", Notification.TYPE_WARNING_MESSAGE);
            } catch (MiddlewareQueryException e) {
                e.printStackTrace();
            }
        }
        lockButton.detach();

        deleteButton.setEnabled(false);      
	}

	public void recreateTab() throws MiddlewareQueryException {
		TabSheet parentTabSheet = listManagerTreeMenu.getDetailsLayout().getTabSheet();
		String tabSheetName = germplasmList.getName();
		Tab tab = Util.getTabAlreadyExist(parentTabSheet, tabSheetName);
		if(tab != null){
			parentTabSheet.removeTab(tab);
		}
		else{
			tabSheetName = "List - " + germplasmList.getName();
			tab = Util.getTabAlreadyExist(parentTabSheet, tabSheetName);
			if(tab != null){
				parentTabSheet.removeTab(tab);
			}
		}
		
		if(tabSheetName.contains("List - ")){
		    listManagerTreeMenu.getDetailsLayout().createListInfoFromSearchScreen(germplasmListId);					
		}
		else{
			listManagerTreeMenu.getDetailsLayout().createListInfoFromBrowseScreen(germplasmListId);
		}
		
		tab = Util.getTabAlreadyExist(parentTabSheet, tabSheetName);
		parentTabSheet.setSelectedTab(tab.getComponent());
	}

	public void unlockGermplasmList() {
		if(germplasmList.getStatus()>=100){
            germplasmList.setStatus(germplasmList.getStatus()-100);
            try {
                germplasmListManager.updateGermplasmList(germplasmList);

                recreateTab();
                
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
            	
            	ListCommonActionsUtil.deleteGermplasmList(germplasmListManager, 
            			germplasmList, workbenchDataManager, getWindow(), messageSource, "list");
               
                lockButton.setEnabled(false);
                deleteButton.setEnabled(false);

                //Close tab
                listManagerTreeMenu.getListManagerMain().removeDeletedListFromUI(germplasmList.getId());           
                
                GermplasmList parentFolder = germplasmList.getParent();
                //Set listId on listManagerTreeComponent so when createTree is invoked, it is expanded until the parent of the deleted list
                if(parentFolder!=null)
                	listManagerTreeMenu.getDetailsLayout().getTreeComponent().setListId(parentFolder.getId());
                
                //Re-use refresh action on GermplasmListTreeComponent
                if (listManagerTreeMenu != null && listManagerTreeMenu.getDetailsLayout()!= null && 
                		listManagerTreeMenu.getDetailsLayout().getTreeComponent()!= null){
                	listManagerTreeMenu.getDetailsLayout().getTreeComponent().createTree();
                }
                
                if(parentFolder!=null)
                	listManagerTreeMenu.getDetailsLayout().getTreeComponent().getGermplasmListTree().expandItem(parentFolder.getId());
                else
                	listManagerTreeMenu.getDetailsLayout().getTreeComponent().getGermplasmListTree().expandItem(ListManagerTreeComponent.LOCAL);
                
            } catch (MiddlewareQueryException e) {
                getWindow().showNotification("Error", "There was a problem deleting the germplasm list", Notification.TYPE_ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    } 
	
	public void setNotesCaption(String caption){
		this.listNotes.setValue(caption);
	}
	
	public void setAddEditNotesCaption(){
		
		if(listNotes.getValue().toString().length() > 0){
			this.addEditViewButton.setCaption("View / Edit Notes");
		}
		else{
			this.addEditViewButton.setCaption("Add Notes");
		}
	}
	
	public Component createBasicDetailsHeader (String header) {
		HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setWidth("92%");
		mainLayout.setHeight("30px");
		
        CssLayout layout = new CssLayout();
        layout.setWidth("100px");
        
        Label l1 = new Label("<b>" + header + "</b>",Label.CONTENT_XHTML);
        l1.setStyleName(Bootstrap.Typography.H4.styleName());
        layout.addComponent(l1);
        	
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        renderStatusField(buttonsLayout);
        
        // delete button only for local unlocked lists - only list owner can delete
        if (this.germplasmList.getId() < 0 && isUnlockedList() && userIsListOwner()){
        	deleteButton = new Button("Delete");
        	deleteButton.setData(DELETE_BUTTON_ID);
        	deleteButton.setWidth("80px");
        	deleteButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        	deleteButton.addListener(new GermplasmListButtonClickListener(this, germplasmList));
        	buttonsLayout.addComponent(deleteButton);
        }
				
        mainLayout.addComponent(layout);
        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_RIGHT);

        return mainLayout;
	}

	private boolean isUnlockedList() {
		return germplasmList.getStatus() == 1;
	}
	
	public void setLblName(String name){
		lblName.setValue("<b>"+messageSource.getMessage(Message.NAME_LABEL)+":</b>&nbsp;&nbsp;"+name);
	}
}
