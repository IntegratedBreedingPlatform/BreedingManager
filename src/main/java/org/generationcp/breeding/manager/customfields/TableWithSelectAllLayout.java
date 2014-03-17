package org.generationcp.breeding.manager.customfields;

import java.util.Collection;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class TableWithSelectAllLayout extends VerticalLayout implements BreedingManagerLayout{
	
	private static final long serialVersionUID = 5246715520145983375L;

	private CheckBox selectAllCheckBox;
	private BreedingManagerTable table;
	private Object checkboxColumnId;
	private Label dummyLabel;
	
	private int recordCount = 0;
	private int maxRecords = 0;
	
	public TableWithSelectAllLayout(int recordCount, int maxRecords, Object checkboxColumnId){
		super();
		this.recordCount = recordCount;
		this.maxRecords = maxRecords;
		this.checkboxColumnId = checkboxColumnId;
		
		setup();
	}
	
	public TableWithSelectAllLayout(Object checkboxColumnId){
		super();
		this.checkboxColumnId = checkboxColumnId;
		
		setup();
	}

	private void setup() {
		instantiateComponents();
		addListeners();
		layoutComponents();
	}
	
	@SuppressWarnings("unchecked")
	private void syncItemCheckBoxes(){
		Collection<Object> entries = (Collection<Object>) table.getItemIds();
		Collection<Object> selectedEntries = (Collection<Object>) table.getValue();
		if(selectedEntries.size() == entries.size()){
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
	 
	public Table getTable(){
		return this.table;
	}
	
	public CheckBox getCheckBox(){
		return this.selectAllCheckBox;
	}

	@Override
	public void instantiateComponents() {
		this.table = new BreedingManagerTable(recordCount, maxRecords);
		this.table.setImmediate(true);
		
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
		this.table.addListener(new Table.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(final com.vaadin.data.Property.ValueChangeEvent event) {
				@SuppressWarnings("unchecked")
				Collection<Object> value = (Collection<Object>) event.getProperty().getValue();
				if (!value.isEmpty()){
					syncItemCheckBoxes();
				}
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
		setSpacing(true);
		
		addComponent(table);
		
		HorizontalLayout layout = new HorizontalLayout();
		layout.addComponent(dummyLabel);
		layout.addComponent(selectAllCheckBox);
		
		addComponent(layout);
	}

}
