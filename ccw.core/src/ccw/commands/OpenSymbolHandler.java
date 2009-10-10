package ccw.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenSymbolHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveWorkbenchWindow(
				event).getShell();
		ElementListSelectionDialog elementListSelectionDialog = new ElementListSelectionDialog(shell, new LabelProvider());
		elementListSelectionDialog.setElements(new String[] {"cons", "conj"});
		elementListSelectionDialog.open();
		return null;
	}

}
