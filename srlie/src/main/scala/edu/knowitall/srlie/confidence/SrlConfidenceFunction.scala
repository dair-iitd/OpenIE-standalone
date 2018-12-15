package edu.knowitall.srlie.confidence

import org.slf4j.LoggerFactory
import edu.knowitall.tool.conf.impl.LogisticRegression
import edu.knowitall.tool.conf.FeatureSet
import java.net.URL
import edu.knowitall.srlie.SrlExtractionInstance

object SrlConfidenceFunction {
  val logger = LoggerFactory.getLogger(classOf[SrlConfidenceFunction])

  type SrlConfidenceFunction = LogisticRegression[SrlExtractionInstance]

  val defaultModelUrl = Option(this.getClass.getResource("default-classifier.txt")).getOrElse {
    throw new IllegalArgumentException("Could not load confidence function resource.")
  }

  def loadDefaultClassifier(): SrlConfidenceFunction = {
    fromUrl(SrlFeatureSet, defaultModelUrl)
  }

  def fromUrl(featureSet: FeatureSet[SrlExtractionInstance, Double], url: URL): SrlConfidenceFunction = {
    LogisticRegression.fromUrl(featureSet, url)
  }
}