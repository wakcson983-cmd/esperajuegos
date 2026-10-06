package com.esperajuegos.client.games;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.Deque;

/** Busca minas: las minas son bolas de Navidad. WASD mueve el cursor, ESPACIO destapa, F pone bandera. */
public class MinesGame extends MiniGame {
    private static final int COLS = 16, ROWS = 10, CELL = 48, MINES = 22, BX = 16;
    private static final int[] NUM_COLORS = {0, 0xFF55AAFF, 0xFF55FF55, 0xFFFF6666, 0xFFBB88FF,
            0xFFFFAA00, 0xFF55FFFF, 0xFFFFFFFF, 0xFFAAAAAA};

    private int[][] val;
    private boolean[][] open, flag;
    private boolean placed, win, boom;
    private int cx, cy, opened, ex, ey;

    public MinesGame() {
        reset();
    }

    @Override public String name() { return "Busca Minas Navideño"; }
    @Override public String help() { return "WASD: mover  ESPACIO: destapar  F: bandera  (o click izq/der)"; }
    @Override protected int periodMs() { return 100; }
    @Override protected String endTitle() { return win ? "¡Despejaste el bosque!" : "¡BOOM! Explotó una bola"; }

    @Override
    protected void reset() {
        val = new int[ROWS][COLS];
        open = new boolean[ROWS][COLS];
        flag = new boolean[ROWS][COLS];
        placed = false;
        win = false;
        boom = false;
        cx = COLS / 2;
        cy = ROWS / 2;
        opened = 0;
        score = 0;
        over = false;
    }

    @Override protected void step() {}

    private void place(int sx, int sy) {
        int n = 0;
        while (n < MINES) {
            int x = rnd.nextInt(COLS), y = rnd.nextInt(ROWS);
            if (val[y][x] == -1) continue;
            if (Math.abs(x - sx) <= 1 && Math.abs(y - sy) <= 1) continue;
            val[y][x] = -1;
            n++;
        }
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (val[y][x] == -1) continue;
                int cnt = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = x + dx, ny = y + dy;
                        if (nx >= 0 && ny >= 0 && nx < COLS && ny < ROWS && val[ny][nx] == -1) cnt++;
                    }
                }
                val[y][x] = cnt;
            }
        }
        placed = true;
    }

    private void reveal(int x, int y) {
        if (over || flag[y][x] || open[y][x]) return;
        if (!placed) place(x, y);
        if (val[y][x] == -1) {
            boom = true;
            ex = x;
            ey = y;
            over = true;
            return;
        }
        Deque<int[]> st = new ArrayDeque<>();
        st.push(new int[]{x, y});
        while (!st.isEmpty()) {
            int[] p = st.pop();
            int px = p[0], py = p[1];
            if (open[py][px] || flag[py][px]) continue;
            open[py][px] = true;
            opened++;
            if (val[py][px] == 0) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = px + dx, ny = py + dy;
                        if (nx >= 0 && ny >= 0 && nx < COLS && ny < ROWS && !open[ny][nx]) {
                            st.push(new int[]{nx, ny});
                        }
                    }
                }
            }
        }
        score = opened;
        if (opened == COLS * ROWS - MINES) {
            win = true;
            over = true;
        }
    }

    @Override
    protected void onKey(int key, boolean fresh) {
        switch (key) {
            case 'W': cy = Math.max(0, cy - 1); break;
            case 'S': cy = Math.min(ROWS - 1, cy + 1); break;
            case 'A': cx = Math.max(0, cx - 1); break;
            case 'D': cx = Math.min(COLS - 1, cx + 1); break;
            case ' ': if (fresh) reveal(cx, cy); break;
            case 'F': if (fresh && !open[cy][cx]) flag[cy][cx] = !flag[cy][cx]; break;
            default: break;
        }
    }

    @Override
    public boolean click(double mx, double my, int button) {
        if (over) return false;
        int x = (int) Math.floor((toLX(mx) - BX) / CELL);
        int y = (int) Math.floor(toLY(my) / CELL);
        if (x < 0 || y < 0 || x >= COLS || y >= ROWS) return false;
        cx = x;
        cy = y;
        if (button == 0) {
            reveal(x, y);
        } else if (button == 1 && !open[y][x]) {
            flag[y][x] = !flag[y][x];
        }
        return true;
    }

    @Override
    protected void draw(DrawContext c, TextRenderer tr) {
        R(c, 0, 0, LW, LH, 0xFF0B2A16);
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                float px = BX + x * CELL, py = y * CELL;
                boolean showMine = boom && val[y][x] == -1 && !flag[y][x];
                if (open[y][x]) {
                    R(c, px + 1, py + 1, CELL - 2, CELL - 2, 0xFF14452A);
                    int v = val[y][x];
                    if (v > 0) {
                        T(c, tr, String.valueOf(v), px + CELL / 2f, py + 10, 28, NUM_COLORS[v], true);
                    }
                } else if (showMine) {
                    R(c, px + 1, py + 1, CELL - 2, CELL - 2, (x == ex && y == ey) ? 0xFFB02020 : 0xFF14452A);
                    circle(c, px + 24, py + 27, 13, 0xFFE63946);
                    circle(c, px + 19, py + 22, 3, 0xFFFFFFFF);
                    R(c, px + 20, py + 9, 8, 6, 0xFFFFD84A);
                } else {
                    R(c, px + 1, py + 1, CELL - 2, CELL - 2, 0xFFB8D4E8);
                    R(c, px + 3, py + 3, CELL - 6, CELL - 6, 0xFFE6F2FA);
                    if (flag[y][x]) {
                        R(c, px + 14, py + 8, 4, 32, 0xFF6B4A2B);
                        R(c, px + 18, py + 8, 18, 6, 0xFF2ECC40);
                        R(c, px + 18, py + 14, 13, 6, 0xFF2ECC40);
                        R(c, px + 18, py + 20, 8, 6, 0xFF2ECC40);
                    }
                }
            }
        }
        if (!over) {
            float px = BX + cx * CELL, py = cy * CELL;
            int g = 0xFFFFD700;
            R(c, px, py, CELL, 3, g);
            R(c, px, py + CELL - 3, CELL, 3, g);
            R(c, px, py, 3, CELL, g);
            R(c, px + CELL - 3, py, 3, CELL, g);
        }
    }
}
