param([string]$EntregaDir)
$ErrorActionPreference = 'Stop'
$auditRoot = $PSScriptRoot
if (-not $EntregaDir) { $EntregaDir = Join-Path $auditRoot 'ENTREGA_PARA_APROBACION' }
$auditDocx = Join-Path $EntregaDir 'REPORTE_QA.docx'
$auditOut = Join-Path $EntregaDir 'EVIDENCIAS\QA_DOCUMENTO'
New-Item -ItemType Directory -Force -Path $auditOut | Out-Null
$auditPdf = Join-Path $auditOut 'REPORTE_QA.pdf'
$beforeIds = @(Get-Process WINWORD -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Id)
$auditWord = New-Object -ComObject Word.Application
$newIds = @(Get-Process WINWORD -ErrorAction SilentlyContinue | Where-Object { $_.Id -notin $beforeIds } | Select-Object -ExpandProperty Id)
if ($newIds.Count -eq 0) { throw 'No se puede garantizar una instancia Word independiente. No se toca la instancia del usuario.' }
try {
    $auditWord.Visible = $false
    $auditWord.DisplayAlerts = 0
    $auditDocument = $auditWord.Documents.Open($auditDocx, $false, $true)
    $auditDocument.ExportAsFixedFormat($auditPdf, 17)
    $auditDocument.Close(0)
    Write-Output "RENDER_WORD_OK $auditPdf"
} finally {
    $auditWord.Quit(0)
    [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($auditWord)
}
