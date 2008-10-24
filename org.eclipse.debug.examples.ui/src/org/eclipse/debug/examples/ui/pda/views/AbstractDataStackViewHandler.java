/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.ui.pda.views;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.examples.core.pda.model.PDADebugElement;
import org.eclipse.debug.examples.core.pda.model.PDADebugTarget;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Base class for command handlers for data stack view.
 */
abstract public class AbstractDataStackViewHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        if (part instanceof DataStackView) {
            DataStackView view = (DataStackView)part;
            
            ISelection selection = DebugUITools.getDebugContextForEventChecked(event);
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection ss = (IStructuredSelection)selection;
                if (ss.getFirstElement() instanceof IAdaptable) {
                    PDADebugElement element = (PDADebugElement)
                        ((IAdaptable)ss.getFirstElement()).getAdapter(PDADebugElement.class);
                    if (element != null) {
                        doExecute(
                            view, 
                            (PDADebugTarget)element.getDebugTarget(), 
                            HandlerUtil.getCurrentSelectionChecked(event));
                    }
                }
            }
        } else {
            throw new ExecutionException("Handler must be with DataStackView only");
        }
        return null;
    }
    
    /**
     * Performs the actual handler operation. 
     * 
     * @param view The view that the handler was invoked in.
     * @param target The current active debug target.
     * @param selection The current selection in view.
     */
    abstract protected void doExecute(DataStackView view, PDADebugTarget target, ISelection selection) throws ExecutionException;
}
