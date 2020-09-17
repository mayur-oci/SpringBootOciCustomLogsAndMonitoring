# source ~/.bash_profile
cd /Users/mraleras/SpringBootOciCustomLogsAndMonitoring/SpringBootOciCustomLogsAndMonitoring
lazygit "latest commit $(date)"
rext 158.101.3.137
'
cd ~

pkill -f "java"

rm -rf SpringBootOciCustomLogsAndMonitoring

git clone https://github.com/mayur-oci/SpringBootOciCustomLogsAndMonitoring.git

time 5

nohup mvn -f /home/opc/SpringBootOciCustomLogsAndMonitoring/pom.xml spring-boot:run -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005" > ~/appTail.log&
'

rext 158.101.3.137 "bash"



