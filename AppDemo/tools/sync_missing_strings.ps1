$ErrorActionPreference = "Stop"
$appRes = "D:\Project\AppDemo\app\src\main\res"
$rcRes = "D:\Project\RemoteControl100\app\src\main\res"
$defaultPath = Join-Path $appRes "values\strings.xml"

function Get-StringMap($path) {
    $map = @{}
    if (-not (Test-Path $path)) { return $map }
    $text = Get-Content $path -Raw -Encoding UTF8
    [regex]::Matches($text, '<string\s+name="([^"]+)"[^>]*>(.*?)</string>', 'Singleline') | ForEach-Object {
        $map[$_.Groups[1].Value] = $_.Groups[2].Value.Trim()
    }
    return $map
}

$defaultMap = Get-StringMap $defaultPath
$defaultKeys = $defaultMap.Keys

Get-ChildItem $appRes -Filter strings.xml -Recurse | Where-Object {
    $_.DirectoryName -match 'values(-|$)'
} | ForEach-Object {
    $locDir = Split-Path $_.Directory -Leaf
    if ($locDir -eq "values") { return }

    $targetPath = $_.FullName
    $targetMap = Get-StringMap $targetPath
    $missing = $defaultKeys | Where-Object { -not $targetMap.ContainsKey($_) }
    if ($missing.Count -eq 0) { return }

    $rcPath = Join-Path $rcRes "$locDir\strings.xml"
    $rcMap = Get-StringMap $rcPath

    $sb = New-Object System.Text.StringBuilder
    [void]$sb.AppendLine("")
    [void]$sb.AppendLine("    <!-- synced missing keys from default / RemoteControl100 -->")
    foreach ($key in ($missing | Sort-Object)) {
        $val = $null
        if ($rcMap.ContainsKey($key)) { $val = $rcMap[$key] }
        elseif ($defaultMap.ContainsKey($key)) { $val = $defaultMap[$key] }
        else { continue }
        $escaped = $val -replace '&', '&amp;' -replace '<', '&lt;' -replace '>', '&gt;' -replace '"', '&quot;'
        [void]$sb.AppendLine("    <string name=""$key"">$escaped</string>")
    }

    $content = Get-Content $targetPath -Raw -Encoding UTF8
    $insert = $sb.ToString()
    if ($content -match '</resources>\s*$') {
        $newContent = $content -replace '</resources>\s*$', ($insert + "</resources>`n")
        Set-Content -Path $targetPath -Value $newContent -Encoding UTF8 -NoNewline
        Write-Host "$locDir : added $($missing.Count) strings"
    }
}
