package com.esperajuegos.client.games;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/** 2048 con colores navideños. WASD desliza las fichas. */
public class Game2048 extends MiniGame {
    private static final int[] COLORS = {
            0xFFFFF4E0, 0xFFFFD6D6, 0xFFFF9B9B, 0xFFFF6B6B, 0xFFE63946, 0xFFB5222E,
            0xFF8FD694, 0xFF4CAF50, 0xFF2E8B57, 0xFFFFD700, 0xFFFFFFFF};

    private int[][] g;

    public Game2048() {
        reset();
    }

    @Override public String name() { return "2048 Navideño"; }
    @Override public String help() { return "W A S D: deslizar fichas   R: reiniciar"; }
    @Override protected int periodMs() { return 100; }
    @Override protected String endTitle() { return "¡Sin movimientos!"; }

    @Override
    protected void reset() {
        g = new int[4][4];
        score = 0;
        over = false;
        spawn();
        spawn();
    }

    @Override protected void step() {}

    private void spawn() {
        List<int[]> empty = new ArrayList<>();
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (g[r][c] == 0) empty.add(new int[]{r, c});
            }
        }
        if (empty.isEmpty()) return;
        int[] p = empty.get(rnd.nextInt(empty.size()));
        g[p[0]][p[1]] = rnd.nextInt(10) == 0 ? 4 : 2;
    }

    private static int row(int dir, int i, int j) {
        return switch (dir) {
            case 0 -> j;
            case 1 -> 3 - j;
            default -> i;
        };
    }

    private static int col(int dir, int i, int j) {
        return switch (dir) {
            case 2 -> j;
            case 3 -> 3 - j;
            default -> i;
        };
    }

    private int[] merge(int[] a) {
        int[] out = new int[4];
        int idx = 0, prev = 0;
        for (int v : a) {
            if (v == 0) continue;
            if (prev == v) {
                out[idx - 1] = v * 2;
                score += v * 2;
                prev = 0;
            } else {
                out[idx++] = v;
                prev = v;
            }
        }
        return out;
    }

    private boolean move(int dir) {
        boolean moved = false;
        for (int i = 0; i < 4; i++) {
            int[] line = new int[4];
            for (int j = 0; j < 4; j++) line[j] = g[row(dir, i, j)][col(dir, i, j)];
            int[] res = merge(line);
            for (int j = 0; j < 4; j++) {
                int r = row(dir, i, j), c = col(dir, i, j);
                if (g[r][c] != res[j]) moved = true;
                g[r][c] = res[j];
            }
        }
        return moved;
    }

    private boolean canMove() {
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (g[r][c] == 0) return true;
                if (r < 3 && g[r][c] == g[r + 1][c]) return true;
                if (c < 3 && g[r][c] == g[r][c + 1]) return true;
            }
        }
        return false;
    }

    @Override
    protected void onKey(int key, boolean fresh) {
        if (!fresh) return;
        int dir = key == 'W' ? 0 : key == 'S' ? 1 : key == 'A' ? 2 : key == 'D' ? 3 : -1;
        if (dir < 0) return;
        if (move(dir)) {
            spawn();
            if (!canMove()) over = true;
        }
    }

    @Override
    protected void draw(DrawContext c, TextRenderer tr) {
        R(c, 0, 0, LW, LH, 0xFF0B2A16);
        float bx = 175, by = 15;
        R(c, bx, by, 450, 450, 0xFF5A1010);
        for (int r = 0; r < 4; r++) {
            for (int q = 0; q < 4; q++) {
                float tx = bx + 10 + q * 110, ty = by + 10 + r * 110;
                int v = g[r][q];
                if (v == 0) {
                    R(c, tx, ty, 100, 100, 0xFF7A1A1A);
                    continue;
                }
                int lg = Integer.numberOfTrailingZeros(v) - 1;
                int col = COLORS[Math.min(lg, COLORS.length - 1)];
                R(c, tx, ty, 100, 100, col);
                String txt = String.valueOf(v);
                float size = txt.length() <= 2 ? 44 : txt.length() == 3 ? 36 : 28;
                int tc = v <= 8 ? 0xFF5A1010 : 0xFFFFFFFF;
                if (v == 2048) tc = 0xFFB5222E;
                T(c, tr, txt, tx + 50, ty + (100 - size * 0.875f) / 2f, size, tc, true);
            }
        }
    }
}
