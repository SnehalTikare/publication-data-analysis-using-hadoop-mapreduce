import org.apache.hadoop.io.{IntWritable, LongWritable, Text}
import org.apache.hadoop.mapreduce.Mapper
import org.slf4j.{Logger, LoggerFactory}
import scala.xml.XML

/**
 * This file contains mapper for all the tasks
 */

class DblpMapper {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  /**
   * Function to extract text from the XML document
   * @param xml - XML String
   * @param element - Tag whose text has to be extracted
   * @return - Required data within the tag
   */

  def getElementFromXML(xml:String, element:String) : List[String] ={

    logger.info("Extracting values from XML")
    val parent = XML.loadString(xml)
    //Extract the details of venue based on the publication type
    if( element == ConstantsHelper.VENUE){
      val venueElement = parent.child.head.label match {
        case ConstantsHelper.ARTICLE => ((parent \ ConstantsHelper.ARTICLE \ConstantsHelper.JOURNAL).map(el => el.text.trim).toList)
        case ConstantsHelper.INPROCEEDINGS => ((parent \ ConstantsHelper.INPROCEEDINGS \ ConstantsHelper.BOOKTITLE).map(el => el.text.trim).toList)
        case ConstantsHelper.PROCEEDINGS => ((parent \ ConstantsHelper.PROCEEDINGS \ ConstantsHelper.BOOKTITLE).map(el => el.text.trim).toList)
        case ConstantsHelper.INCOLLECTION => ((parent \ ConstantsHelper.INCOLLECTION \ConstantsHelper.BOOKTITLE).map(el => el.text.trim).toList)
        case ConstantsHelper.BOOK=> ((parent \ ConstantsHelper.BOOK \ ConstantsHelper.PUBLISHER).map(el => el.text.trim).toList)
        case ConstantsHelper.PHDTHESIS => ((parent \ ConstantsHelper.PHDTHESIS \ConstantsHelper.SCHOOL).map(el => el.text.trim).toList)
        case ConstantsHelper.MASTERSTHESIS => ((parent \ ConstantsHelper.MASTERSTHESIS \ConstantsHelper.SCHOOL).map(el => el.text.trim).toList)
        case ConstantsHelper.WWW =>((parent \\ "@key").text).split('/').toList
      }
      venueElement
    }
    //Return the list of authors/editors of the publication
    else if( element == ConstantsHelper.AUTHOR){
      if((parent \\ ConstantsHelper.AUTHOR).nonEmpty) {
        val author = (parent \\ ConstantsHelper.AUTHOR).map(el => el.text.trim).toList
        return author
      } else if((parent \\ ConstantsHelper.EDITOR).nonEmpty) {
        val editor = (parent \\ ConstantsHelper.EDITOR).map(el => el.text.trim).toList
        return  editor
      }
      else{
        logger.info("No Author or Editor found")
        logger.info(xml)
        val lis = List.empty[String]
        return lis
      }
    }
    //Return the data for other tags(Ex: Title, Year)
    else{
      val elementValue = (parent \\ element).map(el => el.text.trim).toList
      elementValue
    }
  }
}

/**
 * Mapper for Task 1 -  Top ten published authors at each venue
 * The mapper emits the venue and authors under them
 */
class AuthorsVenueMapper extends Mapper[LongWritable, Text, Text, Text] {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  logger.info("Task 1 mapper - AuthorsVenueMapper ")
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
    val dblpdtd = getClass.getResource("dblp.dtd").toURI.toString
    val xmlString =
      s"""<?xml version="1.0" encoding="ISO-8859-1"?>
              <!DOCTYPE dblp SYSTEM "$dblpdtd">
              <dblp>""" + value.toString + "\n" +
        "</dblp>"
    try{
      val venue = new DblpMapper().getElementFromXML(xmlString, ConstantsHelper.VENUE)(0)
      val authors = new DblpMapper().getElementFromXML(xmlString,ConstantsHelper.AUTHOR)
      authors.foreach(author => context.write(new Text(venue),new Text(author)))
    }
    catch {
      case _: Throwable => println("No Venue in the given publication")
        logger.info("No Venue in the publication")
        logger.info(xmlString)
    }
  }
}

/**
 * Mapper for Task 2 - The list of authors who published without interruption for N years where 10 <= N
 * This mapper emits the authors and year of publication for a single publication
 */
class AuthorsNYearsMapper extends Mapper[LongWritable, Text, Text, IntWritable] {

  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, IntWritable]#Context): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)
    logger.info("Task 2 mapper - AuthorsNYearsMapper ")
    val dblpdtd = getClass.getResource("dblp.dtd").toURI.toString
    val xmlString =
      s"""<?xml version="1.0" encoding="ISO-8859-1"?>
              <!DOCTYPE dblp SYSTEM "$dblpdtd">
              <dblp>""" + value.toString + "</dblp>"
    try{
      val authors = new DblpMapper().getElementFromXML(xmlString,ConstantsHelper.AUTHOR)
      val year = new DblpMapper().getElementFromXML(xmlString,ConstantsHelper.YEAR)(0)
      authors.foreach(author => context.write(new Text(author),new IntWritable(year.toInt)))
    }
    catch {
      case _: Throwable => println("No year for the publication")
        logger.info("No year in the publication")
        logger.info(xmlString)
    }
  }
}

/**
 * Mapper for Task 3 -  The list of publications that contains only one author for each venue
 * This mapper emits the venue and publication title if the publication contains only one author
 */
class PublicationOneAuthorMapper extends Mapper[LongWritable, Text, Text, Text]{
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)
    logger.info("Task 3 mapper - PublicationOneAuthorMapper ")
    val dblpdtd = getClass.getResource("dblp.dtd").toURI.toString
    val xmlString =
      s"""<?xml version="1.0" encoding="ISO-8859-1"?>
              <!DOCTYPE dblp SYSTEM "$dblpdtd">
              <dblp>""" + value.toString + "</dblp>"
    try{
      val venue = new DblpMapper().getElementFromXML(xmlString, ConstantsHelper.VENUE)(0)
      val authors = new DblpMapper().getElementFromXML(xmlString,ConstantsHelper.AUTHOR)
      if(authors.length == 1) {
        val title = new DblpMapper().getElementFromXML(xmlString,ConstantsHelper.TITLE)(0)
        context.write(new Text(venue),new Text(title))
      }
    }
    catch {
      case _: Throwable => println("No venue or year found")
        logger.info("No venue in the publication")
        logger.info(xmlString)
    }
  }
}

/**
 * Mapper for Task 4 - The list of publications for each venue that
 * contain the highest number of authors for each of these venues.
 * This mapper emits the venue and Title of publication along with number of authors in the publication
 */
class AuthorPublicationMapper extends Mapper[LongWritable, Text, Text, Text]{
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)
    logger.info("Task 4 mapper - AuthorPublicationMapper ")
    val dblpdtd = getClass.getResource("dblp.dtd").toURI.toString
    val xmlString =
      s"""<?xml version="1.0" encoding="ISO-8859-1"?>
              <!DOCTYPE dblp SYSTEM "$dblpdtd">
              <dblp>""" + value.toString + "</dblp>"
    try{
      val authors = new DblpMapper().getElementFromXML(xmlString,ConstantsHelper.AUTHOR)
      val venue = new DblpMapper().getElementFromXML(xmlString,ConstantsHelper.VENUE)(0)
      val title = new DblpMapper().getElementFromXML(xmlString,ConstantsHelper.TITLE)(0)
      if(authors.length>0)
        context.write(new Text(venue), new Text(title.replace("[^a-zA-Z0-9]","") + "::" + authors.length))
    }
    catch {
      case _: Throwable => println("No author, venue or title found")
        logger.info("No venue or title in the publication")
        logger.info(xmlString)
    }
  }
}

/**
 * Mapper for Task 5 - The list of top 100 authors in the descending order who publish with most co-authors
 * and the list of 100 authors who publish without any co-authors
 * This mapper emits author,co-author pair for each publication
 */
class MostCoAuthorMapper extends  Mapper[LongWritable, Text, Text, Text]{
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)
    logger.info("Task 5 mapper - MostCoAuthorMapper ")
    val dblpdtd = getClass.getResource("dblp.dtd").toURI.toString
    val xmlString =
      s"""<?xml version="1.0" encoding="ISO-8859-1"?>
              <!DOCTYPE dblp SYSTEM "$dblpdtd">
              <dblp>""" + value.toString + "</dblp>"
    try{
      val authors = new DblpMapper().getElementFromXML(xmlString,ConstantsHelper.AUTHOR)
      if(authors.length == 1)
        context.write(new Text(authors(0)),new Text("x") )
      authors.foreach( author =>
        authors.filter(_!=author).foreach(coauthor => context.write(new Text(author), new Text(coauthor))))}
    catch {
      case _: Throwable => println("No authors in the publication")
        logger.info("No authors in the publication")
        logger.info(xmlString)
    }
  }
}

