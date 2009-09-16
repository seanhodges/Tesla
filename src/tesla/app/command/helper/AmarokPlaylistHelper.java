package tesla.app.command.helper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class AmarokPlaylistHelper implements ICommandHelper {
	
	public static final String MAGIC_MARKER = "[amarokplaylist]";
	
	public String compileQuery(String getXmlPathCommand, boolean includeMarker) {
		StringBuilder builder = new StringBuilder();
		if (includeMarker) {
			builder.append("echo " + MAGIC_MARKER + ";");
		}
		builder.append("xml_path=$(" + getXmlPathCommand + "); ");
		builder.append("if [[ ${xml_path} != \"\" ]]; then ");
		builder.append(		"python -c \"");
		builder.append(			"import libxml2;");
		builder.append(			"doc = libxml2.parseFile('${xml_path}');");
		builder.append(			"ctxt = doc.xpathNewContext();");
		builder.append(			"res = ctxt.xpathEval('/playlist');\n");
		builder.append(			"for track in res:\n");
		builder.append(			"    print track;\n");
		builder.append(			"ctxt.xpathFreeContext(); doc.freeDoc()\"; ");
		builder.append("fi");
		return builder.toString();
	}

	public String compileQuery(String xmlFile) {
		return compileQuery(xmlFile, true);
	}

	public List<String> evaluateOutputAsList(String rawOut) {
		rawOut = rawOut.trim();
		
		RhythmDBParser contentHandler = new RhythmDBParser();
		XMLReader reader;
		try {
			InputSource is = new InputSource();
			is.setByteStream((InputStream)new ByteArrayInputStream(rawOut.getBytes("UTF-8")));
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            reader = sp.getXMLReader(); 
			reader.setContentHandler(contentHandler);
			reader.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Get all the playlist data
		List<Map<String, String>> playlistData = contentHandler.getOutput();
		List<String> out = new ArrayList<String>();
		
		// Return a list of the track titles
		for (Map<String, String> item : playlistData) {
			out.add(item.get("title"));
		}
		
		return out;
	}
	
	public boolean evaluateOutputAsBoolean(String rawOut) {
		return false;
	}

	public String evaluateOutputAsString(String rawOut) {
		return null;
	}

	public Map<String, String> evaluateOutputAsMap(String rawOut) {
		return null;
	}

}
