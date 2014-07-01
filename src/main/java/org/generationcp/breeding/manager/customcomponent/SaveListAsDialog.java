package org.generationcp.breeding.manager.customcomponent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerMain;
import org.generationcp.breeding.manager.crossingmanager.listeners.SelectTreeItemOnSaveListener;
import org.generationcp.breeding.manager.customfields.BreedingManagerListDetailsComponent;
import org.generationcp.breeding.manager.customfields.LocalListFoldersTreeComponent;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListBuilderComponent;
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
import com.vaadin.ui.Label;
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
	
	private Label guideMessage;
	private LocalListFoldersTreeComponent germplasmListTree;
	private BreedingManagerListDetailsComponent listDetailsComponent;

	private Button cancelButton;
	private Button saveButton;
	
	private final String windowCaption;
	@SuppressWarnings("unused")
	private String defaultListType;
	
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
	
	public SaveListAsDialog(SaveListAsDialogSource source, String defaultListType, GermplasmList germplasmList){
		this.source = source;
		this.originalGermplasmList = germplasmList;
		this.germplasmList = germplasmList;
		this.defaultListType = defaultListType;
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
		    germplasmListTree = new LocalListFoldersTreeComponent(new SelectTreeItemOnSaveListener(this,source.getParentComponent()), germplasmList.getId(), false, true);
		else
			germplasmListTree = new LocalListFoldersTreeComponent(new SelectTreeItemOnSaveListener(this,source.getParentComponent()), null, false, true);
		
		guideMessage = new Label(messageSource.getMessage(Message.SELECT_A_FOLDER_TO_CREATE_A_LIST_OR_SELECT_AN_EXISTING_LIST_TO_EDIT_AND_OVERWRITE_ITS_ENTRIES)+".");
		
		listDetailsComponent = new BreedingManagerListDetailsComponent(defaultListType(), germplasmList);
		
		cancelButton = new Button(messageSource.getMessage(Message.CANCEL));
		cancelButton.setWidth("80px");
		
		saveButton = new Button(messageSource.getMessage(Message.SAVE_LABEL));
		saveButton.setWidth("80px");
		saveButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}
	
	public String defaultListType(){
		return "LST";
	}

	@Override
	public void initializeValues() {
		if(germplasmList != null){		
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
				
				SimpleDateFormat formatter = new SimpleDateFormat(CrossingManagerMain.DATE_AS_NUMBER_FORMAT);
				
				try {
				    listDetailsComponent.getListDateField().validate();
				} catch (Exception e) {
					MessageNotifier.showError(getWindow().getParent().getWindow(), 
							messageSource.getMessage(Message.ERROR), messageSource.getMessage(Message.DATE_MUST_BE_IN_THIS_FORMAT));
					return;
				}
				
				Date date;
				try {
					date = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH).parse(listDetailsComponent.getListDateField().getValue().toString());
				} catch (ParseException e) {
					date = new Date();
				}
				
				//If target list is locked
				if(germplasmList!=null && germplasmList.getStatus()>=100) {
					MessageNotifier.showError(getWindow().getParent().getWindow(), 
							messageSource.getMessage(Message.ERROR), messageSource.getMessage(Message.UNABLE_TO_EDIT_LOCKED_LIST));
				
				//If target list to be overwritten is not itself and is an existing list
				} else if( (germplasmList.getType()!=null && !germplasmList.getType().equals("FOLDER") && (germplasmList.getId()!=null && originalGermplasmList==null)) 
						|| (germplasmList.getId()!=null && originalGermplasmList!=null &&  germplasmList.getId()!=originalGermplasmList.getId())) {
					
					final GermplasmList gl = getGermplasmListToSave();
					gl.setName(listDetailsComponent.getListNameField().getValue().toString());
					gl.setDescription(listDetailsComponent.getListDescriptionField().getValue().toString());
					gl.setType(listDetailsComponent.getListTypeField().getValue().toString());
					gl.setDate(Long.parseLong(formatter.format(date)));
					gl.setNotes(listDetailsComponent.getListNotesField().getValue().toString());
					
		            ConfirmDialog.show(getWindow().getParent().getWindow(), messageSource.getMessage(Message.DO_YOU_WANT_TO_OVERWRITE_THIS_LIST)+"?", 
			                messageSource.getMessage(Message.LIST_DATA_WILL_BE_DELETED_AND_WILL_BE_REPLACED_WITH_THE_DATA_FROM_THE_LIST_THAT_YOU_JUST_CREATED), 
			                messageSource.getMessage(Message.OK), messageSource.getMessage(Message.CANCEL), 
			                new ConfirmDialog.Listener() {
								private static final long serialVersionUID = 1L;
								public void onClose(ConfirmDialog dialog) {
			                        if (dialog.isConfirmed()) {
			    						source.saveList(gl);
			    						saveReservationChanges();
			    						Window window = event.getButton().getWindow();
			    				        window.getParent().removeWindow(window);
			                        }
			                    }
			                }
			            );
		            
		        //If target list to be overwritten is itself
				} else {
					if(validateAllFields()){
						
						GermplasmList gl = getGermplasmListToSave();
						gl.setName(listDetailsComponent.getListNameField().getValue().toString());
						gl.setDescription(listDetailsComponent.getListDescriptionField().getValue().toString());
						gl.setType(listDetailsComponent.getListTypeField().getValue().toString());
						gl.setDate(Long.parseLong(formatter.format(date)));
						gl.setNotes(listDetailsComponent.getListNotesField().getValue().toString());
						
						source.saveList(gl);
						saveReservationChanges();
						
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
		setHeight("510px");
		
		contentLayout = new HorizontalLayout();
		contentLayout.setSpacing(true);
		contentLayout.addComponent(germplasmListTree);
		contentLayout.addComponent(listDetailsComponent);
		contentLayout.addStyleName("contentLayout");

		contentLayout.setWidth("689px");
		contentLayout.setHeight("341px");
		
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
		mainLayout.addComponent(guideMessage);
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
	
	public SaveListAsDialogSource getSource(){
		return source;
	}
	
	public void setGermplasmList(GermplasmList germplasmList){
		this.germplasmList = germplasmList;
	}
	
	public BreedingManagerListDetailsComponent getListDetailsComponent(){
		return listDetailsComponent;
	}
	
	public LocalListFoldersTreeComponent getGermplasmListTree(){
		return germplasmListTree;
	}
	
	public void saveReservationChanges(){
		if(source instanceof ListBuilderComponent){
			((ListBuilderComponent) source).saveReservationChangesAction();
		}
	}
}
