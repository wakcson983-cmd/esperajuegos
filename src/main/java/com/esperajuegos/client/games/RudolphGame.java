package com.esperajuegos.client.games;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Vuelo de Rudolph: pasa entre las chimeneas. W o ESPACIO para aletear. */
public class RudolphGame extends MiniGame {
    private static final float BX = 160, PW = 70, GAP = 160, GROUND = 466;

    private final List<float[]> pipes = new ArrayList<>(); // x, altura del hueco, ya puntuada
    private float by, vy;
    private boolean started;
    private int tick;

    public RudolphGame() {
        reset();
    }

    @Override public String name() { return "Vuelo de Rudolph"; }
    @Override public String help() { return "W o ESPACIO: aletear   R: reiniciar"; }
    @Override protected int periodMs() { return 16; }
    @Override protected String endTitle() { return "¡Chocaste!"; }

    @Override
    protected void reset() {
        pipes.clear();
        by = 220;
        vy = 0;
        started = false;
        tick = 0;
        score = 0;
        over = false;
    }

    @Override
    protected void step() {
        tick++;
        if (!started) {
            by = 220 + (float) Math.sin(tick / 12.0) * 8f;
            return;
        }
        vy = Math.min(vy + 0.30f, 9f);
        by += vy;
        if (by < 0) {
            by = 0;
            vy = 0;
        }
        if (by + 28 >= GROUND) {
            over = true;
            return;
        }

        if (pipes.isEmpty() || pipes.get(pipes.size() - 1)[0] < LW - 270) {
            pipes.add(new float[]{LW + 20, 60 + rnd.nextInt(201), 0});
        }
        Iterator<float[]> it = pipes.iterator();
        while (it.hasNext()) {
            float[] p = it.next();
            p[0] -= 2.8f;
            if (p[2] == 0 && p[0] + PW < BX) {
                p[2] = 1;
                score++;
            }
            if (p[0] + PW < -10) {
                it.remove();
                continue;
            }
            if (overlap(BX + 4, by + 4, 28, 20, p[0], 0, PW, p[1])
                    || overlap(BX + 4, by + 4, 28, 20, p[0], p[1] + GAP, PW, LH - (p[1] + GAP))) {
                over = true;
            }
        }
    }

    @Override
    protected void onKey(int key, boolean fresh) {
        if ((key == 'W' || key == ' ') && fresh) {
            started = true;
            vy = -6.4f;
        }
    }

    private void brick(DrawContext c, float x, float y, float w, float h) {
        R(c, x, y, w, h, 0xFF9C2B2B);
        for (float yy = y; yy < y + h; yy += 20) {
            R(c, x, yy, w, 2, 0xFF6E1B1B);
        }
    }

    @Override
    protected void draw(DrawContext c, TextRenderer tr) {
        R(c, 0, 0, LW, LH, 0xFF0B1B3A);
        for (int i = 0; i < 36; i++) {
            R(c, (i * 137) % 790, (i * 83) % 400, 3, 3, 0x88FFFFFF);
        }
        for (float[] p : pipes) {
            float x = p[0], gap = p[1];
            brick(c, x, 0, PW, gap);
            R(c, x - 5, gap - 16, PW + 10, 16, 0xFFEAF4FB);
            brick(c, x, gap + GAP, PW, LH - (gap + GAP));
            R(c, x - 5, gap + GAP, PW + 10, 16, 0xFFEAF4FB);
        }
        R(c, 0, GROUND, LW, LH - GROUND, 0xFFEAF4FB);

        // Rudolph
        float x = BX, y = by;
        R(c, x + 4, y + 20, 5, 8, 0xFF6B4320);
        R(c, x + 20, y + 20, 5, 8, 0xFF6B4320);
        R(c, x, y + 8, 28, 16, 0xFF8B5A2B);
        R(c, x + 22, y + 2, 14, 14, 0xFF8B5A2B);
        R(c, x + 33, y + 7, 7, 7, 0xFFFF2222);
        R(c, x + 28, y + 5, 3, 3, 0xFF000000);
        R(c, x + 24, y - 7, 3, 9, 0xFFC9A66B);
        R(c, x + 31, y - 7, 3, 9, 0xFFC9A66B);
        R(c, x + 21, y - 7, 4, 3, 0xFFC9A66B);
        R(c, x + 34, y - 7, 4, 3, 0xFFC9A66B);

        if (!started) {
            T(c, tr, "Pulsa W o ESPACIO para volar", 400, 90, 22, 0xFFFFFFFF, true);
        }
    }
}
