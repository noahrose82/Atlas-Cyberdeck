param(
    [string]$RepoRoot = (Get-Location).Path
)

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "Atlas Cyberdeck Documentation Audit" -ForegroundColor Cyan
Write-Host "Repository: $RepoRoot"
Write-Host ""

$warnings = New-Object System.Collections.Generic.List[string]
$passes = New-Object System.Collections.Generic.List[string]

function Pass([string]$Message) {
    $passes.Add($Message)
    Write-Host "[PASS] $Message" -ForegroundColor Green
}

function Warn([string]$Message) {
    $warnings.Add($Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Get-RepoFiles {
    param([string[]]$Extensions)

    Get-ChildItem -Path $RepoRoot -Recurse -File |
            Where-Object {
                $_.FullName -notmatch '[\\/]\.git[\\/]' -and
                        $_.FullName -notmatch '[\\/]\.gradle[\\/]' -and
                        $_.FullName -notmatch '[\\/]build[\\/]' -and
                        $Extensions -contains $_.Extension.ToLowerInvariant()
            }
}

$requiredFiles = @(
    "README.md",
    "ATLAS_LABS.md",
    "CODE_OF_CONDUCT.md",
    "CONTRIBUTING.md",
    "LICENSE",
    "SECURITY.md",
    "docs\ARCHITECTURE.md",
    "docs\CHANGELOG.md",
    "docs\ROADMAP.md",
    "docs\STYLE_GUIDE.md"
)

Write-Host "== Required files ==" -ForegroundColor Cyan
foreach ($relative in $requiredFiles) {
    $path = Join-Path $RepoRoot $relative
    if (Test-Path $path) {
        Pass $relative
    } else {
        Warn "Missing required file: $relative"
    }
}

$expectedAdrs = @(
    "ADR-001-Filesystem-Architecture.md",
    "ADR-002-Terminal-Architecture.md",
    "ADR-003-Persistence.md",
    "ADR-004-Testing-Strategy.md",
    "ADR-005-Continuous-Integration.md",
    "ADR-006-Linux-Runtime-Architecture.md",
    "ADR-007-PRoot-Runtime.md",
    "ADR-008-Runtime-Safety-Model.md",
    "ADR-009-Controlled-Recovery.md",
    "ADR-010-Atlas-Shell-vs-Ubuntu.md"
)

Write-Host ""
Write-Host "== ADR inventory ==" -ForegroundColor Cyan
foreach ($name in $expectedAdrs) {
    $path = Join-Path $RepoRoot ("docs\adr\" + $name)
    if (Test-Path $path) {
        Pass $name
    } else {
        Warn "Missing ADR: $name"
    }
}

$expectedEngineering = @(
    "Command-Completion.md",
    "Terminal-Engines.md",
    "VirtualFileSystem-Orchestrator.md",
    "Linux-Runtime.md",
    "Ubuntu-RootFS.md",
    "Guest-Command-Execution.md",
    "Package-Management.md",
    "Runtime-Networking.md",
    "Runtime-Safety.md",
    "Runtime-Recovery.md",
    "Runtime-Testing.md"
)

Write-Host ""
Write-Host "== Engineering documentation ==" -ForegroundColor Cyan
foreach ($name in $expectedEngineering) {
    $path = Join-Path $RepoRoot ("docs\engineering\" + $name)
    if (Test-Path $path) {
        Pass $name
    } else {
        Warn "Missing engineering document: $name"
    }
}

Write-Host ""
Write-Host "== Empty Markdown files ==" -ForegroundColor Cyan
$mdFiles = Get-RepoFiles -Extensions @(".md")
$empty = @()

foreach ($file in $mdFiles) {
    $content = Get-Content -Raw -LiteralPath $file.FullName
    if ([string]::IsNullOrWhiteSpace($content)) {
        $empty += $file
        Warn "Empty Markdown file: $($file.FullName.Substring($RepoRoot.Length).TrimStart('\','/'))"
    }
}

if ($empty.Count -eq 0) {
    Pass "No empty Markdown files found."
}

Write-Host ""
Write-Host "== Legacy/stale terminology review ==" -ForegroundColor Cyan

$terms = @(
    "PocketLab",
    "Pocket Lab",
    "Sprint ",
    "Foundation Milestone",
    "Linux emulator",
    "virtual Linux"
)

# These files intentionally mention legacy/stale terminology as examples,
# audit rules, or historical release context. Exclude those occurrences
# so the audit reports actionable warnings instead of self-generated noise.
$alwaysIgnoreTerminologyFiles = @(
    "docs\STYLE_GUIDE.md",
    "docs\DOCUMENTATION_AUDIT.md"
)

$historicalTerminologyFiles = @(
    "docs\CHANGELOG.md"
)

foreach ($term in $terms) {
    $matches = Select-String -Path ($mdFiles.FullName) -Pattern $term -SimpleMatch -ErrorAction SilentlyContinue

    $actionable = @()

    foreach ($m in $matches) {
        $rel = $m.Path.Substring($RepoRoot.Length).TrimStart('\','/')

        if ($alwaysIgnoreTerminologyFiles -contains $rel) {
            continue
        }

        if (($historicalTerminologyFiles -contains $rel) -and
                ($term -eq "Sprint " -or $term -eq "Foundation Milestone")) {
            continue
        }

        # The Android namespace intentionally remains com.noahrose.pocketlab.
        # Do not treat that legacy package identifier as stale product branding.
        if (($term -eq "PocketLab" -or $term -eq "Pocket Lab") -and
                $m.Line -match 'com\.noahrose\.pocketlab') {
            continue
        }

        $actionable += $m
    }

    if ($actionable.Count -gt 0) {
        Warn "Review '$term' occurrences: $($actionable.Count)"
        foreach ($m in ($actionable | Select-Object -First 5)) {
            $rel = $m.Path.Substring($RepoRoot.Length).TrimStart('\','/')
            Write-Host "       ${rel}:$($m.LineNumber)  $($m.Line.Trim())" -ForegroundColor DarkYellow
        }
        if ($actionable.Count -gt 5) {
            Write-Host "       ... plus $($actionable.Count - 5) more" -ForegroundColor DarkYellow
        }
    } else {
        Pass "No actionable '$term' occurrences."
    }
}

Write-Host ""
Write-Host "== Version review ==" -ForegroundColor Cyan

$versionPattern = 'v0\.\d+\.\d+(?:-[A-Za-z0-9.-]+)?'
$versionMatches = Select-String -Path ($mdFiles.FullName) -Pattern $versionPattern -AllMatches -ErrorAction SilentlyContinue
$versions = @()

foreach ($m in $versionMatches) {
    foreach ($match in $m.Matches) {
        $versions += $match.Value
    }
}

$uniqueVersions = $versions | Sort-Object -Unique

if ($uniqueVersions.Count -eq 0) {
    Warn "No version strings found in Markdown."
} else {
    Write-Host "Versions referenced:"
    foreach ($v in $uniqueVersions) {
        if ($v -eq "v0.13.0-alpha") {
            Write-Host "       $v (current)" -ForegroundColor Green
        } else {
            Write-Host "       $v (review whether historical)" -ForegroundColor Yellow
        }
    }
}

Write-Host ""
Write-Host "== Potential stale future-work claims ==" -ForegroundColor Cyan

$completedFeatureTerms = @(
    "rootless Linux runtime",
    "package management",
    "Ubuntu RootFS",
    "persistent Linux shell"
)

# These documents intentionally discuss stale/future-work wording as
# guidance or audit criteria. Their examples should not count as stale claims.
$futureWorkGuidanceFiles = @(
    "CONTRIBUTING.md",
    "docs\STYLE_GUIDE.md",
    "docs\DOCUMENTATION_AUDIT.md"
)

foreach ($term in $completedFeatureTerms) {
    $matches = Select-String -Path ($mdFiles.FullName) -Pattern $term -SimpleMatch -ErrorAction SilentlyContinue
    $futureLike = @()

    foreach ($m in $matches) {
        $rel = $m.Path.Substring($RepoRoot.Length).TrimStart('\','/')

        if ($futureWorkGuidanceFiles -contains $rel) {
            continue
        }

        # Ignore sentences that explicitly say a completed feature must NOT
        # be described as future work.
        if ($m.Line -match '(?i)\b(do not|don''t|must not|should not|not be|no longer)\b' -and
                $m.Line -match '(?i)future|planned|coming|next|roadmap') {
            continue
        }

        if ($m.Line -match '(?i)future|planned|coming|next|roadmap') {
            $futureLike += $m
        }
    }

    if ($futureLike.Count -gt 0) {
        Warn "Potential stale future-work wording for '$term': $($futureLike.Count)"
        foreach ($m in ($futureLike | Select-Object -First 5)) {
            $rel = $m.Path.Substring($RepoRoot.Length).TrimStart('\','/')
            Write-Host "       ${rel}:$($m.LineNumber)  $($m.Line.Trim())" -ForegroundColor DarkYellow
        }
    } else {
        Pass "No actionable future-work wording for '$term'."
    }
}

Write-Host ""
Write-Host "== Relative Markdown link audit ==" -ForegroundColor Cyan

$linkRegex = '\[[^\]]+\]\((?!https?://|mailto:|#)([^)]+)\)'
$brokenLinks = 0

foreach ($file in $mdFiles) {
    $content = Get-Content -Raw -LiteralPath $file.FullName
    $matches = [regex]::Matches($content, $linkRegex)

    foreach ($match in $matches) {
        $targetRaw = $match.Groups[1].Value.Trim()

        if ($targetRaw.StartsWith("<") -and $targetRaw.EndsWith(">")) {
            $targetRaw = $targetRaw.Substring(1, $targetRaw.Length - 2)
        }

        $target = $targetRaw.Split("#")[0]
        if ([string]::IsNullOrWhiteSpace($target)) {
            continue
        }

        $decoded = [System.Uri]::UnescapeDataString($target)
        $candidate = Join-Path $file.DirectoryName $decoded

        if (-not (Test-Path -LiteralPath $candidate)) {
            $brokenLinks++
            $relFile = $file.FullName.Substring($RepoRoot.Length).TrimStart('\','/')
            Warn "Broken relative link in $relFile -> $targetRaw"
        }
    }
}

if ($brokenLinks -eq 0) {
    Pass "No broken relative Markdown file links detected."
}

Write-Host ""
Write-Host "== Duplicate/backup filename review ==" -ForegroundColor Cyan

$suspiciousPatterns = @(
    "*-old.md",
    "*-new.md",
    "*-final.md",
    "*-final2.md",
    "*.bak",
    "*backup*.md",
    "*copy*.md"
)

$suspicious = @()
foreach ($pattern in $suspiciousPatterns) {
    $suspicious += Get-ChildItem -Path $RepoRoot -Recurse -File -Filter $pattern -ErrorAction SilentlyContinue |
            Where-Object {
                $_.FullName -notmatch '[\\/]\.git[\\/]' -and
                        $_.FullName -notmatch '[\\/]build[\\/]'
            }
}

$suspicious = $suspicious | Sort-Object FullName -Unique

if ($suspicious.Count -eq 0) {
    Pass "No obvious backup/duplicate documentation files found."
} else {
    foreach ($file in $suspicious) {
        Warn "Review duplicate/backup file: $($file.FullName.Substring($RepoRoot.Length).TrimStart('\','/'))"
    }
}

Write-Host ""
Write-Host "== Screenshot directories ==" -ForegroundColor Cyan

foreach ($folder in @("docs\images", "docs\screenshots")) {
    $path = Join-Path $RepoRoot $folder
    if (Test-Path $path) {
        $count = (Get-ChildItem -Path $path -File -ErrorAction SilentlyContinue).Count
        Write-Host "       $folder : $count file(s)"
        if ($count -gt 0) {
            Pass "$folder contains assets."
        } else {
            Warn "$folder exists but is empty."
        }
    } else {
        Warn "Missing directory: $folder"
    }
}

Write-Host ""
Write-Host "== Source line count ==" -ForegroundColor Cyan

$sourceExts = @(".kt", ".xml", ".kts", ".properties")
$sourceFiles = Get-RepoFiles -Extensions $sourceExts

$rows = foreach ($ext in $sourceExts) {
    $group = $sourceFiles | Where-Object { $_.Extension.ToLowerInvariant() -eq $ext }
    $lineCount = 0

    foreach ($file in $group) {
        try {
            $lineCount += (Get-Content -LiteralPath $file.FullName).Count
        } catch {
            Warn "Could not count lines in $($file.FullName)"
        }
    }

    [pscustomobject]@{
        Type  = $ext
        Files = $group.Count
        Lines = $lineCount
    }
}

$rows | Format-Table -AutoSize

$totalFiles = ($rows | Measure-Object Files -Sum).Sum
$totalLines = ($rows | Measure-Object Lines -Sum).Sum

Write-Host "TOTAL SOURCE FILES: $totalFiles"
Write-Host "TOTAL SOURCE LINES: $totalLines"
Write-Host ""

Write-Host "== Summary ==" -ForegroundColor Cyan
Write-Host "Passes   : $($passes.Count)" -ForegroundColor Green
Write-Host "Warnings : $($warnings.Count)" -ForegroundColor $(if ($warnings.Count -eq 0) { "Green" } else { "Yellow" })
Write-Host ""

if ($warnings.Count -eq 0) {
    Write-Host "DOCUMENTATION AUDIT: GREEN" -ForegroundColor Green
    exit 0
}

Write-Host "DOCUMENTATION AUDIT: REVIEW REQUIRED" -ForegroundColor Yellow
Write-Host "Warnings may include intentional historical references; review before changing anything."
exit 0
