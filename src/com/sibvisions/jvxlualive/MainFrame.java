/*
 * Copyright 2017 SIB Visions GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.sibvisions.jvxlualive;

import java.nio.charset.StandardCharsets;

import javax.rad.genui.UIColor;
import javax.rad.genui.UIImage;
import javax.rad.genui.component.UICustomComponent;
import javax.rad.genui.component.UIIcon;
import javax.rad.genui.container.UIFrame;
import javax.rad.genui.container.UIPanel;
import javax.rad.genui.container.UISplitPanel;
import javax.rad.genui.layout.UIBorderLayout;
import javax.rad.genui.layout.UIFormLayout;
import javax.rad.ui.IAlignmentConstants;
import javax.rad.ui.IComponent;
import javax.rad.ui.IContainer;
import javax.rad.ui.layout.IFormLayout.IConstraints;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import com.sibvisions.rad.lua.LuaEnvironment;
import com.sibvisions.util.type.FileUtil;
import com.sibvisions.util.type.ResourceUtil;

/**
 * The {@link MainFrame} is an {@link UIFrame} extension and is the main frame.
 * 
 * @author Robert Zenz
 */
public class MainFrame extends UIFrame
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * The {@link UIPanel} which serves as container for the created
	 * {@link IComponent} and as main display area.
	 */
	private UIPanel displayContainer = null;
	
	/** The {@link LuaEnvironment} that will be used. */
	private LuaEnvironment environment = new LuaEnvironment();
	
	/** The {@link ErrorDisplay} if an error occurs. */
	private ErrorDisplay errorDisplay = null;
	
	/** The previously created {@link IComponent}. */
	private IComponent previousComponent = null;
	
	/** The {@link RSyntaxTextArea} that is used. */
	private RSyntaxTextArea textArea = null;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Creates a new instance of {@link MainFrame}.
	 */
	public MainFrame()
	{
		super();
		
		initializeUI();
		
		textArea.setText(new String(FileUtil.getContent(ResourceUtil.getResourceAsStream("/com/sibvisions/jvxlualive/default.lua")), StandardCharsets.UTF_8));
		textArea.getDocument().addDocumentListener(new CodeChangedListener());
		
		updateDisplayedComponent();
	}
	
	/**
	 * Initializes the UI.
	 */
	private void initializeUI()
	{
		UIFormLayout headerLayout = new UIFormLayout();
		
		UIPanel headerPanel = new UIPanel();
		headerPanel.setLayout(headerLayout);
		headerPanel.setBackground(UIColor.white);
		headerPanel.add(new UIIcon(UIImage.getImage("/com/sibvisions/jvxlualive/images/jvx.png")), headerLayout.getConstraints(0, 0));
		headerPanel.add(new UIIcon(UIImage.getImage("/com/sibvisions/jvxlualive/images/lua.png")), headerLayout.getConstraints(-1, 0));
		addBorder(headerPanel, IAlignmentConstants.ALIGN_STRETCH, IAlignmentConstants.ALIGN_BOTTOM);
		
		errorDisplay = new ErrorDisplay("Error");
		
		textArea = new RSyntaxTextArea();
		textArea.setAutoIndentEnabled(true);
		textArea.setBracketMatchingEnabled(true);
		textArea.setCaretPosition(0);
		textArea.setClearWhitespaceLinesEnabled(false);
		textArea.setCloseCurlyBraces(true);
		textArea.setLineWrap(false);
		textArea.setEditable(true);
		textArea.setMarkOccurrences(true);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_LUA);
		textArea.setTabSize(4);
		textArea.setWhitespaceVisible(true);
		
		UICustomComponent wrappedTextArea = new UICustomComponent(new RTextScrollPane(textArea, true));
		
		UIBorderLayout editorLayout = new UIBorderLayout();
		editorLayout.setMargins(10, 10, 10, 10);
		
		UIPanel editorPanel = new UIPanel();
		editorPanel.setLayout(editorLayout);
		editorPanel.add(wrappedTextArea, UIBorderLayout.CENTER);
		
		displayContainer = new UIPanel();
		displayContainer.setLayout(new UIBorderLayout());
		
		UISplitPanel splitPanel = new UISplitPanel(UISplitPanel.SPLIT_LEFT_RIGHT);
		splitPanel.setFirstComponent(editorPanel);
		splitPanel.setSecondComponent(displayContainer);
		splitPanel.setDividerPosition(496);
		
		setLayout(new UIBorderLayout());
		setIconImage(UIImage.getImage("/com/sibvisions/jvxlualive/images/icon.png"));
		setSize(1024, 600);
		setTitle("JVx Lua");
		add(headerPanel, UIBorderLayout.NORTH);
		add(splitPanel, UIBorderLayout.CENTER);
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Adds a "border" to an {@link IContainer}.
	 * 
	 * @param pContainer the {@link IContainer}.
	 * @param pHorizontalAlignment the horizontal alignment.
	 * @param pVerticalAlignment the vertical alignment.
	 */
	private static void addBorder(IContainer pContainer, int pHorizontalAlignment, int pVerticalAlignment)
	{
		UIFormLayout layout = (UIFormLayout)pContainer.getLayout();
		IConstraints constraints = layout.getConstraints(layout.getTopAnchor(), layout.getLeftAnchor(), layout.getBottomAnchor(), layout.getRightAnchor());
		
		UIIcon border = new UIIcon(UIImage.getImage("/com/sibvisions/jvxlualive/images/border-pixel.png"));
		border.setHorizontalAlignment(pHorizontalAlignment);
		border.setVerticalAlignment(pVerticalAlignment);
		
		pContainer.add(border, constraints);
	}
	
	/**
	 * Updates the current display {@link IComponent}, effectively re-evaluating
	 * the Lua script.
	 */
	private void updateDisplayedComponent()
	{
		if (displayContainer.getComponentCount() > 0 && !(displayContainer.getComponent(0) instanceof ErrorDisplay))
		{
			previousComponent = displayContainer.getComponent(0);
		}
		
		displayContainer.removeAll();
		
		try
		{
			IComponent component = (IComponent)CoerceLuaToJava.coerce(environment.execute(textArea.getText()), IComponent.class);
			
			if (component != null)
			{
				displayContainer.add(component, UIBorderLayout.CENTER);
			}
		}
		catch (Throwable th)
		{
			th.printStackTrace();
			errorDisplay.setError(th.getMessage(), previousComponent);
			
			displayContainer.add(errorDisplay, UIBorderLayout.CENTER);
		}
	}
	
	//****************************************************************
	// Subclass definition
	//****************************************************************
	
	/**
	 * The {@link CodeChangedListener} is a {@link DocumentListener} which
	 * invokes the update.
	 * 
	 * @author Robert Zenz
	 */
	private final class CodeChangedListener implements DocumentListener
	{
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Interface implementation
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void changedUpdate(DocumentEvent pEvent)
		{
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void insertUpdate(DocumentEvent pEvent)
		{
			updateDisplayedComponent();
		}
		
		@Override
		public void removeUpdate(DocumentEvent pEvent)
		{
			updateDisplayedComponent();
		}
		
	}	// MainFrame
	
}	// MainFrame
