package uk.gov.hmrc.play.http

import uk.gov.hmrc.play.audit.http.{HeaderCarrier, HttpAuditing}
import uk.gov.hmrc.play.http.logging.{MdcLoggingExecutionContext, ConnectionTracing}

import scala.concurrent.Future
import play.api.libs.json
import play.api.libs.json.{Json, JsValue}
import MdcLoggingExecutionContext._
import play.api.http.HttpVerbs.{GET => GET_VERB}

trait HttpGet extends HttpVerb with ConnectionTracing with HttpAuditing {
  protected def doGet(url: String)(implicit hc: HeaderCarrier): Future[HttpResponse]

  def GET[A](url: String)(implicit rds: HttpReads[A], hc: HeaderCarrier): Future[A] =withTracing(GET_VERB, url) {
    val httpResponse = doGet(url)
    auditRequestWithResponseF(url, GET_VERB, None, httpResponse)
    mapErrors(GET_VERB, url, httpResponse).map(response => rds.read(GET_VERB, url, response))
  }

  @deprecated("use GET[HttpResponse] instead, or implement an HttpReads for your type", "23/2/2015")
  def GET_RawResponse(url:String, fn: HttpResponse => HttpResponse = identity, auditResponseBody: Boolean = true)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    GET[HttpResponse](url).map(fn)

  /**
   * The method wraps the response in Option.
   * For HttpResponse with status 404 or 202, instead of throwing an Exception, None will be returned.
   */
  @deprecated("use GET[Option[A]] instead", "23/2/2015")
  def GET_Optional[A](url: String)(implicit rds: json.Reads[A], mfst: Manifest[A], hc: HeaderCarrier): Future[Option[A]] =
    GET[Option[A]](url)

  /**
   * The method extracts the requested collection (array) from JSON response.
   * arrayFieldName indicates the name of the array field available in the JSON response.
   * For HttpResponse with status 404 or 202, instead of throwing an Exception, empty Seq is returned.
   */
  @deprecated("use GET[Seq[A]] and put a implicit (HttpReads.readJsonFromProperty(arrayFieldName) in scope instead", "23/2/2015")
  def GET_Collection[A](url: String, arrayFieldName: String)(implicit rds: json.Reads[A], mfst: Manifest[A], hc: HeaderCarrier) : Future[Seq[A]] =
    GET[Seq[A]](url)(HttpReads.readSeqFromJsonProperty(arrayFieldName), hc)

  @deprecated("moved to HttpReads", "23/2/2015")
  def readJson[A](url: String, jsValue: JsValue)(implicit rds: json.Reads[A], mf: Manifest[A], hc: HeaderCarrier) =
    HttpReads.readJson(GET_VERB, url, jsValue)
}