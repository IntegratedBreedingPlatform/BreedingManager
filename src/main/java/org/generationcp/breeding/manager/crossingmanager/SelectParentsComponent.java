package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerTreeActionsListener;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.UnsavedChangesSource;
import org.generationcp.breeding.manager.listmanager.ListManagerDetailsLayout;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.CloseHandler;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SelectParentsComponent extends VerticalLayout implements BreedingManagerLayout,InitializingBean, 
												InternationalizableComponent, CrossingManagerTreeActionsListener, UnsavedChangesSource {

	private static final long serialVersionUID = -5109231715662648484L;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	private CrossingManagerMakeCrossesComponent source;
	
	private CrossingManagerListTreeComponent listTreeComponent;
    private Button browseForListsButton;
    
    private Label selectParentsLabel;
    private Label instructionForSelectParents;
    private TabSheet listDetailsTabSheet;
    private Button closeAllTabsButton;
    
    private Map<SelectParentsListDataComponent,Boolean> listStatusForChanges;
    
	public SelectParentsComponent(CrossingManagerMakeCrossesComponent source) {
		super();
		this.source = source;
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
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
        selectParentsLabel = new Label(messageSource.getMessage(Message.SELECT_PARENTS));
        selectParentsLabel.setStyleName(Bootstrap.Typography.H4.styleName());
        selectParentsLabel.addStyleName(AppConstants.CssStyles.BOLD);

        browseForListsButton = new Button(messageSource.getMessage(Message.BROWSE));
        browseForListsButton.setImmediate(true);
        browseForListsButton.setStyleName(Reindeer.BUTTON_LINK);

        listTreeComponent = new CrossingManagerListTreeComponent(this,source);
                
        instructionForSelectParents = new Label("for a list to work with.");
        
        listDetailsTabSheet = new TabSheet();
        listDetailsTabSheet.setWidth("460px");
        listDetailsTabSheet.setHeight("465px");
        listDetailsTabSheet.setVisible(false);
        
        closeAllTabsButton = new Button(messageSource.getMessage(Message.CLOSE_ALL_TABS));
        closeAllTabsButton.setStyleName(BaseTheme.BUTTON_LINK);
        closeAllTabsButton.setVisible(false);
        
        listStatusForChanges = new HashMap<SelectParentsListDataComponent,Boolean>();
	}

	@Override
	public void initializeValues() {
	}

	@Override
	public void addListeners() {
		
		listDetailsTabSheet.setCloseHandler(new CloseHandler() {
			private static final long serialVersionUID = -7085023295466691749L;

			@Override
			public void onTabClose(TabSheet tabsheet, Component tabContent) {
				if(tabsheet.getComponentCount() > 1){
		            String tabCaption=tabsheet.getTab(tabContent).getCaption();
		            Tab tab = Util.getTabToFocus(tabsheet, tabCaption);
		            tabsheet.removeTab(tabsheet.getTab(tabContent));
		            tabsheet.setSelectedTab(tab.getComponent());
		        }else{
		            tabsheet.removeTab(tabsheet.getTab(tabContent));
		            hideDetailsTabsheet();
		        }
			}
		});
		 
		closeAllTabsButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -2946008623293356900L;

			@Override
			public void buttonClick(ClickEvent event) {
				Util.closeAllTab(listDetailsTabSheet);
				listDetailsTabSheet.setVisible(false);
				closeAllTabsButton.setVisible(false);
			}
		});
		
        browseForListsButton.addListener(new Button.ClickListener() {

        	private static final long serialVersionUID = 6385074843600086746L;

			@Override
			public void buttonClick(final ClickEvent event) {
				openBrowseForListDialog();
			}
        });
	}

	protected void hideDetailsTabsheet() {
		closeAllTabsButton.setVisible(false);
		listDetailsTabSheet.setVisible(false);
	}

	@Override
	public void layoutComponents() {
		
		setSpacing(true);
		setWidth("460px");
		
		HeaderLabelLayout selectParentsHeaderLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_SELECT_PARENTS,selectParentsLabel);

		HorizontalLayout leftLayout = new HorizontalLayout();
		leftLayout.setSpacing(true);
		leftLayout.addComponent(browseForListsButton);
		leftLayout.addComponent(instructionForSelectParents);
		
		HorizontalLayout instructionForSelectParentsLayout = new HorizontalLayout(); 
		instructionForSelectParentsLayout.setWidth("100%");
		instructionForSelectParentsLayout.addComponent(leftLayout);
		instructionForSelectParentsLayout.addComponent(closeAllTabsButton);
		instructionForSelectParentsLayout.setComponentAlignment(leftLayout,Alignment.MIDDLE_LEFT);
		instructionForSelectParentsLayout.setComponentAlignment(closeAllTabsButton,Alignment.MIDDLE_RIGHT);
		
		addComponent(selectParentsHeaderLayout);
		addComponent(instructionForSelectParentsLayout);
		addComponent(listDetailsTabSheet);
	}
	
	public void selectListInTree(Integer id){
		listTreeComponent.setListId(id);
		listTreeComponent.createTree();
		listTreeComponent.setSelectedListId(id);
	}
	
	@Override
	public void studyClicked(GermplasmList list) {
		createListDetailsTab(list.getId(), list.getName());
	}
	
	public void openBrowseForListDialog(){
		listTreeComponent.showAddRenameFolderSection(false);
		launchListSelectionWindow(getWindow(), listTreeComponent, messageSource.getMessage(Message.BROWSE_FOR_LISTS));
	}
	
    private Window launchListSelectionWindow (final Window window, final Component content, final String caption) {

        final CssLayout layout = new CssLayout();
        layout.setMargin(true);
        layout.setWidth("100%");
        layout.setHeight("515px");

        layout.addComponent(content);
      
        
        final Window popupWindow = new Window();
        popupWindow.setWidth("900px");
        popupWindow.setHeight("575px");
        popupWindow.setModal(true);
        popupWindow.setResizable(false);
        popupWindow.center();
        popupWindow.setCaption(caption);
        popupWindow.setContent(layout);
        popupWindow.addStyleName(Reindeer.WINDOW_LIGHT);
        popupWindow.addStyleName("lm-list-manager-popup");
        popupWindow.setCloseShortcut(KeyCode.ESCAPE, null);
        popupWindow.setScrollable(false);
        
        window.addWindow(popupWindow);
        
        return popupWindow;
	}

    public void createListDetailsTab(Integer listId, String listName){
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
	    	Tab newTab = listDetailsTabSheet.addTab(new SelectParentsListDataComponent(listId, listName, source.getParentsComponent()), listName);
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
    
	public void updateUIForDeletedList(GermplasmList list){
		String listName = list.getName();
		for(int ctr = 0; ctr < listDetailsTabSheet.getComponentCount(); ctr++){
			Tab tab = listDetailsTabSheet.getTab(ctr);
			if(tab != null && tab.getCaption().equals(listName)){
				listDetailsTabSheet.removeTab(tab);
				return;
			}
		}
	}
	
	public void updateUIForRenamedList(GermplasmList list, String newName){
		Integer listId = list.getId();
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
	public void folderClicked(GermplasmList list) {
		// TODO Auto-generated method stub
		
	}
	
	// SETTERS AND GETTERS
	public TabSheet getListDetailsTabSheet(){
		return listDetailsTabSheet;
	}
	
	public CrossingManagerListTreeComponent getListTreeComponent(){
		return listTreeComponent;
	}

	@Override
	public void addListToFemaleList(Integer germplasmListId) {
		source.getParentsComponent().addListToFemaleTable(germplasmListId);
		
	}

	@Override
	public void addListToMaleList(Integer germplasmListId) {
		source.getParentsComponent().addListToMaleTable(germplasmListId);
	}

	public void updateViewForAllLists(ModeView modeView) {
		List<SelectParentsListDataComponent> selectParentComponents = new ArrayList<SelectParentsListDataComponent>();
		selectParentComponents.addAll(listStatusForChanges.keySet());
		
		if(modeView.equals(ModeView.LIST_VIEW)){
			for(SelectParentsListDataComponent selectParentComponent : selectParentComponents){
				selectParentComponent.changeToListView();
			}
		}
		else if(modeView.equals(ModeView.INVENTORY_VIEW)){
			for(SelectParentsListDataComponent selectParentComponent : selectParentComponents){
				selectParentComponent.viewInventoryActionConfirmed();
			}
		}
	}
	
	public Map<SelectParentsListDataComponent,Boolean> getListStatusForChanges(){
		return listStatusForChanges;
	}

	public void addUpdateListStatusForChanges(
			SelectParentsListDataComponent selectParentsListDataComponent,
			boolean hasChanges) {
		removeListStatusForChanges(selectParentsListDataComponent);
		listStatusForChanges.put(selectParentsListDataComponent, hasChanges);
		
		if(hasUnsavedChanges()){
			setHasUnsavedChangesMain(true);
		}
		else{
			setHasUnsavedChangesMain(false);
		}
	}
	
	public boolean hasUnsavedChanges() {
		List<Boolean> listOfStatus = new ArrayList<Boolean>();
		
		listOfStatus.addAll(listStatusForChanges.values());
		
		for(Boolean status: listOfStatus){
			if(status){
				return true;
			}
		}
		
		return false;
	}

	public void removeListStatusForChanges(SelectParentsListDataComponent selectParentsListDataComponent) {
		if(listStatusForChanges.containsKey(selectParentsListDataComponent)){
			listStatusForChanges.remove(selectParentsListDataComponent);
		}
	}

	@Override
	public void setHasUnsavedChangesMain(boolean hasChanges) {
		source.setHasUnsavedChangesMain(hasChanges);
	}

	public void updateHasChangesForAllList(boolean hasChanges) {
		List<SelectParentsListDataComponent> selectParentComponents = new ArrayList<SelectParentsListDataComponent>();
		selectParentComponents.addAll(listStatusForChanges.keySet());
		
		for(SelectParentsListDataComponent selectParentComponent : selectParentComponents){
			selectParentComponent.setHasUnsavedChanges(hasChanges);
		}
	}
}