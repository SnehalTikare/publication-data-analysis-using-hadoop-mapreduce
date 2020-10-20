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
    val ranges = mutable.ArrayBuffer[Int](1)
    val lis = seq.distinct.sliding(2).foreach{
      case y1 :: tail =>
        if(tail.nonEmpty){
          val y2 = tail.head
          if(y2 - y1 == 1) ranges(ranges.size - 1) += 1
          else ranges += 1
        }
    }
    //val count = seq.sliding(2).count(a => a(0)+ 1 == a(1))
    //count
    ranges.max
  }
}


class AuthorsVenueReducer extends Reducer[Text, Text, Text, Text] {
  @throws[IOException]
  @throws[InterruptedException]
  override def reduce(key: Text, values: lang.Iterable[Text], context: Reducer[Text, Text, Text, Text]#Context): Unit = {
    val hashmap = new util.HashMap[String, Integer]()
    values.forEach(x => hashmap.put(x.toString, hashmap.getOrDefault(x.toString, 0) + 1))
    val scalamap = hashmap.asScala
    val sortedmap = scalamap.toSeq.sortWith(_._2 > _._2).take(10)//Make top ten
    sortedmap.foreach(x => context.write(new Text(key.toString.replace(",","")), new Text(x._1 + " " + x._2)))
  }
}

class AuthorsNYearsReducer extends Reducer[Text,IntWritable,Text,IntWritable]{
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
class PublicationOneAuthorReducer extends Reducer[Text, Text, Text, TextArrayWritable]{
  @throws[IOException]
  @throws[InterruptedException]
  override def reduce(key: Text, values: lang.Iterable[Text], context: Reducer[Text, Text, Text, TextArrayWritable]#Context): Unit = {
    val uniquevals = values.asScala
    val elements = uniquevals.map(x => new Text(x.toString.replace(",",""))).toArray
    val arrayWritable = new TextArrayWritable(elements)
    context.write(new Text(key.toString.replace(",","")),arrayWritable)
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

    val coAuthorList = values.asScala.toStream.distinct
    val filterlist = coAuthorList.filter(_!=new Text("x"))
    //    for(value <- values.asScala){
    //      if(!coAuthorList.contains(value.toString)) {
    //        coAuthorList.addOne(value.toString)
    //      }
    //    }
    //println(key + " " + coAuthorList.toString())
    //val len = coAuthorList.toList.length
    hashmap.put(key.toString,filterlist.length )
  }
  @throws[IOException]
  @throws[InterruptedException]
  override def cleanup(context: Reducer[Text, Text,Text, LongWritable]#Context): Unit = {
    val scalamap = hashmap.asScala
    val sortedmap = scalamap.toSeq.sortWith(_._2 > _._2)
    sortedmap.foreach(x => context.write(new Text(x._1),new LongWritable(x._2)))
  }
}


class NoCoAuthorReducer extends Reducer[Text, Text,Text, LongWritable]{
  val hashmap = new util.HashMap[String, Long]()
  @throws[IOException]
  @throws[InterruptedException]
  override def reduce(key: Text, values: lang.Iterable[Text], context: Reducer[Text, Text, Text, LongWritable]#Context): Unit = {
    //val coAuthorList = new ListBuffer[String]
    val coAuthorList = values.asScala.toStream.distinct
    println(coAuthorList)
    val filterlist = coAuthorList.filter(_!=new Text("x"))

    //    for(value <- values.asScala){
    //      if(!coAuthorList.contains(value.toString)) {
    //        coAuthorList.addOne(value.toString)
    //      }
    //    }
    //  println(key + " " + coAuthorList.toString())
    //val len = coAuthorList.toList.length
    hashmap.put(key.toString,filterlist.length )
  }
  @throws[IOException]
  @throws[InterruptedException]
  override def cleanup(context: Reducer[Text, Text,Text, LongWritable]#Context): Unit = {
    val scalamap = hashmap.asScala
    val sortedmap = scalamap.toSeq.sortWith(_._2 < _._2)
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
    values.asScala.foreach(value => {
      val split_value = value.toString.split("::")
      publicationCount.addOne(value.toString)
      authorcount.addOne(split_value(1).toInt)
      if(split_value(1).toInt > max_count)
        max_count = split_value(1).toInt
    })
    publicationCount.foreach(pub => {
      var count  = pub.split("::")(1)
      if(count.toInt == max_count)
      {
        listResult.addOne(new Text("\"" +pub.split("::" )(0).replace(",","")+ "\"" ))
      }
    })
    val arrayWritable = new TextArrayWritable(listResult.toArray)
    context.write(new Text(key.toString.replace(",","")),arrayWritable)
  }
}
