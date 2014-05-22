package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.SelectTreeItemOnSaveListener;
import org.generationcp.breeding.manager.customfields.BreedingManagerListDetailsComponent;
import org.generationcp.breeding.manager.customfields.LocalListFoldersTreeComponent;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SaveListAsDialog extends Window implements InitializingBean, InternationalizableComponent, BreedingManagerLayout{

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(SaveListAsDialog.class);
	
	private CssLayout mainLayout;
	private HorizontalLayout contentLayout;
	private HorizontalLayout buttonLayout;
	
	private final SaveListAsDialogSource source;
	
//	private Label listLocationLabel;
	private LocalListFoldersTreeComponent germplasmListTree;
	private Integer folderId;
	
	private BreedingManagerListDetailsComponent listDetailsComponent;

	private Button cancelButton;
	private Button saveButton;
	
	private final String windowCaption;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
    private GermplasmListManager germplasmListManager;
	
	private GermplasmList originalGermplasmList;
	private GermplasmList germplasmList;
	
	public static final Integer LIST_NAMES_STATUS = 1;
	
	public SaveListAsDialog(SaveListAsDialogSource source, GermplasmList germplasmList){
		this.source = source;
		this.originalGermplasmList = germplasmList;
		this.germplasmList = germplasmList;
		this.windowCaption = null;
	}
	
	public SaveListAsDialog(SaveListAsDialogSource source, GermplasmList germplasmList, String windowCaption){
		this.source = source;
		this.originalGermplasmList = germplasmList;
		this.germplasmList = germplasmList;
		this.windowCaption = windowCaption;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}
	
	@Override
	public void instantiateComponents() {
		if(windowCaption == null){
			setCaption(messageSource.getMessage(Message.SAVE_LIST_AS));
		} else{
			setCaption(windowCaption);
		}
		
		addStyleName(Reindeer.WINDOW_LIGHT);
		setResizable(false);
		setModal(true);

		if(germplasmList!=null)
		    germplasmListTree = new LocalListFoldersTreeComponent(new SelectTreeItemOnSaveListener(this), germplasmList.getId(), false);
		else
			germplasmListTree = new LocalListFoldersTreeComponent(new SelectTreeItemOnSaveListener(this), null, false);
		
//		listLocationLabel = germplasmListTree.getHeading();
//		listLocationLabel.setValue(messageSource.getMessage(Message.LIST_LOCATION));
//		listLocationLabel.setStyleName(Bootstrap.Typography.H6.styleName());
//		germplasmListTree.setHeading(listLocationLabel);
		
		listDetailsComponent = new BreedingManagerListDetailsComponent(germplasmList);
		
		cancelButton = new Button(messageSource.getMessage(Message.CANCEL));
		cancelButton.setWidth("80px");
		
		saveButton = new Button(messageSource.getMessage(Message.SAVE_LABEL));
		saveButton.setWidth("80px");
		saveButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
		if(germplasmList != null){
			
			GermplasmList parent = germplasmList.getParent();
			//if(parent != null){ // if not "Program Lists"
			//	germplasmListTree.setListId(parent.getId());
			//	germplasmListTree.setSelectedListId(parent.getId());
			//}
			
			germplasmListTree.createTree();
			
			listDetailsComponent.setGermplasmListDetails(germplasmList);

		}
	}

	@Override
	public void addListeners() {
		cancelButton.addListener(new CloseWindowAction());
		saveButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 993268331611479850L;

			@Override
			public void buttonClick(final ClickEvent event) {
				
				//Call method so that the variables will be updated, values will be used for the logic below
				getGermplasmListToSave();
				
				//If target list is locked
				if(germplasmList!=null && germplasmList.getStatus()>=100) {
					MessageNotifier.showError(getWindow().getParent().getWindow(), messageSource.getMessage(Message.ERROR), messageSource.getMessage(Message.UNABLE_TO_EDIT_LOCKED_LIST));
		
				//If target list to be overwritten is not itself and is an existing list
				} else if(!germplasmList.getType().equals("FOLDER") && (germplasmList.getId()!=null && originalGermplasmList==null) || (germplasmList.getId()!=null && originalGermplasmList!=null &&  germplasmList.getId()!=originalGermplasmList.getId())) {
		            ConfirmDialog.show(getWindow().getParent().getWindow(), messageSource.getMessage(Message.DO_YOU_WANT_TO_OVERWRITE_THIS_LIST)+"?", 
			                messageSource.getMessage(Message.LIST_DATA_WILL_BE_DELETED_AND_WILL_BE_REPLACED_WITH_THE_DATA_FROM_THE_LIST_THAT_YOU_JUST_CREATED), 
			                messageSource.getMessage(Message.OK), messageSource.getMessage(Message.CANCEL), 
			                new ConfirmDialog.Listener() {
								private static final long serialVersionUID = 1L;
								public void onClose(ConfirmDialog dialog) {
			                        if (dialog.isConfirmed()) {
			    						source.saveList(getGermplasmListToSave());
			    						Window window = event.getButton().getWindow();
			    				        window.getParent().removeWindow(window);
			                        }
			                    }
			                }
			            );
		            
		        //If target list to be overwritten is itself
				} else {
					if(validateAllFields()){
						source.saveList(getGermplasmListToSave());
						
						Window window = event.getButton().getWindow();
				        window.getParent().removeWindow(window);
					}
				}
			}
			
		});
	}

	@Override
	public void layoutComponents() {
		setWidth("725px");
		setHeight("493px");
		
		contentLayout = new HorizontalLayout();
		contentLayout.setSpacing(true);
		contentLayout.addComponent(germplasmListTree);
		contentLayout.addComponent(listDetailsComponent);
		contentLayout.addStyleName("contentLayout");

		contentLayout.setWidth("689px");
		contentLayout.setHeight("344px");
		
		germplasmListTree.addStyleName("germplasmListTree");
		listDetailsComponent.addStyleName("listDetailsComponent");
		
		buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true);
		buttonLayout.addComponent(cancelButton);
		buttonLayout.addComponent(saveButton);
		buttonLayout.addStyleName("buttonLayout");
		
		HorizontalLayout buttonLayoutMain = new HorizontalLayout();
		buttonLayoutMain.addComponent(buttonLayout);
		buttonLayoutMain.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
		buttonLayoutMain.setWidth("100%");
		buttonLayoutMain.setHeight("60px");
		buttonLayoutMain.addStyleName("buttonLayoutMain");
		
		mainLayout = new CssLayout();
		mainLayout.setSizeFull();
		mainLayout.addComponent(contentLayout);
		mainLayout.addComponent(buttonLayoutMain);
		mainLayout.addStyleName("mainlayout");
		
		addComponent(mainLayout);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
	
	public GermplasmList getSelectedListOnTree(){
		Integer folderId = null;
		if(germplasmListTree.getSelectedListId() instanceof Integer){
			folderId = (Integer) germplasmListTree.getSelectedListId();
		}
		
		GermplasmList folder = null;
		if(folderId != null){
			try {
				folder = germplasmListManager.getGermplasmListById(folderId);
			} catch (MiddlewareQueryException e) {
				LOG.error("Error with retrieving list with id: " + folderId, e);
				e.printStackTrace();
			}
		}
		
		return folder;
	}
	
	public GermplasmList getGermplasmListToSave(){
		Integer currentId = null;
		if(germplasmList != null){
			currentId = germplasmList.getId();
		}
		
		GermplasmList selectedList = getSelectedListOnTree();
		
		//If selected item on list/folder tree is a list, use that as target germplasm list
		if(selectedList!=null && !selectedList.getType().equals("FOLDER")){
			germplasmList = getSelectedListOnTree();
			
			//Needed for overwriting
			source.setCurrentlySavedGermplasmList(germplasmList);
			
			//If selected item is a folder, get parent of that folder
			try {
				selectedList = germplasmListManager.getGermplasmListById(selectedList.getParentId());
			} catch (MiddlewareQueryException e) {
				LOG.error("Error with getting parent list: " + selectedList.getParentId(), e);
				e.printStackTrace();
			}
			
		//If not, use old method, get germplasm list the old way
		} else {
			germplasmList = listDetailsComponent.getGermplasmList();
			germplasmList.setId(currentId);
			germplasmList.setStatus(LIST_NAMES_STATUS);
		}
		
		germplasmList.setParent(selectedList);         
        return germplasmList;
	}

	protected boolean validateAllFields() {
		
		if(!listDetailsComponent.validate()){
			return false;
		}
		
		return true;
	}
	
	public BreedingManagerListDetailsComponent getDetailsComponent(){
		return this.listDetailsComponent;
	}
}
