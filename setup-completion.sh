#!/bin/bash
# Setup make completion automaticamente
# Funciona em macOS, Linux, Windows (Git Bash/WSL)

SHELL_RC=""
COMPLETION_FUNC='
_make_completion() {
    local cur opts
    cur="${COMP_WORDS[COMP_CWORD]}"
    opts=$(make -qp 2>/dev/null | awk -F":" "/^[a-zA-Z_][a-zA-Z0-9_]*:/ {print \$1}" | sort -u)
    COMPREPLY=($(compgen -W "${opts}" -- ${cur}))
}
complete -o bashdefault -o default -o nospace -F _make_completion make
'

# Detecta shell
if [[ "$SHELL" == *"zsh"* ]]; then
    SHELL_RC="$HOME/.zshrc"
elif [[ "$SHELL" == *"bash"* ]]; then
    SHELL_RC="$HOME/.bashrc"
else
    echo "Shell nao suportado. Use bash ou zsh."
    exit 1
fi

# Cria arquivo de completion
COMPLETION_FILE="$HOME/.make-completion.sh"
cat > "$COMPLETION_FILE" << 'EOF'
# Make completion (gerado por setup-completion.sh)
_make_completion() {
    local cur opts
    cur="${COMP_WORDS[COMP_CWORD]}"
    if [[ -f Makefile ]]; then
        opts=$(make -qp 2>/dev/null | awk -F":" "/^[a-zA-Z_][a-zA-Z0-9_]*:/ {print \$1}" | sort -u)
        COMPREPLY=($(compgen -W "${opts}" -- ${cur}))
    fi
}
complete -o bashdefault -o default -o nospace -F _make_completion make
EOF

# Adiciona ao shell config se nao existe
if ! grep -q "make-completion.sh" "$SHELL_RC"; then
    echo "source $COMPLETION_FILE" >> "$SHELL_RC"
    echo "✓ Completion instalado em $SHELL_RC"
    echo "Execute: source $SHELL_RC"
else
    echo "✓ Completion ja configurado"
fi
