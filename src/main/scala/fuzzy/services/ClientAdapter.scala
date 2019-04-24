package fuzzy.services

import org.apache.commons.net.whois.{WhoisClient => Client}

class ClientAdapter(c: Client) extends ClientTrait {
  override def connect(hostname: String): Unit = c.connect(hostname)
  override def disconnect(): Unit = c.disconnect()
  override def query(handle: String): String = c.query(handle)
}
