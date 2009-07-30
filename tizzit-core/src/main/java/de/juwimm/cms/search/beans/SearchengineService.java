package de.juwimm.cms.search.beans;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.compass.core.*;
import org.compass.core.CompassQueryBuilder.CompassBooleanQueryBuilder;
import org.compass.core.lucene.util.LuceneHelper;
import org.compass.core.support.search.CompassSearchCommand;
import org.compass.core.support.search.CompassSearchHelper;
import org.compass.core.support.search.CompassSearchResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Node;

import de.juwimm.cms.beans.foreign.CqPropertiesBeanSpring;
import de.juwimm.cms.common.Constants;
import de.juwimm.cms.model.*;
import de.juwimm.cms.search.res.DocumentResourceLocatorFactory;
import de.juwimm.cms.search.res.HtmlResourceLocator;
import de.juwimm.cms.search.vo.LinkDataValue;
import de.juwimm.cms.search.vo.SearchResultValue;
import de.juwimm.cms.search.vo.XmlSearchValue;
import de.juwimm.cms.search.xmldb.XmlDb;
import de.juwimm.util.XercesHelper;
import de.juwimm.util.tidy.Configuration;
import de.juwimm.util.tidy.Tidy;

/**
 * 
 * @author <a href="mailto:j2ee@juwimm.com">Sascha-Matthias Kulawik</a>
 * company Juwi|MacMillan Group GmbH, Walsrode, Germany
 * @version $Id$
 * @since cqcms-core 03.07.2009
 */
@Transactional(isolation=Isolation.READ_COMMITTED, propagation=Propagation.REQUIRED)
public class SearchengineService {
	private static Logger log = Logger.getLogger(SearchengineService.class); 
	private static final String LUCENE_ESCAPE_CHARS = "[\\\\!\\(\\)\\:\\^\\]\\{\\}\\~]";
	private static final Pattern LUCENE_PATTERN = Pattern.compile(LUCENE_ESCAPE_CHARS);
	private static final String REPLACEMENT_STRING = "\\\\$0";
	@Autowired
	private Compass compass;
	@Autowired
	private XmlDb xmlDb;
	@Autowired
	private HtmlResourceLocator htmlResourceLocator;
	@Autowired
	private DocumentResourceLocatorFactory documentResourceLocatorFactory;
	@Autowired
	private SearchengineDeleteService searchengineDeleteService;
	private ViewComponentHbmDao viewComponentHbmDao;
	private SiteHbmDao siteHbmDao;
	private DocumentHbmDao documentHbmDao;
	private ContentHbmDao contentHbmDao;
	private ViewDocumentHbmDao viewDocumentHbmDao;
	private UnitHbmDao unitHbmDao;
	
	private HttpClient client = new HttpClient();
	
	public void setViewComponentHbmDao(ViewComponentHbmDao viewComponentHbmDao) {
		this.viewComponentHbmDao = viewComponentHbmDao;
	}

	public void setSiteHbmDao(SiteHbmDao siteHbmDao) {
		this.siteHbmDao = siteHbmDao;
	}

	public void setDocumentHbmDao(DocumentHbmDao documentHbmDao) {
		this.documentHbmDao = documentHbmDao;
	}

	public void setContentHbmDao(ContentHbmDao contentHbmDao) {
		this.contentHbmDao = contentHbmDao;
	}

	public void setViewDocumentHbmDao(ViewDocumentHbmDao viewDocumentHbmDao) {
		this.viewDocumentHbmDao = viewDocumentHbmDao;
	}

	public void setUnitHbmDao(UnitHbmDao unitHbmDao) {
		this.unitHbmDao = unitHbmDao;
	} 

	public ViewComponentHbmDao getViewComponentHbmDao() {
		return viewComponentHbmDao;
	}

	public SiteHbmDao getSiteHbmDao() {
		return siteHbmDao;
	}

	public DocumentHbmDao getDocumentHbmDao() {
		return documentHbmDao;
	}

	public ContentHbmDao getContentHbmDao() {
		return contentHbmDao;
	}

	public ViewDocumentHbmDao getViewDocumentHbmDao() {
		return viewDocumentHbmDao;
	}

	public UnitHbmDao getUnitHbmDao() {
		return unitHbmDao;
	}

	/**
	 * @see de.juwimm.cms.search.remote.SearchengineServiceSpring#searchXML(java.lang.Integer, java.lang.String)
	 */
	@Transactional(readOnly=true)
	public XmlSearchValue[] searchXML(Integer siteId, String xpathQuery) throws Exception {
		XmlSearchValue[] retString = null;
		retString = xmlDb.searchXml(siteId, xpathQuery);
		return retString;
	}

	/**
	 * @see de.juwimm.cms.search.remote.SearchengineServiceSpring#startIndexer()
	 */
	public void startIndexer() throws Exception {
		try {
			Date start = new Date();
			Collection sites = getSiteHbmDao().findAll();
			Iterator itSites = sites.iterator();
			while (itSites.hasNext()) {
				SiteHbm site = (SiteHbm) itSites.next();
				if (log.isDebugEnabled()) log.debug("Starting with Site " + site.getName() + " (" + site.getSiteId() + ")");
				reindexSite(site.getSiteId());
			}
			Date end = new Date();
			log.info(end.getTime() - start.getTime() + " total milliseconds");
		} catch (Exception e) {
			log.error("Caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}

	/**
	 * @see de.juwimm.cms.search.remote.SearchengineServiceSpring#reindexSite(java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public void reindexSite(Integer siteId) throws Exception {
		if (log.isDebugEnabled()) log.debug("Starting reindexing site " + siteId);
		CompassSession session = compass.openSession();
		CompassQuery query = session.queryBuilder().term("siteId", siteId);
		if (log.isDebugEnabled()) log.debug("delete sites with siteId: "+siteId+" with query: " + query.toString());
		session.delete(query);
		session.close();
		Date start = new Date();
		try {
			Collection vdocs = getViewDocumentHbmDao().findAll(siteId);
			Iterator itVdocs = vdocs.iterator();
			while (itVdocs.hasNext()) {
				ViewDocumentHbm vdl = (ViewDocumentHbm) itVdocs.next();
				if (log.isDebugEnabled()) log.debug("- Starting ViewDocument: " + vdl.getLanguage() + " " + vdl.getViewType());
				ViewComponentHbm rootvc = vdl.getViewComponent();
				setUpdateSearchIndex4AllVCs(rootvc);
			}
		} catch (Exception e) {
			log.error("Caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
		try {
			Collection<UnitHbm> units = getUnitHbmDao().findBySite(siteId);
			for (UnitHbm unit : units) {
				Collection<DocumentHbm> docs = getDocumentHbmDao().findAllPerUnit(unit.getUnitId());
				for (DocumentHbm doc : docs) {
					// -- Indexing through No-Messaging
					doc.setUpdateSearchIndex(true);
				}
			}
		} catch (Exception e) {
			log.error("Caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
		Date end = new Date();
		log.info(end.getTime() - start.getTime() + " total milliseconds for site " + siteId);
		if (log.isDebugEnabled()) log.debug("finished index for site " + siteId);
	}

	private void setUpdateSearchIndex4AllVCs(ViewComponentHbm viewComponent) {
		if (viewComponent.getViewType() == Constants.VIEW_TYPE_CONTENT || viewComponent.getViewType() == Constants.VIEW_TYPE_UNIT) {
			ContentHbm content = null;
			try {
				content = getContentHbmDao().load(new Integer(viewComponent.getReference()));
				// -- Indexing through No-Messaging
				content.setUpdateSearchIndex(true);
			} catch (Exception exe) {
				log.warn("Could not resolve Content with Id: " + viewComponent.getReference(), exe);
			}
		}
		if (!viewComponent.isLeaf()) {
			Collection children = viewComponent.getChildren();
			Iterator itChildren = children.iterator();
			while (itChildren.hasNext()) {
				ViewComponentHbm child = (ViewComponentHbm) itChildren.next();
				setUpdateSearchIndex4AllVCs(child);
			}
		}
	}

	private LinkDataValue[] getLinkData(Integer siteId, DocumentHbm doc) {
		Collection<LinkDataValue> linkDataList = new ArrayList<LinkDataValue>();
		try {
			XmlSearchValue[] xmlData = this.searchXML(siteId, "//document[@src=\"" + doc.getDocumentId().toString() + "\"]");
			if (xmlData != null && xmlData.length > 0) {
				for (int i = (xmlData.length - 1); i >= 0; i--) {
					LinkDataValue ldv = new LinkDataValue();
					ldv.setUnitId(xmlData[i].getUnitId());
					ldv.setViewComponentId(xmlData[i].getViewComponentId());
					try {
						UnitHbm unit = getUnitHbmDao().load(ldv.getUnitId());
						ldv.setUnitName(unit.getName());
					} catch (Exception e) {
						log.error("Error loading Unit " + ldv.getUnitId() + ": Perhaps SearchIndex is corrupt? " + e.getMessage(), e);
					}
					try {
						ViewComponentHbm viewComponent = getViewComponentHbmDao().load(ldv.getViewComponentId());
						ldv.setViewComponentPath(viewComponent.getPath());
						ViewDocumentHbm viewDocument = viewComponent.getViewDocument();
						ldv.setLanguage(viewDocument.getLanguage());
						ldv.setViewType(viewDocument.getViewType());
					} catch (Exception e) {
						log.error("Error loading ViewComponent " + ldv.getViewComponentId() + ": Perhaps SearchIndex is corrupt? " + e.getMessage(), e);
					}
					linkDataList.add(ldv);
				}
			}
		} catch (Exception e) {
			log.error("Error loading link-data for document " + doc.getDocumentId() + ": " + e.getMessage(), e);
		}
		return linkDataList.toArray(new LinkDataValue[0]);
	}

	@Transactional(readOnly=true)
	public XmlSearchValue[] searchXmlByUnit(Integer unitId, Integer viewDocumentId, String xpathQuery, boolean parentSearch) throws Exception {
		XmlSearchValue[] retString = null;

		if (parentSearch) {
			try {
				ViewComponentHbm vc = getViewComponentHbmDao().find4Unit(unitId, viewDocumentId);
				if (vc != null && !vc.isRoot()) {
					unitId = vc.getParent().getUnit4ViewComponent();
				}
			} catch (Exception e) {
				log.error("Error finding VC by unit " + unitId + " and viewDocument " + viewDocumentId + " in searchXmlByUnit!", e);
			}
		}

		retString = xmlDb.searchXmlByUnit(unitId, viewDocumentId, xpathQuery);
		return retString;
	}
	
	@Transactional(readOnly=true)
	
	public SearchResultValue[] searchWeb(Integer siteId, final String searchItem, Integer pageSize, Integer pageNumber) throws Exception {
		return searchWeb(siteId, searchItem, pageSize, pageNumber, null);
	}

	public SearchResultValue[] searchWeb(Integer siteId, final String searchItem, Integer pageSize, Integer pageNumber, String searchUrl) throws Exception {
		if (pageSize != null && pageSize.intValue() <= 1) pageSize = new Integer(20);
		if (pageNumber != null && pageNumber.intValue() < 0) pageNumber = new Integer(0); // first page
		SearchResultValue[] retArr = null;

		if (log.isDebugEnabled()) log.debug("starting compass-search");
		try {
			
			if (log.isDebugEnabled()){ log.debug("search for: \"" + searchItem + "\"");}
			
			//TODO: find calls of searchWeb and ADD exception handling 
			//special chars with a meaning in Lucene have to be escaped - there is a mechanism for
			//that in Lucene - QueryParser.escape but I don't want to replace '*','?','+','-' in searchItem 
			String searchItemEsc = LUCENE_PATTERN.matcher(searchItem).replaceAll(REPLACEMENT_STRING);
			String searchUrlEsc = null;
			if(searchUrl != null){
				searchUrlEsc = LUCENE_PATTERN.matcher(searchUrl).replaceAll(REPLACEMENT_STRING);
			}
			
			if (log.isDebugEnabled() && !searchItem.equalsIgnoreCase(searchItemEsc)){ 
				log.debug("search for(escaped form): \"" + searchItemEsc + "\"");
			}
			
			CompassSession session = compass.openSession();

			//per default searchItems get connected by AND (compare CompassSettings.java)
			CompassQuery query = buildRatedWildcardQuery(session, siteId, searchItemEsc, searchUrlEsc);
			if(log.isDebugEnabled()){
				log.debug("search for query: "+query.toString());
			}
			CompassSearchHelper searchHelper = new CompassSearchHelper(compass, pageSize) {
				@Override
				protected void doProcessBeforeDetach(CompassSearchCommand searchCommand, CompassSession session, CompassHits hits, int from, int size) {
					for (int i = 0; i < hits.length(); i++) {
						hits.highlighter(i).fragment("contents");
					}
				}
			};

			CompassSearchCommand command = null;
			if (pageSize == null) {
				command = new CompassSearchCommand(query);
			} else {
				command = new CompassSearchCommand(query, pageNumber);
			}

			CompassSearchResults results = searchHelper.search(command);
			if (log.isDebugEnabled()) log.debug("search lasted " + results.getSearchTime() + " milliseconds");
			CompassHit[] hits = results.getHits();
			retArr = new SearchResultValue[hits.length];
			for (int i = 0; i < hits.length; i++) {
				retArr[i] = new SearchResultValue();
				Resource resource = hits[i].getResource();
				retArr[i].setScore(Integer.valueOf(new Float(hits[i].getScore() * 100.0f).intValue()));
				//CompassHighlightedText text = hits[i].getHighlightedText();
				retArr[i].setSummary(stripNonValidXMLCharacters(hits[i].getHighlightedText().getHighlightedText("contents")));
				retArr[i].setUnitId(new Integer(resource.getProperty("unitId").getStringValue()));
				retArr[i].setUnitName(resource.getProperty("unitName").getStringValue());
				retArr[i].setPageSize(pageSize);
				retArr[i].setPageNumber(pageNumber);
				retArr[i].setDuration(Long.valueOf(results.getSearchTime()));
				if (results.getPages() != null) {
					retArr[i].setPageAmount(Integer.valueOf(results.getPages().length));
				} else {
					retArr[i].setPageAmount(new Integer(1));
				}
				retArr[i].setTotalHits(Integer.valueOf(results.getTotalHits()));
				String alias = hits[i].getAlias();
				if ("HtmlSearchValue".equalsIgnoreCase(alias)) {
					String url = resource.getProperty("url").getStringValue();
					if (url != null) {
						retArr[i].setUrl(url);
						retArr[i].setTitle(resource.getProperty("title").getStringValue());
						retArr[i].setLanguage(resource.getProperty("language").getStringValue());
						retArr[i].setViewType(resource.getProperty("viewtype").getStringValue());
						Property template = resource.getProperty("template");
						retArr[i].setTemplate(template != null ? template.getStringValue() : "standard");
					}
				} else {
					String documentId = resource.getProperty("documentId").getStringValue();
					Integer docId = null;
					try {
						docId = Integer.valueOf(documentId);
					} catch (Exception e) {
						log.warn("Error converting documentId: " + e.getMessage());
						if (log.isDebugEnabled()) log.debug(e);
					}
					if (docId != null) {
						retArr[i].setDocumentId(docId);
						retArr[i].setDocumentName(resource.getProperty("documentName").getStringValue());
						retArr[i].setMimeType(resource.getProperty("mimeType").getStringValue());
						retArr[i].setTimeStamp(resource.getProperty("timeStamp").getStringValue());
					}
				}
			}
		} catch (BooleanQuery.TooManyClauses tmc) {
			StringBuffer sb = new StringBuffer(256);
			sb.append("BooleanQuery.TooManyClauses Exception. ");
			sb.append("This typically happens if a PrefixQuery, FuzzyQuery, WildcardQuery, or RangeQuery is expanded to many terms during search. ");
			sb.append("Possible solution: BooleanQuery.setMaxClauseCount() > 1024(default)");
			sb.append(" or reform the search querry with more letters and less wildcards (*).");
			log.error(sb.toString(), tmc);
			//Exception gets thrown again to allow the caller to react
			throw tmc;
		} catch (Exception e) {
			log.error("Error performing search with compass: " + e.getMessage(), e);
		}
		if (log.isDebugEnabled()) log.debug("finished compass-search");
		return retArr;
	}

	public String stripNonValidXMLCharacters(String in) {
		if (in == null) return "";
		String stripped = in.replaceAll("[^\\u0009\\u000a\\u000d\\u0020-\\ud7ff\\e0000-\\ufffd]", "");
		return stripped;
	}

	@SuppressWarnings("unchecked")
	private CompassQuery buildRatedWildcardQuery(CompassSession session, Integer siteId, String searchItem, String searchUrl) 
						throws 	IOException, ParseException, 
								InstantiationException, IllegalAccessException, ClassNotFoundException {
		Analyzer analyzer = null;
		CompassBooleanQueryBuilder queryBuilder = session.queryBuilder().bool();
		IndexReader ir = LuceneHelper.getLuceneInternalSearch(session).getReader();
		Query query;
		QueryParser parser;
		try {
			String analyzerClass = session.getSettings().getSetting("compass.engine.analyzer.search.type");
			Constructor<Analyzer> analyzerConstructor = (Constructor<Analyzer>) (Class.forName(analyzerClass)).getConstructor();
			analyzer = analyzerConstructor.newInstance();
			log.info("Created search analyzer from compass settings - class is: " + analyzer.getClass().getName());
		} catch (Exception e) {
			log.error("Error while instantiating search analyzer from compass settings - going on with StandardAnalyzer");
			analyzer = new StandardAnalyzer();
		}
		if(searchUrl != null){
			if (log.isDebugEnabled()) log.debug("to search all hosts and subpages attaching * at start and end of urlString...");
			// search on this side and all sub sites on all hosts
			searchUrl = "*"+searchUrl+"*";
			if(log.isDebugEnabled()) log.debug("urlString before parsing: "+searchUrl);
			parser = new QueryParser("url", analyzer);
			parser.setAllowLeadingWildcard(true);
			query = parser.parse(searchUrl).rewrite(ir);
			queryBuilder.addMust(LuceneHelper.createCompassQuery(session, query));		
		}
		query = new QueryParser("siteId", analyzer).parse(siteId.toString());
		queryBuilder.addMust(LuceneHelper.createCompassQuery(session, query));
		
		CompassBooleanQueryBuilder subQueryBuilder = session.queryBuilder().bool();
		String searchFields[] = {"metadata", "url", "title", "contents"};		
		for (int i = 0; i < searchFields.length; i++) {
			if(i == (searchFields.length-1) && searchUrl != null){
				searchItem = searchItem + " " + searchUrl;
			}
			parser = new QueryParser(searchFields[i], analyzer);
			parser.setAllowLeadingWildcard(true);
			query = parser.parse(searchItem);
			query = query.rewrite(ir);
			query.setBoost(searchFields.length - i);
			subQueryBuilder.addShould(LuceneHelper.createCompassQuery(session, query));
		}
		queryBuilder.addMust(subQueryBuilder.toQuery());
		return queryBuilder.toQuery();
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void indexPage(Integer contentId) {
		ContentHbm content = getContentHbmDao().load(contentId);
		ContentVersionHbm contentVersion = content.getLastContentVersion();
		if (contentVersion == null) {
			// liveserver?!
			contentVersion = content.getContentVersionForPublish();
		}
		if (contentVersion == null) {
			log.error("ContentVersion not existing for content: " + content.getContentId());
			return;
		}
		String contentText = contentVersion.getText();

		Collection vclColl = getViewComponentHbmDao().findByReferencedContent(content.getContentId().toString());
		Iterator it = vclColl.iterator();
		while (it.hasNext()) {
			ViewComponentHbm viewComponent = (ViewComponentHbm) it.next();
			if (log.isDebugEnabled()) log.debug("Updating Indexes for VCID " + viewComponent.getDisplayLinkName());

			if (viewComponent.isSearchIndexed()) {
				this.indexPage4Lucene(viewComponent, contentText);
			} else {
				searchengineDeleteService.deletePage4Lucene(viewComponent);
			}
			if (viewComponent.isXmlSearchIndexed()) {
				this.indexPage4Xml(viewComponent, contentText);
			} else {
				searchengineDeleteService.deletePage4Xml(viewComponent);
			}

			Collection referencingViewComponents = getViewComponentHbmDao().findByReferencedViewComponent(viewComponent.getViewComponentId().toString());
			Iterator itRef = referencingViewComponents.iterator();
			while (itRef.hasNext()) {
				ViewComponentHbm refViewComponent = (ViewComponentHbm) itRef.next();
				if (refViewComponent.getViewType() == Constants.VIEW_TYPE_SYMLINK) {
					// acts as normal content
					if (log.isDebugEnabled()) log.debug("trying to index symLink " + refViewComponent.getDisplayLinkName());
					if (refViewComponent.isSearchIndexed()) {
						this.indexPage4Lucene(refViewComponent, contentText);
					} else {
						searchengineDeleteService.deletePage4Lucene(refViewComponent);
					}
					if (refViewComponent.isXmlSearchIndexed()) {
						this.indexPage4Xml(refViewComponent, contentText);
					} else {
						searchengineDeleteService.deletePage4Xml(refViewComponent);
					}
				}

				/* Die Referenzen im Tree KÖNNEN nur über "findByReferencedViewComponent"
				   gefunden werden, und nicht über die XML Suchmaschine.
				*/
			}
		}
		content.setUpdateSearchIndex(false);
	}
	
	private void indexPage4Lucene(ViewComponentHbm viewComponent, String contentText) {
		if (log.isDebugEnabled()) log.debug("Lucene-Index create / update for VC " + viewComponent.getViewComponentId());
		ViewDocumentHbm vdl = viewComponent.getViewDocument();
		SiteHbm site = vdl.getSite();
		CompassSession session = null;
		CompassTransaction tx = null;
		try {
			String currentUrl = searchengineDeleteService.getUrl(viewComponent);
			File file = this.downloadFile(currentUrl);
			if (file != null) {
				String cleanUrl = viewComponent.getViewDocument().getSite().getPageNameSearch();
				cleanUrl = currentUrl.substring(0, currentUrl.length() - cleanUrl.length());
				session = compass.openSession();
				Resource resource = htmlResourceLocator.getResource(session, file, cleanUrl, new Date(System.currentTimeMillis()), viewComponent, vdl);
				tx = session.beginTransaction();
				session.save(resource);
				tx.commit();
				session.close();
				session = null;
			} else {
				log.warn("Critical Error during indexPage4Lucene - cound not find Ressource: " + currentUrl);
			}
		} catch (Exception e) {
			log.warn("Error indexPage4Lucene, VCID " + viewComponent.getViewComponentId().toString() + ": " + e.getMessage(), e);
			if (tx != null) tx.rollback();
		} finally {
			if (session != null) session.close();
		}
		if (log.isDebugEnabled()) log.debug("finished indexPage4Lucene");
	}

	private void indexPage4Xml(ViewComponentHbm viewComponent, String contentText) {
		if (log.isDebugEnabled()) log.debug("XML-Index create / update for VC " + viewComponent.getViewComponentId());
		ViewDocumentHbm vdl = viewComponent.getViewDocument();
		SiteHbm site = vdl.getSite();
		org.w3c.dom.Document wdoc = null;
		try {
			wdoc = XercesHelper.string2Dom(contentText);
		} catch (Throwable t) {
		}
		if (wdoc != null) {

			HashMap<String, String> metaAttributes = new HashMap<String, String>();

			if (log.isDebugEnabled()) {
				log.debug("SearchUpdateMessageBeanImpl.indexVC(...) -> infoText = " + viewComponent.getLinkDescription());
				log.debug("SearchUpdateMessageBeanImpl.indexVC(...) -> text = " + viewComponent.getDisplayLinkName());
				log.debug("SearchUpdateMessageBeanImpl.indexVC(...) -> unitId = " + viewComponent.getUnit4ViewComponent());
			}

			metaAttributes.put("infoText", viewComponent.getLinkDescription());
			metaAttributes.put("text", viewComponent.getDisplayLinkName());
			metaAttributes.put("unitId", (viewComponent.getUnit4ViewComponent() == null ? "" : viewComponent.getUnit4ViewComponent().toString()));

			xmlDb.saveXml(site.getSiteId(), viewComponent.getViewComponentId(), contentText, metaAttributes);
		}
		if (log.isDebugEnabled()) log.debug("finished indexPage4Xml");
	}

	private File downloadFile(String strUrl) {
		try {
			File file = File.createTempFile("indexingTempFile", "html");
			file.deleteOnExit();

			client.getParams().setConnectionManagerTimeout(20000);

			HttpMethod method = new GetMethod(strUrl);
			method.setFollowRedirects(true);
			method.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
			for (int i = 0; i <= 3; i++) {
				if (i == 3) {
					log.error("Trying to fetch the URL " + strUrl + " three times with no success - page could not be loaded in searchengine!");
					break;
				}
				if (log.isDebugEnabled()) log.debug("Trying " + i + " to catch the URL");
				if (this.downloadToFile(method, file, strUrl)) {
					if (log.isDebugEnabled()) log.debug("got it!");
					break;
				}
			}
			if (log.isDebugEnabled()) log.debug("downloadFile: FINISHED");
			return file;
		} catch (Exception exe) {
			return null;
		}
	}

	private boolean downloadToFile(HttpMethod method, File file, String strUrl) {
		boolean retVal = false;
		try {
			client.executeMethod(method);
			InputStream in = method.getResponseBodyAsStream();
			OutputStream out = new FileOutputStream(file);

			try {
				byte[] buf = new byte[2048];
				for (;;) {
					int cb = in.read(buf);
					if (cb < 0) break;
					out.write(buf, 0, cb);
				}
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
					}
				}
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
			retVal = true;
		} catch (HttpException he) {
			log.error("Http error connecting to '" + strUrl + "'");
			log.error(he.getMessage());
		} catch (IOException ioe) {
			log.error("Unable to connect to '" + strUrl + "'");
		}
		//clean up the connection resources
		method.releaseConnection();
		return retVal;
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void indexDocument(Integer documentId) {
		if (log.isDebugEnabled()) log.debug("Index create / update for Document: " + documentId);
		DocumentHbm document = getDocumentHbmDao().load(documentId);
		if (!documentResourceLocatorFactory.isSupportedFileFormat(document.getMimeType())) {
			log.info("Document " + document.getDocumentId() + " \"" + document.getDocumentName() + "\" \"" + document.getMimeType() + "\" is not supported, skipping...");
			document.setUpdateSearchIndex(false);
			return;
		}
		CompassSession session = null;
		CompassTransaction tx = null;
		try {
			session = compass.openSession();
			Resource resource = documentResourceLocatorFactory.getResource(session, document);
			tx = session.beginTransaction();
			session.save(resource);
			tx.commit();
			session.close();
			session = null;
			document.setUpdateSearchIndex(false);
		} catch (Exception e) {
			log.warn("Error indexDocument " + document.getDocumentId().toString() + ": " + e.getMessage());
			if (tx != null) tx.rollback();
		} finally {
			if (session != null) session.close();
		}
		if (log.isDebugEnabled()) log.debug("finished indexDocument");
	}
}