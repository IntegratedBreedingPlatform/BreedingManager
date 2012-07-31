package org.generationcp.browser.study.util;


public class DatasetExporterException extends Exception{

    private static final long serialVersionUID = -1639961960516233500L;

    public DatasetExporterException(String message) {
        super(message);
    }
    
    public DatasetExporterException(String message, Throwable cause) {
        super(message, cause);
    }
}
