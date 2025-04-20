package com.gaser.docCollab.websocket;

public class COLORS {
    private static final String[] COLORS = {
            "#FF5733",
            "#33A8FF",
            "#33FF57",
            "#D433FF"
    };

    public static String getColor(int index) {
        return COLORS[index];
    }
}