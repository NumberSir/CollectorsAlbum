package team.tnt.collectorsalbum.service;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import team.tnt.collectorsalbum.util.Side;

public class NeoforgePlatform implements Platform {

    @Override
    public Side getSide() {
        return FMLEnvironment.dist == Dist.CLIENT ? Side.CLIENT : Side.SERVER;
    }
}
