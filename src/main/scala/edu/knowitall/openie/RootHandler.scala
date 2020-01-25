package edu.knowitall.openie

import java.io.{InputStream, InputStreamReader, OutputStream}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import com.google.common.base.Charsets
import com.google.common.io.CharStreams

import com.sun.net.httpserver.{HttpExchange, HttpHandler}
import java.io.StringWriter

class RootHandler(openie: OpenIE) extends HttpHandler {
  
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def handle(t: HttpExchange) {
    val str = CharStreams.toString(new InputStreamReader(t.getRequestBody(), Charsets.UTF_8))
    displayPayload(str)
    sendResponse(t, str)
  }

  private def displayPayload(str: String): Unit ={
    println()
    println("******************** REQUEST START ********************")
    println()
    println(str)
    println()
    println("********************* REQUEST END *********************")
    println()
  }

  private def copyStream(in: InputStream, out: OutputStream) {
    Iterator
      .continually(in.read)
      .takeWhile(-1 !=)
      .foreach(out.write)
  }

  private def sendResponse(t: HttpExchange, str : String) {
    val insts = openie.extract(str)
    println(insts)
    println()
    
    val out = new StringWriter
    mapper.writeValue(out, insts)
    
    val response = out.toString()
    t.sendResponseHeaders(200, response.length())
    val os = t.getResponseBody
    os.write(response.getBytes)
    os.close()
  }

}
