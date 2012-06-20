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

import org.generationcp.browser.study.listeners.StudyButtonClickListener;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Factor;
import org.generationcp.middleware.pojos.Variate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.addon.tableexport.CsvExport;
import com.vaadin.addon.tableexport.TableExport;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * This class creates the Vaadin Table where a dataset can be displayed.
 * 
 * @author Kevin Manansala
 * 
 */
public class RepresentationDatasetComponent extends VerticalLayout{

    private final static Logger LOG = LoggerFactory.getLogger(RepresentationDatasetComponent.class);
    private static final long serialVersionUID = -8476739652987572690L;

    private Table datasetTable;
    private String reportName;
    private Integer studyIdHolder;
    private Integer repIdHolder;

    public RepresentationDatasetComponent(StudyDataManager dataManager, Integer representationId, String datasetTitle, Integer studyId) {
        super();
        this.reportName = datasetTitle;
        this.studyIdHolder = studyId;
        this.repIdHolder = representationId;

        // set the column header ids
        List<Factor> factors = new ArrayList<Factor>();
        List<Variate> variates = new ArrayList<Variate>();
        List<String> columnIds = new ArrayList<String>();

        try {
            factors = dataManager.getFactorsByRepresentationId(representationId);
        } catch (QueryException ex) {
            // Log into the log fie
            LOG.error("Error with getting factors of representation: " + representationId + "\n" + ex.toString());

            // System.out.println("Error with getting factors of representation: "
            // + representationId);
            // System.out.println(ex);
            ex.printStackTrace();
            factors = new ArrayList<Factor>();
        }

        try {
            variates = dataManager.getVariatesByRepresentationId(representationId);
        } catch (QueryException ex) {
            LOG.error("Error with getting variates of representation: " + representationId, ex);
            // System.out.println("Error with getting variates of representation: "
            // + representationId);
            // System.out.println(ex);
            ex.printStackTrace();
            variates = new ArrayList<Variate>();
        }

        for (Factor factor : factors) {
            String columnId = factor.getFactorId() + "-" + factor.getName();
            columnIds.add(columnId);
        }

        for (Variate variate : variates) {
            String columnId = variate.getId().toString();
            columnIds.add(columnId);
        }

        // create item container for dataset table
        RepresentationDatasetQueryFactory factory = new RepresentationDatasetQueryFactory(dataManager, representationId, columnIds);
        LazyQueryContainer datasetContainer = new LazyQueryContainer(factory, false, 50);

        // add the column ids to the LazyQueryContainer
        // tells the container the columns to display for the Table
        for (String columnId : columnIds) {
            datasetContainer.addContainerProperty(columnId, String.class, null);
        }

        datasetContainer.getQueryView().getItem(0); // initialize the first
        // batch of data to be
        // displayed

        // create the Vaadin Table to display the dataset, pass the container
        // object created
        datasetTable = new Table("", datasetContainer);
        datasetTable.setColumnCollapsingAllowed(true);
        datasetTable.setColumnReorderingAllowed(true);
        datasetTable.setPageLength(15); // number of rows to display in the
        // Table
        datasetTable.setSizeFull(); // to make scrollbars appear on the Table
        // component

        // set column headers for the Table
        for (Factor factor : factors) {
            String columnId = factor.getFactorId() + "-" + factor.getName();
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

        Button exportCsvButton = new Button("Export to CSV");

        exportCsvButton.addListener(new StudyButtonClickListener(this));
        addComponent(exportCsvButton);
    }

    // Called by StudyButtonClickListener
    public void exportToCSVAction() {
        CsvExport csvExport;

        String reportTitle = "Dataset-Study[" + studyIdHolder + "]-Rep[" + repIdHolder + "]";
        String fileName = reportTitle + ".csv";
        csvExport = new CsvExport(datasetTable, reportName, reportTitle, fileName, false);
        csvExport.excludeCollapsedColumns();
        csvExport.setMimeType(TableExport.CSV_MIME_TYPE);
        csvExport.export();

    }
}
