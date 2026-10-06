package com.esperajuegos.client.games;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/** Luces de Navidad (tipo Simon): repite la secuencia con W A S D. */
public class LightsGame extends MiniGame {
    private static final int[] DIM = {0xFF6B1414, 0xFF14501E, 0xFF6B5A14, 0xFF14306B};
    private static final int[] BRIGHT = {0xFFFF4040, 0xFF3CFF5A, 0xFFFFE04A, 0xFF4A8CFF};
    private static final char[] KEYS = {'W', 'A', 'S', 'D'};
    private static final float[][] POS = {{400, 120}, {270, 240}, {400, 360}, {530, 240}};

    private final List<Integer> seq = new ArrayList<>();
    private int phase, timer, showIdx, inputIdx, showLit = -1, flashLit = -1, flash;

    public LightsGame() {
        reset();
    }

    @Override public String name() { return "Luces de Navidad"; }
    @Override public String help() { return "Mira la secuencia y repítela con W A S D   R: reiniciar"; }
    @Override protected int periodMs() { return 50; }
    @Override protected String endTitle() { return "¡Te equivocaste!"; }

    @Override
    protected void reset() {
        seq.clear();
        seq.add(rnd.nextInt(4));
        score = 0;
        over = false;
        phase = 0;
        timer = 16;
        showLit = -1;
        flashLit = -1;
        flash = 0;
        inputIdx = 0;
    }

    @Override
    protected void step() {
        if (flash > 0) {
            flash--;
            if (flash == 0) flashLit = -1;
        }
        int on = Math.max(5, 10 - seq.size() / 3);
        int gap = Math.max(2, 4 - seq.size() / 6);
        switch (phase) {
            case 0:
                if (--timer <= 0) {
                    phase = 1;
                    showIdx = 0;
                    timer = on;
                    showLit = seq.get(0);
                }
                break;
            case 1:
                if (--timer <= 0) {
                    showLit = -1;
                    phase = 2;
                    timer = gap;
                }
                break;
            case 2:
                if (--timer <= 0) {
                    showIdx++;
                    if (showIdx >= seq.size()) {
                        phase = 3;
                        inputIdx = 0;
                    } else {
                        phase = 1;
                        timer = on;
                        showLit = seq.get(showIdx);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onKey(int key, boolean fresh) {
        if (phase != 3 || !fresh) return;
        int idx = key == 'W' ? 0 : key == 'A' ? 1 : key == 'S' ? 2 : key == 'D' ? 3 : -1;
        if (idx < 0) return;
        flashLit = idx;
        flash = 6;
        if (idx == seq.get(inputIdx)) {
            inputIdx++;
            if (inputIdx >= seq.size()) {
                score++;
                seq.add(rnd.nextInt(4));
                phase = 0;
                timer = 18;
            }
        } else {
            over = true;
        }
    }

    @Override
    protected void draw(DrawContext c, TextRenderer tr) {
        R(c, 0, 0, LW, LH, 0xFF0B1B3A);
        for (int i = 0; i < 4; i++) {
            boolean lit = showLit == i || flashLit == i;
            float cx = POS[i][0], cy = POS[i][1];
            R(c, cx - 8, cy - 70, 16, 18, 0xFF888888);
            if (lit) {
                circle(c, cx, cy, 72, (BRIGHT[i] & 0x00FFFFFF) | 0x44000000);
            }
            circle(c, cx, cy, 55, lit ? BRIGHT[i] : DIM[i]);
            circle(c, cx - 18, cy - 18, 9, lit ? 0xAAFFFFFF : 0x33FFFFFF);
            T(c, tr, String.valueOf(KEYS[i]), cx, cy - 14, 36, 0xFFFFFFFF, true);
        }
        circle(c, 400, 240, 16, 0xFFFFD84A);
        T(c, tr, phase < 3 ? "Mira la secuencia..." : "¡Tu turno!", 400, 12, 22, 0xFFFFFFFF, true);
        T(c, tr, "Largo: " + seq.size(), 400, 450, 18, 0xFFBBDDFF, true);
    }
}
