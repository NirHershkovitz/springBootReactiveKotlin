package blog.codejunkie.demo

import io.kotlintest.specs.StringSpec
import io.restassured.RestAssured
import io.restassured.RestAssured.`when`
import io.restassured.http.ContentType
import io.restassured.response.Validatable
import io.restassured.response.ValidatableResponseOptions
import io.restassured.specification.RequestSender
import io.restassured.specification.RequestSpecification
import org.junit.Test


class SanityE2ETestsJavaStyle {
    @Test
    fun `isp details service passes sanity - Java`() {
        `when`().
            get("/isp?domain=codejunkie.blog").
        then().
            statusCode(200)
    }
}

class SanityE2ETestsPlain : StringSpec({
    "isp details service passes sanity - Plain" {
        `when`().
            get("/isp?domain=codejunkie.blog").
        then().
            statusCode(200)
    }
})

class SanityE2ETests : StringSpec({
    "isp details service passes sanity for a single domain as input" {
        given {
            on {
                get("/isp?domain=codejunkie.blog") itHas {
                    statusCode(200)
                }
            }
        }
    }

    "isp details service passes sanity for multiple domains as input" {
        given {
            jsonBody(mapOf("domains" to arrayOf("codejunkie.blog", "google.com")))
            on {
                post("/isp") itHas {
                    statusCode(200)
                }
            }
        }
    }
})


private fun given(block: RequestSpecification.() -> Unit): RequestSpecification = RestAssured.given().apply(block)
private fun RequestSpecification.jsonBody(body: Any): RequestSender = this.contentType(ContentType.JSON).body(body)
private fun RequestSpecification.on(block: RequestSender.() -> Unit): RequestSender = this.`when`().apply(block)
private infix fun Validatable<*, *>.itHas(block: ValidatableResponseOptions<*, *>.() -> Unit): ValidatableResponseOptions<*, *> = this.then().apply(block)