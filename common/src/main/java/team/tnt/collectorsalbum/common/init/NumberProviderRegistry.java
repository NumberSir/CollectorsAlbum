package team.tnt.collectorsalbum.common.init;

import team.tnt.collectorsalbum.CollectorsAlbum;
import team.tnt.collectorsalbum.common.CollectorsAlbumRegistries;
import team.tnt.collectorsalbum.common.resource.function.*;
import team.tnt.collectorsalbum.platform.registration.PlatformRegistry;

import java.util.function.Supplier;

public final class NumberProviderRegistry {

    public static final PlatformRegistry<NumberProviderType<?>> REGISTRY = PlatformRegistry.create(CollectorsAlbumRegistries.NUMBER_PROVIDER, CollectorsAlbum.MOD_ID);

    public static final Supplier<NumberProviderType<ConstantNumberProvider>> CONSTANT = REGISTRY.register("constant", () -> new NumberProviderType<>(ConstantNumberProvider.CODEC));
    public static final Supplier<NumberProviderType<RandomNumberProvider>> RANDOM = REGISTRY.register("random", () -> new NumberProviderType<>(RandomNumberProvider.CODEC));
    public static final Supplier<NumberProviderType<ConfigValueIntProvider>> CONFIG_INT = REGISTRY.register("config_int", () -> new NumberProviderType<>(ConfigValueIntProvider.CODEC));
    public static final Supplier<NumberProviderType<ConfigValueFloatProvider>> CONFIG_PERCENT = REGISTRY.register("config_percent", () -> new NumberProviderType<>(ConfigValueFloatProvider.CODEC));
    public static final Supplier<NumberProviderType<SumNumberProvider>> SUM = REGISTRY.register("sum", () -> new NumberProviderType<>(SumNumberProvider.CODEC));
}
