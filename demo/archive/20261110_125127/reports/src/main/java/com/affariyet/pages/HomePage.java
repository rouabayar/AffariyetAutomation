package com.affariyet.pages;

import com.affariyet.config.ConfigReader;
import org.openqa.selenium.WebDriver;

/** Page d'accueil : https://www.affariyet.com/ (body id="index"). */
public class HomePage extends BasePage {

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage open() {
        openUrl(ConfigReader.baseUrl());
        return this;
    }
}
