/*******************************************************************************
* Copyright (c) 2009 Manuel Woelker.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Manuel Woelker - initial prototype implementation
*******************************************************************************/
package ccw.index;

import java.util.List;

import org.eclipse.core.resources.IFile;

import clojure.lang.IMapEntry;
import clojure.lang.Keyword;
import clojure.lang.Obj;

public class IndexEntry {
	private static final Keyword KEYWORD_LINE = Keyword.intern(null, "line"); //$NON-NLS-1
	
	private final Obj obj;
	private final String symbol;
	private final String namespace;
	private final IFile file;

	public IndexEntry(Obj obj, String namespace, IFile file) {
		this.obj = obj;
		this.namespace = namespace;
		this.file = file;
		String symbol = null;
		if (obj instanceof List<?>) {
			List<?> list = (List<?>) obj;
			if (list.size() > 1) {
				symbol = list.get(1).toString();
			}
		}
		if (symbol == null) {
			symbol = obj.toString();
		}
		this.symbol = symbol;
	}

	public String getKey() {
		return namespace + "~" + symbol;
	}

	@Override
	public String toString() {
		return symbol + " - " + namespace;
	}

	public IFile getFile() {
		return file;
	}

	public int getLineNr() {
		int lineNr = -1;
		if (obj.meta() == null) {
			return lineNr;
		}
		IMapEntry line = obj.meta().entryAt(KEYWORD_LINE);
		if (line != null && line.val() instanceof Number) {
			lineNr = ((Number) line.val()).intValue();
		}
		return lineNr;
	}
}
