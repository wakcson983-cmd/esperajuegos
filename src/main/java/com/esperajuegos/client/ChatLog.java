package com.esperajuegos.client;

import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** Guarda los últimos mensajes del chat para mostrarlos dentro de la pantalla de espera. */
public final class ChatLog {
    public static final List<Text> MESSAGES = new ArrayList<>();

    private ChatLog() {}

    public static void add(Text text) {
        MESSAGES.add(text);
        while (MESSAGES.size() > 200) {
            MESSAGES.remove(0);
        }
    }

    public static void clear() {
        MESSAGES.clear();
    }
}
