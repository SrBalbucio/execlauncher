#!/bin/bash

clear
echo "Execlauncher - Build Tool v0.0.1"
echo "Preparando para criar o instalador..."
echo "Não é necessário apagar nenhum arquivo, todos desnecessários serão removidos."
echo "Certifique-se de ter todas as dependências instaladas antes de criar uma versão."

select type in "exe" "app-image" "msi" "rpm" "deb" "pkg" "dmg"; do
  read -r -t 30 -p "Compilar para qual sistema operacional? (Limite: 30 segundos): " reply
  case "$reply" in
    1) type="exe"; break ;;
    2) type="app-image"; break ;;
    3) type="msi"; break ;;
    4) type="rpm"; break ;;
    5) type="deb"; break ;;
    6) type="pkg"; break ;;
    7) type="dmg"; break ;;
    *) echo "Opção inválida."; continue ;;
  esac
done

if [[ -z "$type" ]]; then
  echo "Tempo limite excedido. Operação cancelada."
  exit 1
fi

read -r -t 30 -p "Qual a versão? (Limite: 30 segundos): " version

echo "Sistema operacional: $type"
echo "Versão do App: $version"

mvn clean package -Dmaven.test.skip
cp target/execlauncher.jar jpackage/execlauncher.jar

if [ "$type" == "exe" ]; then
jpackage \
  --input jpackage/ \
  --name "Execlauncher" \
  --description "Execlauncher - Manage executables via a simple GUI." \
  --vendor "balbucio.xyz" \
  --main-jar execlauncher.jar \
  --main-class balbucio.execlauncher.Main \
  --type "$type" \
  --java-options '--enable-preview' \
  --resource-dir resources \
  --icon jpackage/assets/icon.ico \
  --app-version "$version" \
  --verbose \
  --win-dir-chooser \
  --win-menu \
  --win-shortcut \
  --license-file LICENSE
fi