package austeretony.oxygen_shop.common.main;

import austeretony.oxygen_core.common.EnumValueType;
import austeretony.oxygen_core.common.privilege.PrivilegeRegistry;

public enum EnumShopPrivilege {

    SHOP_ACCESS("shop:shopAccess", 1500, EnumValueType.BOOLEAN),   
    SHOP_MANAGEMENT("shop:shopManagement", 1501, EnumValueType.BOOLEAN);

    private final String name;

    private final int id;

    private final EnumValueType type;

    EnumShopPrivilege(String name, int id, EnumValueType type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public int id() {
        return id;
    }

    public static void register() {
        for (EnumShopPrivilege privilege : EnumShopPrivilege.values())
            PrivilegeRegistry.registerPrivilege(privilege.name, privilege.id, privilege.type);
    }
}
