package bob.simple.spring.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @GetMapping("/test/a")
    fun a(): String {
        return "Hello Spring"
    }
}
