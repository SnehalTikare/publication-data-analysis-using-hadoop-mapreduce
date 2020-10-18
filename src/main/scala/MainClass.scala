import com.typesafe.config.{Config, ConfigFactory}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{IntWritable, LongWritable, Text}
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.hadoop.util.GenericOptionsParser
import org.slf4j.{Logger, LoggerFactory}

object MainClass {
  def main(args :Array[String]): Unit = {
    {
      val conf = new Configuration
      val config: Config = ConfigFactory.load()
      conf.set("xmlinput.start",config.getString("START_TAGS"))
      conf.set("xmlinput.end", config.getString("END_TAGS"))
      val otherArgs: Array[String] = new GenericOptionsParser(conf, args).getRemainingArgs
      if (otherArgs.length < 2) {
        System.exit(2)
      }
      val logger: Logger = LoggerFactory.getLogger(this.getClass)
      logger.info("Starting task 1 Top 10 authors for each of the venues");
      {
        conf.set("mapred.textoutputformat.separator", ",")
        val job1: Job = Job.getInstance(conf, "Top 10 Authors")
        job1.setJarByClass(MainClass.getClass)
        job1.setInputFormatClass(classOf[XmlInputFormatWithMultipleTags])
        job1.setMapperClass(classOf[AuthorsVenueMapper])
        job1.setReducerClass(classOf[AuthorsVenueReducer])
        job1.setOutputKeyClass(classOf[Text])
        job1.setOutputValueClass(classOf[Text])
        for (i <- 0 until otherArgs.length - 1) {
          FileInputFormat.addInputPath(job1, new Path(otherArgs(i)))
        }
        FileOutputFormat.setOutputPath(job1, new Path(otherArgs(otherArgs.length - 1)+ config.getString("output_job1")))
        // FileInputFormat.addInputPath(job, new Path(args[0]));
        //FileOutputFormat.setOutputPath(job, new Path(args[1]));
        val out = new Path(args(1))
        out.getFileSystem(conf).delete(out)
        job1.waitForCompletion(true)
      }

      logger.info("Starting task2 - The list of authors who published without interruption for N years where 10 <= N");
      {
      val job2: Job = Job.getInstance(conf, "Authors with N continuous years of publcn")
      job2.setJarByClass(MainClass.getClass)
      job2.setInputFormatClass(classOf[XmlInputFormatWithMultipleTags])
      job2.setMapperClass(classOf[AuthorsNYearsMapper])
      job2.setReducerClass(classOf[AuthorsNYearsReducer])
      job2.setOutputKeyClass(classOf[Text])
      job2.setOutputValueClass(classOf[IntWritable])
      for (i <- 0 until otherArgs.length - 1) {
        FileInputFormat.addInputPath(job2, new Path(otherArgs(i)))
      }
      FileOutputFormat.setOutputPath(job2, new Path(otherArgs(otherArgs.length - 1)+ config.getString("output_job2")))
      // FileInputFormat.addInputPath(job, new Path(args[0]));
      //FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job2.waitForCompletion(true)
    }
      logger.info("Starting task3 - Venues with list of publications that contains only one author.");
      {
        val job3: Job = Job.getInstance(conf, "Single Author")
        job3.setJarByClass(MainClass.getClass)
        job3.setInputFormatClass(classOf[XmlInputFormatWithMultipleTags])
        job3.setMapperClass(classOf[PublicationOneAuthorMapper])
        job3.setReducerClass(classOf[PublicationOneAuthorReducer])
        job3.setOutputKeyClass(classOf[Text])
        job3.setOutputValueClass(classOf[Text])
        for (i <- 0 until otherArgs.length - 1) {
          FileInputFormat.addInputPath(job3, new Path(otherArgs(i)))
        }
        FileOutputFormat.setOutputPath(job3, new Path(otherArgs(otherArgs.length - 1)+ config.getString("output_job3")))
        job3.waitForCompletion(true)
      }
      logger.info("Starting task4 - The list of publications for each venue that contain the highest number of authors for each of these venues.");
      {
        val job4: Job = Job.getInstance(conf, "publications for each venue that contain the highest number of authors ")
        job4.setJarByClass(MainClass.getClass)
        job4.setInputFormatClass(classOf[XmlInputFormatWithMultipleTags])
        job4.setMapperClass(classOf[AuthorPublicationMapper])
        job4.setReducerClass(classOf[AuthorPublicationReducer])
        job4.setOutputKeyClass(classOf[Text])
        job4.setOutputValueClass(classOf[Text])
        for (i <- 0 until otherArgs.length - 1) {
          FileInputFormat.addInputPath(job4, new Path(otherArgs(i)))
        }
        FileOutputFormat.setOutputPath(job4, new Path(otherArgs(otherArgs.length - 1)+ config.getString("output_job4")))
        job4.waitForCompletion(true)
      }
      logger.info("Starting task5 - The list of top 100 authors in the descending order who publish with most co-authors");
      {
        val job5: Job = Job.getInstance(conf, "most co-authors")
        job5.setJarByClass(MainClass.getClass)
        job5.setInputFormatClass(classOf[XmlInputFormatWithMultipleTags])
        job5.setMapperClass(classOf[MostCoAuthorMapper])
        job5.setReducerClass(classOf[MostCoAuthorReducer])
        job5.setOutputKeyClass(classOf[Text])
        job5.setOutputValueClass(classOf[Text])
        for (i <- 0 until otherArgs.length - 1) {
          FileInputFormat.addInputPath(job5, new Path(otherArgs(i)))
        }
        FileOutputFormat.setOutputPath(job5, new Path(otherArgs(otherArgs.length - 1)+ config.getString("output_job5")))
        job5.waitForCompletion(true)
        //System.exit(if (job5.waitForCompletion(true)) 0 else 1)
      }
      logger.info("Starting task6 - The list of top 100 authors with no co-authors");
      {
        val job6: Job = Job.getInstance(conf, "No co-authors")
        job6.setJarByClass(MainClass.getClass)
        job6.setInputFormatClass(classOf[XmlInputFormatWithMultipleTags])
        job6.setMapperClass(classOf[MostCoAuthorMapper])
        job6.setReducerClass(classOf[NoCoAuthorReducer])
        job6.setOutputKeyClass(classOf[Text])
        job6.setOutputValueClass(classOf[Text])
        for (i <- 0 until otherArgs.length - 1) {
          FileInputFormat.addInputPath(job6, new Path(otherArgs(i)))
        }
        FileOutputFormat.setOutputPath(job6, new Path(otherArgs(otherArgs.length - 1)+ config.getString("output_job6")))
        System.exit(if (job6.waitForCompletion(true)) 0 else 1)
        //job6.waitForCompletion(true)
      }
    }

  }
}
