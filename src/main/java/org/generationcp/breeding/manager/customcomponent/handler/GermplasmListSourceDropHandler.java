package org.generationcp.breeding.manager.customcomponent.handler;

import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractSelect;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.GermplasmListSource;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListTreeUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Resource;

@Configurable
public class GermplasmListSourceDropHandler implements DropHandler {

	private static final long serialVersionUID = -6676297159926786216L;

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListSourceDropHandler.class);

	@Resource
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	private final GermplasmListSource targetListSource;
	private final ListSelectorComponent source;
	private final GermplasmListTreeUtil utilSource;

	public GermplasmListSourceDropHandler(final GermplasmListSource targetListSource, final ListSelectorComponent source,
			final GermplasmListTreeUtil utilSource) {
		this.targetListSource = targetListSource;
		this.source = source;
		this.utilSource = utilSource;
	}

	@Override
	public void drop(final DragAndDropEvent dropEvent) {
		final Transferable t = dropEvent.getTransferable();
		if (t.getSourceComponent() != this.targetListSource) {
			return;
		}

		final AbstractSelect.AbstractSelectTargetDetails target = (AbstractSelect.AbstractSelectTargetDetails) dropEvent.getTargetDetails();

		final Object sourceItemId = t.getData("itemId");
		Object targetItemId = target.getItemIdOver();

		final VerticalDropLocation location = target.getDropLocation();

		if (location != VerticalDropLocation.MIDDLE || sourceItemId.equals(targetItemId)) {
			return;
		}

		if (ListSelectorComponent.CROP_LISTS.equals(targetItemId)) {
			final GermplasmList sourceItem = this.germplasmListManager.getGermplasmListById((Integer) sourceItemId);
			if (sourceItem.isFolder()) {
				MessageNotifier
						.showError(dropEvent.getTransferable().getSourceComponent().getWindow(), messageSource.getMessage(Message.ERROR),
								messageSource.getMessage(Message.CANNOT_MOVE_FOLDER_TO_CROP_LISTS_FOLDER));
				return;
			}
		}

		GermplasmList targetList = null;
		try {
			targetList = this.germplasmListManager.getGermplasmListById((Integer) targetItemId);
		} catch (final MiddlewareQueryException e) {
			GermplasmListSourceDropHandler.LOG.error(e.getMessage(), e);
		} catch (final ClassCastException e) {
			GermplasmListSourceDropHandler.LOG.error(e.getMessage(), e);
		}

		// Dropped on a folder / root "Program lists" folder
		if (targetItemId instanceof String || targetList == null || "FOLDER".equalsIgnoreCase(targetList.getType())) {
			this.utilSource.setParent(sourceItemId, targetItemId);
			// Dropped on a list
		} else if (targetList != null) {
			if (targetList.getParentId() == null && (Integer) targetItemId > 0) {
				targetItemId = ListSelectorComponent.PROGRAM_LISTS;
			} else {
				targetItemId = targetList.getParentId();
			}
			this.utilSource.setParent(sourceItemId, targetItemId);
		}

		this.source.refreshRemoteTree();
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		return AcceptAll.get();
	}
}
