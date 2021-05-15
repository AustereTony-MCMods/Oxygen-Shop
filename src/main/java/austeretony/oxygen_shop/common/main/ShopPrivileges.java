package austeretony.oxygen_shop.common.main;

import austeretony.oxygen_core.common.privileges.PrivilegeRegistry;
import austeretony.oxygen_core.common.util.value.ValueType;

public final class ShopPrivileges {

    public static final PrivilegeRegistry.Entry
            SHOP_ACCESS = PrivilegeRegistry.register(900, "shop:shop_access", ValueType.BOOLEAN);

    private ShopPrivileges() {}

    public static void register() {}
}
