package org.generationcp.breeding.manager.crossingmanager;

import java.util.*;

import com.vaadin.ui.*;
import com.vaadin.ui.Window.Notification;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerUploader;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class CrossingManagerMakeCrossesComponent extends VerticalLayout 
        implements InitializingBean, InternationalizableComponent, CrossesMadeContainer {
    
    public static final String SELECT_FEMALE_PARENT_BUTTON_ID = "Female Parent Button";
    public static final String SELECT_MALE_PARENT_BUTTON_ID = "Male Parent Button";
    public static final String MAKE_CROSS_BUTTON_ID = "Make Cross Button";
    public static final String NEXT_BUTTON_ID = "next button";
    public static final String BACK_BUTTON_ID = "back button";

    
    private static final long serialVersionUID = 9097810121003895303L;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private CrossingManagerMain source;
    private Accordion accordion;
    private Component nextScreen;
    private Component previousScreen;
    
    private Label lblFemaleParent;
    private Button btnSelectListFemaleParent;
    private Label lblMaleParent;
    private Button btnSelectListMaleParent;
    private OptionGroup optionGroupMakeCrosses;
    private CheckBox chkBoxMakeReciprocalCrosses;
    private Button btnMakeCross;
    private ListSelect listSelectFemale;
    private ListSelect listSelectMale;
    private GridLayout gridLayoutSelectingParents;
    private GridLayout gridLayoutSelectingParentOptions;
    private VerticalLayout layoutCrossOption;
    private Button backButton;
    private Button nextButton;
    private HorizontalLayout layoutButtonArea;
    
    private MakeCrossesTableComponent crossesTableComponent;
    private Integer lastOpenedListId;

    private CrossesMade crossesMade;
    private Label listnameFemaleParent;
    private Label listnameMaleParent;
    
    private enum CrossType { 
        MULTIPLY, TOP_TO_BOTTOM
    };
    
    public CrossingManagerMakeCrossesComponent(CrossingManagerMain source, Accordion accordion){
        this.source = source;
        this.accordion = accordion;
        lastOpenedListId = null;
    }
    
    public CrossingManagerMain getSource() {
        return source;
    }
    
    public void setNextScreen(Component nextScreen){
        this.nextScreen = nextScreen;
    }
    
    public void setPreviousScreen(Component backScreen){
        this.previousScreen = backScreen;
    }
    
    @Override
    public CrossesMade getCrossesMade() {
        return this.crossesMade;
    }

    @Override
    public void setCrossesMade(CrossesMade crossesMade) {
        this.crossesMade = crossesMade;
        
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.setMargin(true, true, true, true);

        lblFemaleParent= new Label(); 
        listnameFemaleParent= new Label();
        listnameMaleParent=new Label();
        
        btnSelectListFemaleParent= new Button();
        btnSelectListFemaleParent.setData(SELECT_FEMALE_PARENT_BUTTON_ID);
        btnSelectListFemaleParent.addListener(new CrossingManagerImportButtonClickListener(this));
        
        listSelectFemale = new ListSelect();
        listSelectFemale.setRows(10);
        listSelectFemale.setWidth(240, UNITS_PIXELS);
        listSelectFemale.setNullSelectionAllowed(true);
        listSelectFemale.setMultiSelect(true);
        listSelectFemale.setImmediate(true);

        optionGroupMakeCrosses = new OptionGroup();
        optionGroupMakeCrosses.setWidth(560,UNITS_PIXELS);
        optionGroupMakeCrosses.addStyleName("wrapOptionGroupText");
        optionGroupMakeCrosses.addItem(CrossType.MULTIPLY);
        optionGroupMakeCrosses.setItemCaption(CrossType.MULTIPLY, 
                messageSource.getMessage(Message.MAKE_CROSSES_OPTION_GROUP_ITEM_ONE_LABEL));
        optionGroupMakeCrosses.addItem(CrossType.TOP_TO_BOTTOM);
        optionGroupMakeCrosses.setItemCaption(CrossType.TOP_TO_BOTTOM, 
                messageSource.getMessage(Message.MAKE_CROSSES_OPTION_GROUP_ITEM_TWO_LABEL));
        optionGroupMakeCrosses.select(CrossType.MULTIPLY); //first option selected by default
        
        chkBoxMakeReciprocalCrosses = new CheckBox();
    
        btnMakeCross= new Button();
        btnMakeCross.setData(MAKE_CROSS_BUTTON_ID);
        btnMakeCross.addListener(new CrossingManagerImportButtonClickListener(this));
        
        lblMaleParent=new Label();
        
        btnSelectListMaleParent= new Button();
        btnSelectListMaleParent.setData(SELECT_MALE_PARENT_BUTTON_ID);
        btnSelectListMaleParent.addListener(new CrossingManagerImportButtonClickListener(this));
        
        listSelectMale = new ListSelect();
        listSelectMale.setRows(10);
        listSelectMale.setWidth(240, UNITS_PIXELS);
        listSelectMale.setNullSelectionAllowed(true);
        listSelectMale.setMultiSelect(true);
        listSelectMale.setImmediate(true);
        
        CrossingManagerImportButtonClickListener listener = new CrossingManagerImportButtonClickListener(this);
        
        backButton = new Button();
        backButton.setData(BACK_BUTTON_ID);
        backButton.addListener(listener);
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(listener);
        nextButton.setEnabled(false);

        //Widget Layout
        gridLayoutSelectingParents = new GridLayout(2,2);
        gridLayoutSelectingParents.setSpacing(true);
        
        gridLayoutSelectingParents.addComponent(btnSelectListFemaleParent,0,0);
        gridLayoutSelectingParents.addComponent(listSelectFemale,0,1);
        gridLayoutSelectingParents.setComponentAlignment(btnSelectListFemaleParent,  Alignment.MIDDLE_CENTER);
        gridLayoutSelectingParents.setComponentAlignment(listSelectFemale,  Alignment.MIDDLE_CENTER);
        
        gridLayoutSelectingParents.addComponent(btnSelectListMaleParent,1,0);
        gridLayoutSelectingParents.addComponent(listSelectMale,1,1);
        gridLayoutSelectingParents.setComponentAlignment(btnSelectListMaleParent,  Alignment.MIDDLE_CENTER);
        gridLayoutSelectingParents.setComponentAlignment(listSelectMale,  Alignment.MIDDLE_CENTER);
        
        gridLayoutSelectingParents.setWidth(600, UNITS_PIXELS);
        
        gridLayoutSelectingParentOptions = new GridLayout(1,1);
        gridLayoutSelectingParentOptions.setSpacing(true);
        
        layoutCrossOption = new VerticalLayout();
        layoutCrossOption.setSpacing(true);
        layoutCrossOption.addComponent(optionGroupMakeCrosses);
        layoutCrossOption.addComponent(chkBoxMakeReciprocalCrosses);
        layoutCrossOption.addComponent(btnMakeCross);
        layoutCrossOption.setComponentAlignment(btnMakeCross,Alignment.MIDDLE_CENTER);
        
        gridLayoutSelectingParentOptions.setWidth(560, UNITS_PIXELS);
        gridLayoutSelectingParentOptions.setMargin(true, false, false, false);
        
        gridLayoutSelectingParentOptions.addComponent(layoutCrossOption,0,0);
        gridLayoutSelectingParentOptions.setComponentAlignment(layoutCrossOption,  Alignment.TOP_LEFT);
        
        addComponent(gridLayoutSelectingParents);
        addComponent(gridLayoutSelectingParentOptions);
        
        setComponentAlignment(gridLayoutSelectingParents, Alignment.TOP_CENTER);
        setComponentAlignment(gridLayoutSelectingParentOptions, Alignment.TOP_CENTER);
        
        crossesTableComponent = new MakeCrossesTableComponent();
        crossesTableComponent.setWidth(550, UNITS_PIXELS);
        crossesTableComponent.setMargin(true, false, false, false);
        addComponent(crossesTableComponent);
        setComponentAlignment(crossesTableComponent, Alignment.TOP_CENTER);
        
        layoutButtonArea = new HorizontalLayout();
        layoutButtonArea.setSpacing(true);
        layoutButtonArea.setMargin(true);
        layoutButtonArea.addComponent(backButton);
        layoutButtonArea.addComponent(nextButton);
        
        addComponent(layoutButtonArea);
        setComponentAlignment(layoutButtonArea, Alignment.BOTTOM_CENTER);
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(lblFemaleParent, Message.LABEL_FEMALE_PARENTS);
        messageSource.setCaption(lblMaleParent, Message.LABEL_MALE_PARENTS);
        messageSource.setCaption(btnSelectListFemaleParent, Message.SELECT_FEMALE_LIST_BUTTON_LABEL);
        messageSource.setCaption(btnSelectListMaleParent, Message.SELECT_MALE_LIST_BUTTON_LABEL);
        messageSource.setCaption(chkBoxMakeReciprocalCrosses, Message.MAKE_CROSSES_CHECKBOX_LABEL);
        messageSource.setCaption(btnMakeCross, Message.MAKE_CROSSES_BUTTON_LABEL);
        messageSource.setCaption(backButton, Message.BACK);
        messageSource.setCaption(nextButton, Message.NEXT);
    }
    
    /*
     * Action handler for Make Cross button
     */
    @SuppressWarnings("unchecked")
    public void makeCrossButtonAction(){
        
        List<GermplasmListEntry> femaleList = new ArrayList<GermplasmListEntry>();
        femaleList.addAll((Collection<GermplasmListEntry>) listSelectFemale.getValue());
        Collections.sort(femaleList);
        
        List<GermplasmListEntry> maleList = new ArrayList<GermplasmListEntry>();
        maleList.addAll((Collection<GermplasmListEntry>)listSelectMale.getValue());
        Collections.sort(maleList);
        
        
        if (!femaleList.isEmpty() && !maleList.isEmpty()){
            CrossType optionId = (CrossType) optionGroupMakeCrosses.getValue();
            
            // Female - Male Multiplication
            if (CrossType.MULTIPLY.equals(optionId)){
                crossesTableComponent.multiplyParents(femaleList, maleList,listnameFemaleParent,this.listnameMaleParent);
                if (chkBoxMakeReciprocalCrosses.booleanValue()){
                    crossesTableComponent.multiplyParents(maleList, femaleList,listnameFemaleParent,this.listnameMaleParent);
                }               
                
            // Top to Bottom Crossing    
            } else if (CrossType.TOP_TO_BOTTOM.equals(optionId)){
                if (femaleList.size() == maleList.size()){
                    crossesTableComponent.makeTopToBottomCrosses(femaleList, maleList,listnameFemaleParent,this.listnameMaleParent);
                    if (chkBoxMakeReciprocalCrosses.booleanValue()){
                        crossesTableComponent.makeTopToBottomCrosses(maleList, femaleList,listnameFemaleParent,this.listnameMaleParent);
                    }
                } else {
                    MessageNotifier.showError(getWindow(), "Error with selecting parents."
                            ,messageSource.getMessage(Message.ERROR_MALE_AND_FEMALE_PARENTS_MUST_BE_EQUAL)
                            , Notification.POSITION_CENTERED);
                }
            }
            this.nextButton.setEnabled(true);
        }
    }
    
    public void selectFemaleParentList() {
        SelectGermplasmListWindow selectListWindow = new SelectGermplasmListWindow(listSelectFemale, this,this.listnameFemaleParent);
        this.getWindow().addWindow(selectListWindow);
    }
    
    public void selectMaleParentList() {
        SelectGermplasmListWindow selectListWindow = new SelectGermplasmListWindow(listSelectMale, this,this.listnameMaleParent);
        this.getWindow().addWindow(selectListWindow);
    }

     
    public void nextButtonClickAction(){
        nextScreen = source.getWizardScreenThree();
        
        if(this.nextScreen != null){
            assert this.nextScreen instanceof CrossesMadeContainer;
            assert crossesTableComponent instanceof CrossesMadeContainerUpdateListener;
            
            CrossesMadeContainerUpdateListener listener = ((CrossesMadeContainerUpdateListener) crossesTableComponent);
            listener.setCrossesMadeContainer(this);
            listener.updateCrossesMadeContainer();
            ((CrossesMadeContainer) nextScreen).setCrossesMade(this.crossesMade);
        
            source.getWizardScreenThree().setPreviousScreen(this);
            source.enableWizardTabs();
            this.accordion.setSelectedTab(this.nextScreen);
            source.enableOnlyWizardTabThree();
            source.enableWizardTabOne();
            source.enableWizardTabTwo();
        } else {
            this.nextButton.setEnabled(false);
        }
    }
    
    
    public void backButtonClickAction(){
        if(this.previousScreen != null){
            source.enableWizardTabs();
            this.accordion.setSelectedTab(this.previousScreen);
            //if(previousScreen instanceof CrossingManagerImportFileComponent)
            //    source.enableOnlyWizardTabOne();
        } else {
            this.backButton.setEnabled(false);
        }
    }
    
    public Integer getLastOpenedListId() {
        return this.lastOpenedListId;
    }
    
    public void setLastOpenedListId(Integer lastOpenedListId) {
        this.lastOpenedListId = lastOpenedListId;
    }
    
    public void setupDefaultListFromFile(){
        CrossingManagerUploader crossingManagerUploader = crossesMade.getCrossingManagerUploader();
        // retrieve list entries and add them to the parent ListSelect component
        //add checking to provide error
        listSelectMale.removeAllItems();
        listSelectFemale.removeAllItems();
        if(crossingManagerUploader.isFemaleListIdSpecified() && crossingManagerUploader.isMaleListIdSpecified() &&
                crossingManagerUploader.getFemaleGermplasmList() == null && crossingManagerUploader.getMaleGermplasmList() == null){
            MessageNotifier.showWarning(this.getWindow(), "Warning!", messageSource.getMessage(Message.ERROR_GERMPLASM_LIST_IMPORT_BOTH_ID_REQUIRED)
                    , Notification.POSITION_CENTERED);
        }else if(crossingManagerUploader.isFemaleListIdSpecified() && crossingManagerUploader.getFemaleGermplasmList() == null &&
                crossingManagerUploader.isMaleListIdSpecified() && crossingManagerUploader.getMaleGermplasmList() != null){
            MessageNotifier.showWarning(this.getWindow(), "Warning!", messageSource.getMessage(Message.ERROR_GERMPLASM_LIST_IMPORT_FEMALE_ID_REQUIRED)
                    , Notification.POSITION_CENTERED);
            loadListFromUpload(listSelectMale, crossingManagerUploader.getMaleGermplasmList());
        }else if(crossingManagerUploader.isMaleListIdSpecified() && crossingManagerUploader.getMaleGermplasmList() == null &&
                crossingManagerUploader.isFemaleListIdSpecified() && crossingManagerUploader.getFemaleGermplasmList() != null){
            MessageNotifier.showWarning(this.getWindow(), "Warning!", messageSource.getMessage(Message.ERROR_GERMPLASM_LIST_IMPORT_MALE_ID_REQUIRED)
                    , Notification.POSITION_CENTERED);
            loadListFromUpload(listSelectFemale, crossingManagerUploader.getFemaleGermplasmList());
        }else if(crossingManagerUploader.isFemaleListIdSpecified() && crossingManagerUploader.isMaleListIdSpecified()){
            loadListFromUpload(listSelectMale, crossingManagerUploader.getMaleGermplasmList());
            loadListFromUpload(listSelectFemale, crossingManagerUploader.getFemaleGermplasmList());
        }


        listSelectMale.requestRepaint();
        listSelectFemale.requestRepaint();
    }
    private void loadListFromUpload(ListSelect listSelect, GermplasmList germplasmList){
        if(germplasmList != null){
            for (Iterator<?> i = germplasmList.getListData().iterator(); i.hasNext();) {
                // retrieve entries from the table
                GermplasmListData germplasmListData = (GermplasmListData)i.next();

                // add entries to the parent ListSelect
                GermplasmListEntry entry = new GermplasmListEntry(germplasmListData.getId(), germplasmListData.getGid(), 
                        germplasmListData.getEntryId(), germplasmListData.getDesignation());
                listSelect.addItem(entry);
                String itemCaption = entry.getEntryId()+" -> "+entry.getDesignation(); 
                listSelect.setItemCaption(entry, itemCaption);
            }
            listSelect.requestRepaint();
        }
    }
    
    public void disableNextButton(){
        nextButton.setEnabled(false);
    }

    public void clearParentsListsAndCrossesTable(){
        this.listSelectFemale.removeAllItems();
        this.listSelectMale.removeAllItems();
        this.crossesTableComponent.clearCrossesTable();
    }
}
