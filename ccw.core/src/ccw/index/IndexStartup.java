package ccw.index;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IStartup;

import ccw.CCWPlugin;

public class IndexStartup implements IStartup {

	public void earlyStartup() {
		try {
			CCWPlugin.getDefault().getIndexManager().indexAll();
		} catch (CoreException e) {
			CCWPlugin.logError("indexing failed", e);
		}

	}

}
