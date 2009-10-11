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
package ccw.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.index.IndexEntry;
import ccw.index.IndexManager;

public class OpenSymbolHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(
				event);
		Shell shell = window.getShell();
		ElementListSelectionDialog elementListSelectionDialog = new ElementListSelectionDialog(shell, new LabelProvider());
		IndexManager indexManager = CCWPlugin.getDefault().getIndexManager();
		elementListSelectionDialog.setElements(indexManager.query().toArray());
		elementListSelectionDialog.open();
		Object result = elementListSelectionDialog.getFirstResult();
		if (result instanceof IndexEntry) {
			IndexEntry entry = (IndexEntry) result;
			IFile file = entry.getFile();
			int lineNr = entry.getLineNr();
			IEditorPart editor;
				editor = IDE.openEditor(window.getActivePage(), file);
			ClojureCore.gotoEditorLine(editor, lineNr);			
		}
		} catch (Exception e) {
			throw new ExecutionException("could not open symbol",e);
		}
		return null;
	}

}
