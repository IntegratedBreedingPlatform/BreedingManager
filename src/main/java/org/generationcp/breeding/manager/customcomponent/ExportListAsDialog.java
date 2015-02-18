package org.generationcp.breeding.manager.customcomponent;

import java.io.File;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ExportListAsDialog extends BaseSubWindow implements InitializingBean,
						InternationalizableComponent, BreedingManagerLayout {

	private static final String XLS_EXT = ".xls";

	private static final String CSV_EXT = ".csv";

	private static final int NO_OF_REQUIRED_COLUMNS = 3;

	private static final String CSV_FORMAT = "CSV";

	private static final String XLS_FORMAT = "Excel";

	private static final long serialVersionUID = -4214986909789479904L;
	
	private static final Logger LOG = LoggerFactory.getLogger(ExportListAsDialog.class);

	private VerticalLayout mainLayout;
	private Label exportFormalLbl;
	private Label chooseAnExportLbl;
	private ComboBox formatOptionsCbx;
	private Button finishButton;
	private Button cancelButton;
	
	private Component source;
	private GermplasmList germplasmList;
	private GermplasmListExporter listExporter;
	
	private Table listDataTable;
	
	public static String EXPORT_WARNING_MESSAGE;
	private static final String USER_HOME = "user.home";
	public static final String TEMP_FILENAME = System.getProperty( USER_HOME ) + "/temp.csv";
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	public ExportListAsDialog(Component source, GermplasmList germplasmList, Table listDataTable){
		this.source = source;
		this.germplasmList = germplasmList;
		this.listDataTable = listDataTable;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		EXPORT_WARNING_MESSAGE = messageSource.getMessage(Message.EXPORT_WARNING_MESSAGE);
		
		exportFormalLbl = new Label(messageSource.getMessage(Message.EXPORT_FORMAT).toUpperCase());
		exportFormalLbl.setStyleName(Bootstrap.Typography.H2.styleName());
		
		chooseAnExportLbl = new Label(messageSource.getMessage(Message.CHOOSE_AN_EXPORT_FORMAT) + ":");
		
		formatOptionsCbx = new ComboBox();
		formatOptionsCbx.setImmediate(true);
		formatOptionsCbx.setNullSelectionAllowed(false);
		formatOptionsCbx.setTextInputAllowed(false);
		formatOptionsCbx.setWidth("100px");
		
		cancelButton = new Button(messageSource.getMessage(Message.CANCEL));
		cancelButton.setWidth("80px");
		
		finishButton = new Button(messageSource.getMessage(Message.FINISH));
		finishButton.setWidth("80px");
		finishButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		
		listExporter = new GermplasmListExporter(germplasmList.getId());
	}

	@Override
	public void initializeValues() {
		formatOptionsCbx.addItem(XLS_FORMAT);
		formatOptionsCbx.addItem(CSV_FORMAT);
		
		//default value
		formatOptionsCbx.setValue(XLS_FORMAT);
	}

	@Override
	public void addListeners() {
		cancelButton.addListener(new CloseWindowAction());
		 
		finishButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				exportListAction(listDataTable);
			}
		});
	}

	protected void exportListAction(Table table) {
		if(germplasmList.isLockedList()){
			showWarningMessage(table);
			//do the export
			if(XLS_FORMAT.equalsIgnoreCase(formatOptionsCbx.getValue().toString())){
				exportListAsXLS(table);
			} else if(CSV_FORMAT.equalsIgnoreCase(formatOptionsCbx.getValue().toString())){
				exportListAsCSV(table);
			}
		 } else {
            MessageNotifier.showError(this.getWindow()
                    , messageSource.getMessage(Message.ERROR_EXPORTING_LIST)
                    , messageSource.getMessage(Message.ERROR_EXPORT_LIST_MUST_BE_LOCKED));
        }
	}

	@Override
	public void layoutComponents() {
		//window formatting
		this.setCaption(messageSource.getMessage(Message.EXPORT_GERMPLASM_LIST));
		this.addStyleName(Reindeer.WINDOW_LIGHT);
		this.setModal(true);
		this.setResizable(false);
		this.setHeight("225px");
		this.setWidth("380px");
		
		HorizontalLayout fieldLayout = new HorizontalLayout();
		fieldLayout.setSpacing(true);
		fieldLayout.addComponent(chooseAnExportLbl);
		fieldLayout.addComponent(formatOptionsCbx);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setHeight("50px");
		buttonLayout.setWidth("100%");
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(cancelButton);
		buttonLayout.addComponent(finishButton);
		buttonLayout.setComponentAlignment(cancelButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(finishButton, Alignment.BOTTOM_LEFT);
		
		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.addComponent(exportFormalLbl);
		mainLayout.addComponent(fieldLayout);
		mainLayout.addComponent(buttonLayout);
		
		this.addComponent(mainLayout);
	}

	@Override
	public void updateLabels() {
		//do nothing
	}
	
	protected void exportListAsCSV(Table table) {
        try {
            listExporter.exportGermplasmListCSV(TEMP_FILENAME, table);
            FileDownloadResource fileDownloadResource = createFileDownloadResource();
            String listName = germplasmList.getName();
            fileDownloadResource.setFilename(listName.replace(" ", "_") + CSV_EXT);
            source.getWindow().open(fileDownloadResource);
            
            //must figure out other way to clean-up file because deleting it here makes it unavailable for download
        } catch (GermplasmListExporterException e) {
            LOG.error(messageSource.getMessage(Message.ERROR_EXPORTING_LIST), e);
            MessageNotifier.showError(this.getWindow()
                        , messageSource.getMessage(Message.ERROR_EXPORTING_LIST)    
                        , e.getMessage() + ". " + messageSource.getMessage(Message.ERROR_REPORT_TO));
        }
		
	}
	
	protected void exportListAsXLS(Table table){
        try {
            listExporter.exportGermplasmListXLS(TEMP_FILENAME,table);
            FileDownloadResource fileDownloadResource = createFileDownloadResource();
            String listName = germplasmList.getName();
            fileDownloadResource.setFilename(listName.replace(" ", "_") + XLS_EXT);
            source.getWindow().open(fileDownloadResource);
            
            //must figure out other way to clean-up file because deleting it here makes it unavailable for download
        } catch (GermplasmListExporterException | MiddlewareQueryException e) {
            LOG.error(messageSource.getMessage(Message.ERROR_EXPORTING_LIST), e);
            MessageNotifier.showError(this.getWindow()
                        , messageSource.getMessage(Message.ERROR_EXPORTING_LIST)    
                        , e.getMessage() + ". " + messageSource.getMessage(Message.ERROR_REPORT_TO));
        }
	}

	protected FileDownloadResource createFileDownloadResource() {
		FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(TEMP_FILENAME), source.getApplication());
		return fileDownloadResource;
	}

	protected void showWarningMessage(Table table) {
		if(isARequiredColumnHidden(table)){
			showMessage(ExportListAsDialog.EXPORT_WARNING_MESSAGE);
		}
	}

	protected void showMessage(String message) {
		MessageNotifier.showWarning(this.getWindow(), messageSource.getMessage(Message.WARNING), message);
	}
	
	protected boolean isARequiredColumnHidden(Table listDataTable){
		int visibleRequiredColumns = 0;
		Object[] visibleColumns = listDataTable.getVisibleColumns();
		
		for(Object column : visibleColumns){
			if(isARequiredColumn(column.toString())
					&& !listDataTable.isColumnCollapsed(column)){
				visibleRequiredColumns++;
			}
		}
		
		return visibleRequiredColumns < NO_OF_REQUIRED_COLUMNS;
	}

	protected boolean isARequiredColumn(String column) {
		return ListDataTablePropertyID.ENTRY_ID.getName().equalsIgnoreCase(column)
				|| ListDataTablePropertyID.GID.getName().equalsIgnoreCase(column)
				|| ListDataTablePropertyID.DESIGNATION.getName().equalsIgnoreCase(column);
	}

	public void setListExporter(GermplasmListExporter listExporter) {
		this.listExporter = listExporter;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
