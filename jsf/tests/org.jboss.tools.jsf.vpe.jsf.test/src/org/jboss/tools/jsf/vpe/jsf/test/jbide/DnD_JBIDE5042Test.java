/*******************************************************************************
  * Copyright (c) 2007-2009 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
package org.jboss.tools.jsf.vpe.jsf.test.jbide;

import java.io.IOException;
import java.lang.reflect.Field;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.jboss.tools.jsf.vpe.jsf.test.JsfAllTests;
import org.jboss.tools.jst.jsp.jspeditor.JSPMultiPageEditor;
import org.jboss.tools.vpe.dnd.VpeDnD;
import org.jboss.tools.vpe.editor.VpeController;
import org.jboss.tools.vpe.editor.VpeEditorPart;
import org.jboss.tools.vpe.editor.mozilla.MozillaEditor;
import org.jboss.tools.vpe.editor.mozilla.MozillaEventAdapter;
import org.jboss.tools.vpe.ui.test.TestUtil;
import org.jboss.tools.vpe.ui.test.VpeTest;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.mozilla.interfaces.nsIDOMElement;
import org.mozilla.interfaces.nsIDOMEventTarget;
import org.mozilla.interfaces.nsIDOMMouseEvent;
import org.mozilla.interfaces.nsIDOMNSUIEvent;
import org.mozilla.interfaces.nsIDOMNode;
import org.mozilla.interfaces.nsIDragService;
import org.mozilla.interfaces.nsIDragSession;
import org.mozilla.interfaces.nsIScriptableRegion;
import org.mozilla.interfaces.nsISupportsArray;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Tests Drag&Drop functionality of the VPE.
 * 
 * @see JIRA Issue JBIDE-5042 ( https://jira.jboss.org/jira/browse/JBIDE-5042 ):
 * "Enhance DnD support in VPE"
 * 
 * @author yradtsevich
 */
public class DnD_JBIDE5042Test  extends VpeTest {
	private static final String DROP_CONTAINER_ID = "cell_01";
	private static final String DRAG_ICON_ID = "dragIcon";
	private static final String DRAGGABLE_ID = "draggable"; //$NON-NLS-1$
	private static final String TEST_PAGE_NAME = "JBIDE/5042/JBIDE-5042.html"; //$NON-NLS-1$
	private static final Point DRAG_POINT = new Point(0, 0);
	/**Cells in the table are 100x100px. Thus this point means 'top of the second cell'*/
	private static final Point DROP_POINT = new Point(150, 10);
	private Mockery context = new Mockery();

	public DnD_JBIDE5042Test(String name) {
		super(name);
	}
	
	/**
	 * Try to open two pages in VPE and refresh them n times.
	 */
	public void testDnDWithMocks() throws Throwable {
		setException(null);

		JSPMultiPageEditor editor = openPageInVpe(TEST_PAGE_NAME);
		final MozillaEditor visualEditor = ((VpeEditorPart) editor.getVisualEditor())
				.getVisualEditor();
		VpeController controller = TestUtil.getVpeController(editor);
		TestUtil.waitForJobs();
		
		final nsIDragService dragService = mock(nsIDragService.class);
		final nsIDragSession dragSession = mock(nsIDragSession.class);
		checking(new Expectations() {{
			allowing(dragService).getCurrentSession(); will(returnValue(dragSession));
			allowing(dragSession).getSourceDocument(); will(returnValue(visualEditor.getDomDocument()));
			allowing(dragSession).setCanDrop(with(any(Boolean.TYPE)));
		}});
		replaceDragService(controller.getVpeDnD(), dragService);

		Element draggable = findSourceElementById(controller, DRAGGABLE_ID);
		setSelectedNode(controller, draggable);
		TestUtil.waitForJobs();

		final nsIDOMElement dragIcon = controller.getXulRunnerEditor()
				.getDOMDocument().getElementById(DRAG_ICON_ID);

		final nsIDOMMouseEvent mouseDownEvent = createMockMouseEvent(
				DRAG_POINT, "mousedown", dragIcon, "mouseDown");
		final nsIDOMMouseEvent dragOverMouseEvent = createMockMouseEvent(
				DROP_POINT, "dragover", null, "dragover");
		final nsIDOMMouseEvent dragDropMouseEvent = createMockMouseEvent(
				DROP_POINT, "dragdrop", null, "dragdrop");

		final MozillaEventAdapter eventListener = visualEditor.getMozillaEventAdapter();
		checking(new Expectations() {{
			allowing(dragService).invokeDragSession(
					with(any(nsIDOMNode.class)), with(any(nsISupportsArray.class)),
					with(any(nsIScriptableRegion.class)), with(any(Long.TYPE)));
			will(new CustomAction("invokeDragSession") {
				public Object invoke(Invocation invocation) throws Throwable {
					eventListener.handleEvent(dragOverMouseEvent);
					TestUtil.waitForJobs();
					eventListener.handleEvent(dragDropMouseEvent);
					TestUtil.waitForJobs();
					return null;
				}
			});
		}});
					
		eventListener.handleEvent(mouseDownEvent);
		TestUtil.waitForJobs();
		
		draggable = findSourceElementById(controller, DRAGGABLE_ID);
		assertEquals(DROP_CONTAINER_ID, ((Element)draggable.getParentNode()).getAttribute("id"));

		if (getException() != null) {
			throw getException();
		}
	}

	private nsIDOMMouseEvent createMockMouseEvent(final Point mousePos,
			final String type, final nsIDOMElement targetElement, String name) {
		final nsIDOMMouseEvent mouseEvent = mock(nsIDOMMouseEvent.class, name + "_nsIDOMMouseEvent");
		final nsIDOMEventTarget mouseEventTarget = mock(nsIDOMEventTarget.class, name + "_nsIDOMEventTarget");
		final nsIDOMNSUIEvent mouseNsUIEvent = mock(nsIDOMNSUIEvent.class, name + "_nsIDOMNSUIEvent");
		
		checking(new Expectations() {{
			allowing(mouseEvent).getType(); will(returnValue(type));
			allowing(mouseEvent).getButton(); will(returnValue(VpeController.LEFT_BUTTON));
			allowing(mouseEvent).getTarget(); will(returnValue(mouseEventTarget));
			allowing(mouseEvent).queryInterface(nsIDOMMouseEvent.NS_IDOMMOUSEEVENT_IID); will(returnValue(mouseEvent));
			allowing(mouseEventTarget).queryInterface(nsIDOMElement.NS_IDOMELEMENT_IID); will(returnValue(targetElement));
			allowing(mouseEvent).queryInterface(nsIDOMNSUIEvent.NS_IDOMNSUIEVENT_IID); will(returnValue(mouseNsUIEvent));
			allowing(mouseEvent).getClientX(); will(returnValue(mousePos.x));
			allowing(mouseEvent).getClientY(); will(returnValue(mousePos.y));
			allowing(mouseNsUIEvent).getPageX(); will(returnValue(mousePos.x));
			allowing(mouseNsUIEvent).getPageY(); will(returnValue(mousePos.y));
			allowing(mouseEvent).stopPropagation();
			allowing(mouseEvent).preventDefault();
		}});

		return mouseEvent;
	}

	private void replaceDragService(VpeDnD vpeDnD, nsIDragService dragService) throws Throwable {
		Field dragServiceField = vpeDnD.getClass().getDeclaredField("dragService");
		dragServiceField.setAccessible(true);
		dragServiceField.set(vpeDnD, dragService);
	}

	private JSPMultiPageEditor openPageInVpe(final String pageName) throws CoreException,
			PartInitException, IOException {
		IFile elementPageFile = (IFile) TestUtil.getComponentPath(
				pageName, JsfAllTests.IMPORT_PROJECT_NAME);
		IEditorInput input = new FileEditorInput(elementPageFile);

		JSPMultiPageEditor editor = (JSPMultiPageEditor) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().openEditor(input,
						EDITOR_ID, true);

		return editor;
	}
	
	private void setSelectedNode(VpeController controller, Node node) {
		IndexedRegion sourceNodeBounds = ((IndexedRegion)node);
		
		controller.getPageContext().getSourceBuilder().getStructuredTextViewer()
				.setSelectedRange(sourceNodeBounds.getStartOffset(),
						sourceNodeBounds.getEndOffset() - sourceNodeBounds.getStartOffset());
	}

	/** @see org.jmock.Mockery#mock(java.lang.Class, java.lang.String) */
	public <T> T mock(Class<T> typeToMock, String name) {
		return context.mock(typeToMock, name);
	}

	/** @see org.jmock.Mockery#mock(java.lang.Class) */
	public <T> T mock(Class<T> typeToMock) {
		return context.mock(typeToMock);
	}

	/** @see org.jmock.Mockery#checking(Expectations) */
	public void checking(Expectations expectations) {
		context.checking(expectations);
	}
}