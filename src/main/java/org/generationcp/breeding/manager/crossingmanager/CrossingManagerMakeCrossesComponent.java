package org.generationcp.breeding.manager.crossingmanager;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.breeding.manager.listmanager.ListManagerDetailsLayout;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class CrossingManagerMakeCrossesComponent extends AbsoluteLayout 
        implements InitializingBean, InternationalizableComponent, BreedingManagerLayout{
    
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(CrossingManagerMakeCrossesComponent.class);
	
    public static final String NEXT_BUTTON_ID = "next button";
    public static final String BACK_BUTTON_ID = "back button";

    private static final long serialVersionUID = 9097810121003895303L;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private ManageCrossingSettingsMain source;

    private Button backButton;
    private Button nextButton;
    private HorizontalLayout layoutButtonArea;
    
    private MakeCrossesParentsComponent parentsComponent;
    private MakeCrossesTableComponent crossesTableComponent;
    private Integer lastOpenedListId;

    private ListManagerTreeComponent listTree;
    private Label selectParentsLabel;
    private Label instructionForSelectParents;
    private TabSheet listDetailsTabSheet;
    private Button closeAllTabsButton;
    
    public CrossingManagerMakeCrossesComponent(CrossingManagerMain source, Accordion accordion){
        lastOpenedListId = null;
    }
    
    public CrossingManagerMakeCrossesComponent(ManageCrossingSettingsMain manageCrossingSettingsMain){
    	this.source = manageCrossingSettingsMain;
    }
       
    
    @Override
    public void afterPropertiesSet() throws Exception {
    	instantiateComponents();
    	initializeValues();
    	addListeners();
    	layoutComponents();
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(backButton, Message.BACK);
        messageSource.setCaption(nextButton, Message.NEXT);
    }
    
    /*
     * Action handler for Make Cross button
     */
    public void makeCrossButtonAction(List<GermplasmListEntry> femaleList, List<GermplasmListEntry> maleList, 
    		String listnameFemaleParent, String listnameMaleParent, CrossType type, boolean makeReciprocalCrosses){
        
        if (!femaleList.isEmpty() && !maleList.isEmpty()){
            // Female - Male Multiplication
            if (CrossType.MULTIPLY.equals(type)){
                crossesTableComponent.multiplyParents(femaleList, maleList,listnameFemaleParent, listnameMaleParent);
                if (makeReciprocalCrosses){
                    crossesTableComponent.multiplyParents(maleList, femaleList,listnameFemaleParent,listnameMaleParent);
                }               
                
            // Top to Bottom Crossing    
            } else if (CrossType.TOP_TO_BOTTOM.equals(type)){
                if (femaleList.size() == maleList.size()){
                    crossesTableComponent.makeTopToBottomCrosses(femaleList, maleList,listnameFemaleParent, listnameMaleParent);
                    if (makeReciprocalCrosses){
                        crossesTableComponent.makeTopToBottomCrosses(maleList, femaleList,listnameFemaleParent, listnameMaleParent);
                    }
                } else {
                    MessageNotifier.showError(getWindow(), "Error with selecting parents."
                            ,messageSource.getMessage(Message.ERROR_MALE_AND_FEMALE_PARENTS_MUST_BE_EQUAL)
                            , Notification.POSITION_CENTERED);
                }
            }
        } else {
            MessageNotifier.showError(getWindow(), "Error with selecting parents."
                    ,messageSource.getMessage(Message.AT_LEAST_ONE_FEMALE_AND_ONE_MALE_PARENT_MUST_BE_SELECTED)
                    , Notification.POSITION_CENTERED);
        }
    }
    
    public void toggleNextButton(){
		nextButton.setEnabled(isAllListsSaved());
    }
    
    private boolean isAllListsSaved(){
    	return parentsComponent.getFemaleList() != null && 
    		parentsComponent.getMaleList() != null && 
    			crossesTableComponent.getCrossList() != null;
    }
    
    public void nextButtonClickAction(){
    	if (crossesTableComponent.getCrossList() != null){
    		source.viewGermplasmListCreated(crossesTableComponent.getCrossList());
    	}
    }
    
    
    public void backButtonClickAction(){
    	if (this.source != null){
    		this.source.backStep();
    	}
    }

    @Deprecated
    public Integer getLastOpenedListId() {
        return this.lastOpenedListId;
    }
    
    @Deprecated
    public void setLastOpenedListId(Integer lastOpenedListId) {
        this.lastOpenedListId = lastOpenedListId;
    }
    
    public void disableNextButton(){
        nextButton.setEnabled(false);
    }

    @Deprecated
    public void clearParentsListsAndCrossesTable(){
//        this.femaleParents.removeAllItems();
//        this.maleParents.removeAllItems();
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
	    	Tab newTab = listDetailsTabSheet.addTab(new SelectParentsListDataComponent(listId,listName), listName);
	    	newTab.setDescription(ListManagerDetailsLayout.generateTabDescription(listId));
	    	newTab.setClosable(true);
	    	listDetailsTabSheet.setSelectedTab(newTab);
    	}
    	
    	if(listDetailsTabSheet.getComponentCount() >= 2){
    		closeAllTabsButton.setVisible(true);
    	} else{
    		closeAllTabsButton.setVisible(false);
    	}
    }
	
	public void updateUIForDeletedList(String listName){
		for(int ctr = 0; ctr < listDetailsTabSheet.getComponentCount(); ctr++){
			Tab tab = listDetailsTabSheet.getTab(ctr);
			if(tab != null && tab.getCaption().equals(listName)){
				listDetailsTabSheet.removeTab(tab);
				return;
			}
		}
	}
	
	public void updateUIForRenamedList(Integer listId, String newName){
		String description = ListManagerDetailsLayout.generateTabDescription(listId);
		for(int ctr = 0; ctr < listDetailsTabSheet.getComponentCount(); ctr++){
			Tab tab = listDetailsTabSheet.getTab(ctr);
			if(tab != null && tab.getDescription().equals(description)){
				tab.setCaption(newName);
				return;
			}
		}
	}

	@Override
	public void instantiateComponents() {
		setWidth("1200px");
    	setHeight("1050px");
        this.setMargin(true, true, true, true);

        listTree = new ListManagerTreeComponent(this);
        
        selectParentsLabel = new Label("Select Parents");
        selectParentsLabel.setStyleName(Bootstrap.Typography.H4.styleName());
        
        instructionForSelectParents = new Label("To begin making crosses, open one or more lists from the left, then select entries and drag them into</br>"
        		+ "the male and female parent lists below.");
        instructionForSelectParents.setContentMode(Label.CONTENT_XHTML);
        
        listDetailsTabSheet = new TabSheet();
        listDetailsTabSheet.setWidth("800px");
        listDetailsTabSheet.setHeight("380px");
        listDetailsTabSheet.setVisible(false);
        
        closeAllTabsButton = new Button(messageSource.getMessage(Message.CLOSE_ALL_TABS));
        closeAllTabsButton.setStyleName(BaseTheme.BUTTON_LINK);
        closeAllTabsButton.setVisible(false);
        
        backButton = new Button();
        backButton.setData(BACK_BUTTON_ID);
        backButton.setWidth("80px");
        
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.setWidth("80px");
        nextButton.setEnabled(false);
        nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

        parentsComponent = new MakeCrossesParentsComponent(this);
        
        crossesTableComponent = new MakeCrossesTableComponent(this);
        crossesTableComponent.setWidth(550, UNITS_PIXELS);
        crossesTableComponent.setMargin(true, false, false, false);
        
        
        layoutButtonArea = new HorizontalLayout();
        layoutButtonArea.setSpacing(true);
        layoutButtonArea.setMargin(true);
        layoutButtonArea.addComponent(backButton);
        layoutButtonArea.addComponent(nextButton);
	}

	@Override
	public void initializeValues() {
		
	}

	@Override
	public void addListeners() {
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
		
		CrossingManagerImportButtonClickListener listener = new CrossingManagerImportButtonClickListener(this);
        backButton.addListener(listener);
        nextButton.addListener(listener);

	}

	@Override
	public void layoutComponents() {
        addComponent(listTree, "top:15px; left:15px;");
        addComponent(selectParentsLabel, "top:15px; left:250px;");
        addComponent(instructionForSelectParents, "top:50px; left:250px;");
        addComponent(listDetailsTabSheet, "top:40px; left:250px;");
        addComponent(closeAllTabsButton, "top:30px; right:115px;");
    
        addComponent(parentsComponent, "top:435px; left:15px;");
        addComponent(crossesTableComponent, "top:425px; left:590px;");
        addComponent(layoutButtonArea, "top:890px; left:500px;");
	}
	
	public void selectListInTree(Integer id){
		listTree.setListId(id);
		listTree.createTree();
		listTree.setSelectedListId(id);
	}
	
	public void updateCrossesSeedSource(String femaleListName, String maleListName){
		if (crossesTableComponent.getCrossList() != null){
			
		}
	}
	
	public CrossingManagerSetting getCurrentCrossingSetting(){
		return source.getDetailComponent().getCurrentlyDefinedSetting();
	}
	
	public CrossesMadeContainer getCrossesMadeContainer(){
		return source;
	}
}
