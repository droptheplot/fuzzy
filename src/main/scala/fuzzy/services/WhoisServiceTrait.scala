package fuzzy.services

import cats.data.NonEmptyList
import fuzzy.entities._
import org.apache.commons.net.whois.{WhoisClient => Client}
import org.slf4j.Logger

import scala.util.Try

trait WhoisServiceTrait {
  def get(sld: SLD, tld: TLD, server: Server)(implicit client: ClientTrait = new ClientAdapter(new Client),
                                              logger: Logger): Try[SearchResponse]
  def parseDomain(str: String, serverMap: ServerMap): Option[Domain]
  def commonTLDs(tld: Option[TLD]): NonEmptyList[TLD]
  def status(raw: String): Status
}
