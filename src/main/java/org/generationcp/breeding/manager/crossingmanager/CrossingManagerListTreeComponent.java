package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerTreeActionsListener;
import org.generationcp.breeding.manager.customfields.ListTreeComponent;
import org.generationcp.breeding.manager.listmanager.util.InventoryTableDropHandler;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

@Configurable
public class CrossingManagerListTreeComponent extends ListTreeComponent {
	
	private static final long serialVersionUID = 8112173851252075693L;
	
	private Button addToFemaleListButton;
	private Button cancelButton;
	private Button addToMaleListButton;
	private Button openForReviewButton;
	private CrossingManagerMakeCrossesComponent source;
	private CrossingManagerTreeActionsListener crossingTreeActionsListener;

	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	@Autowired
	private InventoryDataManager inventoryDataManager;	
	
	public CrossingManagerListTreeComponent(
			CrossingManagerTreeActionsListener treeActionsListener,
			CrossingManagerMakeCrossesComponent source) {
		super(treeActionsListener);
		this.crossingTreeActionsListener = treeActionsListener;
		this.source = source;
	}

	@Override
	public void addListeners() {
		
		super.addListeners();
	
		
		addToFemaleListButton.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = -3383724866291655410L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				Integer germplasmListId = (Integer) germplasmListTree.getValue();
				
				if(source.getModeView().equals(ModeView.INVENTORY_VIEW)){
					//showWarningInInventoryView();
					
					if(crossingTreeActionsListener instanceof SelectParentsComponent){
						MakeCrossesParentsComponent parentsComponent = ((SelectParentsComponent) crossingTreeActionsListener).getCrossingManagerMakeCrossesComponent().getParentsComponent();
						InventoryTableDropHandler inventoryTableDropHandler = new InventoryTableDropHandler(source.getParentsComponent().getFemaleParentTab(), germplasmDataManager, germplasmListManager, inventoryDataManager, parentsComponent.getFemaleParentTab().getListInventoryTable().getTable());
						inventoryTableDropHandler.addGermplasmListInventoryData(germplasmListId);
						
						if(parentsComponent.getFemaleTable().getItemIds().size()==0){
							crossingTreeActionsListener.addListToFemaleList(germplasmListId);
						} else {
							source.getParentsComponent().getFemaleParentTab().setHasUnsavedChanges(true);
						}
						source.getParentsComponent().getParentTabSheet().setSelectedTab(0);
					
					}
					
					closeTreeWindow(event);
				}
				else{
					crossingTreeActionsListener.addListToFemaleList(germplasmListId);
					closeTreeWindow(event);
				}
			}

		});
		
		addToMaleListButton.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = -7685621731871659880L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				Integer germplasmListId = (Integer) germplasmListTree.getValue();
				
				if(source.getModeView().equals(ModeView.INVENTORY_VIEW)){
					//showWarningInInventoryView();
					
					if(crossingTreeActionsListener instanceof SelectParentsComponent){
						MakeCrossesParentsComponent parentsComponent = ((SelectParentsComponent) crossingTreeActionsListener).getCrossingManagerMakeCrossesComponent().getParentsComponent();
						InventoryTableDropHandler inventoryTableDropHandler = new InventoryTableDropHandler(source.getParentsComponent().getMaleParentTab(), germplasmDataManager, germplasmListManager, inventoryDataManager, parentsComponent.getMaleParentTab().getListInventoryTable().getTable());
						inventoryTableDropHandler.addGermplasmListInventoryData(germplasmListId);
						
						if(parentsComponent.getMaleTable().getItemIds().size()==0){
							crossingTreeActionsListener.addListToMaleList(germplasmListId);
						} else {
							source.getParentsComponent().getMaleParentTab().setHasUnsavedChanges(true);
						}
						source.getParentsComponent().getParentTabSheet().setSelectedTab(1);
					}
					
					closeTreeWindow(event);
				}
				else{
					crossingTreeActionsListener.addListToMaleList(germplasmListId);
					closeTreeWindow(event);
				}
			}
			
		});
		
		openForReviewButton.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 2103866815084444657L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (germplasmList != null){
					getTreeActionsListener().studyClicked(germplasmList);
					closeTreeWindow(event);
				}
	            	
			}
			
		});
		
		cancelButton.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = -3708969669687499248L;

			@Override
			public void buttonClick(ClickEvent event) {
				closeTreeWindow(event);
			}
			
		});
		
	}
	
	protected void closeTreeWindow(ClickEvent event) {
		Window dialog = event.getComponent().getParent().getWindow();
		dialog.getParent().getWindow().removeWindow(dialog);
	}
	
	public void showWarningInInventoryView(){
		String message = "Please switch to list view first before adding entries to parent lists.";
    	MessageNotifier.showError(getWindow(),"Warning!", message);
	}

	@Override
	public void layoutComponents() {
		
		super.layoutComponents();
		
		HorizontalLayout actionButtonsLayout = new HorizontalLayout();
		actionButtonsLayout.setSpacing(true);
		actionButtonsLayout.setStyleName("align-center");
		actionButtonsLayout.setMargin(true, false, false, false);
		

		actionButtonsLayout.addComponent(cancelButton);
		actionButtonsLayout.addComponent(addToFemaleListButton);
		actionButtonsLayout.addComponent(addToMaleListButton);
		actionButtonsLayout.addComponent(openForReviewButton);
	
		addComponent(actionButtonsLayout);
		
		
	}

	@Override
	public void instantiateComponents() {
		
		super.instantiateComponents();
		
		addToFemaleListButton = new Button();
		addToFemaleListButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		addToFemaleListButton.setCaption(messageSource.getMessage(Message.DIALOG_ADD_TO_FEMALE_LABEL));
		addToFemaleListButton.setEnabled(false);
		
		addToMaleListButton = new Button();
		addToMaleListButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		addToMaleListButton.setCaption(messageSource.getMessage(Message.DIALOG_ADD_TO_MALE_LABEL));
		addToMaleListButton.setEnabled(false);
		
		openForReviewButton = new Button();
		openForReviewButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		openForReviewButton.setCaption(messageSource.getMessage(Message.DIALOG_OPEN_FOR_REVIEW_LABEL));
		openForReviewButton.setEnabled(false);
		
		cancelButton = new Button();
		cancelButton.setStyleName(Bootstrap.Buttons.DEFAULT.styleName());
		cancelButton.setCaption(messageSource.getMessage(Message.CANCEL));
		
	}

	@Override
	protected boolean doIncludeActionsButtons() {
		return true;
	}


	@Override
	protected boolean doIncludeRefreshButton() {
		return false;
	}

	@Override
	protected boolean isTreeItemsDraggable() {
		return true;
	}

	@Override
	protected boolean doIncludeCentralLists() {
		return true;
	}

	@Override
	protected boolean doShowFoldersOnly() {
		return false;
	}
	
	@Override
	protected String getTreeStyleName() {
		return "crossingManagerTree";
	}
	
	@Override
	public void refreshRemoteTree(){
	}
	
	@Override
	public void studyClickedAction(GermplasmList germplasmList) {
		toggleListSelectionButtons(true);
	}
	
	@Override
	public void folderClickedAction(GermplasmList germplasmList) {
		toggleListSelectionButtons(false);
	}
	
	private void toggleListSelectionButtons(boolean enabled){
		addToFemaleListButton.setEnabled(enabled);
		addToMaleListButton.setEnabled(enabled);
		openForReviewButton.setEnabled(enabled);
	}
	

}
