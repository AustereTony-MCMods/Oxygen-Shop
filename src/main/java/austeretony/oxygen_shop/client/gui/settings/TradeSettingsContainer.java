package austeretony.oxygen_shop.client.gui.settings;

import austeretony.alternateui.screen.framework.GUIElementsFramework;
import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.gui.elements.OxygenCheckBoxButton;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList.OxygenDropDownListEntry;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.client.gui.settings.ElementsContainer;
import austeretony.oxygen_core.client.gui.settings.gui.callback.SetColorCallback;
import austeretony.oxygen_core.client.gui.settings.gui.callback.SetKeyCallback;
import austeretony.oxygen_core.client.gui.settings.gui.callback.SetOffsetCallback;
import austeretony.oxygen_core.client.gui.settings.gui.callback.SetScaleCallback;
import austeretony.oxygen_shop.client.settings.EnumShopClientSetting;
import austeretony.oxygen_shop.client.settings.gui.EnumShopGUISetting;

public class TradeSettingsContainer implements ElementsContainer {

    //common

    private OxygenCheckBoxButton addShopMenuButton;

    //interface

    private OxygenDropDownList alignmentShopMenu;


    @Override
    public String getLocalizedName() {
        return ClientReference.localize("oxygen_shop.gui.settings.module.shop");
    }

    @Override
    public boolean hasCommonSettings() {
        return true;
    }

    @Override
    public boolean hasGUISettings() {
        return true;
    }

    @Override
    public void addCommon(GUIElementsFramework framework) {
        framework.addElement(new OxygenTextLabel(68, 25, ClientReference.localize("oxygen_core.gui.settings.option.oxygenMenu"), EnumBaseGUISetting.TEXT_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        //add shop menu to menu
        framework.addElement(new OxygenTextLabel(78, 34, ClientReference.localize("oxygen_shop.gui.settings.option.addShopMenu"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.1F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));

        framework.addElement(this.addShopMenuButton = new OxygenCheckBoxButton(68, 29));
        this.addShopMenuButton.setToggled(EnumShopClientSetting.ADD_SHOP_MENU.get().asBoolean());
        this.addShopMenuButton.setClickListener((mouseX, mouseY, mouseButton)->{
            EnumShopClientSetting.ADD_SHOP_MENU.get().setValue(String.valueOf(this.addShopMenuButton.isToggled()));
            OxygenManagerClient.instance().getClientSettingManager().changed();
        });
    }

    @Override
    public void addGUI(GUIElementsFramework framework) {
        framework.addElement(new OxygenTextLabel(68, 25, ClientReference.localize("oxygen_core.gui.settings.option.alignment"), EnumBaseGUISetting.TEXT_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        //shop menu alignment

        String currAlignmentStr;
        switch (EnumShopGUISetting.SHOP_MENU_ALIGNMENT.get().asInt()) {
        case - 1: 
            currAlignmentStr = ClientReference.localize("oxygen_core.alignment.left");
            break;
        case 0:
            currAlignmentStr = ClientReference.localize("oxygen_core.alignment.center");
            break;
        case 1:
            currAlignmentStr = ClientReference.localize("oxygen_core.alignment.right");
            break;    
        default:
            currAlignmentStr = ClientReference.localize("oxygen_core.alignment.center");
            break;
        }
        framework.addElement(this.alignmentShopMenu = new OxygenDropDownList(68, 35, 55, currAlignmentStr));
        this.alignmentShopMenu.addElement(new OxygenDropDownListEntry<Integer>(- 1, ClientReference.localize("oxygen_core.alignment.left")));
        this.alignmentShopMenu.addElement(new OxygenDropDownListEntry<Integer>(0, ClientReference.localize("oxygen_core.alignment.center")));
        this.alignmentShopMenu.addElement(new OxygenDropDownListEntry<Integer>(1, ClientReference.localize("oxygen_core.alignment.right")));

        this.alignmentShopMenu.<OxygenDropDownListEntry<Integer>>setClickListener((element)->{
            EnumShopGUISetting.SHOP_MENU_ALIGNMENT.get().setValue(String.valueOf(element.index));
            OxygenManagerClient.instance().getClientSettingManager().changed();
        });

        framework.addElement(new OxygenTextLabel(68, 33, ClientReference.localize("oxygen_shop.gui.settings.option.alignmentShopMenu"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.1F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
    }

    @Override
    public void resetCommon() {
        //add shop menu to menu
        this.addShopMenuButton.setToggled(false);
        EnumShopClientSetting.ADD_SHOP_MENU.get().reset();    

        OxygenManagerClient.instance().getClientSettingManager().changed();
    }

    @Override
    public void resetGUI() {
        //shop menu alignment
        this.alignmentShopMenu.setDisplayText(ClientReference.localize("oxygen_core.alignment.center"));
        EnumShopGUISetting.SHOP_MENU_ALIGNMENT.get().reset();

        OxygenManagerClient.instance().getClientSettingManager().changed();
    }

    @Override
    public void initSetColorCallback(SetColorCallback callback) {}

    @Override
    public void initSetScaleCallback(SetScaleCallback callback) {}

    @Override
    public void initSetOffsetCallback(SetOffsetCallback callback) {}

    @Override
    public void initSetKeyCallback(SetKeyCallback callback) {}
}
