param(
    [Parameter(Mandatory)][string]$PdfPath,
    [Parameter(Mandatory)][string]$OutputDir
)
$ErrorActionPreference='Stop'
$cache=Join-Path $env:USERPROFILE '.gradle/caches/modules-2/files-2.1'
$pdfboxRoot=Join-Path $cache 'org.apache.pdfbox/pdfbox'
$version=Get-ChildItem -LiteralPath $pdfboxRoot -Directory -ErrorAction SilentlyContinue |
    Where-Object Name -match '^3\.\d+\.\d+$' | Sort-Object { [version]$_.Name } -Descending | Select-Object -First 1 -ExpandProperty Name
if(-not $version){throw 'PDFBox 3 is not cached. Prepare a PDF renderer without changing project Gradle dependencies; see SKILL.md.'}
$jars=@(foreach($artifact in @('pdfbox','pdfbox-io','fontbox')) {
    $artifactPath=Join-Path $cache "org.apache.pdfbox/$artifact/$version"
    $jar=Get-ChildItem -LiteralPath $artifactPath -Recurse -File -Filter "$artifact-$version.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
    if(-not $jar){throw "Missing PDFBox runtime dependency: $artifact $version"}
    $jar.FullName
})
$logging=Get-ChildItem -LiteralPath (Join-Path $cache 'commons-logging/commons-logging') -Recurse -File -Filter 'commons-logging-*.jar' -ErrorAction SilentlyContinue |
    Where-Object Name -notmatch '(sources|javadoc)' | Select-Object -First 1
if(-not $logging){throw 'Missing cached commons-logging runtime for PDFBox.'}
$jars+=$logging.FullName
$java=Get-Command java -ErrorAction Stop
& $java.Source --class-path ($jars -join [IO.Path]::PathSeparator) (Join-Path $PSScriptRoot 'InspectBook.java') $PdfPath $OutputDir
if($LASTEXITCODE -ne 0){throw "PDF rendering failed with exit code $LASTEXITCODE"}

