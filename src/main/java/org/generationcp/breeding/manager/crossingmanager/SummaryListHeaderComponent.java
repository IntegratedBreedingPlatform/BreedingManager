
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
public class SummaryListHeaderComponent extends VerticalLayout implements BreedingManagerLayout, InitializingBean {

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
	private final String sectionTitle;

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
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		this.sectionTitleLabel = new Label(this.sectionTitle.toUpperCase());
		this.sectionTitleLabel.addStyleName(Bootstrap.Typography.H4.styleName());
		this.sectionTitleLabel.addStyleName(AppConstants.CssStyles.BOLD);

		this.savedAsLabel = new Label(this.messageSource.getMessage(Message.SAVED_AS) + ":");
		this.savedAsLabel.addStyleName(AppConstants.CssStyles.BOLD);

		this.descriptionLabel = new Label(this.messageSource.getMessage(Message.DESCRIPTION_LABEL) + ":");
		this.descriptionLabel.addStyleName(AppConstants.CssStyles.BOLD);

		this.listTypeLabel = new Label(this.messageSource.getMessage(Message.LIST_TYPE) + ":");
		this.listTypeLabel.addStyleName(AppConstants.CssStyles.BOLD);

		this.listDateLabel = new Label(this.messageSource.getMessage(Message.DATE_LABEL) + ":");
		this.listDateLabel.addStyleName(AppConstants.CssStyles.BOLD);

		this.folderPathValue = new Label();

		this.listNameValue = new Label();
		this.listNameValue.addStyleName(AppConstants.CssStyles.BOLD);

		this.descriptionValue = new Label();

		this.listTypeValue = new Label();
		this.listTypeValue.setWidth("150px");

		this.listDateValue = new Label();
	}

	private void retrieveListDetails() {
		try {
			this.list = this.germplasmListManager.getGermplasmListById(this.listId);
		} catch (MiddlewareQueryException e) {
			SummaryListHeaderComponent.LOG.error("Error in getting parent list:" + e.getMessage(), e);
		}
	}

	@Override
	public void initializeValues() {
		if (this.list == null) {
			this.retrieveListDetails();
		}

		this.listNameValue.setValue(this.list.getName());

		String description = this.list.getDescription();
		if (description.length() > 70) {
			description = description.substring(0, SummaryListHeaderComponent.DESCRIPTION_LENGTH) + "...";
		}
		this.descriptionValue.setValue(description);
		this.descriptionValue.setDescription(this.list.getDescription());

		this.listDateValue.setValue(this.list.getDate());

		this.initializeListTypeValue();
		try {
			this.folderPathValue.setValue(Util.generateListFolderPathLabel(this.germplasmListManager, this.list.getParent()));
		} catch (MiddlewareQueryException e) {
			SummaryListHeaderComponent.LOG.error("Error getting list folder path " + e.getMessage());
		}

	}

	private void initializeListTypeValue() {
		this.listTypeValue.setValue(this.list.getType());
		try {
			List<UserDefinedField> listTypes = this.germplasmListManager.getGermplasmListTypes();
			for (UserDefinedField field : listTypes) {
				if (field.getFcode().equals(this.list.getType())) {
					this.listTypeValue.setValue(field.getFname());
				}
			}
		} catch (MiddlewareQueryException e) {
			SummaryListHeaderComponent.LOG.error("Error getting list types " + e.getMessage());
		}
	}

	@Override
	public void addListeners() {
		// not implemented
	}

	@Override
	public void layoutComponents() {
		this.setSpacing(true);

		HorizontalLayout row1 = new HorizontalLayout();
		row1.setSpacing(true);
		row1.addComponent(this.savedAsLabel);
		row1.addComponent(this.folderPathValue);
		row1.addComponent(this.listNameValue);

		HorizontalLayout row2 = new HorizontalLayout();
		row2.setSpacing(true);
		row2.addComponent(this.descriptionLabel);
		row2.addComponent(this.descriptionValue);

		HorizontalLayout row3 = new HorizontalLayout();
		row3.setSpacing(true);
		row3.addComponent(this.listTypeLabel);
		row3.addComponent(this.listTypeValue);
		row3.addComponent(this.listDateLabel);
		row3.addComponent(this.listDateValue);

		this.addComponent(this.sectionTitleLabel);
		this.addComponent(row1);
		this.addComponent(row2);
		this.addComponent(row3);
	}

}
