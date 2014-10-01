package org.generationcp.breeding.manager.customfields;


import org.generationcp.breeding.manager.customcomponent.GermplasmListTree;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public abstract class ListTreeComponent extends ListSelectorComponent {
	
	private static final long serialVersionUID = -4025353842975688857L;

    public ListTreeComponent(Integer selectListId){
    	this.listId = selectListId;
    	selectProgramListsByDefault = false;
    }

    public ListTreeComponent(ListTreeActionsListener treeActionsListener){
    	this.treeActionsListener = treeActionsListener;
    	selectProgramListsByDefault = false;
    }
    
    public ListTreeComponent(ListTreeActionsListener treeActionsListener, Integer selectedListId){
    	this.treeActionsListener = treeActionsListener;
    	this.listId = selectedListId;
    	selectProgramListsByDefault = true;
    }

    @Override
    public String getTreeStyleName(){
        return "listTree";
    }

    @Override
    public String getMainTreeStyleName() {
        return "listTree";
    }

    @Override
    public void instantiateGermplasmListSourceComponent(){
        setGermplasmListSource( new GermplasmListTree());
    }

    @Override
    public Object[] generateCellInfo(String name, String owner, String description, String listType, String numberOfEntries){
        //just return an empty array, due to sonar inspection warning
        return new Object[1];
    }
    @Override
    public void setNodeItemIcon(Object itemId, boolean isFolder){

    }
}
