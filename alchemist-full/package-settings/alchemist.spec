# Overridden by command-line arguments in jpackage
Summary: alchemist
Name: alchemist
Version: APPLICATION_VERSION
Release: 1
License: Unknown
Vendor: Unknown

%if "xhttps://alchemistsimulator.github.io/" != "x"
URL: https://alchemistsimulator.github.io/
%endif

%if "x/opt" != "x"
Prefix: /opt
%endif

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

%description
alchemist

%global __os_install_post %{nil}

%prep

%build

%install
rm -rf %{buildroot}
install -d -m 755 %{buildroot}/usr/lib/%{name}
cp -r %{_sourcedir}/opt/%{name}/* %{buildroot}/usr/lib/%{name}
#%define license_install_file %{_defaultlicensedir}/%{name}-%{version}/
#install -d -m 755 "%{buildroot}%{dirname:%{license_install_file}}"
#install -m 644 "/home/zimbrando/Projects/Alchemist/LICENSE.md" "%{buildroot}%{license_install_file}"
(cd %{buildroot} && find . -type d) | sed -e 's/^\.//' -e '/^$/d' | sort > %{app_filelist}
{ rpm -ql filesystem || echo %{default_filesystem}; } | sort > %{filesystem_filelist}
comm -23 %{app_filelist} %{filesystem_filelist} > %{package_filelist}
sed -i -e 's/.*/%dir "&"/' %{package_filelist}
(cd %{buildroot} && find . -not -type d) | sed -e 's/^\.//' -e 's/.*/"&"/' >> %{package_filelist}
sed -i -e 's|"%{license_install_file}"||' -e '/^$/d' %{package_filelist}

%files -f %{package_filelist}
#%license "%{license_install_file}"

%post
xdg-desktop-menu install /opt/alchemist/lib/alchemist-alchemist.desktop
# Create a soft link from usr/bin to the application launcher
ln -s "/usr/lib/%{name}/bin/%{name}" "%{buildroot}/%{_bindir}/%{name}"

%preun
xdg-desktop-menu uninstall /opt/alchemist/lib/alchemist-alchemist.desktop

%clean
