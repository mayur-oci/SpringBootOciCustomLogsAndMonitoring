# source ~/.bash_profile
cd /Users/mraleras/SpringBootOciCustomLogsAndMonitoring/SpringBootOciCustomLogsAndMonitoring
lazygit "latest commit $(date)"
ossh 158.101.3.137

cd ~

pkill -f "java"

rm -rf SpringBootOciCustomLogsAndMonitoring

rm -rf /home/opc/tmp/*

git clone https://github.com/mayur-oci/SpringBootOciCustomLogsAndMonitoring.git

time 3

nohup mvn -f /home/opc/SpringBootOciCustomLogsAndMonitoring/pom.xml spring-boot:run -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005" > ~/appTail.log&

# systemctl status unified-monitoring-agent

# tail -f ~/appTail.log

cd /home/opc/tmp/

tail -f /var/log/unified-monitoring-agent/unified-monitoring-agent.log



