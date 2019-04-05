package fuzzy.entities

final case class SearchResponse(sld: String, tld: String, status: Status, raw: String) {
  override def toString: String = sld + "." + tld

  override def hashCode(): Int = toString.hashCode
  override def equals(obj: Any): Boolean = hashCode == obj.hashCode
}
