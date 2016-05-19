/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.breeding.manager.study;

import java.io.File;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.study.listeners.StudyButtonClickListener;
import org.generationcp.breeding.manager.study.util.DirectoryTreeBrowser;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
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
public class SaveRepresentationDatasetExcelDialog extends GridLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;

	public static final String SAVE_EXCEL_BUTTON_ID = "Save to File";
	public static final String CANCEL_EXCEL_BUTTON_ID = "Cancel";
	public static final String BROWSE_FOLDER_BUTTON_ID = "Browse";

	public static final String USER_HOME = "user.home";

	private Label labelFileName;
	private TextField txtFileName;

	private Label labelDestinationFolder;
	private TextField txtDestinationFolder;

	private String uploadPath;

	private Button btnSave;
	private Button btnCancel;
	private Button btnBrowseDirectory;

	private final Window dialogWindow;
	private final Window mainWindow;
	private Window fileSystemWindow;

	private final Application mainApplication;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public SaveRepresentationDatasetExcelDialog(Window mainWindow, Window dialogWindow, Integer studyId, Integer representationId,
			Application application) {
		this.dialogWindow = dialogWindow;
		this.mainWindow = mainWindow;
		this.mainApplication = application;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setRows(8);
		this.setColumns(4);
		this.setSpacing(true);
		this.setMargin(true);

		this.uploadPath = System.getProperty(SaveRepresentationDatasetExcelDialog.USER_HOME) + File.separator + "Desktop";

		this.labelFileName = new Label();
		this.labelFileName.setDescription(this.messageSource.getMessage(Message.INPUT_FILE_NAME_TEXT));
		this.labelFileName.setWidth("120px");
		this.txtFileName = new TextField();
		this.txtFileName.setWidth("300px");

		this.labelDestinationFolder = new Label();
		this.labelDestinationFolder.setCaption(this.messageSource.getMessage(Message.DESTINATION_FOLDER_TEXT));
		this.labelDestinationFolder.setWidth("120px");
		this.labelDestinationFolder.setDescription(this.messageSource.getMessage(Message.DESTINATION_FOLDER_TEXT));
		this.txtDestinationFolder = new TextField();
		this.txtDestinationFolder.setWidth("300px");
		this.txtDestinationFolder.setValue(this.uploadPath);

		this.btnSave = new Button();
		this.btnSave.setWidth("80px");
		this.btnSave.setData(SaveRepresentationDatasetExcelDialog.SAVE_EXCEL_BUTTON_ID);
		this.btnSave.setDescription(this.messageSource.getMessage(Message.SAVE_FIELDBOOK_EXCEL_FILE_LABEL));
		this.btnSave.addListener(new StudyButtonClickListener(this));

		this.btnCancel = new Button();
		this.btnCancel.setWidth("80px");
		this.btnCancel.setData(SaveRepresentationDatasetExcelDialog.CANCEL_EXCEL_BUTTON_ID);
		this.btnCancel.setDescription(this.messageSource.getMessage(Message.CANCEL_SAVE_FIELDBOOK_EXCEL_FILE_LABEL));
		this.btnCancel.addListener(new StudyButtonClickListener(this));

		this.btnBrowseDirectory = new Button("Browse");
		this.btnBrowseDirectory.setWidth("80px");
		this.btnBrowseDirectory.setData(SaveRepresentationDatasetExcelDialog.BROWSE_FOLDER_BUTTON_ID);
		this.btnBrowseDirectory.setDescription("Browse destination folder");
		this.btnBrowseDirectory.addListener(new StudyButtonClickListener(this));

		HorizontalLayout hButton = new HorizontalLayout();
		hButton.setSpacing(true);
		hButton.addComponent(this.btnSave);
		hButton.addComponent(this.btnCancel);

		this.addComponent(this.labelFileName, 1, 1);
		this.addComponent(this.txtFileName, 2, 1);

		this.addComponent(this.labelDestinationFolder, 1, 2);
		this.addComponent(this.txtDestinationFolder, 2, 2);
		this.addComponent(this.btnBrowseDirectory, 3, 2);

		this.addComponent(hButton, 1, 5);
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this.labelFileName, Message.FILE_NAME_LABEL);
		this.messageSource.setCaption(this.btnSave, Message.SAVE_LABEL);
		this.messageSource.setCaption(this.btnCancel, Message.CANCEL_LABEL);
		this.messageSource.setCaption(this.btnBrowseDirectory, Message.BROWSE_LABEL);
	}

	@SuppressWarnings("deprecation")
	public void browseDirectoryButtonClickAction() throws InternationalizableException {
		String destFolder = null;
		if (this.txtDestinationFolder != null) {
			destFolder = (String) this.txtDestinationFolder.getValue();
			if (!Util.isDirectory(destFolder)) {
				MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_TEXT),
						this.messageSource.getMessage(Message.ERROR_INVALID_DIRECTORY));
				return;
			}
		}

		this.fileSystemWindow = new BaseSubWindow("Destination Directory (Double-Click to Select)");
		this.fileSystemWindow.setModal(true);
		this.fileSystemWindow.setWidth(700);
		this.fileSystemWindow.setHeight(350);
		this.fileSystemWindow.addComponent(new DirectoryTreeBrowser(this, destFolder));
		this.mainWindow.addWindow(this.fileSystemWindow);
	}

	public Application getMainApplication() {
		return this.mainApplication;
	}

	public void closeFileSystemWindow() {
		this.mainWindow.removeWindow(this.fileSystemWindow);

	}

	// Called by TreeFileBrowser OK button
	public void setDestinationFolderValue(String newValue) {
		this.txtDestinationFolder.setValue(newValue);
		this.txtDestinationFolder.requestRepaint();
	}

	public void saveExcelFileButtonClickAction() throws InternationalizableException {

		// Get file name from the input text field
		String fileName = (String) this.txtFileName.getValue();

		// Get the destination folder from the input
		if (this.txtDestinationFolder != null && this.txtDestinationFolder.getValue() != null) {
			String destinationPath = (String) this.txtDestinationFolder.getValue();
			if (Util.isDirectory(destinationPath)) {
				this.setUploadPath(destinationPath);
			} else {
				MessageNotifier.showError(this.mainWindow.getWindow(), this.messageSource.getMessage(Message.ERROR_TEXT),
						this.messageSource.getMessage(Message.ERROR_INVALID_DESTINATION_FOLDER_TEXT));
				return;
			}
		}

		// Check if a file name is supplied
		if (fileName == null || fileName.equals("")) {
			MessageNotifier.showError(this.mainWindow.getWindow(), this.messageSource.getMessage(Message.ERROR_TEXT),
					this.messageSource.getMessage(Message.ERROR_PLEASE_INPUT_FILE_NAME_TEXT));
			return;
		}

		// If file name has no .xls extension, add one
		if (!fileName.endsWith(".xls")) {
			fileName += ".xls";
		}

		this.closeDialog();
		MessageNotifier.showMessage(this.mainWindow.getWindow(), fileName,
				this.messageSource.getMessage(Message.SAVE_FIELDBOOK_EXCEL_FILE_SUCCESSFUL_TEXT));

	}

	public void cancelSavingExcelFileButtonClickAction() {
		this.closeDialog();
	}

	public void closeDialog() {
		this.mainWindow.removeWindow(this.dialogWindow);
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
