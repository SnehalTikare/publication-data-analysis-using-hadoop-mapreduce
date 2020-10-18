import java.io.IOException

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.hadoop.io.{IntWritable, LongWritable, Text}
import org.apache.hadoop.mapreduce.Mapper
import org.slf4j.{Logger, LoggerFactory}
import org.apache.hadoop.io.ArrayWritable

import scala.io.Source
import scala.xml.XML
class DblpMapper {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  def getElementFromXML(xml:String, element:String) : List[String] ={
    //logger.info("extracting values from XML")
    //logger.info(xml)
    val parent = XML.loadString(xml)
    //val key = (parent \ element \ "@key")
    //val elementValue = ((parent \\ element \\ "@key").text).split('/').toList
      val elementValue = element match {
        case "venue" => ((parent \\ "@key").text).split('/').toList
        case _ =>(parent \\ element).map(el => el.text.trim).toList}
    //val element =  ((parent \\ element \\ "@key").text).split('/')(1)
    elementValue
  }
}

class AuthorsVenueMapper extends Mapper[LongWritable, Text, Text, Text] {
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
    val config: Config = ConfigFactory.load()
    val dblpdtd = getClass.getResource("dblp.dtd").toURI.toString
    val xmlString =
      s"""<?xml version="1.0" encoding="ISO-8859-1"?>
              <!DOCTYPE dblp SYSTEM "$dblpdtd">
              <dblp>""" + value.toString + "\n" +
        "</dblp>"
    try{
      val venue = new DblpMapper().getElementFromXML(xmlString, config.getString("venue"))(1)
      val authors = new DblpMapper().getElementFromXML(value.toString,config.getString("author"))
      for (author <- authors) context.write(new Text(venue),new Text(author))
    }
    catch {
      case _: Throwable => println("Exception ignored")
    }


  }
}
class AuthorsNYearsMapper extends Mapper[LongWritable, Text, Text, IntWritable] {
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, IntWritable]#Context): Unit = {
    val config: Config = ConfigFactory.load()
    // Source.fromURL(getClass.getResource("HW2/src/main/resource/dblp.dtd"))
   // val dtd = scala.io.Source.fromFile(s"/Users/snehaltikare/Documents/3rd Sem/CS441 Distributed Cloud Computing/midterm/snehal_tikare_midterm/HW2/src/main/resources/dblp.dtd").getLines().mkString
    val dblpdtd = getClass.getResource("dblp.dtd").toURI.toString
    val xmlString =
    s"""<?xml version="1.0" encoding="ISO-8859-1"?>
              <!DOCTYPE dblp SYSTEM "$dblpdtd">
              <dblp>""" + value.toString + "</dblp>"
    val authors = new DblpMapper().getElementFromXML(xmlString,config.getString("author"))
    val year = new DblpMapper().getElementFromXML(xmlString,config.getString("year"))(0)
    for (author <- authors) context.write(new Text(author),new IntWritable(year.toInt))
  }
}

class PublicationOneAuthorMapper extends Mapper[LongWritable, Text, Text, Text]{
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
    val config: Config = ConfigFactory.load()
    // Source.fromURL(getClass.getResource("HW2/src/main/resource/dblp.dtd"))
    //val dtd = scala.io.Source.fromFile(s"/Users/snehaltikare/Documents/3rd Sem/CS441 Distributed Cloud Computing/midterm/snehal_tikare_midterm/HW2/src/main/resources/dblp.dtd").getLines().mkString
    val dblpdtd = getClass.getResource("dblp.dtd").toURI.toString
    val xmlString =
    s"""<?xml version="1.0" encoding="ISO-8859-1"?>
              <!DOCTYPE dblp SYSTEM "$dblpdtd">
              <dblp>""" + value.toString + "</dblp>"
    val venue = new DblpMapper().getElementFromXML(xmlString, config.getString("venue"))(1)
    val authors = new DblpMapper().getElementFromXML(xmlString,config.getString("author"))
    if(authors.length == 1) {
      val title = new DblpMapper().getElementFromXML(xmlString,config.getString("title"))(0)
      context.write(new Text(venue),new Text(title))
    }
  }
}

class MostCoAuthorMapper extends  Mapper[LongWritable, Text, Text, Text]{
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
    val config: Config = ConfigFactory.load()
    //val dtd = scala.io.Source.fromFile(s"/Users/snehaltikare/Documents/3rd Sem/CS441 Distributed Cloud Computing/midterm/snehal_tikare_midterm/HW2/src/main/resources/dblp.dtd").getLines().mkString
    val dblpdtd = getClass.getResource("dblp.dtd").toURI.toString
    val xmlString =
    s"""<?xml version="1.0" encoding="ISO-8859-1"?>
              <!DOCTYPE dblp SYSTEM "$dblpdtd">
              <dblp>""" + value.toString + "</dblp>"
    val authors = new DblpMapper().getElementFromXML(xmlString,config.getString("author"))
    if(authors.length == 1)
      context.write(new Text(authors(0)),new Text(" ") )
    for(author <- authors){
      for(coauthor <- authors){
        if(author != coauthor)
          context.write(new Text(author), new Text(coauthor))
      }
    }
  }
}

class AuthorPublicationMapper extends Mapper[LongWritable, Text, Text, Text]{
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
    val config: Config = ConfigFactory.load()
    //val dtd = scala.io.Source.fromFile(s"/Users/snehaltikare/Documents/3rd Sem/CS441 Distributed Cloud Computing/midterm/snehal_tikare_midterm/HW2/src/main/resources/dblp.dtd").getLines().mkString
    val dblpdtd = getClass.getResource("dblp.dtd").toURI.toString
    val xmlString =
      s"""<?xml version="1.0" encoding="ISO-8859-1"?>
              <!DOCTYPE dblp SYSTEM "$dblpdtd">
              <dblp>""" + value.toString + "</dblp>"
    val authors = new DblpMapper().getElementFromXML(xmlString,config.getString("author"))
    val venue = new DblpMapper().getElementFromXML(xmlString,config.getString("venue"))(1)
    val title = new DblpMapper().getElementFromXML(xmlString,config.getString("title"))(0)
    context.write(new Text(venue), new Text(title + "::" + authors.length))
  }
}