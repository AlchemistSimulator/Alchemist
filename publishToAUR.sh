#!/bin/bash

# AUR publishing script

set -o errexit
set -o nounset
set -o pipefail

Help()
{
   # Display Help
   echo "AUR publishing script for alchemist"
   echo
   echo "Syntax: publishToAUR pkgbuild commit_username commit_email ssh_private_key"
   echo
}

if [[ $# -ne 4 ]]; then
    Help
    exit 1
fi

aur_repo_dir="/tmp/aur-repo"
main_repo_dir=$(pwd)

pkgname="alchemist"
pkgbuild="$1"
username="$2"
email="$3"
ssh_key="$4"

echo "AUR PUBLISHER: Set up ssh"
mkdir -p ~/.ssh
touch ~/.ssh/known_hosts
ssh-keyscan -v -t "rsa" aur.archlinux.org >> ~/.ssh/known_hosts
echo "$ssh_key" > ~/.ssh/aur
chmod -vR 600 ~/.ssh/aur*
ssh-keygen -vy -f ~/.ssh/aur > ~/.ssh/aur.pub
echo "Host aur.archlinux.org
    IdentityFile ~/.ssh/aur
    User $username" > ~/.ssh/config

echo "AUR PUBLISHER: Clone the AUR repository in $aur_repo_dir"
git clone -v "https://aur.archlinux.org/${pkgname}.git" $aur_repo_dir

echo "AUR PUBLISHER: Make $aur_repo_dir writeable"
chmod 777 "$aur_repo_dir"

echo "AUR PUBLISHER: read the image version to use"
MAKEPKG_IMAGE="$(grep 'FROM danysk/makepkg' < deps-utils/Dockerfile | sed 's/FROM //')"
(
    echo "AUR PUBLISHER: switch to $aur_repo_dir"
    cd "$aur_repo_dir"
    echo "AUR PUBLISHER: Set up git for $username <$email>"
    git config user.name "$username"
    git config user.email "$email"
    echo "AUR PUBLISHER: Copy the PKGBUILD to $aur_repo_dir"
    cp -r "$main_repo_dir/$pkgbuild" "$aur_repo_dir"
    echo "AUR PUBLISHER: Reading the package version from PKGBUILD"
    version=$(< PKGBUILD grep pkgver | cut -d'=' -f 2 | tr -d ' ')
    echo "AUR PUBLISHER: Generate .SRCINFO for alchemist $version using $MAKEPKG_IMAGE"
    docker run --rm -v "$aur_repo_dir:/pkg:rw" "$MAKEPKG_IMAGE" makepkg --printsrcinfo > .SRCINFO
    echo "AUR PUBLISHER: Commit the update to version $version"
    git add -fv PKGBUILD .SRCINFO
    git commit -m "Update to version $version"
    echo "AUR PUBLISHER: Publish version $version"
    git remote add aur "ssh://aur@aur.archlinux.org/${pkgname}.git"
    git push -v aur master
)
