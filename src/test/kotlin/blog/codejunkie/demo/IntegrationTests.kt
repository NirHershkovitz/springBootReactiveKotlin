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
        "isp controller passes integration" {
            given {
                on {
                    get("/isp") itHas {
                        statusCode(200)
                        body("country", CoreMatchers.equalTo("United States"))
                        body("isp", CoreMatchers.equalTo("GoDaddy.com, LLC"))
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