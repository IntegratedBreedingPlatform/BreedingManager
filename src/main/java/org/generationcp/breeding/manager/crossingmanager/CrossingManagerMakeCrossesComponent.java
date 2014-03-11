package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerActionHandler;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.listeners.ParentsTableCheckboxListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.settings.ApplyCrossingSettingAction;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerUploader;
import org.generationcp.breeding.manager.listmanager.ListManagerDetailsLayout;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.Table.TableTransferable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class CrossingManagerMakeCrossesComponent extends AbsoluteLayout 
        implements InitializingBean, InternationalizableComponent, CrossesMadeContainerUpdateListener {
    
	private static final Logger LOG = LoggerFactory.getLogger(CrossingManagerMakeCrossesComponent.class);
	
	public static final String SELECT_FEMALE_PARENT_BUTTON_ID = "Female Parent Button";
    public static final String SELECT_MALE_PARENT_BUTTON_ID = "Male Parent Button";
    public static final String MAKE_CROSS_BUTTON_ID = "Make Cross Button";
    public static final String NEXT_BUTTON_ID = "next button";
    public static final String BACK_BUTTON_ID = "back button";

    private static final String TAG_COLUMN_ID = "Tag";
    
    private static final long serialVersionUID = 9097810121003895303L;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
//    private CrossingManagerMain source;
//    private Accordion accordion;
    private ManageCrossingSettingsMain source;
    
    private Label lblFemaleParent;
    private Label lblMaleParent;
    private Label crossingMethodLabel;
    private ComboBox crossingMethodComboBox;
    private CheckBox chkBoxMakeReciprocalCrosses;
    private Button btnMakeCross;
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
        
    private ListManagerTreeComponent listTree;
    private Label selectParentsLabel;
    private Label instructionForSelectParents;
    private TabSheet listDetailsTabSheet;
    private Button closeAllTabsButton;
    
    public CrossingManagerMakeCrossesComponent(CrossingManagerMain source, Accordion accordion){
//        this.source = source;
//        this.accordion = accordion;
        lastOpenedListId = null;
    }
    
    public CrossingManagerMakeCrossesComponent(ManageCrossingSettingsMain manageCrossingSettingsMain){
    	this.source = manageCrossingSettingsMain;
    }
       
    
    @Override
    public void afterPropertiesSet() throws Exception {
    	
    	setHeight("840px");
        this.setMargin(true, true, true, true);

        listTree = new ListManagerTreeComponent(this);
        addComponent(listTree, "top:15px; left:15px;");
        
        selectParentsLabel = new Label("Select Parents");
        selectParentsLabel.setStyleName(Bootstrap.Typography.H4.styleName());
        addComponent(selectParentsLabel, "top:15px; left:250px;");
        
        instructionForSelectParents = new Label("To begin making crosses, open one or more lists from the left, then select entries and drag them into</br>"
        		+ "the male and female parent lists below.");
        instructionForSelectParents.setContentMode(Label.CONTENT_XHTML);
        addComponent(instructionForSelectParents, "top:50px; left:250px;");
        
        listDetailsTabSheet = new TabSheet();
        listDetailsTabSheet.setWidth("800px");
        listDetailsTabSheet.setHeight("380px");
        listDetailsTabSheet.setVisible(false);
        addComponent(listDetailsTabSheet, "top:40px; left:250px;");
        
        closeAllTabsButton = new Button(messageSource.getMessage(Message.CLOSE_ALL_TABS));
        closeAllTabsButton.setStyleName(BaseTheme.BUTTON_LINK);
        closeAllTabsButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -2946008623293356900L;

			@Override
			public void buttonClick(ClickEvent event) {
				Util.closeAllTab(listDetailsTabSheet);
				listDetailsTabSheet.setVisible(false);
				instructionForSelectParents.setVisible(true);
				closeAllTabsButton.setVisible(false);
			}
		});
        closeAllTabsButton.setVisible(false);
        addComponent(closeAllTabsButton, "top:30px; right:115px;");
        
        lblFemaleParent= new Label(); 
        listnameFemaleParent= new Label();
        listnameMaleParent=new Label();
        
        femaleParents = new Table();
        femaleParents.setHeight(180, UNITS_PIXELS);
        femaleParents.setWidth(240, UNITS_PIXELS);
        femaleParents.setNullSelectionAllowed(true);
        femaleParents.setSelectable(true);
        femaleParents.setMultiSelect(true);
        femaleParents.setImmediate(true);
        femaleParents.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
        femaleParents.addContainerProperty("Female Parents", String.class, null);
        femaleParents.setColumnWidth(TAG_COLUMN_ID, 25);
        //femaleParents.setColumnWidth("Female Parents", 160);
        femaleParents.setDragMode(TableDragMode.ROW);
        femaleParents.setDropHandler(new DropHandler() {
            private static final long serialVersionUID = -3048433522366977000L;

				@SuppressWarnings("unchecked")
				public void drop(DragAndDropEvent dropEvent) {
					TableTransferable transferable = (TableTransferable) dropEvent.getTransferable();
                       
                    Table sourceTable = (Table) transferable.getSourceComponent();
                    Table targetTable = (Table) dropEvent.getTargetDetails().getTarget();
                        
                    AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent.getTargetDetails());
                    Object targetItemId = dropData.getItemIdOver();
                    
                    if(sourceTable.equals(femaleParents)){
	                    Collection<GermplasmListEntry> selectedEntries = (Collection<GermplasmListEntry>) sourceTable.getValue();
	
	                    //Check first if item is dropped on top of itself
	                    if(!transferable.getItemId().equals(targetItemId)){
	                		String femaleParentValue = (String) sourceTable.getItem(transferable.getItemId()).getItemProperty("Female Parents").getValue();
	                		GermplasmListEntry femaleItemId = (GermplasmListEntry) transferable.getItemId();
	                		CheckBox tag = (CheckBox) sourceTable.getItem(femaleItemId).getItemProperty(TAG_COLUMN_ID).getValue();
							
	                		sourceTable.removeItem(transferable.getItemId());
	                		
							Item item = targetTable.addItemAfter(targetItemId, transferable.getItemId());
	                    	item.getItemProperty("Female Parents").setValue(femaleParentValue);
	                      	item.getItemProperty(TAG_COLUMN_ID).setValue(tag);
	                      	
	                      	if(selectedEntries.contains(femaleItemId)){
	                      		tag.setValue(true);
								tag.addListener(new ParentsTableCheckboxListener(targetTable, femaleItemId, femaleParentsTagAll));
					            tag.setImmediate(true);
					            targetTable.select(transferable.getItemId());
							} 	
	                      	
	                    }
                    } else if(sourceTable.getData().equals(SelectParentsListDataComponent.LIST_DATA_TABLE_ID)){
                    	dropToFemaleOrMaleTable(sourceTable, femaleParents);
                    }
                }

                public AcceptCriterion getAcceptCriterion() {
                	return AcceptAll.get();
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
				if(selectedEntries.size() == entries.size()){
					femaleParentsTagAll.setValue(true);
				}
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
        femaleParentsTagAll.setCaption(messageSource.getMessage(Message.SELECT_ALL));
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
        
        crossingMethodLabel = new Label(messageSource.getMessage(Message.CROSSING_METHOD));
        crossingMethodLabel.addStyleName("bold");
        
        crossingMethodComboBox = new ComboBox();
        crossingMethodComboBox.setNewItemsAllowed(false);
        crossingMethodComboBox.setNullSelectionAllowed(false);
        crossingMethodComboBox.setWidth("500px");
        crossingMethodComboBox.addItem(CrossType.MULTIPLY);
        crossingMethodComboBox.setItemCaption(CrossType.MULTIPLY, messageSource.getMessage(Message.MAKE_CROSSES_OPTION_GROUP_ITEM_ONE_LABEL));
        crossingMethodComboBox.addItem(CrossType.TOP_TO_BOTTOM);
        crossingMethodComboBox.setItemCaption(CrossType.TOP_TO_BOTTOM, messageSource.getMessage(Message.MAKE_CROSSES_OPTION_GROUP_ITEM_TWO_LABEL));
        crossingMethodComboBox.select(CrossType.MULTIPLY);
        
        chkBoxMakeReciprocalCrosses = new CheckBox();
    
        btnMakeCross= new Button();
        btnMakeCross.setData(MAKE_CROSS_BUTTON_ID);
        btnMakeCross.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        btnMakeCross.addListener(new CrossingManagerImportButtonClickListener(this));
        
        lblMaleParent=new Label();
        
        maleParents = new Table();
        maleParents.setHeight(180, UNITS_PIXELS);
        maleParents.setWidth(240, UNITS_PIXELS);
        maleParents.setNullSelectionAllowed(true);
        maleParents.setSelectable(true);
        maleParents.setMultiSelect(true);
        maleParents.setImmediate(true);
        maleParents.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
        maleParents.addContainerProperty("Male Parents", String.class, null);
        maleParents.setColumnWidth(TAG_COLUMN_ID, 25);
        //maleParents.setColumnWidth("Male Parents", 160);
        maleParents.setDragMode(TableDragMode.ROW);
        maleParents.setDropHandler(new DropHandler() {
            private static final long serialVersionUID = -6464944116431652229L;

				@SuppressWarnings("unchecked")
				public void drop(DragAndDropEvent dropEvent) {
                    TableTransferable transferable = (TableTransferable) dropEvent.getTransferable();
                        
                    Table sourceTable = (Table) transferable.getSourceComponent();
                    Table targetTable = (Table) dropEvent.getTargetDetails().getTarget();
                    
                    AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent.getTargetDetails());
                    Object targetItemId = dropData.getItemIdOver();

                    if(sourceTable.equals(maleParents)){
                    	Collection<GermplasmListEntry> selectedEntries = (Collection<GermplasmListEntry>) sourceTable.getValue();
                        
	                    //Check first if item is dropped on top of itself
	                    if(!transferable.getItemId().equals(targetItemId)){
	                        String maleParentValue = (String) sourceTable.getItem(transferable.getItemId()).getItemProperty("Male Parents").getValue();
	                        GermplasmListEntry maleItemId = (GermplasmListEntry) transferable.getItemId();
	                        CheckBox tag = (CheckBox) sourceTable.getItem(maleItemId).getItemProperty(TAG_COLUMN_ID).getValue();
	                        	
	                        sourceTable.removeItem(transferable.getItemId());
	                        
							Item item = targetTable.addItemAfter(targetItemId, maleItemId);
	                      	item.getItemProperty("Male Parents").setValue(maleParentValue);
	                      	item.getItemProperty(TAG_COLUMN_ID).setValue(tag);
	                      	
	                      	if(selectedEntries.contains(maleItemId)){
								tag.setValue(true);
								tag.addListener(new ParentsTableCheckboxListener(targetTable, maleItemId, maleParentsTagAll));
					            tag.setImmediate(true);
					            targetTable.select(transferable.getItemId());
							}
	                	}
                    } else if(sourceTable.getData().equals(SelectParentsListDataComponent.LIST_DATA_TABLE_ID)){
                    	dropToFemaleOrMaleTable(sourceTable, maleParents);
                    }
                }

                public AcceptCriterion getAcceptCriterion() {
                	return AcceptAll.get();
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
				
				if(selectedEntries.size() == entries.size()){
					maleParentsTagAll.setValue(true);
				}
				
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
        maleParentsTagAll.setCaption(messageSource.getMessage(Message.SELECT_ALL));
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
        gridLayoutSelectingParents = new GridLayout(2,1);
        gridLayoutSelectingParents.setSpacing(true);
        
        AbsoluteLayout femaleParentsTableLayout = new AbsoluteLayout();
        femaleParentsTableLayout.setWidth("260px");
        femaleParentsTableLayout.setHeight("200px");
        femaleParentsTableLayout.addComponent(femaleParents, "top:0px;left:20px");
        femaleParentsTableLayout.addComponent(femaleParentsTagAll, "top:184px;left:27px");
        gridLayoutSelectingParents.addComponent(femaleParentsTableLayout,0,0);
        gridLayoutSelectingParents.setComponentAlignment(femaleParents,  Alignment.MIDDLE_CENTER);
        
        AbsoluteLayout maleParentsTableLayout = new AbsoluteLayout();
        maleParentsTableLayout.setWidth("260px");
        maleParentsTableLayout.setHeight("200px");
        maleParentsTableLayout.addComponent(maleParents, "top:0px;left:20px");
        maleParentsTableLayout.addComponent(maleParentsTagAll, "top:184px;left:27px");
        gridLayoutSelectingParents.addComponent(maleParentsTableLayout,1,0);
        gridLayoutSelectingParents.setComponentAlignment(maleParents,  Alignment.MIDDLE_CENTER);
        
        gridLayoutSelectingParents.setWidth(550, UNITS_PIXELS);
        
        gridLayoutSelectingParentOptions = new GridLayout(1,1);
        gridLayoutSelectingParentOptions.setSpacing(true);
        gridLayoutSelectingParentOptions.setMargin(true, false, false, true);
        
        layoutCrossOption = new VerticalLayout();
        layoutCrossOption.setSpacing(true);
        layoutCrossOption.addComponent(crossingMethodLabel);
        layoutCrossOption.addComponent(crossingMethodComboBox);
        layoutCrossOption.addComponent(chkBoxMakeReciprocalCrosses);
        layoutCrossOption.addComponent(btnMakeCross);
        layoutCrossOption.setComponentAlignment(btnMakeCross,Alignment.MIDDLE_CENTER);
        
        gridLayoutSelectingParentOptions.setWidth(500, UNITS_PIXELS);
        gridLayoutSelectingParentOptions.setMargin(true, false, false, false);
        
        gridLayoutSelectingParentOptions.addComponent(layoutCrossOption,0,0);
        gridLayoutSelectingParentOptions.setComponentAlignment(layoutCrossOption,  Alignment.TOP_LEFT);
        
        addComponent(gridLayoutSelectingParents, "top:435px; left:15px;");
        addComponent(gridLayoutSelectingParentOptions, "top:650px; left:35px;");
        
        crossesTableComponent = new MakeCrossesTableComponent();
        crossesTableComponent.setWidth(550, UNITS_PIXELS);
        crossesTableComponent.setMargin(true, false, false, false);
        
        addComponent(crossesTableComponent, "top:435px; left:590px;");
        
        layoutButtonArea = new HorizontalLayout();
        layoutButtonArea.setSpacing(true);
        layoutButtonArea.setMargin(true);
        layoutButtonArea.addComponent(backButton);
        layoutButtonArea.addComponent(nextButton);
        
        addComponent(layoutButtonArea, "top:780px; left:500px;");
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
            CrossType optionId = (CrossType) crossingMethodComboBox.getValue();
            
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
        } else {
            MessageNotifier.showError(getWindow(), "Error with selecting parents."
                    ,messageSource.getMessage(Message.AT_LEAST_ONE_FEMALE_AND_ONE_MALE_PARENT_MUST_BE_SELECTED)
                    , Notification.POSITION_CENTERED);
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
    	if (this.source != null){
    		this.source.nextStep();
    	}
    }
    
    
    public void backButtonClickAction(){
    	if (this.source != null){
    		this.source.backStep();
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
    
    public void createListDetailsTab(Integer listId, String listName){
    	instructionForSelectParents.setVisible(false);
    	listDetailsTabSheet.setVisible(true);
    	if(Util.isTabExist(listDetailsTabSheet, listName)){
    		Tab tabToFocus = null;
    		for(int ctr = 0; ctr < listDetailsTabSheet.getComponentCount(); ctr++){
    			Tab tab = listDetailsTabSheet.getTab(ctr);
    			if(tab != null && tab.getCaption().equals(listName)){
    				tabToFocus = tab;
    			}
    		}
    		if (tabToFocus != null){
            	listDetailsTabSheet.setSelectedTab(tabToFocus);
            }
	    } else{
	    	Tab newTab = listDetailsTabSheet.addTab(new SelectParentsListDataComponent(listId), listName);
	    	newTab.setDescription(ListManagerDetailsLayout.generateTabDescription(listId));
	    	listDetailsTabSheet.setSelectedTab(newTab);
    	}
    	
    	if(listDetailsTabSheet.getComponentCount() >= 2){
    		closeAllTabsButton.setVisible(true);
    	} else{
    		closeAllTabsButton.setVisible(false);
    	}
    }

	@Override
	public boolean updateCrossesMadeContainer(CrossesMadeContainer container) {
		boolean updatedFromTable = crossesTableComponent.updateCrossesMadeContainer(container);
		
		if (updatedFromTable){
			ApplyCrossingSettingAction applySetting = new ApplyCrossingSettingAction(source.getDetailComponent().getCurrentlyDefinedSetting());
			return applySetting.updateCrossesMadeContainer(container);
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private void dropToFemaleOrMaleTable(Table sourceTable, Table targetTable){
		List<Integer> selectedListEntries = new ArrayList<Integer>();
    	selectedListEntries.addAll((Collection<Integer>) sourceTable.getValue());
    	
    	for(Integer itemId : selectedListEntries){
    		Integer entryId = (Integer) sourceTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).getValue();
    		Button designationButton = (Button) sourceTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue(); 
    		String designation = designationButton.getCaption();
    		Button gidButton = (Button) sourceTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.GID.getName()).getValue();
    		Integer gid = Integer.valueOf(Integer.parseInt(gidButton.getCaption())); 
    		
    		GermplasmListEntry entryObject = new GermplasmListEntry(itemId, gid, entryId, designation);
    		Item item = targetTable.addItem(entryObject);
    		if(item != null){
	    		if(targetTable.equals(femaleParents)){
	    			item.getItemProperty("Female Parents").setValue(entryObject.getEntryId() + " -> " + entryObject.getDesignation());
	    		} else{
	    			item.getItemProperty("Male Parents").setValue(entryObject.getEntryId() + " -> " + entryObject.getDesignation());
	    		}
	    		
	    		CheckBox tag = new CheckBox();
	    		if(targetTable.equals(femaleParents)){
	    			tag.addListener(new ParentsTableCheckboxListener(targetTable, entryObject, femaleParentsTagAll));
	    		} else{
	    			tag.addListener(new ParentsTableCheckboxListener(targetTable, entryObject, maleParentsTagAll));
	    		}
	            tag.setImmediate(true);
	            item.getItemProperty(TAG_COLUMN_ID).setValue(tag);
    		}
            
            targetTable.requestRepaint();
        }
	}
}
