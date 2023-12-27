package com.example.sortedversion;

import android.app.Activity;
import android.view.GestureDetector;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.util.logging.Logger;

public class MainActivity extends Activity {
    private FieldRendering mapView;
    private GestureDetector gestureDetector;
    private static final Logger LOGGER = Logger.getLogger(CreatingMap.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapView = new FieldRendering(this);
        setContentView(mapView);
        gestureDetector = new GestureDetector(this, new MapGestureListener());
        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        long startTime = System.nanoTime();
        GameMap gameMap = new GameMap();
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;

        LOGGER.info("'Все вместе' Время выполнения: " + elapsedTime / 1e6 + " миллисекунд");
        mapView.setMatrix(gameMap);
    }

    private class MapGestureListener extends GestureDetector.SimpleOnGestureListener {

        private float lastX, lastY;

        @Override
        public boolean onDown(MotionEvent e) {
            lastX = e.getX();
            lastY = e.getY();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float deltaX = e2.getX() - lastX;
            float deltaY = e2.getY() - lastY;

            mapView.scrollBy((int) -deltaX, (int) -deltaY);

            lastX = e2.getX();
            lastY = e2.getY();

            return true;
        }
    }
}