package com.example.sortedversion;

import static com.example.sortedversion.Config.SIZE;
import static com.example.sortedversion.Config.SEED;
import static com.example.sortedversion.Config.NUM_POINTS;
import static com.example.sortedversion.Config.NUM_ITERATIONS;
import static com.example.sortedversion.Config.BORDER_WIDTH;

import android.graphics.Point;
import java.util.Random;
import java.util.logging.Logger;

public class CreatingMap {
    public static Point[] generateRandomPoints() {
        Point[] points = new Point[NUM_POINTS];
        Random random = new Random(SEED);
        for (int i = 0; i < NUM_POINTS; i++) {
            points[i] = new Point(random.nextInt(SIZE), random.nextInt(SIZE));
        }
        return points;
    } // генерируем некоторое количество биомных точек

    private static int findNearestPointIndex(int x, int y, Point[] points) {
        int minDistance = Integer.MAX_VALUE;
        int nearestPointIndex = -1;

        for (int i = 0; i < NUM_POINTS; i++) {
            int distance = calculateDistance(points[i], x, y);
            if (distance < minDistance) {
                minDistance = distance;
                nearestPointIndex = i;
            }
        }

        return nearestPointIndex;
    } // поиск ближайших точек для диограммы вороного

    private static int calculateDistance(Point p1, int x, int y) {
        return (int) Math.sqrt(Math.pow(p1.x - x, 2) + Math.pow(p1.y - y, 2));
    } // Считаем дистанцию

    public static float[][] generateNoiseMap(int res, int SID, int octaves) {
        Perlin2D perlin = new Perlin2D(SID);
        float[][] noiseMap = new float[SIZE][SIZE];
        float scale = (float) SIZE / res;

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                noiseMap[x][y] = perlin.getNoise(x/scale, y/scale, octaves, 0.5f);
            }
        }
        return noiseMap;
    } // Генерируем карты для размытия

    private static int[] generateGradientList() {
        int[] gradientList = new int[SIZE];

        for (int i = 0; i < SIZE; i++) {
            double interpolation;

            if (i < SIZE / 5) {
                // От -35 до -5 (1/5 массива)
                interpolation = (double) i / (SIZE / 5 - 1);
                gradientList[i] = (int) (-35 + interpolation * (-5 - (-35)));
            } else if (i < (4 * SIZE / 5)) {
                // От -5 до 25 (3/5 массива)
                interpolation = (double) (i - SIZE / 5) / (3 * SIZE / 5 - 1);
                gradientList[i] = (int) (-5 + interpolation * (25 - (-5)));
            } else {
                // От 25 до 40 (1/5 массива)
                interpolation = (double) (i - 4 * SIZE / 5) / (SIZE / 5 - 1);
                gradientList[i] = (int) (25 + interpolation * (40 - 25));
            }
        }

        return gradientList;
    } // Лист градиента для получения температуры окр. среды

    private static final Logger LOGGER = Logger.getLogger(CreatingMap.class.getName()); // Для вывода логов

    public int[][][] getMap() {
        int[][][] collection_matrices = new int[3][SIZE][SIZE];
        int[][] preformMap = new int[SIZE][SIZE];
        int [][] HelpingLloyd = new int[NUM_POINTS][3];

        Point[] points = generateRandomPoints();
        long startTime = System.nanoTime();
        // Определение областей Вороного и Релаксация Ллойда
        for (int iteration = 0; iteration < NUM_ITERATIONS; iteration++) {
            // Определение областей Вороного и закрашивание их цветом
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    int nearestPointIndex = findNearestPointIndex(x, y, points);
                    preformMap[x][y] = nearestPointIndex;
                    HelpingLloyd[nearestPointIndex][0] += x;
                    HelpingLloyd[nearestPointIndex][1] += y;
                    HelpingLloyd[nearestPointIndex][2] ++;
                }
            }

            // Релаксация Ллойда - перемещение точек к центру масс их ячеек
            for (int i = 0; i < NUM_POINTS; i++) {
                int sumX = HelpingLloyd[i][0];
                int sumY = HelpingLloyd[i][1];
                int count = HelpingLloyd[i][2];
                HelpingLloyd[i][0] = 0;
                HelpingLloyd[i][1] = 0;
                HelpingLloyd[i][2] = 0;
                points[i] = new Point(sumX / count, sumY / count);
            }
        }
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;

        LOGGER.info("'Релаксация Ллойда' Время выполнения: " + elapsedTime / 1e6 + " миллисекунд");
        startTime = System.nanoTime();

        // добаление размытия
        float[][] noiseMap1 = generateNoiseMap(32, 200, 8);
        float[][] noiseMap2 = generateNoiseMap(32, 250, 8);
        // Создаем размытую карту Вороного на основе индексов границ
        int[][] blurredVoronoiMap = new int[SIZE][SIZE];
        int[][] temperature_by_yMap = new int[SIZE][SIZE];
        int[] gradientList = generateGradientList(); // Выберите необходимый размер списка (например, 100).
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                preformMap[x][y] = findNearestPointIndex(x, y, points);
                int i = (int) (x + BORDER_WIDTH * noiseMap1[x][y]) % SIZE;
                int j = (int) (y + BORDER_WIDTH * noiseMap2[x][y]) % SIZE;

                // Обработка отрицательных индексов
                i = (i + SIZE) % SIZE;
                j = (j + SIZE) % SIZE;

                // Записываем индекс в массив границ
                int index = i * SIZE + j;

                i = index / SIZE;
                j = index % SIZE;

                int diffX = x - i;
                int diffY = y - j;

                int dist = Math.max(Math.abs(diffX), Math.abs(diffY));

                blurredVoronoiMap[x][y] = (dist > BORDER_WIDTH) ? preformMap[x][y] : preformMap[i][j];
                int height_by_y = points[blurredVoronoiMap[x][y]].y;
                int temperature = gradientList[height_by_y];
                temperature_by_yMap[x][y] = temperature;
            }
        }
        int[][] elevation = new int[SIZE][SIZE];
        Perlin2D perlin = new Perlin2D(SEED);
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                float value = perlin.getNoise(x/100f,y/100f, 6, 0.5f);
                elevation[x][y] = (int) (128 + (127 * Math.tanh(value * 3)));
            }
        }
        endTime = System.nanoTime();
        elapsedTime = endTime - startTime;
        LOGGER.info("'Создаем размытую карту' Время выполнения: " + elapsedTime / 1e6 + " миллисекунд");
        collection_matrices[0] = blurredVoronoiMap;
        collection_matrices[1] = temperature_by_yMap;
        collection_matrices[2] = elevation;
        return collection_matrices;
    } // Получаем нужный нам список матриц
}
