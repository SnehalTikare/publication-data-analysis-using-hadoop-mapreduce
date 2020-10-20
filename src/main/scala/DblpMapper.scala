import com.typesafe.config.{Config, ConfigFactory}
import org.apache.hadoop.io.{IntWritable, LongWritable, Text}
import org.apache.hadoop.mapreduce.Mapper
import org.slf4j.{Logger, LoggerFactory}

import scala.xml.XML
class DblpMapper {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  def getElementFromXML(xml:String, element:String) : List[String] ={
    //logger.info("extracting values from XML")
    //logger.info(xml)
    val parent = XML.loadString(xml)
    if( element == "venue"){
      val venueElement = parent.child.head.label match {
        case "article" => ((parent \ "article" \"journal").map(el => el.text.trim).toList)
        case "inproceedings" => ((parent \ "inproceedings" \"booktitle").map(el => el.text.trim).toList)
        case "proceedings" => ((parent \ "inproceedings" \"booktitle").map(el => el.text.trim).toList)
        case "incollection" => ((parent \ "inproceedings" \"booktitle").map(el => el.text.trim).toList)
        case "book" => ((parent \ "book" \"publisher").map(el => el.text.trim).toList)
        case "phdthesis" => ((parent \ "phdthesis" \"school").map(el => el.text.trim).toList)
        case "mastersthesis" => ((parent \ "mastersthesis" \"school").map(el => el.text.trim).toList)
        case "www" =>((parent \\ "@key").text).split('/').toList
      }
      venueElement
    }
    else if( element == "author"){
      println((parent \\ "author"))
      println((parent \\ "author").nonEmpty)
      if((parent \\ "author").nonEmpty) {
        val author = (parent \\ "author").map(el => el.text.trim).toList
        return author
      } else if((parent \\ "editor").nonEmpty) {
        val editor = (parent \\ "editor").map(el => el.text.trim).toList
        return  editor
      }
      else{
        logger.info("No Author or Editor found")
        logger.info(xml)
        val lis = List.empty[String]
        return lis
      }
    }
    else{
      val elementValue = (parent \\ element).map(el => el.text.trim).toList
      elementValue
    }
    //val key = (parent \ element \ "@key")
    //val elementValue = ((parent \\ element \\ "@key").text).split('/').toList
    /* val elementValue = element match {
       case "venue" => ((parent \\ "@key").text).split('/').toList
       case _ =>(parent \\ element).map(el => el.text.trim).toList}
   //val element =  ((parent \\ element \\ "@key").text).split('/')(1)
   elementValue*/
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
      val venue = new DblpMapper().getElementFromXML(xmlString, config.getString("venue"))(0)
      val authors = new DblpMapper().getElementFromXML(xmlString,config.getString("author"))
      authors.foreach(author => context.write(new Text(venue),new Text(author)))
      //for (author <- authors) context.write(new Text(venue),new Text(author))
    }
    catch {
      case _: Throwable => println("No author or venue in the given publication")
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
    try{
      val authors = new DblpMapper().getElementFromXML(xmlString,config.getString("author"))
      val year = new DblpMapper().getElementFromXML(xmlString,config.getString("year"))(0)
      authors.foreach(author => context.write(new Text(author),new IntWritable(year.toInt)))
      //for (author <- authors) context.write(new Text(author),new IntWritable(year.toInt))
    }
    catch {
      case _: Throwable => println("No author or year for the publication")
    }
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
    try{
      val venue = new DblpMapper().getElementFromXML(xmlString, config.getString("venue"))(0)
      val authors = new DblpMapper().getElementFromXML(xmlString,config.getString("author"))
      if(authors.length == 1) {
        val title = new DblpMapper().getElementFromXML(xmlString,config.getString("title"))(0)
        context.write(new Text(venue),new Text(title))
      }
    }
    catch {
      case _: Throwable => println("No author, venue or year found")
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
    try{
      val authors = new DblpMapper().getElementFromXML(xmlString,config.getString("author"))
      if(authors.length == 1)
        context.write(new Text(authors(0)),new Text("x") )
      authors.foreach( author =>
        authors.filter(_!=author).foreach(
          coauthor =>
            context.write(new Text(author), new Text(coauthor))
        )
      )
    }
    catch {
      case _: Throwable => println("No authors in the publication")
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
    try{
      val authors = new DblpMapper().getElementFromXML(xmlString,config.getString("author"))
      val venue = new DblpMapper().getElementFromXML(xmlString,config.getString("venue"))(0)
      val title = new DblpMapper().getElementFromXML(xmlString,config.getString("title"))(0)
      context.write(new Text(venue), new Text(title + "::" + authors.length))
    }
    catch {
      case _: Throwable => println("No author, venue or title found")
    }
  }
}