
#!/bin/bash
BUILD_JAR=$(ls /home/ubuntu/Yakchat_Server_Test/build/libs/*.jar)
JAR_NAME=$(basename $BUILD_JAR)
echo "> build 파일명: $JAR_NAME" >> /home/ubuntu/Yakchat_Server_Test/deploy.log

echo "> build 파일 복사" >> /home/ubuntu/Yakchat_Server_Test/deploy.log
DEPLOY_PATH=/home/ubuntu/Yakchat_Server_Test/
cp $BUILD_JAR $DEPLOY_PATH

#echo "> application.yml 파일 복사" >> /home/ubuntu/Yakchat_Server_Test/deploy.log
#YML_COPY_PATH = /home/ubuntu/Yakchat_Server/src/main/resources
#YML_PATH=/home/ubuntu/Yakchat_Server_Test/src/main
#cp -r $YML_COPY_PATH $YML_PATH

echo "> 현재 실행중인 애플리케이션 pid 확인" >> /home/ubuntu/Yakchat_Server_Test/deploy.log
CURRENT_PID=$(pgrep -f .jar)

if [ -z $CURRENT_PID ]
then
  echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다." >> /home/ubuntu/Yakchat_Server_Test/deploy.log
else

  echo "> kill -9 $CURRENT_PID"
  kill -9 $CURRENT_PID
  sleep 5
fi

DEPLOY_JAR=$DEPLOY_PATH$JAR_NAME
echo "> DEPLOY_JAR 배포"    >> /home/ubuntu/Yakchat_Server_Test/deploy.log
source /home/ubuntu/.bashrc
nohup java -jar $DEPLOY_JAR >> /home/ubuntu/Yakchat_Server_Test/deploy.log 2>/home/ubuntu/Yakchat_Server_Test/deploy_err.log &