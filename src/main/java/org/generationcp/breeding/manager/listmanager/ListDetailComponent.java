package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

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
    
    private boolean usedForDetailsOnly;
    
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
		setRows(2);
        setColumns(5);
        setColumnExpandRatio(0, 3);
        setColumnExpandRatio(2, 3);
        setColumnExpandRatio(4, 3);
        setSpacing(true);
        setMargin(true);
        
        // get GermplasmList Detail
        germplasmList = germplasmListManager.getGermplasmListById(germplasmListId);
        
        lblName = new Label( "<b>" + messageSource.getMessage(Message.NAME_LABEL) + ":</b> " + germplasmList.getName(), Label.CONTENT_XHTML); // "Name"
        lblDescription = new Label("<b>" + messageSource.getMessage(Message.DESCRIPTION_LABEL) + ":</b> " + germplasmList.getDescription(), Label.CONTENT_XHTML ); // "Description"
        lblCreationDate = new Label("<b>" + messageSource.getMessage(Message.CREATION_DATE_LABEL) + ":</b> " + String.valueOf(germplasmList.getDate()), Label.CONTENT_XHTML); // "Creation Date"
        lblType = new Label("<b>" + messageSource.getMessage(Message.TYPE_LABEL) + ":</b> " + germplasmList.getType(), Label.CONTENT_XHTML); // "Type"
        lblStatus = new Label("<b>" + messageSource.getMessage(Message.STATUS_LABEL) + ":</b> " + germplasmList.getStatusString(), Label.CONTENT_XHTML); // "Status"
        lblListOwner = new Label("<b>" + messageSource.getMessage(Message.LIST_OWNER_LABEL) + ":</b> " + getOwnerListName(germplasmList.getUserId()), Label.CONTENT_XHTML); // "List Owner"
       
        addComponent(lblName, 0, 0);
        addComponent(lblDescription, 2, 0);
        addComponent(lblType, 4, 0);
        addComponent(lblCreationDate, 0, 1);
        addComponent(lblListOwner, 2, 1);
        addComponent(lblStatus, 4, 1);
        
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
	
}
