package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.customfields.BreedingManagerTable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class TableLayout extends VerticalLayout implements
		BreedingManagerLayout, InitializingBean {

	private static final long serialVersionUID = -6261586644242232751L;

	protected BreedingManagerTable table;
	private Label emptyTableLabel = new Label("No information retrieved.");
	
	private int recordCount = 0;
	private int maxRecords = 0;
	
	public TableLayout(int recordCount, int maxRecords){
		super();
		this.recordCount = recordCount;
		this.maxRecords = maxRecords;
	}
	
	public TableLayout(int recordCount){
		super();
		this.recordCount = recordCount;
		this.maxRecords = recordCount;
	}
	
	public TableLayout(){
	}
	
	private void setup() {
		instantiateComponents();
		addListeners();
		layoutComponents();
	}
	
	@Override
	public void instantiateComponents() {
		this.table = new BreedingManagerTable(recordCount, maxRecords);
		this.table.setImmediate(true);
	}

	@Override
	public void initializeValues() {

	}

	@Override
	public void addListeners() {

	}

	@Override
	public void layoutComponents() {
		setSpacing(true);
		if (!(doHideEmptyTable() && this.recordCount == 0)){
			addComponent(table);
		} else {
			addComponent(emptyTableLabel);
		}

	}
	
	protected boolean doHideEmptyTable(){
		return false;
	}
	
	public Table getTable(){
		return this.table;
	}
	
	public void setEmptyTableMessage(String message){
		emptyTableLabel.setValue(message);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		setup();
		
	}

}
