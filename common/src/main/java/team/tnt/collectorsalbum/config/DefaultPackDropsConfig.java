package team.tnt.collectorsalbum.config;

import dev.toma.configuration.config.Configurable;
import team.tnt.collectorsalbum.common.item.CardPackItem;

public final class DefaultPackDropsConfig {

    @Configurable
    @Configurable.Range(min = 1, max = CardPackItem.MAX_PACK_CARDS)
    @Configurable.Comment(value = "Defines how many cards will be dropped from normal card packs", localize = true)
    public int packDropsCount = 5;

    @Configurable
    @Configurable.Range(min = 1, max = CardPackItem.MAX_PACK_CARDS)
    @Configurable.Comment(value = "Defines how many cards will be dropped from repacked card packs", localize = true)
    public int repackedDropsCount = 3;

    @Configurable
    @Configurable.Comment(value = "Defines card rarity drop distributions in common packs", localize = true)
    public PackDropConfig commonDrops = new PackDropConfig(90, 25, 8, 5, 2, 0);

    @Configurable
    @Configurable.Comment(value = "Defines card rarity drop distributions in uncommon packs", localize = true)
    public PackDropConfig uncommonDrops = new PackDropConfig(60, 40, 10, 8, 4, 2);

    @Configurable
    @Configurable.Comment(value = "Defines card rarity drop distributions in rare packs", localize = true)
    public PackDropConfig rareDrops = new PackDropConfig(35, 50, 35, 15, 7, 4);

    @Configurable
    @Configurable.Comment(value = "Defines card rarity drop distributions in epic packs", localize = true)
    public PackDropConfig epicDrops = new PackDropConfig(15, 35, 40, 25, 10, 6);

    @Configurable
    @Configurable.Comment(value = "Defines card rarity drop distributions in legendary packs", localize = true)
    public PackDropConfig legendaryDrops = new PackDropConfig(0, 25, 45, 35, 15, 10);

    @Configurable
    @Configurable.Comment(value = "Defines card rarity drop distributions in mythical packs", localize = true)
    public PackDropConfig mythicalDrops = new PackDropConfig(0, 0, 50, 40, 30, 20);
}
