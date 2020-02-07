rm -rf tmp/ns3/
mkdir tmp
cd tmp
mkdir ns3
cd ns3
wget https://www.nsnam.org/release/ns-allinone-3.29.tar.bz2
tar xjf ns-allinone-3.29.tar.bz2
cd ns-allinone-3.29/ns-3.29
git clone --depth=1 https://github.com/gscaparrotti/ns3asy src/ns3asy
./waf configure --disable-python
./waf build --disable-python