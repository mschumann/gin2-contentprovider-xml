package net.sf.iqser.plugin.xml;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;
import com.iqser.gin.developer.test.plugin.provider.ContentProviderTestCase;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Recommended test-workflow:
 * <ul>
 *   <li>Prepare your test data</li>
 *   <li>Add your expectations</li>
 *   <li>Initialize the test</li>
 *   <li>Execute the method(s) under test</li>
 *   <li>Destroy the plugin</li>
 *   <li>Verify if your expectations were met</li>
 *  </ul>
 */
public class XMLContentProviderTest extends ContentProviderTestCase {

	/**
	 * Test method for {@link XMLContentProvider#doSynchronization()}.
	 * @throws Exception 
	 */
	@Test
	public void testDoSynchronization() throws Exception {

		// the ContentProvider to test
		File file = new File(System.getProperty("user.dir") + "/src/test/data.xml");

		Properties initParams = new Properties();
		initParams.setProperty("xml-file-path", file.toString());
		initParams.setProperty("url-tag-name", "id");
		initParams.setProperty("date-tag-name", "date");
		initParams.setProperty("key-attribute-names", "name");
		initParams.setProperty("number-attribute-names", "age");
		initParams.setProperty("date-format", "dd.MM.yyyy");
		initParams.setProperty("content-type", "Contact");

		XMLContentProvider providerUnderTest = new XMLContentProvider();
		providerUnderTest.setInitParams(initParams);
		providerUnderTest.setName("provider");

		// prepare your test data
		Content content = new Content();
		content.setContentUrl("1");
		content.setProvider("provider");
		content.setType("Contact");
		DateFormat format = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.GERMAN);
		Date date = format.parse("23.03.2005");
		content.setModificationDate(date.getTime());
		content.addAttribute(new Attribute("NAME", "Max Miller", Attribute.ATTRIBUTE_TYPE_TEXT, true));
		content.addAttribute(new Attribute("CITY", "Berlin", Attribute.ATTRIBUTE_TYPE_TEXT));
		content.addAttribute(new Attribute("AGE", "23", Attribute.ATTRIBUTE_TYPE_NUMBER));

		Content anotherContent = new Content();
		anotherContent.setContentUrl("2");
		anotherContent.setProvider("provider");
		anotherContent.setType("Contact");
		Date anotherDate = format.parse("12.12.1997");
		anotherContent.setModificationDate(anotherDate.getTime());
		anotherContent.addAttribute(new Attribute("NAME", "Peter Mason", Attribute.ATTRIBUTE_TYPE_TEXT, true));
		anotherContent.addAttribute(new Attribute("CITY", "New York", Attribute.ATTRIBUTE_TYPE_TEXT));
		anotherContent.addAttribute(new Attribute("AGE", "34", Attribute.ATTRIBUTE_TYPE_NUMBER));

		// add your expectation
		expectsAddContent(content);
		expectsAddContent(anotherContent);
		expectsIsExistingContent("provider", "1", false);
		expectsIsExistingContent("provider", "2", false);

		// initialize the test
		prepare(); 
		providerUnderTest.init();		

		// execute the method(s) under test
		providerUnderTest.doSynchronization();

		// destroy the plugin
		providerUnderTest.destroy();

		// verify if your expectations were met
		verify(); 
	}	

	/**
	 * Test method for {@link XMLContentProvider#doHousekeeping()}.
	 * @throws Exception 
	 */
	@Test
	public void testDoHousekeeping() throws Exception {

		// the ContentProvider to test
		File file = new File(System.getProperty("user.dir") + "/src/test/data.xml");

		Properties initParams = new Properties();
		initParams.setProperty("xml-file-path", file.toString());
		initParams.setProperty("url-tag-name", "id");
		initParams.setProperty("date-tag-name", "date");
		initParams.setProperty("date-format", "dd.MM.yyyy");
		initParams.setProperty("key-attribute-names", "name");
		initParams.setProperty("number-attribute-names", "age");
		initParams.setProperty("content-type", "Contact");

		XMLContentProvider providerUnderTest = new XMLContentProvider();
		providerUnderTest.setInitParams(initParams);
		providerUnderTest.setName("provider");

		// prepare your test data
		Content content1 = new Content();
		content1.setContentUrl("1");
		content1.setProvider("provider");

		Content content2 = new Content();
		content2.setContentUrl("2");
		content2.setProvider("provider");

		Content content3 = new Content();
		content3.setContentUrl("3");
		content3.setProvider("provider");

		Collection<Content> existingContent = new ArrayList<Content>();
		existingContent.add(content1);
		existingContent.add(content2);
		existingContent.add(content3);

		// add your expectations
		expectsGetExistingContents("provider", existingContent);
		expectsRemoveContent(content3.getProvider(), content3.getContentUrl());

		// initialize the test
		prepare(); 
		providerUnderTest.init();		

		// execute the method(s) under test
		providerUnderTest.doHousekeeping();

		// destroy the plugin
		providerUnderTest.destroy();

		// verify if your expectations were met
		verify(); 
	}

	/**
	 * Test method for {@link XMLContentProvider#createContent(java.lang.String)}.
	 * @throws Exception 
	 */
	@Test
	public void testCreateContentString() throws Exception {

		// the ContentProvider to test
		File file = new File(System.getProperty("user.dir") + "/src/test/data.xml");

		assertTrue(file.exists());
		
		Properties initParams = new Properties();
		initParams.setProperty("xml-file-path", file.toString());
		initParams.setProperty("url-tag-name", "id");
		initParams.setProperty("date-tag-name", "date");
		initParams.setProperty("date-format", "dd.MM.yyyy");
		initParams.setProperty("key-attribute-names", "name,city");
		initParams.setProperty("number-attribute-names", "age");
		initParams.setProperty("content-type", "Contact");


		XMLContentProvider providerUnderTest = new XMLContentProvider();
		providerUnderTest.setInitParams(initParams);
		providerUnderTest.setName("provider");	

		// prepare your test data
		Content content = new Content();
		content.setContentUrl("2");
		content.setProvider("provider");
		content.setType("Contact");
		DateFormat format = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.GERMAN);
		Date date = format.parse("12.12.1997");
		content.setModificationDate(date.getTime());
		content.addAttribute(new Attribute("NAME", "Peter Mason", Attribute.ATTRIBUTE_TYPE_TEXT, true));
		content.addAttribute(new Attribute("CITY", "New York", Attribute.ATTRIBUTE_TYPE_TEXT,true));
		content.addAttribute(new Attribute("AGE", "34", Attribute.ATTRIBUTE_TYPE_NUMBER));		

		// initialize the test
		prepare(); 
		providerUnderTest.init();		

		// execute the method(s) under test
		Content contentFromProvider = providerUnderTest.createContent(content.getContentUrl());
		assertNotNull(contentFromProvider);
		assertEquals(content, contentFromProvider);
		//assertTrue(content.equals(contentFromProvider));
	
		// destroy the plugin
		providerUnderTest.destroy();

		// verify if your expectations were met
		verify(); 
	}
}