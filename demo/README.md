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

### 3.1 Exécution de référence (recommandée, PowerShell)
Dans le dossier `demo`, **une commande par ligne** :
```powershell
powershell -ExecutionPolicy Bypass -File .\archiver-resultats.ps1   # 1. archive les anciens résultats
powershell -ExecutionPolicy Bypass -File .\lancer-tests.ps1         # 2. exécute les 20 tests
```
- `archiver-resultats.ps1` déplace le contenu de `screenshots\`, `reports\` et `test-output\` dans
  `archive\<date_heure>\` (**rien n'est supprimé**) : les preuves de l'exécution suivante ne se mélangent pas aux anciennes.
- `lancer-tests.ps1` passe la console et Maven en UTF-8, lance `mvn -ntp clean test` et **transmet le vrai résultat
  de Maven** (`BUILD FAILURE` et code de sortie ≠ 0 si un test échoue).
- Il enregistre aussi un **journal UTF-8 de la console** dans `reports\logs\console.txt`, à joindre plutôt qu'un
  copier-coller de la fenêtre PowerShell (qui recolle les lignes atteignant exactement la largeur de la fenêtre).
  Ce journal est exploitable mais n'est pas une copie octet par octet : les lignes sont relues par PowerShell puis
  réécrites en UTF-8, et l'ordre relatif des rares lignes écrites sur la sortie d'erreur peut légèrement différer.
  Si le fichier ne peut pas être écrit, un message l'indique et l'exécution continue avec le même code de sortie.

### 3.2 Autres commandes
| Objectif | Commande |
|---|---|
| Lancer les 20 tests (sans le script) | `mvn clean test` |
| Navigateur invisible | `mvn clean test "-Dheadless=true"` |
| Autre navigateur | `mvn clean test "-Dbrowser=edge"` (ou `firefox`) |
| Une seule classe | `mvn test "-Dtest=PanierTest"` |
| Un seul test | `mvn test "-Dtest=PanierTest#tc08_ajoutAuPanier"` |
| Réafficher les avertissements CDP de Selenium | `mvn clean test "-Dselenium.cdp.avertissements=true"` |

> Avec `-Dtest=…`, Surefire **n'utilise pas `testng.xml`** : ni le découpage en 3 blocs ni les listeners du projet
> (pas de `rapport-execution.html`). Pour l'exécution de référence, utiliser `lancer-tests.ps1` ou `mvn clean test`.

### 3.3 Ordre d'exécution TC01 → TC20
Avec TestNG 7, `priority` n'ordonne les méthodes qu'**à l'intérieur d'une classe**, et les classes s'exécutent dans
l'ordre de `testng.xml`. `RechercheTest` contenant TC03, TC04, TC19 et TC20, `testng.xml` découpe la suite en
**3 blocs `<test>` exécutés l'un après l'autre** (chaque méthode n'y figure qu'une fois : 20 tests) :

| Bloc | Classes / méthodes | Tests |
|---|---|---|
| 1. Accueil et recherche | `AccueilTest`, `RechercheTest` (tc03, tc04) | TC01–TC04 |
| 2. Catalogue, panier, compte et informations | `CatalogueTest`, `PanierTest`, `CompteTest`, `PagesInformationTest` | TC05–TC18 |
| 3. Pagination et tri | `RechercheTest` (tc19, tc20) | TC19–TC20 |

Les tests sont indépendants (navigateur neuf par test, aucune dépendance) : l'ordre n'influence pas les résultats.

### 3.4 Test TC13 (connexion valide)
Le projet ne contient **aucun identifiant réel**. Pour exécuter TC13, créez vous-même un compte de test sur Affariyet,
puis définissez ces variables **dans la même fenêtre PowerShell**, juste avant l'exécution :
```powershell
$env:AFFARIYET_EMAIL="votre.email.de.test@exemple.com"
$env:AFFARIYET_PASSWORD="votre_mot_de_passe"
powershell -ExecutionPolicy Bypass -File .\lancer-tests.ps1
```
Sans ces variables, TC13 est **ignoré** (*Skipped*) avec son motif. L'e-mail est masqué dans les traces (`r***@domaine`)
et le mot de passe n'est jamais écrit.

### 3.5 Lire le terminal
| Message | Signification |
|---|---|
| `==== TC03 - … ====` puis `[ETAPE] [TC03] …` | Début du test et étapes, préfixées par l'identifiant du test |
| `[INFO ] [TC03] RÉUSSI (10,0 s)` | Test réussi |
| `[ERREUR] [TC18] ÉCHOUÉ (12,1 s) — …` | **Échec réel** : cause en clair (trace complète dans `reports\logs\execution.log`) |
| `[INFO ] [TC13] IGNORÉ — …` | Test non exécuté, avec son motif |
| `[AVERT] [TC04] Pace.js toujours actif 20 s après le chargement de …` | **Avertissement**, pas un échec : la page est chargée (`readyState = complete`) mais la barre de chargement Pace.js du site tourne encore. Un seul avertissement par page ; sur cette page, Pace n'est plus vérifié et le test continue avec ses attentes bloquantes (champ cliquable, URL, page de résultats…) et ses assertions |
| `RÉUSSI (… s) — avec 1 avertissement` | Test réussi (statut inchangé) au cours duquel un avertissement a été émis |
| `BILAN GLOBAL : …` | Bilan unique de la suite : totaux, échecs, tests ignorés, **avertissements (non comptés comme échecs)**, chemins des rapports |
| `[ERROR] … <<< FAILURE!` (Maven) | Niveau de journal de Maven pour un test échoué (ce n'est pas le compteur `Errors`) |
| `Tests run / Failures / Errors / Skipped` | `Failures` : tests échoués (avec TestNG, toute exception d'un test est une *Failure*) ; `Errors` : incident d'infrastructure (presque toujours 0) ; `Skipped` : tests ignorés |
| `BUILD FAILURE` | Résultat **normal** dès qu'au moins un test échoue : il n'est jamais masqué |
| `Note : avertissements « CDP / DevTools » … filtrés` | Selenium 4.27 (version imposée) ne contient pas les modules DevTools de Chrome 156 ; le projet n'utilise pas DevTools. Seuls les 2 composants Selenium concernés sont filtrés |

Corrections appliquées pour un terminal lisible :
- **SLF4J** : `slf4j-nop` 1.7.36 (portée `test`, même version que le `slf4j-api` apporté par TestNG 7.10.2,
  vérifiée par `mvn dependency:tree "-Dincludes=org.slf4j"`) supprime l'avertissement `StaticLoggerBinder`.
- **Accents** : JVM de test en UTF-8 (`-Dsun.stdout.encoding`, propriété reconnue par Java 17) et console en UTF-8
  (`lancer-tests.ps1`). Sans le script, taper d'abord `chcp 65001` dans PowerShell.
- **API dépréciée** : les outils `tools/SiteInspector*.java` utilisent `getDomProperty()` au lieu de `getAttribute()`.
- **Traces** : `trimStackTrace` raccourcit les traces dans la console Maven ; elles restent complètes dans
  `test-output\`, `reports\rapport-execution.html` et `reports\logs\execution.log`.

### 3.6 Outil de diagnostic de TC20 (hors suite)
`tools/Tc20Diagnostic.java` observe pendant 60 s le comportement du tri « Prix, croissant » (URL, indicateurs de
chargement, prix, requêtes AJAX, console du navigateur) puis ouvre directement l'URL triée. Il **ne fait pas partie des
20 tests** et ne modifie ni TC20 ni ses résultats :
```powershell
mvn test -Pdiagnostic-tc20
```
- Le profil Maven `diagnostic-tc20` (aussi activé par `mvn test "-Dtest=Tc20Diagnostic"`) lance **uniquement**
  `Tc20Diagnostic` : `testng.xml`, les 20 tests et leurs listeners ne sont pas chargés.
- Chaque lancement crée un **nouveau dossier** `diagnostics\tc20\<aaaaMMjj_HHmmss>_UTC\` (heure UTC de Maven) :
  `diagnostic.txt`, captures `A-00s`, `A-20s`, `A-60s`, `B-url-directe` (`.png` + `.html`), `surefire\` (rapports
  Surefire/TestNG du diagnostic) et `journal\logs\execution.log`.
- **Rien n'est écrit** dans `reports\`, `screenshots\` ni `test-output\`, et un dossier de diagnostic existant n'est
  jamais réutilisé. Sans le profil, l'outil s'arrête avant d'ouvrir le navigateur.
- `archiver-resultats.ps1` ne touche pas au dossier `diagnostics\`.

## 4. Résultats produits (générés à chaque exécution)

| Emplacement | Contenu |
|---|---|
| `reports/rapport-execution.html` | Tableau de bord : nombre de tests réussis / échoués / ignorés, avertissements (comptés à part), durée, erreur, trace, liens vers les captures |
| `reports/resume-execution.txt` | Même bilan en texte |
| `reports/logs/execution.log` | Traces détaillées de chaque étape |
| `test-output/index.html`, `test-output/emailable-report.html` | Rapports HTML standard de TestNG |
| `test-output/testng-results.xml` | Résultats bruts |
| `screenshots/` | Capture PNG **et** code HTML de la page au moment de chaque échec |
| `reports/logs/console.txt` | Journal UTF-8 de la console de la dernière exécution par `lancer-tests.ps1` |
| `diagnostics/tc20/<date_heure>_UTC/` | Résultats de l'outil de diagnostic de TC20 (un dossier par lancement, hors résultats de la suite) |
| `archive/<date_heure>/` | Résultats des exécutions précédentes, déplacés par `archiver-resultats.ps1` |

## 5. Architecture

```
demo/
├── pom.xml                     Dépendances et plugins Maven
├── testng.xml                  Suite : 6 classes, 20 tests, 3 blocs d'exécution, 2 listeners
├── README.md
├── lancer-tests.ps1            Exécution de référence (UTF-8, vrai résultat Maven)
├── archiver-resultats.ps1      Archive les anciens résultats dans archive\<date_heure>\
├── test-data/testdata.csv      Données de test (mots-clés, catégories, identifiants fictifs, messages attendus)
├── screenshots/                Preuves des échecs (générées)
├── reports/                    Rapport HTML + logs (générés)
├── test-output/                Rapports TestNG (générés)
└── src/
    ├── main/java/com/affariyet/
    │   ├── config/ConfigReader.java       Lecture de config.properties (+ surcharge -Dcle=valeur)
    │   ├── utils/                         Classes utilitaires communes
    │   │   ├── DriverFactory.java         Création du navigateur (Chrome / Edge / Firefox, headless)
    │   │   ├── WaitUtils.java             Attentes explicites WebDriverWait (chargement, Pace.js, recherche)
    │   │   ├── Avertissements.java        Avertissements (non comptés comme échecs) pour le bilan et le rapport
    │   │   ├── UrlUtils.java              Lecture décodée des paramètres d'URL (mot-clé de recherche)
    │   │   ├── ScreenshotUtils.java       Capture PNG + HTML
    │   │   ├── CsvDataReader.java         Lecture de testdata.csv
    │   │   ├── PriceUtils.java            Lecture / format des prix « 2 369,000 TND »
    │   │   ├── ContentChecks.java         Règles de conformité du contenu (anglais, Lorem Ipsum)
    │   │   └── Log.java                   Traces console ([TCxx], [AVERT]) + fichier détaillé
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
        │   │   ├── TestListener.java      Statut de chaque test + bilan global unique
        │   │   └── HtmlReportGenerator.java Rapport reports/rapport-execution.html
        │   └── tests/                     Les 20 scripts de test
        │       ├── AccueilTest.java       TC01–TC02
        │       ├── RechercheTest.java     TC03, TC04, TC19, TC20
        │       ├── CatalogueTest.java     TC05–TC07
        │       ├── PanierTest.java        TC08–TC12
        │       ├── CompteTest.java        TC13–TC16
        │       └── PagesInformationTest.java TC17–TC18
        ├── java/tools/                    Hors suite : SiteInspector, SiteInspectorEtape2, Tc20Diagnostic
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
| TC18 | Page contact (sans envoi) + conformité du contenu : libellés en français, consentement sans Lorem Ipsum | PagesInformationTest |
| TC19 | Navigation entre les pages de produits | RechercheTest |
| TC20 | Tri par prix croissant | RechercheTest |

## 7. Bonnes pratiques appliquées

- **Sélecteurs vérifiés** sur le HTML réel du site (inspections `tools/SiteInspector*.java`).
- **Aucun `Thread.sleep` dans les tests** : uniquement des attentes explicites (`WebDriverWait`). Seul l'outil de
  diagnostic `Tc20Diagnostic` relève l'état de la page à intervalle fixe (2 s), ce qui est son objet.
- Saisie robuste : attente de la fin du chargement et d'un champ réellement utilisable avant d'écrire.
- Vérifications groupées par `SoftAssert` (TC18) : toutes les anomalies d'une page sont signalées en une fois.
- Attente de la fin de la barre de chargement **Pace.js** (`body.pace-running`), qui interceptait les clics.
  Si Pace tourne encore 20 s après le chargement complet d'une page, un **avertissement** est émis (une fois par page)
  au lieu d'un échec : l'état réel est tracé, et le test reste soumis à ses attentes bloquantes et à ses assertions.
- Recherche vérifiée : le paramètre `s` de l'URL, décodé (« + », « %20 », « %C3%A9 »…), doit être exactement le
  mot-clé saisi, et la page affichée doit être la page de résultats (`body#search`).
- **Un navigateur neuf par test** : tests indépendants, panier toujours vide au départ.
- Données externalisées dans `test-data/testdata.csv`, configuration dans `config.properties`.
- Aucun vrai mot de passe stocké dans le projet.

## 8. Limites connues

- Le site est en production : les produits, prix et stocks changent. Les tests vérifient donc des **règles**
  (ex. le prix est identique entre la liste et la fiche) plutôt que des valeurs fixes.
- TC13 nécessite un compte de test personnel (sinon il est ignoré).
- Les performances du site (chargement, AJAX) peuvent faire varier la durée d'exécution.
