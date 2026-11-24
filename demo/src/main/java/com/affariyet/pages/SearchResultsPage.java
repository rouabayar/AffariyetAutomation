package com.affariyet.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Résultats de recherche : /recherche?controller=search&s=... (body id="search"). */
public class SearchResultsPage extends ProductListPage {

    /** Bloc affiché quand aucun produit ne correspond (vérifié : section#content.page-not-found h4). */
    private static final By NO_RESULT_MESSAGE = By.cssSelector("#content.page-not-found h4");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isNoResultMessageDisplayed() {
        return isDisplayed(NO_RESULT_MESSAGE);
    }

    public String getNoResultMessage() {
        return text(NO_RESULT_MESSAGE);
    }
}
