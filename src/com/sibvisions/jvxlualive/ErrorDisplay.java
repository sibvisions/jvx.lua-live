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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.rad.genui.IFontAwesome;
import javax.rad.genui.UIColor;
import javax.rad.genui.UIComponent;
import javax.rad.genui.UIFont;
import javax.rad.genui.UIImage;
import javax.rad.genui.component.UICustomComponent;
import javax.rad.genui.component.UIIcon;
import javax.rad.genui.component.UILabel;
import javax.rad.genui.container.UIPanel;
import javax.rad.genui.layout.UIFormLayout;
import javax.rad.ui.IColor;
import javax.rad.ui.IComponent;
import javax.rad.ui.IDimension;
import javax.rad.ui.IImage;
import javax.rad.ui.container.IPanel;
import javax.swing.ImageIcon;

import com.sibvisions.rad.ui.swing.impl.SwingImage;

/**
 * The {@link ErrorDisplay} is an {@link UICustomComponent} which shows an error
 * together with an icon. It also has the ability to simulate an overlay by
 * using a picture of a component as abckground.
 * 
 * @author Robert Zenz
 */
public class ErrorDisplay extends UIComponent<IPanel>
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/** The {@link UIIcon} used for the background. */
	private UIIcon backgroundIcon = null;
	
	/** The {@link UILabel} for the error message. */
	private UILabel label = null;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Creates a new instance of {@link ErrorDisplay}.
	 *
	 * @param pErrorMessage the {@link String error message}.
	 */
	public ErrorDisplay(String pErrorMessage)
	{
		super(new UIPanel());
		
		UIIcon errorIcon = new UIIcon();
		errorIcon.setHorizontalAlignment(UIIcon.ALIGN_CENTER);
		errorIcon.setImage(UIImage.getImage(IFontAwesome.EXCLAMATION_TRIANGLE_SMALL + ";size=128;color=" + UIColor.toHex(Tango.SCARLET_RED_1)));
		errorIcon.setVerticalAlignment(UIIcon.ALIGN_CENTER);
		
		label = new UILabel();
		label.setBackground(null);
		label.setFont(UIFont.getDefaultFont().deriveFont(UIFont.BOLD, 16));
		label.setForeground(Tango.SCARLET_RED_1);
		label.setHorizontalAlignment(UILabel.ALIGN_CENTER);
		
		backgroundIcon = new UIIcon();
		backgroundIcon.setHorizontalAlignment(UIIcon.ALIGN_LEFT);
		backgroundIcon.setVerticalAlignment(UIIcon.ALIGN_TOP);
		
		UIFormLayout innerLayout = new UIFormLayout();
		innerLayout.setHorizontalAlignment(UIFormLayout.ALIGN_CENTER);
		innerLayout.setVerticalAlignment(UIFormLayout.ALIGN_CENTER);
		
		UIPanel innerPanel = new UIPanel();
		innerPanel.setLayout(innerLayout);
		innerPanel.setBackground(null);
		innerPanel.add(errorIcon, innerLayout.getConstraints(0, 0, -1, 0));
		innerPanel.add(label, innerLayout.getConstraints(0, 1, -1, 1));
		
		UIFormLayout layout = new UIFormLayout();
		
		uiResource.setLayout(layout);
		uiResource.setBackground(Tango.ALUMINIUM_6);
		uiResource.add(innerPanel, layout.getConstraints(0, 0, -1, -1));
		uiResource.add(backgroundIcon, layout.getConstraints(layout.getTopAnchor(), layout.getLeftAnchor(), layout.getBottomAnchor(), layout.getRightAnchor()));
		
		setError(pErrorMessage, null);
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Colorizes the given {@link Image}.
	 * 
	 * @param pImage the {@link Image} to colorize.
	 * @param pColor the {@link IColor} to use.
	 * @param pAlpha the alpha value of the color.
	 * @return the colorized {@link Image}.
	 */
	private static final Image colorize(Image pImage, IColor pColor, float pAlpha)
	{
		BufferedImage colorizedImage = new BufferedImage(
				pImage.getWidth(null),
				pImage.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D graphics = colorizedImage.createGraphics();
		
		graphics.drawImage(pImage, 0, 0, null);
		graphics.setColor(new Color(
				pColor.getRed() / 255f,
				pColor.getGreen() / 255f,
				pColor.getBlue() / 255f,
				pAlpha));
		graphics.fillRect(0, 0, colorizedImage.getWidth(null), colorizedImage.getHeight(null));
		
		graphics.dispose();
		
		return colorizedImage;
	}
	
	/**
	 * Sets a new error message and background image.
	 * 
	 * @param pErrorMessage the error message to set.
	 * @param pBackgroundComponent the {@link IComponent} to use as background,
	 *            can be {@code null} for a plain background.
	 */
	public void setError(String pErrorMessage, IComponent pBackgroundComponent)
	{
		label.setText("<html>" + pErrorMessage.replace("\n", "<br>").replace("\t", "    ").replace(" ", "&nbsp;") + "</html>");
		
		if (pBackgroundComponent == null)
		{
			backgroundIcon.setImage(null);
		}
		else
		{
			IDimension size = pBackgroundComponent.getSize();
			IImage backgroundImage = pBackgroundComponent.capture(size.getWidth(), size.getHeight());
			backgroundImage = new SwingImage(new ImageIcon(colorize(((ImageIcon)backgroundImage.getResource()).getImage(), Tango.ALUMINIUM_6, 0.82f), null));
			backgroundIcon.setImage(backgroundImage);
		}
	}
	
}	// ErrorDisplay
