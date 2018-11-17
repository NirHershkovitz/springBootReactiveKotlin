package blog.codejunkie.demo

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import io.kotlintest.matchers.concurrent.shouldCompleteWithin
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Validatable
import io.restassured.response.ValidatableResponseOptions
import io.restassured.specification.RequestSender
import io.restassured.specification.RequestSpecification
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.lanwen.wiremock.config.WiremockConfigFactory
import ru.lanwen.wiremock.ext.WiremockResolver
import ru.lanwen.wiremock.ext.WiremockResolver.Wiremock
import ru.lanwen.wiremock.ext.WiremockUriResolver
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

//Had to do some tweaks and to define a context only with the application and a configuration defined inside the test,
//to make this test work due to Spring test failing to load ReactiveWebApplicationContext when using a Configuration
//inside the test. Also had to use a static WireMock port.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                classes = [DemoApplication::class,ParallelIntegrationTests.TestConfiguration::class])
@ExtendWith(SpringExtension::class, WiremockResolver::class, WiremockUriResolver::class)
class ParallelIntegrationTests {
    @LocalServerPort
    var port: Int = 0

    val semaphore1 = Semaphore(0)
    val semaphore2 = Semaphore(0)

    @Configuration
    class TestConfiguration {
        @Bean
        fun ipApiConnector() = IpApiConnector("http://localhost:9210")
    }

    @BeforeEach
    fun setup() {
        RestAssured.port = port
    }

    @Test()
    fun `two domains are fetched in parallel`(@Wiremock(factory = WireMockConfig::class) server: WireMockServer) {
        shouldCompleteWithin(5, TimeUnit.SECONDS, {
            server.addMockServiceRequestListener { request, response ->
                if (request.url.toString().contains("codejunkie")) {
                    semaphore2.release()
                    semaphore1.acquire()
                } else {
                    semaphore1.release()
                    semaphore2.acquire()
                }
                response
            }

            server.stubFor(WireMock.get(WireMock.urlEqualTo("/json/codejunkie.blog")).
                    willReturn(WireMock.aResponse().
                    withStatus(200).
                    withHeader("Content-Type", "application/json").
                    withBody("""
                                    {
                                       "country":"Netherlands",
                                       "isp":"GoDaddy.com, LLC",
                                       "status":"success"
                                    }
                                    """
                    )))
            server.stubFor(WireMock.get(WireMock.urlEqualTo("/json/google.com")).
                    willReturn(WireMock.aResponse().
                    withStatus(200).
                    withHeader("Content-Type", "application/json").
                    withBody("""
                                    {
                                       "country":"United States",
                                       "isp":"Google",
                                       "status":"success"
                                    }
                                    """
            )))

            given {
                jsonBody(mapOf("domains" to arrayOf("codejunkie.blog", "google.com")))
                on {
                    post("/isp") itHas {
                        statusCode(200)
                        body("'codejunkie.blog'.country", CoreMatchers.equalTo("Netherlands"))
                        body("'codejunkie.blog'.isp", CoreMatchers.equalTo("GoDaddy.com, LLC"))
                        body("'google.com'.country", CoreMatchers.equalTo("United States"))
                        body("'google.com'.isp", CoreMatchers.equalTo("Google"))

                    }
                }
            }
        })
    }
}

class WireMockConfig : WiremockConfigFactory {
    override fun create(): WireMockConfiguration {
        return options().port(9210).notifier(Slf4jNotifier(true))
    }

}

private fun given(block: RequestSpecification.() -> Unit): RequestSpecification = RestAssured.given().apply(block)
private fun RequestSpecification.jsonBody(body: Any): RequestSender = this.contentType(ContentType.JSON).body(body)
private fun RequestSpecification.on(block: RequestSender.() -> Unit): RequestSender = this.`when`().apply(block)
private infix fun Validatable<*, *>.itHas(block: ValidatableResponseOptions<*, *>.() -> Unit): ValidatableResponseOptions<*, *> = this.then().apply(block)