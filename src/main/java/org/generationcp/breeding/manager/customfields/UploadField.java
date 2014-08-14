package org.generationcp.breeding.manager.customfields;

import org.generationcp.commons.vaadin.theme.Bootstrap;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;

public class UploadField extends org.vaadin.easyuploads.UploadField {
    private static final long serialVersionUID = 1L;
    
    private ClickListener deleteButtonListener;

    private Label noFileLabel;
    
    private String noFileSelectedText;
    
    private String selectedFileText;
    
    public String getNoFileSelectedText() {
        return noFileSelectedText;
    }

    public void setNoFileSelectedText(String noFileSelectedText) {
        this.noFileSelectedText = noFileSelectedText;
    }

    public void setDeleteButtonListener(Button.ClickListener listener) {
        this.deleteButtonListener = listener;
    }
    
    public String getSelectedFileText() {
        return selectedFileText;
    }

    public void setSelectedFileText(String selectedFileText) {
        this.selectedFileText = selectedFileText;
    }

    @Override
    protected void attachDeleteButton(Button b) {
        super.attachDeleteButton(b);
        
        b.addListener(deleteButtonListener);
        
        getRootLayout().removeComponent(upload);
    }
    
    @Override
    protected void buildDefaulLayout() {
        super.buildDefaulLayout();
        upload.addStyleName(Bootstrap.Buttons.INFO.styleName() +" restoreUploadButton");
        noFileLabel = new Label(noFileSelectedText);
        getRootLayout().addComponent(noFileLabel);
    }
    
    @Override
    protected String getDisplayDetails() {
        if (noFileLabel != null) {
            getRootLayout().removeComponent(noFileLabel);
        }
        
        StringBuilder sb = new StringBuilder();
        // Bug #3 will display FileName when no present
        if( lastFileName != null )
        {
            sb.append("<b>" + selectedFileText + "</b>" + "\u00a0");
            sb.append("<i>"+ lastFileName + "</i>");
            sb.append("</br> ");
        }

        Object value = getValue();
        if (showFileStart == false) {
            boolean isByte = getFieldType() == FieldType.BYTE_ARRAY;
            sb.append("Length:\u00a0").append(
                    (isByte
                            ? ((byte[]) value).length
                            : value.toString().length()));
        } else
        {
            sb.append("<em>");
            if (getFieldType() == FieldType.BYTE_ARRAY) {
                byte[] ba = (byte[]) value;
                int shownBytes = MAX_SHOWN_BYTES;
                if (ba.length < MAX_SHOWN_BYTES) {
                    shownBytes = ba.length;
                }
                for (int i = 0; i < shownBytes; i++) {
                    byte b = ba[i];
                    sb.append(Integer.toHexString(b));
                }
                if (ba.length > MAX_SHOWN_BYTES) {
                    sb.append("...");
                    sb.append("(" + ba.length + " bytes)");
                }
            } else {
                String string = value == null ? null : value.toString();
                if (string.length() > 200) {
                    string = string.substring(0, 199) + "...";
                }
                sb.append(string);
            }
            sb.append("</em>");
        }

        return sb.toString();
    }
}