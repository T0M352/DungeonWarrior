package com.tomesz.game.map.dungeonGenerator;

import java.util.HashSet;

public class WallByteTypes {
    public static HashSet<Integer> wallTop = new HashSet<Integer>() {{
        add(0b1111);
        add(0b0110);
        add(0b0011);
        add(0b0010);
        add(0b1010);
        add(0b1100);
        add(0b1110);
        add(0b1011);
        add(0b0111);
    }};

    public static HashSet<Integer> wallSideLeft = new HashSet<Integer>() {{
        add(0b0100);
    }};

    public static HashSet<Integer> wallSideRight = new HashSet<Integer>() {{
        add(0b0001);
    }};

    public static HashSet<Integer> wallBottom = new HashSet<Integer>() {{
        add(0b1000);
    }};

    public static HashSet<Integer> wallInnerCornerDownLeft = new HashSet<Integer>() {{
        add(0b11110001);
        add(0b11100000);
        add(0b11110000);
        add(0b11100001);
        add(0b10100000);
        add(0b01010001);
        add(0b11010001);
        add(0b01100001);
        add(0b11010000);
        add(0b01110001);
        add(0b00010001);
        add(0b10110001);
        add(0b10100001);
        add(0b10010000);
        add(0b00110001);
        add(0b10110000);
        add(0b00100001);
        add(0b10010001);
    }};

    public static HashSet<Integer> wallInnerCornerDownRight = new HashSet<Integer>() {{
        add(0b11000111);
        add(0b11000011);
        add(0b10000011);
        add(0b10000111);
        add(0b10000010);
        add(0b01000101);
        add(0b11000101);
        add(0b01000011);
        add(0b10000101);
        add(0b01000111);
        add(0b01000100);
        add(0b11000110);
        add(0b11000010);
        add(0b10000100);
        add(0b01000110);
        add(0b10000110);
        add(0b11000100);
        add(0b01000010);
    }};

    public static HashSet<Integer> wallDiagonalCornerDownLeft = new HashSet<Integer>() {{
        add(0b01000000);
    }};

    public static HashSet<Integer> wallDiagonalCornerDownRight = new HashSet<Integer>() {{
        add(0b00000001);
    }};

    public static HashSet<Integer> wallDiagonalCornerUpLeft = new HashSet<Integer>() {{
        add(0b00010000);
        add(0b01010000);
    }};

    public static HashSet<Integer> wallDiagonalCornerUpRight = new HashSet<Integer>() {{
        add(0b00000100);
        add(0b00000101);
    }};

    public static HashSet<Integer> wallFull = new HashSet<Integer>() {{
        add(0b1101);
        add(0b0101);
        add(0b1001);
    }};

    public static HashSet<Integer> wallFullEightDirections = new HashSet<Integer>() {{
        add(0b00010100);
        add(0b11100100);
        add(0b10010011);
        add(0b01110100);
        add(0b00010111);
        add(0b00010110);
        add(0b00110100);
        add(0b00010101);
        add(0b01010100);
        add(0b00010010);
        add(0b00100100);
        add(0b00010011);
        add(0b01100100);
        add(0b10010111);
        add(0b11110100);
        add(0b10010110);
        add(0b10110100);
        add(0b11100101);
        add(0b11010011);
        add(0b01110101);
        add(0b01010111);
        add(0b01100101);
        add(0b01010011);
        add(0b01010010);
        add(0b00100101);
        add(0b00110101);
        add(0b01010110);
        add(0b11010101);
        add(0b11010100);
        add(0b10010101);
    }};

    public static HashSet<Integer> wallBottomEightDirections = new HashSet<Integer>() {{
        add(0b01000001);
    }};

}
