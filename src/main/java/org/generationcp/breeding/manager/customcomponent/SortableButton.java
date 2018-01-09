package org.generationcp.breeding.manager.customcomponent;

import com.vaadin.ui.Button;

/**
 * Note: this class has a natural ordering that is inconsistent with equals
 */
public class SortableButton extends Button implements Comparable<Button> {

	public SortableButton(String caption, ClickListener listener) {
		super(caption, listener);
	}

	public SortableButton() {
		super();
	}

	@Override
	public int compareTo(final Button o) {
		if (o == null
			|| this == o
			|| super.getCaption() == null
			|| o.getCaption() == null) {
			return 0;
		}
		return super.getCaption().compareTo(o.getCaption());
	}
}
