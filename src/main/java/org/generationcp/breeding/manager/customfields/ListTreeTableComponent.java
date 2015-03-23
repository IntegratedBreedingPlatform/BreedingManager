package org.generationcp.breeding.manager.customfields;


import com.vaadin.terminal.ThemeResource;
import org.generationcp.breeding.manager.customcomponent.GermplasmListTreeTable;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public abstract class ListTreeTableComponent extends ListSelectorComponent {

	private static final long serialVersionUID = -4025353842975688857L;

    private ThemeResource folderResource = new ThemeResource("images/folder-icon.png");
    private ThemeResource leafResource = new ThemeResource("images/document-icon.png");

    public ListTreeTableComponent(){
        selectListsFolderByDefault = false;
    }

    public ListTreeTableComponent(ListTreeActionsListener treeActionsListener){
    	this.treeActionsListener = treeActionsListener;
    	selectListsFolderByDefault = false;
    }

    public ListTreeTableComponent(ListTreeActionsListener treeActionsListener, Integer selectedListId){
    	this.treeActionsListener = treeActionsListener;
    	this.listId = selectedListId;
    	selectListsFolderByDefault = true;
    }

	/* #########################################################################
	 * START OF ABSTRACT / PROTECTED METHODS W/C CAN BE OVERRIDEN BY SUBCLASSES
	 * #########################################################################
	 */

    @Override
	public String getTreeStyleName(){
		return "tree-table-list";
	}
    @Override
    public String getMainTreeStyleName() {
        return "tree-table-list";
    }

    @Override
    public void instantiateGermplasmListSourceComponent(){
        setGermplasmListSource(new GermplasmListTreeTable());

        getGermplasmListSource().addContainerProperty(GermplasmListTreeTable.NAME_COL, String.class, "");
        getGermplasmListSource().addContainerProperty(GermplasmListTreeTable.OWNER_COL, String.class, "");
        getGermplasmListSource().addContainerProperty(GermplasmListTreeTable.DESCRIPTION_COL, String.class, "");
        getGermplasmListSource().addContainerProperty(GermplasmListTreeTable.LIST_TYPE_COL, String.class, "");
        getGermplasmListSource().addContainerProperty(GermplasmListTreeTable.NUMBER_OF_ENTRIES_COL, String.class, "");

        getGermplasmListSource().setSizeFull();

        getGermplasmListSource().setColumnWidth(GermplasmListTreeTable.NAME_COL, 200);
        getGermplasmListSource().setColumnExpandRatio(GermplasmListTreeTable.OWNER_COL, 1);
        getGermplasmListSource().setColumnExpandRatio(GermplasmListTreeTable.DESCRIPTION_COL, 1);
        getGermplasmListSource().setColumnExpandRatio(GermplasmListTreeTable.LIST_TYPE_COL, 1);
        getGermplasmListSource().setColumnWidth(GermplasmListTreeTable.NUMBER_OF_ENTRIES_COL, 80);
        getGermplasmListSource().setSelectable(true);

    }

    @Override
    public Object[] generateCellInfo(String name, String owner, String description, String listType, String numberOfEntries){
        Object[] cells = new Object[5];
        cells[0] = " " + name;
        cells[1] = owner != null ? owner : "";
        cells[2] = description != null ? description : "";
        cells[3] = listType != null ? listType : "";
        cells[4] = numberOfEntries != null ? numberOfEntries : "";
        return cells;
    }

    @Override
    public void setNodeItemIcon(Object itemId, boolean isFolder){
        if(isFolder){
            getGermplasmListSource().setItemIcon(itemId, folderResource);
        }else{
            getGermplasmListSource().setItemIcon(itemId, leafResource);
        }
    }
    
    @Override
    public void addListTreeItemDescription() {
    	//override so that list details is not shown upon hover on tree table
    }
}
