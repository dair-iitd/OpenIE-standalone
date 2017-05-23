package edu.knowitall.openie.util

import java.net.URL
import scala.io.Source

class ReplaceChars()  {
  private val pairChars =  scala.collection.mutable.Set[Array[String]]()
  private val tab = """\t""".r
  private val url = Option(this.getClass.getResource("non-UTF.txt")).getOrElse {
    throw new IllegalArgumentException("Could not load non UTF pair resource.")
  }

  def load() = {
  try{
    val input = url.openStream;
    val source = Source.fromInputStream(input)
    source.getLines.foreach { line => 
    var arr = tab.split(line)
    arr(0) = arr(0).trim
    arr(1) = arr(1).trim
    pairChars.add(arr) }
 }
  catch{
	case e: Exception =>
              System.err.println("Error on loading file: non-UTF.txt")
    }
  }

  def replacenow(line: String): String = {
    var replaced = line
    pairChars.foreach(arr=>{replaced = replaced.replaceAll(arr(0),arr(1))})
    
    replaced
  }
}
