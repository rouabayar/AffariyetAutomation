package com.affariyet.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Pages de contenu (ex. /content/livraison.html, body id="cms"). */
public class CmsPage extends BasePage {

    private static final By CONTENT = By.cssSelector("#content.page-cms");

    public CmsPage(WebDriver driver) {
        super(driver);
        wait.visible(CONTENT);
    }

    public String getContentText() {
        return text(CONTENT);
    }
}
