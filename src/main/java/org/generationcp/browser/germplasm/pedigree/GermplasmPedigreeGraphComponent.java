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

package org.generationcp.browser.germplasm.pedigree;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.GermplasmQueries;
import org.generationcp.browser.germplasm.listeners.GermplasmButtonClickListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmPedigreeGraphComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent{

	private static final long serialVersionUID = 1L;
	private GermplasmQueries qQuery;

	public static final String  UPDATE_PEDIGREE_GRAPH_BUTTON_ID="Update Pedigree Graph";
	@SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory.getLogger(GermplasmPedigreeGraphComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	private TextField txtLevel;
	private int gid;
	private Button btnDisplay;
	private Component pedigree_level_label;
	private Panel panelPedigree;
	private static final String PEDIGREE_IMAGE_PATH = "../gcp-default/graph/";
	private String BSLASH = "\\";
	private String FSLASH = "/";

	public GermplasmPedigreeGraphComponent(int gid, GermplasmQueries qQuery) throws InternationalizableException {
		super();
		this.gid=gid;
		this.qQuery=qQuery;
	}


	@Override
	public void afterPropertiesSet() {

		HorizontalLayout hLayout= new HorizontalLayout();
		hLayout.setSpacing(true);
		pedigree_level_label = new Label();
		txtLevel = new TextField();
		txtLevel.setWidth("50px");
		btnDisplay = new Button("Display");
		btnDisplay.setData(UPDATE_PEDIGREE_GRAPH_BUTTON_ID);
		btnDisplay.setWidth("80px");
		btnDisplay.addListener(new GermplasmButtonClickListener(this));

		hLayout.addComponent(pedigree_level_label);
		hLayout.addComponent(txtLevel);
		hLayout.addComponent(btnDisplay);

		addComponent(hLayout);
		panelPedigree= new Panel();
		panelPedigree.setHeight("500px");
		panelPedigree.setWidth("98%");
		panelPedigree.setScrollable(true);
	
		addComponent(panelPedigree);
		
	}

	@Override
	public void attach() {
		super.attach();
		updateLabels();
	}


	@Override
	public void updateLabels() {
		messageSource.setCaption(pedigree_level_label, Message.PEDIGREE_LEVEL_LABEL);
	}

	public void updatePedigreeGraphButtonClickAction() throws FileNotFoundException, URISyntaxException, MiddlewareQueryException {
		
		panelPedigree.removeAllComponents();
		panelPedigree.requestRepaint();

//		String graphName="Pedigree_"+String.valueOf(this.gid)+"_"+String.valueOf(txtLevel.getValue().toString()+"_"+cal.getTimeInMillis());
		String graphName="Pedigree";
		CreatePedigreeGraph pedigree= new CreatePedigreeGraph(this.gid,Integer.valueOf(txtLevel.getValue().toString()),this.getWindow(),this.qQuery);
		pedigree.create(graphName);

		String basepath = getWindow().getApplication().getContext().getBaseDirectory().getAbsolutePath().replace(BSLASH, FSLASH)+"/WEB-INF/image/";

		FileResource resource =new FileResource(new File(basepath +graphName+".png"),this.getApplication());

		Embedded em = new Embedded("", resource);
		em.setMimeType("image/png");
		em.setSizeUndefined();
		em.requestRepaint();

		panelPedigree.addComponent(em);
		panelPedigree.getContent().setSizeUndefined();
		
		
	}

}
