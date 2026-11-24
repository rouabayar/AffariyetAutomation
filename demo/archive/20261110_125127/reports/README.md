# Tests fonctionnels automatisés — Affariyet

Mini-projet de tests fonctionnels automatisés du site e-commerce tunisien **[Affariyet](https://www.affariyet.com/)**
(PrestaShop, thème ZOneTheme) : **20 cas de test** Selenium WebDriver + TestNG, organisés selon le modèle
**Page Object Model (POM)**.

> Aucun test ne passe de commande, n'effectue de paiement, n'envoie de message ni ne crée de compte.
> Les seules données saisies sont fictives (domaine réservé `example.com`).

---

## 1. Technologies

| Outil | Version |
|---|---|
| Java (JDK) | 17 |
| Selenium WebDriver | 4.27.0 (Selenium Manager télécharge le driver automatiquement) |
| TestNG | 7.10.2 |
| Maven | 3.9.x (Compiler Plugin 3.13.0, Surefire Plugin 3.2.5) |
| IDE | VS Code (Extension Pack for Java) |
| Navigateur | Google Chrome (Edge et Firefox possibles) |

## 2. Installation

1. Installer le **JDK 17** et définir `JAVA_HOME` (vérifier avec `mvn -v` → *Java version: 17*).
2. Installer **Maven** et l'ajouter au `PATH`.
3. Installer **Google Chrome** (à jour).
4. Ouvrir le dossier `demo` dans VS Code.
5. Télécharger les dépendances et compiler :
   ```
   mvn clean test-compile
   ```

## 3. Exécution

| Objectif | Commande |
|---|---|
| Lancer les 20 tests | `mvn clean test` |
| Navigateur invisible | `mvn clean test -Dheadless=true` |
| Autre navigateur | `mvn clean test -Dbrowser=edge` (ou `firefox`) |
| Une seule classe | `mvn test -Dtest=PanierTest` |
| Un seul test | `mvn test -Dtest=PanierTest#tc08_ajoutAuPanier` |

> Quand un test échoue, Maven affiche `BUILD FAILURE` : c'est normal, les rapports sont quand même générés.

### Test TC13 (connexion valide)
Le projet ne contient **aucun vrai mot de passe**. Pour exécuter TC13, créez vous-même un compte de test sur
Affariyet puis définissez ces variables **dans le terminal** avant de lancer Maven (PowerShell) :
```powershell
$env:AFFARIYET_EMAIL="votre.email.de.test@exemple.com"
$env:AFFARIYET_PASSWORD="votre_mot_de_passe"
mvn test -Dtest=CompteTest
```
Sans ces variables, TC13 est **ignoré** (statut *Skipped*) avec un message explicatif.

## 4. Résultats produits (générés à chaque exécution)

| Emplacement | Contenu |
|---|---|
| `reports/rapport-execution.html` | Tableau de bord : nombre de tests réussis / échoués / ignorés, durée, erreur, trace, liens vers les captures |
| `reports/resume-execution.txt` | Même bilan en texte |
| `reports/logs/execution.log` | Traces détaillées de chaque étape |
| `test-output/index.html`, `test-output/emailable-report.html` | Rapports HTML standard de TestNG |
| `test-output/testng-results.xml` | Résultats bruts |
| `screenshots/` | Capture PNG **et** code HTML de la page au moment de chaque échec |

## 5. Architecture

```
demo/
├── pom.xml                     Dépendances et plugins Maven
├── testng.xml                  Suite : 6 classes, 20 tests, 2 listeners
├── README.md
├── test-data/testdata.csv      Données de test (mots-clés, catégories, identifiants fictifs, messages attendus)
├── screenshots/                Preuves des échecs (générées)
├── reports/                    Rapport HTML + logs (générés)
├── test-output/                Rapports TestNG (générés)
└── src/
    ├── main/java/com/affariyet/
    │   ├── config/ConfigReader.java       Lecture de config.properties (+ surcharge -Dcle=valeur)
    │   ├── utils/                         Classes utilitaires communes
    │   │   ├── DriverFactory.java         Création du navigateur (Chrome / Edge / Firefox, headless)
    │   │   ├── WaitUtils.java             Attentes explicites WebDriverWait (dont fin du chargement Pace.js)
    │   │   ├── ScreenshotUtils.java       Capture PNG + HTML
    │   │   ├── CsvDataReader.java         Lecture de testdata.csv
    │   │   ├── PriceUtils.java            Lecture / format des prix « 2 369,000 TND »
    │   │   └── Log.java                   Traces console + fichier
    │   └── pages/                         Page Objects (sélecteurs + actions)
    │       ├── BasePage.java              En-tête, pied de page, clic robuste, recherche, compteur panier
    │       ├── HomePage.java
    │       ├── ProductListPage.java       Commun recherche / catégorie : produits, prix, pagination, tri
    │       ├── SearchResultsPage.java
    │       ├── CategoryPage.java
    │       ├── ProductPage.java
    │       ├── CartPage.java
    │       ├── LoginPage.java
    │       ├── RegistrationPage.java
    │       ├── ContactPage.java
    │       └── CmsPage.java               Pages de contenu (Livraison)
    └── test/
        ├── java/com/affariyet/
        │   ├── base/BaseTest.java         Navigateur neuf par test, capture si échec, fermeture
        │   ├── listeners/
        │   │   ├── TestListener.java      Traces de début / fin / statut
        │   │   └── HtmlReportGenerator.java Rapport reports/rapport-execution.html
        │   └── tests/                     Les 20 scripts de test
        │       ├── AccueilTest.java       TC01–TC02
        │       ├── RechercheTest.java     TC03, TC04, TC19, TC20
        │       ├── CatalogueTest.java     TC05–TC07
        │       ├── PanierTest.java        TC08–TC12
        │       ├── CompteTest.java        TC13–TC16
        │       └── PagesInformationTest.java TC17–TC18
        ├── java/tools/                    Outils d'inspection du site (hors suite de tests)
        └── resources/config.properties    URL, navigateur, délais, dossiers
```

## 6. Les 20 cas de test

| ID | Fonctionnalité | Classe |
|---|---|---|
| TC01 | Accès à la page d'accueil | AccueilTest |
| TC02 | Vérification du titre | AccueilTest |
| TC03 | Recherche d'un produit existant | RechercheTest |
| TC04 | Recherche d'un produit inexistant | RechercheTest |
| TC05 | Navigation entre catégories | CatalogueTest |
| TC06 | Consultation d'un produit | CatalogueTest |
| TC07 | Vérification du prix | CatalogueTest |
| TC08 | Ajout au panier | PanierTest |
| TC09 | Vérification du panier | PanierTest |
| TC10 | Modification de quantité | PanierTest |
| TC11 | Suppression d'un produit | PanierTest |
| TC12 | Panier vide | PanierTest |
| TC13 | Connexion valide (identifiants via variables d'environnement) | CompteTest |
| TC14 | Connexion invalide | CompteTest |
| TC15 | Accès à l'inscription | CompteTest |
| TC16 | Validation des champs obligatoires | CompteTest |
| TC17 | Informations de livraison | PagesInformationTest |
| TC18 | Page contact (sans envoi) | PagesInformationTest |
| TC19 | Navigation entre les pages de produits | RechercheTest |
| TC20 | Tri par prix croissant | RechercheTest |

## 7. Bonnes pratiques appliquées

- **Sélecteurs vérifiés** sur le HTML réel du site (inspections `tools/SiteInspector*.java`).
- **Aucun `Thread.sleep`** : uniquement des attentes explicites (`WebDriverWait`).
- Attente de la fin de la barre de chargement **Pace.js** (`body.pace-running`), qui interceptait les clics.
- **Un navigateur neuf par test** : tests indépendants, panier toujours vide au départ.
- Données externalisées dans `test-data/testdata.csv`, configuration dans `config.properties`.
- Aucun vrai mot de passe stocké dans le projet.

## 8. Limites connues

- Le site est en production : les produits, prix et stocks changent. Les tests vérifient donc des **règles**
  (ex. le prix est identique entre la liste et la fiche) plutôt que des valeurs fixes.
- TC13 nécessite un compte de test personnel (sinon il est ignoré).
- Les performances du site (chargement, AJAX) peuvent faire varier la durée d'exécution.
