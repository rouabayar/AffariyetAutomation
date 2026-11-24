# lancer-tests.ps1
# Exécution de référence des 20 tests avec un affichage lisible dans PowerShell :
#  - console et JVM Maven en UTF-8 (accents et tirets affichés correctement) ;
#  - option -ntp : pas de lignes de progression des téléchargements ;
#  - le vrai résultat Maven est conservé (BUILD FAILURE si des tests échouent, code de sortie transmis) ;
#  - journal UTF-8 de la console dans reports\logs\console.txt.
# Utilisation (dans le dossier demo) :
#   powershell -ExecutionPolicy Bypass -File .\lancer-tests.ps1
# Arguments supplémentaires transmis à Maven, ex. :
#   powershell -ExecutionPolicy Bypass -File .\lancer-tests.ps1 "-Dselenium.cdp.avertissements=true"

chcp 65001 > $null
$utf8 = New-Object System.Text.UTF8Encoding($false)   # UTF-8 sans BOM, défini explicitement
[Console]::OutputEncoding = $utf8
$OutputEncoding = $utf8
$env:MAVEN_OPTS = "-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8"
$ErrorActionPreference = "Continue"   # les lignes écrites par Maven sur stderr ne doivent pas interrompre le script

# Information sur TC13 (aucune valeur affichée)
if ([string]::IsNullOrWhiteSpace($env:AFFARIYET_EMAIL) -or [string]::IsNullOrWhiteSpace($env:AFFARIYET_PASSWORD)) {
    Write-Host "Info : AFFARIYET_EMAIL / AFFARIYET_PASSWORD non définis -> TC13 (connexion valide) sera IGNORÉ."
} else {
    Write-Host "Info : identifiants de test détectés -> TC13 (connexion valide) sera exécuté."
}

Set-Location $PSScriptRoot

# Journal UTF-8 de la console : reports\logs\console.txt (exploitable sans les recollements du copier-coller
# de la fenêtre PowerShell). Ce n'est pas une copie octet par octet : les lignes sont relues par PowerShell
# puis réécrites en UTF-8. Si le fichier ne peut pas être écrit, l'exécution continue et le code de sortie
# de Maven reste celui transmis.
$code = 1          # valeur par défaut si Maven ne peut pas être lancé (ex. mvn introuvable)
$writer = $null
$consoleFile = Join-Path $PSScriptRoot "reports\logs\console.txt"
try {
    New-Item -ItemType Directory -Force -Path (Split-Path $consoleFile) | Out-Null
    $writer = New-Object System.IO.StreamWriter($consoleFile, $false, $utf8)
    $writer.AutoFlush = $true
} catch {
    Write-Host ("Avertissement : journal {0} non créé ({1}) ; les tests sont lancés quand même." -f $consoleFile, $_.Exception.Message)
    $writer = $null
}

function Write-Journal([string]$ligne) {
    if ($null -ne $script:writer) {
        try { $script:writer.WriteLine($ligne) }
        catch {
            Write-Host ("Avertissement : écriture de console.txt interrompue ({0})." -f $_.Exception.Message)
            $script:writer = $null
        }
    }
}

try {
    & mvn -ntp clean test @args 2>&1 | ForEach-Object {
        $ligne = "$_"
        Write-Host $ligne
        Write-Journal $ligne
    }
    $code = $LASTEXITCODE      # code de sortie réel de Maven (0 = BUILD SUCCESS)
} catch {
    Write-Host ("Erreur au lancement de Maven : {0}" -f $_.Exception.Message)
} finally {
    $bilan = "Code de sortie Maven : {0} ({1})" -f $code, $(if ($code -eq 0) { "BUILD SUCCESS" } else { "BUILD FAILURE : voir le bilan ci-dessus et reports\rapport-execution.html" })
    Write-Host $bilan
    Write-Journal $bilan
    if ($null -ne $writer) {
        try { $writer.Close() } catch { }
        Write-Host ("Journal de la console : {0}" -f $consoleFile)
    }
}
exit $code
