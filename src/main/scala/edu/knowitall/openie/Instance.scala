package edu.knowitall.openie

/***
 * A concrete instance of an extractions.
 */
case class Instance(confidence: Double, sentence: String, extraction: Extraction) {
  def conf = confidence
  def extr = extraction

  override def toString = f"$conf%1.2f $extraction"
}