import java.{lang, util}
import java.io.IOException

import com.typesafe.config.ConfigFactory
import org.apache.hadoop.io.{ArrayWritable, DoubleWritable, IntWritable, LongWritable, Text, Writable}
import org.apache.hadoop.mapreduce.Reducer

import scala.collection.JavaConverters._
import scala.collection.immutable.{ListMap, TreeMap}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer


class DblpReducer{
  def isConsecutive(seq: List[Int]): Int = {
    val count = seq.sliding(2).count(a => a(0)+ 1 == a(1))
    count
  }
}


class AuthorsVenueReducer extends Reducer[Text, Text, Text, Text] {
  val hashmap = new util.HashMap[String, Integer]()
  @throws[IOException]
  @throws[InterruptedException]
  override def reduce(key: Text, values: lang.Iterable[Text], context: Reducer[Text, Text, Text, Text]#Context): Unit = {
    values.forEach(x => hashmap.put(x.toString, hashmap.getOrDefault(x.toString, 0) + 1))
    val scalamap = hashmap.asScala
    val sortedmap = ListMap(scalamap.toSeq.sortWith(_._2 > _._2): _*)//Make top ten
    sortedmap.foreach(x => context.write(key, new Text(x._1 + " " + x._2)))
  }
}

class AuthorsNYearsReducer extends Reducer[Text,IntWritable,Text,IntWritable]{
  val config = ConfigFactory.load()
    @throws[IOException]
    @throws[InterruptedException]
    override def reduce(key: Text, values: lang.Iterable[IntWritable], context: Reducer[Text, IntWritable, Text, IntWritable]#Context): Unit ={
      val years = new ListBuffer[Int]
      for(value <- values.asScala){
        years.addOne(value.get())
      }
      if(years.length > 1){
        val count = new DblpReducer().isConsecutive(years.toList.sorted)
        if(count >= config.getInt("consecutive_years")) context.write(key,new IntWritable(count))
      }
    }
}
class PublicationOneAuthorReducer extends Reducer[Text, Text, Text, TextArrayWritable]{
  @throws[IOException]
  @throws[InterruptedException]
  override def reduce(key: Text, values: lang.Iterable[Text], context: Reducer[Text, Text, Text, TextArrayWritable]#Context): Unit = {
    val uniquevals = values.asScala
    val elements = uniquevals.map(x => new Text(x)).toArray
    val arrayWritable = new TextArrayWritable(elements)
    context.write(key,arrayWritable)
  }
}
class MostCoAuthorReducer extends Reducer[Text, Text,Text, LongWritable]{
  val hashmap = new util.HashMap[String, Long]()
  @throws[IOException]
  @throws[InterruptedException]
  /*override def setup(context: Reducer[Text, Text, Text,LongWritable]#Context): Unit = {
    var hashmap = new util.HashMap[String, Long]()
  }*/
  override def reduce(key: Text, values: lang.Iterable[Text], context: Reducer[Text, Text, Text, LongWritable]#Context): Unit = {

    val coAuthorList = new ListBuffer[String]
    for(value <- values.asScala){
      if(!coAuthorList.contains(value.toString)) {
        coAuthorList.addOne(value.toString)
      }
    }
    println(key + " " + coAuthorList.toString())
    //val len = coAuthorList.toList.length
    hashmap.put(key.toString,coAuthorList.length )
  }
  @throws[IOException]
  @throws[InterruptedException]
  override def cleanup(context: Reducer[Text, Text,Text, LongWritable]#Context): Unit = {
    val scalamap = hashmap.asScala
    val sortedmap = ListMap(scalamap.toSeq.sortWith(_._2 > _._2): _*)
    sortedmap.foreach(x => context.write(new Text(x._1),new LongWritable(x._2)))
  }
}


class NoCoAuthorReducer extends Reducer[Text, Text,Text, LongWritable]{
  val hashmap = new util.HashMap[String, Long]()
  @throws[IOException]
  @throws[InterruptedException]
  override def reduce(key: Text, values: lang.Iterable[Text], context: Reducer[Text, Text, Text, LongWritable]#Context): Unit = {
    val coAuthorList = new ListBuffer[String]
    for(value <- values.asScala){
      if(!coAuthorList.contains(value.toString)) {
        coAuthorList.addOne(value.toString)
      }
    }
    println(key + " " + coAuthorList.toString())
    //val len = coAuthorList.toList.length
    hashmap.put(key.toString,coAuthorList.length )
  }
  @throws[IOException]
  @throws[InterruptedException]
  override def cleanup(context: Reducer[Text, Text,Text, LongWritable]#Context): Unit = {
    val scalamap = hashmap.asScala
    val sortedmap = ListMap(scalamap.toSeq.sortWith(_._2 < _._2): _*)
    sortedmap.foreach(x => context.write(new Text(x._1),new LongWritable(x._2)))
  }
}
class AuthorPublicationReducer extends Reducer[Text, Text,Text, TextArrayWritable]{
  var treemap= new TreeMap[Integer, List[Text]]()(implicitly[Ordering[Integer]].reverse)

  @throws[IOException]
  @throws[InterruptedException]
  override def reduce(key: Text, values: lang.Iterable[Text], context: Reducer[Text, Text, Text, TextArrayWritable]#Context): Unit = {
    val publicationCount = new ListBuffer[String]
    var max_count = 0
    val authorcount = new ListBuffer[Integer]
    val listResult = new ListBuffer[Text]
    for(value <- values.asScala){
      val split_value = value.toString.split("::")
      publicationCount.addOne(value.toString)
      authorcount.addOne(split_value(1).toInt)
      if(split_value(1).toInt > max_count)
        max_count = split_value(1).toInt
    }
    for(pub <- publicationCount){
      var count  = pub.split("::")(1)
      if(count.toInt == max_count)
        {
          listResult.addOne(new Text("\"" +pub.split("::" )(0)+ "\"" ))
        }
    }
    val arrayWritable = new TextArrayWritable(listResult.toArray)
    context.write(key,arrayWritable)
  }
}
