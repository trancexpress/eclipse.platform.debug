package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.ant.preferences.AntClasspathBlock;
import org.eclipse.ui.externaltools.internal.ant.preferences.IAntBlockContainer;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.help.WorkbenchHelp;

public class AntClasspathTab extends AbstractLaunchConfigurationTab implements IAntBlockContainer {

	private Button useDefaultButton;
	private AntClasspathBlock antClasspathBlock= new AntClasspathBlock();
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		top.setLayout(layout);
		top.setLayoutData(new GridData(GridData.FILL_BOTH));

		setControl(top);
		WorkbenchHelp.setHelp(top, IExternalToolsHelpContextIds.EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_ANT_CLASSPATH_TAB);
		
		createChangeClasspath(top);
		antClasspathBlock.setContainer(this);
		antClasspathBlock.createContents(top);
	}

	private void createChangeClasspath(Composite top) {
		Composite changeClasspath = new Composite(top, SWT.NONE);
		changeClasspath.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		changeClasspath.setLayout(layout);
		changeClasspath.setFont(top.getFont());

		useDefaultButton = new Button(changeClasspath, SWT.CHECK);
		useDefaultButton.setFont(top.getFont());
		useDefaultButton.setText(AntLaunchConfigurationMessages.getString("AntClasspathTab.Use_&global")); //$NON-NLS-1$
		useDefaultButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				toggleUseDefaultClasspath();
				updateLaunchConfigurationDialog();
			}

		});
	}

	private void toggleUseDefaultClasspath() {
		boolean enable = !useDefaultButton.getSelection();
		antClasspathBlock.setEnabled(enable);
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		String urlStrings= null;
		try {
			urlStrings = configuration.getAttribute(IExternalToolConstants.ATTR_ANT_CUSTOM_CLASSPATH, (String) null);
		} catch (CoreException e) {
		}
		if (urlStrings == null) {
			useDefaultButton.setSelection(true);
			antClasspathBlock.setTablesEnabled(false);
		} else {
			String antHomeString= null;
			try {
				antHomeString= configuration.getAttribute(IExternalToolConstants.ATTR_ANT_HOME, (String)null);
			} catch (CoreException e) {
			}
			antClasspathBlock.initializeAntHome(antHomeString);
			useDefaultButton.setSelection(false);

			List userURLs= new ArrayList();
			List antURLs= new ArrayList();
			AntUtil.getCustomClasspaths(configuration, antURLs, userURLs);
			antClasspathBlock.setUserTableInput(userURLs);
			antClasspathBlock.setAntTableInput(antURLs);
			antClasspathBlock.setTablesEnabled(true);
		}

		toggleUseDefaultClasspath();
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (useDefaultButton.getSelection()) {
			configuration.setAttribute(IExternalToolConstants.ATTR_ANT_CUSTOM_CLASSPATH, (String)null);
			configuration.setAttribute(IExternalToolConstants.ATTR_ANT_HOME, (String)null);
			return;
		}
		List antUrls= antClasspathBlock.getAntURLs();
		List userUrls= antClasspathBlock.getUserURLs();
		StringBuffer urlString= new StringBuffer();
		Iterator antUrlsItr= antUrls.iterator();
		while (antUrlsItr.hasNext()) {
			URL url = (URL) antUrlsItr.next();
			urlString.append(url.getFile());
			urlString.append(',');
		}
		if (userUrls.size() > 0) {
			urlString.append('*');
		}
		Iterator userUrlsItr= userUrls.iterator();
		while (userUrlsItr.hasNext()) {
			URL url = (URL) userUrlsItr.next();
			urlString.append(url.getFile());
			urlString.append(',');
		}
		if (urlString.length() > 0) {
			configuration.setAttribute(IExternalToolConstants.ATTR_ANT_CUSTOM_CLASSPATH, urlString.substring(0, urlString.length() - 1));
		} else {
			configuration.setAttribute(IExternalToolConstants.ATTR_ANT_CUSTOM_CLASSPATH, (String)null);
		}
		
		String antHomeText= antClasspathBlock.getAntHome();
		configuration.setAttribute(IExternalToolConstants.ATTR_ANT_HOME, antHomeText);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return AntLaunchConfigurationMessages.getString("AntClasspathTab.Classpath_6"); //$NON-NLS-1$
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return antClasspathBlock.getClasspathImage();
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#canSave()
	 */
	public boolean canSave() {
		if (antClasspathBlock.isAntHomeEnabled()) {
			return antClasspathBlock.validateAntHome();
		} else {
			return super.canSave();
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		setMessage(null);
		if (antClasspathBlock.isAntHomeEnabled()) {
			return antClasspathBlock.validateAntHome();
		} else {
			return super.isValid(launchConfig);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.externaltools.internal.ant.preferences.IAntClasspathBlockContainer#setMessage(java.lang.String)
	 */
	public void setMessage(String message) {
		super.setMessage(message);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.externaltools.internal.ant.preferences.IAntClasspathBlockContainer#setErrorMessage(java.lang.String)
	 */
	public void setErrorMessage(String message) {
		super.setErrorMessage(message);
	}
	
	/* (non-Javadoc)
	* @see org.eclipse.ui.externaltools.internal.ant.preferences.IAntClasspathBlockContainer#createPushButton(org.eclipse.swt.widgets.Composite, java.lang.String)
	*/
	public Button createPushButton(Composite parent, String buttonText) {
		return super.createPushButton(parent, buttonText, null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.externaltools.internal.ant.preferences.IAntClasspathBlockContainer#update()
	 */
	public void update() {
		updateLaunchConfigurationDialog();
	}
}