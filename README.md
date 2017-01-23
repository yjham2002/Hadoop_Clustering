# Hadoop Clustering Tutorial
### 하둡 클러스터링 튜토리얼

- A Project for explaining Basic level of Apache Hadoop based Clustering
- 기초적인 하둡 기반 클러스터링에 대한 해설을 제공하기 위한 프로젝트입니다.

* 본 프로젝트는 Mac OSX와 유닉스 기반의 운영체제를 대상으로 합니다.

### 개요
- 본 내용은 *Chuck Lam 저 Hadoop in Action (지앤선)* 을 참고하여 작성하였습니다.
- 하둡은 방대한 양의 데이터를 다루기 위해 개발된 프레임워크로 구글의 MapReduce 방식을 기반으로 합니다.

### 용어 및 개념 
- MapReduce : MapReduce는 네임밸류페어(키, 값) 형태의 데이터를 기반으로 Mapper 및 Reducer를 통해 일련의 연산 과정으로 결과를 도출해내는 방식을 의미합니다.
- 

### Development
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
