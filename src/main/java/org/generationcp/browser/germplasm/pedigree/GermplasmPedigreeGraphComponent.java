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
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.UUID;

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
import com.vaadin.ui.CheckBox;
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
    private CheckBox pedigreeDerivativeCheckbox;
    private Panel panelPedigree;
    private static final String PEDIGREE_IMAGE_PATH = "../gcp-default/graph/";
    private String BSLASH = "\\";
    private String FSLASH = "/";
    private static final int DEFAULT_TREE_LEVEL=3;

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
        txtLevel.setValue(DEFAULT_TREE_LEVEL);
        
        pedigreeDerivativeCheckbox = new CheckBox();
        
        btnDisplay = new Button("Display");
        btnDisplay.setData(UPDATE_PEDIGREE_GRAPH_BUTTON_ID);
        btnDisplay.setWidth("80px");
        btnDisplay.addListener(new GermplasmButtonClickListener(this));

        hLayout.addComponent(pedigree_level_label);
        hLayout.addComponent(txtLevel);
        hLayout.addComponent(pedigreeDerivativeCheckbox);
        hLayout.addComponent(btnDisplay);

        addComponent(hLayout);
        panelPedigree= new Panel();
        panelPedigree.setHeight("500px");
        panelPedigree.setWidth("100%");
        panelPedigree.setScrollable(true);
    
        addComponent(panelPedigree);
        
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
        try {
            updatePedigreeGraphButtonClickAction();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MiddlewareQueryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Override
    public void updateLabels() {
        messageSource.setCaption(pedigree_level_label, Message.PEDIGREE_LEVEL_LABEL);
        messageSource.setCaption(pedigreeDerivativeCheckbox, Message.INCLUDE_DERIVATIVE_LINES);
    }

    public void updatePedigreeGraphButtonClickAction() throws FileNotFoundException, URISyntaxException, MiddlewareQueryException {

        int treeLevel;
        panelPedigree.removeAllComponents();
        try {
            Thread.sleep(350);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        panelPedigree.requestRepaint();

//        String graphName="Pedigree_"+String.valueOf(this.gid)+"_"+String.valueOf(txtLevel.getValue().toString()+"_"+cal.getTimeInMillis());
        //String graphName="Pedigree";
        
        String basepath = getWindow().getApplication().getContext().getBaseDirectory().getAbsolutePath().replace(BSLASH, FSLASH)+"/WEB-INF/image/";
        UUID randomUUID = UUID.randomUUID();
        
        File directory = new File(basepath);  
        File[] toBeDeleted = directory.listFiles(new FileFilter() {  
            public boolean accept(File theFile) {  
                if (theFile.isFile()) {  
                    return theFile.getName().endsWith("_tree.png");  
                }  
                return false;  
            }  
        });  
           
        for(File deletableFile:toBeDeleted){  
            deletableFile.delete();  
        }        
        
        if(txtLevel.getValue().toString().length() > 0){
            treeLevel=Integer.valueOf(txtLevel.getValue().toString());
        }else{
            treeLevel=DEFAULT_TREE_LEVEL;
        }
        
        String graphName = randomUUID.toString()+"_tree";
        CreatePedigreeGraph pedigree= new CreatePedigreeGraph(this.gid,treeLevel,(Boolean) pedigreeDerivativeCheckbox.getValue(),this.getWindow(),this.qQuery);
        pedigree.create(graphName);

        FileResource resource =new FileResource(new File(basepath + graphName + ".png"),this.getApplication());

        Embedded em = new Embedded("", resource);
        em.setMimeType("image/png");
        em.setSizeUndefined();
        em.requestRepaint();

        panelPedigree.addComponent(em);
        panelPedigree.getContent().setSizeUndefined();
        
        
    }

}
