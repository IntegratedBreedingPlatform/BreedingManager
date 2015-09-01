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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

/**
 * This class opens a pop-up window for selecting attributes
 *
 * @author Mark Agarrado
 */

@Configurable
public class FillWithAttributeWindow extends BaseSubWindow implements InternationalizableComponent, InitializingBean, BreedingManagerLayout {

	private static final long serialVersionUID = -8850686249688989080L;

	private static final Logger LOG = LoggerFactory.getLogger(FillWithAttributeWindow.class);

	private final SimpleResourceBundleMessageSource messageSource;

	private final Table targetTable;
	private final String gidPropertyId;
	private final String targetPropertyId;
	private HorizontalLayout attributeLayout;
	private ComboBox attributeBox;
	private Button okButton;
	private List<UserDefinedField> attributeList;
	private ListTabComponent listDetailsComponent;
	private org.generationcp.breeding.manager.listmanager.ListBuilderComponent buildListComponent;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	public FillWithAttributeWindow(Table targetTable, String gidPropertyId, String targetPropertyId,
			SimpleResourceBundleMessageSource messageSource) {
		this.targetTable = targetTable;
		this.gidPropertyId = gidPropertyId;
		this.targetPropertyId = targetPropertyId;
		this.messageSource = messageSource;
	}

	public FillWithAttributeWindow(Table targetTable, String gidPropertyId, String targetPropertyId,
			SimpleResourceBundleMessageSource messageSource, ListTabComponent listDetailsComponent,
			org.generationcp.breeding.manager.listmanager.ListBuilderComponent buildListComponent) {
		this.targetTable = targetTable;
		this.gidPropertyId = gidPropertyId;
		this.targetPropertyId = targetPropertyId;
		this.messageSource = messageSource;
		this.listDetailsComponent = listDetailsComponent;
		this.buildListComponent = buildListComponent;
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
		this.attributeBox.setNullSelectionAllowed(false);
		this.okButton = new Button();
	}

	@Override
	public void initializeValues() {
		try {
			List<Integer> gids = this.getGidsFromTable(this.targetTable);
			this.attributeList = this.germplasmDataManager.getAttributeTypesByGIDList(gids);

			for (UserDefinedField attribute : this.attributeList) {
				this.attributeBox.addItem(attribute.getFldno());
				this.attributeBox.setItemCaption(attribute.getFldno(), attribute.getFname());
			}
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public void addListeners() {
		this.okButton.addListener(new ClickListener() {

			private static final long serialVersionUID = -7472646361265849940L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				FillWithAttributeWindow.this.fillWithAttribute((Integer) FillWithAttributeWindow.this.attributeBox.getValue());
				// close pop-up
				Window attributeWindow = ((Button) event.getSource()).getWindow();
				attributeWindow.getParent().removeWindow(attributeWindow);
			}
		});
	}

	@Override
	public void layoutComponents() {
		this.attributeBox.setWidth("300px");

		this.attributeLayout = new HorizontalLayout();
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

	private void fillWithAttribute(Integer attributeType) {
		if (attributeType != null) {
			try {
				List<Integer> gids = this.getGidsFromTable(this.targetTable);
				Map<Integer, String> gidAttributeMap = this.germplasmDataManager.getAttributeValuesByTypeAndGIDList(attributeType, gids);

				List<Integer> itemIds = this.getItemIds(this.targetTable);
				for (Integer itemId : itemIds) {
					Integer gid =
							Integer.valueOf(((Button) this.targetTable.getItem(itemId).getItemProperty(this.gidPropertyId).getValue())
									.getCaption().toString());
					this.targetTable.getItem(itemId).getItemProperty(this.targetPropertyId).setValue(gidAttributeMap.get(gid));
				}
			} catch (MiddlewareQueryException e) {
				LOG.error(e.getMessage(), e);
			}
		}

		// mark flag that changes have been made in listDataTable
		if (this.listDetailsComponent != null) {
			this.listDetailsComponent.getListComponent().setHasUnsavedChanges(true);
		}

		if (this.buildListComponent != null) {
			this.buildListComponent.setHasUnsavedChanges(true);
		}
	}

	private List<Integer> getGidsFromTable(Table table) {
		List<Integer> gids = new ArrayList<Integer>();
		List<Integer> listDataItemIds = this.getItemIds(table);
		for (Integer itemId : listDataItemIds) {
			gids.add(Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(this.gidPropertyId).getValue()).getCaption()
					.toString()));
		}
		return gids;
	}

	@SuppressWarnings("unchecked")
	private List<Integer> getItemIds(Table table) {
		List<Integer> itemIds = new ArrayList<Integer>();
		itemIds.addAll((Collection<? extends Integer>) table.getItemIds());
		return itemIds;
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
