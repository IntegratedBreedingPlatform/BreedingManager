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
import java.util.ArrayList;
import java.util.List;

import org.generationcp.browser.application.GermplasmStudyBrowserApplication;
import org.generationcp.browser.application.Message;
import org.generationcp.browser.study.containers.RepresentationDatasetQueryFactory;
import org.generationcp.browser.study.listeners.StudyButtonClickListener;
import org.generationcp.browser.study.util.DatasetExporter;
import org.generationcp.browser.study.util.DatasetExporterException;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.VariableType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.StudyDataManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.addon.tableexport.CsvExport;
import com.vaadin.addon.tableexport.TableExport;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
//import org.generationcp.middleware.domain.dms.TermId;

/**
 * This class creates the Vaadin Table where a dataset can be displayed.
 * 
 * @author Kevin Manansala
 * 
 */
@Configurable
public class RepresentationDatasetComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

    private final static Logger LOG = LoggerFactory.getLogger(RepresentationDatasetComponent.class);
    private static final long serialVersionUID = -8476739652987572690L;
    
    public static final String EXPORT_CSV_BUTTON_ID = "RepresentationDatasetComponent Export CSV Button";
    public static final String EXPORT_EXCEL_BUTTON_ID = "RepresentationDatasetComponent Export to FieldBook Excel File Button";
    public static final String OPEN_TABLE_VIEWER_BUTTON_ID = "RepresentationDatasetComponent Open Table Viewer Button";

    private Table datasetTable;
    private String reportName;
    private Integer studyIdHolder;
    private Integer datasetId;

    private Button exportCsvButton;
    private Button exportExcelButton;
    private Button openTableViewerButton;
    private StringBuffer reportTitle;
    
    private StudyDataManagerImpl studyDataManager;
    
    private boolean fromUrl;                //this is true if this component is created by accessing the Study Details page directly from the URL

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public RepresentationDatasetComponent(StudyDataManagerImpl studyDataManager,
            Integer datasetId, String datasetTitle, Integer studyId, boolean fromUrl) {
        this.reportName = datasetTitle;
        this.studyIdHolder = studyId;
        this.datasetId = datasetId;
        this.studyDataManager = studyDataManager;
        this.fromUrl = fromUrl;
    }

    // Called by StudyButtonClickListener
    public void exportToCSVAction() {
        CsvExport csvExport;
        // reportTitle = "Dataset-Study[" + studyIdHolder + "]-Rep[" + repIdHolder + "]";
        reportTitle = new StringBuffer().append(messageSource.getMessage(Message.REPORT_TITLE1_TEXT)).append("[").append(studyIdHolder)
                   .append("]-").append(messageSource.getMessage(Message.REPORT_TITLE2_TEXT)).append("[").append(datasetId).append("]-");
        
        StringBuffer fileName = new StringBuffer();
        
        fileName = reportTitle.append(".csv");
        
        csvExport = new CsvExport(datasetTable, reportName, reportTitle.toString(), fileName.toString(), false);
        csvExport.excludeCollapsedColumns();
        csvExport.setMimeType(TableExport.CSV_MIME_TYPE);
        csvExport.export();
    }
    
    // Called by StudyButtonClickListener
  
    @SuppressWarnings("deprecation")
    public void exportToExcelAction() {
        
        String tempFilename = "dataset-temp.xls";
        
        DatasetExporter datasetExporter;
        datasetExporter = new DatasetExporter(studyDataManager, studyIdHolder, datasetId);
        try {
            datasetExporter.exportToFieldBookExcelUsingIBDBv2(tempFilename);
            FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFilename), this.getApplication());
            fileDownloadResource.setFilename("export.xls");
            
            Window downloadWindow = new Window();
            downloadWindow.setWidth(0);
            downloadWindow.setHeight(0);
            downloadWindow.open(fileDownloadResource);
            this.getWindow().addWindow(downloadWindow);
            
            //TODO must figure out other way to clean-up file because deleting it here makes it unavailable for download
            //File tempFile = new File(tempFilename);
            //tempFile.delete();
        } catch (DatasetExporterException e) {
            MessageNotifier.showError(this.getApplication().getWindow(GermplasmStudyBrowserApplication.STUDY_WINDOW_NAME), e.getMessage(), "");
        }        
        
    }
    
    // Called by StudyButtonClickListener
    public void openTableViewerAction() {
        try {
            long expCount = studyDataManager.countExperiments(datasetId);
            if (expCount > 500) {
                //ask confirmation from user for generating large datasets           
                String confirmDialogCaption=messageSource.getMessage(Message.TABLE_VIEWER_CAPTION);
                String confirmDialogMessage=messageSource.getMessage(Message.CONFIRM_DIALOG_MESSAGE_OPEN_TABLE_VIEWER); 

                ConfirmDialog.show(this.getWindow(),confirmDialogCaption ,confirmDialogMessage ,
                    messageSource.getMessage(Message.TABLE_VIEWER_OK_LABEL), messageSource.getMessage(Message.CANCEL_LABEL), new ConfirmDialog.Listener() {
                    private static final long serialVersionUID = 1L;

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            openTableViewer();
                        }
                    }
                });
            } else {
                openTableViewer();
            }
        }  catch (MiddlewareQueryException ex) {
            LOG.error("Error with getting experiments for dataset: " + datasetId + "\n" + ex.toString());
        }
    }
    
    private void openTableViewer() {
    	Window mainWindow = this.getWindow();
    	TableViewerDatasetTable tableViewerDataset = new TableViewerDatasetTable(studyDataManager, studyIdHolder, datasetId);
    	String studyName;
		try {
			studyName = studyDataManager.getStudy(studyIdHolder).getName();
			Window tableViewer = new TableViewerComponent(tableViewerDataset,studyName);
			mainWindow.addWindow(tableViewer);
		} catch (MiddlewareQueryException e) {
			Window tableViewer = new TableViewerComponent(tableViewerDataset);
			mainWindow.addWindow(tableViewer);
		}
    }
    
    
    @Override
    public void afterPropertiesSet() throws Exception{
        
        datasetTable = generateLazyDatasetTable(false);
        
        setMargin(true);
        setSpacing(true);
        addComponent(datasetTable);
        setData(this.reportName);
        
        exportCsvButton = new Button(); // "Export to CSV"
        exportCsvButton.setData(EXPORT_CSV_BUTTON_ID);
        exportCsvButton.addListener(new StudyButtonClickListener(this));

        exportExcelButton = new Button(); // "Export to Fieldbook Excel File"
        exportExcelButton.setData(EXPORT_EXCEL_BUTTON_ID);
        exportExcelButton.addListener(new StudyButtonClickListener(this));
        
        openTableViewerButton = new Button();
        openTableViewerButton.setData(OPEN_TABLE_VIEWER_BUTTON_ID);
        openTableViewerButton.addListener(new StudyButtonClickListener(this));
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        //TODO uncomment this when the feature of exporting to CSV is working properly
        //buttonLayout.addComponent(exportCsvButton);
        //only show Fieldbook Export to Excel button if study page not accessed directly from URL
        if (!fromUrl) {
            buttonLayout.addComponent(exportExcelButton);
            buttonLayout.addComponent(openTableViewerButton);
        }

        addComponent(buttonLayout);
    }
    
    private Table generateLazyDatasetTable(boolean fromUrl) {
    	// set the column header ids
        List<VariableType> variables = new ArrayList<VariableType>();
        List<String> columnIds = new ArrayList<String>();

        try {
            DataSet dataset = studyDataManager.getDataSet(datasetId);
            variables = dataset.getVariableTypes().getVariableTypes();
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in getting variables of dataset: "
                            + datasetId + "\n" + e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            variables = new ArrayList<VariableType>();
            if (getWindow() != null) {
                MessageNotifier.showWarning(getWindow(), 
                        messageSource.getMessage(Message.ERROR_DATABASE), 
                        messageSource.getMessage(Message.ERROR_IN_GETTING_VARIABLES_OF_DATASET)  + " " + datasetId); 
            }
        }
        
        for(VariableType variable : variables)
        {
            if(variable.getStandardVariable().getStoredIn().getId() != TermId.STUDY_INFORMATION.getId()){
                String columnId = new StringBuffer().append(variable.getId()).append("-").append(variable.getLocalName()).toString();
                columnIds.add(columnId);
            }
        }

        // create item container for dataset table
        RepresentationDatasetQueryFactory factory = new RepresentationDatasetQueryFactory(studyDataManager, datasetId, columnIds, fromUrl);
        LazyQueryContainer datasetContainer = new LazyQueryContainer(factory, false, 50);

        // add the column ids to the LazyQueryContainer tells the container the columns to display for the Table
        for (String columnId : columnIds) {
            if (columnId.contains("GID") && !fromUrl) {
                datasetContainer.addContainerProperty(columnId, Link.class, null);
            } else {
                datasetContainer.addContainerProperty(columnId, String.class, null);
            }
        }

        datasetContainer.getQueryView().getItem(0); // initialize the first batch of data to be displayed

        // create the Vaadin Table to display the dataset, pass the container object created
        Table datasetTable = new Table("", datasetContainer);
        datasetTable.setColumnCollapsingAllowed(true);
        datasetTable.setColumnReorderingAllowed(true);
        datasetTable.setPageLength(15); // number of rows to display in the Table
        datasetTable.setSizeFull(); // to make scrollbars appear on the Table component

        // Use cell selector utility on datasetTable
        //TableViewerCellSelectorUtil tableViewerCellSelectorUtil = new TableViewerCellSelectorUtil(this, datasetTable);
        
        // set column headers for the Table
        for (VariableType variable : variables) {
            String columnId = new StringBuffer().append(variable.getId()).append("-").append(variable.getLocalName()).toString();
            String columnHeader = variable.getLocalName();
            datasetTable.setColumnHeader(columnId, columnHeader);
        }
        
        return datasetTable;
    }
    
    @Override
    public void attach() {        
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(exportCsvButton, Message.EXPORT_TO_CSV_LABEL);
        messageSource.setCaption(exportExcelButton, Message.EXPORT_TO_EXCEL_LABEL);
        messageSource.setCaption(openTableViewerButton, Message.OPEN_TABLE_VIEWER_LABEL);
    }
    
}
