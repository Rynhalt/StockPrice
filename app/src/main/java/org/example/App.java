package org.example;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.apple.eawt.Application;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class App extends Application {

    private static final Queue<String> stockHistory = new LinkedList<>();
    private XYChart.Series<Number, Number> series;
    private int timeCounter = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Live DJI Stock Price Chart");

        // Defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (ticks)");
        yAxis.setLabel("Price (USD)");

        // Creating the line chart
        final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Dow Jones Industrial Average (DJI)");

        // Defining a series
        series = new XYChart.Series<>();
        series.setName("DJI Price");
        lineChart.getData().add(series);

        // Create a scene and display
        Scene scene = new Scene(lineChart, 800, 600);
        stage.setScene(scene);
        stage.show();

        // Start fetching stock data
        startStockDataFetching();
    }

    private void startStockDataFetching() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            try {
                Stock dow = YahooFinance.get("^DJI");
                BigDecimal price = dow.getQuote().getPrice();
                LocalDateTime now = LocalDateTime.now();
                String record = now.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " - DJI: $" + price;
                stockHistory.add(record);
                System.out.println(record);

                Platform.runLater(() -> {
                    // Add data point to the chart
                    series.getData().add(new XYChart.Data<>(timeCounter++, price.doubleValue()));

                    // Optionally limit points to keep chart clean
                    if (series.getData().size() > 30) {
                        series.getData().remove(0);
                    }
                });

            } catch (IOException e) {
                System.err.println("Error fetching stock data: " + e.getMessage());
            }
        };

        scheduler.scheduleAtFixedRate(task, 0, 5, TimeUnit.SECONDS);
    }
}
