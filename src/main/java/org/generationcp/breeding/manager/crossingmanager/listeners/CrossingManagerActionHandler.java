package org.generationcp.breeding.manager.crossingmanager.listeners;

import java.util.Collection;

import org.generationcp.breeding.manager.crossingmanager.MakeCrossesParentsComponent;
import org.generationcp.breeding.manager.crossingmanager.MakeCrossesTableComponent;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.ui.Table;

/**
 * Action handler (context menu options) for Crossing Manager tables
 * 
 * @author Darla Ani
 *
 */
public class CrossingManagerActionHandler implements Handler {

	private static final long serialVersionUID = 5470824414143199719L;
	
	private static final Action ACTION_SELECT_ALL = new Action("Select All");
	private static final Action ACTION_DELETE_CROSSES = new Action("Delete selected crosses");
	private static final Action[] SELECT_LIST_ENTRIES = new Action[] {ACTION_SELECT_ALL};
	private static final Action[] MAKE_CROSSES_ACTIONS = new Action[] {ACTION_SELECT_ALL, ACTION_DELETE_CROSSES};

	private Object source;
	
	public CrossingManagerActionHandler(Object source) {
		super();
		this.source = source;
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		if (source instanceof MakeCrossesParentsComponent ){
			return SELECT_LIST_ENTRIES;
			
		} else if (source instanceof MakeCrossesTableComponent){
			return MAKE_CROSSES_ACTIONS;
		}
		return null;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (ACTION_SELECT_ALL.equals(action) && sender instanceof Table){
			selectAllAction((Table) sender);
            
		} else if (ACTION_DELETE_CROSSES.equals(action)) {
			((MakeCrossesTableComponent) source).deleteCrossAction();
		}

	}

	// Select All rows in the table
	private void selectAllAction(Table table) {
		Collection<?> itemIds = table.getItemIds();
		if (itemIds != null && !itemIds.isEmpty()){
			table.setValue(itemIds);
//			table.setPageLength(0);
		}
	}

}
