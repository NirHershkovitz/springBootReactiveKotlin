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
class ConnectorTests {
    @Test
    fun `ip-api return correct hosting details`(@Wiremock server : WireMockServer,
                                                @WiremockUri baseUrl : String) {
        server.stubFor(get(urlEqualTo("/json/codejunkie.blog")).
                willReturn(aResponse().
                        withStatus(200).
                        withHeader("Content-Type", "application/json").
                        withBody(
                            """
                                    {
                                       "country":"United States",
                                       "isp":"GoDaddy.com, LLC"
                                    }
                                """
                        )))

        IpApiConnector(baseUrl).invoke("codejunkie.blog").block() shouldBe
                HostingDetails(isp = "GoDaddy.com, LLC", country = "United States")
    }
}


