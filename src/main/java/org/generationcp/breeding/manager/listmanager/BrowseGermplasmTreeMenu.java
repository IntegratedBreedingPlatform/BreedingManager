package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.GermplasmQueries;
import org.generationcp.breeding.manager.util.ComponentTree;
import org.generationcp.breeding.manager.util.ComponentTree.ComponentTreeItem;
import org.generationcp.breeding.manager.util.GermplasmDetailModel;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class BrowseGermplasmTreeMenu extends VerticalLayout implements
		InitializingBean, InternationalizableComponent {
	
	public static final String SAVE_TO_LIST = "Germplasm Details - Save to List";
	public static final String MORE_DETAILS = "Germplasm Details - More Details";

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(BrowseGermplasmTreeMenu.class);
	private static final long serialVersionUID = -6789065165873336884L;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
    private GermplasmQueries qQuery;
    private GermplasmDetailModel gDetailModel;
    private GermplasmDetailsComponent basicDetailsComponent;
    private ListManagerMain listManagerMain;
    
    private Button saveToListLink;
    private Button moreDetailsLink;
    
    private Integer germplasmId;
    
    public BrowseGermplasmTreeMenu(Integer germplasmId){
    	this.germplasmId = germplasmId;
    }


	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		qQuery = new GermplasmQueries();
		gDetailModel = qQuery.getGermplasmDetails(this.germplasmId);
		
		basicDetailsComponent = new GermplasmDetailsComponent(gDetailModel);
		
		ComponentTree content = new ComponentTree();
        content.setWidth("95%");
        
        ComponentTreeItem basicDetails = content.addChild(createBasicDetailsHeader(messageSource.getMessage(Message.BASIC_DETAILS)));
        basicDetails.showChild();
        basicDetails.addChild(basicDetailsComponent);
        
        ComponentTreeItem attributesDetails = content.addChild(Util.createHeaderComponent(messageSource.getMessage(Message.ATTRIBUTES)));
        attributesDetails.addChild(Util.createHeaderComponent(messageSource.getMessage(Message.ATTRIBUTES)));
        
        ComponentTreeItem pedigreeDetails = content.addChild(Util.createHeaderComponent(messageSource.getMessage(Message.PEDIGREE_TREE)));
        pedigreeDetails.addChild(Util.createHeaderComponent(messageSource.getMessage(Message.PEDIGREE_TREE)));
		
		
		addComponent(content);
	}
	
	private Component createBasicDetailsHeader (String header) {
		HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setWidth("95%");
		mainLayout.setHeight("25px");
		
        CssLayout layout = new CssLayout();
        layout.setWidth("130px");
        
        Label l1 = new Label("<b>" + header + "</b>",Label.CONTENT_XHTML);
        layout.addComponent(l1);
        
        saveToListLink = new Button(messageSource.getMessage(Message.SAVE_TO_LIST));
		saveToListLink.setData(SAVE_TO_LIST);
		saveToListLink.setImmediate(true);
		saveToListLink.setStyleName(BaseTheme.BUTTON_LINK);
		saveToListLink.addStyleName("link_with_plus_icon");
		
		moreDetailsLink = new Button(messageSource.getMessage(Message.MORE_DETAILS));
		moreDetailsLink.setData(MORE_DETAILS);
		moreDetailsLink.setImmediate(true);
		moreDetailsLink.setStyleName(BaseTheme.BUTTON_LINK);
		moreDetailsLink.addListener( new GidLinkButtonClickListener(this.germplasmId.toString(), true));
        
		HorizontalLayout leftLayout = new HorizontalLayout();
		leftLayout.addComponent(layout);
		leftLayout.addComponent(saveToListLink);
		
        mainLayout.addComponent(leftLayout);
        mainLayout.addComponent(moreDetailsLink);
        mainLayout.setComponentAlignment(leftLayout, Alignment.BOTTOM_LEFT);
        mainLayout.setComponentAlignment(moreDetailsLink, Alignment.BOTTOM_RIGHT);

        return mainLayout;
	}

}
