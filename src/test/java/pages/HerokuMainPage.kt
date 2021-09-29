package pages

import model.DisplayName
import model.Static
import tools.PageObject

class HerokuMainPage : PageObject() {
    @Static
    private val header by Xpath(".//h1[@class='heading']")

    @DisplayName("user selects %s example from main page")
    fun `click on example`(example: String) {
        val exampleRow by Xpath(".//li/a[text()='$example']")
        exampleRow.click()
    }

    fun `verify that header title is equal to`(text: String) {
        assert(header.text == text) { "Header title should be $text but was ${header.text}"}
    }
}