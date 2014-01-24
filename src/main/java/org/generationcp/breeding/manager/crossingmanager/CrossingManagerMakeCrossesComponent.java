package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerActionHandler;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerUploader;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.And;
import com.vaadin.event.dd.acceptcriteria.SourceIs;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.AbstractSelect.AcceptItem;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.Table.TableTransferable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class CrossingManagerMakeCrossesComponent extends VerticalLayout 
        implements InitializingBean, InternationalizableComponent, CrossesMadeContainer {
    
    public static final String SELECT_FEMALE_PARENT_BUTTON_ID = "Female Parent Button";
    public static final String SELECT_MALE_PARENT_BUTTON_ID = "Male Parent Button";
    public static final String MAKE_CROSS_BUTTON_ID = "Make Cross Button";
    public static final String NEXT_BUTTON_ID = "next button";
    public static final String BACK_BUTTON_ID = "back button";

    private static final String TAG_COLUMN_ID = "Tag";
    
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
    //private ListSelect listSelectFemale;
    //private ListSelect listSelectMale;
    private Table femaleParents;
    private CheckBox femaleParentsTagAll;
    private Table maleParents;
    private CheckBox maleParentsTagAll;
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
        
        //listSelectFemale = new ListSelect();
        //listSelectFemale.setRows(10);
        //listSelectFemale.setWidth(240, UNITS_PIXELS);
        //listSelectFemale.setNullSelectionAllowed(true);
        //listSelectFemale.setMultiSelect(true);
        //listSelectFemale.setImmediate(true);
        
        femaleParents = new Table();
        femaleParents.setHeight(180, UNITS_PIXELS);
        femaleParents.setWidth(240, UNITS_PIXELS);
        femaleParents.setNullSelectionAllowed(true);
        femaleParents.setSelectable(true);
        femaleParents.setMultiSelect(true);
        femaleParents.setImmediate(true);
        femaleParents.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
        femaleParents.addContainerProperty("Female Parents", String.class, null);
        femaleParents.setColumnWidth("Female Parents", 150);
        femaleParents.setDragMode(TableDragMode.ROW);
        femaleParents.setDropHandler(new DropHandler() {
            private static final long serialVersionUID = -3048433522366977000L;

				public void drop(DragAndDropEvent dropEvent) {
                    TableTransferable transferable = (TableTransferable) dropEvent.getTransferable();
                       
                    Table sourceTable = (Table) transferable.getSourceComponent();
                    Table targetTable = (Table) dropEvent.getTargetDetails().getTarget();
                        
                    AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent.getTargetDetails());
                    Object targetItemId = dropData.getItemIdOver();

                    //Check first if item is dropped on top of itself
                    if(!transferable.getItemId().equals(targetItemId)){
                		String femaleParentValue = (String) sourceTable.getItem(transferable.getItemId()).getItemProperty("Female Parents").getValue();
                		sourceTable.removeItem(transferable.getItemId());
                		Item item;
                    	//switch (dropData.getDropLocation()){
                        //	case BOTTOM :
                        		item = targetTable.addItemAfter(targetItemId, transferable.getItemId());
                        //	break;
                        //	case MIDDLE: 
                        //	case TOP :
                        //		item = targetTable.addItemAt(indexOfId(targetItemId), transferable.getItemId());
                        //	break;
                    	//}
                    	item.getItemProperty("Female Parents").setValue(femaleParentValue);
                    }
                }

                public AcceptCriterion getAcceptCriterion() {
                	return new And(new SourceIs(femaleParents), AcceptItem.ALL);
                }
        });
        femaleParents.addActionHandler(new CrossingManagerActionHandler(this));
        
        femaleParents.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 4586965346236384519L;

			@SuppressWarnings("unchecked")
			@Override
			public void valueChange(ValueChangeEvent event) {
				Collection<GermplasmListEntry> entries = (Collection<GermplasmListEntry>) femaleParents.getItemIds();
				Collection<GermplasmListEntry> selectedEntries = (Collection<GermplasmListEntry>) femaleParents.getValue();
				for(GermplasmListEntry entry : entries){
					CheckBox tag = (CheckBox) femaleParents.getItem(entry).getItemProperty(TAG_COLUMN_ID).getValue();
					if(selectedEntries.contains(entry)){
						tag.setValue(true);
					} else{
						tag.setValue(false);
					}
				}
			}
		});
        
        femaleParents.setItemDescriptionGenerator(new ItemDescriptionGenerator() {                             
			private static final long serialVersionUID = -3207714818504151649L;

			public String generateDescription(Component source, Object itemId, Object propertyId) {
				if(propertyId != null && propertyId == "Female Parents") {
			    	Table theTable = (Table) source;
			    	Item item = theTable.getItem(itemId);
			    	String name = (String) item.getItemProperty("Female Parents").getValue();
			    	return name;
			    }                                                                       
			    return null;
			}
		});
        
        femaleParentsTagAll = new CheckBox();
        femaleParentsTagAll.setImmediate(true);
        femaleParentsTagAll.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 7882379695058054587L;
			
			@SuppressWarnings("unchecked")
			@Override
			public void buttonClick(ClickEvent event) {
				boolean checkBoxValue = event.getButton().booleanValue();
				Collection<GermplasmListEntry> entries = (Collection<GermplasmListEntry>) femaleParents.getItemIds();
				if(checkBoxValue){
					for(GermplasmListEntry entry : entries){
						CheckBox tag = (CheckBox) femaleParents.getItem(entry).getItemProperty(TAG_COLUMN_ID).getValue();
						tag.setValue(true);
					}
					femaleParents.setValue(entries);
				} else{
					for(GermplasmListEntry entry : entries){
						CheckBox tag = (CheckBox) femaleParents.getItem(entry).getItemProperty(TAG_COLUMN_ID).getValue();
						tag.setValue(false);
					}
					femaleParents.setValue(null);
				}
			}
		});
        
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
        btnMakeCross.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        btnMakeCross.addListener(new CrossingManagerImportButtonClickListener(this));
        
        lblMaleParent=new Label();
        
        btnSelectListMaleParent= new Button();
        btnSelectListMaleParent.setData(SELECT_MALE_PARENT_BUTTON_ID);
        btnSelectListMaleParent.addListener(new CrossingManagerImportButtonClickListener(this));
        
        //listSelectMale = new ListSelect();
        //listSelectMale.setRows(10);
        //listSelectMale.setWidth(240, UNITS_PIXELS);
        //listSelectMale.setNullSelectionAllowed(true);
        //listSelectMale.setMultiSelect(true);
        //listSelectMale.setImmediate(true);
        
        
        maleParents = new Table();
        maleParents.setHeight(180, UNITS_PIXELS);
        maleParents.setWidth(240, UNITS_PIXELS);
        maleParents.setNullSelectionAllowed(true);
        maleParents.setSelectable(true);
        maleParents.setMultiSelect(true);
        maleParents.setImmediate(true);
        maleParents.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
        maleParents.addContainerProperty("Male Parents", String.class, null);
        maleParents.setColumnWidth("Male Parents", 150);
        maleParents.setDragMode(TableDragMode.ROW);
        maleParents.setDropHandler(new DropHandler() {
            private static final long serialVersionUID = -6464944116431652229L;

				public void drop(DragAndDropEvent dropEvent) {
                    TableTransferable transferable = (TableTransferable) dropEvent.getTransferable();
                        
                    Table sourceTable = (Table) transferable.getSourceComponent();
                    Table targetTable = (Table) dropEvent.getTargetDetails().getTarget();
                        
                    AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent.getTargetDetails());
                    Object targetItemId = dropData.getItemIdOver();

                    //Check first if item is dropped on top of itself
                    if(!transferable.getItemId().equals(targetItemId)){
                        String maleParentValue = (String) sourceTable.getItem(transferable.getItemId()).getItemProperty("Male Parents").getValue();
                        GermplasmListEntry maleItemId = (GermplasmListEntry) transferable.getItemId();
                        
                        sourceTable.removeItem(transferable.getItemId());
                        
                        Item item = targetTable.addItemAfter(targetItemId, maleItemId);
                      	item.getItemProperty("Male Parents").setValue(maleParentValue);
                	}
                }

                public AcceptCriterion getAcceptCriterion() {
                	return new And(new SourceIs(maleParents), AcceptItem.ALL);
                }
        });
        maleParents.addActionHandler(new CrossingManagerActionHandler(this));
        
        maleParents.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 4586965346236384519L;

			@SuppressWarnings("unchecked")
			@Override
			public void valueChange(ValueChangeEvent event) {
				Collection<GermplasmListEntry> entries = (Collection<GermplasmListEntry>) maleParents.getItemIds();
				Collection<GermplasmListEntry> selectedEntries = (Collection<GermplasmListEntry>) maleParents.getValue();
				for(GermplasmListEntry entry : entries){
					CheckBox tag = (CheckBox) maleParents.getItem(entry).getItemProperty(TAG_COLUMN_ID).getValue();
					if(selectedEntries.contains(entry)){
						tag.setValue(true);
					} else{
						tag.setValue(false);
					}
				}
			}
		});
        
        maleParents.setItemDescriptionGenerator(new ItemDescriptionGenerator() {                             
			private static final long serialVersionUID = -3207714818504151649L;

			public String generateDescription(Component source, Object itemId, Object propertyId) {
				if(propertyId != null && propertyId == "Male Parents") {
			    	Table theTable = (Table) source;
			    	Item item = theTable.getItem(itemId);
			    	String name = (String) item.getItemProperty("Male Parents").getValue();
			    	return name;
			    }                                                                       
			    return null;
			}
		});
        
        maleParentsTagAll = new CheckBox();
        maleParentsTagAll.setImmediate(true);
        maleParentsTagAll.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -6111529620495423779L;

			@SuppressWarnings("unchecked")
			@Override
			public void buttonClick(ClickEvent event) {
				boolean checkBoxValue = event.getButton().booleanValue();
				Collection<GermplasmListEntry> entries = (Collection<GermplasmListEntry>) maleParents.getItemIds();
				if(checkBoxValue){
					for(GermplasmListEntry entry : entries){
						CheckBox tag = (CheckBox) maleParents.getItem(entry).getItemProperty(TAG_COLUMN_ID).getValue();
						tag.setValue(true);
					}
					maleParents.setValue(entries);
				} else{
					for(GermplasmListEntry entry : entries){
						CheckBox tag = (CheckBox) maleParents.getItem(entry).getItemProperty(TAG_COLUMN_ID).getValue();
						tag.setValue(false);
					}
					maleParents.setValue(null);
				}
			}
		});
        
        CrossingManagerImportButtonClickListener listener = new CrossingManagerImportButtonClickListener(this);
        
        backButton = new Button();
        backButton.setData(BACK_BUTTON_ID);
        backButton.addListener(listener);
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(listener);
        nextButton.setEnabled(false);
        nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

        //Widget Layout
        gridLayoutSelectingParents = new GridLayout(2,2);
        gridLayoutSelectingParents.setSpacing(true);
        
        gridLayoutSelectingParents.addComponent(btnSelectListFemaleParent,0,0);
        AbsoluteLayout femaleParentsTableLayout = new AbsoluteLayout();
        femaleParentsTableLayout.setWidth("260px");
        femaleParentsTableLayout.setHeight("180px");
        femaleParentsTableLayout.addComponent(femaleParents, "top:0px;left:20px");
        femaleParentsTableLayout.addComponent(femaleParentsTagAll, "top:4px;left:55px");
        gridLayoutSelectingParents.addComponent(femaleParentsTableLayout,0,1);
        gridLayoutSelectingParents.setComponentAlignment(btnSelectListFemaleParent,  Alignment.MIDDLE_CENTER);
        gridLayoutSelectingParents.setComponentAlignment(femaleParents,  Alignment.MIDDLE_CENTER);
        
        gridLayoutSelectingParents.addComponent(btnSelectListMaleParent,1,0);
        AbsoluteLayout maleParentsTableLayout = new AbsoluteLayout();
        maleParentsTableLayout.setWidth("260px");
        maleParentsTableLayout.setHeight("180px");
        maleParentsTableLayout.addComponent(maleParents, "top:0px;left:20px");
        maleParentsTableLayout.addComponent(maleParentsTagAll, "top:4px;left:55px");
        gridLayoutSelectingParents.addComponent(maleParentsTableLayout,1,1);
        gridLayoutSelectingParents.setComponentAlignment(btnSelectListMaleParent,  Alignment.MIDDLE_CENTER);
        gridLayoutSelectingParents.setComponentAlignment(maleParents,  Alignment.MIDDLE_CENTER);
        
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
    public void makeCrossButtonAction(){
        
        List<GermplasmListEntry> femaleList = getCorrectSortedValue(femaleParents);
        List<GermplasmListEntry> maleList = getCorrectSortedValue(maleParents);
        
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
        SelectGermplasmListWindow selectListWindow = new SelectGermplasmListWindow(femaleParents, this,this.listnameFemaleParent, femaleParentsTagAll);
        selectListWindow.addStyleName(Reindeer.WINDOW_LIGHT);
        this.getWindow().addWindow(selectListWindow);
    }
    
    public void selectMaleParentList() {
        SelectGermplasmListWindow selectListWindow = new SelectGermplasmListWindow(maleParents, this,this.listnameMaleParent, maleParentsTagAll);
        selectListWindow.addStyleName(Reindeer.WINDOW_LIGHT);
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
    
    
    /**
     * Implemented something similar to table.getValue(), because that method returns
     *     a collection of items, but does not follow the sorting done by the 
     *     drag n drop sorting, this one does
     * @param table
     * @return List of selected germplasm list entries
     */
    @SuppressWarnings("unchecked")
	private List<GermplasmListEntry> getCorrectSortedValue(Table table){
    	List<GermplasmListEntry> allItemIds = new ArrayList<GermplasmListEntry>();
    	List<GermplasmListEntry> selectedItemIds = new ArrayList<GermplasmListEntry>();
    	List<GermplasmListEntry> sortedSelectedValues = new ArrayList<GermplasmListEntry>();
    	
    	allItemIds.addAll((Collection<GermplasmListEntry>) table.getItemIds());
    	selectedItemIds.addAll((Collection<GermplasmListEntry>) table.getValue());

    	for(GermplasmListEntry entry : allItemIds){
			CheckBox tag = (CheckBox) table.getItem(entry).getItemProperty(TAG_COLUMN_ID).getValue();
			Boolean tagValue = (Boolean) tag.getValue();
			if(tagValue.booleanValue()){
				selectedItemIds.add(entry);
			} 
		}
    	
    	for(GermplasmListEntry itemId : allItemIds){
    		for(GermplasmListEntry selectedItemId : selectedItemIds){
    			if(itemId.equals(selectedItemId))
    				sortedSelectedValues.add(selectedItemId);    			
    		}
    	}
    	return sortedSelectedValues;
    }
    
    public void setupDefaultListFromFile(){
        CrossingManagerUploader crossingManagerUploader = crossesMade.getCrossingManagerUploader();
        // retrieve list entries and add them to the parent ListSelect component
        //add checking to provide error
        maleParents.removeAllItems();
        femaleParents.removeAllItems();
        if(crossingManagerUploader.isFemaleListIdSpecified() && crossingManagerUploader.isMaleListIdSpecified() &&
                crossingManagerUploader.getFemaleGermplasmList() == null && crossingManagerUploader.getMaleGermplasmList() == null){
            MessageNotifier.showWarning(this.getWindow(), "Warning!", messageSource.getMessage(Message.ERROR_GERMPLASM_LIST_IMPORT_BOTH_ID_REQUIRED)
                    , Notification.POSITION_CENTERED);
        }else if(crossingManagerUploader.isFemaleListIdSpecified() && crossingManagerUploader.getFemaleGermplasmList() == null &&
                crossingManagerUploader.isMaleListIdSpecified() && crossingManagerUploader.getMaleGermplasmList() != null){
            MessageNotifier.showWarning(this.getWindow(), "Warning!", messageSource.getMessage(Message.ERROR_GERMPLASM_LIST_IMPORT_FEMALE_ID_REQUIRED)
                    , Notification.POSITION_CENTERED);
            loadListFromUpload(maleParents, crossingManagerUploader.getMaleGermplasmList());
            listnameMaleParent.setValue(crossingManagerUploader.getMaleGermplasmList().getName());
        }else if(crossingManagerUploader.isMaleListIdSpecified() && crossingManagerUploader.getMaleGermplasmList() == null &&
                crossingManagerUploader.isFemaleListIdSpecified() && crossingManagerUploader.getFemaleGermplasmList() != null){
            MessageNotifier.showWarning(this.getWindow(), "Warning!", messageSource.getMessage(Message.ERROR_GERMPLASM_LIST_IMPORT_MALE_ID_REQUIRED)
                    , Notification.POSITION_CENTERED);
            loadListFromUpload(femaleParents, crossingManagerUploader.getFemaleGermplasmList());
            listnameFemaleParent.setValue(crossingManagerUploader.getFemaleGermplasmList().getName());
        }else if(crossingManagerUploader.isFemaleListIdSpecified() && crossingManagerUploader.isMaleListIdSpecified()){
            loadListFromUpload(maleParents, crossingManagerUploader.getMaleGermplasmList());
            listnameMaleParent.setValue(crossingManagerUploader.getMaleGermplasmList().getName());
            loadListFromUpload(femaleParents, crossingManagerUploader.getFemaleGermplasmList());
            listnameFemaleParent.setValue(crossingManagerUploader.getFemaleGermplasmList().getName());
        }


        maleParents.requestRepaint();
        femaleParents.requestRepaint();
    }

	private void loadListFromUpload(Table listSelect, GermplasmList germplasmList){
        if(germplasmList != null){
            for (Iterator<?> i = germplasmList.getListData().iterator(); i.hasNext();) {
                // retrieve entries from the table
                GermplasmListData germplasmListData = (GermplasmListData)i.next();

                // add entries to the parent ListSelect
                GermplasmListEntry entry = new GermplasmListEntry(germplasmListData.getId(), germplasmListData.getGid(), 
                        germplasmListData.getEntryId(), germplasmListData.getDesignation());
                listSelect.addItem(new Object[] {entry.getEntryId()+" -> "+entry.getDesignation()}, entry);
            }
            listSelect.requestRepaint();
        }
    }
    
    public void disableNextButton(){
        nextButton.setEnabled(false);
    }

    public void clearParentsListsAndCrossesTable(){
        this.femaleParents.removeAllItems();
        this.maleParents.removeAllItems();
        this.crossesTableComponent.clearCrossesTable();
    }
}
