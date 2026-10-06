package com.esperajuegos.client.games;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Base de todos los minijuegos. Cada juego dibuja en un espacio lógico de 800x480
 * que se escala al cuadro disponible. Las teclas llegan como códigos GLFW
 * (para letras y espacio coinciden con ASCII mayúscula: 'W', 'A', 'S', 'D', ' ').
 */
public abstract class MiniGame {
    public static final int LW = 800, LH = 480;

    protected final Random rnd = new Random();
    public int score;
    public boolean over;

    private final Set<Integer> held = new HashSet<>();
    private long lastNs = 0, accNs = 0;
    protected float s = 1f, ox, oy;

    public abstract String name();
    public abstract String help();
    /** Milisegundos entre pasos de la simulación. */
    protected abstract int periodMs();
    protected abstract void reset();
    protected abstract void step();
    protected abstract void onKey(int key, boolean fresh);
    protected abstract void draw(DrawContext c, TextRenderer tr);

    protected String endTitle() {
        return "¡Fin del juego!";
    }

    /** Click del mouse (solo algunos juegos lo usan). */
    public boolean click(double mx, double my, int button) {
        return false;
    }

    // ---------------------------------------------------------------- entrada

    public final void press(int key) {
        boolean fresh = held.add(key);
        if (key == 'R') {
            if (fresh) {
                reset();
            }
            return;
        }
        if (over) {
            return;
        }
        onKey(key, fresh);
    }

    public final void release(int key) {
        held.remove(key);
    }

    public final void clearKeys() {
        held.clear();
    }

    protected boolean down(int key) {
        return held.contains(key);
    }

    // ---------------------------------------------------------------- bucle

    public final void update() {
        long now = System.nanoTime();
        if (lastNs == 0) {
            lastNs = now;
        }
        accNs += now - lastNs;
        lastNs = now;
        long p = periodMs() * 1_000_000L;
        int n = 0;
        while (accNs >= p && n < 6) {
            if (!over) {
                step();
            }
            accNs -= p;
            n++;
        }
        if (n >= 6) {
            accNs = 0;
        }
    }

    public final void render(DrawContext c, TextRenderer tr, int x, int y, int w, int h) {
        s = Math.min(w / (float) LW, h / (float) LH);
        ox = x + (w - LW * s) / 2f;
        oy = y + (h - LH * s) / 2f;
        update();
        draw(c, tr);
        if (over) {
            R(c, 0, 0, LW, LH, 0xB0000000);
            T(c, tr, endTitle(), 400, 150, 38, 0xFFFFD84A, true);
            T(c, tr, "Puntos: " + score, 400, 215, 24, 0xFFFFFFFF, true);
            T(c, tr, "Pulsa R para jugar otra vez", 400, 265, 18, 0xFFBBDDFF, true);
        }
    }

    // ---------------------------------------------------------------- dibujo

    /** Rectángulo en coordenadas lógicas. */
    protected void R(DrawContext c, float lx, float ly, float lw, float lh, int color) {
        int x1 = Math.round(ox + lx * s);
        int y1 = Math.round(oy + ly * s);
        int x2 = Math.round(ox + (lx + lw) * s);
        int y2 = Math.round(oy + (ly + lh) * s);
        if (x2 <= x1) x2 = x1 + 1;
        if (y2 <= y1) y2 = y1 + 1;
        c.fill(x1, y1, x2, y2, color);
    }

    protected void circle(DrawContext c, float cx, float cy, float r, int color) {
        for (float dy = -r; dy < r; dy += 2f) {
            float yy = dy + 1f;
            float half = (float) Math.sqrt(Math.max(0f, r * r - yy * yy));
            R(c, cx - half, cy + dy, half * 2f, 2.3f, color);
        }
    }

    /** Texto en coordenadas lógicas; size = alto aproximado del texto en unidades lógicas. */
    protected void T(DrawContext c, TextRenderer tr, String str, float lx, float ly, float size, int color, boolean center) {
        float k = s * size / 8f;
        float wpx = tr.getWidth(str) * k;
        float px = ox + lx * s - (center ? wpx / 2f : 0f);
        float py = oy + ly * s;
        MatrixStack m = c.getMatrices();
        m.push();
        m.translate(px, py, 0f);
        m.scale(k, k, 1f);
        c.drawText(tr, str, 0, 0, color, false);
        m.pop();
    }

    protected float toLX(double mx) {
        return (float) ((mx - ox) / s);
    }

    protected float toLY(double my) {
        return (float) ((my - oy) / s);
    }

    protected static boolean overlap(float ax, float ay, float aw, float ah, float bx, float by, float bw, float bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    protected static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
