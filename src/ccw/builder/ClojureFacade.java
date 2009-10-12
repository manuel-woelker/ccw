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
package ccw.builder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import ccw.CCWPlugin;

public class ClojureFacade {
	ClassLoader classLoader;
	private Class<?> rtClass;
	private Class<?> compilerClass;
	private Class<?> varClass;
	private Class<?> associativeClass;
	private Method mPushThreadBindings;
	private Method mPopThreadBindings;
	private Method mMap;
	private Method mVar;
	private Object varCompilePath;
	private Object varCompileFiles;
	private Object varUseContextClassloader;
	private Method mLoadFile;

	public ClojureFacade(File outputDir) {
		this(outputDir, getDefaultClojureLib(), getDefaultClojureContribLib());
	}

	public ClojureFacade(File outputDir, File... classpath) {
		try {
			ArrayList<URL> urls = new ArrayList<URL>();
			urls.add(outputDir.toURI().toURL());
			for (File file : classpath) {
				urls.add(file.toURI().toURL());
			}
			classLoader = new URLClassLoader(urls.toArray(new URL[0]));
			rtClass = classLoader.loadClass("clojure.lang.RT");
			varClass = classLoader.loadClass("clojure.lang.Var");
			compilerClass = classLoader.loadClass("clojure.lang.Compiler");
			associativeClass = classLoader
					.loadClass("clojure.lang.Associative");
			mPushThreadBindings = varClass.getMethod("pushThreadBindings",
					associativeClass);
			mPopThreadBindings = varClass.getMethod("popThreadBindings");
			mMap = rtClass.getMethod("map", Array.newInstance(Object.class, 0)
					.getClass());
			mVar = rtClass.getMethod("var", String.class, String.class);
			mLoadFile = compilerClass.getMethod("loadFile", String.class);
			varCompilePath = mVar
					.invoke(null, "clojure.core", "*compile-path*");
			varCompileFiles = mVar.invoke(null, "clojure.core",
					"*compile-files*");
			varUseContextClassloader = mVar.invoke(null, "clojure.core",
					"*use-context-classloader*");
		} catch (Exception e) {
			throw new RuntimeException("Error creating Clojure Facade", e);
		}
	}

	private static File getDefaultClojureLib() {
		return getJarInsidePlugin("ccw.clojure", "clojure");
	}

	private static File getDefaultClojureContribLib() {
		return getJarInsidePlugin("ccw.clojurecontrib", "clojure-contrib");
	}

	private static File getJarInsidePlugin(String pluginName, String jarName) {
		try {
			Bundle bundle = Platform.getBundle(pluginName);
			File clojureBundlePath = FileLocator.getBundleFile(bundle);
			if (clojureBundlePath.isFile()) {
				CCWPlugin.logError(pluginName
						+ " plugin should be deployed as a directory");
				return null;
			}

			File clojureLibEntry = new File(clojureBundlePath, jarName + ".jar");
			if (!clojureLibEntry.exists()) {
				CCWPlugin.logError("Unable to locate " + jarName + " jar in "
						+ pluginName + " plugin");
				return null;
			}
			return clojureLibEntry;
		} catch (IOException e) {
			CCWPlugin.logError("Unable to find " + pluginName + " plugin");
			return null;
		}
	}

	// ClassLoader oldContextClassLoader =
	// Thread.currentThread().getContextClassLoader();
	// try {
	// Thread.currentThread().setContextClassLoader(RT.class.getClassLoader());
	// Var.pushThreadBindings(RT.map(compile_path, outputDir,
	// compile_files, true, use_context_classloader, true));
	// Compiler.loadFile(filename);
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } finally {
	// Thread.currentThread().setContextClassLoader(oldContextClassLoader);
	// Var.popThreadBindings();
	// }

	public void compile(String filename, String outputDir) {
		ClassLoader oldContextClassLoader = Thread.currentThread()
				.getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(classLoader);
			System.out.println("SIZE: " + mMap.getParameterTypes().length);
			System.out
					.println("TYPE: " + mMap.getParameterTypes()[0].getName());
			Object mmap = mMap.invoke(null, (Object) new Object[] {
					varCompilePath, outputDir, varCompileFiles, true,
					varUseContextClassloader, true });
			mPushThreadBindings.invoke(null, mmap);
			mLoadFile.invoke(null, filename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Thread.currentThread().setContextClassLoader(oldContextClassLoader);
			try {
				mPopThreadBindings.invoke(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
