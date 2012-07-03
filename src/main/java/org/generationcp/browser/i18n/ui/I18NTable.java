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

package org.generationcp.browser.i18n.ui;

import java.util.Locale;

import com.github.peholmst.i18n4vaadin.I18N;
import com.github.peholmst.i18n4vaadin.I18NComponent;
import com.github.peholmst.i18n4vaadin.I18NListener;
import com.github.peholmst.i18n4vaadin.support.I18NComponentSupport;
import com.vaadin.data.Container;
import com.vaadin.ui.Table;

public class I18NTable extends Table implements I18NComponent, I18NListener{

    private static final long serialVersionUID = -8664895710378325629L;
    private final I18NComponentSupport support;

    public I18NTable(I18N i18n) {
        super();
        support = new I18NComponentSupport(this);
        support.setI18N(i18n);
    }

    public I18NTable(String caption, I18N i18n) {
        this(i18n);
        setCaption(caption);
    }

    /**
     * Creates a new table with caption and connect it to a Container.
     * 
     * @param caption
     * @param dataSource
     * @param i18n
     */
    public I18NTable(String caption, Container dataSource, I18N i18n) {

        this(i18n);
        setCaption(caption);
        setContainerDataSource(dataSource);
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
        // getI18N().addListener(this);
        updateLabels();
    }

    @Override
    public void detach() {
        // getI18N().removeListener(this);
        super.detach();
    }

    @Override
    public void localeChanged(I18N sender, Locale oldLocale, Locale newLocale) {
        updateLabels();
    }

    /**
     * Change the label of the components based on the selected Locale Override
     * if there's a need to change the labels on the fly
     */
    protected void updateLabels() {
    }

}
