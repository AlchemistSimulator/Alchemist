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

echo "-- Setting up ssh configuration"
mkdir -p ~/.ssh
touch ~/.ssh/known_hosts
ssh-keyscan -v -t "rsa" aur.archlinux.org >> ~/.ssh/known_hosts
echo "$ssh_key" > ~/.ssh/aur
chmod -vR 600 ~/.ssh/aur*
ssh-keygen -vy -f ~/.ssh/aur > ~/.ssh/aur.pub
echo "Host aur.archlinux.org
    IdentityFile ~/.ssh/aur
    User $username" > ~/.ssh/config 

echo "-- Cloning the AUR repository in $aur_repo_dir"
git clone -v "https://aur.archlinux.org/${pkgname}.git" $aur_repo_dir

(
    cd $aur_repo_dir
    git config user.name "$username"
    git config user.email "$email"
    cp -r "$main_repo_dir/$pkgbuild" $aur_repo_dir
    # Retrieve the version from the PKGBUILD
    version=$(< PKGBUILD grep pkgver | cut -d'=' -f 2)
    makepkg --printsrcinfo > .SRCINFO
    echo "-- Committing the update to version $version"

    git add -fv PKGBUILD .SRCINFO
    git commit -m "Update to version $version"
    git remote add aur "ssh://aur@aur.archlinux.org/${pkgname}.git"

    echo "-- Publishing the version $version"
    git push -v aur master
)
