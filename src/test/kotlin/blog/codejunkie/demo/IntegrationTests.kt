package blog.codejunkie.demo

import io.kotlintest.Description
import io.kotlintest.specs.StringSpec
import io.kotlintest.spring.SpringListener
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Validatable
import io.restassured.response.ValidatableResponseOptions
import io.restassured.specification.RequestSender
import io.restassured.specification.RequestSpecification
import org.hamcrest.CoreMatchers
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTests : StringSpec() {
    @LocalServerPort
    var port: Int = 0

    override fun listeners() = listOf(SpringListener)

    override fun beforeTest(description: Description) {
        RestAssured.port = port
    }

    init {
        "codejunkie.blog return the correct details" {
            given {
                on {
                    get("/isp?domain=codejunkie.blog") itHas {
                        statusCode(200)
                        body("country", CoreMatchers.equalTo("United States"))
                        body("isp", CoreMatchers.equalTo("GoDaddy.com, LLC"))
                    }
                }
            }
        }

        "google.com return the correct details" {
            given {
                on {
                    get("/isp?domain=google.com") itHas {
                        statusCode(200)
                        body("country", CoreMatchers.equalTo("United States"))
                        body("isp", CoreMatchers.equalTo("Google"))
                    }
                }
            }
        }

        "codejunkie.com and google.com return the correct details" {
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
}

private fun given(block: RequestSpecification.() -> Unit): RequestSpecification = RestAssured.given().apply(block)
private fun RequestSpecification.jsonBody(body: Any): RequestSender = this.contentType(ContentType.JSON).body(body)
private fun RequestSpecification.on(block: RequestSender.() -> Unit): RequestSender = this.`when`().apply(block)
private infix fun Validatable<*, *>.itHas(block: ValidatableResponseOptions<*, *>.() -> Unit): ValidatableResponseOptions<*, *> = this.then().apply(block)