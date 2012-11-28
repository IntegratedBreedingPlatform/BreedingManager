package org.generationcp.browser.util;


public class InvalidDateException extends Exception{

    private static final long serialVersionUID = 1L;

    public InvalidDateException(String message) {
        super(message);
    }
    
    public InvalidDateException(String message, Throwable cause) {
        super(message, cause);
    }
}
