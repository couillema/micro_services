%define _SVCNAME     msv-vision-green

Summary:	Microservices Vision
Name:		visionmicroservicesgreen
Vendor:		Telintrans
URL:		http://www.telintrans.fr
Version:	##VERSION##
Release:	1.tlt
License:	GPL
Group:		Applications/Java/j2ee
BuildRoot:	%{_tmppath}/%{name}
BuildArch:	noarch
Packager:	Telintrans <http://www.telintrans.fr>
Requires(pre):	tar
Requires:	j2sdk1.7
Source:		%{name}-%{version}.tgz
AutoReqProv:	yes

%description
############
. Ce serveur tomee gere les reponses http de l'application calculretard

. Il est accede sur les entres DNS:
   - sbrange.chronopost.fr

. Il ecoute sur les ports:
        - srvport 58710 (cf.  conf/svcjava/te-sbrangei1.conf)
        - srvport 58720 (cf.  conf/svcjava/te-sbrangei2.conf)
        - srvhttpport 58711 (cf.  conf/svcjava/te-sbrangei1.conf)
        - srvhttpport 58721 (cf.  conf/svcjava/te-sbrangei2.conf)
        - srvjkport 55712  (cf.  conf/svcjava/te-sbrangei1.conf)
        - srvjkport 55722  (cf.  conf/svcjava/te-sbrangei2.conf)


. Il utilise les entres DNS suivantes:
        -sasuaccescolisi3.chronopost.fr


%pre
############
%{_LOG_PRE}

export PATH="/usr/local/bin:$PATH"

# java user
id %{_JAVA_USER} >/dev/null 2>&1
if [ ! "$?" = "0" ] ; then
  echo  "%{_JAVA_USER} must be created before installation"
fi

# if update, desinst old
if [ $_NUMINST -gt 1 ] ; then
	cd %{_JAVA_J2EE}/%{_SVCNAME}
	echo "Desinstallation de ancien service ..."
	svcjava ./conf/%{_SVCNAME}.conf uninstall
fi


%post
############
%{_LOG_POST}

export PATH="/usr/local/bin:$PATH"

# installation en service
cd %{_JAVA_J2EE}/%{_SVCNAME}
echo "Installation du service ..."
svcjava ./conf/%{_SVCNAME}.conf install


%preun
############
%{_LOG_PREUN}

export PATH="/usr/local/bin:$PATH"

if [ $_NUMINST -ne 0 ] ; then exit 0 ; fi

# desinstallation service
cd %{_JAVA_J2EE}/%{_SVCNAME}
echo "Desinstallation du service ..."	
svcjava ./conf/%{_SVCNAME}.conf uninstall


%prep

%setup

%build
############

%install
############
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT/%{_JAVA_J2EE}/%{_SVCNAME}
mv * $RPM_BUILD_ROOT/%{_JAVA_J2EE}/%{_SVCNAME}

%clean
############
rm -rf $RPM_BUILD_ROOT
rm -rf $RPM_BUILD_DIR/%{name}-%{version}

%files
############
%defattr(-,%{_JAVA_USER},%{_JAVA_GROUP})
%{_JAVA_J2EE}/%{_SVCNAME}/

%changelog
############
