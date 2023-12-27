package com.example.sortedversion;

import static com.example.sortedversion.CreatingMap.generateRandomPoints;

import android.graphics.Color;
import android.graphics.Point;

import static com.example.sortedversion.Config.SIZE;
import static com.example.sortedversion.Config.SEED;
import static com.example.sortedversion.Config.TREE_COUNT;
import static com.example.sortedversion.Config.SEA_LEVEL;

import java.util.logging.Logger;

public class GameMap {
    protected int[][] elevation; // Высота клетки
    protected int[][] biome;
    protected int[][] temperatureOfMap; // Температура по карте. карта всегда будет стремиться воссоздать эту температуру
    protected int[][] TemporaryTemperatureChanges; // Покрытие клетки. заснежинность и т.д.
    protected int[][] roadType; // Тип дороги
    protected String[][] unit; // юниты
    protected int[][] weather;
    protected int[][][] mapForRendering;// Добавлено поле для карты погоды
    protected Point [] points;
    private static final Logger LOGGER = Logger.getLogger(GameMap.class.getName());

    public GameMap() {
        this.TemporaryTemperatureChanges = new int[SIZE][SIZE];
        this.roadType = new int[SIZE][SIZE];
        this.unit = new String[SIZE][SIZE];
        this.mapForRendering = new int[SIZE][SIZE][3];

        CreatingMap matrix = new CreatingMap();
        int[][][] collection_matrices = matrix.getMap();
        this.biome = collection_matrices[0];
        this.temperatureOfMap = collection_matrices[1];
        this.TemporaryTemperatureChanges = collection_matrices[1];
        this.elevation = collection_matrices[2];
        this.points = generateRandomPoints();
        postProcessingMap();
    } // Конструктор для создания карты

    private void postProcessingMap() {
        Perlin2D noise = new Perlin2D(SEED);
        // Стремная тема с числами, но пока лучше не придумал
        // mapForRendering[x][y][0] 0, 1, 2, 3, 4 == океан, льды, средняя зона1, средняя зона2, пустыня.
        // mapForRendering[x][y][1] 0, 1, 2, 3 == если первое океан то степень глубины, иначе пустырь, лес, горы
        // mapForRendering[x][y][2] 0, 1, 2, 3 == особенности биомов, вид леса, заполним по обстаятельствам
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                boolean f_pes = false;
                for (int i = -1; i < 2; i++) {
                    if (f_pes) {break;}
                    for (int j = -1; j < 2; j++) {
                        if (i + x >= 0 && j + y >= 0 && i + x < SIZE && j + y < SIZE) {
                            if (elevation[i + x][j + y] <= 255 * SEA_LEVEL) {
                                f_pes = true;
                                break;
                            }
                        }
                    }
                }
                // определяем океан
                if (elevation[x][y] < 255 * SEA_LEVEL) {
                    mapForRendering[x][y][0] = 0;
                    mapForRendering[x][y][1] = (int) (elevation[x][y] / (255 * SEA_LEVEL / 4));
                    continue;
                }
                float noisL = noise.getNoise(x / 1000f, y / 1000f, 8, 1f); // Шум леса
                float noisG = noise.getNoise(x / 100f, y / 100f, 6, 0.5f); // Шум горы
                if (f_pes) {mapForRendering[x][y][1] = 3;}
                else if (0.2f < noisG) {mapForRendering[x][y][1] = 2;}
                else if (0.01f < noisL) {mapForRendering[x][y][1] = 1;}
                else {mapForRendering[x][y][1] = 0;}


                //Типы суши
                if (TemporaryTemperatureChanges[x][y] < -5) {mapForRendering[x][y][0] = 1;}
                else if (TemporaryTemperatureChanges[x][y] < 10) {mapForRendering[x][y][0] = 2;}
                else if (TemporaryTemperatureChanges[x][y] < 25) {mapForRendering[x][y][0] = 3;}
                else {mapForRendering[x][y][0] = 4;}
            }
        }
    }

    public static int generateTreeCount(int height, int x, int y) {
        // Параметры для генерации шума
        double scale = 20.0;
        double persistence = 0.5;
        double lacunarity = 2.0;
        int octaves = 6;

        Perlin2D noise = new Perlin2D(SEED);

        // Генерация шума
        double noiseValue = noise.getNoise((float) x/100f,y/100f, 8, 0.5f);

        // Задаем высотный порог для суши
        int landThreshold = (int) (255 * SEA_LEVEL);

        // Задаем количество деревьев для различных уровней высоты
        if (height < landThreshold || height > 200) {
            return 0; // Вода
        } else {
            double treeDensity = Math.max(0, noiseValue); // Используем значение шума для определения плотности деревьев
            return (int) (treeDensity * TREE_COUNT);
        }
    }
}

