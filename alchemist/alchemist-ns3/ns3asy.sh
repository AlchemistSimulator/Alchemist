source ns3asy_script_config.sh
rm -rf tmp/
mkdir -p tmp/ns3
cd tmp/ns3
echo "downloading required files for ns3 and ns3asy..."
curl ${NS3_LINK} --output ns-allinone-3.29.tar.bz2 --silent
tar xjf ns-allinone-3.29.tar.bz2 > /dev/null 2>&1
cd ns-allinone-3.29/ns-3.29
git clone --depth=1 ${NS3ASY_LINK} src/ns3asy > /dev/null 2>&1
echo "configuring ns3 build..."
./waf configure --disable-python > /dev/null 2>&1
echo "building ns3 (this may take some minutes)..."
./waf build --disable-python > /dev/null 2>&1
echo "ns3 build done."
