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

import java.util.ArrayList;
import java.util.List;

import org.generationcp.browser.application.GermplasmStudyBrowserApplication;
import org.generationcp.browser.application.Message;
import org.generationcp.browser.study.containers.RepresentationDatasetQueryFactory;
import org.generationcp.browser.study.listeners.StudyButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Factor;
import org.generationcp.middleware.pojos.Variate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.addon.tableexport.CsvExport;
import com.vaadin.addon.tableexport.TableExport;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

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

    private Table datasetTable;
    private String reportName;
    private Integer studyIdHolder;
    private Integer representationId;

    private Button exportCsvButton;
    private Button exportExcelButton;
    private StringBuffer reportTitle;
    
    private StudyDataManager studyDataManager;
    
    private Window saveFieldBookExcelFileDialog;
    
    private boolean forStudyWindow;         //this is true if this component is created for the study browser only window

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public RepresentationDatasetComponent(StudyDataManager studyDataManager, Integer representationId, String datasetTitle, Integer studyId, boolean forStudyWindow) {
        this.reportName = datasetTitle;
        this.studyIdHolder = studyId;
        this.representationId = representationId;
        this.studyDataManager = studyDataManager;
        this.forStudyWindow = forStudyWindow;
    }

    // Called by StudyButtonClickListener
    public void exportToCSVAction() {
        CsvExport csvExport;
        // reportTitle = "Dataset-Study[" + studyIdHolder + "]-Rep[" + repIdHolder + "]";
        reportTitle = new StringBuffer().append(messageSource.getMessage(Message.REPORT_TITLE1_TEXT)).append("[").append(studyIdHolder)
                   .append("]-").append(messageSource.getMessage(Message.REPORT_TITLE2_TEXT)).append("[").append(representationId).append("]-");
        
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
        
        saveFieldBookExcelFileDialog = new Window(messageSource.getMessage(Message.EXPORT_TO_EXCEL_LABEL));        
        saveFieldBookExcelFileDialog.setModal(true);
        saveFieldBookExcelFileDialog.setWidth(700);
        saveFieldBookExcelFileDialog.setHeight(350);

        if(this.forStudyWindow){
            saveFieldBookExcelFileDialog.addComponent(new SaveRepresentationDatasetExcelDialog(
                    this.getApplication().getWindow(GermplasmStudyBrowserApplication.STUDY_WINDOW_NAME)
                    , saveFieldBookExcelFileDialog, studyIdHolder, representationId, this.getApplication()));
            this.getApplication().getWindow(GermplasmStudyBrowserApplication.STUDY_WINDOW_NAME).addWindow(saveFieldBookExcelFileDialog);
        } else{
            saveFieldBookExcelFileDialog.addComponent(new SaveRepresentationDatasetExcelDialog(
                    this.getApplication().getMainWindow(), saveFieldBookExcelFileDialog, studyIdHolder, representationId, this.getApplication()));
            this.getApplication().getMainWindow().addWindow(saveFieldBookExcelFileDialog);
        }
        
    }
    
    @Override
    public void afterPropertiesSet() throws Exception{
    	
    	 // set the column header ids
        List<Factor> factors = new ArrayList<Factor>();
        List<Variate> variates = new ArrayList<Variate>();
        List<String> columnIds = new ArrayList<String>();

        try {
            factors = studyDataManager.getFactorsByRepresentationId(representationId);
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in getting factors of representation: "
                            + representationId + "\n" + e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            factors = new ArrayList<Factor>();
            if (getWindow() != null) {
                MessageNotifier.showWarning(getWindow(), 
                        messageSource.getMessage(Message.ERROR_DATABASE), 
                        messageSource.getMessage(Message.ERROR_IN_GETTING_FACTORS_OF_REPRESENTATION)  + " " + representationId); 
            }
        }

        try {
            variates = studyDataManager.getVariatesByRepresentationId(representationId);
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in getting variates of representation: " 
                            + representationId + "\n" + e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            variates = new ArrayList<Variate>();
            if (getWindow() != null) {
                MessageNotifier.showWarning(getWindow(), 
                        messageSource.getMessage(Message.ERROR_DATABASE), 
                        messageSource.getMessage(Message.ERROR_IN_GETTING_VARIATES_OF_REPRESENTATION)  + " " + representationId);
            }
        }

        for (Factor factor : factors) {
            String columnId = new StringBuffer().append(factor.getFactorId()).append("-").append(factor.getName()).toString();
            columnIds.add(columnId);
        }

        for (Variate variate : variates) {
            String columnId = variate.getId().toString();
            columnIds.add(columnId);
        }

        // create item container for dataset table
        RepresentationDatasetQueryFactory factory = new RepresentationDatasetQueryFactory(studyDataManager, representationId, columnIds);
        LazyQueryContainer datasetContainer = new LazyQueryContainer(factory, false, 50);

        // add the column ids to the LazyQueryContainer tells the container the columns to display for the Table
        for (String columnId : columnIds) {
            if (columnId.contains("GID")) {
                datasetContainer.addContainerProperty(columnId, Link.class, null);
            } else {
                datasetContainer.addContainerProperty(columnId, String.class, null);
            }
        }

        datasetContainer.getQueryView().getItem(0); // initialize the first batch of data to be displayed

        // create the Vaadin Table to display the dataset, pass the container object created
        datasetTable = new Table("", datasetContainer);
        datasetTable.setColumnCollapsingAllowed(true);
        datasetTable.setColumnReorderingAllowed(true);
        datasetTable.setPageLength(15); // number of rows to display in the Table
        datasetTable.setSizeFull(); // to make scrollbars appear on the Table component

        // set column headers for the Table
        for (Factor factor : factors) {
            String columnId = new StringBuffer().append(factor.getFactorId()).append("-").append(factor.getName()).toString();
            String columnHeader = factor.getName();
            datasetTable.setColumnHeader(columnId, columnHeader);
        }

        for (Variate variate : variates) {
            String columnId = variate.getId().toString();
            String columnHeader = variate.getName();
            datasetTable.setColumnHeader(columnId, columnHeader);
        }

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
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        //TODO uncomment this when the feature of exporting to CSV is working properly
        //buttonLayout.addComponent(exportCsvButton);
        buttonLayout.addComponent(exportExcelButton);

        addComponent(buttonLayout);
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
    }
    
}
