package com.example.sortedversion;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import static com.example.sortedversion.Config.SEED;
import static com.example.sortedversion.Config.SIZE;
import static com.example.sortedversion.Config.MapColors;
import static com.example.sortedversion.Config.SEA_LEVEL;

public class FieldRendering extends View {
    private static final int[][] ColorMap = new int[][] {
            {Color.rgb(0, 0, 139), Color.rgb(0, 0, 128), Color.rgb(30, 144, 255), Color.rgb(0, 191, 255)},
            {Color.rgb(240, 240, 255), Color.rgb(0, 44, 0), Color.rgb(245, 245, 245), Color.rgb(230, 200, 150)},
            {Color.rgb(86, 98, 49), Color.rgb(37, 54, 13), Color.rgb(120, 120, 120), Color.rgb(230, 200, 150)},
            {Color.rgb(102, 128, 68), Color.rgb(29, 36, 17), Color.rgb(120, 120, 120), Color.rgb(230, 200, 150)},
            {Color.rgb(255, 219, 88), Color.rgb(84, 76, 3), Color.rgb(120, 120, 120), Color.rgb(230, 200, 150)}
    };
    private static GameMap gameMap;
    private int hexRadius; // величина преближения
    private int offsetX, offsetY;
    private static final Logger LOGGER = Logger.getLogger(FieldRendering.class.getName());

    public FieldRendering(Context context) {
        super(context);
    }
    public void setMatrix(GameMap gameMap) {
        // получаем матрицу для отрисовки
        FieldRendering.gameMap = gameMap;
        invalidate();
    }

    @Override // Пересчитываем размер шестиугольника при изменении размера View
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        hexRadius = w / 1300;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override // рисуем карту
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // начало и конец отрисовки для оптимизации игры
        int startX = (int) Math.floor((offsetX + hexRadius) / (1.5 * hexRadius));
        int endX = (int) Math.ceil((getWidth() + offsetX + hexRadius) / (1.5 * hexRadius));
        int startY = (int) Math.floor((offsetY + hexRadius) / (hexRadius * Math.sqrt(3)));
        int endY = (int) Math.ceil((getHeight() + offsetY + hexRadius) / (hexRadius * Math.sqrt(3)));

        // циклы отрисовки
        for (int i = startX + 1; i < endX - 1; i++) {
            for (int j = startY + 1; j < endY - 2; j++) {
                if (i >= 0 && j >= 0 && i < SIZE && j < SIZE) {
                    float centerX = (float) (i * 1.5 * hexRadius - offsetX);
                    float centerY = (float) ((j + (i % 2 == 0 ? 0 : 0.5)) * Math.sqrt(3) * hexRadius - offsetY);
                    Path hexagonPath = getHexagonPath(centerX, centerY, hexRadius);
                    int grayColor = convtrtColorCell(i, j);
                    Paint grayFillPaint = new Paint();
                    grayFillPaint.setColor(grayColor);
                    grayFillPaint.setStyle(Paint.Style.FILL);
                    canvas.drawPath(hexagonPath, grayFillPaint);
                }
            }
        }
    }
    public static int convtrtColorCell(int x, int y){
        int a = gameMap.mapForRendering[x][y][0];
        int b = gameMap.mapForRendering[x][y][1];
        int c = gameMap.mapForRendering[x][y][2];

        return ColorMap[a][b];
    }

    // Вычесляем радиус и всю тему шестиугольника
    private Path getHexagonPath(float cx, float cy, float radius) {
        Path path = new Path();
        float angle = (float) (Math.PI / 3);

        for (int i = 0; i < 6; i++) {
            float x = cx + radius * (float) Math.cos(angle * i);
            float y = cy + radius * (float) Math.sin(angle * i);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
        return path;
    }
    @Override //Нужна для скролла
    public void scrollBy(int x, int y) {
        int newOffsetX = offsetX + x;
        int newOffsetY = offsetY + y;

        if (newOffsetX >= -hexRadius && newOffsetX <= (SIZE + 1) * hexRadius * 1.5 - getWidth()) {
            offsetX = newOffsetX;
        }

        if (newOffsetY >= -hexRadius && newOffsetY <= (SIZE + 1) * hexRadius * Math.sqrt(3) - getHeight()) {
            offsetY = newOffsetY;
        }
        invalidate();
    }

    // ниже точечные деревья
    private List<PointF> generateRandomPointsInsideHexagon(int numPoints, float centerX, float centerY, float hexRadius, long seed) {
        List<PointF> points = new ArrayList<>();

        // Инициализация генератора случайных чисел с заданным сидом
        Random random = new Random(seed);

        float hexArea = calculateHexagonArea(hexRadius);

        for (int i = 0; i < numPoints; i++) {
            // Генерируем случайные координаты внутри шестиугольника
            float x = centerX + (random.nextFloat() - 0.5f) * 2 * hexRadius;
            float y = centerY + (random.nextFloat() - 0.5f) * 2 * hexRadius;

            // Проверяем, что точка находится внутри шестиугольника
            if (isPointInsideHexagon(x, y, centerX, centerY, hexRadius)) {
                points.add(new PointF(x, y));
            } else {
                // Если точка не внутри шестиугольника, генерируем новую
                i--;
            }
        }

        return points;
    }
    private boolean isPointInsideHexagon(float x, float y, float centerX, float centerY, float hexRadius) {
        // Проверяем, что точка внутри шестиугольника
        float deltaX = x - centerX;
        float deltaY = y - centerY;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        return distance < hexRadius / 10 * 9;
    }
    private float calculateHexagonArea(float hexRadius) {
        return (3 * (float) Math.sqrt(3) / 2) * hexRadius * hexRadius;
    }
}