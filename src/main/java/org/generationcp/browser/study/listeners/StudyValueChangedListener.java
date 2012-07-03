/***************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * @author Kevin L. Manansala
 * 
 *         This software is licensed for use under the terms of the GNU General
 *         Public License (http://bit.ly/8Ztv8M) and the provisions of Part F of
 *         the Generation Challenge Programme Amended Consortium Agreement
 *         (http://bit.ly/KQX1nL)
 * 
 **************************************************************/

package org.generationcp.browser.study.listeners;

import org.generationcp.browser.study.StudyEffectComponent;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Layout;

public class StudyValueChangedListener implements Property.ValueChangeListener{

    private static final long serialVersionUID = -5550338215403241759L;

    private Layout source;

    public StudyValueChangedListener(Layout source) {
        this.source = source;
    }

    @Override
    public void valueChange(ValueChangeEvent event) {
        if (source instanceof StudyEffectComponent) {
            ((StudyEffectComponent) source).datasetListValueChangeAction(event.getProperty().toString());
        }
    }

}
