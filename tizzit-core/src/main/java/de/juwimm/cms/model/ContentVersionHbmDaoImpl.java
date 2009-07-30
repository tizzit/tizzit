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
// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 */
package de.juwimm.cms.model;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.juwimm.cms.remote.helper.AuthenticationHelper;
import de.juwimm.cms.vo.ContentVersionValue;
import de.juwimm.util.Base64;
import de.juwimm.util.DateConverter;
import de.juwimm.util.XercesHelper;

/**
 * @see de.juwimm.cms.model.ContentVersionHbm
 * @author <a href="mailto:carsten.schalm@juwimm.com">Carsten Schalm</a> ,
 *         Juwi|MacMillan Group Gmbh, Walsrode, Germany
 * @version $Id$
 */
public class ContentVersionHbmDaoImpl extends ContentVersionHbmDaoBase {
	private static Log log = LogFactory.getLog(ContentVersionHbmDaoImpl.class);

	@Autowired
	private SequenceHbmDao sequenceHbmDao;

	@Override
	public ContentVersionHbm create(ContentVersionHbm contentVersionHbm) {
		if (contentVersionHbm.getContentVersionId() == null || contentVersionHbm.getContentVersionId().intValue() == 0) {
			try {
				Integer id = sequenceHbmDao.getNextSequenceNumber("contentversion.content_version_id");
				contentVersionHbm.setContentVersionId(id);
			} catch (Exception e) {
				log.error("Error creating primary key", e);
			}
		}
		if (contentVersionHbm.getVersion() == null) {
			contentVersionHbm.setVersion("1");
		}
		contentVersionHbm.setCreateDate(System.currentTimeMillis());
		contentVersionHbm.setCreator(AuthenticationHelper.getUserName());
		return super.create(contentVersionHbm);
	}

	/**
	 * @see de.juwimm.cms.model.ContentVersionHbm#toXml(int)
	 */
	@Override
	public String handleToXml(ContentVersionHbm contentVersion) {
		StringBuilder sb = new StringBuilder();
		sb.append("<contentVersion id=\"");
		sb.append(contentVersion.getContentVersionId());
		sb.append("\">\n");
		sb.append("\t<heading><![CDATA[").append(contentVersion.getHeading()).append("]]></heading>\n");
		sb.append("\t<creator><userName>").append(contentVersion.getCreator()).append("</userName>").append("</creator>\n");
		sb.append("\t<createDate>").append(DateConverter.getSql2String(new Date(contentVersion.getCreateDate()))).append("</createDate>\n");
		/* KICK the XML Banner out */
		String xmlbanner = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
		String txt = contentVersion.getText().trim();
		if (txt.length() >= xmlbanner.length() && txt.substring(0, xmlbanner.length()).equals(xmlbanner)) {
			txt = txt.substring(xmlbanner.length());
		}
		txt = Base64.encodeString(txt, false);
		sb.append("\t<text>").append(txt).append("</text>\n");
		sb.append("\t<version>").append(contentVersion.getVersion()).append("</version>\n");
		sb.append("</contentVersion>\n");
		return sb.toString();
	}

	/**
	 * @see de.juwimm.cms.model.ContentVersionHbm#getDao()
	 */
	@Override
	public ContentVersionValue handleGetDao(ContentVersionHbm contentVersion) {
		ContentVersionValue dao = new ContentVersionValue();
		dao.setContentVersionId(contentVersion.getContentVersionId());
		dao.setHeading(contentVersion.getHeading());
		dao.setCreateDate(contentVersion.getCreateDate());
		dao.setCreator(contentVersion.getCreator());
		dao.setText(contentVersion.getText());
		dao.setVersion(contentVersion.getVersion());
		return dao;
	}

	@Override
	protected ContentVersionHbm handleCreateFromXml(Element cvnde, boolean reusePrimaryKey, boolean liveDeploy) throws Exception {
		ContentVersionHbm contentVersion = ContentVersionHbm.Factory.newInstance();
		if (reusePrimaryKey) {
			Integer id = new Integer(cvnde.getAttribute("id"));
			if (log.isDebugEnabled()) log.debug("creating ContentVersion with existing id " + id);
			contentVersion.setContentVersionId(id);
		}
		contentVersion.setCreateDate(DateConverter.getString2Sql(XercesHelper.getNodeValue(cvnde, "./createDate")).getTime());
		contentVersion.setHeading(XercesHelper.getNodeValue(cvnde, "./heading"));
		contentVersion.setVersion(XercesHelper.getNodeValue(cvnde, "./version"));
		contentVersion.setCreator(XercesHelper.getNodeValue(cvnde, "./creator/userName"));
		String ttext = null;
		try {
			Node ttextnode = XercesHelper.findNode(cvnde, "./text");
			ttext = XercesHelper.nodeList2string(ttextnode.getChildNodes());
			if (ttext != null) ttext = Base64.decodeToString(ttext);
		} catch (Exception e) {
		}
		if (ttext == null || ttext.equals("<text/>")) {
			ttext = "";
		}
		contentVersion.setText(ttext);
		contentVersion = create(contentVersion);

		return contentVersion;
	}
}