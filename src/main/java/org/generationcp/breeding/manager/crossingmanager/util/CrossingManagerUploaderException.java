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
package org.generationcp.breeding.manager.crossingmanager.util;


public class CrossingManagerUploaderException extends Exception{

    private static final long serialVersionUID = -38638637163746263L;
    
    public CrossingManagerUploaderException(String message){
        super(message);
    }

    public CrossingManagerUploaderException(String message, Throwable cause){
        super(message, cause);
    }
}
