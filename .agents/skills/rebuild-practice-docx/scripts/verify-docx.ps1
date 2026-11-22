param(
    [Parameter(Mandatory)][string]$ProjectRoot,
    [Parameter(Mandatory)][string]$DocxPath,
    [Parameter(Mandatory)][string]$WorkDir
)
$ErrorActionPreference='Stop'
. (Join-Path $PSScriptRoot 'book-inputs.ps1')
$config=Get-BookConfig
Add-Type -AssemblyName System.IO.Compression.FileSystem
$root=(Resolve-Path -LiteralPath $ProjectRoot).Path
$path=(Resolve-Path -LiteralPath $DocxPath).Path
$manifest=Get-Content -LiteralPath (Join-Path $WorkDir 'manifest.json') -Raw -Encoding utf8 | ConvertFrom-Json
$zip=[System.IO.Compression.ZipFile]::OpenRead($path)
try {
    function ReadPart([string]$part){$reader=[System.IO.StreamReader]::new($zip.GetEntry($part).Open());try{[xml]$reader.ReadToEnd()}finally{$reader.Dispose()}}
    $xml=ReadPart 'word/document.xml';$styles=ReadPart 'word/styles.xml'
    $ns=[System.Xml.XmlNamespaceManager]::new($xml.NameTable);$ns.AddNamespace('w','http://schemas.openxmlformats.org/wordprocessingml/2006/main')
    $w=$ns.LookupNamespace('w')
    $codeStyle=$styles.SelectSingleNode('//w:style[w:name/@w:val="실습 코드"]',$ns).GetAttribute('styleId',$w)
    $nodes=@($xml.SelectNodes('//w:p[w:pPr/w:pStyle/@w:val="'+$codeStyle+'"]',$ns))
    $source=[System.Collections.Generic.List[string]]::new()
    foreach($file in $config.chapters) {
        $body=[System.IO.File]::ReadAllText((Join-Path $root ('practice-book/'+$file))).Replace("`r`n","`n")
        foreach($match in [regex]::Matches($body,'(?ms)^```([^\n]*)\n(.*?)\n```')) {
            if ($match.Groups[1].Value -ne 'mermaid') { $source.Add($match.Groups[2].Value.Replace("`t",'    ')) }
        }
    }
    if($nodes.Count -ne $source.Count){throw "Code block count mismatch: $($nodes.Count) / $($source.Count)"}
    for($i=0;$i -lt $nodes.Count;$i++) {
        $s=[System.Text.StringBuilder]::new()
        foreach($node in $nodes[$i].SelectNodes('.//w:t|.//w:br|.//w:tab',$ns)) {
            if($node.LocalName -eq 'br'){[void]$s.Append("`n")}elseif($node.LocalName -eq 'tab'){[void]$s.Append("`t")}else{[void]$s.Append($node.InnerText)}
        }
        if($s.ToString() -ne $source[$i]){throw ('Code content mismatch at block '+($i+1))}
    }
    $pictureCount=$xml.SelectNodes('//w:drawing',$ns).Count
    $headingCount=$xml.SelectNodes('//w:p[w:pPr/w:pStyle/@w:val="Heading1" or w:pPr/w:pStyle/@w:val="Heading2" or w:pPr/w:pStyle/@w:val="Heading3"]',$ns).Count
    $tocEntries=([regex]::Split([System.IO.File]::ReadAllText((Join-Path $WorkDir 'toc.txt')).Trim(),'\r\n|\r|\n')).Count
    $expectedEntries=$xml.SelectNodes('//w:p[w:pPr/w:pStyle/@w:val="Heading1" or w:pPr/w:pStyle/@w:val="Heading2"]',$ns).Count
    if($tocEntries -ne $expectedEntries){throw "TOC mismatch: $tocEntries / $expectedEntries"}
    if($xml.OuterXml.Contains('__BOOK_TOC__')){throw 'Unresolved TOC token'}
    $fields=($xml.SelectNodes('//w:instrText',$ns)|ForEach-Object InnerText)-join ' '
    if($fields -notmatch 'TOC '){throw 'Missing TOC field'}
    if($headingCount -ne $manifest.Headings.Count){throw 'Heading count differs from Markdown'}
    $actualHeadings=@($xml.SelectNodes('//w:p[w:pPr/w:pStyle/@w:val="Heading1" or w:pPr/w:pStyle/@w:val="Heading2" or w:pPr/w:pStyle/@w:val="Heading3"]',$ns))
    for($i=0;$i -lt $actualHeadings.Count;$i++) {
        $headingText=($actualHeadings[$i].SelectNodes('.//w:t',$ns)|ForEach-Object InnerText)-join ''
        if($headingText -ne $manifest.Headings[$i].Text){throw "Heading differs from Markdown: $headingText"}
    }
    $tableCount=$xml.SelectNodes('//w:body//w:tbl',$ns).Count
    if($tableCount -ne $manifest.Tables){throw 'Table count differs from Markdown'}
    $mediaHashes=@($zip.Entries|Where-Object FullName -like 'word/media/*'|ForEach-Object {
        $stream=$_.Open();try{[Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($stream))}finally{$stream.Dispose()}
    })
    foreach($asset in $manifest.Media) {
        if($mediaHashes -notcontains (Get-FileHash -LiteralPath $asset.Path -Algorithm SHA256).Hash){throw "Original image missing or altered: $($asset.Path)"}
    }
    $coverZip=[IO.Compression.ZipFile]::OpenRead((Join-Path $root $config.cover))
    try {
        $coverReader=[IO.StreamReader]::new($coverZip.GetEntry('word/document.xml').Open())
        try{[xml]$coverXml=$coverReader.ReadToEnd()}finally{$coverReader.Dispose()}
        $coverText=($coverXml.SelectNodes('//w:body//w:t',$ns)|ForEach-Object InnerText)-join ''
        $actualCoverText=[Text.StringBuilder]::new()
        foreach($paragraph in $xml.SelectNodes('//w:body/w:p',$ns)) {
            if($paragraph.SelectSingleNode('w:pPr/w:sectPr',$ns)){break}
            [void]$actualCoverText.Append(($paragraph.SelectNodes('.//w:t',$ns)|ForEach-Object InnerText)-join '')
        }
        if($coverText -ne $actualCoverText.ToString()){throw 'Cover text differs from original cover'}
        $coverPictures=$coverXml.SelectNodes('//w:drawing',$ns).Count
        if($pictureCount -ne $manifest.Images+$coverPictures){throw 'Inline picture count mismatch'}
        foreach($entry in $coverZip.Entries|Where-Object FullName -like 'word/media/*') {
            $stream=$entry.Open();try{$hash=[Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($stream))}finally{$stream.Dispose()}
            if($mediaHashes -notcontains $hash){throw 'Original cover image changed'}
        }
    } finally{$coverZip.Dispose()}
    if(-not $xml.SelectSingleNode('//w:sectPr/w:pgNumType[@w:fmt="lowerRoman" and @w:start="1"]',$ns)){throw 'Missing Roman front-matter page numbering'}
    if(-not $xml.SelectSingleNode('//w:sectPr/w:pgNumType[(@w:fmt="decimal" or not(@w:fmt)) and @w:start="1"]',$ns)){throw 'Missing main page-number restart'}
    $footerFields=($zip.Entries|Where-Object FullName -match '^word/.*footer.*\.xml$'|ForEach-Object {
        $footerReader=[IO.StreamReader]::new($_.Open());try{$footerReader.ReadToEnd()}finally{$footerReader.Dispose()}
    })-join ' '
    if($footerFields -notmatch ' PAGE '){throw 'Missing live PAGE footer fields'}
    [pscustomobject]@{Passed=$true;CodeBlocks=$source.Count;Images=$pictureCount;Tables=$tableCount;Headings=$headingCount;TocEntries=$tocEntries;DocxSha256=(Get-FileHash -LiteralPath $path).Hash} |
        ConvertTo-Json | Set-Content -LiteralPath (Join-Path $WorkDir 'verification.json') -Encoding utf8
    Write-Output "Verified $($source.Count) exact code blocks, $pictureCount inline images, $headingCount headings, $tocEntries TOC entries."
}finally{$zip.Dispose()}
