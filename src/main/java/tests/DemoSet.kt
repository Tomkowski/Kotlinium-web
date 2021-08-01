package tests

import org.junit.Test
import tools.*

class DemoSet: KotliniumTest() {
    @Test
    @Jira("IDM-18564")
    @Description("When phrase is entered and Search button is pressed, then Google yields some results")
    fun `user can search for words and press button to search`() {
        googlePage {
            `accept cookies`()
            `type search phrase`("Psy domowe")
            `press search buton`()
        }
    }
    @Test
    @Jira("IDM-18564")
    @Description("When phrase is entered and enter key is pressed, then Google yields some results")
    fun `user can search for words and press enter to search`() {
        googlePage {
            `accept cookies`()
            `type search phrase`("Psy domowe")
            `press enter on Search bar`()
            `accept cookies`()
        }
    }
}