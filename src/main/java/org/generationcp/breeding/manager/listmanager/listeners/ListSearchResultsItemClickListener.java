
package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListSelectionLayout;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.MouseEvents.ClickEvent;

public class ListSearchResultsItemClickListener implements ItemClickListener {

	private static final Logger LOG = LoggerFactory.getLogger(ListSearchResultsItemClickListener.class);
	private static final long serialVersionUID = 1267325300705610720L;

	private final ListSelectionLayout detailsLayout;

	public ListSearchResultsItemClickListener(final ListSelectionLayout detailsLayout) {
		this.detailsLayout = detailsLayout;
	}

	@Override
	public void itemClick(ItemClickEvent event) {
		Integer itemId = (Integer) event.getItemId();

		if (!event.isCtrlKey() && !event.isShiftKey()) {
			if (event.getButton() == ClickEvent.BUTTON_LEFT) {
				try {
					this.detailsLayout.createListDetailsTab(itemId);
				} catch (MiddlewareQueryException e) {
					ListSearchResultsItemClickListener.LOG.error("Error in displaying germplasm list details.", e);
					throw new InternationalizableException(e, Message.ERROR_DATABASE,
							Message.ERROR_IN_CREATING_GERMPLASMLIST_DETAILS_WINDOW);
				}
			}
		}
	}
}
