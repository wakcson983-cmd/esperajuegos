package com.esperajuegos.client;

import com.esperajuegos.EsperaJuegos;
import com.esperajuegos.client.games.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Pantalla "Esperando jugadores": chat a la izquierda, menú/minijuegos a la derecha, contador abajo. */
public class WaitingScreen extends Screen {

    private static final int GREEN = 0xFF22B14C, DARK = 0xFF07180D, BROWN = 0xFFB97A57, RED = 0xFF8B0C1B;
    private static final int[] LIGHT_COLORS = {0xFFFF3B3B, 0xFFFFE04A, 0xFF3B9CFF, 0xFFFFFFFF};
    private static final String[] LABELS = {"Serpiente", "Minas", "Regalos", "Laberinto", "2048", "Pong", "Luces", "Rudolph"};
    private static final String[] TEX = {"snake", "mines", "gifts", "maze", "g2048", "pong", "lights", "rudolph"};
    private static final Identifier TREE = Identifier.of(EsperaJuegos.MOD_ID, "textures/gui/tree.png");
    private static final Identifier GIFT = Identifier.of(EsperaJuegos.MOD_ID, "textures/gui/gift.png");

    private TextFieldWidget chatBox;
    private MiniGame game; // null = menú principal

    private final float[] snowX = new float[90], snowY = new float[90], snowS = new float[90];
    private long lastMs = 0;

    // diseño
    private int chatX, chatY, chatW, chatH;
    private int panX, panY, panW, panH;
    private int barX, barY, barW, barH;
    private int gridX, gridY, tile, gapX, gapY;

    public WaitingScreen() {
        super(Text.literal("Sala de espera"));
        Random r = new Random();
        for (int i = 0; i < snowX.length; i++) {
            snowX[i] = r.nextFloat();
            snowY[i] = r.nextFloat();
            snowS[i] = 0.4f + r.nextFloat() * 0.9f;
        }
    }

    // ------------------------------------------------------------------ diseño

    private void layout() {
        chatX = (int) (width * 0.037);
        chatY = (int) (height * 0.20);
        chatW = (int) (width * 0.357);
        chatH = (int) (height * 0.755);
        panX = (int) (width * 0.403);
        panY = (int) (height * 0.095);
        panW = (int) (width * 0.577);
        panH = (int) (height * 0.622);
        barX = (int) (width * 0.514);
        barY = (int) (height * 0.748);
        barW = (int) (width * 0.395);
        barH = (int) (height * 0.122);

        gapX = Math.max(3, (int) (panW * 0.026));
        gapY = Math.max(3, (int) (panH * 0.08));
        int padX = (int) (panW * 0.061);
        int byWidth = (panW - 2 * padX - 3 * gapX) / 4;
        int byHeight = (int) ((panH * 0.80 - gapY) / 2);
        tile = Math.max(10, Math.min(byWidth, byHeight));
        int gridW = 4 * tile + 3 * gapX;
        gridX = panX + (panW - gridW) / 2;
        gridY = panY + (int) (panH * 0.06);
    }

    private int tileX(int i) { return gridX + (i % 4) * (tile + gapX); }
    private int tileY(int i) { return gridY + (i / 4) * (tile + gapY); }

    private int tileAt(double mx, double my) {
        for (int i = 0; i < 8; i++) {
            int x = tileX(i), y = tileY(i);
            if (mx >= x && mx < x + tile && my >= y && my < y + tile) return i;
        }
        return -1;
    }

    // game rect: gx, gy, gw, gh
    private int gameX() { return panX + 4; }
    private int gameY() { return panY + 22; }
    private int gameW() { return panW - 8; }
    private int gameH() { return panH - 22 - 14; }

    private int backW() { return textRenderer.getWidth("Volver al Menú") + 10; }
    private int backX() { return panX + panW - 6 - backW(); }
    private int backY() { return panY + 5; }

    // ------------------------------------------------------------------ init

    @Override
    protected void init() {
        this.clearChildren();
        layout();
        String old = chatBox != null ? chatBox.getText() : "";
        chatBox = new TextFieldWidget(this.textRenderer, chatX + 4, chatY + chatH - 18, chatW - 8, 14, Text.literal("Chat"));
        chatBox.setMaxLength(256);
        chatBox.setText(old);
        this.addDrawableChild(chatBox);
    }

    @Override public boolean shouldPause() { return false; }
    @Override public boolean shouldCloseOnEsc() { return false; }

    // ------------------------------------------------------------------ dibujo

    private static void rrect(DrawContext c, int x, int y, int w, int h, int color) {
        c.fill(x + 2, y, x + w - 2, y + h, color);
        c.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
        c.fill(x, y + 2, x + w, y + h - 2, color);
    }

    private void panel(DrawContext c, int x, int y, int w, int h) {
        c.fill(x, y, x + w, y + h, BROWN);
        c.fill(x + 2, y + 2, x + w - 2, y + h - 2, DARK);
    }

    private void scaled(DrawContext c, String s, float x, float y, float k, int color, boolean center, boolean shadow) {
        float w = textRenderer.getWidth(s) * k;
        MatrixStack m = c.getMatrices();
        m.push();
        m.translate(center ? x - w / 2f : x, y, 0f);
        m.scale(k, k, 1f);
        c.drawText(textRenderer, s, 0, 0, color, shadow);
        m.pop();
    }

    private void drawSnow(DrawContext c) {
        long now = Util.getMeasuringTimeMs();
        float dt = Math.min(50L, now - lastMs) / 16f;
        lastMs = now;
        for (int i = 0; i < snowX.length; i++) {
            snowY[i] += snowS[i] * dt * 0.0016f;
            snowX[i] += (float) Math.sin(now / 900.0 + i) * 0.00015f * dt;
            if (snowX[i] < 0) snowX[i] += 1f;
            if (snowX[i] > 1) snowX[i] -= 1f;
            if (snowY[i] > 1f) {
                snowY[i] = 0f;
            }
            int px = (int) (snowX[i] * width), py = (int) (snowY[i] * height);
            int sz = snowS[i] > 0.9f ? 3 : 2;
            c.fill(px, py, px + sz, py + sz, 0xDDFFFFFF);
        }
    }

    private void drawLights(DrawContext c) {
        c.fill(0, 2, width, 3, 0xFF0B3D1E);
        long t = Util.getMeasuringTimeMs() / 450;
        int i = 0;
        for (int x = 4; x < width; x += 14, i++) {
            int col = LIGHT_COLORS[(int) ((i + t) % 4)];
            int y = 3 + (i % 2 == 0 ? 0 : 3);
            c.fill(x, y, x + 4, y + 5, col);
        }
    }

    @Override
    public void renderBackground(DrawContext c, int mouseX, int mouseY, float delta) {
        c.fill(0, 0, width, height, GREEN);
        drawSnow(c);
        drawLights(c);

        // Título CHAT
        scaled(c, "CHAT", width * 0.035f, height * 0.045f, Math.max(2f, height / 765f * 13f), 0xFFFFFFFF, false, true);

        // ---------------- chat
        panel(c, chatX, chatY, chatW, chatH);
        int innerW = chatW - 12;
        int bottom = chatY + chatH - 22;
        int maxLines = Math.max(1, (bottom - (chatY + 5)) / 10);
        List<OrderedText> lines = new ArrayList<>();
        List<Text> msgs = ChatLog.MESSAGES;
        for (int i = msgs.size() - 1; i >= 0 && lines.size() < maxLines; i--) {
            List<OrderedText> wrapped = textRenderer.wrapLines(msgs.get(i), innerW);
            for (int j = wrapped.size() - 1; j >= 0 && lines.size() < maxLines; j--) {
                lines.add(wrapped.get(j));
            }
        }
        int ly = bottom - 10;
        for (OrderedText line : lines) {
            c.drawText(textRenderer, line, chatX + 6, ly, 0xFFFFFFFF, false);
            ly -= 10;
        }

        // ---------------- panel de juegos
        panel(c, panX, panY, panW, panH);
        if (game == null) {
            int hover = tileAt(mouseX, mouseY);
            for (int i = 0; i < 8; i++) {
                int x = tileX(i), y = tileY(i);
                rrect(c, x, y, tile, tile, RED);
                rrect(c, x + 2, y + 2, tile - 4, tile - 4, i == hover ? 0xFFFFF3C4 : 0xFFFFFFFF);
                int icon = Math.max(8, tile - 10 - 10);
                Identifier tex = Identifier.of(EsperaJuegos.MOD_ID, "textures/gui/" + TEX[i] + ".png");
                c.drawTexture(tex, x + (tile - icon) / 2, y + 5, icon, icon, 0f, 0f, 64, 64, 64, 64);
                float k = Math.min(1f, (tile - 6f) / Math.max(1, textRenderer.getWidth(LABELS[i])));
                scaled(c, LABELS[i], x + tile / 2f, y + tile - 11, k, 0xFF8B0C1B, true, false);
            }
            String hint = "Haz click en un juego para jugar";
            c.drawText(textRenderer, hint, panX + (panW - textRenderer.getWidth(hint)) / 2, panY + panH - 14, 0xFFB7E4C7, false);
        } else {
            // cabecera
            c.fill(panX + 4, panY + 4, panX + panW - 4, panY + 20, 0xFF0D2A16);
            c.drawText(textRenderer, game.name(), panX + 8, panY + 9, 0xFFFFD84A, false);
            String sc = "Puntos: " + game.score;
            c.drawText(textRenderer, sc, panX + (panW - textRenderer.getWidth(sc)) / 2, panY + 9, 0xFFFFFFFF, false);
            boolean overBack = mouseX >= backX() && mouseX < backX() + backW() && mouseY >= backY() && mouseY < backY() + 14;
            c.fill(backX(), backY(), backX() + backW(), backY() + 14, overBack ? 0xFFC0182E : RED);
            c.drawText(textRenderer, "Volver al Menú", backX() + 5, backY() + 3, 0xFFFFFFFF, false);
            // juego
            game.render(c, textRenderer, gameX(), gameY(), gameW(), gameH());
            // ayuda
            String help = game.help();
            c.drawText(textRenderer, help, panX + (panW - textRenderer.getWidth(help)) / 2, panY + panH - 12, 0xFFB7E4C7, false);
        }

        // ---------------- barra de jugadores
        rrect(c, barX, barY, barW, barH, 0xFFFFFFFF);
        rrect(c, barX + 1, barY + 1, barW - 2, barH - 2, 0xFF000000);
        int dots = (int) ((Util.getMeasuringTimeMs() / 400) % 4);
        String waiting = "Esperando jugadores" + ".".repeat(dots);
        float k1 = Math.max(1f, barH / 30f);
        float fullW = textRenderer.getWidth("Esperando jugadores...") * k1;
        scaled(c, waiting, barX + (barW - fullW) / 2f, barY + barH * 0.14f, k1, 0xFFFFFFFF, false, true);
        int count = EsperaJuegosClient.count;
        String cnt = "Jugadores: " + count + " / " + EsperaJuegos.MAX_PLAYERS;
        scaled(c, cnt, barX + barW / 2f, barY + barH * 0.52f, Math.max(1f, k1 * 0.8f), 0xFFFFD84A, true, true);
        int pbx = barX + 8, pbw = barW - 16, pby = barY + barH - 8;
        c.fill(pbx, pby, pbx + pbw, pby + 4, 0xFF333333);
        c.fill(pbx, pby, pbx + (int) (pbw * Math.min(1f, count / (float) EsperaJuegos.MAX_PLAYERS)), pby + 4, 0xFF2ECC40);

        // ---------------- adornos
        int rightW = width - (barX + barW);
        int treeSize = Math.max(16, Math.min(rightW - 6, (int) (height * 0.2)));
        c.drawTexture(TREE, barX + barW + (rightW - treeSize) / 2, height - treeSize - 3, treeSize, treeSize, 0f, 0f, 32, 32, 32, 32);
        int midW = barX - (chatX + chatW);
        int giftSize = Math.max(14, Math.min(midW - 8, (int) (height * 0.14)));
        c.drawTexture(GIFT, chatX + chatW + (midW - giftSize) / 2, barY + (barH - giftSize) / 2, giftSize, giftSize, 0f, 0f, 32, 32, 32, 32);
        String merry = "¡Feliz Navidad!";
        scaled(c, merry, barX + barW / 2f, barY + barH + (height - barY - barH) / 2f - 4, Math.max(1f, height / 480f * 1.4f), 0xFFFFD84A, true, true);
    }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        super.render(c, mouseX, mouseY, delta);
        if (chatBox.getText().isEmpty() && !chatBox.isFocused()) {
            c.drawText(textRenderer, "Click o Enter para escribir...", chatBox.getX() + 4, chatBox.getY() + 3, 0xFF707070, false);
        }
    }

    // ------------------------------------------------------------------ entrada

    private MiniGame create(int i) {
        return switch (i) {
            case 0 -> new SnakeGame();
            case 1 -> new MinesGame();
            case 2 -> new CatchGame();
            case 3 -> new MazeGame();
            case 4 -> new Game2048();
            case 5 -> new PongGame();
            case 6 -> new LightsGame();
            default -> new RudolphGame();
        };
    }

    private static int map(int key) {
        return switch (key) {
            case GLFW.GLFW_KEY_UP -> 'W';
            case GLFW.GLFW_KEY_DOWN -> 'S';
            case GLFW.GLFW_KEY_LEFT -> 'A';
            case GLFW.GLFW_KEY_RIGHT -> 'D';
            default -> key;
        };
    }

    private void sendChat() {
        String m = chatBox.getText().trim();
        if (!m.isEmpty() && client != null && client.getNetworkHandler() != null) {
            if (m.startsWith("/")) {
                client.getNetworkHandler().sendChatCommand(m.substring(1));
            } else {
                client.getNetworkHandler().sendChatMessage(m);
            }
        }
        chatBox.setText("");
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) {
            return true;
        }
        if (game == null) {
            if (button == 0) {
                int i = tileAt(mx, my);
                if (i >= 0) {
                    game = create(i);
                    return true;
                }
            }
        } else {
            if (button == 0 && mx >= backX() && mx < backX() + backW() && my >= backY() && my < backY() + 14) {
                game.clearKeys();
                game = null;
                return true;
            }
            if (game.click(mx, my, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (chatBox.isFocused()) {
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                sendChat();
                this.setFocused(null);
                return true;
            }
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                this.setFocused(null);
                return true;
            }
            return chatBox.keyPressed(key, scancode, modifiers);
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            this.setFocused(chatBox);
            return true;
        }
        if (game != null) {
            game.press(map(key));
            return true;
        }
        return true;
    }

    @Override
    public boolean keyReleased(int key, int scancode, int modifiers) {
        if (game != null) {
            game.release(map(key));
        }
        return true;
    }

    @Override
    public void removed() {
        if (game != null) {
            game.clearKeys();
        }
    }
}
