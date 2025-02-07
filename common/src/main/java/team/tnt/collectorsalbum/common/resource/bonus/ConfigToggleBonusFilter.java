package team.tnt.collectorsalbum.common.resource.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.ConfigHolder;
import dev.toma.configuration.config.value.IConfigValue;
import dev.toma.configuration.config.value.IConfigValueReadable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import team.tnt.collectorsalbum.common.AlbumBonusDescriptionOutput;
import team.tnt.collectorsalbum.common.CommonLabels;
import team.tnt.collectorsalbum.common.init.AlbumBonusRegistry;
import team.tnt.collectorsalbum.common.resource.util.ActionContext;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ConfigToggleBonusFilter implements IntermediateAlbumBonus {

    private static final String IS_ENABLED = "collectorsalbum.label.bonus.config_toggle.enabled";
    private static final String IS_DISABLED = "collectorsalbum.label.bonus.config_toggle.disabled";
    private static final Component UNKNOWN_CONFIG_OPTION = Component.translatable("collectorsalbum.label.bonus.config_toggle.unknown").withStyle(ChatFormatting.RED);
    public static final MapCodec<ConfigToggleBonusFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Configuration.BY_ID_CODEC.fieldOf("config").forGetter(t -> t.config),
            Codec.STRING.fieldOf("path").forGetter(t -> t.path),
            AlbumBonusType.INSTANCE_CODEC.fieldOf("enabled").forGetter(t -> t.enabled),
            AlbumBonusType.INSTANCE_CODEC.optionalFieldOf("disabled", NoBonus.INSTANCE).forGetter(t -> t.disabled)
    ).apply(instance, ConfigToggleBonusFilter::new));

    private final ConfigHolder<?> config;
    private final String path;
    private final AlbumBonus enabled;
    private final AlbumBonus disabled;

    public ConfigToggleBonusFilter(ConfigHolder<?> config, String path, AlbumBonus enabled, AlbumBonus disabled) {
        this.config = config;
        this.path = path;
        this.enabled = enabled;
        this.disabled = disabled;
    }

    @Override
    public void addDescription(AlbumBonusDescriptionOutput description) {
        Optional<IConfigValue<Boolean>> configValue = this.config.getConfigValue(this.path, Boolean.class);
        Component configLabel = configValue.map(val -> (Component) val.getTitle().copy().withStyle(ChatFormatting.BLUE))
                .orElse(UNKNOWN_CONFIG_OPTION);
        Component configName = Component.translatable("config.screen." + config.getConfigId());
        description.text(configLabel, configName);
        boolean enabled = this.canApply(description.getContext());
        if (this.enabled != NoBonus.INSTANCE) {
            description.nested(() -> {
                description.text(Component.translatable(IS_ENABLED).withStyle(AlbumBonusDescriptionOutput.getBooleanColor(enabled)), CommonLabels.getBoolState(enabled));
                description.nested(() -> this.enabled.addDescription(description));
            });
        }
        if (this.disabled != NoBonus.INSTANCE) {
            description.nested(() -> {
                description.text(Component.translatable(IS_DISABLED).withStyle(AlbumBonusDescriptionOutput.getBooleanColor(!enabled)), CommonLabels.getBoolState(!enabled));
                description.nested(() -> this.disabled.addDescription(description));
            });
        }
    }

    @Override
    public void apply(ActionContext context) {
        if (this.canApply(context)) {
            this.enabled.apply(context);
        } else {
            this.disabled.apply(context);
        }
    }

    @Override
    public void removed(ActionContext context) {
        this.enabled.removed(context);
        this.disabled.removed(context);
    }

    @Override
    public List<AlbumBonus> children() {
        return Arrays.asList(enabled, disabled);
    }

    @Override
    public AlbumBonusType<?> getType() {
        return AlbumBonusRegistry.CONFIG_TOGGLE.get();
    }

    @Override
    public boolean canApply(ActionContext context) {
        return this.config.getConfigValue(this.path, Boolean.class).map(value -> value.get(IConfigValueReadable.Mode.SAVED))
                .orElse(false);
    }
}
