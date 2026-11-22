param([Parameter(Mandatory)][string]$WorkDir)
$ErrorActionPreference='Stop'
. (Join-Path $PSScriptRoot 'book-inputs.ps1')
$WorkDir=(Resolve-Path -LiteralPath $WorkDir).Path
$release=Get-Content -LiteralPath (Join-Path $WorkDir 'release.json') -Raw -Encoding utf8 | ConvertFrom-Json
$verification=Get-Content -LiteralPath (Join-Path $WorkDir 'verification.json') -Raw -Encoding utf8 | ConvertFrom-Json
$audit=Get-Content -LiteralPath (Join-Path $WorkDir 'render/layout-audit.json') -Raw -Encoding utf8 | ConvertFrom-Json
if(-not $verification.Passed -or $audit.OutlyingGlyphs -ne 0 -or $audit.Pages -ne $release.Pages){throw 'Incomplete or failed release checks.'}
$hash=(Get-FileHash -LiteralPath $release.DocxPath -Algorithm SHA256).Hash
if($hash -ne $release.DocxSha256 -or $hash -ne $verification.DocxSha256){throw 'Staged DOCX changed after verification. Rebuild and inspect again.'}
Assert-BookInputsUnchanged $release.ProjectRoot $release.Inputs
$config=Get-BookConfig
if($config.output -ne $release.Output){throw 'Release destination changed. Rebuild before publishing.'}
$destination=Join-Path $release.ProjectRoot $release.Output
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $destination) | Out-Null
$replacement=$destination+'.'+[guid]::NewGuid().ToString('N')+'.tmp'
try {
    [IO.File]::Copy($release.DocxPath,$replacement,$false)
    if(Test-Path -LiteralPath $destination){
        $backup=Join-Path $WorkDir ('previous-edition-'+[guid]::NewGuid().ToString('N')+'.docx')
        [IO.File]::Copy($destination,$backup,$false)
        [IO.File]::Replace($replacement,$destination,[NullString]::Value)
    } else {
        [IO.File]::Move($replacement,$destination)
    }
    if((Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash -ne $hash){throw 'Published DOCX checksum mismatch.'}
} finally {
    if(Test-Path -LiteralPath $replacement){Remove-Item -LiteralPath $replacement}
}
Write-Output "Published $($release.Pages) pages: $destination"
