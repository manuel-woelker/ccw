/**
 * 
 */
package ccw.editors.antlrbased;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

final class ClojureSymbolHyperlink implements IHyperlink {
	private final String symbolString;
	private final IRegion word;

	ClojureSymbolHyperlink(String symbolString, IRegion word) {
		this.symbolString = symbolString;
		this.word = word;
	}

	public IRegion getHyperlinkRegion() {

		return word;
	}

	public String getHyperlinkText() {
		return "Go to definiton of \""+symbolString+"\"";
	}

	public String getTypeLabel() {
		return "clojure symbol";
	}

	public void open() {
		// TODO: get namespace
		// TODO: open symbol
		System.out.println("open link "+symbolString);
	}
}