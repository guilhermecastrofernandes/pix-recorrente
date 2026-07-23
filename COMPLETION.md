# Make Completion Setup

Autocomplete para `make` commands em todos os SOs.

## Instalação Rápida

### macOS / Linux (Bash/Zsh)

```bash
make setup-completion
```

Depois reload do shell:
```bash
source ~/.bashrc   # ou ~/.zshrc
```

### Windows (PowerShell)

```powershell
powershell -ExecutionPolicy Bypass -File setup-completion.ps1
```

Reinicie PowerShell.

### Windows (Git Bash/WSL)

Mesmo que macOS/Linux:
```bash
make setup-completion
```

## Uso

Depois de configurado, use `[TAB]` para autocomplete:

```bash
make do[TAB]      # expande para: make docker-build, docker-down, docker-logs...
make d[TAB]       # expande para: make docker-*, docker-restart...
make [TAB]        # lista todos os comandos
```

## Ver Todos os Targets

Sem setup de completion, liste com:

```bash
make list-targets
```

## Manual Setup (se script nao funcionar)

### Bash/Zsh

Adicione ao `~/.bashrc` ou `~/.zshrc`:

```bash
_make_completion() {
    local cur opts
    cur="${COMP_WORDS[COMP_CWORD]}"
    if [[ -f Makefile ]]; then
        opts=$(make -qp 2>/dev/null | awk -F":" "/^[a-zA-Z_][a-zA-Z0-9_]*:/ {print \$1}" | sort -u)
        COMPREPLY=($(compgen -W "${opts}" -- ${cur}))
    fi
}
complete -o bashdefault -o default -o nospace -F _make_completion make
```

### PowerShell

Adicione ao `$PROFILE`:

```powershell
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
```

## Troubleshooting

- **Completion nao funciona**: Verifique se `make -qp` funciona
- **Bash antigo**: Algumas versoes nao suportam `-o bashdefault`
- **WSL**: Use setup-completion.sh (Linux)
- **Git Bash**: Use setup-completion.sh (Linux)
