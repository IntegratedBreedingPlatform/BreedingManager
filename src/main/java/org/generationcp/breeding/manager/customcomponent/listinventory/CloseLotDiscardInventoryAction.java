package org.generationcp.breeding.manager.customcomponent.listinventory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Window;

@Configurable
public class CloseLotDiscardInventoryAction implements Serializable {

	private static final long serialVersionUID = -9047259985457065559L;

	private List<CloseLotDiscardInventoryListener> closeLotListener = new ArrayList<>();

	private ListComponent source;
	private List<ListEntryLotDetails> lotDetails;

	public CloseLotDiscardInventoryAction(ListComponent source) {
		this.source = source;
	}

	public void processLotCloseWithDiscard() {
		for (ListEntryLotDetails lotDetail : this.lotDetails) {
			CloseLotDiscardInventoryConfirmDialog closeLotDiscardInventoryConfirmDialog =
					new CloseLotDiscardInventoryConfirmDialog(this.source, this, lotDetail);
			this.addCloseLotListener(closeLotDiscardInventoryConfirmDialog);
		}

	}

	public void showClotLotListener(final CloseLotDiscardInventoryListener listener) {
		if (listener instanceof Window) {
			this.source.getWindow().addWindow((Window) listener);
		}

	}

	public void addCloseLotListener(final CloseLotDiscardInventoryListener listener) {
		if (this.closeLotListener.isEmpty()) {
			this.showClotLotListener(listener);
		}
		this.closeLotListener.add(listener);
	}

	public void removeCurrentCloseLotListenerAndProcessNextItem(final CloseLotDiscardInventoryListener listener) {
		this.removeClotLotListener(listener);
		this.processNextClotLotListenerItems();
	}

	public void removeClotLotListener(final CloseLotDiscardInventoryListener importEntryListener) {
		this.closeLotListener.remove(importEntryListener);
	}

	public void closeAllLotCloseListeners() {
		for (int i = 0; i < this.closeLotListener.size(); i++) {
			final CloseLotDiscardInventoryListener listener = this.closeLotListener.get(i);
			if (listener instanceof Window) {
				final Window window = (Window) listener;
				this.source.getWindow().removeWindow(window);
			}
		}
		this.closeLotListener.clear();
	}

	public void processNextClotLotListenerItems() {
		final Iterator<CloseLotDiscardInventoryListener> listenersIterator = this.closeLotListener.iterator();
		if (!listenersIterator.hasNext()) {
			return;
		}
		final CloseLotDiscardInventoryListener listener = listenersIterator.next();
		this.showClotLotListener(listener);
	}

	public List<CloseLotDiscardInventoryListener> getCloseLotListener() {
		return closeLotListener;
	}

	public void setCloseLotListener(List<CloseLotDiscardInventoryListener> closeLotListener) {
		this.closeLotListener = closeLotListener;
	}

	public void setLotDetails(List<ListEntryLotDetails> lotDetails) {
		this.lotDetails = lotDetails;
	}
}
