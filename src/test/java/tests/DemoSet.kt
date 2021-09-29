package tests

import model.Description
import model.Jira
import org.junit.jupiter.api.Tag
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import tools.KotliniumTest
import tools.herokuMainPage
import tools.loginPage

@Tag("Demo")
class DemoSet : KotliniumTest() {
    @Jira("Fix-2021")
    @Description("An example of test written using Kotlinium")
    @ParameterizedTest
    @CsvSource("tomsmith,SuperSecretPassword!")
    fun `user can search for words and press button to search`(username: String, password: String) {
        herokuMainPage {
            `verify that header title is equal to`("Welcome to the-internet")
            `click on example`("Form Authentication")
        }
        loginPage {
            `fill credentials for user`(username)
            `press 'Login' button`()
            `user is successfully logged in`()
        }
    }
}