# Setup make completion para PowerShell (Windows)
# Execute: powershell -ExecutionPolicy Bypass -File setup-completion.ps1

$ProfilePath = $PROFILE
$CompletionScript = @'
# Make completion (PowerShell)
Register-ArgumentCompleter -CommandName make -ScriptBlock {
    param($wordToComplete, $commandAst, $cursorPosition)

    if (Test-Path "Makefile") {
        $targets = make -qp 2>$null |
            Where-Object { $_ -match '^[a-zA-Z_][a-zA-Z0-9_]*:' } |
            ForEach-Object { $_.Split(':')[0] } |
            Sort-Object -Unique

        $targets | Where-Object { $_ -like "$wordToComplete*" } |
            ForEach-Object { [System.Management.Automation.CompletionResult]::new($_, $_, 'ParameterValue', $_) }
    }
}
'@

# Cria profile se nao existe
if (-not (Test-Path $ProfilePath)) {
    New-Item -ItemType File -Path $ProfilePath -Force | Out-Null
    Write-Host "Perfil PowerShell criado: $ProfilePath"
}

# Adiciona completion ao profile se nao existe
if (-not (Get-Content $ProfilePath | Select-String "make completion")) {
    Add-Content -Path $ProfilePath -Value "`n# Make completion (gerado por setup-completion.ps1)`n$CompletionScript"
    Write-Host "✓ Completion instalado em $ProfilePath"
    Write-Host "Reinicie PowerShell para ativar"
} else {
    Write-Host "✓ Completion ja configurado"
}
