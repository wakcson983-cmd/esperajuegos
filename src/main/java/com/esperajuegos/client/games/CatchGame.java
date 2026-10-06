package com.esperajuegos.client.games;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Atrapa los regalos con la canasta (A/D) y esquiva el carbón. */
public class CatchGame extends MiniGame {
    private static final int[] GIFT_COLORS = {0xFFD62828, 0xFF2ECC40, 0xFF3A86FF};

    private final List<float[]> items = new ArrayList<>(); // x, y, velocidad, tipo (3 = carbón)
    private float bx;
    private int lives;

    public CatchGame() {
        reset();
    }

    @Override public String name() { return "Atrapa Regalos"; }
    @Override public String help() { return "A / D: mover la canasta   R: reiniciar"; }
    @Override protected int periodMs() { return 16; }
    @Override protected String endTitle() { return "¡Te quedaste sin vidas!"; }

    @Override
    protected void reset() {
        items.clear();
        bx = 400;
        lives = 5;
        score = 0;
        over = false;
    }

    @Override
    protected void step() {
        if (down('A')) bx -= 6.5f;
        if (down('D')) bx += 6.5f;
        bx = clamp(bx, 66, 734);

        int rate = Math.max(16, 44 - score / 2);
        if (rnd.nextInt(rate) == 0) {
            float type = rnd.nextInt(100) < 28 ? 3 : rnd.nextInt(3);
            float speed = 2.4f + Math.min(3.5f, score * 0.05f) + rnd.nextFloat() * 1.2f;
            items.add(new float[]{40 + rnd.nextFloat() * 720, -30, speed, type});
        }

        Iterator<float[]> it = items.iterator();
        while (it.hasNext()) {
            float[] a = it.next();
            a[1] += a[2];
            if (a[1] + 28 >= 430 && a[1] <= 462 && Math.abs(a[0] - bx) <= 66) {
                if (a[3] == 3) {
                    lives--;
                } else {
                    score++;
                }
                it.remove();
                continue;
            }
            if (a[1] > 480) {
                if (a[3] != 3) lives--;
                it.remove();
            }
        }
        if (lives <= 0) {
            lives = 0;
            over = true;
        }
    }

    @Override protected void onKey(int key, boolean fresh) {}

    @Override
    protected void draw(DrawContext c, TextRenderer tr) {
        R(c, 0, 0, LW, LH, 0xFF0B1B3A);
        for (int i = 0; i < 36; i++) {
            R(c, (i * 137) % 790, (i * 83) % 420, 3, 3, 0x88FFFFFF);
        }
        R(c, 0, 462, LW, 18, 0xFFEAF4FB);

        for (float[] a : items) {
            float x = a[0], y = a[1];
            if (a[3] == 3) {
                R(c, x - 14, y + 2, 28, 24, 0xFF2B2B2B);
                R(c, x - 8, y + 8, 6, 6, 0xFF666666);
                R(c, x + 2, y + 14, 6, 6, 0xFF555555);
            } else {
                R(c, x - 14, y, 28, 28, GIFT_COLORS[(int) a[3]]);
                R(c, x - 3, y, 6, 28, 0xFFFFD84A);
                R(c, x - 14, y + 11, 28, 6, 0xFFFFD84A);
                R(c, x - 6, y - 6, 12, 6, 0xFFFFD84A);
            }
        }

        R(c, bx - 60, 432, 120, 30, 0xFF8B5A2B);
        R(c, bx - 60, 442, 120, 4, 0xFF6B4320);
        R(c, bx - 60, 452, 120, 4, 0xFF6B4320);
        R(c, bx - 68, 426, 136, 8, 0xFFC08A4B);

        T(c, tr, "Vidas: " + lives, 12, 10, 20, 0xFFFF6666, false);
    }
}
