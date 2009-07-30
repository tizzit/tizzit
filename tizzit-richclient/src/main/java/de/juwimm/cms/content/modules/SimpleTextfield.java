/**
 * Copyright (c) 2009 Juwi MacMillan Group GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.juwimm.cms.content.modules;

import static de.juwimm.cms.common.Constants.*;

import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.w3c.dom.Node;

import de.juwimm.cms.content.frame.DlgModalModule;
import de.juwimm.cms.content.panel.PanSimpleTextfield;
import de.juwimm.util.XercesHelper;

/**
 * 
 * @author <a href="sascha.kulawik@juwimm.com">Sascha-Matthias Kulawik</a>
 * @version $Id$
 */
public class SimpleTextfield extends AbstractModule {
	private PanSimpleTextfield pan = new PanSimpleTextfield(this);

	public void setCustomProperties(String methodname, Properties parameters) {
		super.setCustomProperties(methodname, parameters);
	}

	public boolean isModuleValid() {
		setValidationError("");
		if (isMandatory() && !pan.isModuleValid()) {
			setValidationError(rb.getString("exception.SimpleTextValueRequired"));
			return false;
		}
		return true;
	}

	public JPanel viewPanelUI() {
		return pan;
	}

	public JDialog viewModalUI(boolean modal) {
		DlgModalModule frm = new DlgModalModule(this, pan, 150, 450, modal);
		frm.setVisible(true);
		return frm;
	}

	/* (non-Javadoc)
	 * @see de.juwimm.cms.content.modules.Module#save(org.xml.sax.ContentHandler)
	 */
	public void load() {
	}

	public void setProperties(Node node) {
		setDescription(XercesHelper.getNodeValue(node));
		pan.setProperties(node);
	}

	public Node getProperties() {
		Node node = pan.getProperties();
		setDescription(XercesHelper.getNodeValue(node));
		return node;
	}

	public String getPaneImage() {
		return "16_fett.png";
	}

	public String getIconImage() {
		return "16_fett.png";
	}

	public void setEnabled(boolean enabling) {
		pan.setEnabled(enabling);
	}
	
	public void recycle() {
		pan.setProperties(null);
	}
}