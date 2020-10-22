import java.{lang, util}
import java.io.IOException

import com.typesafe.config.ConfigFactory
import org.apache.hadoop.io.{IntWritable, LongWritable, Text}
import org.apache.hadoop.mapreduce.Reducer
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer


/**
 * This file contains reducers for all the tasks
 */
class DblpReducer{
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  /**
   * Function to find Maximum number of consecutive years in the given list
   * @param seq - Sorted list of year
   * @return Maximum number of consecutive year
   */
  def isConsecutive(seq: List[Int]): Int = {
    val intermediate = mutable.ArrayBuffer[Int](1)
    val lis = seq.distinct.sliding(2).foreach{
      case y1 :: tail =>
        if(tail.nonEmpty){
          val y2 = tail.head
          if(y2 - y1 == 1) intermediate(intermediate.size - 1) += 1
          else intermediate += 1
        }
    }
    intermediate.max
  }
}

/**
 * Reducer for Task 1 - Top ten published authors at each venue
 * Reducer receives venue and list of authors for the venue
 * Reducer emits the venue and top ten authors who have published under the same venue
 */

class AuthorsVenueReducer extends Reducer[Text, Text, Text, Text] {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  logger.info("Running the reducer for Task 1 - AuthorsVenueReducer")
  @throws[IOException]
  @throws[InterruptedException]
  override def reduce(key: Text, values: lang.Iterable[Text], context: Reducer[Text, Text, Text, Text]#Context): Unit = {
    val hashmap = new util.HashMap[String, Integer]()
    values.forEach(x => hashmap.put(x.toString, hashmap.getOrDefault(x.toString, 0) + 1))
    val scalamap = hashmap.asScala
    val sortedmap = scalamap.toSeq.sortWith(_._2 > _._2).take(10)
    sortedmap.foreach(x => context.write(new Text(key.toString.replace(",","")), new Text(x._1 + " " + x._2)))
  }
}

/**
 * Reducer for Task 2 - The list of authors who published without interruption for N years where 10 <= N
 * Reducer receives a key of authors and list of years they have published in
 * Reducer emits authors and the number of consecutive years they have published if it greater than or equal to 10
 */
class AuthorsNYearsReducer extends Reducer[Text,IntWritable,Text,IntWritable]{
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  logger.info("Running the reducer for Task 2 - AuthorsNYearsReducer")
  val config = ConfigFactory.load()
  @throws[IOException]
  @throws[InterruptedException]
  override def reduce(key: Text, values: lang.Iterable[IntWritable], context: Reducer[Text, IntWritable, Text, IntWritable]#Context): Unit ={
    val years = new ListBuffer[Int]
    values.asScala.foreach( value => years.addOne(value.get()))
    if(years.length > 9){
      val count = new DblpReducer().isConsecutive(years.toList.sorted)
      if(count >= config.getInt("consecutive_years")) context.write(key,new IntWritable(count))
    }
  }
}

/**
 * Reducer for Task 3 - The list of publications that contains only one author for each venue
 * Reducer receives a key venue and List of publications with one author
 * Reducer emits the venue and list of publications with one author
 */
class PublicationOneAuthorReducer extends Reducer[Text, Text, Text, TextArrayWritable]{
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  logger.info("Running the reducer for Task 3 - PublicationOneAuthorReducer")
  @throws[IOException]
  @throws[InterruptedException]
  override def reduce(key: Text, values: lang.Iterable[Text], context: Reducer[Text, Text, Text, TextArrayWritable]#Context): Unit = {
    val uniquevals = values.asScala
    val elements = uniquevals.map(x => new Text(x.toString.replace(",",""))).toArray
    val arrayWritable = new TextArrayWritable(elements)
    context.write(new Text(key.toString.replace(",","")),arrayWritable)
  }
}

/**
 * Reducer for Task 4 - The list of publications for each venue that
 * contain the highest number of authors for each of these venues.
 * Reducer receives venue and list of pairs of publication and number of authors in the publication
 * Reducer emits the venue and list of publications which contains the highest number of authors under that venue
 */
class AuthorPublicationReducer extends Reducer[Text, Text,Text, TextArrayWritable]{
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  logger.info("Running the reducer for Task 4 - AuthorPublicationReducer")
  @throws[IOException]
  @throws[InterruptedException]
  override def reduce(key: Text, values: lang.Iterable[Text], context: Reducer[Text, Text, Text, TextArrayWritable]#Context): Unit = {
    val publicationCount = new ListBuffer[String]
    var max_count = 0
    //val authorcount = new ListBuffer[Integer]
    val listResult = new ListBuffer[Text]
    try{
    values.asScala.foreach(value => {
      val split_value = value.toString.split("::")
      publicationCount.addOne(value.toString)
      //authorcount.addOne(split_value(1).toInt)
      if(split_value(1).toInt > max_count)
        max_count = split_value(1).toInt
    })}
    catch{
      case _: Throwable => println("Conversion ignored")
    }
    try{
    publicationCount.foreach(pub => {
      val count  = pub.split("::")(1)
      if(count.toInt == max_count)
      {
        listResult.addOne(new Text("\"" +pub.split("::" )(0).replace(",","")+ "\"" ))
      }
    })
    val arrayWritable = new TextArrayWritable(listResult.toArray)
    context.write(new Text(key.toString.replace(",","")),arrayWritable)
    }
    catch{
      case _: Throwable => println("Exception ignored")
    }
  }
}

/**
 * Reducer for Task 5 - - The list of top 100 authors in the descending order who publish with most co-authors
 * and the list of 100 authors who publish without any co-authors
 * Reducer receives author and list of co-authors with whom the author has published
 * Reducer emits the list of Top 100 authors with highest number of co-authors
 * and the list of top 100 authors with no co authors or least number of co authors
 */
class MostCoAuthorReducer extends Reducer[Text, Text,Text, LongWritable]{
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  val hashmap = new util.HashMap[String, Long]()
  logger.info("Running the reducer for Task 5 - MostCoAuthorReducer")
  @throws[IOException]
  @throws[InterruptedException]
  override def reduce(key: Text, values: lang.Iterable[Text], context: Reducer[Text, Text, Text, LongWritable]#Context): Unit = {

    val coAuthorList = values.asScala.toStream.distinct
    val filterlist = coAuthorList.filter(_!=new Text("x"))
    hashmap.put(key.toString,filterlist.length )
  }

  @throws[IOException]
  @throws[InterruptedException]
  override def cleanup(context: Reducer[Text, Text,Text, LongWritable]#Context): Unit = {
    try{
    val scalamap = hashmap.asScala
    val sortedmap_desc = scalamap.toSeq.sortWith(_._2 > _._2).take(100)
    context.write(new Text("Top 100 authors with most co-authors"), new LongWritable())
    logger.info("Writing Top 100 authors with most co-author")
    context.write(new Text(""),new LongWritable())
    sortedmap_desc.foreach(x => context.write(new Text(x._1),new LongWritable(x._2)))
    val sortedmap_asc = scalamap.toSeq.sortWith(_._2 < _._2).take(100)
    context.write(new Text(""),new LongWritable())
    context.write(new Text("Top 100 authors with no co-authors"), new LongWritable())
    logger.info("Writing Top 100 authors with no co-author")
    context.write(new Text(""),new LongWritable())
    sortedmap_asc.foreach(x => context.write(new Text(x._1),new LongWritable(x._2)))}
    catch{
      case  _ : Throwable => println("Exception ignored")
    }
  }
}

