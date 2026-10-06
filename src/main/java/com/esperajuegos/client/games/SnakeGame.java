package com.esperajuegos.client.games;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.Deque;

/** Serpiente-bastón de caramelo que recoge regalos. */
public class SnakeGame extends MiniGame {
    private static final int COLS = 20, ROWS = 12, CELL = 40;

    private final Deque<int[]> body = new ArrayDeque<>();
    private int dx, dy, ndx, ndy, fx, fy, period = 130;

    public SnakeGame() {
        reset();
    }

    @Override public String name() { return "Serpiente Navideña"; }
    @Override public String help() { return "W A S D: mover   R: reiniciar"; }
    @Override protected int periodMs() { return period; }
    @Override protected String endTitle() { return "¡Te chocaste!"; }

    @Override
    protected void reset() {
        body.clear();
        body.addLast(new int[]{10, 6});
        body.addLast(new int[]{9, 6});
        body.addLast(new int[]{8, 6});
        dx = 1; ndx = 1;
        dy = 0; ndy = 0;
        score = 0;
        over = false;
        period = 130;
        placeFood();
    }

    private void placeFood() {
        for (int tries = 0; tries < 500; tries++) {
            fx = rnd.nextInt(COLS);
            fy = rnd.nextInt(ROWS);
            boolean ok = true;
            for (int[] b : body) {
                if (b[0] == fx && b[1] == fy) {
                    ok = false;
                    break;
                }
            }
            if (ok) return;
        }
    }

    @Override
    protected void step() {
        dx = ndx;
        dy = ndy;
        int[] head = body.peekFirst();
        int nx = head[0] + dx, ny = head[1] + dy;
        if (nx < 0 || ny < 0 || nx >= COLS || ny >= ROWS) {
            over = true;
            return;
        }
        boolean eat = nx == fx && ny == fy;
        if (!eat) {
            body.removeLast();
        }
        for (int[] b : body) {
            if (b[0] == nx && b[1] == ny) {
                over = true;
                return;
            }
        }
        body.addFirst(new int[]{nx, ny});
        if (eat) {
            score++;
            period = Math.max(70, 130 - score * 3);
            placeFood();
        }
    }

    @Override
    protected void onKey(int key, boolean fresh) {
        switch (key) {
            case 'W': if (dy == 0) { ndx = 0; ndy = -1; } break;
            case 'S': if (dy == 0) { ndx = 0; ndy = 1; } break;
            case 'A': if (dx == 0) { ndx = -1; ndy = 0; } break;
            case 'D': if (dx == 0) { ndx = 1; ndy = 0; } break;
            default: break;
        }
    }

    @Override
    protected void draw(DrawContext c, TextRenderer tr) {
        for (int j = 0; j < ROWS; j++) {
            for (int i = 0; i < COLS; i++) {
                R(c, i * CELL, j * CELL, CELL, CELL, (i + j) % 2 == 0 ? 0xFF0E3B1E : 0xFF0B3019);
            }
        }
        // regalo
        float gx = fx * CELL, gy = fy * CELL;
        R(c, gx + 6, gy + 6, 28, 28, 0xFF1F9E3F);
        R(c, gx + 17, gy + 6, 6, 28, 0xFFD62828);
        R(c, gx + 6, gy + 17, 28, 6, 0xFFD62828);
        R(c, gx + 13, gy + 1, 14, 6, 0xFFFFD84A);

        int k = 0;
        for (int[] b : body) {
            int col = k == 0 ? 0xFFFFD84A : (k % 2 == 0 ? 0xFFD62828 : 0xFFF1F1F1);
            R(c, b[0] * CELL + 2, b[1] * CELL + 2, CELL - 4, CELL - 4, col);
            k++;
        }
        int[] h = body.peekFirst();
        float hx = h[0] * CELL, hy = h[1] * CELL;
        if (dx != 0) {
            float ex = dx > 0 ? 24 : 10;
            R(c, hx + ex, hy + 9, 6, 6, 0xFF000000);
            R(c, hx + ex, hy + 25, 6, 6, 0xFF000000);
        } else {
            float ey = dy > 0 ? 24 : 10;
            R(c, hx + 9, hy + ey, 6, 6, 0xFF000000);
            R(c, hx + 25, hy + ey, 6, 6, 0xFF000000);
        }
    }
}
