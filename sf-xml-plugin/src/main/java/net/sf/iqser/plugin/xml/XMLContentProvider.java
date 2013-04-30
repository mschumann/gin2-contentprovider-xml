package net.sf.iqser.plugin.xml;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.iqser.core.exception.IQserException;
import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;
import com.iqser.core.model.Parameter;
import com.iqser.core.plugin.provider.AbstractContentProvider;

public class XMLContentProvider extends AbstractContentProvider {

	/** The Logger */
    private static Logger logger = Logger.getLogger( XMLContentProvider.class );
    
    /** The XML document */
    private Document doc;
    
    /** Initialization parameters */
    private String URL_TAG_NAME;     // mandatory
    private String CONTENT_TYPE;     // mandatory
    private String DATE_TAG_NAME;    // optional
    private String TEXT_TAG_NAME;    // optional
    private List<String> KEY_ATTRIBUTE_NAMES;    // optional
    private List<String> DATE_ATTRIBUTE_NAMES;   // optional
    private List<String> NUMBER_ATTRIBUTE_NAMES; // optional
    private DateFormat DATE_FORMAT;  // optional
    
	@Override
	public void init() {
		logger.debug("init() called");

		URL_TAG_NAME = getInitParams().getProperty("url-tag-name");
		CONTENT_TYPE = getInitParams().getProperty("content-type");
		DATE_TAG_NAME = getInitParams().getProperty("date-tag-name");
		TEXT_TAG_NAME = getInitParams().getProperty("text-tag-name");
		DATE_FORMAT = DateFormat.getDateInstance();
		
		if (getInitParams().contains("date-format")) {
			DATE_FORMAT = new SimpleDateFormat(getInitParams().getProperty("date-format"));
		}
		
		if (getInitParams().containsKey("key-attribute-names")) {
			String[] keys = getInitParams().getProperty("key-attribute-names").split("||");
			KEY_ATTRIBUTE_NAMES = Arrays.asList(keys);
		}
		
		if (getInitParams().containsKey("date-attribute-names")) {
			String[] dan = getInitParams().getProperty("date-attribute-names").split("||");
			DATE_ATTRIBUTE_NAMES = Arrays.asList(dan);
		}
		
		if (getInitParams().containsKey("number-attribute-names")) {
			String[] nan = getInitParams().getProperty("number-attribute-names").split("||");
			NUMBER_ATTRIBUTE_NAMES = Arrays.asList(nan);
		}
		
		File file = new File(getInitParams().getProperty("xml-file-path"));
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace(true);
//		dbf.setIgnoringComments(true);
//		dbf.setValidating(true);
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(file);
		} catch (ParserConfigurationException e) {
			logger.error("Couldn't create an xml document builder", e);
		} catch (SAXException e) {
			logger.error("Couldn't parse the xml source file", e);
		} catch (IOException e) {
			logger.error("Couldn't create the xml document representation" , e);
		}
		
		logger.debug("init() finished");
	}

	@Override
	public void destroy() { }

	@Override
	public void preRemoveInstance() {
		// do not remove this line
		super.preRemoveInstance();
	}

	@Override
	public void postCreateInstance() { }

	@Override
	public void doSynchronization() {
		logger.debug("doSynchronization() called");
	
		if (doc != null) {
			NodeList nodeList = doc.getElementsByTagName(URL_TAG_NAME);
			
			for (int idx = 0; idx < nodeList.getLength(); idx++) {
		    	Node urlNode = nodeList.item(idx);
			    String contentURL = urlNode.getTextContent();
			    			    
				Content content = createContent(urlNode.getParentNode(), contentURL);

		    	try {
					if (isExistingContent(contentURL)) {
						Content oldContent = getExistingContent(contentURL);
						
						if (!content.equals(oldContent)) {
							updateContent(content);
						}
					} else {
						addContent(content);
					}
				} catch (IQserException e) {
					logger.error("Could not add or update content", e);
				}
			}
		}
		
		logger.debug("doSynchronization() finished");
	}

	@Override
	public void doHousekeeping() {
		logger.debug("doHousekeeping() called");
		
		Collection<String> xmlContentURLs = new ArrayList<String>();
		NodeList urlNodeList = doc.getElementsByTagName(URL_TAG_NAME);
		
		for (int idx = 0; idx < urlNodeList.getLength(); idx++) {
			Element urlNode = (Element)urlNodeList.item(idx);
			String contentURL = urlNode.getTextContent();
			
			if (contentURL.length() > 0) {
				xmlContentURLs.add(contentURL);
			}
		}
		
		try {
			Collection<Content> ginContents = this.getExistingContents();
			
			for (Content ginContent : ginContents) {
				if (!xmlContentURLs.contains(ginContent.getContentUrl())) {
					this.removeContent(ginContent.getContentUrl());
				}
			}
		} catch (IQserException e) {
			logger.error("Coult not retrieve or remoce extisting content", e);
		}
		
		logger.debug("doHousekeeping() finished");
	}

	@Override
	public Content createContent(String contentURL) {
				
		NodeList urlNodes = doc.getElementsByTagName(URL_TAG_NAME);
		
		for (int idx = 0; idx < urlNodes.getLength(); idx++) {
			Node urlNode = urlNodes.item(idx);
			
			if (urlNode.getTextContent().equalsIgnoreCase(contentURL)) {
				return createContent(urlNode.getParentNode(), contentURL);
			}
		}
		
		logger.error("The contentURL " + contentURL + " was not found in the xml source.");
		
		return null;
	}
	
	private Content createContent(Node contentNode, String contentUrl) {
		
		Content content = new Content();
		content.setContentUrl(contentUrl);
		content.setProvider(getName());
		content.setType(CONTENT_TYPE);
		
		NodeList attributeNodes = contentNode.getChildNodes();
				
		for (int idx = 0; idx < attributeNodes.getLength(); idx++) {
			Node attributeNode = attributeNodes.item(idx);
						
			if (!attributeNode.getNodeName().equalsIgnoreCase(URL_TAG_NAME) 
					&& !attributeNode.getNodeName().equalsIgnoreCase(DATE_TAG_NAME)
					&& !attributeNode.getNodeName().equalsIgnoreCase(TEXT_TAG_NAME)
					&& !attributeNode.getNodeName().equalsIgnoreCase("#text")
					&& attributeNode.getChildNodes().getLength() == 1) 
			{					
				Attribute a = new Attribute();
				a.setName(attributeNode.getNodeName().toUpperCase());
				a.setValue(attributeNode.getTextContent());
				a.setType(Attribute.ATTRIBUTE_TYPE_TEXT);
				
				if (KEY_ATTRIBUTE_NAMES != null 
						&& KEY_ATTRIBUTE_NAMES.contains(attributeNode.getNodeName())) {
					a.setKey(true);
				} else if (DATE_ATTRIBUTE_NAMES != null 
						&& DATE_ATTRIBUTE_NAMES.contains(attributeNode.getNodeName())) {
					a.setType(Attribute.ATTRIBUTE_TYPE_DATE);
				} else if (NUMBER_ATTRIBUTE_NAMES != null
						&& NUMBER_ATTRIBUTE_NAMES.contains(attributeNode.getNodeName())) {
					a.setType(Attribute.ATTRIBUTE_TYPE_NUMBER);
				} else {
					NumberFormat format = NumberFormat.getInstance();
					Number number = null;
					
					try {
						number = format.parse(a.getValue());
					} catch (ParseException e) { }
					
					if (number != null) {
						a.setType(Attribute.ATTRIBUTE_TYPE_NUMBER);
					}
				}
								
				content.addAttribute(a);
			} else if (attributeNode.getNodeName().equalsIgnoreCase(DATE_TAG_NAME)) {
				String text = attributeNode.getTextContent();
				
				if (text != null) {
					try {
						Date date = DATE_FORMAT.parse(text);
						content.setModificationDate(date.getTime());
					} catch (ParseException e) {
						logger.error("Could not parse modification date", e);
					}
				}
			} else if (attributeNode.getNodeName().equalsIgnoreCase(TEXT_TAG_NAME)) {
				String text = attributeNode.getTextContent();
				
				if (text != null && text.length() > 0) {
					content.setFulltext(text);
				}
			}
		}
				
		return content;
	}

	@Override
	public Content createContent(InputStream in) {
		return null;
	}

	@Override
	public byte[] getBinaryData(Content arg0) {
		return null;
	}

	@Override
	public Collection<String> getActions(Content arg0) {
		return null;
	}

	@Override
	public void performAction(String arg0, Collection<Parameter> arg1, Content arg2) { }
}
