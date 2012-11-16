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

import org.generationcp.browser.application.Message;
import org.generationcp.browser.study.listeners.StudyButtonClickListener;
import org.generationcp.browser.study.util.DatasetExporter;
import org.generationcp.browser.study.util.DatasetExporterException;
import org.generationcp.browser.study.util.DirectoryTreeBrowser;
import org.generationcp.browser.util.Util;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.TraitDataManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

/**
 * This class opens a dialog box that prompts for the file name to export the Fieldbook Excel file into.
 * 
 * @author Joyce Avestro
 * 
 */
@Configurable
public class SaveRepresentationDatasetExcelDialog extends GridLayout 
                            implements  InitializingBean, InternationalizableComponent{
    
	private static final long serialVersionUID = 1L;
	
	public static final String SAVE_EXCEL_BUTTON_ID = "Save to File";
    public static final String CANCEL_EXCEL_BUTTON_ID = "Cancel";
    public static final String BROWSE_FOLDER_BUTTON_ID = "Browse";
	
    private Label labelFileName;
    private TextField txtFileName;
    
    private Label labelDestinationFolder;
    private TextField txtDestinationFolder;
    
    private String uploadPath;

    private Button btnSave;
    private Button btnCancel;
    private Button btnBrowseDirectory;

	private Window dialogWindow;
	private Window mainWindow;
	private Window fileSystemWindow;
	
	private Integer studyId;
    private Integer representationId;
    
    private Application mainApplication;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

    @Autowired
	private StudyDataManager studyDataManager;
	
    @Autowired
	private TraitDataManager traitDataManager;
	
	public SaveRepresentationDatasetExcelDialog(Window mainWindow, Window dialogWindow, Integer studyId, Integer representationId, Application application) {
		this.dialogWindow = dialogWindow;
		this.mainWindow = mainWindow;
		this.studyId = studyId;
		this.representationId = representationId;
		this.mainApplication = application;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		setRows(8);
		setColumns(4);
		setSpacing(true);
		setMargin(true);
		
        uploadPath = System.getProperty("user.home") + File.separator + "Desktop" ;
        
        labelFileName = new Label();
        labelFileName.setDescription(messageSource.getMessage(Message.INPUT_FILE_NAME_TEXT));
        labelFileName.setWidth("120px");
        txtFileName = new TextField();
        txtFileName.setWidth("300px");
        
        labelDestinationFolder = new Label();
        labelDestinationFolder.setCaption(messageSource.getMessage(Message.DESTINATION_FOLDER_TEXT));
        labelDestinationFolder.setWidth("120px");
        labelDestinationFolder.setDescription(messageSource.getMessage(Message.DESTINATION_FOLDER_TEXT));
        txtDestinationFolder = new TextField();
        txtDestinationFolder.setWidth("300px");
        txtDestinationFolder.setValue(uploadPath);
        
		btnSave = new Button();
		btnSave.setWidth("80px");
		btnSave.setData(SAVE_EXCEL_BUTTON_ID);
		btnSave.setDescription(messageSource.getMessage(Message.SAVE_FIELDBOOK_EXCEL_FILE_LABEL));
		btnSave.addListener(new StudyButtonClickListener(this));
		
		btnCancel = new Button();
		btnCancel.setWidth("80px");
		btnCancel.setData(CANCEL_EXCEL_BUTTON_ID);
		btnCancel.setDescription(messageSource.getMessage(Message.CANCEL_SAVE_FIELDBOOK_EXCEL_FILE_LABEL));
		btnCancel.addListener(new StudyButtonClickListener(this));

		btnBrowseDirectory = new Button("Browse");
		btnBrowseDirectory.setWidth("80px");
		btnBrowseDirectory.setData(BROWSE_FOLDER_BUTTON_ID);
		btnBrowseDirectory.setDescription("Browse destination folder");
		btnBrowseDirectory.addListener(new StudyButtonClickListener(this));
        
        HorizontalLayout hButton = new HorizontalLayout();
        hButton.setSpacing(true);
        hButton.addComponent(btnSave);
        hButton.addComponent(btnCancel);

		addComponent(labelFileName, 1, 1);
		addComponent(txtFileName, 2, 1);
		
        addComponent(labelDestinationFolder, 1, 2);
        addComponent(txtDestinationFolder, 2, 2);
        addComponent(btnBrowseDirectory, 3, 2);

        addComponent(hButton, 1, 5);
	}



	@Override
	public void attach() {
		super.attach();
		updateLabels();
	}

	@Override
	public void updateLabels() {
		messageSource.setCaption(labelFileName, Message.FILE_NAME_LABEL);
		messageSource.setCaption(btnSave, Message.SAVE_LABEL);
        messageSource.setCaption(btnCancel, Message.CANCEL_LABEL);
        messageSource.setCaption(btnBrowseDirectory, Message.BROWSE_LABEL);
	}
	
    @SuppressWarnings("deprecation")
    public void browseDirectoryButtonClickAction() throws InternationalizableException {
        String destFolder = null;
        if (txtDestinationFolder != null) {
            destFolder = (String) txtDestinationFolder.getValue();
            if (!Util.isDirectory(destFolder)){
                MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_TEXT), messageSource.getMessage(Message.ERROR_INVALID_DIRECTORY));
                return;
            }
        }
        
        fileSystemWindow = new Window("Destination Directory (Double-Click to Select)");
        fileSystemWindow.setModal(true);
        fileSystemWindow.setWidth(700);
        fileSystemWindow.setHeight(350);
        fileSystemWindow.addComponent(new DirectoryTreeBrowser(this, destFolder));
        mainWindow.addWindow(fileSystemWindow);
    }
    
    public Application getMainApplication(){
        return mainApplication;
    }
    public void closeFileSystemWindow(){
        mainWindow.removeWindow(fileSystemWindow);
        
    }
    // Called by TreeFileBrowser OK button
    public void setDestinationFolderValue(String newValue){
        txtDestinationFolder.setValue(newValue);
        txtDestinationFolder.requestRepaint();
    }
    
    
	public void saveExcelFileButtonClickAction() throws InternationalizableException {
	    
        //Get file name from the input text field
	    String fileName = (String) txtFileName.getValue();
	    
	    //Get the destination folder from the input
	    if (txtDestinationFolder != null && txtDestinationFolder.getValue() != null){
	        String destinationPath = (String) txtDestinationFolder.getValue();
	        if (Util.isDirectory(destinationPath)){
	            setUploadPath(destinationPath);
	        } else {
	            MessageNotifier.showError(mainWindow.getWindow(), messageSource.getMessage(Message.ERROR_TEXT), messageSource.getMessage(Message.ERROR_INVALID_DESTINATION_FOLDER_TEXT));
	            return;
	        }
	    }
	    
	    // Check if a file name is supplied
	    if (fileName == null || fileName.equals("")){
	        MessageNotifier.showError(mainWindow.getWindow(), messageSource.getMessage(Message.ERROR_TEXT), messageSource.getMessage(Message.ERROR_PLEASE_INPUT_FILE_NAME_TEXT));
	        return;
	    }
	    
        // If file name has no .xls extension, add one
	    if (!fileName.endsWith(".xls")){
	        fileName += ".xls";
	    }   

        DatasetExporter datasetExporter;
        datasetExporter = new DatasetExporter(studyDataManager, traitDataManager, studyId, representationId);
        try {
            datasetExporter.exportToFieldBookExcel(uploadPath + fileName);
        } catch (DatasetExporterException e) {
            MessageNotifier.showError(mainWindow.getWindow(), e.getMessage(), "");
        }
        
        closeDialog();
        MessageNotifier.showMessage(mainWindow.getWindow(), fileName,
                messageSource.getMessage(Message.SAVE_FIELDBOOK_EXCEL_FILE_SUCCESSFUL_TEXT));

	}

	public void cancelSavingExcelFileButtonClickAction() {
	    closeDialog();
	}

	public void closeDialog() {
		this.mainWindow.removeWindow(dialogWindow);
	}

    /** 
     * This is called to set the upload path
     */
    public void setUploadPath(String uploadPath) {
        String newUploadPath = uploadPath;
        if (!uploadPath.endsWith(File.separator)) {
            newUploadPath += File.separator;
        }
        this.uploadPath = newUploadPath;
    }

}
