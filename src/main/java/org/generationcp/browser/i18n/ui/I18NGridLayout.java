package org.generationcp.browser.i18n.ui;

import java.util.Locale;

import com.github.peholmst.i18n4vaadin.I18N;
import com.github.peholmst.i18n4vaadin.I18NComponent;
import com.github.peholmst.i18n4vaadin.I18NListener;
import com.github.peholmst.i18n4vaadin.support.I18NComponentSupport;
import com.vaadin.ui.GridLayout;


public class I18NGridLayout extends GridLayout implements
		I18NComponent, I18NListener {

	private static final long serialVersionUID = -8664895710378325629L;
	private final I18NComponentSupport support;

	public I18NGridLayout(I18N i18n) {
		
		super();
		support = new I18NComponentSupport(this);
		support.setI18N(i18n);
	}
	
    public I18NGridLayout(int columns, int rows, I18N i18n) {
    	
    	this(i18n);
        setColumns(columns);
        setRows(rows);
    }
	
	@Override
	public void setI18N(I18N i18n) {
		support.setI18N(i18n);
	}

	@Override
	public I18N getI18N() {
		return support.getI18N();
	}

	@Override
	public void attach() {
		super.attach();
		//getI18N().addListener(this);
		updateLabels();
	}

	@Override
	public void detach() {
		//getI18N().removeListener(this);
		super.detach();
	}

	@Override
	public void localeChanged(I18N sender, Locale oldLocale, Locale newLocale) {
		updateLabels();
	}

	/** 
	 * Change the label of the components based on the selected Locale
	 * Override if there's a need to change the labels on the fly
	 */
	protected void updateLabels() {
	}


}
