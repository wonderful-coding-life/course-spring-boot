param(
    [Parameter(Mandatory)][string]$DocxPath,
    [Parameter(Mandatory)][string]$WorkDir
)
$ErrorActionPreference = 'Stop'
$path = (Resolve-Path -LiteralPath $DocxPath).Path
$work = (Resolve-Path -LiteralPath $WorkDir).Path
$word=$null; $doc=$null
try {
    $word=New-Object -ComObject Word.Application
    $word.Visible=$false; $word.DisplayAlerts=0
    $word.Options.CheckSpellingAsYouType=$false
    $word.Options.CheckGrammarAsYouType=$false
    Write-Output 'Opening document'
    $doc=$word.Documents.Open($path,$false,$false)
    Write-Output 'Opened document'
    $word.ScreenUpdating=$false
    # Install a true Word TOC, including live page-number and hyperlink fields.
    $range=$doc.Content
    if (-not $range.Find.Execute('__BOOK_TOC__')) {throw 'TOC anchor not found'}
    $range.Text=''
    Write-Output 'Adding TOC'
    $toc=$doc.TablesOfContents.Add($range,$true,1,2,$false,'',$true,$true,'',$true,$true,$false)
    Write-Output 'TOC added'
    foreach($level in @(1,2)) {
        $style=$doc.Styles.Item((-19-$level))
        $style.Font.Name='맑은 고딕';$style.Font.NameFarEast='맑은 고딕'
        $style.Font.Size=10; $style.Font.Color=0
        $style.ParagraphFormat.SpaceBefore=0; $style.ParagraphFormat.SpaceAfter=1
        $style.ParagraphFormat.LineSpacingRule=4; $style.ParagraphFormat.LineSpacing=14.5
        $style.ParagraphFormat.KeepWithNext=$false
        if($level -eq 1){$style.Font.Bold=1;$style.ParagraphFormat.SpaceBefore=5}else{$style.Font.Bold=0}
    }
    Write-Output 'Repaginating'
    $doc.Repaginate()
    Write-Output 'Updating fields'
    [void]$doc.Fields.Update()
    $toc.Update()
    Write-Output 'Fields updated'
    $doc.Repaginate()
    $toc.UpdatePageNumbers()
    foreach($section in $doc.Sections) {
        foreach($story in @($section.Headers.Item(1).Range,$section.Footers.Item(1).Range)) { [void]$story.Fields.Update() }
    }
    Write-Output 'Saving document'
    $doc.Save()
    Write-Output 'Exporting PDF'
    $doc.ExportAsFixedFormat((Join-Path $work 'book-preview.pdf'),17)
    $stats=[pscustomobject]@{Pages=$doc.ComputeStatistics(2);Sections=$doc.Sections.Count;TOCEntries=$toc.Range.Paragraphs.Count;InlinePictures=$doc.InlineShapes.Count;Tables=$doc.Tables.Count;Paragraphs=$doc.Paragraphs.Count}
    $stats | ConvertTo-Json | Set-Content (Join-Path $work 'word-stats.json') -Encoding utf8
    $toc.Range.Text | Set-Content (Join-Path $work 'toc.txt') -Encoding utf8
    $stats | Format-List
} finally {
    if($null -ne $doc){$doc.Close(0)}
    if($null -ne $word){$word.Quit();[void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($word)}
}
