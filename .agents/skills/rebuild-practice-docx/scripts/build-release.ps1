param(
    [string]$ProjectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../../..')),
    [string]$WorkDir = (Join-Path ([IO.Path]::GetTempPath()) ('practice-docx-'+(Get-Date -Format 'yyyyMMdd-HHmmss')+'-'+[guid]::NewGuid().ToString('N').Substring(0,8)))
)
$ErrorActionPreference='Stop'
if(-not $IsWindows){throw 'This project publisher requires Windows PowerShell 7 and Microsoft Word.'}
. (Join-Path $PSScriptRoot 'book-inputs.ps1')
$ProjectRoot=(Resolve-Path -LiteralPath $ProjectRoot).Path
$WorkDir=[IO.Path]::GetFullPath($WorkDir)
if(Test-Path -LiteralPath $WorkDir){throw 'Use a new work directory so that stale renderings cannot pass verification.'}
$config=Get-BookConfig
Assert-DiagramMappings $ProjectRoot $config
$inputs=@(Get-BookInputRecords $ProjectRoot $config)
New-Item -ItemType Directory -Path $WorkDir | Out-Null
$docxPath=Join-Path $WorkDir ([IO.Path]::GetFileName($config.output))
& (Join-Path $PSScriptRoot 'build-docx.ps1') -ProjectRoot $ProjectRoot -WorkDir $WorkDir
& (Join-Path $PSScriptRoot 'update-fields.ps1') -DocxPath $docxPath -WorkDir $WorkDir
& (Join-Path $PSScriptRoot 'verify-docx.ps1') -ProjectRoot $ProjectRoot -DocxPath $docxPath -WorkDir $WorkDir
$renderDir=Join-Path $WorkDir 'render'
& (Join-Path $PSScriptRoot 'render-pages.ps1') -PdfPath (Join-Path $WorkDir 'book-preview.pdf') -OutputDir $renderDir
$audit=Get-Content -LiteralPath (Join-Path $renderDir 'layout-audit.json') -Raw -Encoding utf8 | ConvertFrom-Json
$stats=Get-Content -LiteralPath (Join-Path $WorkDir 'word-stats.json') -Raw -Encoding utf8 | ConvertFrom-Json
if($audit.Pages -ne $stats.Pages){throw 'Word and rendered PDF page counts differ.'}
if($audit.OutlyingGlyphs -ne 0){throw 'Inspect and resolve out-of-page text before publishing.'}
Assert-BookInputsUnchanged $ProjectRoot $inputs
[pscustomobject]@{
    ProjectRoot=$ProjectRoot;WorkDir=$WorkDir;DocxPath=$docxPath;Output=$config.output
    Pages=$stats.Pages;DocxSha256=(Get-FileHash -LiteralPath $docxPath -Algorithm SHA256).Hash
    Inputs=$inputs;Created=(Get-Date).ToString('o')
} | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $WorkDir 'release.json') -Encoding utf8
Write-Output "Ready for visual review: $WorkDir"
Write-Output "DOCX pages: $($stats.Pages). Inspect render/page-*.png before running publish-release.ps1."

