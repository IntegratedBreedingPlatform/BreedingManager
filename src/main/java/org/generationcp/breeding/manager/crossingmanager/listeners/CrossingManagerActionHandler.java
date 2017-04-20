
package org.generationcp.breeding.manager.crossingmanager.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.crossingmanager.MakeCrossesParentsComponent;
import org.generationcp.breeding.manager.crossingmanager.MakeCrossesTableComponent;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.ui.Table;

public class CrossingManagerActionHandler implements Handler {

	private static final long serialVersionUID = 5470824414143199719L;

	private static final Action ACTION_SELECT_ALL = new Action("Select All");
	public static final Action ACTION_REMOVE_SELECTED_ENTRIES = new Action("Remove selected entries");
	private static final Action ACTION_DELETE_CROSSES = new Action("Delete selected crosses");
	private static final Action CLEAR_ALL_CROSSES = new Action("Clear All");
	private static final Action CLEAR_ALL_PARENTS = new Action("Clear All");
	private static final Action[] SELECT_LIST_ENTRIES = new Action[] {CrossingManagerActionHandler.ACTION_SELECT_ALL,
			CrossingManagerActionHandler.ACTION_REMOVE_SELECTED_ENTRIES, CrossingManagerActionHandler.CLEAR_ALL_PARENTS};
	private static final Action[] MAKE_CROSSES_ACTIONS = new Action[] {CrossingManagerActionHandler.ACTION_SELECT_ALL,
			CrossingManagerActionHandler.ACTION_DELETE_CROSSES, CrossingManagerActionHandler.CLEAR_ALL_CROSSES};

	private final Object source;

	public CrossingManagerActionHandler(final Object source) {
		super();
		this.source = source;
	}

	@Override
	public Action[] getActions(final Object target, final Object sender) {
		if (this.source instanceof MakeCrossesParentsComponent) {
			return CrossingManagerActionHandler.SELECT_LIST_ENTRIES;

		} else if (this.source instanceof MakeCrossesTableComponent) {
			return CrossingManagerActionHandler.MAKE_CROSSES_ACTIONS;
		}
		return new Action[0];
	}

	@Override
	public void handleAction(final Action action, final Object sender, final Object target) {
		if (CrossingManagerActionHandler.ACTION_SELECT_ALL.equals(action) && sender instanceof Table) {
			this.selectAllAction((Table) sender);
		} else if (CrossingManagerActionHandler.ACTION_REMOVE_SELECTED_ENTRIES.equals(action) && sender instanceof Table) {
			this.removeSelectedEntriesAction((Table) sender);
			if (this.source instanceof MakeCrossesParentsComponent) {
				final MakeCrossesParentsComponent makeCrosses = (MakeCrossesParentsComponent) this.source;
				makeCrosses.assignEntryNumber((Table) sender);
			}
		} else if (CrossingManagerActionHandler.ACTION_DELETE_CROSSES.equals(action)) {
			((MakeCrossesTableComponent) this.source).deleteCrossAction();
		} else if (CrossingManagerActionHandler.CLEAR_ALL_PARENTS.equals(action) && sender instanceof Table) {
			this.handleAction(CrossingManagerActionHandler.ACTION_SELECT_ALL, sender, target);
			this.handleAction(CrossingManagerActionHandler.ACTION_REMOVE_SELECTED_ENTRIES, sender, target);
		} else if (CrossingManagerActionHandler.CLEAR_ALL_CROSSES.equals(action) && sender instanceof Table) {
			this.handleAction(CrossingManagerActionHandler.ACTION_SELECT_ALL, sender, target);
			this.handleAction(CrossingManagerActionHandler.ACTION_DELETE_CROSSES, sender, target);
		}

	}

	// Select All rows in the table
	private void selectAllAction(final Table table) {
		final Collection<?> itemIds = table.getItemIds();
		if (itemIds != null && !itemIds.isEmpty()) {
			table.setValue(itemIds);
		}
	}

	@SuppressWarnings("unchecked")
	public void removeSelectedEntriesAction(final Table table) {

		final List<Object> itemsBeforeDelete = new ArrayList<Object>();
		itemsBeforeDelete.addAll(table.getItemIds());

		final List<Object> selectedItemIds = new ArrayList<Object>();
		selectedItemIds.addAll((Collection<? extends Integer>) table.getValue());

		if (table.getItemIds().size() == selectedItemIds.size()) {
			table.getContainerDataSource().removeAllItems();
		} else {
			for (final Object selectedItemId : selectedItemIds) {
				table.getContainerDataSource().removeItem(selectedItemId);
			}
		}

		// reset selection
		table.setValue(null);

		final List<Object> itemsLeftAfterDelete = new ArrayList<Object>();
		itemsLeftAfterDelete.addAll(table.getItemIds());

		if(this.source instanceof MakeCrossesParentsComponent) {
			// update the number of entries of male/female after delete
			if (((MakeCrossesParentsComponent) this.source).getFemaleTable().equals(table)) {
				((MakeCrossesParentsComponent) this.source).getFemaleParentTab().updateNoOfEntries(table.size());
				
			} else if (((MakeCrossesParentsComponent) this.source).getMaleTable().equals(table)) {
				((MakeCrossesParentsComponent) this.source).getMaleParentTab().updateNoOfEntries(table.size());
				
			}
		}
	}

	public Object getSource() {
		return this.source;
	}
}
