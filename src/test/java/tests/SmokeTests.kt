package tests

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import tools.KotliniumTest
import tools.googlePage

@Tag("Demo")
class SmokeTests: KotliniumTest(){

    @Test
    fun foo(){
        googlePage {
            assert(5*5 == 25){"Not"}
        }
    }
}