package blog.codejunkie.demo.extra

import blog.codejunkie.demo.IpApiConnector
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.ALL
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router

/**
 * Present how to use the new functional routing instead of a controller
 */
@Configuration
class RoutesExample(val connector : IpApiConnector) {

    @Bean
    fun router() = router {
        (accept(ALL)).nest {
                GET("/ispUsingRouter", {
                    ok().
                        contentType(APPLICATION_JSON).
                        body(connector.invoke(it.queryParam("domain").get()))})
            }
    }
}