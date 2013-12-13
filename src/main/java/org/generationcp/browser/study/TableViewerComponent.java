/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package org.generationcp.browser.study;

import java.io.File;

import org.generationcp.browser.application.GermplasmStudyBrowserApplication;
import org.generationcp.browser.application.Message;
import org.generationcp.browser.study.listeners.StudyButtonClickListener;
import org.generationcp.browser.study.util.DatasetExporterException;
import org.generationcp.browser.study.util.TableViewerCellSelectorUtil;
import org.generationcp.browser.study.util.TableViewerExporter;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Layout.MarginInfo;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * This creates a Vaadin sub-window that displays the Table Viewer.
 * 
 * @author Mark Agarrado
 *
 */
@Configurable
public class TableViewerComponent extends Window implements InitializingBean, InternationalizableComponent {
	
	private static final long serialVersionUID = 477658402146083181L;
	public static final String TABLE_VIEWER_WINDOW_NAME = "table-viewer";
    public static final String EXPORT_EXCEL_BUTTON_ID = "Export Dataset to Excel";
	
    private TableViewerCellSelectorUtil tableViewerCellSelectorUtil;
	private TableViewerDatasetTable displayTable;
	private Button exportExcelButton;
	private String studyName;
	private VerticalLayout verticalLayout;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	public TableViewerComponent (TableViewerDatasetTable displayTable) {
		this.displayTable = displayTable;
        tableViewerCellSelectorUtil = new TableViewerCellSelectorUtil(this, displayTable);
	}

	public TableViewerComponent (TableViewerDatasetTable displayTable, String studyName) {
		this.displayTable = displayTable;
        tableViewerCellSelectorUtil = new TableViewerCellSelectorUtil(this, displayTable);
        this.studyName = studyName;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		assemble();
    }
    
    protected void assemble() {
        initializeComponents();
        initializeValues();
        initializeLayout();
        initializeActions();
    }
    
    protected void initializeComponents() {
        exportExcelButton = new Button(); // "Export to Fieldbook Excel File"
        exportExcelButton.setData(EXPORT_EXCEL_BUTTON_ID);
        exportExcelButton.addListener(new StudyButtonClickListener(this));
        exportExcelButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        verticalLayout = new VerticalLayout();
        
        verticalLayout.addComponent(exportExcelButton);
        verticalLayout.addComponent(displayTable);
        verticalLayout.setSpacing(true);
        
        addComponent(verticalLayout);
	}
    
    protected void initializeValues() {
    	
    }
    
    protected void initializeLayout() {
    	setName(TABLE_VIEWER_WINDOW_NAME);
    	setCaption(messageSource.getMessage(Message.TABLE_VIEWER_CAPTION));
    	setSizeFull();
        center();
        setResizable(true);
        setScrollable(true);
        setModal(true);
        
        // enable basic edit options on the specified table
        displayTable.setColumnCollapsingAllowed(true);
        displayTable.setColumnReorderingAllowed(true);
        displayTable.setPageLength(18); // display all rows of the table to the browser
        displayTable.setSizeFull(); // to make scrollbars appear on the Table component
        
        exportExcelButton.setCaption(messageSource.getMessage(Message.EXPORT_TO_EXCEL));
        
        verticalLayout.setMargin(new MarginInfo(true,true,true,true));
        
    }
    
    protected void initializeActions() {
    	//attach listener code here
    }
	
	@Override
	public void updateLabels() {
		
	}

    @SuppressWarnings("deprecation")
    public void exportToExcelAction() {
        
        String tempFilename = "TVDataset.xlsx";
        
        try {
            TableViewerExporter tableViewerExporter = new TableViewerExporter(displayTable, tableViewerCellSelectorUtil);
            
            tableViewerExporter.exportToExcel(tempFilename);
            FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFilename), this.getApplication());
            if(studyName!=null){
            	fileDownloadResource.setFilename("TVDataset_"+studyName.replace(" ", "_").trim()+".xlsx");
            } else {
            	fileDownloadResource.setFilename(tempFilename);
            }
            
            Window downloadWindow = new Window();
            downloadWindow.setWidth(0);
            downloadWindow.setHeight(0);
            downloadWindow.open(fileDownloadResource);
            this.getParent().getWindow().addWindow(downloadWindow);
            
            //TODO must figure out other way to clean-up file because deleting it here makes it unavailable for download
            //File tempFile = new File(tempFilename);
            //tempFile.delete();
            
        } catch (DatasetExporterException e) {
            MessageNotifier.showError(this.getApplication().getWindow(GermplasmStudyBrowserApplication.STUDY_WINDOW_NAME), e.getMessage(), "");
        } 
               
        
    }
	
}
