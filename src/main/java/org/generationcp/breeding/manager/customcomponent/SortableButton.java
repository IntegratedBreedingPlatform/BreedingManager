package org.generationcp.breeding.manager.customcomponent;

import com.vaadin.ui.Button;

public class SortableButton extends Button implements Comparable<Button> {

	public SortableButton(String caption, ClickListener listener) {
		super(caption, listener);
	}

	@Override
	public int compareTo(final Button o) {
		if (o == null || this == o) {
			return 0;
		}
		return super.getCaption().compareTo(o.getCaption());
	}
}
