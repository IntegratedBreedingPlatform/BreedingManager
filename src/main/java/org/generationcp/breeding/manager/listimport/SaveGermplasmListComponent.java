package org.generationcp.breeding.manager.listimport;

import com.vaadin.ui.*;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.SaveCrossesMadeAction;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.breeding.manager.listimport.util.GermplasmImportUtil;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import org.vaadin.dialogs.ConfirmDialog;

import java.text.SimpleDateFormat;
import java.util.*;

@Configurable
public class SaveGermplasmListComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent{

    private static final long serialVersionUID = -2761199444687629112L;
    private final static Logger LOG = LoggerFactory.getLogger(SaveGermplasmListComponent.class);
    
    private GermplasmImportMain source;
    
    public static final String BACK_BUTTON_ID = "back button";
    public static final String DONE_BUTTON_ID = "done button";
    
    private Label listNameLabel;
    private Label descriptionLabel;
    private Label listTypeLabel;
    private Label listDateLabel;
    
    private TextField listNameText;
    private TextField descriptionText;
    
    private ComboBox listTypeComboBox;
    
    private DateField listDateField;
    
    private Button doneButton;
    private Button backButton;
    
    private Accordion accordion;
    private Component previousScreen;

    private List<Germplasm> germplasmList;
    private List<Name> nameList;

    private String DEFAULT_LIST_TYPE = "LST";
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    @Autowired
    private GermplasmListManager germplasmListManager;
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    private String filename;

    
    public SaveGermplasmListComponent(GermplasmImportMain source, Accordion accordion){
        this.source = source;
        this.accordion = accordion;
    }
    
    public void setPreviousScreen(Component previousScreen){
        this.previousScreen = previousScreen;
    }

    public List<Germplasm> getGermplasmList() {
        return germplasmList;
    }

    public void setGermplasmList(List<Germplasm> germplasmList) {
        this.germplasmList = germplasmList;
    }

    public List<Name> getNameList() {
        return nameList;
    }

    public void setNameList(List<Name> nameList) {
        this.nameList = nameList;
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
        
        listNameLabel = new Label();
        addComponent(listNameLabel, "top:30px;left:20px");
        
        listNameText = new TextField();
        listNameText.setWidth("400px");
        addComponent(listNameText, "top:10px;left:200px");
        
        descriptionLabel = new Label();
        addComponent(descriptionLabel, "top:60px;left:20px");
        
        descriptionText = new TextField();
        descriptionText.setWidth("400px");
        addComponent(descriptionText, "top:40px;left:200px");
        
        listTypeLabel = new Label();
        addComponent(listTypeLabel, "top:90px;left:20px");
        
        listTypeComboBox = new ComboBox();
        listTypeComboBox.setWidth("400px");
        listTypeComboBox.setNullSelectionAllowed(false);
        List<UserDefinedField> userDefinedFieldList = germplasmListManager.getGermplasmListTypes();
        String firstId = null;
              boolean hasDefault = false;
        for(UserDefinedField userDefinedField : userDefinedFieldList){
                  //method.getMcode()
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

        listTypeComboBox.setTextInputAllowed(false);
        listTypeComboBox.setImmediate(true);
        addComponent(listTypeComboBox, "top:70px;left:200px");
        
        listDateLabel = new Label();
        addComponent(listDateLabel, "top:120px;left:20px");
        
        listDateField = new DateField();
        listDateField.setDateFormat("yyyy-MM-dd");
        listDateField.setResolution(DateField.RESOLUTION_DAY);
        addComponent(listDateField, "top:100px;left:200px");
        
        GermplasmImportButtonClickListener clickListener = new GermplasmImportButtonClickListener(this);
        
        backButton = new Button();
        backButton.setData(BACK_BUTTON_ID);
        backButton.addListener(clickListener);
        addComponent(backButton, "top:200px;left:600px");
        
        doneButton = new Button();
        doneButton.setData(DONE_BUTTON_ID);
        doneButton.addListener(clickListener);
        addComponent(doneButton, "top:200px;left:670px");
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(listNameLabel, Message.LIST_NAME_LABEL);
        messageSource.setCaption(descriptionLabel, Message.LIST_DESCRIPTION_LABEL);
        messageSource.setCaption(listTypeLabel, Message.LIST_TYPE_LABEL);
        messageSource.setCaption(listDateLabel, Message.LIST_DATE_LABEL);
        messageSource.setCaption(backButton, Message.BACK);
        messageSource.setCaption(doneButton, Message.DONE);
    }

    public void backButtonClickAction(){
       
        if(this.previousScreen != null){
            this.accordion.setSelectedTab(previousScreen);
        } else{
            this.backButton.setEnabled(false);
        }
    }

    public void nextButtonClickAction() throws InternationalizableException {
         //do the saving now
        if (validateRequiredFields()){

                    ConfirmDialog.show(this.getWindow(), messageSource.getMessage(Message.SAVE_GERMPLASM_LIST),
                            messageSource.getMessage(Message.CONFIRM_RECORDS_WILL_BE_SAVED_FOR_GERMPLASM),
                            messageSource.getMessage(Message.OK), messageSource.getMessage(Message.CANCEL_LABEL),
                            new ConfirmDialog.Listener() {

                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        saveRecords();
                                    }
                                }

                            }
                    );
                }
    }


    private boolean validateRequiredFields(){

          return
          GermplasmImportUtil.validateRequiredStringField(getWindow(), listNameText,
                  messageSource, (String) listNameLabel.getCaption())

          && GermplasmImportUtil.validateRequiredStringField(getWindow(), descriptionText,
              messageSource,     (String) descriptionLabel.getCaption())

          && GermplasmImportUtil.validateRequiredField(getWindow(), listTypeComboBox,
              messageSource, (String) listTypeLabel.getCaption())

          && GermplasmImportUtil.validateRequiredField(getWindow(), listDateField,
              messageSource, (String) listDateLabel.getCaption());
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
             germplasmList.setParent(null);
             germplasmList.setStatus(1);

             LinkedHashMap<Germplasm, Name> germplasmNameMap = new LinkedHashMap<Germplasm, Name>();
             for(int i = 0 ; i < this.getGermplasmList().size() ; i++){
                 germplasmNameMap.put(this.getGermplasmList().get(i), this.getNameList().get(i));
             }
             Integer listId = saveAction.saveRecords(germplasmList, germplasmNameMap, getFilename());
             MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS),
                    messageSource.getMessage(Message.GERMPLASM_LIST_SAVED_SUCCESSFULLY), 3000, Window.Notification.POSITION_CENTERED);

            this.source.viewGermplasmListCreated(listId);

        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage() + " " + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
                messageSource.getMessage(Message.ERROR_IN_SAVING_CROSSES_DEFINED), Window.Notification.POSITION_CENTERED);
        }

    }

    public void setListDetails(String name, String description, Date date){
        listNameText.setValue(name);
        descriptionText.setValue(description);
        listDateField.setValue(date);
    }
    
    public GermplasmImportMain getSource() {
        return source;
    }    
}
