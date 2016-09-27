package com.jobaline.uiautomation.framework.util;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by damian on 4/20/15.
 */
public class XMLDocument
{
	private Document xmlDocument = null;
	private XPathFactory xPathFactory;


	public XMLDocument(String content) throws XMLDocumentBuildException
	{
		try
		{
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			xmlDocument = documentBuilder.parse(new InputSource(new StringReader(content)));
		}
		catch(ParserConfigurationException | SAXException | IOException e)
		{
			e.printStackTrace();
			throw new XMLDocumentBuildException(e);
		}
	}


	private Document getXmlDocument()
	{
		return xmlDocument;
	}


	private XPathFactory getXPathFactory()
	{
		if(xPathFactory == null)
		{
			xPathFactory = XPathFactory.newInstance();
		}

		return xPathFactory;
	}


	public List<String> getElementsTexts(String elementName)
	{
		List<String> texts = new ArrayList<>();

		XPath xPath = getXPathFactory().newXPath();
		NodeList nodes;
		try
		{
			nodes = (NodeList)xPath.evaluate("//" + elementName + "/text()", getXmlDocument(), XPathConstants.NODESET);
		}
		catch(XPathExpressionException e)
		{
			e.printStackTrace();
			throw new RuntimeException("There was an exception when tried to create an XPath expression to find the elements in the xml document. See the console output for the stacktrace. Is the element name valid?");
		}

		for(int i = 0; i < nodes.getLength(); ++i)
		{
			Text nodeText = (Text)nodes.item(i); // If the text is inside a CDATA tag, this tag will not be present in nodeText, just the text enclosed by the CDATA tag
			texts.add(nodeText.getTextContent());
		}

		return texts;
	}


	public NodeList getElements(String tagName)
	{
		XPath xPath = getXPathFactory().newXPath();

		NodeList nodes;
		try
		{
			nodes = (NodeList)xPath.evaluate("//" + tagName, getXmlDocument(), XPathConstants.NODESET);
		}
		catch(XPathExpressionException e)
		{
			e.printStackTrace();
			throw new RuntimeException("There was an exception when tried to create an XPath expression to find the elements in the xml document. See the console output for the stacktrace. Is the element name valid?");
		}

		return nodes;
	}

}
