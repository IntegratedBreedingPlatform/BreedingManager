package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.SearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerDetailsLayout;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;

public class SearchResultsItemClickListener implements ItemClickListener {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(SearchResultsItemClickListener.class);
	private static final long serialVersionUID = 1267325300705610720L;

	private String resultType;
	private ListManagerDetailsLayout detailsLayout;

    public SearchResultsItemClickListener(String resultType, ListManagerDetailsLayout detailsLayout) {
        this.resultType = resultType;
        this.detailsLayout = detailsLayout;
    }
    
	@Override
	public void itemClick(ItemClickEvent event) {
		Integer itemId = (Integer) event.getItemId();
		
		if(!event.isCtrlKey() && !event.isShiftKey()){
			if (resultType.equals(SearchResultsComponent.MATCHING_GEMRPLASMS_TABLE_DATA)){
				detailsLayout.createGermplasmDetailsTab(itemId);
			} else if (resultType.equals(SearchResultsComponent.MATCHING_LISTS_TABLE_DATA)){
			    try{
			        detailsLayout.createListDetailsTab(itemId);
		        } catch (MiddlewareQueryException e){
		            LOG.error("Error in displaying germplasm list details.", e);
		            throw new InternationalizableException(e, Message.ERROR_DATABASE,
		                    Message.ERROR_IN_CREATING_GERMPLASMLIST_DETAILS_WINDOW);
		        }
				
			}
		}

	}

}
