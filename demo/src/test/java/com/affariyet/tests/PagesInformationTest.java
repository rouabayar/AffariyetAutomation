package com.affariyet.tests;

import com.affariyet.base.BaseTest;
import com.affariyet.pages.CmsPage;
import com.affariyet.pages.ContactPage;
import com.affariyet.utils.ContentChecks;
import com.affariyet.utils.Log;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.List;

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

        // Étape 4 : conformité du contenu du formulaire.
        // SoftAssert : les deux vérifications s'exécutent toujours, et toutes les anomalies sont listées ensemble.
        SoftAssert conformite = new SoftAssert();

        // 4a. Langue : libellés et mentions du formulaire cohérents avec le français du site
        String fileComment = contact.getFileUploadComment();
        Log.info("Mention lue pour « Document joint » : \"" + fileComment + "\"");
        List<String> fieldTexts = new ArrayList<>(contact.getFieldLabels());
        fieldTexts.addAll(contact.getFieldComments());
        Log.info("Libellés et mentions lus dans le formulaire : " + fieldTexts);
        List<String> nonFrench = new ArrayList<>();
        for (String t : fieldTexts) {
            if (!ContentChecks.findEnglishUiTerms(t).isEmpty()) {
                nonFrench.add("\"" + t + "\"" + (t.equals(fileComment) ? " (champ Document joint)" : ""));
            }
        }
        conformite.assertTrue(nonFrench.isEmpty(),
                "[Langue] Libellé non français dans le formulaire de contact : " + String.join(", ", nonFrench));

        // 4b. Consentement : texte compréhensible, pas de texte de remplissage Lorem Ipsum
        String consent = contact.getConsentText();
        Log.info("Texte de consentement lu : \"" + consent + "\"");
        conformite.assertFalse(consent.isEmpty(), "[Consentement] Aucun texte n'accompagne la case de consentement");
        List<String> lorem = ContentChecks.findLoremIpsumWords(consent);
        conformite.assertTrue(lorem.size() < ContentChecks.LOREM_IPSUM_THRESHOLD,
                "[Consentement] Texte de remplissage (Lorem Ipsum) : \"" + consent + "\" — mots détectés : " + lorem);

        conformite.assertAll();
    }
}
