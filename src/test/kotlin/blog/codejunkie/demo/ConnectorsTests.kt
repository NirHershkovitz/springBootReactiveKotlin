package blog.codejunkie.demo

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ru.lanwen.wiremock.ext.WiremockResolver
import ru.lanwen.wiremock.ext.WiremockResolver.Wiremock
import ru.lanwen.wiremock.ext.WiremockUriResolver
import ru.lanwen.wiremock.ext.WiremockUriResolver.WiremockUri

@ExtendWith(WiremockResolver::class, WiremockUriResolver::class)
class ConnectorsTests {
    @Test
    fun `ip-api returns correct hosting details`(@Wiremock server : WireMockServer,
                                                @WiremockUri baseUrl : String) {
        server.stubFor(get(urlEqualTo("/json/codejunkie.blog")).
                willReturn(aResponse().
                        withStatus(200).
                        withHeader("Content-Type", "application/json").
                        withBody(
                            """
                                    {
                                       "country":"Netherlands",
                                       "isp":"GoDaddy.com, LLC",
                                       "status":"success"
                                    }
                                """
                        )))

        IpApiConnector(baseUrl).invoke("codejunkie.blog").block() shouldBe
                IpApiResponse(isp = "GoDaddy.com, LLC", country = "Netherlands", status = "success")
    }

    @Test()
    fun `status and message are populated when isp is not found`(@Wiremock server : WireMockServer,
                                                                 @WiremockUri baseUrl : String) {
        server.stubFor(get(urlEqualTo("/json/ontexistingXyz8X231AA.com")).
                willReturn(aResponse().
                        withStatus(200).
                        withHeader("Content-Type", "application/json").
                        withBody(
                                """
                                    {
                                       "status":"fail",
                                       "message":"invalid query"
                                    }
                                """
                        )))

        IpApiConnector(baseUrl).invoke("ontexistingXyz8X231AA.com").block() shouldBe
                IpApiResponse(status = "fail", message = "invalid query")
    }
}


