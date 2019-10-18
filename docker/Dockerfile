FROM         centos:7
MAINTAINER    wangjun
 
ADD ./jdk-8u191-linux-x64.tar.gz /usr/local/
ENV JAVA_HOME /usr/local/jdk1.8.0_191
ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
ENV PATH $PATH:$JAVA_HOME/bin
COPY ./demo-0.0.1-SNAPSHOT.jar /usr/local/demo-0.0.1-SNAPSHOT.jar
CMD ["java","-jar","/usr/local/demo-0.0.1-SNAPSHOT.jar",">" ,"log.log","&"] 
EXPOSE  8080