
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
		return this.noFileSelectedText;
	}

	public void setNoFileSelectedText(String noFileSelectedText) {
		this.noFileSelectedText = noFileSelectedText;
	}

	public void setDeleteButtonListener(Button.ClickListener listener) {
		this.deleteButtonListener = listener;
	}

	public String getSelectedFileText() {
		return this.selectedFileText;
	}

	public void setSelectedFileText(String selectedFileText) {
		this.selectedFileText = selectedFileText;
	}

	@Override
	protected void attachDeleteButton(Button b) {
		super.attachDeleteButton(b);

		b.addListener(this.deleteButtonListener);

		this.getRootLayout().removeComponent(this.upload);
	}

	@Override
	protected void buildDefaulLayout() {
		super.buildDefaulLayout();
		this.upload.addStyleName(Bootstrap.Buttons.INFO.styleName() + " restoreUploadButton");
		this.noFileLabel = new Label(this.noFileSelectedText);
		this.noFileLabel.setDebugId("noFileLabel");
		this.getRootLayout().addComponent(this.noFileLabel);
	}

	@Override
	protected String getDisplayDetails() {
		if (this.noFileLabel != null) {
			this.getRootLayout().removeComponent(this.noFileLabel);
		}

		StringBuilder sb = new StringBuilder();
		// Bug #3 will display FileName when no present
		if (this.lastFileName != null) {
			sb.append("<b>" + this.selectedFileText + "</b>" + "\u00a0");
			sb.append("<i>" + this.lastFileName + "</i>");
			sb.append("</br> ");
		}

		Object value = this.getValue();
		if (this.showFileStart == false) {
			boolean isByte = this.getFieldType() == FieldType.BYTE_ARRAY;
			sb.append("Length:\u00a0").append(isByte ? ((byte[]) value).length : value.toString().length());
		} else {
			sb.append("<em>");
			if (this.getFieldType() == FieldType.BYTE_ARRAY) {
				byte[] ba = (byte[]) value;
				int shownBytes = UploadField.MAX_SHOWN_BYTES;
				if (ba.length < UploadField.MAX_SHOWN_BYTES) {
					shownBytes = ba.length;
				}
				for (int i = 0; i < shownBytes; i++) {
					byte b = ba[i];
					sb.append(Integer.toHexString(b));
				}
				if (ba.length > UploadField.MAX_SHOWN_BYTES) {
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
