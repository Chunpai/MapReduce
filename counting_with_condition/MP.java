//Map Reduce version
//modify the parameters (thresholds) in the code

package MP;

import java.io.IOException;
import java.util.Arrays;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class MP extends Configured implements Tool {
    public static void main(String[] args) throws Exception {
        System.out.println(Arrays.toString(args));
        int res = ToolRunner.run(new Configuration(), new MP(), args);
      
        System.exit(res);
    }
    

    @Override
    public int run(String[] args) throws Exception {
        //job object configuration
        System.out.println(Arrays.toString(args));
        Job job = new Job(getConf(), "MP");
        job.setJarByClass(MP.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);
        
        //set mapper, combiner, reducer classes
        job.setMapperClass(Map.class);
        //combiner class does have its own interface
        //it must implement Reducer interface and reduce() method, combiner will be called on each map output key
        //the combiner class's reduce() method mush have the same input and output key-value types as the reducer class
        //job.setCombinerClass(Recude.class);        
        job.setReducerClass(Reduce.class);
        
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);

        return 0;
    }
   

    // Map Class
    public static class Map extends Mapper<LongWritable, Text, Text, DoubleWritable> {
        private final static DoubleWritable rate = new DoubleWritable();  // hadoop supported data types
        private Text book = new Text();
    
        //The Mapper implementation, via the map method, processes one line at a time, 
        //as provided by the specified TextInputFormat.
        // It then splits the line into tokens separated by whitespaces, 
        // via the StringTokenizer, and emits a key-value pair of < <word>, 1>.    
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] tokens = value.toString().split(",");  //split line by comma, and store in tokens
            book.set(tokens[0]);       //assign string to Text type
            rate.set( Double.parseDouble(tokens[2]) );
            context.write(book, rate); // set the output <key,value> pair
      }
   }
    

    //Reduce Class
    public static class Reduce extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
        @Override
        public void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {
            double sum = 0;
            double avg = 0;
            int count = 0;
            for (DoubleWritable val : values){
                count++;
                sum += val.get();
            }
            if (count >= 5){
                avg = sum / count;
                if (avg >= 4.95){
                    context.write(key, new DoubleWritable(avg));
                }
            }           
        }
   }




}
