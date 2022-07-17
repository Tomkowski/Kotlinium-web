package pages

import model.DisplayName
import model.Static
import org.openqa.selenium.Keys
import org.openqa.selenium.support.ui.ExpectedConditions
import tools.PageObjectIDM

class GooglePage : PageObjectIDM() {
    @Static
    private val acceptButton by Xpath("//*[@id=\"L2AGLb\"]/div")
    private val searchBar by Xpath(".//input[@title='Search']")
    @Static
    private val searchButton by Xpath(".//div[@class = 'FPdoLc lJ9FBc']//input[@value='Google Search']")

    fun `accept cookies`() = acceptButton.click()
    @DisplayName("user types %s in search box")
    fun `type search phrase`(phrase: String) = searchBar.sendKeys(phrase)
    fun `press 'search' button`() = searchButton.click()
    fun `press enter on Search bar`() = searchBar.sendKeys(Keys.RETURN)
}