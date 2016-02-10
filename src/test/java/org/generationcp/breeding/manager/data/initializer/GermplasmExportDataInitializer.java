package org.generationcp.breeding.manager.data.initializer;

import org.generationcp.commons.pojo.CustomReportType;

import java.util.ArrayList;
import java.util.List;

public class GermplasmExportDataInitializer {
    public static final String TEST_REPORT_CODE = "MFbShipList";
    public static final String TEST_REPORT_NAME = "Template, Maize";

    public static List<CustomReportType> createCustomReportTypeList() {
        CustomReportType reportType = new CustomReportType(TEST_REPORT_CODE, TEST_REPORT_NAME);
        List<CustomReportType> customReportTypes = new ArrayList<>();
        customReportTypes.add(reportType);

        return customReportTypes;
    }
}
