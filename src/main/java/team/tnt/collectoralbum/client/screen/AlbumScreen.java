package team.tnt.collectoralbum.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;
import team.tnt.collectoralbum.CollectorsAlbum;
import team.tnt.collectoralbum.common.AlbumStats;
import team.tnt.collectoralbum.common.item.CardCategory;
import team.tnt.collectoralbum.common.item.CardRarity;
import team.tnt.collectoralbum.common.menu.AlbumMenu;
import team.tnt.collectoralbum.network.Networking;
import team.tnt.collectoralbum.network.packet.RequestAlbumPagePacket;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AlbumScreen extends AbstractContainerScreen<AlbumMenu> {

    private static final ResourceLocation TITLE = new ResourceLocation(CollectorsAlbum.MODID, "textures/screen/album_title.png");
    private static final ResourceLocation BACKGROUND = new ResourceLocation(CollectorsAlbum.MODID, "textures/screen/album.png");
    private static final ResourceLocation ARROW_LEFT = new ResourceLocation(CollectorsAlbum.MODID, "textures/screen/album_previous.png");
    private static final ResourceLocation ARROW_RIGHT = new ResourceLocation(CollectorsAlbum.MODID, "textures/screen/album_next.png");

    // Localizations
    private static final MutableComponent TEXT_HEADER = new TranslatableComponent("text.collectorsalbum.album.header").withStyle(ChatFormatting.BOLD);
    private static final MutableComponent TEXT_CATEGORIES = new TranslatableComponent("text.collectorsalbum.album.categories").withStyle(ChatFormatting.UNDERLINE);
    private static final MutableComponent TEXT_RARITIES = new TranslatableComponent("text.collectorsalbum.album.rarities").withStyle(ChatFormatting.UNDERLINE);
    private static final Function<Integer, TranslatableComponent> TEXT_POINTS = points -> new TranslatableComponent("text.collectorsalbum.album.points", points);
    private static final BiFunction<Integer, Integer, TranslatableComponent> TEXT_TOTAL_CARDS = (cards, total) -> new TranslatableComponent("text.collectorsalbum.album.total_cards", cards, total);

    private final int pageIndex;
    private AlbumStats stats;

    public AlbumScreen(AlbumMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
        this.imageWidth = 306;
        this.imageHeight = 257;
        this.pageIndex = menu.isTitle() ? 0 : menu.getCategoryIndex();
    }

    @Override
    protected void init() {
        super.init();
        if (pageIndex > 0) {
            ArrowWidget widget = addRenderableWidget(new ArrowWidget(leftPos + 18, topPos + 5, 16, 16, ARROW_LEFT));
            widget.setOnClickResponder(this::clickPrevPage);
        }
        if (pageIndex < CardCategory.values().length) {
            ArrowWidget widget = addRenderableWidget(new ArrowWidget(leftPos + 265, topPos + 4, 16, 16, ARROW_RIGHT));
            widget.setOnClickResponder(this::clickNextPage);
        }
        this.stats = menu.getContainer().getStats();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_D || keyCode == GLFW.GLFW_KEY_RIGHT) {
            changePage(1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_A || keyCode == GLFW.GLFW_KEY_LEFT) {
            changePage(-1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, this.menu.isTitle() ? TITLE : BACKGROUND);
        Matrix4f pose = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(pose, leftPos, topPos, 0).uv(0.0F, 0.0F).endVertex();
        bufferBuilder.vertex(pose, leftPos, topPos + imageHeight, 0).uv(0.0F, 1.0F).endVertex();
        bufferBuilder.vertex(pose, leftPos + imageWidth, topPos + imageHeight, 0).uv(1.0F, 1.0F).endVertex();
        bufferBuilder.vertex(pose, leftPos + imageWidth, topPos, 0).uv(1.0F, 0.0F).endVertex();
        tesselator.end();
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        if (this.menu.isTitle()) {
            // left page
            // header
            int headerWidth = font.width(TEXT_HEADER);
            font.draw(poseStack, TEXT_HEADER, 20 + (130 - headerWidth) / 2.0F, 13, 0x7C5D4D);
            // rarity pcts
            font.draw(poseStack, TEXT_RARITIES, 27, 55, 0x7C5D4D);
            int i = 0;
            Map<CardRarity, Integer> byRarity = stats.getCardsByRarity();
            for (CardRarity rarity : CardRarity.values()) {
                int value = byRarity.getOrDefault(rarity, 0);
                String name = rarity.name();
                String pct = Math.round(value / (float) stats.getCardsCollected() * 100) + "%";
                String text = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase() + ": " + pct;
                font.draw(poseStack, text, 30, 67 + i++ * 10, 0x7C5D4D);
            }
            // total cards
            int collected = stats.getCardsCollected();
            int total = stats.getTotalCards();
            font.draw(poseStack, TEXT_TOTAL_CARDS.apply(collected, total), 27, 35, 0x7C5D4D);
            // points
            int points = stats.getPoints();
            font.draw(poseStack, TEXT_POINTS.apply(points), 27, 80 + i * 10, 0x7C5D4D);

            // right page
            font.draw(poseStack, TEXT_CATEGORIES, 164, 35, 0x7C5D4D);
            int j = 0;
            Map<CardCategory, Integer> map = stats.getCardsByCategory();
            for (CardCategory category : CardCategory.values()) {
                int value = map.getOrDefault(category, 0);
                String categoryText = category.name();
                String count = value + " / 30";
                String text = categoryText.substring(0, 1).toUpperCase() + categoryText.substring(1).toLowerCase() + " - " + count;
                font.draw(poseStack, text, 167, 47 + j++ * 10, 0x7C5D4D);
            }
            return;
        }
        for (Slot slot : this.menu.slots) {
            if (slot instanceof AlbumMenu.CardSlot cardSlot) {
                int cardNumber = cardSlot.getCardNumber();
                String text = "#" + cardNumber;
                font.draw(poseStack, text, slot.x + (18 - font.width(text)) / 2.0F - 1, slot.y + 18, 0x7C5D4D);
            }
        }
        CardCategory category = menu.getCategory();
        MutableComponent component = new TextComponent(category.name()).withStyle(ChatFormatting.ITALIC);
        font.draw(poseStack, component, 40, 10, 0x7C5D4D);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    private void clickPrevPage(ArrowWidget widget) {
        changePage(-1);
    }

    private void clickNextPage(ArrowWidget widget) {
        changePage(1);
    }

    private void changePage(int indexOffset) {
        int nextIndex = this.pageIndex + indexOffset;
        if (nextIndex < 0 || nextIndex > CardCategory.values().length) return;
        CardCategory category = nextIndex == 0 ? null : CardCategory.values()[nextIndex - 1];
        Networking.dispatchServerPacket(new RequestAlbumPagePacket(category));
    }

    private static final class ArrowWidget extends AbstractWidget {

        private final ResourceLocation location;
        private ClickResponder clickResponder = widget -> {
        };

        public ArrowWidget(int x, int y, int width, int height, ResourceLocation location) {
            super(x, y, width, height, TextComponent.EMPTY);
            this.location = location;
        }

        public void setOnClickResponder(ClickResponder responder) {
            this.clickResponder = responder;
        }

        @Override
        public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, location);
            Matrix4f pose = poseStack.last().pose();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder.vertex(pose, x, y, 0).uv(0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(pose, x, y + width, 0).uv(0.0F, 1.0F).endVertex();
            bufferBuilder.vertex(pose, x + height, y + width, 0).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(pose, x + height, y, 0).uv(1.0F, 0.0F).endVertex();
            tesselator.end();
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            clickResponder.onClick(this);
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {
        }

        @FunctionalInterface
        interface ClickResponder {
            void onClick(ArrowWidget widget);
        }
    }
}
