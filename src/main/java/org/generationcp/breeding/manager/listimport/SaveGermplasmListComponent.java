package org.generationcp.breeding.manager.listimport;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.breeding.manager.listmanager.dialog.SelectLocationFolderDialog;
import org.generationcp.breeding.manager.listmanager.dialog.SelectLocationFolderDialogSource;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListTreeUtil;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SaveGermplasmListComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent, SelectLocationFolderDialogSource{

    private static final long serialVersionUID = -2761199444687629112L;
    private final static Logger LOG = LoggerFactory.getLogger(SaveGermplasmListComponent.class);
    
    private GermplasmImportMain source;
    
    public static final String BACK_BUTTON_ID = "back button";
    public static final String DONE_BUTTON_ID = "done button";
    
    private Label saveInFolderLabel;
    private Label folderToSaveListTo;
    private Label listNameLabel;
    private Label descriptionLabel;
    private Label listTypeLabel;
    private Label listDateLabel;
    private Label doneLabel;
    
    private TextField listNameText;
    private TextField descriptionText;
    
    private ComboBox listTypeComboBox;
    
    private DateField listDateField;
    
    private Button doneButton;
    private Button backButton;
    private Button changeLocationFolderButton;
    
    private Accordion accordion;
    private Component previousScreen;

    private String DEFAULT_LIST_TYPE = "LST";
    @SuppressWarnings("unused")
	private Method breedingMethod; 
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    private String filename;

    private List<Integer> doNotCreateGermplasmsWithId = new ArrayList<Integer>();
    private boolean viaPopup;
    
    public SaveGermplasmListComponent(GermplasmImportMain source, Accordion accordion){
        this.source = source;
        this.accordion = accordion;
    }
    public SaveGermplasmListComponent(GermplasmImportMain source, Accordion accordion, boolean viaPopup){
        this.source = source;
        this.accordion = accordion;
        this.viaPopup = viaPopup;
    }
    
    
    public void setPreviousScreen(Component previousScreen){
        this.previousScreen = previousScreen;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("300px");
        setWidth("800px");
        
        saveInFolderLabel = new Label();
        addComponent(saveInFolderLabel, "top:30px;left:20px");
        
        folderToSaveListTo = new Label("Program Lists");
        folderToSaveListTo.setData(null);
        folderToSaveListTo.addStyleName("not-bold");
        folderToSaveListTo.setWidth("300px");
        addComponent(folderToSaveListTo, "top:10px;left:200px");
        
        changeLocationFolderButton = new Button();
        changeLocationFolderButton.addStyleName(Reindeer.BUTTON_LINK);
        changeLocationFolderButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 415799611820196717L;

			@Override
			public void buttonClick(ClickEvent event) {
				displaySelectFolderDialog();
			}
		});
        addComponent(changeLocationFolderButton, "top:10px;left:505px");
        
        listNameLabel = new Label();
        addComponent(listNameLabel, "top:60px;left:20px");
        
        listNameText = new TextField();
        listNameText.setWidth("400px");
        listNameText.setMaxLength(50);
        addComponent(listNameText, "top:40px;left:200px");
        
        descriptionLabel = new Label();
        addComponent(descriptionLabel, "top:90px;left:20px");
        
        descriptionText = new TextField();
        descriptionText.setWidth("400px");
        addComponent(descriptionText, "top:70px;left:200px");
        
        listTypeLabel = new Label();
        addComponent(listTypeLabel, "top:120px;left:20px");
        
        listTypeComboBox = new ComboBox();
        listTypeComboBox.setWidth("400px");
        listTypeComboBox.setNullSelectionAllowed(false);
        List<UserDefinedField> userDefinedFieldList = germplasmListManager.getGermplasmListTypes();
        String firstId = null;
              boolean hasDefault = false;
        for(UserDefinedField userDefinedField : userDefinedFieldList){
                  
            if(firstId == null){
                          firstId = userDefinedField.getFcode();
                      }
            listTypeComboBox.addItem(userDefinedField.getFcode());
            listTypeComboBox.setItemCaption(userDefinedField.getFcode(), userDefinedField.getFname());
                  if(DEFAULT_LIST_TYPE.equalsIgnoreCase(userDefinedField.getFcode())){
                      listTypeComboBox.setValue(userDefinedField.getFcode());
                      hasDefault = true;
                  }
              }
        if(hasDefault == false && firstId != null){
            listTypeComboBox.setValue(firstId);
        }

        listTypeComboBox.setTextInputAllowed(true);
        listTypeComboBox.setNewItemsAllowed(false);
        listTypeComboBox.setImmediate(true);
        addComponent(listTypeComboBox, "top:100px;left:200px");
        
        listDateLabel = new Label();
        addComponent(listDateLabel, "top:150px;left:20px");
        
        listDateField = new DateField();
        listDateField.setDateFormat("yyyy-MM-dd");
        listDateField.setResolution(DateField.RESOLUTION_DAY);
        addComponent(listDateField, "top:130px;left:200px");
        
        GermplasmImportButtonClickListener clickListener = new GermplasmImportButtonClickListener(this);
        
        backButton = new Button();
        backButton.setData(BACK_BUTTON_ID);
        backButton.addListener(clickListener);
        addComponent(backButton, "top:200px;left:600px");
        
        doneButton = new Button();
        doneButton.setData(DONE_BUTTON_ID);
        doneButton.addListener(clickListener);
        doneButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        addComponent(doneButton, "top:200px;left:670px");
        
        doneLabel = new Label();
        addComponent(doneLabel, "top:220px;left:20px");
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
    	messageSource.setCaption(saveInFolderLabel, Message.SAVE_IN);
    	messageSource.setCaption(changeLocationFolderButton, Message.CHANGE_LOCATION);
        messageSource.setCaption(listNameLabel, Message.LIST_NAME_LABEL);
        messageSource.setCaption(descriptionLabel, Message.LIST_DESCRIPTION_LABEL);
        messageSource.setCaption(listTypeLabel, Message.LIST_TYPE_LABEL);
        messageSource.setCaption(listDateLabel, Message.LIST_DATE_LABEL);
        messageSource.setCaption(backButton, Message.BACK);
        messageSource.setCaption(doneButton, Message.DONE);
        messageSource.setCaption(doneLabel, Message.BY_CLICKING_ON_THE_DONE_BUTTON);
    }

    public void backButtonClickAction(){
       
        if(this.previousScreen != null){
        	source.enableAllTabs();
            this.accordion.setSelectedTab(previousScreen);
            source.enableTab(2);
            source.alsoEnableTab(1);
        } else{
            this.backButton.setEnabled(false);
        }
    }

    public void nextButtonClickAction() throws InternationalizableException {
         //do the saving now
    	

    	
        if (validateListName() && validateRequiredFields()){

        saveRecords();
        
//                    ConfirmDialog.show(this.getWindow(), messageSource.getMessage(Message.SAVE_GERMPLASM_LIST),
//                            messageSource.getMessage(Message.CONFIRM_RECORDS_WILL_BE_SAVED_FOR_GERMPLASM),
//                            messageSource.getMessage(Message.OK), messageSource.getMessage(Message.CANCEL),
//                            new ConfirmDialog.Listener() {
//
//                                public void onClose(ConfirmDialog dialog) {
//                                    if (dialog.isConfirmed()) {
//                                        saveRecords();
//                                    }
//                                }
//
//                            }
//                    );
                }
    }


    private boolean validateRequiredFields(){

          return
          BreedingManagerUtil.validateRequiredStringField(getWindow(), listNameText,
                  messageSource, (String) listNameLabel.getCaption())

          && BreedingManagerUtil.validateRequiredStringField(getWindow(), descriptionText,
              messageSource,     (String) descriptionLabel.getCaption())

          && BreedingManagerUtil.validateRequiredField(getWindow(), listTypeComboBox,
              messageSource, (String) listTypeLabel.getCaption())

          && BreedingManagerUtil.validateRequiredField(getWindow(), listDateField,
              messageSource, (String) listDateLabel.getCaption());
      }
    private void notifyExternalApplication(Integer listId){
    	 this.getWindow().executeJavaScript("window.parent.closeImportFrame("+listId+");");
    	 source.reset();
    }
     //Save records into DB and redirects to GermplasmListBrowser to view created list
    private void saveRecords() {
        SaveGermplasmListAction saveAction = new SaveGermplasmListAction();

        try {

            GermplasmList germplasmList = new GermplasmList();
            /*
                  listuid - local IBDB user id of the current logged in workbench user
              */
            SimpleDateFormat formatter = new SimpleDateFormat(GermplasmImportMain.DATE_FORMAT);
            String sDate = formatter.format(listDateField.getValue());

            Long dataLongValue = Long.parseLong(sDate.replace("-", ""));
             germplasmList.setName((String)listNameText.getValue());
             germplasmList.setDate(dataLongValue);
             germplasmList.setType((String)listTypeComboBox.getValue());
             germplasmList.setDescription((String)descriptionText.getValue());
             germplasmList.setParent((GermplasmList) folderToSaveListTo.getData());
             germplasmList.setStatus(1);
             
             //LinkedHashMap<Germplasm, Name> germplasmNameMap = new LinkedHashMap<Germplasm, Name>();
//             for(int i = 0 ; i < this.getNameList().size() ; i++){
//                 if(doNotCreateGermplasmsWithId.contains(this.getGermplasmList().get(i).getGid())){
//                     //Get germplasm using temporarily set GID, then create map
//                     Germplasm germplasmToBeUsed = germplasmDataManager.getGermplasmByGID(this.getGermplasmList().get(i).getGid());
//                     germplasmNameMap.put(germplasmToBeUsed, this.getNameList().get(i));
//                     
//                     List<Germplasm> germplasmListToBeUsed = this.getGermplasmList();
//                     germplasmListToBeUsed.set(i, germplasmToBeUsed);
//                     this.setGermplasmList(germplasmListToBeUsed);
//                 } else {
//                     //Create map from data from previous screen
//                     germplasmNameMap.put(this.getGermplasmList().get(i), this.getNameList().get(i));
//                 }
//             }
             
             List<GermplasmName> germplasmNameObjects = ((SpecifyGermplasmDetailsComponent) previousScreen).getGermplasmNameObjects();
             List<GermplasmName> germplasmNameObjectsToBeSaved = new ArrayList<GermplasmName>();
             
             for(int i = 0 ; i < germplasmNameObjects.size() ; i++){
                 if(doNotCreateGermplasmsWithId.contains(germplasmNameObjects.get(i).getGermplasm().getGid())){
                     //Get germplasm using temporarily set GID, then create map
                     Germplasm germplasmToBeUsed = germplasmDataManager.getGermplasmByGID(germplasmNameObjects.get(i).getGermplasm().getGid());
                     //germplasmNameMap.put(germplasmToBeUsed, germplasmNameObjects.get(i).getName());
                     
                     germplasmNameObjectsToBeSaved.add(new GermplasmName(germplasmToBeUsed, germplasmNameObjects.get(i).getName()));
                     
                     //List<Germplasm> germplasmListToBeUsed = this.getGermplasmList();
                     //germplasmListToBeUsed.set(i, germplasmToBeUsed);
                     //this.setGermplasmList(germplasmListToBeUsed);
                 } else {
                     //Create map from data from previous screen
                     //germplasmNameMap.put(germplasmNameObjects.get(i).getGermplasm(), germplasmNameObjects.get(i).getName());
                	 
                	 //if(breedingMethod.getMtype().equals("DER")
                	 //|| breedingMethod.getMtype().equals("MAN")
                	 //)
                	 
                	 if(germplasmNameObjects.get(i).getGermplasm().getGpid1()==0 && germplasmNameObjects.get(i).getGermplasm().getGpid2()==0)
                		 germplasmNameObjects.get(i).getGermplasm().setGnpgs(-1);
                	 
                     germplasmNameObjectsToBeSaved.add(new GermplasmName(germplasmNameObjects.get(i).getGermplasm(), germplasmNameObjects.get(i).getName()));
                 }
             }             
             
             List<ImportedGermplasm> importedGermplasms = ((SpecifyGermplasmDetailsComponent) previousScreen).getImportedGermplasms();
             Integer listId = saveAction.saveRecords(germplasmList, germplasmNameObjects, getFilename(), doNotCreateGermplasmsWithId, importedGermplasms);
             MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS),
                    messageSource.getMessage(Message.GERMPLASM_LIST_SAVED_SUCCESSFULLY), 3000, Window.Notification.POSITION_CENTERED);
             if(viaPopup)
            	 notifyExternalApplication(listId);
             else
            	 this.source.viewGermplasmListCreated(listId);
            
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage() + " " + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
                messageSource.getMessage(Message.ERROR_IN_SAVING_CROSSES_DEFINED), Window.Notification.POSITION_CENTERED);
        }

    }

    public void setListDetails(String name, String description, Date date, String listType){
    	if(name.length()>50)
    		name = name.substring(0, 50);
        listNameText.setValue(name);
        descriptionText.setValue(description);
        listDateField.setValue(date);
        if(listType != null && listType.length() > 0){
        	listTypeComboBox.setValue(listType);
        }
    }
    
    public void setBreedingMethod(Method method){
    	this.breedingMethod = method;
    }
    
    public GermplasmImportMain getSource() {
        return source;
    }

    public void setDoNotCreateGermplasmsWithId(List<Integer> doNotCreateGermplasmsWithId) {
        this.doNotCreateGermplasmsWithId = doNotCreateGermplasmsWithId;
    }
    
    private Boolean validateListName(){
    	Long nameMatches = Long.valueOf(0);
    	try {
			nameMatches += germplasmListManager.countGermplasmListByName(listNameText.getValue().toString(), Operation.EQUAL, Database.LOCAL);
			nameMatches += germplasmListManager.countGermplasmListByName(listNameText.getValue().toString(), Operation.EQUAL, Database.CENTRAL);
			if(nameMatches>0){
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
		                messageSource.getMessage(Message.EXISTING_LIST_ERROR_MESSAGE), Window.Notification.POSITION_CENTERED);
				return false;
			}
		} catch (MiddlewareQueryException e) {
			LOG.error("Error with validating list name.", e);
		}
    	return true;
    }

	@Override
	public void setSelectedFolder(GermplasmList folder) {
		try{
			Deque<GermplasmList> parentFolders = new ArrayDeque<GermplasmList>();
	        GermplasmListTreeUtil.traverseParentsOfList(germplasmListManager, folder, parentFolders);
	        
	        StringBuilder locationFolderString = new StringBuilder();
	        locationFolderString.append("Program Lists");
	        
	        while(!parentFolders.isEmpty())
	        {
	        	locationFolderString.append(" > ");
	        	GermplasmList parentFolder = parentFolders.pop();
	        	locationFolderString.append(parentFolder.getName());
	        }
	        
	        if(folder != null){
	        	locationFolderString.append(" > ");
	        	locationFolderString.append(folder.getName());
	        }
	        
	        if(folder != null && folder.getName().length() >= 40){
	        	this.folderToSaveListTo.setValue(folder.getName().substring(0, 47));
	        } else if(locationFolderString.length() > 47){
	        	int lengthOfFolderName = folder.getName().length();
	        	this.folderToSaveListTo.setValue(locationFolderString.substring(0, (47 - lengthOfFolderName - 6)) + "... > " + folder.getName());
	        } else{
	        	this.folderToSaveListTo.setValue(locationFolderString.toString());
	        }
	        
	        this.folderToSaveListTo.setDescription(locationFolderString.toString());
	        this.folderToSaveListTo.setData(folder);
	    } catch(MiddlewareQueryException ex){
			LOG.error("Error with traversing parents of list: " + folder.getId(), ex);
		}
	}
	
	private void displaySelectFolderDialog(){
		GermplasmList selectedFolder = (GermplasmList) folderToSaveListTo.getData();
		SelectLocationFolderDialog selectFolderDialog = null;
		if(selectedFolder != null){
			selectFolderDialog = new SelectLocationFolderDialog(this, selectedFolder.getId());
		} else{
			selectFolderDialog = new SelectLocationFolderDialog(this, null);
		}
		this.getWindow().addWindow(selectFolderDialog);
	}
}
