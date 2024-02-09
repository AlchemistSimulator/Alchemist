# Overridden by command-line arguments in jpackage
Summary: alchemist
Name: alchemist
Version: APPLICATION_VERSION
Release: 1
License: Unknown
Vendor: Unknown
URL: https://alchemistsimulator.github.io/
Prefix: /opt

Provides: alchemist

%if "x" != "x"
Group: 
%endif

Autoprov: 0
Autoreq: 0
%if "xxdg-utils" != "x" || "x" != "x"
Requires: xdg-utils 
%endif

%define __jar_repack %{nil}
%define package_filelist %{_tmppath}/%{name}.files
%define app_filelist %{_tmppath}/%{name}.app.files
%define filesystem_filelist %{_tmppath}/%{name}.filesystem.files
%define default_filesystem / /opt /usr /usr/bin /usr/lib /usr/local /usr/local/bin /usr/local/lib

# Overridden by command-line arguments in jpackage
%description
alchemist

%global __os_install_post %{nil}

%prep

%build

%install

# Not needed on Fedora but it is on some other distros
mkdir -p "%{buildroot}"

install -d -m 755 %{buildroot}/usr/lib/%{name}

cp -r %{_sourcedir}/opt/%{name} %{buildroot}/usr/lib/

%files
/usr/lib/%{name}
#%license "%{license_install_file}"

%post
xdg-desktop-menu install /usr/lib/%{name}/lib/alchemist-alchemist.desktop
# Create a soft link from usr/bin to the application launcher
ln -f -s "/usr/lib/%{name}/bin/%{name}" "%{_bindir}/%{name}"

%preun
xdg-desktop-menu uninstall /usr/lib/%{name}/lib/alchemist-alchemist.desktop

%clean
rm -rf %{buildroot}
