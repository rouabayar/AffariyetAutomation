package com.affariyet.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Page catégorie, ex. /electromenager-tunisie/lave-linge/ (body id="category"). */
public class CategoryPage extends ProductListPage {

    private static final By HEADING = By.cssSelector(".page-heading.js-category-page");

    public CategoryPage(WebDriver driver) {
        super(driver);
    }

    public String getHeading() {
        return text(HEADING);
    }

    /** Identifiant interne de la catégorie (attribut data-current-category-id). */
    public String getCategoryId() {
        return wait.present(HEADING).getDomAttribute("data-current-category-id");
    }
}
