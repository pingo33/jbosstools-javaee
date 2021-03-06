/*******************************************************************************
 * Copyright (c) 2011-2012 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jsf.test;

import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.jboss.tools.common.model.project.IModelNature;
import org.jboss.tools.common.model.util.EclipseResourceUtil;
import org.jboss.tools.jst.web.project.list.IWebPromptingProvider;
import org.jboss.tools.jst.web.project.list.WebPromptingProvider;
import org.jboss.tools.test.util.ProjectImportTestSetup;

public class JSFBeansTest extends TestCase {
	IProject project = null;

	public JSFBeansTest() {}

	public void setUp() throws CoreException {
		project = ProjectImportTestSetup.loadProject("JSFKickStartOldFormat");
	}
	
	public void testBeanWithSuper() {
		IModelNature n = EclipseResourceUtil.getModelNature(project);
		assertNotNull("Test project " + project.getName() + " has no model nature.", n);
		assertNotNull("XModel for project " + project.getName() + " is not loaded.", n.getModel());
		List<Object> result = WebPromptingProvider.getInstance().getList(n.getModel(), IWebPromptingProvider.JSF_BEAN_PROPERTIES, "user.", new Properties());
		assertNotNull("No results for bean " + " user.", n.getModel());
		
		assertTrue("Property 'parent' inherited from super class is not found in bean 'user'", result.contains("parent"));
		
	}
	
	public void testGettersAndSetters() {
		IModelNature n = EclipseResourceUtil.getModelNature(project);
		List<Object> result = WebPromptingProvider.getInstance().getList(n.getModel(), IWebPromptingProvider.JSF_BEAN_METHODS, "user.", new Properties());
		assertTrue("Method getX1 is not found. It is not a getter because it has type void.", result.contains("getX1"));
		assertTrue("Method getX2 is not found. It is not a getter because it has a parameter.", result.contains("getX2"));
		assertTrue("Method setX3 is not found. It is not a setter because it has 2 parameters", result.contains("setX3"));
	}

//	protected void tearDown() throws CoreException{
//		if(provider!=null) {
//			provider.dispose();
//			provider = null;
//		}
//	}

}
