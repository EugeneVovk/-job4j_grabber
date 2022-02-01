package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * По техническому заданию мы должны сделать запрос на сервер,
 * получить HTML и получить данные с сайта - https://www.sql.ru/forum/job-offers
 * jsoup позволяет сделать запрос на сервер и извлечь нужный текст
 * из полученного HTML по атрибутам тегов HTML.
 */
public class SqlRuParse {
    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers").get();
        Elements row = doc.select(".postslisttopic");
        for (Element td : row) {
            Element href = td.child(0);
            System.out.println(href.attr("href"));
            System.out.println(href.text());
            Element parent = td.parent();
            System.out.println(parent.child(5).text());
        }
    }
}
