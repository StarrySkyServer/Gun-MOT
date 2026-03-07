package cn.cookiestudio.gun.guns;

import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;

public abstract class ItemMagBase extends ItemCustom {
    public ItemMagBase(String name) {
        super("gun:" + name, name, name);
    }

    public abstract int getSkinId();

    public abstract float getDropItemScale();

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition
			.simpleBuilder(this, CreativeItemCategory.EQUIPMENT)
                .creativeGroup("itemGroup.name.ammo")
                .allowOffHand(true)
                .build();
    }
}
