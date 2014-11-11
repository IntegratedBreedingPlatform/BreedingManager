package org.generationcp.breeding.manager.customcomponent;

import java.io.File;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporterException;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
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

	private static final int NO_OF_REQUIRED_COLUMNS = 3;

	private static final String CSV_FORMAT = ".csv";

	private static final String XLS_FORMAT = ".xls";

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
	
	private Table listDataTable;
	
	private static final String USER_HOME = "user.home";
	
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
				exportListAction();
			}
		});
	}

	protected void exportListAction() {
		if(!germplasmList.isLocalList() || (germplasmList.isLocalList() && germplasmList.isLockedList())){
			showWarningMessage();
			//do the export
			if(XLS_FORMAT.equalsIgnoreCase(formatOptionsCbx.getValue().toString())){
				exportListAsXLS();
			} else if(CSV_FORMAT.equalsIgnoreCase(formatOptionsCbx.getValue().toString())){
				exportListAsCSV();
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
	
	protected void exportListAsCSV() {
		String tempFileName = System.getProperty( USER_HOME ) + "/temp.csv";
		GermplasmListExporter listExporter = new GermplasmListExporter(germplasmList.getId());
        try {
            listExporter.exportGermplasmListCSV(tempFileName, listDataTable);
            FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), source.getApplication());
            String listName = germplasmList.getName();
            fileDownloadResource.setFilename(listName.replace(" ", "_") + CSV_FORMAT);
            source.getWindow().open(fileDownloadResource);
            
            //must figure out other way to clean-up file because deleting it here makes it unavailable for download
        } catch (GermplasmListExporterException e) {
            LOG.error(messageSource.getMessage(Message.ERROR_EXPORTING_LIST), e);
            MessageNotifier.showError(this.getWindow()
                        , messageSource.getMessage(Message.ERROR_EXPORTING_LIST)    
                        , e.getMessage() + ". " + messageSource.getMessage(Message.ERROR_REPORT_TO));
        }
		
	}
	
	public void exportListAsXLS(){	
        String tempFileName = System.getProperty( USER_HOME ) + "/temp.xls";
        GermplasmListExporter listExporter = new GermplasmListExporter(germplasmList.getId());
        try {
            listExporter.exportGermplasmListExcel(tempFileName,listDataTable);
            FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), source.getApplication());
            String listName = germplasmList.getName();
            fileDownloadResource.setFilename(listName.replace(" ", "_") + XLS_FORMAT);
            source.getWindow().open(fileDownloadResource);
            
            //must figure out other way to clean-up file because deleting it here makes it unavailable for download
        } catch (GermplasmListExporterException e) {
            LOG.error(messageSource.getMessage(Message.ERROR_EXPORTING_LIST), e);
            MessageNotifier.showError(this.getWindow()
                        , messageSource.getMessage(Message.ERROR_EXPORTING_LIST)    
                        , e.getMessage() + ". " + messageSource.getMessage(Message.ERROR_REPORT_TO));
        }
	}

	private void showWarningMessage() {
		if(isARequiredColumnHidden(listDataTable)){
			String message = "The export file will leave out contain columns that you have marked as hidden in the table view, "
					+ "with the exception of key columns that are necessary to identify your data when you import it back into the system.";
			
			MessageNotifier.showWarning(this.getWindow(), messageSource.getMessage(Message.WARNING), message);
		}
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

	private boolean isARequiredColumn(String column) {
		return ListDataTablePropertyID.ENTRY_ID.getName().equalsIgnoreCase(column)
				|| ListDataTablePropertyID.GID.getName().equalsIgnoreCase(column)
				|| ListDataTablePropertyID.DESIGNATION.getName().equalsIgnoreCase(column);
	}

}
