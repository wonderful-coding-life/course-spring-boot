function Get-BookConfig {
    Get-Content -LiteralPath (Join-Path $PSScriptRoot '../references/book.json') -Raw -Encoding utf8 | ConvertFrom-Json
}

function Get-BookInputRecords([string]$ProjectRoot, $Config) {
    $bookDir = Join-Path $ProjectRoot 'practice-book'
    $paths = [Collections.Generic.List[string]]::new()
    $paths.Add((Join-Path $ProjectRoot $Config.cover))
    foreach ($file in @($Config.preface) + @($Config.chapters)) {
        $path = Join-Path $bookDir $file
        $paths.Add($path)
        $text = [IO.File]::ReadAllText($path)
        foreach ($match in [regex]::Matches($text, '(?m)^!\[[^\]]*\]\(([^)]+)\)')) {
            $paths.Add((Join-Path $bookDir $match.Groups[1].Value))
        }
    }
    foreach ($diagram in $Config.diagrams) { $paths.Add((Join-Path $bookDir $diagram.image)) }
    foreach ($path in @($paths | Sort-Object -Unique)) {
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Missing book input: $path" }
        [pscustomobject]@{
            Path = [IO.Path]::GetRelativePath($ProjectRoot, $path).Replace('\','/')
            Sha256 = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash
        }
    }
}

function Get-MermaidHash([string]$Text) {
    [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData([Text.Encoding]::UTF8.GetBytes($Text.Replace("`r`n","`n").Trim())))
}

function Assert-DiagramMappings([string]$ProjectRoot, $Config) {
    foreach ($file in $Config.chapters) {
        $text = [IO.File]::ReadAllText((Join-Path $ProjectRoot "practice-book/$file")).Replace("`r`n","`n")
        foreach ($block in [regex]::Matches($text, '(?ms)^```([^\n]*)\n(.*?)\n```')) {
            if ($block.Groups[1].Value.Trim() -ne 'mermaid') { continue }
            $hash = Get-MermaidHash $block.Groups[2].Value
            $mapping = @($Config.diagrams | Where-Object { $_.source -eq $file -and $_.mermaidSha256 -eq $hash })
            if ($mapping.Count -ne 1) { throw "Mermaid diagram changed or is unmapped in $file. Render and inspect its image before updating references/book.json." }
            if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot "practice-book/$($mapping[0].image)"))) { throw "Missing diagram image: $($mapping[0].image)" }
        }
    }
}

function Assert-BookInputsUnchanged([string]$ProjectRoot, $Inputs) {
    foreach ($inputFile in $Inputs) {
        $path = Join-Path $ProjectRoot $inputFile.Path
        if (-not (Test-Path -LiteralPath $path) -or (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash -ne $inputFile.Sha256) {
            throw "Book input changed since this release was generated: $($inputFile.Path). Rebuild before publishing."
        }
    }
}
