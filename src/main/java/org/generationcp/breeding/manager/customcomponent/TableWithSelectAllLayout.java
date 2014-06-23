package org.generationcp.breeding.manager.customcomponent;

import java.util.Collection;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

@Configurable
public class TableWithSelectAllLayout extends TableLayout implements BreedingManagerLayout {
	
	private static final long serialVersionUID = 5246715520145983375L;

	protected CheckBox selectAllCheckBox;
	protected final Object checkboxColumnId;
	protected Label dummyLabel;
	
	public TableWithSelectAllLayout(int recordCount, int maxRecords, Object checkboxColumnId){
		super(recordCount, maxRecords);
		this.checkboxColumnId = checkboxColumnId;
	}
	
	public TableWithSelectAllLayout(Object checkboxColumnId){
		super();
		this.checkboxColumnId = checkboxColumnId;
	}
	
	public TableWithSelectAllLayout(int recordCount, Object checkboxColumnId){
		super(recordCount);
		this.checkboxColumnId = checkboxColumnId;
	}

	@SuppressWarnings("unchecked")
	private void syncItemCheckBoxes(){
		Collection<Object> entries = (Collection<Object>) table.getItemIds();
		Collection<Object> selectedEntries = (Collection<Object>) table.getValue();
		if(selectedEntries.size() == entries.size() && selectedEntries.size() > 0){
			selectAllCheckBox.setValue(true);
		} else{
			selectAllCheckBox.setValue(false);
		}
		
		for(Object entry : entries){
			Property itemProperty = table.getItem(entry).getItemProperty(checkboxColumnId);
			if (itemProperty != null){
				CheckBox tag = (CheckBox) itemProperty.getValue();
				if(selectedEntries.contains(entry)){
					tag.setValue(true);
				} else{
					tag.setValue(false);
				}
			}
		}
    }
	
	@Override
	public Table getTable(){
		return this.table;
	}
	
	public CheckBox getCheckBox(){
		return this.selectAllCheckBox;
	}

	@Override
	public void instantiateComponents() {
		super.instantiateComponents();
		
		this.selectAllCheckBox = new CheckBox("Select All");
		this.selectAllCheckBox.setImmediate(true);
		
		// label is just for indenting the Select All checkbox to align with table checkboxes
		this.dummyLabel = new Label(); 
		dummyLabel.setWidth("7px");
	}

	@Override
	public void initializeValues() {
	}

	@Override
	public void addListeners() {
		super.addListeners();
		
		table.addListener(new Table.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(final com.vaadin.data.Property.ValueChangeEvent event) {
				syncItemCheckBoxes();
             }
         });
		
		this.selectAllCheckBox.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 7882379695058054587L;
			
			@SuppressWarnings("unchecked")
			@Override
			public void buttonClick(ClickEvent event) {
				boolean checkBoxValue = event.getButton().booleanValue();
				Collection<Object> entries = (Collection<Object>) table.getItemIds();
				for(Object entry : entries){
					CheckBox tag = (CheckBox) table.getItem(entry).getItemProperty(checkboxColumnId).getValue();
					tag.setValue(checkBoxValue);
				}
				if (checkBoxValue){
					table.setValue(entries);
				} else {
					table.setValue(null);
				}
			
			}
		});
	}

	@Override
	public void layoutComponents() {
		super.layoutComponents();
		
		HorizontalLayout layout = new HorizontalLayout();
		layout.addComponent(dummyLabel);
		layout.addComponent(selectAllCheckBox);
		
		selectAllCheckBox.addStyleName("lm-table-select-all");
		
		addComponent(layout);
	}

}
