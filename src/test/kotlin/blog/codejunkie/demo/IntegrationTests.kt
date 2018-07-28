package blog.codejunkie.demo

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
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Mono


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
class IntegrationTests {
    @LocalServerPort
    var port: Int = 0


    @MockBean
    lateinit var ipApiConnector: IpApiConnector

    @BeforeEach
    fun setup() {
        RestAssured.port = port
    }

    @Test
    fun `codejunkieblog return the correct details`() {
        `when`(ipApiConnector.invoke(anyString())).thenReturn(
                Mono.just(HostingDetails(isp = "GoDaddy.com, LLC", country = "United States")))


        given {
            on {
                get("/isp?domain=codejunkie.blog") itHas {
                    statusCode(200)
                    body("country", CoreMatchers.equalTo("United States"))
                    body("isp", CoreMatchers.equalTo("GoDaddy.com, LLC"))
                }
            }
        }

        verify(ipApiConnector).invoke("codejunkie.blog")
    }

    @Test
    fun `codejunkiecom and googlecom return the correct details`() {
        `when`(ipApiConnector.invoke("codejunkie.blog")).
                thenReturn(Mono.just(HostingDetails(isp = "GoDaddy.com, LLC", country = "United States")))
        `when`(ipApiConnector.invoke("google.com")).
                thenReturn(Mono.just(HostingDetails(isp = "Google", country = "United States")))

        given {
            jsonBody(mapOf("domains" to arrayOf("codejunkie.blog", "google.com")))
            on {
                post("/isp") itHas {
                    statusCode(200)
                    body("'codejunkie.blog'.country", CoreMatchers.equalTo("United States"))
                    body("'codejunkie.blog'.isp", CoreMatchers.equalTo("GoDaddy.com, LLC"))
                    body("'google.com'.country", CoreMatchers.equalTo("United States"))
                    body("'google.com'.isp", CoreMatchers.equalTo("Google"))

                }
            }
        }
    }
}

private fun given(block: RequestSpecification.() -> Unit): RequestSpecification = RestAssured.given().apply(block)
private fun RequestSpecification.jsonBody(body: Any): RequestSender = this.contentType(ContentType.JSON).body(body)
private fun RequestSpecification.on(block: RequestSender.() -> Unit): RequestSender = this.`when`().apply(block)
private infix fun Validatable<*, *>.itHas(block: ValidatableResponseOptions<*, *>.() -> Unit): ValidatableResponseOptions<*, *> = this.then().apply(block)