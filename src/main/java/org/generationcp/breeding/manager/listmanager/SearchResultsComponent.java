package org.generationcp.breeding.manager.listmanager;

import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Name;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class SearchResultsComponent extends AbsoluteLayout implements
		InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 5314653969843976836L;

	private Label matchingListsLabel;
	private Label matchingListsDescription;
	private Table matchingListsTable;
	
	private Label matchingGermplasmsLabel;
	private Label matchingGermplasmsDescription;
	private Table matchingGermplasmsTable;
	
	public static final String MATCHING_GEMRPLASMS_TABLE_DATA = "Matching Germplasms Table";
	public static final String MATCHING_LISTS_TABLE_DATA = "Matching Lists Table";
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterPropertiesSet() throws Exception {

		matchingListsLabel = new Label();
		matchingListsLabel.setValue(messageSource.getMessage(Message.MATCHING_LISTS)+": 0");
		matchingListsLabel.addStyleName("gcp-content-title");
		
		matchingListsDescription = new Label();
		matchingListsDescription.setValue(messageSource.getMessage(Message.SELECT_A_LIST_TO_VIEW_THE_DETAILS));
		
		matchingListsTable = new Table();
		matchingListsTable.setData(MATCHING_LISTS_TABLE_DATA);
		matchingListsTable.addContainerProperty("NAME", String.class, null);
		matchingListsTable.addContainerProperty("DESCRIPTION", String.class, null);
		matchingListsTable.setWidth("350px");
		matchingListsTable.setHeight("140px");
		matchingListsTable.setMultiSelect(false);
		matchingListsTable.setSelectable(true);
		
		matchingGermplasmsLabel = new Label();
		matchingGermplasmsLabel.setValue(messageSource.getMessage(Message.MATCHING_GERMPLASM)+": 0");
		matchingGermplasmsLabel.addStyleName("gcp-content-title");
		
		matchingGermplasmsDescription = new Label();
		matchingGermplasmsDescription.setValue(messageSource.getMessage(Message.SELECT_A_GERMPLASM_TO_VIEW_THE_DETAILS));
		
		matchingGermplasmsTable = new Table();
		matchingGermplasmsTable.setData(MATCHING_GEMRPLASMS_TABLE_DATA);
		matchingGermplasmsTable.addContainerProperty("GID", Button.class, null);
		matchingGermplasmsTable.addContainerProperty("NAMES", String.class,null);
		matchingGermplasmsTable.addContainerProperty("PARENTAGE", String.class,null);
		matchingGermplasmsTable.setWidth("350px");
		matchingGermplasmsTable.setHeight("200px");
		matchingGermplasmsTable.setMultiSelect(false);
		matchingGermplasmsTable.setSelectable(true);
		
		matchingGermplasmsTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
			private static final long serialVersionUID = 1L;

			public String generateDescription(Component source, Object itemId,
					Object propertyId) {
				if(propertyId=="NAMES"){
					Item item = matchingGermplasmsTable.getItem(itemId);
					Integer gid = Integer.valueOf(((Button) item.getItemProperty("GID").getValue()).getCaption());
					return getGermplasmNames(gid);
				} else {
					return null;
				}
			}
        });
		
		addComponent(matchingListsLabel, "top:0px; left:0px;");
		addComponent(matchingListsDescription, "top:20px; left:0px;");
		addComponent(matchingListsTable, "top:40px; left:0px;");
		
		addComponent(matchingGermplasmsLabel, "top:195px; left:0px;");
		addComponent(matchingGermplasmsDescription, "top:215px; left:0px;");
		addComponent(matchingGermplasmsTable, "top:235px; left:0px;");
	}

		
	public void applyGermplasmListResults(List<GermplasmList> germplasmLists){
		matchingListsLabel.setValue(messageSource.getMessage(Message.MATCHING_LISTS)+": "+String.valueOf(germplasmLists.size()));
		matchingListsTable.removeAllItems();
		for(GermplasmList germplasmList:germplasmLists){
			matchingListsTable.addItem(new Object[]{germplasmList.getName(),germplasmList.getDescription()},germplasmList.getId());
		}
	}
	
	public void applyGermplasmResults(List<Germplasm> germplasms){
		matchingGermplasmsLabel.setValue(messageSource.getMessage(Message.MATCHING_GERMPLASM)+": "+String.valueOf(germplasms.size()));
		matchingGermplasmsTable.removeAllItems();
		for(Germplasm germplasm:germplasms){

        	Button gidButton = new Button(String.format("%s", germplasm.getGid().toString()), new GidLinkButtonClickListener(germplasm.getGid().toString(), true));
            gidButton.setStyleName(BaseTheme.BUTTON_LINK);
			
			String shortenedNames = getShortenedGermplasmNames(germplasm.getGid());
			
            String crossExpansion = "";
            if(germplasm!=null){
            	try {
            		if(germplasmDataManager!=null)
            			crossExpansion = germplasmDataManager.getCrossExpansion(germplasm.getGid(), 1);
            	} catch(MiddlewareQueryException ex){
                    crossExpansion = "-";
                }
        	}
            
            matchingGermplasmsTable.addItem(new Object[]{gidButton, shortenedNames, crossExpansion},germplasm.getGid());
		}
		
	}

    private String getGermplasmNames(int gid) throws InternationalizableException {

        try {
            List<Name> names = germplasmDataManager.getNamesByGID(new Integer(gid), null, null);
            StringBuffer germplasmNames = new StringBuffer("");
            int i = 0;
            for (Name n : names) {
                if (i < names.size() - 1) {
                    germplasmNames.append(n.getNval() + ", ");
                } else {
                    germplasmNames.append(n.getNval());
                }
                i++;
            }

            return germplasmNames.toString();
        } catch (MiddlewareQueryException e) {
            return null;
        }
    }	
	
    private String getShortenedGermplasmNames(int gid) throws InternationalizableException {
        try {
            List<Name> names = germplasmDataManager.getNamesByGID(new Integer(gid), null, null);
            StringBuffer germplasmNames = new StringBuffer("");
            int i = 0;
            for (Name n : names) {
                if (i < names.size() - 1) {
                    germplasmNames.append(n.getNval() + ", ");
                } else {
                    germplasmNames.append(n.getNval());
                }
                i++;
            }
            String n = germplasmNames.toString();
            if(n.length()>20){
            	n = n.substring(0, 20) + "...";
            }
            return n;
        } catch (MiddlewareQueryException e) {
            return null;
        }
    }
	
    public Table getMatchingGermplasmsTable(){
    	return matchingGermplasmsTable;
    }
    
    public Table getMatchingListsTable(){
    	return matchingListsTable;
    }    
	
}
