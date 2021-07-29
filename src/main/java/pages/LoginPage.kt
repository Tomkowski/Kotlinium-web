package pages

import tools.PageObjectIDM
import java.util.*

class LoginPage: PageObjectIDM() {

    private val usernameField by Id("username")
    private val passwordField by Id("password")
    private val loginButton by Xpath(".//p[@class='prompt']/input[@value='Login']")
    //3 's' in 'passsword' is not a typo (10th of June, 2021)
    private val forgotPasswordLink by Xpath(".//p[@class='prompt']/a[@class='forgot-passsword-link']")

    private val genericPassword = String(Base64.getDecoder().decode("S3VyNEVsQ2h1cGFjYWJyYQ"))

    fun `provide username`(username: String){
        usernameField.sendKeys(username)
    }

    fun `provide password`(password: String){
        passwordField.sendKeys(password)
    }

    fun `fill credentials of user`(username: String){
        `provide username`(username)
        `provide password`(genericPassword)
    }

    fun `press Login button`(){
        loginButton.click()
    }

    fun `press "Forgot your password?" button`(){
        forgotPasswordLink.click()
    }
}