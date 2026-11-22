param(
    [Parameter(Mandatory)][string]$ProjectRoot,
    [Parameter(Mandatory)][string]$WorkDir
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'book-inputs.ps1')
$config = Get-BookConfig
Assert-DiagramMappings $ProjectRoot $config
Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.Drawing
$root = (Resolve-Path -LiteralPath $ProjectRoot).Path
$bookDir = Join-Path $root 'practice-book'
$work = [IO.Path]::GetFullPath($WorkDir)
New-Item -ItemType Directory -Force -Path $work | Out-Null
$sourceCover = Join-Path $root $config.cover
$output = Join-Path $work ([IO.Path]::GetFileName($config.output))
Copy-Item -LiteralPath $sourceCover -Destination $output -Force
$utf8 = [System.Text.UTF8Encoding]::new($false)
$script:media = [System.Collections.Generic.List[object]]::new()
$script:paragraphs = 0; $script:codeCount = 0; $script:tableCount = 0; $script:imageCount = 0
$script:sourceLabels = [System.Collections.Generic.List[object]]::new()
$script:headingRecords = [System.Collections.Generic.List[object]]::new()
function Esc([string]$s) { [System.Security.SecurityElement]::Escape($s) }
function Run([string]$s,[string]$props='') { '<w:r><w:rPr>'+$props+'</w:rPr><w:t xml:space="preserve">'+(Esc $s)+'</w:t></w:r>' }
function Inline([string]$s) {
    $s = [regex]::Replace($s,'!?(\[([^\]]+)\])\(([^)]+)\)', '$2')
    $b = [System.Text.StringBuilder]::new()
    $offset = 0
    foreach ($m in [regex]::Matches($s,'(`[^`]+`|\*\*[^*]+\*\*)')) {
        if ($m.Index -gt $offset) { [void]$b.Append((Run $s.Substring($offset,$m.Index-$offset))) }
        if ($m.Value.StartsWith('`')) {
            [void]$b.Append((Run $m.Value.Trim('`') '<w:rFonts w:ascii="Consolas" w:hAnsi="Consolas" w:eastAsia="맑은 고딕"/><w:sz w:val="20"/><w:color w:val="35683C"/>'))
        } else { [void]$b.Append((Run $m.Value.Substring(2,$m.Value.Length-4) '<w:b/>')) }
        $offset = $m.Index+$m.Length
    }
    if ($offset -lt $s.Length) { [void]$b.Append((Run $s.Substring($offset))) }
    $b.ToString()
}
function Para([string]$s,[string]$style='Normal',[string]$extra='') {
    $script:paragraphs++
    '<w:p><w:pPr><w:pStyle w:val="'+$style+'"/>'+$extra+'</w:pPr>'+(Inline $s)+'</w:p>'
}
function Heading([string]$s,[int]$level,[bool]$front=$false) {
    $style = if ($front) { if ($level -eq 1) { 'FrontTitle' } else { 'FrontHeading' } } else { 'Heading'+$level }
    if (-not $front) { $script:headingRecords.Add([pscustomobject]@{Text=$s;Level=$level}) }
    if ($s -eq '5.5 학습 확인') { Para $s $style '<w:spacing w:before="160" w:after="90"/>' } else { Para $s $style }
}
function Code([string[]]$lines,[string]$language) {
    $script:codeCount++
    $b = [System.Text.StringBuilder]::new()
    $keep=if($lines.Count -le 8){'1'}else{'0'}
    [void]$b.Append('<w:p><w:pPr><w:pStyle w:val="Code"/><w:keepNext w:val="0"/><w:keepLines w:val="'+$keep+'"/><w:widowControl/></w:pPr>')
    for ($i=0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i].Replace("`t",'    ')
        $color = if ($line.TrimStart() -match '^(//|--|#|<!--|\* |/\*)') { '5B7661' } else { '20352A' }
        $props = '<w:color w:val="'+$color+'"/>'
        [void]$b.Append((Run $line $props))
        if ($i -lt $lines.Count-1) { [void]$b.Append('<w:r><w:br/></w:r>') }
    }
    [void]$b.Append('</w:p>')
    $b.ToString()
}
function Table([string[]]$rows) {
    $script:tableCount++
    $all = @($rows | Where-Object { $_ -notmatch '^\|?\s*:?-{3,}' } | ForEach-Object { ,@($_.Trim().Trim('|').Split('|') | ForEach-Object Trim) })
    $n = $all[0].Count
    $widths = switch ($n) { 2 { @(3300,6260) } 3 { @(2300,3500,3760) } 4 { @(1600,2550,2800,2610) } default { @(for ($k=0;$k -lt $n;$k++) {[int](9560/$n)}) } }
    if ($all[0][0] -match '실습 교재') { $widths = @(4200,5360) }
    if ($all[0][0] -eq '이름' -and $n -eq 3) { $widths = @(1900,6160,1500) }
    $b=[System.Text.StringBuilder]::new()
    [void]$b.Append('<w:tbl><w:tblPr><w:tblW w:w="9560" w:type="dxa"/><w:tblLayout w:type="fixed"/><w:tblBorders>')
    foreach ($edge in @('top','left','bottom','right','insideH','insideV')) { [void]$b.Append('<w:'+$edge+' w:val="single" w:sz="4" w:color="D9D9D9"/>') }
    [void]$b.Append('</w:tblBorders><w:tblCellMar><w:top w:w="95" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:bottom w:w="95" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tblCellMar></w:tblPr><w:tblGrid>')
    foreach ($width in $widths) { [void]$b.Append('<w:gridCol w:w="'+$width+'"/>') }
    [void]$b.Append('</w:tblGrid>')
    for ($r=0;$r -lt $all.Count;$r++) {
        $head = if ($r -eq 0) { '<w:tblHeader/>' } else { '' }
        [void]$b.Append('<w:tr><w:trPr><w:cantSplit/>'+$head+'</w:trPr>')
        for ($c=0;$c -lt $n;$c++) {
            $fill = if ($r -eq 0) { '33483A' } elseif ($r % 2 -eq 0) { 'F3F6F2' } else { 'FFFFFF' }
            $color = if ($r -eq 0) { 'FFFFFF' } else { '24302A' }
            $text = if ($c -lt $all[$r].Count) {$all[$r][$c]} else {''}
            [void]$b.Append('<w:tc><w:tcPr><w:tcW w:w="'+$widths[$c]+'" w:type="dxa"/><w:shd w:fill="'+$fill+'"/><w:vAlign w:val="center"/></w:tcPr>')
            $content = Inline $text
            if ($r -eq 0) { $content = Run ([regex]::Replace($text,'\[([^\]]+)\]\([^)]+\)','$1')) '<w:b/><w:color w:val="FFFFFF"/>' }
            [void]$b.Append('<w:p><w:pPr><w:pStyle w:val="TableText"/></w:pPr>'+$content+'</w:p></w:tc>')
        }
        [void]$b.Append('</w:tr>')
    }
    [void]$b.Append('</w:tbl>')
    [void]$b.Append('<w:p><w:pPr><w:spacing w:before="0" w:after="70"/><w:rPr><w:sz w:val="2"/></w:rPr></w:pPr></w:p>')
    $b.ToString()
}
function Picture([string]$relative,[string]$alt) {
    $script:imageCount++
    $path=Join-Path $bookDir $relative
    $img=[System.Drawing.Image]::FromFile($path)
    try { $iw=$img.Width; $ih=$img.Height } finally {$img.Dispose()}
    # Show the dependency panel at a readable print scale; preserve the full image in the package.
    $crop = if ($relative -notmatch 'initializr') { @(0,0,$iw,$ih) } else { switch -Wildcard ($relative) {
        '*01-spring-core*' { @(1005,210,960,235) }
        '*02-1-jdbc*' { @(1005,210,960,340) }
        '*02-2-mybatis*' { @(1000,210,960,535) }
        '*02-3-jpa*' { @(1000,210,960,520) }
        '*03-restful-api*' { @(1000,210,960,660) }
        '*thymeleaf-initializr*' { @(1000,210,960,580) }
        '*mvc-initializr*' { @(1000,220,950,1010) }
        '*05-spring-security*' { @(1005,230,970,1055) }
        default { @(0,0,$iw,$ih) }
    } }
    $left=[int]($crop[0]/$iw*100000); $top=[int]($crop[1]/$ih*100000)
    $right=[int](($iw-$crop[0]-$crop[2])/$iw*100000); $bottom=[int](($ih-$crop[1]-$crop[3])/$ih*100000)
    $width=440; $height=$width*$crop[3]/$crop[2]
    if ($height -gt 470) { $width=$width*470/$height; $height=470 }
    $cx=[long]($width*12700); $cy=[long]($height*12700)
    $rid='rIdBookImage'+$script:imageCount; $name='book-image-'+$script:imageCount+'.png'
    $script:media.Add([pscustomobject]@{Id=$rid;Name=$name;Path=$path})
    $id=500+$script:imageCount
    $xml='<w:p><w:pPr><w:pStyle w:val="Figure"/><w:keepNext/></w:pPr><w:r><w:drawing><wp:inline distT="0" distB="0" distL="0" distR="0"><wp:extent cx="'+$cx+'" cy="'+$cy+'"/><wp:effectExtent l="0" t="0" r="0" b="0"/><wp:docPr id="'+$id+'" name="'+$name+'" descr="'+(Esc $alt)+'"/><wp:cNvGraphicFramePr><a:graphicFrameLocks xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" noChangeAspect="1"/></wp:cNvGraphicFramePr><a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:nvPicPr><pic:cNvPr id="'+$id+'" name="'+$name+'"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed="'+$rid+'"/><a:srcRect l="'+$left+'" t="'+$top+'" r="'+$right+'" b="'+$bottom+'"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="'+$cx+'" cy="'+$cy+'"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>'
    $caption='그림 '+$script:imageCount+'. '+($alt -replace '프로젝트 생성 설정과 ','')
    if ($relative -match 'initializr') { $caption+=' — 의존성 영역 확대' }
    $xml+(Para $caption 'FigureCaption')
}
function ConvertMarkdown([string]$file,[bool]$front=$false) {
    $lines=[System.IO.File]::ReadAllText((Join-Path $bookDir $file)).Replace("`r`n","`n").Split("`n")
    $b=[System.Text.StringBuilder]::new()
    $i=0
    while ($i -lt $lines.Length) {
        $line=$lines[$i]
        if($front) {$line=$line.Replace('코드 블록 위의 파일 링크로 원문을 열 수 있습니다.','코드 블록 위에 원본 파일 이름과 프로젝트 안의 경로를 표시했습니다.').Replace('메서드 발췌는 링크에 표시한 기존 클래스 안의 코드입니다.','메서드 발췌는 표시한 기존 클래스 안의 코드입니다.')}
        if (-not $line.Trim()) {$i++;continue}
        if ($line -match '^```(.*)$') {
            $lang=$Matches[1]; $i++; $code=[System.Collections.Generic.List[string]]::new()
            while ($i -lt $lines.Length -and $lines[$i] -notmatch '^```') {$code.Add($lines[$i]);$i++}
            if ($lang -eq 'mermaid') {
                $diagramHash = Get-MermaidHash ($code -join "`n")
                $mapping = @($config.diagrams | Where-Object { $_.source -eq $file -and $_.mermaidSha256 -eq $diagramHash })
                if ($mapping.Count -ne 1) { throw "Unmapped Mermaid diagram in $file" }
                [void]$b.Append((Picture $mapping[0].image $mapping[0].alt))
            } else { [void]$b.Append((Code $code.ToArray() $lang)) }
            $i++;continue
        }
        if ($line -match '^\|') {
            $rows=[System.Collections.Generic.List[string]]::new()
            while ($i -lt $lines.Length -and $lines[$i] -match '^\|') {$rows.Add($lines[$i]);$i++}
            [void]$b.Append((Table $rows.ToArray()));continue
        }
        if ($line -match '^!\[([^\]]+)\]\(([^)]+)\)') {
            [void]$b.Append((Picture $Matches[2] $Matches[1]));$i++;continue
        }
        if ($line -match '^(#{1,3}) (.+)$') {
            $level=$Matches[1].Length; $title=$Matches[2]
            if ($front -and $title -eq 'Word 변환을 위한 문서 규칙') {break}
            if ($front -and $title -eq '목차') {
                $i++
                while($i -lt $lines.Length -and $lines[$i] -notmatch '^## ') {$i++}
                continue
            }
            if ($front -and $level -eq 1) {$title='이 책을 시작하기 전에'}
            $finalReview = ($file -eq '05-spring-security.md' -and $title -eq '5.5 학습 확인')
            [void]$b.Append((Heading $title $level $front));$i++;continue
        }
        if ($line -match '^\[목차\]|^\[프로젝트 전체 안내\]') {$i++;continue}
        if ($line -match '^>\s?(.*)$') {
            $refs=[System.Collections.Generic.List[string]]::new()
            while ($i -lt $lines.Length -and $lines[$i] -match '^>\s?(.*)$') {$refs.Add($Matches[1].Trim());$i++}
            [void]$b.Append((Para ($refs -join ' ') 'Reference'));continue
        }
        if ($line -match '^\[([^\]]+)\]\(([^)]+)\)$') {
            $label=$Matches[1]; $target=$Matches[2]
            if ($i+2 -lt $lines.Length -and $lines[$i+2] -match '^```') {
                $script:sourceLabels.Add([pscustomobject]@{Label=$label;Path=$target;File=$file})
                [void]$b.Append((Para $label 'SourceCaption'))
                # Keep the project-relative path visible in the offline edition.
                $sourcePath=$target -replace '^\.\./',''
                [void]$b.Append((Para $sourcePath 'SourcePath'))
                $i++;continue
            }
        }
        if ($line -match '^(- |\d+\. )(.+)$') {
            $prefix=if ($Matches[1] -eq '- ') {'• '} else {$Matches[1]}
            $listExtra = if ($finalReview) { '<w:spacing w:after="0" w:line="300" w:lineRule="auto"/>' } else { '' }
            [void]$b.Append((Para ($prefix+$Matches[2]) 'ListText' $listExtra));$i++;continue
        }
        if ($line -match '^\*\*[^*]+\*\*$') {
            $label=$line.Trim('*')
            if ($label -eq '실습 코드') {$i++;continue}
            [void]$b.Append((Para $label 'LessonLabel'));$i++;continue
        }
        $para=$line.Trim();$i++
        while ($i -lt $lines.Length -and $lines[$i].Trim() -and $lines[$i] -notmatch '^(#|```|\||>|!\[|- |\d+\. |\*\*[^*]+\*\*$)') {$para+=' '+$lines[$i].Trim();$i++}
        if ($front) {
            $para=$para.Replace('Markdown 문서입니다.','실습 교재입니다.').Replace('캡처 이미지는 이 문서를 기준으로 `images/장별 폴더/`에 저장되어 있습니다.','캡처 이미지는 각 장의 의존성 선택 설명에서 확인합니다.').Replace('코드 블록 위의 파일 링크로 원문을 열 수 있습니다.','코드 블록 위에 원본 파일 이름과 프로젝트 안의 경로를 표시했습니다.').Replace('메서드 발췌는 링크에 표시한 기존 클래스 안의 코드입니다.','메서드 발췌는 표시한 기존 클래스 안의 코드입니다.')
        }
        $bodyStyle=if($front){'FrontBody'}else{'Normal'}
        [void]$b.Append((Para $para $bodyStyle))
    }
    $b.ToString()
}
function GetEntryText($zip,[string]$name) {
    $reader=[System.IO.StreamReader]::new($zip.GetEntry($name).Open())
    try {$reader.ReadToEnd()} finally {$reader.Dispose()}
}
function PutText($zip,[string]$name,[string]$value) {
    $old=$zip.GetEntry($name); if($null -ne $old){$old.Delete()}
    $entry=$zip.CreateEntry($name)
    $writer=[System.IO.StreamWriter]::new($entry.Open(),$utf8)
    try{$writer.Write($value)}finally{$writer.Dispose()}
}
function Footer([bool]$front) {
    $fmt=if($front){'ROMAN'}else{'ARABIC'}
    '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><w:ftr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:p><w:pPr><w:tabs><w:tab w:val="right" w:pos="9560"/></w:tabs><w:spacing w:before="0" w:after="0"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="맑은 고딕" w:eastAsia="맑은 고딕"/><w:sz w:val="17"/><w:color w:val="7A837D"/></w:rPr><w:t>올인원 스프링부트 정복</w:t><w:tab/></w:r><w:r><w:rPr><w:sz w:val="18"/><w:color w:val="4A6350"/></w:rPr><w:fldChar w:fldCharType="begin"/></w:r><w:r><w:instrText xml:space="preserve"> PAGE </w:instrText></w:r><w:r><w:fldChar w:fldCharType="separate"/></w:r><w:r><w:t>1</w:t></w:r><w:r><w:fldChar w:fldCharType="end"/></w:r></w:p></w:ftr>'
}
function Section([string]$mode) {
    $header=if($mode -eq 'cover'){'rIdBookBlankHeader'}elseif($mode -eq 'front'){'rIdBookFrontHeader'}else{'rIdBookMainHeader'}
    $footer=if($mode -eq 'cover'){'rIdBookBlankFooter'}elseif($mode -eq 'front'){'rIdBookFrontFooter'}else{'rIdBookMainFooter'}
    $nums=if($mode -eq 'front'){'<w:pgNumType w:fmt="lowerRoman" w:start="1"/>'}elseif($mode -eq 'main'){'<w:pgNumType w:fmt="decimal" w:start="1"/>'}else{''}
    $margins=if($mode -eq 'cover'){'<w:pgMar w:top="1180" w:right="1340" w:bottom="1080" w:left="1340" w:header="0" w:footer="0" w:gutter="0"/>'}else{'<w:pgMar w:top="1160" w:right="1340" w:bottom="1120" w:left="1340" w:header="500" w:footer="520" w:gutter="0"/>'}
    '<w:sectPr><w:headerReference w:type="default" r:id="'+$header+'"/><w:footerReference w:type="default" r:id="'+$footer+'"/><w:type w:val="nextPage"/><w:pgSz w:w="12240" w:h="15840"/>'+$margins+$nums+'<w:cols w:space="720"/></w:sectPr>'
}

$zip=[System.IO.Compression.ZipFile]::Open($output,[System.IO.Compression.ZipArchiveMode]::Update)
try {
    $coverXml=GetEntryText $zip 'word/document.xml'
    $start=$coverXml.IndexOf('<w:body>')+8
    $end=$coverXml.LastIndexOf('<w:sectPr')
    $coverBody=$coverXml.Substring($start,$end-$start)
    # The imported cover keeps its original direct formatting and its native editable text.
    $body=[System.Text.StringBuilder]::new()
    [void]$body.Append($coverBody)
    [void]$body.Append('<w:p><w:pPr>'+(Section 'cover')+'</w:pPr></w:p>')
    [void]$body.Append((Para '목차' 'FrontTitle'))
    [void]$body.Append((Para '__BOOK_TOC__'))
    [void]$body.Append('<w:p><w:r><w:br w:type="page"/></w:r></w:p>')
    [void]$body.Append((ConvertMarkdown $config.preface $true))
    [void]$body.Append('<w:p><w:pPr>'+(Section 'front')+'</w:pPr></w:p>')
    $files=@($config.chapters)
    foreach($file in $files){[void]$body.Append((ConvertMarkdown $file))}
    [void]$body.Append((Section 'main'))
    $document=$coverXml.Substring(0,$start)+$body.ToString()+'</w:body></w:document>'
    PutText $zip 'word/document.xml' $document

    # Preserve the cover styles and add the book's paragraph system.
    [xml]$styles=GetEntryText $zip 'word/styles.xml'
    $ns=[System.Xml.XmlNamespaceManager]::new($styles.NameTable);$ns.AddNamespace('w','http://schemas.openxmlformats.org/wordprocessingml/2006/main')
    $newStyles=[System.Collections.Generic.List[string]]::new()
    $specs=@(
        @('Normal','본문',21,'24302A',0,120,0,9,0,0),
        @('Heading1','heading 1',48,'234D31',350,260,1,0,0,1),
        @('Heading2','heading 2',31,'2E613B',270,140,1,1,0,0),
        @('Heading3','heading 3',25,'1E3928',220,110,1,2,0,0),
        @('FrontTitle','앞부분 제목',38,'234D31',0,260,1,9,0,0),
        @('FrontBody','앞부분 본문',20,'24302A',0,100,0,9,0,0),
        @('FrontHeading','앞부분 소제목',28,'2E613B',260,140,1,9,0,0),
        @('Reference','본 교재 참고',18,'526658',70,130,0,9,0,0),
        @('LessonLabel','실습 구분',21,'35583C',170,75,1,9,0,0),
        @('SourceCaption','코드 파일 이름',19,'35583C',130,25,1,9,0,0),
        @('SourcePath','코드 원본 경로',16,'6D7770',0,70,0,9,0,0),
        @('Figure','캡처 이미지',18,'24302A',140,70,0,9,0,0),
        @('FigureCaption','그림 설명',18,'607067',0,180,0,9,0,0),
        @('TableText','표 본문',19,'24302A',0,15,0,9,0,0),
        @('ListText','목록 본문',21,'24302A',0,75,0,9,0,0),
        @('Code','실습 코드',18,'20352A',0,160,0,9,1,0)
    )
    foreach($s in $specs){
        $id=$s[0];$old=$styles.SelectSingleNode('/w:styles/w:style[@w:styleId="'+$id+'"]',$ns)
        if($null -ne $old){[void]$old.ParentNode.RemoveChild($old)}
        $p='<w:widowControl/><w:spacing w:before="'+$s[4]+'" w:after="'+$s[5]+'" w:line="320" w:lineRule="auto"/><w:suppressAutoHyphens/><w:snapToGrid w:val="0"/>'
        if($s[6]){$p+='<w:keepNext/>'}
        if($s[7] -lt 9){$p+='<w:outlineLvl w:val="'+$s[7]+'"/>'}else{$p+='<w:outlineLvl w:val="9"/>'}
        if($s[9]){$p+='<w:pageBreakBefore/>'}
        if($id -eq 'Code'){$p='<w:spacing w:before="100" w:after="160" w:line="245" w:lineRule="exact"/><w:shd w:fill="F3F6F2"/><w:ind w:left="135" w:right="100"/><w:keepNext w:val="0"/><w:keepLines w:val="0"/><w:widowControl/><w:wordWrap/><w:snapToGrid w:val="0"/>'}
        if($id -eq 'Figure' -or $id -eq 'FigureCaption'){$p+='<w:jc w:val="center"/>'}
        if($id -eq 'SourcePath' -or $id -eq 'SourceCaption'){$p+='<w:keepNext/>'}
        if($id -eq 'ListText'){$p+='<w:ind w:left="220" w:hanging="220"/>'}
        if($id -eq 'TableText'){$p='<w:spacing w:before="0" w:after="20" w:line="275" w:lineRule="auto"/><w:widowControl/><w:outlineLvl w:val="9"/>'}
        $font=if($s[8]){'Consolas'}else{'맑은 고딕'}
        $r='<w:rFonts w:ascii="'+$font+'" w:hAnsi="'+$font+'" w:eastAsia="맑은 고딕"/><w:color w:val="'+$s[3]+'"/><w:sz w:val="'+$s[2]+'"/><w:szCs w:val="'+$s[2]+'"/>'
        if($s[6]){$r+='<w:b/>'}
        $fragment=$styles.CreateDocumentFragment()
        $fragment.InnerXml='<w:style xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" w:type="paragraph" w:styleId="'+$id+'"><w:name w:val="'+$s[1]+'"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:qFormat/><w:pPr>'+$p+'</w:pPr><w:rPr>'+$r+'</w:rPr></w:style>'
        if($id -eq 'Normal'){$fragment.FirstChild.RemoveChild($fragment.FirstChild.SelectSingleNode('w:basedOn',$ns))|Out-Null}
        [void]$styles.DocumentElement.AppendChild($fragment)
    }
    PutText $zip 'word/styles.xml' $styles.OuterXml

    [xml]$rels=GetEntryText $zip 'word/_rels/document.xml.rels'
    $relNs='http://schemas.openxmlformats.org/package/2006/relationships'
    function AddRel([string]$id,[string]$type,[string]$target){$el=$rels.CreateElement('Relationship',$relNs);$el.SetAttribute('Id',$id);$el.SetAttribute('Type','http://schemas.openxmlformats.org/officeDocument/2006/relationships/'+$type);$el.SetAttribute('Target',$target);[void]$rels.DocumentElement.AppendChild($el)}
    foreach($asset in $script:media){
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip,$asset.Path,'word/media/'+$asset.Name)|Out-Null
        AddRel $asset.Id 'image' ('media/'+$asset.Name)
    }
    $parts=@(
        @('rIdBookBlankHeader','header','book-blank-header.xml','<w:hdr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:p/></w:hdr>'),
        @('rIdBookBlankFooter','footer','book-blank-footer.xml','<w:ftr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:p/></w:ftr>'),
        @('rIdBookFrontHeader','header','book-front-header.xml','<w:hdr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:p><w:pPr><w:jc w:val="right"/></w:pPr><w:r><w:rPr><w:rFonts w:eastAsia="맑은 고딕"/><w:sz w:val="17"/><w:color w:val="7A837D"/></w:rPr><w:t>실습 교재 안내</w:t></w:r></w:p></w:hdr>'),
        @('rIdBookMainHeader','header','book-main-header.xml','<w:hdr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:p><w:pPr><w:jc w:val="right"/></w:pPr><w:r><w:rPr><w:sz w:val="17"/><w:color w:val="7A837D"/></w:rPr><w:fldChar w:fldCharType="begin"/></w:r><w:r><w:instrText xml:space="preserve"> STYLEREF 1 </w:instrText></w:r><w:r><w:fldChar w:fldCharType="separate"/></w:r><w:r><w:t>올인원 스프링부트 정복</w:t></w:r><w:r><w:fldChar w:fldCharType="end"/></w:r></w:p></w:hdr>'),
        @('rIdBookFrontFooter','footer','book-front-footer.xml',(Footer $true)),
        @('rIdBookMainFooter','footer','book-main-footer.xml',(Footer $false))
    )
    [xml]$types=GetEntryText $zip '[Content_Types].xml'
    foreach($part in $parts){
        AddRel $part[0] $part[1] $part[2]
        PutText $zip ('word/'+$part[2]) $part[3]
        $type=$types.CreateElement('Override','http://schemas.openxmlformats.org/package/2006/content-types')
        $type.SetAttribute('PartName','/word/'+$part[2]);$type.SetAttribute('ContentType','application/vnd.openxmlformats-officedocument.wordprocessingml.'+$part[1]+'+xml');[void]$types.DocumentElement.AppendChild($type)
    }
    PutText $zip 'word/_rels/document.xml.rels' $rels.OuterXml
    PutText $zip '[Content_Types].xml' $types.OuterXml
} finally {$zip.Dispose()}
[pscustomobject]@{Files=$files;Paragraphs=$script:paragraphs;CodeBlocks=$script:codeCount;Tables=$script:tableCount;Images=$script:imageCount;Headings=$script:headingRecords;SourceLabels=$script:sourceLabels;Media=$script:media} | ConvertTo-Json -Depth 6 | Set-Content (Join-Path $work 'manifest.json') -Encoding utf8
Write-Output "Built DOCX: $output"
Write-Output "Code blocks: $script:codeCount / Tables: $script:tableCount / Images: $script:imageCount / Headings: $($script:headingRecords.Count)"
