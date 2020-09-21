
ossh 158.101.13.88

ossh 129.146.255.98

ossh 158.101.14.162

sudo bash


sestatus

setenforce 0

search='SELINUX=enforcing'
replace='SELINUX=disabled'
sed -i "s/${search}/${replace}/g" /etc/selinux/config # Note the double quotes

systemctl stop firewalld
systemctl disable firewalld

yum upgrade -y -q


curl -O https://dl.grafana.com/oss/release/grafana-7.1.5-1.x86_64.rpm
yum localinstall -y grafana-7.1.5-1.x86_64.rpm


search=';allow_loading_unsigned_plugins ='
replace='allow_loading_unsigned_plugins = "oci-datasource,oci-logs-datasource"'
sed -i "s/${search}/${replace}/g" /etc/grafana/grafana.ini
# nano /etc/grafana/grafana.ini

#cd /usr/share/grafana/public/app/plugins/datasource/
systemctl start grafana-server
sleep 5
mkdir -p /var/lib/grafana/plugins
cd /var/lib/grafana/plugins
systemctl stop grafana-server



mkdir oci-logs
cd oci-logs
#curl -O https://objectstorage.us-phoenix-1.oraclecloud.com/p/J4ptp7a0GYJs22Jslsxfe2TN1fdjW-kMNEWpWmJuIl4ZaMOSvyC9D3oUwH2FWVtz/n/intrandallbarnes/b/grafanaBucket/o/dist-500.zip
curl -O https://objectstorage.us-phoenix-1.oraclecloud.com/p/efuRZbNbtwXERR0WCyQR501AlecP0kotHe9HbH0LGtZXc_DQJc-VFfQ0wQi-KcIJ/n/intrandallbarnes/b/grafanaBucket/o/data-only.zip
unzip -qq data-only.zip
rm -rf data-only.zip

cd ..
mkdir oci-mon
cd oci-mon
wget https://github.com/oracle/oci-grafana-plugin/releases/download/v1.1.2/plugin.tar
tar xvf plugin.tar
rm -rf plugin.tar

systemctl restart grafana-server


