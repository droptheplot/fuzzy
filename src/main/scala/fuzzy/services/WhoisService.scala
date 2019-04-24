package fuzzy.services

import cats.data.NonEmptyList
import fuzzy.entities._
import org.apache.commons.net.whois.{WhoisClient => Client}
import org.slf4j.Logger
import spray.json._
import spray.json.DefaultJsonProtocol._

import scala.io.Source
import scala.util.Try

class WhoisService extends WhoisServiceTrait {

  /** Common domains we're always checking. */
  val commonTLDs: NonEmptyList[TLD] = NonEmptyList.of[TLD]("com", "net", "org", "co", "io", "app")

  /** Whois for taken domains would contain these lines. */
  val takenLines: List[String] = List[String](
    "NOT FOUND",
    "Not found",
    "No match for",
    "No Data Found",
    "No registrar found",
    "No entries found",
    "Domain not found"
  )

  def commonTLDs(tld: Option[TLD]): NonEmptyList[TLD] = tld match {
    case Some(_tld) => NonEmptyList.of[TLD](_tld, commonTLDs.filterNot(_ == _tld): _*)
    case None       => commonTLDs
  }

  def get(sld: SLD, tld: TLD, server: Server)(implicit client: ClientTrait = new ClientAdapter(new Client),
                                              logger: Logger): Try[SearchResponse] = Try {
    val result = new StringBuilder("")

    logger.info(s"WhoisUsecase.get sld=$sld tld=$tld")

    client.connect(server.hostname)
    result.append(client.query(sld + "." + tld))

    client.disconnect()

    val raw: String = result.toString

    SearchResponse(sld, tld, status(raw), raw)
  }

  def status(raw: String): Status = {
    raw.lines.anyMatch { line =>
      takenLines.exists(line.startsWith)
    } match {
      case true => Status.Available
      case _    => Status.Taken
    }
  }

  def parseDomain(str: String, serverMap: ServerMap): Option[Domain] =
    serverMap
      .find { case (tld, _) => str.endsWith("." + tld) } match {
      case Some((tld, _)) =>
        str
          .stripSuffix("." + tld)
          .split('.')
          .takeRight(1) match {
          case Array(name) if !name.isEmpty => Some(Domain(name, Some(tld)))
          case _                            => None
        }
      case None =>
        str.split('.').lastOption match {
          case Some(name) if !name.isEmpty => Some(Domain(name, None))
          case _                           => None
        }
    }
}

object WhoisService {

  val serverResource: String = "whois.json"

  def loadServers(): Try[ServerMap] = {
    implicit object serverFormat extends JsonFormat[Server] {
      override def write(obj: Server): JsValue = JsObject()

      override def read(json: JsValue): Server = json match {
        case JsObject(fields) if fields.isDefinedAt("hostname") =>
          Server(fields("hostname").convertTo[String])
        case _ => Server("")
      }
    }

    Try {
      Source
        .fromResource(serverResource)
        .mkString
        .parseJson
        .convertTo[ServerMap]
        .filterNot { case (_, server) => server.hostname.isBlank }
    }
  }

}
