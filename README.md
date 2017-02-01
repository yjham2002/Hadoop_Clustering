# Hadoop Clustering Tutorial
### 하둡 클러스터링 튜토리얼

#### This is on [Github](https://github.com/yjham2002).

- A Project for explaining Basic level of Apache Hadoop based Clustering
- 기초적인 하둡 기반 클러스터링에 대한 해설을 제공하기 위한 프로젝트입니다.

* 본 프로젝트는 Mac OSX와 유닉스 기반의 운영체제를 대상으로 합니다.

### 개요

- 본 내용은 *Chuck Lam 저 Hadoop in Action (지앤선)* 을 참고하여 작성하였습니다.
- 하둡은 방대한 양의 데이터를 다루기 위해 개발된 프레임워크로 구글의 MapReduce 방식을 기반으로 합니다.
- 방대한 데이터 처리를 위해 하둡은 데이터를 받아 코드에 따라 처리하지 않고, 코드를 보내 해당 노드(클러스터 내의 범용PC)에서 연산되도록 합니다.

### 개념

- MapReduce : MapReduce는 네임밸류페어(키, 값) 형태의 데이터를 기반으로 Mapper 및 Reducer를 통해 일련의 연산 과정으로 결과를 도출해내는 방식을 의미합니다.
- HDFS(Hadoop Distributed File System) : 하둡은 분산 저장 및 연산에서 마스터/슬레이브 구조를 가지며, 이의 저장 시스템을 일컫는다.

### 구성요소

- NameNode : 가장 필수적인 하둡 데몬으로, HDFS의 마스터 역할로서 이의 슬레이브인 DataNode 데몬에게 I/O 작업을 지시한다. 기본적으로 하둡은 실패에 비교적 강인하나 NameNode는 유일한 단일 실패 지점으로 자동적인 조치가 취해지지 않는다.
- DataNode : 클러스터에 포함되어 있는 슬레이브 머신에 존재하며, 로컬 파일 시스템에 위치한 파일에 HDFS 블록을 기록하거나 이를 읽는 등의 단순한 기능을 수행한다.
- SecondaryNameNode(SNN) : 클러스터로 구성된 HDFS의 상태를 모니터링하는 보조 성격의 데몬으로, 각 클러스터별로 하나씩을 포함하며 NameNode의 메타데이터 스냅샷을 찍어 NameNode의 실패 복구를 가속화한다.
- JobTracker : 클러스터 노드에서 실행되는 사용자 애플리케이션들을 관리한다.
- TaskTracker : 마스터/슬레이브 구조로, MapReduce 과정의 전제적인 감독을 수행하며, 각 슬레이브 노드에 할당된 작업의 실행을 담당한다.

### 모드

- StandAlone 모드 : 개발 및 디버깅 목적의 환경으로, 독립 실행된다.
- Pseudo-Distributed 모드 : 실행중인 워크스테이션을 하나의 클러스터로 취급하여 실행된다.
- Fully Distributed 모드 : 모든 하둡의 장점을 이용할 수 있는 모드로 실운용 시 이용된다.

- *중요* (Hadoop 실행 시 필수적으로 개방되어야하는 포트 목록)
```sh
8020/14000/50070/50470/8485/50010/50075/50030/50090/50020/8032/8030/8031/8033/8025/8088/8041/8040/8042/9000/9001/10020/13562/19888
```

### Development(Pseudo-Distributed Mode : MapReduce 개발 및 테스트용)

*CentOS 및 RedHat 계열에서의 환경 구축을 원하시는 분께서는 본 파트를 참조하지 마십시오.*

- 본 환경설정에 대한 내용은 [링크](https://dtflaneur.wordpress.com/2015/10/02/installing-hadoop-on-mac-osx-el-capitan/)를 번역 및 재가공한 내용입니다.

#### 0. Mac OSX 사용자는 패키지 관리자 brew를 설치합니다.
```sh
ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
```

#### 1. 시스템 패키지 관리자를 통해 hadoop을 설치합니다.
```sh
brew search hadoop
brew install hadoop
```
-  /usr/local/Cellar/hadoop 에 하둡이 설치됩니다.

#### 2. /usr/local/Cellar/hadoop/(Your Version)/libexec/etc/hadoop/hadoop-env.sh 을 다음과 같이 수정합니다.
- 다음과 같은 부분을 수정합니다.
```sh
export HADOOP_OPTS="$HADOOP_OPTS -Djava.net.preferIPv4Stack=true"
```
- 위 내용을 다음과 같이 정정합니다.
```sh
export HADOOP_OPTS="$HADOOP_OPTS -Djava.net.preferIPv4Stack=true -Djava.security.krb5.realm= -Djava.security.krb5.kdc="
```

#### 3. /usr/local/Cellar/hadoop/(Your Version)/libexec/etc/hadoop/core-site.xml 에 다음과 같은 내용을 추가합니다.
- <configuration> 아래에 다음 내용을 추가합니다.
```xml
<property>
<name>hadoop.tmp.dir</name>
<value>/usr/local/Cellar/hadoop/hdfs/tmp</value>
<description>A base for other temporary directories.</description>
</property>
<property>
<name>fs.default.name</name>
<value>hdfs://localhost:9000</value>
</property>
```

#### 4.  /usr/local/Cellar/hadoop/(Your Version)/libexec/etc/hadoop/mapred-site.xml 에 다음과 같은 내용을 추가합니다.
- <configuration> 아래에 다음 내용을 추가합니다.
```xml
<property>
  <name>mapred.job.tracker</name>
  <value>localhost:9010</value>
</property>
```

#### 5. 하둡 시스템의 시작과 종료를 보다 수월하게 진행하기 위해 다음과 같은 얼라이어스를 등록합니다.
- 주의 : 본 단계에서 하둡을 실행 시 연결에 실패할 수 있습니다.
```sh
$ alias hstart="/usr/local/Cellar/hadoop/2.6.0/sbin/start-dfs.sh;/usr/local/Cellar/hadoop/2.6.0/sbin/start-yarn.sh"
$ alias hstop="/usr/local/Cellar/hadoop/2.6.0/sbin/stop-yarn.sh;/usr/local/Cellar/hadoop/2.6.0/sbin/stop-dfs.sh"
```

#### 6. 하둡 실행을 위해 다음과 같은 스크립트를 통해 환경을 포맷합니다.
```sh
$ hdfs namenode -format
```

#### 7. 홈 경로(~)로 이동하여 SSH를 통해 관리 인터페이스 접속을 위한 RSA 키를 생성합니다.
```sh
$ ssh-keygen -t rsa
```
- 키의 이름은 id_rsa 로 지정하며, 암호는 임의의 내용으로 지정합니다.
- 로컬호스트로의 접속을 위해 유닉스 환경 내의 원격 로그인 설정을 허용해야 합니다. (Mac OSX의 경우, 시스템 환경설정-공유-원격 로그인)

#### 8. 앞서 등록한 얼라이어스인 hstart를 통해 하둡을 실행합니다.
- 하둡 환경 시작
```sh
$ hstart
```
- 하둡 환경 종료
```sh
$ hstop
```
- 본 단계에서는 여러 차례 RSA 키 혹은 리눅스 사용자 계정의 암호를 요구할 수 있습니다.

#### 9. 하둡은 다음과 같은 포트를 통해 관리 인터페이스를 제공하며, 다음과 같은 커맨드를 사용할 수 있습니다.
```sh
Resource Manager: http://localhost:50070
JobTracker: http://localhost:8088/
Node Specific Info: http://localhost:8042/
 
Command
$ jps
7379 DataNode
7459 SecondaryNameNode
7316 NameNode
7636 NodeManager
7562 ResourceManager
7676 Jps 
 
$ yarn // For resource management more information than the web interface.
$ mapred // Detailed information about jobs
```

#### 10. 간단한 Java기반의 단어 색인 프로그램을 통해 하둡을 경험하세요.
```java
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
 
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
 
public class WordCount extends Configured implements Tool {
   private final static LongWritable ONE = new LongWritable(1L);
 
// Mapper Class, Counts words in each line. For each line, break the line into words and emits them as (word, 1)
 
public static class MapClass extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
  private final static IntWritable one = new IntWritable(1);
 private Text word = new Text();
 
public void map(LongWritable key, Text value,
   OutputCollector<text, intwritable> output,
   Reporter reporter) throws IOException {
 
  String line = value.toString();
  StringTokenizer itr = new StringTokenizer(line);
  while (itr.hasMoreTokens()) {
    word.set(itr.nextToken());
     output.collect(word, one);
   }
 }
}
 
// Reducer class that just emits the sum of the input values.
 
public static class Reduce extends MapReduceBase implements Reducer< Text, IntWritable, Text, IntWritable > {
public void reduce(Text key, Iterator values,
 OutputCollector<text, intwritable=""> output,
 Reporter reporter) throws IOException {
      int sum = 0;
      while (values.hasNext()) {
        sum += values.next().get();
      }
      output.collect(key, new IntWritable(sum));
    }
  }
 
static int printUsage() {
System.out.println("wordcount [-m #mappers ] [-r #reducers] input_file output_file");
    ToolRunner.printGenericCommandUsage(System.out);
    return -1;
  }
 
public int run(String[] args) throws Exception {
 
    JobConf conf = new JobConf(getConf(), WordCount.class);
    conf.setJobName("wordcount");
 
// the keys are words (strings)
   conf.setOutputKeyClass(Text.class);
// the values are counts (ints)
   conf.setOutputValueClass(IntWritable.class);
 
   conf.setMapperClass(MapClass.class);
// Here we set the combiner!!!!
   conf.setCombinerClass(Reduce.class);
   conf.setReducerClass(Reduce.class);
 
  List other_args = new ArrayList();
   for(int i=0; i < args.length; ++i) {
     try {
        if ("-m".equals(args[i])) {
conf.setNumMapTasks(Integer.parseInt(args[++i]));
        } else if ("-r".equals(args[i])) {
conf.setNumReduceTasks(Integer.parseInt(args[++i]));
        } else {
          other_args.add(args[i]);
        }
      } catch (NumberFormatException except) {
        System.out.println("ERROR: Integer expected instead of " + args[i]);
        return printUsage();
      } catch (ArrayIndexOutOfBoundsException except) {
        System.out.println("ERROR: Required parameter missing from " +
            args[i-1]);
        return printUsage();
      }
    }
// Make sure there are exactly 2 parameters left.
   if (other_args.size() != 2) {
      System.out.println("ERROR: Wrong number of parameters: " +
          other_args.size() + " instead of 2.");
      return printUsage();
    }
    FileInputFormat.setInputPaths(conf, other_args.get(0));
    FileOutputFormat.setOutputPath(conf, new Path(other_args.get(1)));
 
    JobClient.runJob(conf);
    return 0;
  }
 
public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new Configuration(), new WordCount(), args);
    System.exit(res);
  }
}
```

#### 11. 다음과 같이 컴파일한 뒤, 해당 스크립트를 실행하여 하둡을 작동시키십시오.
- 작성한 프로그램의 컴파일
```sh
$ javac WordCount.java -cp $(hadoop classpath)
```

- 하둡으로의 실행 스크립트
```sh
$ hadoop jar ./target/bdp-1.3.jar dataSet3.txt  dataOutput1
```

#### 12. 프로그램의 입력 파일은 다음과 같이 업로드합니다.
```sh
$ hdfs dfs -put book.txt /data
$ hdfs dfs -ls /
```

## 적용

### Development(Fully Distributed Mode - 라이브 하둡 서비스 운용)

- 본 내용은 [링크](http://atoz91.tistory.com/50)를 참조하였으며, 위 Pseudo-Distributed 모드 설치 내용 이후의 작업을 대상으로 합니다.

#### 1. 환경변수를 설정합니다.

- Mac OSX의 경우, Java 환경변수 및 Hadoop의 설치경로가 다를 수 있으니 주의하십시오.

```sh
#Java Setting
export JAVA_HOME=$HOME/jdk
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=$JAVA_HOME/lib:$CLASSPATH

# Hadoop Path
export HADOOP_PREFIX=$HOME/hadoop
export PATH=$PATH:$HADOOP_PREFIX/bin
export PATH=$PATH:$HADOOP_PREFIX/sbin
export HADOOP_HOME=$HOME/hadoop
export HADOOP_MAPRED_HOME=${HADOOP_PREFIX}
export HADOOP_COMMON_HOME=${HADOOP_PREFIX}
export HADOOP_HDFS_HOME=${HADOOP_PREFIX}
export YARN_HOME=${HADOOP_PREFIX}
export HADOOP_YARN_HOME=${HADOOP_PREFIX}
export HADOOP_CONF_DIR=${HADOOP_PREFIX}/etc/hadoop

# Native Path
export HADOOP_COMMON_LIB_NATIVE_DIR=${YARN_HOME}/lib/native
export HADOOP_OPTS="-Djava.library.path=$YARN_HOME/lib/native"
```

#### 2. Key Distribution

- 마스터 노드(마스터 PC)에서 위 Pseudo-Distributed 모드 설정 시 생성했던 키를 authrized_keys로 복제한다.(키 경로 : ~/.ssh)

- 만약 위 Pseudo-Distributed 모드 설정 과정에서 키 생성을 하지 않았다면 생성한다.

- 주의 : Mac OSX의 경우, authorized_keys 대신 known_hosts가 이용되는 경우가 있습니다.

```sh
~$ ssh-keygen -t rsa -P ""
~$ cd ~/.ssh
~/.ssh$ cat id_rsa.pub >> authorized_keys
```

- 위 키 생성 및 복제 과정을 모든 노드에서 동일하게 수행하고, 이를 마스터 노드의 authorized_keys로 전송합니다.

```sh
~$ ssh hadoop@slave01 'cat ~/.ssh/id_rsa.pub' >> ~/.ssh/authorized_keys
~$ ssh hadoop@slave02 'cat ~/.ssh/id_rsa.pub' >> ~/.ssh/authorized_keys
```

- 마스터 노드의 authorized_keys로 모든 키의 복제가 완료되면, 마스터 노드의 공개키를 모든 타 노드들에게 배포합니다.

```sh
~$ scp authorized_keys hadoop@slave01:~/.ssh/authorized_keys
~$ scp authorized_keys hadoop@slave02:~/.ssh/authorized_keys
```

- 주의 : ~/.ssh의 권한은 700이고, authorized_keys의 권한은 600이어야 합니다.

#### 3. 디렉토리 생성

- 다음과 같은 스크립트를 통해 필요한 디렉토리를 생성합니다.

- 아래의 디렉토리명은 사용자가 이후 과정에서 등록할 때 동일하게 작성해야 하므로 주의해야 하며, 임의의 이름이 가능합니다.

```sh
~$ mkdir –p ${HADOOP_PREFIX}/hadoop/hdfs/namenode
~$ mkdir –p ${HADOOP_PREFIX}/hadoop/hdfs/datanode
~$ mkdir –p ${HADOOP_PREFIX}/hadoop/mapred/system
~$ mkdir –p ${HADOOP_PREFIX}/hadoop/mapred/local
```

#### 4. Fully Distributed 모드의 설정

- Hadoop의 모든 설정파일은 $HADOOP_PREFIX/etc/hadoop 내에 존재하며 내용은 다음과 같습니다.

- Hadoop의 설정 구성요소

|파일명|형식|해설|
|:-:|:-:|:-:|
|hadoop-env.sh|Bash 스크립트|Hadoop의 구동을 위한 스크립트에서 사용되는 환경변수|
|core-site.xml|Hadoop Setting XML|HDFS 및 MapReduce에 공통적으로 사용되는 I/O설정과 같은 Hadoop 코어를 위한 환경 설정 구성|
|hdfs-site.xml|Hadoop Setting XML|NameNode, SecondaryNameNode, DataNode 등과 같은 HDFS 데몬을 위한 환경 설정 구성|
|mapred-site.xml|Hadoop Setting XML|JobTracker, TaskTracker와 같은 MapReduce 데몬을 위한 환경 설정 구성|
|masters|Text|SecondaryNameNode를 구동시킬 컴퓨터의 목록(라인당 하나의 컴퓨터)|
|slaves|Text|DataNode와 TaskTracker를 구동시킬 컴퓨터의 목록(라인당 하나의 컴퓨터)|
|hadoop-metric.properties|Java Property|매트릭스가 Hadoop에서 어떻게 표시되는지를 제어하는 속성|
|log4i.properties|Java Property|시스템 로그 파일을 위한 속성, NameNode의 감시 로그, TaskTracker의 자식 프로세스의 수행 로그|

- 설정 구성요소별 세부 사항

세부설정 사항은 추후 업데이트됩니다.

#### 5. 데몬의 실행과 시작

- 최초 실행 시 nameNode를 포맷합니다.

```sh
~$ hdfs namenode -format
```

- 데몬에 따른 시작 및 종료 스크립트 파일

|-|시작|종료|
|:-:|:-:|:-:|
|All|start-all.sh|stop-all.sh|
|HDFS Only|start-dfs.sh|stop-dfs.sh|
|YARN Only|start-yarn.sh|stop-yarn.sh|

- 위의 Pseudo-Distributed Mode 설정 시 구성한 얼라이어스인 *hstart*나 *hstop*을 이용하거나, 유사하게 얼라이어스할 수 있습니다.

## CentOS 및 Redhat 계열 환경 구성

#### Prerequisite (본 환경에서는 root를 이용하여 진행하기에 적절한 내용 수정이 필수적임)

- Java 설치 및 환경 변수 등록 ($JAVA_HOME)
- yum 을 통한 wget, ssh 설치
- Hadoop 을 이용할 계정 생성
 
```sh
# adduser hadoop
# passwd hadoop
```

#### 키 생성 및 인증

```sh
# su - hadoop
$ ssh-keygen -t rsa
$ cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
$ chmod 0600 ~/.ssh/authorized_keys

$ ssh localhost
$ exit
```

#### Downloading Hadoop

```sh
$ cd ~
$ wget http://apache.claz.org/hadoop/common/hadoop-2.7.1/hadoop-2.7.1.tar.gz
$ tar xzf hadoop-2.7.1.tar.gz
$ mv hadoop-2.7.1 hadoop
```

#### Setting Environmental Variables

```sh
export HADOOP_HOME=/YOUR-HADOOP-USER-NAME/hadoop
export HADOOP_INSTALL=$HADOOP_HOME
export HADOOP_MAPRED_HOME=$HADOOP_HOME
export HADOOP_COMMON_HOME=$HADOOP_HOME
export HADOOP_HDFS_HOME=$HADOOP_HOME
export YARN_HOME=$HADOOP_HOME
export HADOOP_COMMON_LIB_NATIVE_DIR=$HADOOP_HOME/lib/native
export PATH=$PATH:$HADOOP_HOME/sbin:$HADOOP_HOME/bin
export JAVA_HOME=/usr/lib/jvm/java-1.8.0
export HADOOP_OPTS="-Djava.library.path=$HADOOP_HOME/lib"

alias hd_start="$HADOOP_HOME/sbin/start-all.sh"
alias hd_stop="$HADOOP_HOME/sbin/stop-all.sh"
```

- 적용

```sh
$ source ~/.bashrc
```

#### Formatting and Starting

```sh
$ hdfs namenode -format
$ hd_start (Aliased as above)
```

- namenode의 포맷이 정상적으로 이루어지지 않고, 호스트 네임과 관련된 오류 발생 시 hostname을 localhost로 수정해야 함.

```sh
$ hostname localhost
```

## 이용 사례연구

