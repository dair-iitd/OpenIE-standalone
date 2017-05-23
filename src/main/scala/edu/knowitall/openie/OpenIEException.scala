package edu.knowitall.openie

case class OpenIEException(msg: String, e: Exception) extends Exception(msg, e)
