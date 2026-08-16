#!/bin/bash
set -e

clear
echo "Execlauncher - Build Tool v0.0.1"
echo "Preparando para criar o instalador..."
echo "Não é necessário apagar nenhum arquivo, todos desnecessários serão removidos."
echo "Certifique-se de ter todas as dependências instaladas antes de criar uma versão."

type=""
select type in "exe" "app-image" "msi" "rpm" "deb" "pkg" "dmg"; do
  if [[ -n "$type" ]]; then
    break
  fi
  echo "Opção inválida."
done

if [[ -z "$type" ]]; then
  echo "Operação cancelada."
  exit 1
fi

read -r -t 30 -p "Qual a versão? (Limite: 30 segundos): " version || true
if [[ -z "$version" ]]; then
  echo "Versão não informada. Operação cancelada."
  exit 1
fi

echo "Sistema operacional: $type"
echo "Versão do App: $version"

mvn clean package -Dmaven.test.skip
cp target/execlauncher.jar jpackage/execlauncher.jar

JPACKAGE_OPTS=(
  --input jpackage/
  --name "Execlauncher"
  --description "Execlauncher - Manage executables via a simple GUI."
  --vendor "balbucio.xyz"
  --main-jar execlauncher.jar
  --main-class balbucio.execlauncher.Main
  --type "$type"
  --resource-dir resources
  --icon jpackage/assets/icon.ico
  --app-version "$version"
  --license-file LICENSE
)

case "$type" in
  exe|msi)
    JPACKAGE_OPTS+=(--win-dir-chooser --win-menu --win-shortcut)
    ;;
esac

jpackage "${JPACKAGE_OPTS[@]}"