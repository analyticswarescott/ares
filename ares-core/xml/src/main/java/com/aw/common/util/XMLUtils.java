package com.aw.common.util;

import java.io.Reader;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * XML related utilities
 *
 *
 *
 */
public class XMLUtils {

	private static final String PACKAGE_INFO = "package-info";
	private static class NamespaceFilter<T> extends XMLFilterImpl {

		public NamespaceFilter(Class<T> type) throws Exception {

			Class<?> packageInfo = Class.forName(type.getPackage().getName() + "." + PACKAGE_INFO);
			m_packageInfo = packageInfo;

		}

	    @Override
	    public void endElement(String uri, String localName, String qName)
	            throws SAXException {
	        super.endElement(m_packageInfo.getAnnotation(XmlSchema.class).namespace(), localName, qName);
	    }

	    @Override
	    public void startElement(String uri, String localName, String qName,
	            Attributes atts) throws SAXException {
	        super.startElement(m_packageInfo.getAnnotation(XmlSchema.class).namespace(), localName, qName, atts);
	    }

	    private Class<?> m_packageInfo;

	}

	/**
	 *
	 * Unmarshal an xml file into the given class
	 *
	 * @param reader The reader to read from
	 * @return The object that was deserialized
	 * @throws Exception
	 */
	public static <T> T unmarshal(Reader reader, Class<T> type) throws Exception {

		//create the context
        JAXBContext jc = JAXBContext.newInstance(type);

        //create the filter
        XMLFilter filter = new NamespaceFilter<T>(type);

		//connect up the filter to the parser
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        filter.setParent(xr);

        //build the unmarshaller
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();

        //set the content handler
        filter.setContentHandler(unmarshallerHandler);

        //parse
        filter.parse(new InputSource(reader));

        //return the result
        return (T)unmarshallerHandler.getResult();

	}

	/**
	 * Write the object as XML to the given writer
	 *
	 * @param writer The writer to write to
	 * @param object The object to serialize
	 * @throws Exception If anything goes wrong
	 */
	public static void marshal(Writer writer, Object object) throws Exception {

		//create the context
        JAXBContext jc = JAXBContext.newInstance(object.getClass());

        //marshal it pretty
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(object, writer);

	}

}
