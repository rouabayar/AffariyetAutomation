# audit-projet.ps1
# Collecte l'état du projet Maven (arborescence, pom.xml, testng.xml, packages Java)
# dans un seul fichier texte : audit-projet.txt
# Lancement (dans le dossier demo) :
#   powershell -ExecutionPolicy Bypass -File .\audit-projet.ps1

$out = Join-Path $PSScriptRoot "audit-projet.txt"
$root = $PSScriptRoot
"" | Out-File $out -Encoding utf8

function Add($text) { $text | Out-File $out -Append -Encoding utf8 }

Add "===== VERSIONS ====="
Add (cmd /c "java -version 2>&1")
Add (cmd /c "mvn -v 2>&1")

Add "`n===== ARBORESCENCE (sans target/.git) ====="
Get-ChildItem -Path $root -Recurse -Force |
    Where-Object { $_.FullName -notmatch '\\(target|\.git|node_modules)(\\|$)' } |
    ForEach-Object { Add ($_.FullName.Substring($root.Length)) }

foreach ($f in @("pom.xml", "testng.xml", "README.md")) {
    $p = Join-Path $root $f
    Add "`n===== $f ====="
    if (Test-Path $p) { Add (Get-Content $p -Raw) } else { Add "(absent)" }
}

Add "`n===== FICHIERS JAVA : chemin + package + classe ====="
Get-ChildItem -Path (Join-Path $root "src") -Recurse -Filter *.java -ErrorAction SilentlyContinue |
    ForEach-Object {
        $pkg = (Select-String -Path $_.FullName -Pattern '^\s*package\s+([\w\.]+)' | Select-Object -First 1).Matches.Groups[1].Value
        Add ("{0}  ->  package {1}" -f $_.FullName.Substring($root.Length), $pkg)
    }

Add "`n===== RESOURCES / test-data ====="
foreach ($d in @("src\test\resources", "src\main\resources", "test-data")) {
    $p = Join-Path $root $d
    if (Test-Path $p) {
        Get-ChildItem $p -Recurse -File | ForEach-Object {
            Add "--- $($_.FullName.Substring($root.Length))"
            if ($_.Length -lt 20000) { Add (Get-Content $_.FullName -Raw) }
        }
    }
}

Add "`n===== COMPILATION (mvn -q test-compile) ====="
Push-Location $root
Add (cmd /c "mvn -q test-compile 2>&1")
Pop-Location

Write-Host "Audit termine -> $out"
