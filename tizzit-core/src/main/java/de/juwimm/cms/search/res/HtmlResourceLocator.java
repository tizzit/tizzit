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
package de.juwimm.cms.search.res;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.lucene.document.DateTools;
import org.compass.core.CompassSession;
import org.compass.core.Resource;
import org.compass.core.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.juwimm.cms.model.*;
import de.juwimm.cms.search.res.html.HTMLParser;
import de.juwimm.util.XercesHelper;

/**
 * Helper for getting injected the Compass-Instances from Spring and offering getters for them
 * @author <a href="mailto:carsten.schalm@juwimm.com">Carsten Schalm</a>
 * company Juwi|MacMillan Group Gmbh, Walsrode, Germany
 * @version $Id$
 */
public class HtmlResourceLocator {
	private static Logger log = Logger.getLogger(HtmlResourceLocator.class);
	@Autowired
	private UnitHbmDao unitHbmDao;
	@Autowired
	private ContentHbmDao contentHbmDao;

	public Resource getResource(CompassSession session, File file, String url, Date modifiedDate, ViewComponentHbm vcl, ViewDocumentHbm vdl) throws IOException, InterruptedException {
		ResourceFactory resourceFactory = session.resourceFactory();
		Resource resource = resourceFactory.createResource("HtmlSearchValue");
		resource.addProperty("url", url);
		resource.addProperty("siteId", vdl.getSite().getSiteId().toString());
		resource.addProperty("language", vdl.getLanguage());
		resource.addProperty("viewtype", vdl.getViewType());
		resource.addProperty("modified", DateTools.timeToString(modifiedDate.getTime(), DateTools.Resolution.MINUTE));
		resource.addProperty("uid", url);
		try {
			Integer unitId = vcl.getUnit4ViewComponent();
			if (unitId != null) {
				UnitHbm unit = unitHbmDao.load(unitId);
				resource.addProperty("unitId", unit.getUnitId().toString());
				resource.addProperty("unitName", unit.getName());
			}
		} catch (Exception exe) {
		}
		try {
			ContentHbm content = contentHbmDao.load(new Integer(vcl.getReference()));
			resource.addProperty("template", content.getTemplate());
		} catch (Exception exe) {
		}

		HTMLParser parser = new HTMLParser(new FileInputStream(file));
		Reader reader = parser.getReader();
		StringWriter sw = new StringWriter();
		org.apache.commons.io.IOUtils.copy(reader, sw);
		String sresult = sw.toString();

		log.debug("Saving tokenized HTML value into searchengine: " + sresult);
		resource.addProperty("contents", stripNonValidXMLCharacters(sresult));

		Properties prop = parser.getMetaTags();
		Collection metafields = prop.values();
		String metadata = "";
		Iterator it = metafields.iterator();
		while (it.hasNext()) {
			StringTokenizer st = new StringTokenizer((String) it.next(), ",");
			while (st.hasMoreElements()) {
				String token = st.nextToken().trim();
				metadata += token + " ";
			}
		}
		// tidy the metadata
		metadata = XercesHelper.html2utf8string(metadata);
		resource.addProperty("metadata", metadata);

		// Add the summary as a field that is stored and returned with
		// hit documents for display.
		resource.addProperty("summary", parser.getSummary());

		// Add the title as a field that it can be searched and that is stored.
		resource.addProperty("title", parser.getTitle());

		return resource;
	}

	public String stripNonValidXMLCharacters(String in) {
		if(in == null) return "";
		String stripped = in.replaceAll("[^\\u0009\\u000a\\u000d\\u0020-\\ud7ff\\e0000-\\ufffd]", "").replaceAll("[&<>]", "");
		return stripped;
	}
}