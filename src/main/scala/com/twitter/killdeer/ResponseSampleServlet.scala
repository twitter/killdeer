package com.twitter.killdeer

import org.eclipse.jetty.continuation.ContinuationSupport
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

class ResponseSampleServlet(responseSampleFilename: String) extends HttpServlet {
  def txnid(req: HttpServletRequest) = req.getHeader("X-Transaction-Id") match {
    case null => "-"
    case s => s
  }
  val sample = new ResponseSampleLoader(responseSampleFilename)

  override def doGet(req: HttpServletRequest, res: HttpServletResponse) {
    val continuation = ContinuationSupport.getContinuation(req)

    if (continuation.isInitial) {
      val response: Response = sample.next()
      req.setAttribute("response", response)
      continuation.setTimeout(response.latencyMs)
      continuation.suspend()
    } else if (continuation.isExpired) {
      val response = req.getAttribute("response").asInstanceOf[Response]
      //res.setContentLength(response.size)
      res.setStatus(200)
      //res.getWriter().write("." * response.size)
    }
  }
}
