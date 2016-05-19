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

package org.generationcp.breeding.manager.germplasm.pedigree;

import java.io.File;
import java.io.FileFilter;
import java.util.UUID;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.germplasm.GermplasmQueries;
import org.generationcp.breeding.manager.germplasm.listeners.GermplasmButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmPedigreeGraphComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;
	private final GermplasmQueries qQuery;

	public static final String UPDATE_PEDIGREE_GRAPH_BUTTON_ID = "Update Pedigree Graph";
	private static final Logger LOG = LoggerFactory.getLogger(GermplasmPedigreeGraphComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	private TextField txtLevel;
	private final int gid;
	private Button btnDisplay;
	private Component pedigreeLevelLabel;
	private CheckBox pedigreeDerivativeCheckbox;
	private Panel panelPedigree;
	private static final String BSLASH = "\\";
	private static final String FSLASH = "/";
	private static final int DEFAULT_TREE_LEVEL = 3;

	public GermplasmPedigreeGraphComponent(final int gid, final GermplasmQueries qQuery) {
		super();
		this.gid = gid;
		this.qQuery = qQuery;
	}

	@Override
	public void afterPropertiesSet() {
		this.setSpacing(true);

		final HorizontalLayout hLayout = new HorizontalLayout();
		hLayout.setSpacing(true);
		this.pedigreeLevelLabel = new Label();
		this.txtLevel = new TextField();
		this.txtLevel.setWidth("50px");
		this.txtLevel.setValue(GermplasmPedigreeGraphComponent.DEFAULT_TREE_LEVEL);

		this.pedigreeDerivativeCheckbox = new CheckBox();

		this.btnDisplay = new Button("Display");
		this.btnDisplay.setData(GermplasmPedigreeGraphComponent.UPDATE_PEDIGREE_GRAPH_BUTTON_ID);
		this.btnDisplay.setWidth("80px");
		this.btnDisplay.addListener(new GermplasmButtonClickListener(this));
		this.btnDisplay.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		hLayout.addComponent(this.pedigreeLevelLabel);
		hLayout.addComponent(this.txtLevel);
		hLayout.addComponent(this.pedigreeDerivativeCheckbox);
		hLayout.addComponent(this.btnDisplay);

		this.addComponent(hLayout);
		this.panelPedigree = new Panel();
		this.panelPedigree.setHeight("500px");
		this.panelPedigree.setWidth("100%");
		this.panelPedigree.setScrollable(true);

		this.addComponent(this.panelPedigree);

	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
		this.updatePedigreeGraphButtonClickAction();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this.pedigreeLevelLabel, Message.PEDIGREE_LEVEL_LABEL);
		this.messageSource.setCaption(this.pedigreeDerivativeCheckbox, Message.INCLUDE_DERIVATIVE_LINES);
	}

	public void updatePedigreeGraphButtonClickAction() {

		int treeLevel;
		this.panelPedigree.removeAllComponents();
		try {
			Thread.sleep(350);
		} catch (final InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}
		this.panelPedigree.requestRepaint();

		final String basepath =
				this.getWindow().getApplication().getContext().getBaseDirectory().getAbsolutePath().replace(this.BSLASH, this.FSLASH)
						+ "/WEB-INF/image/";
		final UUID randomUUID = UUID.randomUUID();

		final File directory = new File(basepath);
		final File[] toBeDeleted = directory.listFiles(new FileFilter() {

			@Override
			public boolean accept(final File theFile) {
				if (theFile.isFile()) {
					return theFile.getName().endsWith("_tree.png");
				}
				return false;
			}
		});

		for (final File deletableFile : toBeDeleted) {
			deletableFile.delete();
		}

		if (this.txtLevel.getValue().toString().length() > 0) {
			treeLevel = Integer.valueOf(this.txtLevel.getValue().toString());
		} else {
			treeLevel = GermplasmPedigreeGraphComponent.DEFAULT_TREE_LEVEL;
		}

		final String graphName = randomUUID.toString() + "_tree";
		final CreatePedigreeGraph pedigree =
				new CreatePedigreeGraph(this.gid, treeLevel, (Boolean) this.pedigreeDerivativeCheckbox.getValue(), this.getWindow(),
						this.qQuery);
		pedigree.create(graphName);

		final FileResource resource = new FileResource(new File(basepath + graphName + ".png"), this.getApplication());

		final Embedded em = new Embedded("", resource);
		em.setMimeType("image/png");
		em.setSizeUndefined();
		em.requestRepaint();

		this.panelPedigree.addComponent(em);
		this.panelPedigree.getContent().setSizeUndefined();
	}

}
