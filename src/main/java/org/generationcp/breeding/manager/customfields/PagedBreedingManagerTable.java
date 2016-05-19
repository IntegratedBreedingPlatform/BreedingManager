package org.generationcp.breeding.manager.customfields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

import com.jensjansson.pagedtable.PagedTable;

@Configurable
public class PagedBreedingManagerTable extends PagedTable {
	public interface EntrySelectSyncHandler {
		void dispatch();
	}

	private EntrySelectSyncHandler entrySelectSyncHandler;


	private TableMultipleSelectionHandler tableMultipleSelectionHandler;
	private Integer pageLength;

	@Resource
	private SimpleResourceBundleMessageSource messageSource;

	public PagedBreedingManagerTable(int recordCount, int maxRecords) {
		super();

		pageLength = Math.min(recordCount, maxRecords);
		if (pageLength <= 0) {
			pageLength = 20;
		}

		this.setPageLength(pageLength);

		this.setTableHandler(new TableMultipleSelectionHandler(this));
	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		// clone the variables map into mutable hashmap
		// this fixes the vaadin issue that the page length is being modified when this method activates
		Map<String, Object> variablesCopy = new HashMap<>(variables);
		if (variablesCopy.containsKey("pagelength")) {
			variablesCopy.remove("pagelength");
		}

		// perform actual table.changeVariables
		this.doChangeVariables(source, variablesCopy);
	}

	/**
	 * This will just override the styles and look of the PagedTable paging controls
	 */
	@Override
	public HorizontalLayout createControls() {
		HorizontalLayout controls = super.createControls();
		updateItemsPerPageSelect(controls);
		updatePagingComponents(controls);

		return controls;
	}

	private void updatePagingComponents(HorizontalLayout controls) {
		Iterator<Component> iterator = ((HorizontalLayout) controls.getComponent(1)).getComponentIterator();
		while (iterator.hasNext()) {
			Component pagingComponent = iterator.next();

			if (pagingComponent instanceof Button) {
				pagingComponent.setStyleName("");
				((Button) pagingComponent).addListener(new Button.ClickListener() {

					@Override
					public void buttonClick(final Button.ClickEvent clickEvent) {
						PagedBreedingManagerTable.this.entrySelectSyncHandler.dispatch();
					}
				});
			}

			// this is the textbox in the paging components where you can change page by specifying page no.
			if (pagingComponent instanceof TextField) {
				final TextField pagingTextField = ((TextField) pagingComponent);
				// remove existing incompatible validator created by super.createControls()
				pagingTextField.removeAllValidators();
				pagingTextField.setImmediate(true);
				pagingTextField.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.EAGER);
				pagingTextField.setWidth("30px");

				// remove existing listener, since we need to provide custom behavior to it
				ValueChangeListener vcl = (ValueChangeListener) new ArrayList<>(pagingTextField.getListeners(Property.ValueChangeEvent.class)).get(0);
				pagingTextField.removeListener(vcl);

				// this new handle will set to last previous valid value when set to non numeric
				pagingTextField.addListener(new ValueChangeListener() {
					String lastValue = "1";
					@Override
					public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
						final String value = (String) pagingTextField.getValue();

						if (!StringUtils.isNumeric(value)) {
							pagingTextField.setValue(lastValue);
							return;
						}

						lastValue = value;
						PagedBreedingManagerTable.this.setCurrentPage(Integer.valueOf(value));
					}
				});
			}
		}
	}

	private void updateItemsPerPageSelect(HorizontalLayout controls) {
		final ComboBox newItemsPerPageSelect = new ComboBox();
		for (String item : new String[] {"5", "10", "15", "20", "25"}) {
			newItemsPerPageSelect.addItem(item);
		}

		newItemsPerPageSelect.setImmediate(true);
		newItemsPerPageSelect.setNullSelectionAllowed(false);
		newItemsPerPageSelect.setWidth("50px");
		newItemsPerPageSelect.select(String.valueOf(this.pageLength));
		this.setPageLength(this.pageLength);
		newItemsPerPageSelect.addListener(new ValueChangeListener() {

			@Override
			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				PagedBreedingManagerTable.this.setPageLength(Integer.valueOf(String.valueOf(valueChangeEvent.getProperty().getValue())));
				PagedBreedingManagerTable.this.entrySelectSyncHandler.dispatch();
			}
		});

		final ComboBox oldItemsPerPageSelect = ((ComboBox) ((HorizontalLayout) controls.getComponent(0)).getComponent(1));
		((HorizontalLayout) controls.getComponent(0)).replaceComponent(oldItemsPerPageSelect, newItemsPerPageSelect);

	}

	/**
	 * Set the instance of tableMultipleSelectionHandler
	 */
	void setTableHandler(TableMultipleSelectionHandler tableMultipleSelectionHandler) {

		// remove this tables listener if exists and replace it with the new handler
		this.removeListener(this.tableMultipleSelectionHandler);
		this.removeShortcutListener(this.tableMultipleSelectionHandler);

		// set this table's current handler
		this.tableMultipleSelectionHandler = tableMultipleSelectionHandler;

		// add the new handler as this table's listener
		this.addListener(this.tableMultipleSelectionHandler);
		this.addShortcutListener(this.tableMultipleSelectionHandler);
	}

	/**
	 * Perform actual changeVariables
	 *
	 * @param source
	 * @param variablesCopy
	 */
	void doChangeVariables(Object source, Map<String, Object> variablesCopy) {
		super.changeVariables(source, variablesCopy);
		tableMultipleSelectionHandler.setValueForSelectedItems();
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void registerTableSelectHandler(final EntrySelectSyncHandler handler) {
		this.entrySelectSyncHandler = handler;
	}

}
