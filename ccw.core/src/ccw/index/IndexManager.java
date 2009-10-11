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

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;

import ccw.ClojureCore;
import ccw.ClojureProject;
import clojure.lang.LineNumberingPushbackReader;
import clojure.lang.LispReader;
import clojure.lang.Obj;
import clojure.lang.LispReader.ReaderException;

public class IndexManager {
	private final ConcurrentHashMap<String, IndexEntry> entryMap = new ConcurrentHashMap<String, IndexEntry>();

	public void index(IProject project) {
		try {
			System.out.println("Indexing clojure files");
			final List<IFile> clojureFiles = new ArrayList<IFile>();
			ClojureProject clojureProject = ClojureCore.getClojureProject(project);
			IResourceProxyVisitor cljFileCollector = new IResourceProxyVisitor() {

				public boolean visit(IResourceProxy proxy) throws CoreException {
					if (IResource.FILE == proxy.getType()) {
						if ("clj".equals(proxy.requestFullPath()
								.getFileExtension())) {
							clojureFiles.add((IFile) proxy.requestResource());
							
							return false;
						}
					}
					return true;
				}
			};
			for(IFolder sourceFolder: clojureProject.sourceFolders()) {
				sourceFolder.accept(cljFileCollector, IResource.NONE);
				
			}
			for (IFile resource : clojureFiles) {
				System.out.println("Indexing: " + resource);
				index(resource);
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void index(IFile file) throws CoreException {
		InputStreamReader reader = new InputStreamReader(file.getContents(),
				Charset.forName(file.getCharset()));
		LineNumberingPushbackReader pushbackReader = new LineNumberingPushbackReader(
				reader);
		Object EOF = new Object();
		ArrayList<Object> input = new ArrayList<Object>();
		Object result = null;
		while (true) {
			try {
				result = LispReader.read(pushbackReader, false, EOF, false);
				if (result == EOF) {
					break;
				}
				input.add(result);
				if(result instanceof Obj) {
					IndexEntry indexEntry = new IndexEntry((Obj) result, file.getProjectRelativePath().toPortableString(), file);
					entryMap.put(indexEntry.getKey(), indexEntry);
				}
			} catch (ReaderException e) {
				// ignore, probably a syntax error
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public List<IndexEntry> query() {
		List<IndexEntry> result = new ArrayList<IndexEntry>(entryMap.values());
		return result;
	}
}
