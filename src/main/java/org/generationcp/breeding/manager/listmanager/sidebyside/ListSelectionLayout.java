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
package org.generationcp.breeding.manager.listmanager.sidebyside;

import com.vaadin.ui.*;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.util.ListManagerDetailsTabCloseHandler;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.themes.Runo;

/**
 * @author Mark Agarrado
 */
@Configurable
public class ListSelectionLayout extends VerticalLayout implements InternationalizableComponent, InitializingBean, BreedingManagerLayout {

    protected static final Logger LOG = LoggerFactory.getLogger(ListSelectionLayout.class);
    private static final long serialVersionUID = -6583178887344009055L;
    
    public static final String CLOSE_ALL_TABS_ID = "ListManagerDetailsLayout Close All Tabs ID";
    public static final String TAB_DESCRIPTION_PREFIX = "List ID: ";
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private final ListManagerMain listManagerMain;
    
    private Label headingLabel;
    private Label noListLabel;
    private Label defaultLabel;
    
    private Button btnCloseAllTabs;
    private Button browseForLists;
    private Button searchForLists;
    private Label or;
    private Label toWorkWith;

    private HorizontalLayout headerLayout;
    private VerticalLayout innerLayout;
    
    private TabSheet detailsTabSheet;
    
    private final Integer listId;
    private Button listBuilderToggleBtn;

    public ListSelectionLayout(final ListManagerMain listManagerMain, final Integer listId) {
    	super();
        this.listManagerMain = listManagerMain;
        this.listId = listId;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        instantiateComponents();
        initializeValues();
        layoutComponents();
        addListeners();
        
        if(listId != null){
        	try{
        		createListDetailsTab(listId);
        	} catch(MiddlewareQueryException ex){
        		LOG.error("Error with opening list details tab of list with id: " + listId);
        	}
        }
        else{
        	displayDefault();
        }
    }
    
    @Override
    public void instantiateComponents() {
        listBuilderToggleBtn = new Button("<span class='fa fa-chevron-left'" +
                "style='font-size: 16px;" +
                "position: relative;" +
                "right: 3px;" +
                "top: 1px;'></span>" + "SHOW LIST BUILDER");
        listBuilderToggleBtn.setHtmlContentAllowed(true);
        listBuilderToggleBtn.setStyleName(Bootstrap.Buttons.BORDERED.styleName());

        noListLabel = new Label();
    	noListLabel.setImmediate(true);
    	
    	headingLabel = new Label();
    	headingLabel.setImmediate(true);
    	headingLabel.setWidth("300px");
    	headingLabel.setStyleName(Bootstrap.Typography.H4.styleName());
    	headingLabel.addStyleName(AppConstants.CssStyles.BOLD);
    	
    	defaultLabel = new Label();
    	
    	headerLayout = new HorizontalLayout();
    	
    	detailsTabSheet = new TabSheet();
    	detailsTabSheet.setWidth("100%");
    	detailsTabSheet.addStyleName("listDetails");
    	
        btnCloseAllTabs = new Button(messageSource.getMessage(Message.CLOSE_ALL_TABS));
        btnCloseAllTabs.setData(CLOSE_ALL_TABS_ID);
        btnCloseAllTabs.setImmediate(true);
        btnCloseAllTabs.setStyleName(Reindeer.BUTTON_LINK);
        
        browseForLists = new Button();
        browseForLists.setImmediate(true);
        browseForLists.setStyleName(Reindeer.BUTTON_LINK);
        
        searchForLists = new Button();
        searchForLists.setImmediate(true);
        searchForLists.setStyleName(Reindeer.BUTTON_LINK);
        
        or = new Label();
        or.setImmediate(true);
        
        toWorkWith = new Label();
        toWorkWith.setImmediate(true);

    }

    @Override
    public void initializeValues() {
        headingLabel.setValue(messageSource.getMessage(Message.MANAGE_LISTS));
        defaultLabel.setValue(messageSource.getMessage(Message.BROWSE_LIST_DEFAULT_MESSAGE)); 
        browseForLists.setCaption(messageSource.getMessage(Message.BROWSE_FOR_A_LIST) + " ");
        searchForLists.setCaption(messageSource.getMessage(Message.SEARCH_FOR_A_LIST) + " ");
        or.setValue(messageSource.getMessage(Message.OR) + " ");
        toWorkWith.setValue(messageSource.getMessage(Message.A_LIST_TO_WORK_WITH));
    }
    
    @Override
    public void layoutComponents() {

        final HorizontalLayout listSelectionHeaderContainer = new HorizontalLayout();
        listSelectionHeaderContainer.setWidth("100%");

        final HeaderLabelLayout headerLbl = new HeaderLabelLayout(AppConstants.Icons.ICON_REVIEW_LIST_DETAILS,headingLabel);

        final HorizontalLayout searchOrBrowseLayout = new HorizontalLayout();
        // Ugh, bit of a hack - can't figure out how to space these nicely
        searchForLists.setWidth("43px");
        or.setWidth("16px");
        browseForLists.setWidth("48px");

        searchOrBrowseLayout.addComponent(searchForLists);
        searchOrBrowseLayout.addComponent(or);
        searchOrBrowseLayout.addComponent(browseForLists);
        searchOrBrowseLayout.addComponent(toWorkWith);

        final VerticalLayout headerAndDesc = new VerticalLayout();
        headerAndDesc.setSpacing(true);
        headerAndDesc.addComponent(noListLabel);
        headerAndDesc.addComponent(headerLbl);
        headerAndDesc.addComponent(searchOrBrowseLayout);

        final VerticalLayout headerBtnContainer = new VerticalLayout();
        headerBtnContainer.setSizeUndefined();
        headerBtnContainer.setSpacing(true);
        headerBtnContainer.addComponent(listBuilderToggleBtn);
        headerBtnContainer.addComponent(btnCloseAllTabs);
        headerBtnContainer.setComponentAlignment(btnCloseAllTabs,Alignment.BOTTOM_RIGHT);

        listSelectionHeaderContainer.addComponent(headerAndDesc);
        listSelectionHeaderContainer.addComponent(headerBtnContainer);
        listSelectionHeaderContainer.setExpandRatio(headerAndDesc,1.0F);
        listSelectionHeaderContainer.setComponentAlignment(headerBtnContainer,Alignment.TOP_RIGHT);

        this.addComponent(listSelectionHeaderContainer);
        this.addComponent(detailsTabSheet);
        this.setExpandRatio(detailsTabSheet,1.0f);




        /*
        * setWidth("100%");
    	setStyleName(Reindeer.TABSHEET_SMALL);
        setMargin(false);

        setDetailsTabSheetHeight();

    	//Components
        headerLayout.setSizeUndefined();
        final HeaderLabelLayout headerLbl = new HeaderLabelLayout(AppConstants.Icons.ICON_REVIEW_LIST_DETAILS, headingLabel);
        headerLbl.addStyleName("lm-title");
        headerLbl.setHeight("30px");

        headerLayout.addComponent(headerLbl);

        innerLayout = new VerticalLayout();
        innerLayout.setSizeUndefined();

        final VerticalLayout headerBtnContainer = new VerticalLayout();
        headerBtnContainer.setSpacing(true);
        headerBtnContainer.addComponent(listBuilderToggleBtn);
        headerBtnContainer.addComponent(btnCloseAllTabs);

        final HorizontalLayout listSelectionHeader = new HorizontalLayout();
        listSelectionHeader.setWidth("100%");

        listSelectionHeader.addComponent(innerLayout);
        listSelectionHeader.addComponent(headerBtnContainer);
        listSelectionHeader.setExpandRatio(innerLayout,1.0F);




        searchOrBrowseLayout.addComponent(searchForLists);
        searchOrBrowseLayout.addComponent(or);
        searchOrBrowseLayout.addComponent(browseForLists);
        searchOrBrowseLayout.addComponent(toWorkWith);

        innerLayout.addComponent(noListLabel);
        innerLayout.addComponent(headerLayout);
        innerLayout.addComponent(searchOrBrowseLayout);

        addComponent(listSelectionHeader);
        addComponent(detailsTabSheet);

        *
        * */
        this.displayDefault();
        this.setSizeFull();
    }
    
    public void setDetailsTabSheetHeight() {
    	detailsTabSheet.setHeight("534px");
	}

	public void displayDefault(){
    	noListLabel.setVisible(false);
        headerLayout.setVisible(true);
        btnCloseAllTabs.setVisible(false);
        detailsTabSheet.setVisible(false);
    }

    @Override
    public void addListeners() {
        ListManagerDetailsTabCloseHandler closeHandler = new ListManagerDetailsTabCloseHandler(this);
        btnCloseAllTabs.addListener(closeHandler);
        detailsTabSheet.setCloseHandler(closeHandler);
        detailsTabSheet.addListener(new TabSheet.SelectedTabChangeListener() {

            private static final long serialVersionUID = -7822326039221887888L;

            @Override
            public void selectedTabChange(SelectedTabChangeEvent event) {
                if(detailsTabSheet.getComponentCount() <= 1){
                    btnCloseAllTabs.setVisible(false);
                }
                else{
                    btnCloseAllTabs.setVisible(true);
                }
            }
        });
        
        browseForLists.addListener(new Button.ClickListener() {

        	private static final long serialVersionUID = 6385074843600086746L;

			@Override
			public void buttonClick(final ClickEvent event) {
				listManagerMain.getListSelectionComponent().openListBrowseDialog();
			}
        });
        
        searchForLists.addListener(new Button.ClickListener() {

        	private static final long serialVersionUID = 6385074843600086746L;

			@Override
			public void buttonClick(final ClickEvent event) {
				listManagerMain.getListSelectionComponent().openListSearchDialog();
			}
        });

        listBuilderToggleBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                listManagerMain.toggleListBuilder(listBuilderToggleBtn);
            }
        });
    }

    @Override
    public void updateLabels() {
        headingLabel.setValue(messageSource.getMessage(Message.MANAGE_LISTS));
        defaultLabel.setValue(messageSource.getMessage(Message.BROWSE_LIST_DEFAULT_MESSAGE)); 
        browseForLists.setCaption(messageSource.getMessage(Message.BROWSE_FOR_A_LIST) + " ");
        searchForLists.setCaption(messageSource.getMessage(Message.SEARCH_FOR_A_LIST) + " ");
        or.setValue(messageSource.getMessage(Message.OR) + " ");
        toWorkWith.setValue(messageSource.getMessage(Message.A_LIST_TO_WORK_WITH));
    }

    public void createListDetailsTab(Integer listId) throws MiddlewareQueryException{
        GermplasmList germplasmList = germplasmListManager.getGermplasmListById(listId);
        if (germplasmList == null) {
            hideDetailsTabsheet();
            this.noListLabel.setCaption("There is no list in the database with id: " + listId);
            this.noListLabel.setVisible(true);
        } else {
            noListLabel.setVisible(false);
            final String tabName = germplasmList.getName();
            this.createTab(listId, germplasmList, tabName);
            this.showDetailsTabsheet();
        }
    }
    
    private void createTab(final int id, final GermplasmList germplasmList, final String tabName) {
        
    	final boolean tabExists = Util.isTabDescriptionExist(detailsTabSheet, generateTabDescription(germplasmList.getId()));
        
        if (!tabExists) {
            
        	final Component tabContent = new ListTabComponent(listManagerMain, this, germplasmList);
            final Tab tab = detailsTabSheet.addTab(tabContent, tabName, null);
            
            if (germplasmList != null){
                tab.setDescription(generateTabDescription(germplasmList.getId()));
            }
            
            tab.setClosable(true);
            detailsTabSheet.setSelectedTab(tabContent);
            
        } else {
            final Tab tab = Util.getTabWithDescription(detailsTabSheet, generateTabDescription(germplasmList.getId()));

            if (tab != null){
                detailsTabSheet.setSelectedTab(tab.getComponent());
            }
        }
    }
    
    private String generateTabDescription(Integer listId){
        return TAB_DESCRIPTION_PREFIX + listId;
    }
    
    public TabSheet getDetailsTabsheet() {
        return this.detailsTabSheet;
    }
    
    public void showDetailsTabsheet() {
        detailsTabSheet.setVisible(true);
    }
    
    public void hideDetailsTabsheet() {
        btnCloseAllTabs.setVisible(false);
        detailsTabSheet.setVisible(false);
    }
    
    public void repaintTabsheet() {
    	if(detailsTabSheet.isVisible()){
    	    this.removeAllComponents();
    	    this.addComponent(headerLayout);
    	    this.addComponent(innerLayout);
    	    this.addComponent(detailsTabSheet);
    	
            headerLayout.setVisible(true);
            defaultLabel.setVisible(false);
            detailsTabSheet.setVisible(true);
            
            if(detailsTabSheet.getComponentCount() > 1){
            	btnCloseAllTabs.setVisible(true);
            }
            this.requestRepaintAll();
    	}
    }
    
    public void renameTab(Integer listId, String newName){
        String tabDescription = generateTabDescription(listId);
        Tab tab = Util.getTabWithDescription(detailsTabSheet, tabDescription);
        if (tab != null){
            tab.setCaption(newName);
            ListTabComponent listDetails = (ListTabComponent) tab.getComponent();
            listDetails.setListNameLabel(newName);
        }
    }
    
    public void removeTab(Integer listId){
        String tabDescription = generateTabDescription(listId);
        Tab tab = Util.getTabWithDescription(detailsTabSheet, tabDescription);
        if (tab != null){
            detailsTabSheet.removeTab(tab);
        }
        
        if(detailsTabSheet.getComponentCount() == 0){
            this.hideDetailsTabsheet();
        }
    }
    
    /*public void closeAllTab(String tabName){
    	// TODO method stub
    }*/

}
