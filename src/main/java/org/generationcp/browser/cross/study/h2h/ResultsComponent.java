package org.generationcp.browser.cross.study.h2h;

import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

@Configurable
public class ResultsComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 2305982279660448571L;
	
	private static final String TRAIT_COLUMN_ID = "ResultsComponent Trait Column ID";
	private static final String NUM_OF_ENV_COLUMN_ID = "ResultsComponent Num Of Env Column ID";
	private static final String NUM_SUP_COLUMN_ID = "ResultsComponent Num Sup Column ID";
	private static final String MEAN_TEST_COLUMN_ID = "ResultsComponent Mean Test Column ID";
	private static final String MEAN_STD_COLUMN_ID = "ResultsComponent Mean STD Column ID";
	private static final String MEAN_DIFF_COLUMN_ID = "ResultsComponent Mean Diff Column ID";
	private static final String PVAL_COLUMN_ID = "ResultsComponent Pval Column ID";

	private Table resultsTable;
	
	private Label testEntryLabel;
	private Label standardEntryLabel;
	private Label testEntryNameLabel;
	private Label standardEntryNameLabel;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		setHeight("550px");
        setWidth("1000px");
        
		testEntryLabel = new Label("<b>Test Entry:</b>");
		testEntryLabel.setContentMode(Label.CONTENT_XHTML);
		addComponent(testEntryLabel, "top:20px;left:30px");
		
		testEntryNameLabel = new Label("IR 71692-45-2-2-1");
		addComponent(testEntryNameLabel, "top:20px;left:120px");
		
		standardEntryLabel = new Label("<b>Standard Entry:</b>");
		standardEntryLabel.setContentMode(Label.CONTENT_XHTML);
		addComponent(standardEntryLabel, "top:20px;left:450px");
		
		standardEntryNameLabel = new Label("IR 64");
		addComponent(standardEntryNameLabel, "top:20px;left:570px");
		
		resultsTable = new Table();
		resultsTable.setWidth("800px");
		resultsTable.setHeight("400px");
		resultsTable.setImmediate(true);
		resultsTable.setColumnCollapsingAllowed(true);
		resultsTable.setColumnReorderingAllowed(true);
		
		resultsTable.addContainerProperty(TRAIT_COLUMN_ID, String.class, null);
		resultsTable.addContainerProperty(NUM_OF_ENV_COLUMN_ID, Integer.class, null);
		resultsTable.addContainerProperty(NUM_SUP_COLUMN_ID, Integer.class, null);
		resultsTable.addContainerProperty(MEAN_STD_COLUMN_ID, Double.class, null);
		resultsTable.addContainerProperty(MEAN_TEST_COLUMN_ID, Double.class, null);
		resultsTable.addContainerProperty(MEAN_DIFF_COLUMN_ID, Double.class, null);
		resultsTable.addContainerProperty(PVAL_COLUMN_ID, Double.class, null);
		
		resultsTable.setColumnHeader(TRAIT_COLUMN_ID, "TRAIT");
		resultsTable.setColumnHeader(NUM_OF_ENV_COLUMN_ID, "# OF ENV");
		resultsTable.setColumnHeader(NUM_SUP_COLUMN_ID, "# SUP");
		resultsTable.setColumnHeader(MEAN_STD_COLUMN_ID, "MEAN STD");
		resultsTable.setColumnHeader(MEAN_TEST_COLUMN_ID, "MEAN TEST");
		resultsTable.setColumnHeader(MEAN_DIFF_COLUMN_ID, "MEAN DIFF");
		resultsTable.setColumnHeader(PVAL_COLUMN_ID, "PVAL");
		
		addComponent(resultsTable, "top:70px;left:30px");
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
}
