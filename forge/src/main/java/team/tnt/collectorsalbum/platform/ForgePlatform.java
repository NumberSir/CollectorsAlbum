package team.tnt.collectorsalbum.platform;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;
import team.tnt.collectorsalbum.client.screen.AlbumNavigationHelper;

public class ForgePlatform implements Platform {

    @Override
    public Side getSide() {
        return FMLEnvironment.dist == Dist.CLIENT ? Side.CLIENT : Side.SERVER;
    }

    @Override
    public boolean isModLoaded(String namespace) {
        return ModList.get().isLoaded(namespace);
    }

    @Override
    public MinecraftServer getServerInstance() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public <T> void openMenu(ServerPlayer player, StreamCodec<? super FriendlyByteBuf, T> codec, PlatformMenuProvider<T> provider) {
        player.openMenu(new SimpleMenuProvider(
                provider::createMenu,
                provider.getTitle()
        ), buf -> {
            T data = provider.getMenuData(player);
            codec.encode(buf, data);
        });
    }

    @Override
    public <M extends AbstractContainerMenu, D> MenuType<M> createMenu(MenuFactory<M, D> factory, StreamCodec<? super FriendlyByteBuf, D> dataCodec) {
        return IForgeMenuType.create((id, inv, buf) -> {
            D data = dataCodec.decode(buf);
            return factory.createMenu(id, inv, data);
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void openAlbumUi(ItemStack itemStack) {
        AlbumNavigationHelper.storeItemStack(itemStack);
        AlbumNavigationHelper.navigateHomepage();
    }
}
