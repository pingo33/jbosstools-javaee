/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.seam.ui.views.actions;

import java.util.Arrays;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.IExtensionActivationListener;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.INavigatorActivationService;
import org.jboss.tools.seam.core.SeamPreferences;
import org.jboss.tools.seam.ui.views.ViewConstants;

/**
 * Action provider for Seam Components view.
 * @author Viacheslav Kabanovich
 */
public class ScopePresentationActionProvider extends CommonActionProvider implements ViewConstants {

	private ICommonActionExtensionSite fExtensionSite;
	IExtensionStateModel stateModel;
	private String fExtensionId;
	private IActionBars fActionBars;
	private boolean fEnabled = false;
	SeamViewLayoutActionGroup actionGroup;

	private IExtensionActivationListener fMenuUpdater= new IExtensionActivationListener() {

		public void onExtensionActivation(String viewerId, String[] theNavigatorExtensionIds, boolean isCurrentlyActive) {

			if (fExtensionSite != null && fActionBars != null) {

				int search= Arrays.binarySearch(theNavigatorExtensionIds, fExtensionId);
				if (search > -1) {
					if (isMyViewer(viewerId)) {
						if (wasEnabled(isCurrentlyActive))
							actionGroup.fillActionBars(fActionBars);

						else
							if (wasDisabled(isCurrentlyActive)) {
								actionGroup.unfillActionBars(fActionBars);
							}
						// else no change 
					}
					fEnabled = isCurrentlyActive;
				}
			}

		}

		private boolean isMyViewer(String viewerId) {
			String myViewerId= fExtensionSite.getViewSite().getId();
			return myViewerId != null && myViewerId.equals(viewerId);
		}

		private boolean wasDisabled(boolean isActive) {
			return fEnabled && !isActive;
		}

		private boolean wasEnabled(boolean isActive) {
			return !fEnabled && isActive;
		}
	};


	public ScopePresentationActionProvider() {}

	public void init(ICommonActionExtensionSite site) {
		super.init(site);
		fExtensionSite = site;
		stateModel = site.getExtensionStateModel();
		actionGroup = new SeamViewLayoutActionGroup(fExtensionSite.getStructuredViewer(), stateModel);
		INavigatorActivationService activationService= fExtensionSite.getContentService().getActivationService();
		activationService.addExtensionActivationListener(fMenuUpdater);
		fExtensionId = fExtensionSite.getExtensionId();
		fEnabled = true;
	}

	public void setPackageStructureFlat(boolean s) {
		IEclipsePreferences p = SeamPreferences.getInstancePreferences();
		p.put(PACKAGE_STRUCTURE, s ? "flat" : "hierarchical");
	}
	
	public void setScopePresentedAsLabel(boolean s) {
		IEclipsePreferences p = SeamPreferences.getInstancePreferences();
		p.put(SCOPE_PRESENTATION, s ? "label" : "node");
	}
	
    public void fillActionBars(IActionBars actionBars) {
    	fActionBars = actionBars;
		actionGroup.fillActionBars(actionBars);
    }

	boolean isFlatLayout = true;
	boolean isScopeLable = false;
    
    public void dispose() {
    	super.dispose();
		fExtensionSite.getContentService().getActivationService().removeExtensionActivationListener(fMenuUpdater);
    }

	void updateViewer() {
		BusyIndicator.showWhile(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell().getDisplay(),
			new Runnable() {
				public void run() {
					try {
						getActionSite().getStructuredViewer().refresh();
					} catch (Exception e2) {
						e2.printStackTrace();
						//ignore
					}
				}
			}
		);
	}

	public void restoreState(IMemento memento) {
		boolean isCurrentLayoutFlat = true;
		String state = null;
		if (memento != null)
			state = memento.getString("seam-isFlatLayout");

		if (state == null) {
			state = SeamPreferences.getInstancePreference(PACKAGE_STRUCTURE);
		}

		isCurrentLayoutFlat = !(PACKAGES_HIERARCHICAL.equals(state));

		setFlatLayout(isCurrentLayoutFlat);
		
		boolean isCurrentScopeLabel = false;
		state = null;
		if (memento != null)
			state = memento.getString("seam-isScopeLabel");
		
		if (state == null) {
			state = SeamPreferences.getInstancePreference(SCOPE_PRESENTATION);
		}
		
		isCurrentScopeLabel = !SCOPE_AS_NODE.equals(state);

		setScopeLable(isCurrentScopeLabel);
		
	}

	public void saveState(IMemento aMemento) {
		super.saveState(aMemento);
		
		setPackageStructureFlat(isFlatLayout);
		setScopePresentedAsLabel(isScopeLable);
		
	}

	void setFlatLayout(boolean b) {
		isFlatLayout = b;
		stateModel.setBooleanProperty(PACKAGE_STRUCTURE, b);
		actionGroup.setFlatLayout(b);
	}
	
	void setScopeLable(boolean b) {
		isScopeLable = b;
		stateModel.setBooleanProperty(SCOPE_PRESENTATION, b);
		actionGroup.setScopeLable(b);
	}

}
