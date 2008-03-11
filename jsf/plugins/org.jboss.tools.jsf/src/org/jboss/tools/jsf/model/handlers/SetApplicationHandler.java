/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.jsf.model.handlers;

import java.util.*;
import org.jboss.tools.common.meta.action.XActionInvoker;
import org.jboss.tools.common.meta.action.impl.*;
import org.jboss.tools.common.meta.action.impl.handlers.DefaultCreateHandler;
import org.jboss.tools.common.model.*;

public class SetApplicationHandler extends AbstractHandler {
	static String NAME_APPLICATION = "application";
	static String ENTITY_APPLICATION = "JSFApplication";
	static String ENTITY_APPLICATION_12 = "JSFApplication12";

	public boolean isEnabled(XModelObject object) {
		return object != null && object.isObjectEditable();
	}

	public void executeHandler(XModelObject object, Properties p) throws Exception {
		XModelObject child = object.getChildByPath(NAME_APPLICATION);
		if(child == null) {
			String childEntity = getApplicationEntity(object);
			if(childEntity == null) return;
			child = object.getModel().createModelObject(childEntity, null);
		}
		long ts = child.getTimeStamp();
		XActionInvoker.invoke("EditActions.Edit", child, p);
		if(!child.isActive() && ts != child.getTimeStamp()) {
			DefaultCreateHandler.addCreatedObject(object, child, p);
		}
	}
	
	String getApplicationEntity(XModelObject object) {
		if(object.getModelEntity().getChild(ENTITY_APPLICATION) != null) return ENTITY_APPLICATION;
		if(object.getModelEntity().getChild(ENTITY_APPLICATION_12) != null) return ENTITY_APPLICATION_12;
		return null;
	}

}
