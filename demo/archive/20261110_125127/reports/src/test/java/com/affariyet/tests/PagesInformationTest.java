package com.affariyet.tests;

import com.affariyet.base.BaseTest;
import com.affariyet.pages.CmsPage;
import com.affariyet.pages.ContactPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/** TC17 – TC18 : pages d'information. Aucun message n'est envoyé. */
public class PagesInformationTest extends BaseTest {

    @Test(priority = 17, description = "TC17 - Consultation des informations de livraison")
    public void tc17_informationsLivraison() {
        CmsPage delivery = openHomePage().goToDeliveryFromFooter();

        Assert.assertEquals(delivery.getBodyId(), "cms", "La page de contenu n'est pas affichée");
        Assert.assertTrue(delivery.getCurrentUrl().contains("livraison"), "URL inattendue : " + delivery.getCurrentUrl());
        Assert.assertEquals(delivery.getBreadcrumbLast(), "Livraison", "Fil d'Ariane inattendu");
        String content = delivery.getContentText();
        Assert.assertTrue(content.contains(data("livraison.texte.1")), "« Frais de livraison » absent de la page");
        Assert.assertTrue(content.contains(data("livraison.texte.2")), "« Délais de livraison » absent de la page");
    }

    @Test(priority = 18, description = "TC18 - Consultation de la page contact")
    public void tc18_pageContact() {
        ContactPage contact = openHomePage().goToContactFromMenu();

        // Étape 1 : la page et ses champs
        Assert.assertEquals(contact.getBodyId(), "contact", "La page contact n'est pas affichée");
        Assert.assertTrue(contact.areFieldsDisplayed(), "Sujet / e-mail / message non affichés");
        Assert.assertTrue(contact.getSubjects().contains("Service client"), "Sujet « Service client » absent");
        Assert.assertTrue(contact.getContactInfoText().contains("contact@affariyet.com"), "E-mail de contact absent");

        // Étape 2 : « Envoyer » reste désactivé sans consentement, même avec le formulaire rempli
        contact.fillForm(data("contact.email"), data("contact.message"));
        Assert.assertFalse(contact.isSubmitEnabled(), "« Envoyer » devrait être désactivé sans consentement");

        // Étape 3 : après consentement, le bouton s'active (on ne clique PAS dessus)
        contact.acceptConsent();
        Assert.assertTrue(contact.isConsentChecked(), "La case de consentement n'est pas cochée");
        Assert.assertTrue(contact.isSubmitEnabled(), "« Envoyer » devrait être activé après consentement");
    }
}
