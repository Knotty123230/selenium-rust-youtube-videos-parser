package org.example;

import org.example.entity.Video;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Main {
    private final Map<String, Video> videos = new ConcurrentHashMap<>();
    private final Map<String, Video> existsVideos = new ConcurrentHashMap<>();

    private WebDriver webDriver;

    public void initializeWebDriver() throws MalformedURLException {
        ChromeOptions chromeOptions = new ChromeOptions();
        webDriver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), chromeOptions);
        webDriver.manage().timeouts().pageLoadTimeout(Duration.of(10, ChronoUnit.SECONDS));
        webDriver.manage().timeouts().implicitlyWait(Duration.of(10, ChronoUnit.SECONDS));
        webDriver.manage().timeouts().scriptTimeout(Duration.of(10, ChronoUnit.SECONDS));
        webDriver.manage().window().maximize();
    }


    private synchronized void extracted() throws MalformedURLException {
        initializeWebDriver();
        String pageSource = null;
        try {
            webDriver.get("https://www.youtube.com/results?search_query=%D1%80%D0%B0%D1%81%D1%82");
            webDriver.navigate().refresh();
            int count = 0;
            while (count != 5) {
                JavascriptExecutor jse = (JavascriptExecutor) webDriver;
                jse.executeScript("window.scrollBy(0,document.body.scrollHeight)");
                count++;
            }
            pageSource = webDriver.getPageSource();
        } catch (Exception e) {
            System.out.println("e.getCause() = " + e.getCause());
            webDriver.quit();
            return;
        }

        if (pageSource == null) {
            webDriver.quit();
            return;
        }
        Document page = Jsoup.parse(pageSource);
        Elements divVideos = page.getElementsByClass("style-scope ytd-video-renderer");
        for (Element divVideo : divVideos) {
            String href = divVideo.getElementsByClass("yt-simple-endpoint style-scope ytd-video-renderer").attr("href");
            String chanel = divVideo.getElementsByClass("yt-simple-endpoint style-scope yt-formatted-string").text();
            String title = divVideo.getElementsByClass("style-scope ytd-video-renderer").text().split(chanel)[0];
            String[] viewsAndDate = divVideo.getElementsByClass("inline-metadata-item style-scope ytd-video-meta-block").text().split(" ");
            if (viewsAndDate.length < 4) continue;
            String views = viewsAndDate[0] + " " + viewsAndDate[1];
            String date = viewsAndDate[2] + " " + viewsAndDate[3];
            if (!title.isEmpty() && !chanel.isEmpty() && !href.isEmpty() && !existsVideos.containsKey(title)) {
                Video video = new Video
                        .Builder()
                        .setHref("https://youtube.com" + href)
                        .setTitle(title)
                        .setViews(views)
                        .setChannel(chanel)
                        .setDate(date)
                        .build();
                videos.put(title, video);
            }
        }
        Map<String, Video> collect = videos.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getDate().contains("days") || entry.getValue().getDate().startsWith("hou"))
                .sorted(Comparator.comparing(value -> value.getValue().getDate()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (x, y) -> x,
                        LinkedHashMap::new
                ));
        collect.forEach((title, video) -> System.out.println(video));
        existsVideos.putAll(collect);
        videos.clear();
        webDriver.quit();
    }

    public static void main(String[] args) throws MalformedURLException {
        Main main = new Main();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    main.extracted();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, 40000);
    }


}