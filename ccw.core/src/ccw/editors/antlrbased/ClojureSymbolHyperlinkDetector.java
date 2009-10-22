package ccw.editors.antlrbased;

import java.util.HashSet;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class ClojureSymbolHyperlinkDetector extends AbstractHyperlinkDetector {

	// TODO: Is this complete?
	private static final String CLOJURE_IDENTIFIER_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFHIJKLMNOPQRSTUVWXYZ0123456789-_*/:?!.";
	private static final HashSet<Character> clojureIdentifierChars = new HashSet<Character>();
	static {
		for (char c : CLOJURE_IDENTIFIER_CHARACTERS.toCharArray()) {
			clojureIdentifierChars.add(c);
		}
	}

	public ClojureSymbolHyperlinkDetector() {
	}

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			final IRegion region, boolean canShowMultipleHyperlinks) {
		IDocument document = textViewer.getDocument();
		final IRegion word = findWord(document, region.getOffset());
		try {
			final String symbolString = document.get(word.getOffset(), word
					.getLength());
			if (word != null) {
				return new IHyperlink[] { new ClojureSymbolHyperlink(symbolString, word) };

			}
		} catch (BadLocationException e) {
			return null;
		}
		return null;
	}

	// adapted from org.eclipse.jdt.internal.ui.text.JavaWordFinder
	public static IRegion findWord(IDocument document, int offset) {

		int start = -2;
		int end = -1;

		try {
			int pos = offset;
			char c;

			while (pos >= 0) {
				c = document.getChar(pos);
				if (!isClojureIdentifierPart(c))
					break;
				--pos;
			}
			start = pos;

			pos = offset;
			int length = document.getLength();

			while (pos < length) {
				c = document.getChar(pos);
				if (!isClojureIdentifierPart(c))
					break;
				++pos;
			}
			end = pos;

		} catch (BadLocationException x) {
		}

		if (start >= -1 && end > -1) {
			if (start == offset && end == offset)
				return new Region(offset, 0);
			else if (start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		}

		return null;
	}

	private static boolean isClojureIdentifierPart(char c) {
		return clojureIdentifierChars.contains(c);
	}
}
