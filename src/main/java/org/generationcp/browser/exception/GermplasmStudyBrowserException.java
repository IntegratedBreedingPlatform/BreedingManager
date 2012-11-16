package org.generationcp.browser.exception;


public class GermplasmStudyBrowserException extends Exception{

    private static final long serialVersionUID = 1L;

    public GermplasmStudyBrowserException(String message) {
        super(message);
    }
    
    public GermplasmStudyBrowserException(String message, Throwable cause) {
        super(message, cause);
    }
}
