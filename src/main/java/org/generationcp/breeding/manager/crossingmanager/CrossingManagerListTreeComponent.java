package org.generationcp.breeding.manager.crossingmanager;

import java.util.Collection;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.customfields.ListTreeComponent;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

@Configurable
public class CrossingManagerListTreeComponent extends ListTreeComponent {
	
	private static final long serialVersionUID = 8112173851252075693L;
	
	private static final Logger LOG = LoggerFactory.getLogger(CrossingManagerListTreeComponent.class);
	
	private Button addToFemaleListButton;
	private Button cancelButton;
	private Button addToMaleListButton;
	private Button openForReviewButton;
	private CrossingManagerMakeCrossesComponent source;

	public CrossingManagerListTreeComponent(
			ListTreeActionsListener treeActionsListener,
			CrossingManagerMakeCrossesComponent source) {
		super(treeActionsListener);
		this.source = source;
	}

	@Override
	public void addListeners() {
		
		super.addListeners();
	
		
		addToFemaleListButton.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = -3383724866291655410L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(source.getModeView().equals(ModeView.INVENTORY_VIEW)){
					showWarningInInventoryView();
				}
				else{
					Integer germplasmListId = (Integer) germplasmListTree.getValue();
					getTreeActionsListener().addListToFemaleList(germplasmListId);
					Window dialog = event.getComponent().getParent().getWindow();
					dialog.getParent().getWindow().removeWindow(dialog);
				}
			}
			
		});
		
		addToMaleListButton.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = -7685621731871659880L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(source.getModeView().equals(ModeView.INVENTORY_VIEW)){
					showWarningInInventoryView();
				}
				else{
					Integer germplasmListId = (Integer) germplasmListTree.getValue();
					getTreeActionsListener().addListToMaleList(germplasmListId);
					Window dialog = event.getComponent().getParent().getWindow();
					dialog.getParent().getWindow().removeWindow(dialog);
				}
			}
			
		});
		
		openForReviewButton.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 2103866815084444657L;

			@Override
			public void buttonClick(ClickEvent event) {
				String item = germplasmListTree.getValue().toString();
	        	
		        	if(!item.equals("CENTRAL") && !item.equals("LOCAL")){
		        		int germplasmListId = Integer.valueOf(item);
		                    try {
		                        treeItemClickAction(germplasmListId);
		                    } catch (InternationalizableException e) {
		                        LOG.error(e.toString() + "\n" + e.getStackTrace());
		                        e.printStackTrace();
		                        MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
		                    }
		        	} else{
		        		expandOrCollapseListTreeNode(item);
		        		getTreeActionsListener().folderClicked(null);

		        	}
		        	
		        	setSelectedListId(item);
	            	updateButtons(item);
	            	
	            	Window dialog = event.getComponent().getParent().getWindow();
					dialog.getParent().getWindow().removeWindow(dialog);
	            	
			}
			
		});
		
		cancelButton.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = -3708969669687499248L;

			@Override
			public void buttonClick(ClickEvent event) {
				Window dialog = event.getComponent().getParent().getWindow();
				dialog.getParent().getWindow().removeWindow(dialog);
			}
			
		});
		
		
		germplasmListTree.addListener(new ItemClickEvent.ItemClickListener() {
			private static final long serialVersionUID = -4428462607366745162L;

			@Override
			public void itemClick(ItemClickEvent event) {
				String item = event.getItemId().toString();
	        	
	        	if(!item.equals("CENTRAL") && !item.equals("LOCAL")){
	        		
	        		if (isFolder(item)){
	        			addToFemaleListButton.setEnabled(false);
		        		addToMaleListButton.setEnabled(false);
		        		openForReviewButton.setEnabled(false);
		        		
		        		expandOrCollapseListTreeNode(event.getItemId());
			        	getTreeActionsListener().folderClicked(null);
			        	
			        	CrossingManagerListTreeComponent.this.updateButtons(event.getItemId());
		        		
	        		}else{
	        			addToFemaleListButton.setEnabled(true);
		        		addToMaleListButton.setEnabled(true);
		        		openForReviewButton.setEnabled(true);
		        		
		        		CrossingManagerListTreeComponent.this.updateButtons(event.getItemId());
	        		}
	        		
	        	} else{
	        		addToFemaleListButton.setEnabled(false);
	        		addToMaleListButton.setEnabled(false);
	        		openForReviewButton.setEnabled(false);

		        	expandOrCollapseListTreeNode(event.getItemId());
		        	getTreeActionsListener().folderClicked(null);
		        	CrossingManagerListTreeComponent.this.updateButtons(event.getItemId());

	        	}
				
			}
		});
		
	}
	
	public void showWarningInInventoryView(){
		String message = "Please switch to list view first before adding entries to parent lists.";
    	MessageNotifier.showWarning(getWindow(),"Warning!", message);
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
		
		Collection<?> listeners =  germplasmListTree.getListeners(ItemClickEvent.class);
		for (Object l : listeners){
			germplasmListTree.removeListener(ItemClickEvent.class,l);

		}
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

}
