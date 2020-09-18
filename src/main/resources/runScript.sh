# source ~/.bash_profile
cd /Users/mraleras/SpringBootOciCustomLogsAndMonitoring/SpringBootOciCustomLogsAndMonitoring
lazygit "latest commit $(date)"
ossh 158.101.3.137

cd ~

pkill -f "java"

rm -rf SpringBootOciCustomLogsAndMonitoring

rm -rf /home/opc/tmp/*

echo '/home/opc/tmp/logger.log.0'

git clone https://github.com/mayur-oci/SpringBootOciCustomLogsAndMonitoring.git

time 3

# nohup mvn -f /home/opc/SpringBootOciCustomLogsAndMonitoring/pom.xml spring-boot:run -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005" > ~/appTail.log&

nohup mvn -f /home/opc/SpringBootOciCustomLogsAndMonitoring/pom.xml spring-boot:run -Drun.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005" > ~/appTail.log&

#nohup mvn -f /home/opc/SpringBootOciCustomLogsAndMonitoring/pom.xml spring-boot:run > ~/appTail.log&

# systemctl status unified-monitoring-agent

# tail -f ~/appTail.log

cd /home/opc/tmp/

# tail -f /var/log/unified-monitoring-agent/unified-monitoring-agent.log



