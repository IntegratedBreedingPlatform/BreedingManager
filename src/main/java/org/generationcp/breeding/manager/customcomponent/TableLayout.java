
package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.customfields.BreedingManagerTable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

@Configurable
public class TableLayout extends CssLayout implements BreedingManagerLayout, InitializingBean {

	private static final long serialVersionUID = -6261586644242232751L;

	protected ControllableRefreshTable table;
	
	final Label emptyTableLabel = new Label("No information retrieved.");

	int recordCount = 0;
	int maxRecords = 0;

	public TableLayout(int recordCount, int maxRecords) {
		super();
		this.recordCount = recordCount;
		this.maxRecords = maxRecords;
		emptyTableLabel.setDebugId("emptyTableLabel");

	}

	public TableLayout(int recordCount) {
		super();
		this.recordCount = recordCount;
		this.maxRecords = recordCount;
		emptyTableLabel.setDebugId("emptyTableLabel");

	}

	public TableLayout() {
		emptyTableLabel.setDebugId("emptyTableLabel");
	}

	private void setup() {
		this.instantiateComponents();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		this.table = new BreedingManagerTable(this.recordCount, this.maxRecords);
		this.table.setDebugId("table");
		this.table.setImmediate(true);
	}

	@Override
	public void initializeValues() {
		// not implemented
	}

	@Override
	public void addListeners() {
		// not implemented
	}

	@Override
	public void layoutComponents() {
		this.setWidth("100%");
		if (!(this.doHideEmptyTable() && this.recordCount == 0)) {
			this.addComponent(this.table);
		} else {
			this.addComponent(this.emptyTableLabel);
		}

	}

	protected boolean doHideEmptyTable() {
		return false;
	}

	public Table getTable() {
		return this.table;
	}

	public void setEmptyTableMessage(String message) {
		this.emptyTableLabel.setValue(message);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setup();

	}

}
