import com.typesafe.config.ConfigFactory
import org.apache.hadoop.io.{IntWritable, Text}
import org.scalatest.FunSuite

import scala.collection.mutable.ListBuffer

class TestClass extends FunSuite {
  val config = ConfigFactory.load()
  test("Loading of configurations"){

    val year = config.getString("year")
    assert(year == "year")
  }

  test("Output from XML Parser - Venue"){
    val dblpdtd = getClass.getClassLoader.getResource("dblp.dtd").toURI
    val xmlString = "<article><journal>meltdown.com</journal></article>"
    val inputString =
      s"""<?xml version="1.0" encoding="ISO-8859-1"?>
              <!DOCTYPE dblp SYSTEM "$dblpdtd">
              <dblp>""" + xmlString + "</dblp>"
    val venue = new DblpMapper().getElementFromXML(inputString,"venue")(0)
    assert(venue.equals("meltdown.com"))
  }

  test("Output from XML Parser - author"){
    val dblpdtd = getClass.getClassLoader.getResource("dblp.dtd").toURI
    val xmlString = "<article><author>Mike Hamburg</author><author>Moritz Lipp</author><author>Stefan Mangard</author></article>"
    val inputString =
      s"""<?xml version="1.0" encoding="ISO-8859-1"?>
              <!DOCTYPE dblp SYSTEM "$dblpdtd">
              <dblp>""" + xmlString + "</dblp>"
    val authors = new DblpMapper().getElementFromXML(inputString,"author")
    assert(authors.length==3)
    assert(authors(0).equals("Mike Hamburg"))
  }

  test("Testing PublicationOneAuthorMapper"){
    val dblpdtd = getClass.getClassLoader.getResource("dblp.dtd").toURI
    val xmlString = "<article><journal>meltdown.com</journal><author>Mike Hamburg</author><title>Spectre Attacks: Exploiting Speculative Execution.</title></article>"
    val inputString =
      s"""<?xml version="1.0" encoding="ISO-8859-1"?>
              <!DOCTYPE dblp SYSTEM "$dblpdtd">
              <dblp>""" + xmlString + "</dblp>"
    val venue = new DblpMapper().getElementFromXML(inputString, config.getString("venue"))(0)
    val authors = new DblpMapper().getElementFromXML(inputString,config.getString("author"))
    if(authors.length == 1) {
      val title = new DblpMapper().getElementFromXML(inputString,config.getString("title"))(0)
      val context = (new Text(venue),new Text(title))
      assert(context._1.equals(new Text("meltdown.com")))
      assert(context._2.equals(new Text("Spectre Attacks: Exploiting Speculative Execution.")))
    }
  }

  test("Testing AuthorsNYearsMapper"){
    val dblpdtd = getClass.getClassLoader.getResource("dblp.dtd").toURI
    val xmlString = "<article><journal>meltdown.com</journal><author>Mike Hamburg</author><title>Spectre Attacks: Exploiting Speculative Execution.</title><year>2020</year></article>"
    val inputString =
      s"""<?xml version="1.0" encoding="ISO-8859-1"?>
              <!DOCTYPE dblp SYSTEM "$dblpdtd">
              <dblp>""" + xmlString + "</dblp>"
    val authors = new DblpMapper().getElementFromXML(inputString,config.getString("author"))
    val year = new DblpMapper().getElementFromXML(inputString,config.getString("year"))(0)
    val context = (new Text(authors(0)),new IntWritable(year.toInt))
    assert(context._1.equals(new Text("Mike Hamburg")))
    assert(context._2.equals(new IntWritable(2020)))
  }

  test("Function isConsecutive"){
    val years =List(1993,1994,1995,1996)
    val count = new DblpReducer().isConsecutive(years)
    assert(count == years.length)
    val years2 = List(1998,1999,2000,2015,2016)
    val count2 = new DblpReducer().isConsecutive(years2)
    assert(count2 == 3)
    val years3 = List(2000,2015)
    val count3 = new DblpReducer().isConsecutive(years3)
    assert(count3 == 1)
  }
  test("PublicationOneAuthorReducer"){
    val key = new Text("meltdown.com")
    val values = List(new Text("Spectre Attacks: Exploiting Speculative Execution.::4"),new Text("An Evaluation of Object-Oriented DBMS Developments: 1994 Edition::4"),new Text("DARWIN: On the Incremental Migration of Legacy Information Systems::2"))
    val publicationCount = new ListBuffer[String]
    var max_count = 0
    val authorcount = new ListBuffer[Integer]
    val listResult = new ListBuffer[Text]
    for(value <- values){
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
    assert(listResult.length==2)
  }

}
