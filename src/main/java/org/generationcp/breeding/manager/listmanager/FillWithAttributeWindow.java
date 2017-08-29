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

package org.generationcp.breeding.manager.listmanager;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

/**
 * This class opens a pop-up window for selecting attribute types available for GIDs to process. It will proceed to fill to add selected
 * attribute type as column to source table, if no existing propery specified, and fill up attribute values for chosen attribute type per
 * germplasm on target table.
 *
 */

@Configurable
public class FillWithAttributeWindow extends BaseSubWindow implements InternationalizableComponent, InitializingBean, BreedingManagerLayout {

	private static final long serialVersionUID = -8850686249688989080L;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private final AddColumnSource addColumnSource;
	private final String targetPropertyId;
	private HorizontalLayout attributeLayout;
	private ComboBox attributeBox;
	private Button okButton;
	private List<UserDefinedField> attributeList;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	public FillWithAttributeWindow(final AddColumnSource addColumnSource, final String targetPropertyId) {
		this.addColumnSource = addColumnSource;
		this.targetPropertyId = targetPropertyId;
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
		this.attributeBox = new ComboBox();
		this.attributeBox.setDebugId("attributeBox");
		this.attributeBox.setNullSelectionAllowed(false);
		this.okButton = new Button();
		this.okButton.setDebugId("okButton");
	}

	@Override
	public void initializeValues() {
		final List<Integer> gids = this.addColumnSource.getGidsToProcess();
		this.attributeList = this.germplasmDataManager.getAttributeTypesByGIDList(gids);

		for (UserDefinedField attribute : this.attributeList) {
			this.attributeBox.addItem(attribute.getFldno());
			this.attributeBox.setItemCaption(attribute.getFldno(), attribute.getFname());
		}
	}

	@Override
	public void addListeners() {
		this.okButton.addListener(new ClickListener() {

			private static final long serialVersionUID = -7472646361265849940L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				final Integer attributeTypeId = (Integer) FillWithAttributeWindow.this.attributeBox.getValue();
				if (attributeTypeId != null) {
					final String attributeType = FillWithAttributeWindow.this.attributeBox.getItemCaption(attributeTypeId).toUpperCase();
					String finalProperty = FillWithAttributeWindow.this.targetPropertyId;
					// Add selected attribute type as column if no existing property was specified
					if (finalProperty == null) {
						FillWithAttributeWindow.this.addColumnSource.addColumn(attributeType);
						finalProperty = attributeType;
					}
					// Generate values for target column
					GermplasmColumnValuesGenerator valuesGenerator =
							new GermplasmColumnValuesGenerator(FillWithAttributeWindow.this.addColumnSource);
					valuesGenerator.fillWithAttribute(attributeTypeId, finalProperty);
				}
				
				// Close pop-up
				Window attributeWindow = ((Button) event.getSource()).getWindow();
				attributeWindow.getParent().removeWindow(attributeWindow);
			}
		});
	}

	@Override
	public void layoutComponents() {
		this.attributeBox.setWidth("300px");

		this.attributeLayout = new HorizontalLayout();
		this.attributeLayout.setDebugId("attributeLayout");
		this.attributeLayout.setMargin(true);
		this.attributeLayout.setSpacing(true);

		this.attributeLayout.addComponent(this.attributeBox);
		this.attributeLayout.addComponent(this.okButton);

		// set window properties
		this.setContent(this.attributeLayout);
		this.setWidth("400px");
		this.setHeight("30px");
		this.center();
		this.setResizable(false);
		this.setModal(true);
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this, Message.FILL_WITH_ATTRIBUTE_WINDOW);
		this.messageSource.setCaption(this.okButton, Message.OK);
	}
}
