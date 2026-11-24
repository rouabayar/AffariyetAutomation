# archiver-resultats.ps1
# Déplace les résultats des exécutions précédentes dans archive\<date-heure>\ (RIEN n'est supprimé),
# pour que screenshots\, reports\ et test-output\ ne contiennent que l'exécution de référence suivante.
# Utilisation (dans le dossier demo) :
#   powershell -ExecutionPolicy Bypass -File .\archiver-resultats.ps1

$racine = $PSScriptRoot
$horodatage = Get-Date -Format "yyyyMMdd_HHmmss"
$destination = Join-Path $racine (Join-Path "archive" $horodatage)
$dossiers = @("screenshots", "reports", "test-output")
$deplaces = 0

foreach ($nom in $dossiers) {
    $source = Join-Path $racine $nom
    if (-not (Test-Path $source)) { continue }
    $elements = Get-ChildItem -Path $source -Force | Where-Object { $_.Name -ne ".gitkeep" }
    if ($elements.Count -eq 0) { continue }
    $cible = Join-Path $destination $nom
    New-Item -ItemType Directory -Path $cible -Force | Out-Null
    foreach ($e in $elements) {
        Move-Item -Path $e.FullName -Destination $cible -Force
        $deplaces++
    }
    Write-Host ("{0,-12} -> archive\{1}\{0}  ({2} élément(s))" -f $nom, $horodatage, $elements.Count)
}

foreach ($nom in $dossiers) {
    $d = Join-Path $racine $nom
    New-Item -ItemType Directory -Path $d -Force | Out-Null
}

if ($deplaces -eq 0) {
    Write-Host "Aucun ancien résultat à archiver."
} else {
    Write-Host "Archivage terminé : $deplaces élément(s) déplacé(s) dans $destination"
}
