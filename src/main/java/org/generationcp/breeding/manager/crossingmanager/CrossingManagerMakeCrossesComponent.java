package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.Action;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@Configurable
public class CrossingManagerMakeCrossesComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent{
    
	public static final String SELECT_FEMALE_PARENT_BUTTON_ID = "Female Parent Button";
	public static final String SELECT_MALE_PARENT_BUTTON_ID = "Male Parent Button";
	public static final String MAKE_CROSS_BUTTON_ID = "Make Cross";
	public static final String NEXT_BUTTON_ID = "next button";
	public static final String BACK_BUTTON_ID = "back button";
	public static final String PARENTS_DELIMITER = ",";
	
    private static final long serialVersionUID = 9097810121003895303L;
    
    private static final Integer CROSS_OPTIONID_ONE = 1;
    private static final Integer CROSS_OPTIONID_TWO = 2;
    private static final Action ACTION_SELECT_ALL = new Action("Select All");
	private static final Action ACTION_DELETE = new Action("Delete selected crosses");
	private static final Action[] ACTIONS_TABLE_CONTEXT_MENU = new Action[] { ACTION_SELECT_ALL, ACTION_DELETE };
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private CrossingManagerMain source;
    private Accordion accordion;
    private Label lblFemaleParent;
    private Button btnSelectListFemaleParent;
    private Label lblMaleParent;
    private Button btnSelectListMaleParent;
    private OptionGroup optionGroupMakeCrosses;
    private CheckBox chkBoxMakeReciprocalCrosses;
    private Button btnMakeCross;
    private ListSelect listSelectFemale;
    private ListSelect listSelectMale;
    private Table tableCrossesMade;
    private Label lblCrossMade;
    private GridLayout gridLayoutSelectingParents;
    private VerticalLayout layoutCrossOption;
    private Button backButton;
    private Button nextButton;
    private HorizontalLayout layoutButtonArea;
    
    private Integer lastOpenedListId;
    
    public CrossingManagerMakeCrossesComponent(CrossingManagerMain source, Accordion accordion){
    	this.source = source;
        this.accordion = accordion;
        lastOpenedListId = null;
    }
    
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.setMargin(true, true, true, true);

        lblFemaleParent= new Label();       
        
        btnSelectListFemaleParent= new Button();
        btnSelectListFemaleParent.setData(SELECT_FEMALE_PARENT_BUTTON_ID);
        btnSelectListFemaleParent.addListener(new CrossingManagerImportButtonClickListener(this));
        
        listSelectFemale = new ListSelect();
        listSelectFemale.setRows(10);
        listSelectFemale.setWidth("200px");
        listSelectFemale.setNullSelectionAllowed(true);
        listSelectFemale.setMultiSelect(true);
        listSelectFemale.setImmediate(true);
        //populateFemaleList();

        optionGroupMakeCrosses = new OptionGroup();
        optionGroupMakeCrosses.setWidth("300px");
        optionGroupMakeCrosses.addStyleName("wrapOptionGroupText");
        optionGroupMakeCrosses.addItem(CROSS_OPTIONID_ONE);
        optionGroupMakeCrosses.setItemCaption(CROSS_OPTIONID_ONE, 
        		messageSource.getMessage(Message.MAKE_CROSSES_OPTION_GROUP_ITEM_ONE_LABEL));
        optionGroupMakeCrosses.addItem(CROSS_OPTIONID_TWO);
        optionGroupMakeCrosses.setItemCaption(CROSS_OPTIONID_TWO, 
        		messageSource.getMessage(Message.MAKE_CROSSES_OPTION_GROUP_ITEM_TWO_LABEL));
        optionGroupMakeCrosses.select(CROSS_OPTIONID_ONE); //first option selected by default
        
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
        listSelectMale.setWidth("200px");
        listSelectMale.setNullSelectionAllowed(true);
        listSelectMale.setMultiSelect(true);
        listSelectMale.setImmediate(true);
        //populateMaleList();
        
        lblCrossMade=new Label();
        tableCrossesMade = new Table();
        tableCrossesMade.setSizeFull();
        tableCrossesMade.setSelectable(true);	
        tableCrossesMade.setMultiSelect(true);
        
        tableCrossesMade.addContainerProperty(1, String.class, null);
        tableCrossesMade.addContainerProperty(2, String.class, null);
        tableCrossesMade.addContainerProperty(3, String.class, null);
        tableCrossesMade.setColumnHeaders(new String[]{"Cross Name", "Female Parent", "Male Parent"});
        tableCrossesMade.addActionHandler(new Action.Handler() {
			public Action[] getActions(Object target, Object sender) {
					return ACTIONS_TABLE_CONTEXT_MENU;
			}

			public void handleAction(Action action, Object sender, Object target) {
				if (ACTION_DELETE == action) {
					deleteCrossAction();
				} else if (ACTION_SELECT_ALL == action) {
					tableCrossesMade.setValue(tableCrossesMade.getItemIds());
				}
			}
		});

        backButton = new Button();
        backButton.setData(BACK_BUTTON_ID);
       
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);

        //Widget Layout
        gridLayoutSelectingParents = new GridLayout(4,5);
        gridLayoutSelectingParents.setSpacing(true);
        
        gridLayoutSelectingParents.addComponent(lblFemaleParent,0,0);
        gridLayoutSelectingParents.addComponent(btnSelectListFemaleParent,0,1);
        gridLayoutSelectingParents.addComponent(listSelectFemale,0,2);
        gridLayoutSelectingParents.setComponentAlignment(lblFemaleParent,  Alignment.MIDDLE_CENTER);
        gridLayoutSelectingParents.setComponentAlignment(btnSelectListFemaleParent,  Alignment.MIDDLE_CENTER);
        
        gridLayoutSelectingParents.addComponent(lblMaleParent,2,0);
        gridLayoutSelectingParents.addComponent(btnSelectListMaleParent,2,1);
        gridLayoutSelectingParents.addComponent(listSelectMale,2,2);
        gridLayoutSelectingParents.setComponentAlignment(lblMaleParent,  Alignment.MIDDLE_CENTER);
        gridLayoutSelectingParents.setComponentAlignment(btnSelectListMaleParent,  Alignment.MIDDLE_CENTER);
       
        layoutCrossOption = new VerticalLayout();
        layoutCrossOption.setSpacing(true);
        layoutCrossOption.addComponent(optionGroupMakeCrosses);
        layoutCrossOption.addComponent(chkBoxMakeReciprocalCrosses);
        layoutCrossOption.addComponent(btnMakeCross);
        layoutCrossOption.setComponentAlignment(btnMakeCross,Alignment.MIDDLE_CENTER);
        
        gridLayoutSelectingParents.addComponent(layoutCrossOption,1,2);
        gridLayoutSelectingParents.setComponentAlignment(layoutCrossOption,  Alignment.TOP_LEFT);
        
        addComponent(gridLayoutSelectingParents);
        addComponent(lblCrossMade);
        addComponent(tableCrossesMade);
        
        layoutButtonArea = new HorizontalLayout();
        layoutButtonArea.setSpacing(true);
        layoutButtonArea.setMargin(true);
        layoutButtonArea.addComponent(backButton);
        layoutButtonArea.addComponent(nextButton);
        
        addComponent(layoutButtonArea);
        setComponentAlignment(layoutButtonArea, Alignment.BOTTOM_RIGHT);
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
		messageSource.setCaption(lblFemaleParent, Message.LABEL_FEMALE_PARENT);
		messageSource.setCaption(lblMaleParent, Message.LABEL_MALE_PARENT);
		messageSource.setCaption(btnSelectListFemaleParent, Message.SELECT_LIST_BUTTON_LABEL);
		messageSource.setCaption(btnSelectListMaleParent, Message.SELECT_LIST_BUTTON_LABEL);
		messageSource.setCaption(chkBoxMakeReciprocalCrosses, Message.MAKE_CROSSES_CHECKBOX_LABEL);
		messageSource.setCaption(btnMakeCross, Message.MAKE_CROSSES_BUTTON_LABEL);
		messageSource.setCaption(lblCrossMade, Message.LABEL_CROSS_MADE);
        messageSource.setCaption(backButton, Message.BACK);
        messageSource.setCaption(nextButton, Message.NEXT);
    }
    
    /*
     * Action handler for Make Cross button
     */
    @SuppressWarnings("unchecked")
    public void makeCrosses(){
    	
    	List<GermplasmListEntry> femaleList = new ArrayList<GermplasmListEntry>();
    	femaleList.addAll((Collection<GermplasmListEntry>)listSelectFemale.getValue());
    	Collections.sort(femaleList);
    	
    	List<GermplasmListEntry> maleList = new ArrayList<GermplasmListEntry>();
    	maleList.addAll((Collection<GermplasmListEntry>)listSelectMale.getValue());
    	Collections.sort(maleList);
    	
    	
    	if (!femaleList.isEmpty() && !maleList.isEmpty()){
    		Integer optionId = (Integer) optionGroupMakeCrosses.getValue();
    		
    		// Female - Male Multiplication
    		if (CROSS_OPTIONID_ONE.equals(optionId)){
    			multiplyParents(femaleList, maleList);
    			if (chkBoxMakeReciprocalCrosses.booleanValue()){
    				multiplyParents(maleList, femaleList);
    			}   			
    			
    		// Top to Bottom Crossing	
    		} else if (CROSS_OPTIONID_TWO.equals(optionId)){
    			if (femaleList.size() == maleList.size()){
    				makeTopToBottomCrosses(femaleList, maleList);
    				if (chkBoxMakeReciprocalCrosses.booleanValue()){
    					makeTopToBottomCrosses(maleList, femaleList);
    				}
    			} else {
    				accordion.getApplication().getMainWindow()
    					.showNotification("The number of male and female parents should be equal.", 
    							Notification.TYPE_ERROR_MESSAGE);
    			}
    		}
    	}
    }
    
    public void selectFemaleParentList() {
        SelectGermplasmListWindow selectListWindow = new SelectGermplasmListWindow(listSelectFemale, this);
        this.getWindow().addWindow(selectListWindow);
    }
    
    public void selectMaleParentList() {
        SelectGermplasmListWindow selectListWindow = new SelectGermplasmListWindow(listSelectMale, this);
        this.getWindow().addWindow(selectListWindow);
    }

    /**
     * Crosses each item on first list with its counterpart (same index or position) 
     * on second list. Assumes that checking if list sizes are equal was done beforehand.
     * The generated crossings are then added to Crossings Table.
     * 
     * @param parents1 - list of GermplasmList entries as first parents
     * @param parents2 - list of GermplasmList entries as second parents
     */
	private void makeTopToBottomCrosses(List<GermplasmListEntry> parents1, List<GermplasmListEntry> parents2) {
		
    	ListIterator<GermplasmListEntry> iterator1 = parents1.listIterator();
    	ListIterator<GermplasmListEntry> iterator2 = parents2.listIterator();
    	
    	while (iterator1.hasNext()){
    		GermplasmListEntry parent1 = iterator1.next();
			GermplasmListEntry parent2 = iterator2.next();
			String caption1 = parent1.getDesignation();
			String caption2 = parent2.getDesignation();
			String crossingId = getCrossingID(parent1.getGid(), parent2.getGid());
			
			if (!crossAlreadyExists(crossingId)){
				tableCrossesMade.addItem(new Object[] {
					getCrossingText(caption1, caption2), caption1, caption2 }, 
					crossingId); 
			}
    	}

	}
    
    /**
     * Multiplies each item on first list with each item on second list.
     * The generated crossings are then added to Crossings Table.
     * 
     * @param parents1 - list of GermplasmList entries as first parents
     * @param parents2 - list of GermplasmList entries as second parents
     */
    private void multiplyParents(List<GermplasmListEntry> parents1, List<GermplasmListEntry> parents2){
    	
    	for (GermplasmListEntry parent1 : parents1){
			String caption1 = parent1.getDesignation();
			
			for (GermplasmListEntry parent2 : parents2){
				String caption2 = parent2.getDesignation();
				String crossingId = getCrossingID(parent1.getGid(), parent2.getGid());
				
				if (!crossAlreadyExists(crossingId)){
					tableCrossesMade.addItem(new Object[] {
							getCrossingText(caption1, caption2), caption1, caption2 }, 
							crossingId); 					
				}
			}
		}
    }

    // Checks if crossing ID already exists in Crossing Made table
	private boolean crossAlreadyExists(String crossingId) {
		for (Object itemId : tableCrossesMade.getItemIds()){
			String idString = (String) itemId;
			if (idString.equals(crossingId)){
				return true;
			}
		}
		return false;
	}
    
    // Action handler for Delete Selected Crosses context menu option
    private void deleteCrossAction(){
    	final Collection<?> selectedIds = (Collection<?>) tableCrossesMade.getValue();
    	if (!selectedIds.isEmpty()){
    		for (Object itemId : selectedIds){
				tableCrossesMade.removeItem(itemId);
			}
    	} else {
    		MessageNotifier.showWarning(this.getWindow(), messageSource.getMessage(Message.ERROR_CROSS_MUST_BE_SELECTED), "");
    	}
    }

    // Concatenation of male and female parents' item caption
	private String getCrossingText(String caption1, String caption2) {
		return caption1 + "/" + caption2;
	}

    // Crossing ID = the GIDs of parents separated by delimiter (eg. 1,2)
	private String getCrossingID(Integer parent1, Integer parent2) {
		return parent1 + PARENTS_DELIMITER + parent2;
	}
	
	

    public CrossingManagerMain getSource() {
    	return source;
    }
    
    public Integer getLastOpenedListId() {
        return this.lastOpenedListId;
    }
    
    public void setLastOpenedListId(Integer lastOpenedListId) {
        this.lastOpenedListId = lastOpenedListId;
    }
}
