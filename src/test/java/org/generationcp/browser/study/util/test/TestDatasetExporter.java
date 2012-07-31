package org.generationcp.browser.study.util.test;


import org.generationcp.browser.study.util.DatasetExporter;
import org.generationcp.middleware.manager.DatabaseConnectionParameters;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.TraitDataManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestDatasetExporter{
    
    private static DatasetExporter exporter;
    private static ManagerFactory factory;

    @BeforeClass
    public static void setUp() throws Exception {
        DatabaseConnectionParameters local = new DatabaseConnectionParameters("IBPDatasource.properties", "local");
        DatabaseConnectionParameters central = new DatabaseConnectionParameters("IBPDatasource.properties", "central");
        factory = new ManagerFactory(local, central);
        StudyDataManager studyManager = factory.getStudyDataManager();
        TraitDataManager traitManager = factory.getTraitDataManager();
        exporter = new DatasetExporter(studyManager, traitManager, Integer.valueOf(1), Integer.valueOf(2));
    }
    
    @Test
    public void testExportToFieldBookExcel() throws Exception {
        exporter.exportToFieldBookExcel("testing.xls");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        factory.close();
    }

}
