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

import org.apache.log4j.Logger;

/**
 * 
 * @author <a href="sascha.kulawik@juwimm.com">Sascha-Matthias Kulawik</a>
 * @version $Id$
 */
public class TxtSubline extends SimpleTextfield implements Module {
	private static Logger log = Logger.getLogger(TxtSubline.class);

	public String getPaneImage() {
		try {
			return getURLEncodedISO("svgsubline." + getDescription());
		} catch (Exception exe) {
			log.error("Error returning pane image", exe);
			return "16_komp_zwischenueberschr.png";
		}
	}

	public String getIconImage() {
		return "16_komp_zwischenueberschr.png";
	}
}