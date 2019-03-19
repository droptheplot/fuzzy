package com.entities

final case class Domain(sld: String, tld: Option[String]) {

  /** Returns "sld.tld" if tld exists, "sld" otherwise. */
  override def toString: String = tld match {
    case Some(_tld) => sld + "." + _tld
    case None       => sld
  }

  /** Returns sld with given tld */
  def toString(tld: String): String = sld + "." + tld
}
