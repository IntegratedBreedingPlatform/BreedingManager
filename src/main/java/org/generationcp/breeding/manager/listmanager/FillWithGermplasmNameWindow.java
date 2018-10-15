/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * <p/>
 * Generation Challenge Programme (GCP)
 * <p/>
 * <p/>
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package org.generationcp.breeding.manager.listmanager;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.listeners.FillWithGermplasmNameButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;

/**
 * This class opens a pop-up window for selecting name types available for all GIDs of source screen. It will proceed to fill to add
 * selected name type as column to source table, if no existing property specified, and fill up names for chosen name type per germplasm on
 * target table.
 *
 */

@Configurable
public class FillWithGermplasmNameWindow extends BaseSubWindow
		implements InternationalizableComponent, InitializingBean, BreedingManagerLayout {

	private static final long serialVersionUID = -8850686249688989080L;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private AddColumnSource addColumnSource;
	private final String targetPropertyId;
	private HorizontalLayout layout;
	private ComboBox namesTypeBox;
	private Button okButton;
	private List<UserDefinedField> nameTypesList;
	private final boolean isFromGermplasmSearchWindow;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	public FillWithGermplasmNameWindow(final AddColumnSource addColumnSource, final String targetPropertyId,
			final boolean isFromGermplasmSearchWindow) {
		this.addColumnSource = addColumnSource;
		this.targetPropertyId = targetPropertyId;
		this.isFromGermplasmSearchWindow = isFromGermplasmSearchWindow;
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
		this.namesTypeBox = new ComboBox();
		this.namesTypeBox.setDebugId("namesTypeBox");
		this.namesTypeBox.setNullSelectionAllowed(false);
		this.okButton = new Button();
		this.okButton.setDebugId("okButton");
	}

	@Override
	public void initializeValues() {
		final List<Integer> gids = this.addColumnSource.getAllGids();
		this.nameTypesList = this.germplasmDataManager.getNameTypesByGIDList(gids);

		for (final UserDefinedField nameType : this.nameTypesList) {
			if (!this.addColumnSource.columnExists(nameType.getFname())) {
				this.namesTypeBox.addItem(nameType.getFldno());
				this.namesTypeBox.setItemCaption(nameType.getFldno(), nameType.getFname().toUpperCase());
			}
		}
	}

	@Override
	public void addListeners() {
		this.okButton.addListener(new FillWithGermplasmNameButtonClickListener(this.addColumnSource, this.namesTypeBox,
				this.targetPropertyId, this.isFromGermplasmSearchWindow));
	}

	@Override
	public void layoutComponents() {
		this.namesTypeBox.setWidth("300px");

		this.layout = new HorizontalLayout();
		this.layout.setDebugId("attributeLayout");
		this.layout.setMargin(true);
		this.layout.setSpacing(true);

		this.layout.addComponent(this.namesTypeBox);
		this.layout.addComponent(this.okButton);

		// set window properties
		this.setContent(this.layout);
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
		this.messageSource.setCaption(this, Message.FILL_WITH_GERMPLASM_NAME_TYPE);
		this.messageSource.setCaption(this.okButton, Message.OK);
	}

	public AddColumnSource getAddColumnSource() {
		return this.addColumnSource;
	}

	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	public ComboBox getNamesTypeBox() {
		return this.namesTypeBox;
	}

	public Button getOkButton() {
		return this.okButton;
	}
}
