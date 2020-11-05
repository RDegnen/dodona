package dodona.lib.http

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.apache.commons.codec.binary.{Base64, Hex}

object HttpClientUtils {
  private val hmacSHA256 = "HmacSHA256"
  private val hmacSHA512 = "HmacSHA512"

  def binanceSignature(
      message: String,
      secret: String
  ): String = {
    val mac = Mac.getInstance(hmacSHA256)
    mac.init(new SecretKeySpec(secret.getBytes, hmacSHA256))
    new String(Hex.encodeHex(mac.doFinal(message.getBytes)))
  }

  def krakenSignature(
      path: String,
      nonce: Long,
      postData: String,
      apiSecret: String
  ): String = {
    val md = MessageDigest.getInstance("SHA-256")
    md.update((nonce + postData).getBytes())
    val mac = Mac.getInstance(hmacSHA512)
    mac.init(new SecretKeySpec(Base64.decodeBase64(apiSecret), hmacSHA512))
    mac.update(path.getBytes())
    new String(Base64.encodeBase64(mac.doFinal(md.digest())))
  }
}