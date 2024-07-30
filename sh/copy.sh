#cd /home/f/文档/java_project/test
#mvn clean package
cd /home/f/文档/java_project/e-rasp-renew || exit
cp /home/f/文档/java_project/e-rasp-renew/core/target/rasp-core-shade.jar /home/f/文档/java_project/e-rasp-renew/boot/target
cp /home/f/文档/java_project/e-rasp-renew/agent/target/agent.jar /home/f/文档/java_project/e-rasp-renew/boot/target
#java "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8064" -jar "/home/f/文档/java_project/test/target/test.jar"
