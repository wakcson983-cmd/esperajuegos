package com.esperajuegos.client.games;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Laberinto: lleva al elfo hasta el regalo. Cada laberinto es nuevo. */
public class MazeGame extends MiniGame {
    private static final int W = 25, H = 15, CELL = 32;

    private boolean[][] wall;
    private int px, py;

    public MazeGame() {
        reset();
    }

    @Override public String name() { return "Laberinto del Elfo"; }
    @Override public String help() { return "W A S D: mover al elfo   R: nuevo laberinto"; }
    @Override protected int periodMs() { return 100; }
    @Override protected String endTitle() { return "¡Encontraste el regalo!"; }

    @Override
    protected void reset() {
        over = false;
        wall = new boolean[H][W];
        for (boolean[] row : wall) java.util.Arrays.fill(row, true);
        int[][] dirs = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}};
        Deque<int[]> st = new ArrayDeque<>();
        wall[1][1] = false;
        st.push(new int[]{1, 1});
        while (!st.isEmpty()) {
            int[] cur = st.peek();
            List<int[]> opts = new ArrayList<>();
            for (int[] d : dirs) {
                int nx = cur[0] + d[0], ny = cur[1] + d[1];
                if (nx > 0 && ny > 0 && nx < W - 1 && ny < H - 1 && wall[ny][nx]) opts.add(d);
            }
            if (opts.isEmpty()) {
                st.pop();
                continue;
            }
            int[] d = opts.get(rnd.nextInt(opts.size()));
            wall[cur[1] + d[1] / 2][cur[0] + d[0] / 2] = false;
            wall[cur[1] + d[1]][cur[0] + d[0]] = false;
            st.push(new int[]{cur[0] + d[0], cur[1] + d[1]});
        }
        px = 1;
        py = 1;
    }

    @Override protected void step() {}

    @Override
    protected void onKey(int key, boolean fresh) {
        int nx = px, ny = py;
        switch (key) {
            case 'W': ny--; break;
            case 'S': ny++; break;
            case 'A': nx--; break;
            case 'D': nx++; break;
            default: return;
        }
        if (nx >= 0 && ny >= 0 && nx < W && ny < H && !wall[ny][nx]) {
            px = nx;
            py = ny;
        }
        if (px == W - 2 && py == H - 2) {
            score++;
            over = true;
        }
    }

    @Override
    protected void draw(DrawContext c, TextRenderer tr) {
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                if (wall[y][x]) {
                    R(c, x * CELL, y * CELL, CELL, CELL, (x + y) % 2 == 0 ? 0xFFC62828 : 0xFFF1F1F1);
                } else {
                    R(c, x * CELL, y * CELL, CELL, CELL, 0xFF0F3D22);
                }
            }
        }
        // regalo (meta)
        float gx = (W - 2) * CELL, gy = (H - 2) * CELL;
        R(c, gx + 4, gy + 6, 24, 22, 0xFF3A86FF);
        R(c, gx + 13, gy + 6, 6, 22, 0xFFFFD84A);
        R(c, gx + 4, gy + 15, 24, 5, 0xFFFFD84A);
        R(c, gx + 10, gy + 1, 12, 6, 0xFFFFD84A);
        // elfo
        float ex = px * CELL, ey = py * CELL;
        R(c, ex + 8, ey + 14, 16, 14, 0xFF2ECC40);
        R(c, ex + 9, ey + 6, 14, 8, 0xFFFFD2A8);
        R(c, ex + 8, ey + 2, 16, 6, 0xFFD62828);
        R(c, ex + 14, ey, 4, 4, 0xFFFFFFFF);
        R(c, ex + 12, ey + 9, 2, 2, 0xFF000000);
        R(c, ex + 18, ey + 9, 2, 2, 0xFF000000);
    }
}
