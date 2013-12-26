package org.generationcp.breeding.manager.listmanager.util;

import java.io.Serializable;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;
import com.vaadin.ui.Tree.TreeTargetDetails;

@Configurable
public class GermplasmListTreeUtil implements InternationalizableComponent, InitializingBean, Serializable {

	private static final long serialVersionUID = 1L;

	private Window parentWindow;
	private Tree targetTree;
	
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	public GermplasmListTreeUtil(Window window, Tree targetTree){
		this.parentWindow = window;
		this.targetTree = targetTree;
		setupTreeDragAndDropHandler();
	}
	
    public void setParent(Object sourceItemId, Object targetItemId){

		System.out.println("");
		System.out.println("Drop details");
		System.out.println("Source Item ID: "+sourceItemId);
		System.out.println("Target Item ID: "+targetItemId);
    	
    	if(sourceItemId.equals(ListManagerTreeComponent.LOCAL) || sourceItemId.equals(ListManagerTreeComponent.CENTRAL)){
    		if(parentWindow!=null){
	    		MessageNotifier.showWarning(parentWindow, 
	                    messageSource.getMessage(Message.ERROR_WITH_MODIFYING_LIST_TREE), 
	                    messageSource.getMessage(Message.UNABLE_TO_MOVE_ROOT_FOLDERS));
	    		return;
    		}
    	}
    	
    	if(targetItemId!=null && targetItemId.equals(ListManagerTreeComponent.CENTRAL)){
    		if(parentWindow!=null){
	    		MessageNotifier.showWarning(parentWindow, 
	                    messageSource.getMessage(Message.ERROR_WITH_MODIFYING_LIST_TREE), 
	                    messageSource.getMessage(Message.UNABLE_TO_MOVE_YOUR_LISTS_TO_PUBLIC_FOLDERS));
	    		return;
    		}
    	}
    	
    	Integer sourceId = null;
    	Integer targetId = null;
    	
    	if(sourceItemId!=null && !sourceItemId.equals(ListManagerTreeComponent.LOCAL) && !sourceItemId.equals(ListManagerTreeComponent.CENTRAL))
    		sourceId = Integer.valueOf(sourceItemId.toString());
    	if(targetItemId!=null && !targetItemId.equals(ListManagerTreeComponent.LOCAL) && !targetItemId.equals(ListManagerTreeComponent.CENTRAL))
    		targetId = Integer.valueOf(targetItemId.toString());
    	
		if(sourceId!=null && sourceId>0){
			if(parentWindow!=null){
				MessageNotifier.showWarning(parentWindow, 
						messageSource.getMessage(Message.ERROR_WITH_MODIFYING_LIST_TREE), 
						messageSource.getMessage(Message.UNABLE_TO_MOVE_PUBLIC_LISTS));
				return;
			}
		}    	
	
    	if(targetId!=null && targetId>0){
    		if(parentWindow!=null){
	    		MessageNotifier.showWarning(parentWindow, 
	                    messageSource.getMessage(Message.ERROR_WITH_MODIFYING_LIST_TREE), 
	                    messageSource.getMessage(Message.UNABLE_TO_MOVE_YOUR_LISTS_TO_PUBLIC_FOLDERS));
	    		return;
    		}
    	}    	
    	
    	
    	try {
			//studyDataManager.moveFolder(sourceId, targetId);
            GermplasmList sourceGermplasmList = germplasmListManager.getGermplasmListById(sourceId);
            if (targetId != null) {
                GermplasmList targetGermplasmList = germplasmListManager.getGermplasmListById(targetId);
                sourceGermplasmList.setParent(targetGermplasmList);
            } else {
            	sourceGermplasmList.setParent(null);
            }
            germplasmListManager.updateGermplasmList(sourceGermplasmList);
            if(targetItemId==null)
            	targetTree.setParent(sourceItemId, ListManagerTreeComponent.LOCAL);
            else
            	targetTree.setParent(sourceItemId, targetItemId);
		} catch (MiddlewareQueryException e) {
			if(parentWindow!=null){
				MessageNotifier.showError(parentWindow, 
	                    messageSource.getMessage(Message.ERROR_INTERNAL), 
	                    messageSource.getMessage(Message.ERROR_REPORT_TO));
				e.printStackTrace();
			}
		}
    }

    public void setupTreeDragAndDropHandler(){
		targetTree.setDropHandler(new DropHandler() {
			private static final long serialVersionUID = -6676297159926786216L;

			public void drop(DragAndDropEvent dropEvent) {
				System.out.println("Dropped!");
		        Transferable t = dropEvent.getTransferable();
		        if (t.getSourceComponent() != targetTree)
		            return;
		        
		        TreeTargetDetails target = (TreeTargetDetails) dropEvent.getTargetDetails();
		        
		        Object sourceItemId = t.getData("itemId");
		        Object targetItemId = target.getItemIdOver();
		        
		        VerticalDropLocation location = target.getDropLocation();
				
		        //HierarchicalContainer container = (HierarchicalContainer) germplasmListTree.getContainerDataSource();
				//Tree sourceTree = (Tree) t.getSourceComponent();
				
		        GermplasmList targetList = null;
		        try {
					targetList = germplasmListManager.getGermplasmListById((Integer) targetItemId);
				} catch (MiddlewareQueryException e) {
				} catch (ClassCastException e) {
				}
		        
		        if(location == VerticalDropLocation.MIDDLE && targetList==null){
					setParent(sourceItemId, targetItemId);		        	
		        } else if (location == VerticalDropLocation.MIDDLE && targetList.getType().equals("FOLDER")){
		            setParent(sourceItemId, targetItemId);
				} else {
					setParent(sourceItemId, targetList.getParentId());
				}
			}

			@Override
			public AcceptCriterion getAcceptCriterion() {
				return AcceptAll.get();
				//return SourceIsTarget.get();
			}
		});
    }
    
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
	
}
