package com.esperajuegos.client.games;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Pong contra el Grinch. W/S mueven tu paleta. El primero en 7 gana. */
public class PongGame extends MiniGame {
    private float py, ay, bx, by, vx, vy;
    private int ps, as;

    public PongGame() {
        reset();
    }

    @Override public String name() { return "Pong del Grinch"; }
    @Override public String help() { return "W / S: mover tu paleta   R: reiniciar"; }
    @Override protected int periodMs() { return 16; }
    @Override protected String endTitle() { return ps >= 7 ? "¡Le ganaste al Grinch!" : "¡El Grinch ganó!"; }

    @Override
    protected void reset() {
        py = 195;
        ay = 195;
        ps = 0;
        as = 0;
        score = 0;
        over = false;
        serve(1);
    }

    private void serve(int dir) {
        bx = 393;
        by = 233;
        vx = 5f * dir;
        vy = (rnd.nextFloat() - 0.5f) * 5f;
    }

    @Override
    protected void step() {
        if (down('W')) py -= 6.5f;
        if (down('S')) py += 6.5f;
        py = clamp(py, 0, LH - 90);

        float target = vx > 0 ? by + 7 : 240;
        float ac = ay + 45;
        if (Math.abs(target - ac) > 10) {
            ay += Math.signum(target - ac) * 3.9f;
        }
        ay = clamp(ay, 0, LH - 90);

        bx += vx;
        by += vy;
        if (by < 0) { by = 0; vy = -vy; }
        if (by > LH - 14) { by = LH - 14; vy = -vy; }

        if (vx < 0 && bx <= 38 && bx + 14 >= 24 && by + 14 >= py && by <= py + 90) {
            bx = 38;
            vx = Math.min(11f, -vx * 1.06f);
            vy = ((by + 7) - (py + 45)) / 45f * 5.5f;
        }
        if (vx > 0 && bx + 14 >= 762 && bx <= 776 && by + 14 >= ay && by <= ay + 90) {
            bx = 748;
            vx = -Math.min(11f, vx * 1.04f);
            vy = ((by + 7) - (ay + 45)) / 45f * 5.5f;
        }

        if (bx < -20) {
            as++;
            serve(1);
        } else if (bx > LW + 20) {
            ps++;
            score = ps;
            serve(-1);
        }
        if (ps >= 7 || as >= 7) over = true;
    }

    @Override protected void onKey(int key, boolean fresh) {}

    @Override
    protected void draw(DrawContext c, TextRenderer tr) {
        R(c, 0, 0, LW, LH, 0xFF0C3A1F);
        for (int y = 0; y < LH; y += 30) {
            R(c, 396, y, 8, 16, 0x66FFFFFF);
        }
        T(c, tr, String.valueOf(ps), 300, 20, 48, 0xFFFFFFFF, true);
        T(c, tr, String.valueOf(as), 500, 20, 48, 0xFFFFFFFF, true);
        R(c, 24, py, 14, 90, 0xFFD62828);
        R(c, 24, py + 30, 14, 8, 0xFFFFFFFF);
        R(c, 762, ay, 14, 90, 0xFF6BCB3A);
        R(c, bx, by, 14, 14, 0xFFFFFFFF);
        T(c, tr, "Tu", 60, 452, 16, 0xFFFF9999, false);
        T(c, tr, "Grinch", 690, 452, 16, 0xFF9BEF6A, false);
    }
}
