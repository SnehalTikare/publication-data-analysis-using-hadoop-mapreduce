import scala.xml.XML

object ParseXML {
  def isConsecutive(seq: List[Int]): (Boolean, Int) = {
    val count = seq.sliding(2).count(a => a(0)+1 == a(1))
    (count > 0, count)
  }
  def parse():Unit ={
    //val mystring = " <article mdate=\"2019-10-25\" key=\"tr/gte/TR-0231-08-93-165\" publtype=\"informal\">\n        <author>Frank Manola</author>\n        <author>Sandra Heiler</author>\n        <title>A 'RISC' Object Model for Object System Interoperation: Concepts and Applications.</title>\n        <journal>GTE Laboratories Incorporated</journal>\n        <volume>TR-0231-08-93-165</volume>\n        <month>August</month>\n        <year>1993</year>\n        <url>db/journals/gtelab/index.html#TR-0231-08-93-165</url>\n    </article>"
    System.out.println(ParseXML.getClass.getResource("dblp_sample.xml").toURI.toString)
   // System.out.println(ParseXML.getClass.getClassLoader.getResource("/dblp_sample.xml"))

    val xml =  XML.load("src/main/resources/dblp_sample.xml")
    val authors = xml \\ "article"
    val key = (authors(0) \ "@key").text
    println(key.split('/')(1))
  }

 def main(args:Array[String]):Unit = {
  ParseXML.parse()
  println(isConsecutive(List(1994,1992, 1995, 2019).sorted))
 }

}
