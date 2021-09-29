package pages

import model.DisplayName
import tools.PageObject

class LoginPage : PageObject() {

    private val usernameField by Id("username")
    private val passwordField by Id("password")
    private val loginButton by Xpath(".//button[@type='submit']") {
        it.isDisplayed && usernameField.getAttribute("value").isNotEmpty() && passwordField.getAttribute("value").isNotEmpty()
    }
    private val loginAlert by Xpath(".//div[@id='flash']")

    @DisplayName("user types %s to username field")
    fun `fill username`(name: String) = usernameField.sendKeys(name)

    @DisplayName("user types %s to password field")
    fun `fill password`(password: String) = passwordField.sendKeys(password)

    fun `fill credentials for user`(user: String) {
        `fill username`(user)
        `fill password`("SuperSecretPassword!")
    }

    fun `press 'Login' button`() = loginButton.click()
    fun `user is successfully logged in`() {
        val headerText = loginAlert.text
        assert(headerText.trim().contains("You logged into a secure area!"))
    }
}