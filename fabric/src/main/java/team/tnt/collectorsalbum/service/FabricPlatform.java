package team.tnt.collectorsalbum.service;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import team.tnt.collectorsalbum.platform.Side;
import team.tnt.collectorsalbum.platform.Platform;

public class FabricPlatform implements Platform {

    @Override
    public Side getSide() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Side.CLIENT : Side.SERVER;
    }
}
