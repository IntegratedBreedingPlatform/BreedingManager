package org.generationcp.breeding.manager.customcomponent;

import com.vaadin.ui.Button;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class SortableButtonTest {

	@Test
	public void testCompareTo() {
		assertThat(createButton("caption1").compareTo(createButton("caption1")), equalTo(0));
		assertThat(createButton("caption2").compareTo(createButton("caption1")), equalTo(1));
		assertThat(createButton("caption1").compareTo(createButton("caption2")), equalTo(-1));
	}

	private static SortableButton createButton(String caption) {
		return new SortableButton(caption, Mockito.mock(Button.ClickListener.class));
	}
}
