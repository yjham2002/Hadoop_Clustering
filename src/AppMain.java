import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * Created by Ham.EuiJin on 2017-01-23.
 */
public class AppMain {
    public static void main(String[] args){
        args = new String[]{"input.txt", "output.txt"};

        Configuration conf = new Configuration();
        String[] otherArgs = null;
        try {
            otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

            if (otherArgs.length != 2) {
                System.err.println("Usage: wordcount <in> <out>");
                System.exit(2);
            }
            Job job = new Job(conf);

            job.setJarByClass(TaskMapper.class);
            job.setMapperClass(TaskMapper.TokenizerMapper.class);
            job.setCombinerClass(TaskMapper.IntSumReducer.class);
            job.setReducerClass(TaskMapper.IntSumReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(IntWritable.class);
            FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
            FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
            System.exit(job.waitForCompletion(true) ? 0 : 1);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
