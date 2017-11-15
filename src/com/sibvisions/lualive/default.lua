-- This is a demonstration of a simple Java/Lua bridge which
-- allows to create JVx GUIs directly in Lua.

-- You can edit this code and preview your changes live on
-- the right.


-- Introduction
local sibVisionsLogo = UIImage.getImage("/com/sibvisions/lualive/images/sib-visions.png")
local sibVisionsIcon = UIIcon.new(sibVisionsLogo)

local descriptionLabel = UILabel.new("SIB Visions is the company behind JVx, the Enterprise Application Framework.")
descriptionLabel:setHorizontalAlignment(UILabel.ALIGN_CENTER)

local jvxIntroductionLabel = UILabel.new("JVx allows to quickly and easily create GUIs for the desktop and web.")
jvxIntroductionLabel:setHorizontalAlignment(UILabel.ALIGN_CENTER)


-- Component samples
local button = UIButton.new("Button #1")
button:eventAction():addListener(function()
	local number = tonumber(button:getText():sub(9))
	number = number + 1
	
	button:setText("Button #" .. number)
end)

local toggleButton = UIToggleButton.new("ToggleButton")

local textField = UITextField.new("TextField")

local componentSampleLayout = UIFlowLayout.new(UIFlowLayout.VERTICAL)

local componentSamplePanel = UIGroupPanel.new("Sample components")
componentSamplePanel:setLayout(componentSampleLayout)
componentSamplePanel:setBackground(nil)
componentSamplePanel:add(button)
componentSamplePanel:add(toggleButton)
componentSamplePanel:add(textField)


-- Putting it all together
local layout = UIFormLayout.new()
layout:setHorizontalAlignment(UIFormLayout.ALIGN_CENTER)

local panel = UIPanel.new()
panel:setLayout(layout)
panel:setBackground(UIColor.white)
panel:add(sibVisionsIcon, layout:getConstraints(0, 0))
panel:add(descriptionLabel, layout:getConstraints(0, 1))
panel:add(jvxIntroductionLabel, layout:getConstraints(0, 2))
panel:add(componentSamplePanel, layout:getConstraints(0, -1))

return panel
