package org.generationcp.breeding.manager.crossingmanager;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class SummaryListHeaderComponent extends VerticalLayout
		implements BreedingManagerLayout, InitializingBean {

	private static final int DESCRIPTION_LENGTH = 80;
	private static final long serialVersionUID = 6735189578521540285L;
	private static final Logger LOG = LoggerFactory.getLogger(SummaryListHeaderComponent.class);
	
	@Autowired
	private GermplasmListManager germplasmListManager;
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	private Label sectionTitleLabel;
	private Label savedAsLabel;
	private Label descriptionLabel;
	private Label listTypeLabel;
	private Label listDateLabel;
	
	private Label folderPathValue;
	private Label listNameValue;
	private Label descriptionValue;
	private Label listTypeValue;
	private Label listDateValue;
	
	private GermplasmList list;
	private Integer listId;
	private String sectionTitle;

	public SummaryListHeaderComponent(GermplasmList list, String sectionTitle) {
		super();
		this.list = list;
		this.sectionTitle = sectionTitle;
	}
	
	public SummaryListHeaderComponent(Integer listId, String sectionTitle) {
		super();
		this.listId = listId;
		this.sectionTitle = sectionTitle;
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
		sectionTitleLabel = new Label(this.sectionTitle.toUpperCase());
		sectionTitleLabel.addStyleName(Bootstrap.Typography.H4.styleName());
		sectionTitleLabel.addStyleName(AppConstants.CssStyles.BOLD);
		
		savedAsLabel = new Label(messageSource.getMessage(Message.SAVED_AS) + ":");
		savedAsLabel.addStyleName(AppConstants.CssStyles.BOLD);
		
		descriptionLabel = new Label(messageSource.getMessage(Message.DESCRIPTION_LABEL) + ":");
		descriptionLabel.addStyleName(AppConstants.CssStyles.BOLD);
		
		listTypeLabel = new Label(messageSource.getMessage(Message.LIST_TYPE) + ":");
		listTypeLabel.addStyleName(AppConstants.CssStyles.BOLD);
		
		listDateLabel = new Label(messageSource.getMessage(Message.DATE_LABEL) + ":");
		listDateLabel.addStyleName(AppConstants.CssStyles.BOLD);
		
		folderPathValue = new Label();

		listNameValue = new Label();
		listNameValue.addStyleName(AppConstants.CssStyles.BOLD);
		
		descriptionValue = new Label();
		
		listTypeValue = new Label();
		listTypeValue.setWidth("150px");
		
		listDateValue = new Label();
	}

	private void retrieveListDetails() {
		try {
			list = germplasmListManager.getGermplasmListById(listId);
		} catch (MiddlewareQueryException e) {
			LOG.error("Error in getting parent list:" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void initializeValues() {
		if (list == null){
			retrieveListDetails();
		}
		
		listNameValue.setValue(list.getName());
		
		String description = list.getDescription();
		if (description.length() > 70){
			description = description.substring(0, DESCRIPTION_LENGTH) + "...";
		}
		descriptionValue.setValue(description);
		descriptionValue.setDescription(list.getDescription());
		
		listDateValue.setValue(list.getDate());

		initializeListTypeValue();
		try {
			folderPathValue.setValue(Util.generateListFolderPathLabel(germplasmListManager, list.getParent()));
		} catch (MiddlewareQueryException e) {
			LOG.error("Error getting list folder path " + e.getMessage());
		}
		
		
	}

	private void initializeListTypeValue() {
		listTypeValue.setValue(list.getType());
		try {
			List<UserDefinedField> listTypes = this.germplasmListManager.getGermplasmListTypes();
			for (UserDefinedField field: listTypes){
				if (field.getFcode().equals(list.getType())){
					listTypeValue.setValue(field.getFname());
				}
			}
		} catch (MiddlewareQueryException e) {
			LOG.error("Error getting list types " + e.getMessage());
		}
	}

	@Override
	public void addListeners() {
	}

	@Override
	public void layoutComponents() {
		setSpacing(true);
		
		HorizontalLayout row1 = new HorizontalLayout();
		row1.setSpacing(true);
		row1.addComponent(savedAsLabel);
		row1.addComponent(folderPathValue);
		row1.addComponent(listNameValue);
		
		HorizontalLayout row2 = new HorizontalLayout();
		row2.setSpacing(true);
		row2.addComponent(descriptionLabel);
		row2.addComponent(descriptionValue);
		
		HorizontalLayout row3 = new HorizontalLayout();
		row3.setSpacing(true);
		row3.addComponent(listTypeLabel);
		row3.addComponent(listTypeValue);
		row3.addComponent(listDateLabel);
		row3.addComponent(listDateValue);
		
		addComponent(sectionTitleLabel);
		addComponent(row1);
		addComponent(row2);
		addComponent(row3);
	}

}
