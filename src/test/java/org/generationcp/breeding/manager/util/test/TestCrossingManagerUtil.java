package org.generationcp.breeding.manager.util.test;

import org.generationcp.breeding.manager.util.CrossingManagerUtil;
import org.generationcp.middleware.manager.DatabaseConnectionParameters;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestCrossingManagerUtil{


    private static ManagerFactory factory;
    private static GermplasmDataManager germplasmManager;

    @BeforeClass
    public static void setUp() throws Exception {
	DatabaseConnectionParameters local = new DatabaseConnectionParameters("IBPDatasource.properties", "local");
	DatabaseConnectionParameters central = new DatabaseConnectionParameters("IBPDatasource.properties", "central");
	factory = new ManagerFactory(local, central);
	germplasmManager = factory.getGermplasmDataManager();

    }

    @Test
    public void testSetCrossingBreedingMethod() throws Exception {
	Integer gid = Integer.valueOf(10);
	Integer femaleGID=Integer.valueOf(50533);
	Integer maleGID=Integer.valueOf(456);
	
	Germplasm gc = germplasmManager.getGermplasmByGID(gid);
	System.out.println("Germplasm Cross(gid " + gid + "): " + gc);
	
	CrossingManagerUtil cm= new CrossingManagerUtil(germplasmManager);
	Germplasm g= cm.setCrossingBreedingMethod(gc, femaleGID, maleGID);
	
	System.out.println("Germplasm for the cross with assigned value for the methn (gid " + gid + "): " + gc);

    }

    @AfterClass
    public static void tearDown() throws Exception {
	factory.close();
    }
}
