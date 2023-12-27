package com.example.sortedversion;

import java.util.HashMap;
import java.util.Map;
import android.graphics.Color;


public class Config {
    // Основные настройки
    public static final int SIZE = 512; // размер карты
    public static final int SEED = 911; // любое число
    public final int minHexRadius = 10; // минимальная величина преближения
    public final int maxHexRadius = 100; // максимальная величина преближения

    // Побочные настройки карты
    public static final int NUM_ITERATIONS = 1; // Сколько раз мы будем выравнивать точки
    public static final int NUM_POINTS = 250;  // количество биомных точек
    public static final int BORDER_WIDTH = 16; // Размер границы биомов
    public static final int TREE_COUNT = 150; // плотность леса
    public static final float SEA_LEVEL = 0.45F; // Уровень воды

    public static final Map<Integer, Map<Integer, Color>> MapColors = new HashMap<Integer, Map<Integer, Color>>() {{
        // Добавляем элементы для ключа 1
        put(1, new HashMap<Integer, Color>() {{
            put(1, Color.valueOf(Color.rgb(255, 0, 0))); // Красный
            put(2, Color.valueOf(Color.rgb(255, 255, 0))); // Желтый
            put(3, Color.valueOf(Color.rgb(128, 0, 128)));
            put(4, Color.valueOf(Color.rgb(128, 0, 128)));// Пурпурный
        }});

        // Добавляем элементы для ключа 2
        put(2, new HashMap<Integer, Color>() {{
            put(1, Color.valueOf(Color.rgb(0, 0, 255))); // Синий
            put(2, Color.valueOf(Color.rgb(192, 192, 192))); // Серебристый
        }});
    }};
}
