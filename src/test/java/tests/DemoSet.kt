package tests

import model.Description
import model.Jira
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import tools.KotliniumTest
import tools.googlePage

@Tag("Demo")
class DemoSet : KotliniumTest() {
    @Jira("IDM-18564")
    @Description("When phrase is entered and Search button is pressed, then Google yields some results")
    @ParameterizedTest
    @CsvSource("idmt07,user", "idmt08,admin", "idmt09,user")
    fun `user can search for words and press button to search`(username: String, role: String) {
        googlePage {
            `accept cookies`()
            `type search phrase`("Psy domowe")
            `press enter on Search bar`()
        }
    }

    @Test
    @Jira("IDM-18564")
    @Description("When phrase is entered and enter key is pressed, then Google yields some results")
    fun `user can search for words and press enter to search`() {
        googlePage {
            `accept cookies`()
            `type search phrase`("Psy domowe")
            `press 'search' button`()
        }
    }
}