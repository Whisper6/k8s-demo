# 项目简介
此项目是k8s单节点部署案例，为个人学习相关期间做的一个简要demo，
涉及到docker，springboot，k8s知识，建议学习完基本概念用法，在参照demo进行实际操作学习。
# k8s环境搭建
## 配置信息
+ centos7.3 2核+4G #可以使用windows自带的Hyper-v进行虚拟机搭建
## 搭建步骤
### 基础环境准备
#### 安装centos7 epel源
    yum install epel-release.noarch -y
#### 安装基础工具包
    yum install -y vim wget git net-tools htop
#### 关闭防火墙
    systemctl stop firewalld
    systemctl disable firewalld
#### 关闭selinux
    sed -i 's/enforcing/disabled/' /etc/selinux/config
    setenforce 0
    getenforce
#### 关闭swap
    swapoff -a
    vim /etc/fstab #注释掉带有swap那行
#### 将桥接的IPv4流量传递到iptables的链
    cat > /etc/sysctl.d/k8s.conf << EOF
    net.bridge.bridge-nf-call-ip6tables = 1
    net.bridge.bridge-nf-call-iptables = 1
    EOF
    
    sysctl --system #查看设置是否成功
#### 安装docker(目前k8s兼容的最高docker版本)
    wget https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo -O/etc/yum.repos.d/docker-ce.repo
    yum -y install docker-ce-18.06.1.ce-3.el7
    systemctl enable docker && sudo systemctl start docker
    usermod -aG docker `whoami`
#### 部署Kubernetes Master
##### 添加阿里yum源
    cat <<EOF > /etc/yum.repos.d/kubernetes.repo
    [kubernetes]
    name=Kubernetes
    baseurl=https://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64
    enabled=1
    gpgcheck=1
    repo_gpgcheck=1
    gpgkey=https://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg https://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
    EOF
##### 安装kubeadm,kubectl,kubelet
    yum install -y kubeadm-1.14.1 kubectl-1.14.1 kubelet-1.14.1 --disableexcludes=kubernetes
##### 设置kubectl开机启动
    systemctl enable kubelet
##### 初始化K8s,(根据机器的ip addr信息更改apiserver-advertise-address配置)
    kubeadm init \
         --image-repository registry.aliyuncs.com/google_containers \
         --kubernetes-version v1.14.1 \
         --service-cidr=10.1.0.0/16 \
         --pod-network-cidr=10.244.0.0/16

##### 初始化kubectl需要的配置
    mkdir -p $HOME/.kube
    cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
    chown $(id -u):$(id -g) $HOME/.kube/config
通过**kubectl get nodes**可以查看当前node为NotReady状态，需要安装flannel

##### 安装Pod网络插件(CNI)
    kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/a70459be0084506e4ec919aa1c114638878db11b/Documentation/kube-flannel.yml
镜像可能会下载的比较慢，可以通过**docker pull  quay.io/coreos/flannel:v0.11.0-amd64**拉取并查看进度

##### 验证网络插件是否安装成功
通过**kubectl get pods -n kube-system**查看各个pods是否都为run状态，**kubectl get nodes**查看master状态是否为ready

##### 设置pod可以运行在master节点（集群安装不建议这么操作）
    kubectl taint nodes --all node-role.kubernetes.io/master- #输出 ... untainted, 证明设置成功

##### 验证k8s是否安装成功
    kubectl get deploy,pod,svc -n kube-system
<img src="https://github.com/Whisper6/k8s-demo/blob/master/md-image/result.png" />

# docker镜像制作
## dockerfile编写
### 1. 拉取基础镜像
    FROM  centos:7 #使用centos7作为基础镜像
### 2. 上传jdk并解压到指定目录
    ADD ./jdk-8u191-linux-x64.tar.gz /usr/local/ #将jdk8上传到镜像并解压到指定目录
### 3. 配置java环境
    ENV JAVA_HOME /usr/local/jdk1.8.0_191
    ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
    ENV PATH $PATH:$JAVA_HOME/bin
### 4. 打包项目jar包
    mvn clean package # jar包在target目录下
### 4. 上传项目jar包
    COPY ./demo-0.0.1-SNAPSHOT.jar /usr/local/demo-0.0.1-SNAPSHOT.jar
### 5. 运行，并暴露端口
    CMD ["java","-jar","/usr/local/demo-0.0.1-SNAPSHOT.jar",">" ,"log.log","&"] 
    EXPOSE  8080
### 6.在dockerfile所在目录下执行命令，制作镜像
    docker build -t .
### 7. 将镜像托管到阿里云，托管后的地址为（相关方法请参考docker官方文档，上传镜像部分）
    registry.cn-shenzhen.aliyuncs.com/18616568207/springboot-demo:v1
