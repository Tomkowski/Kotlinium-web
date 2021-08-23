package pages

import org.openqa.selenium.Keys
import tools.PageObjectIDM

class GooglePage : PageObjectIDM() {
    private val acceptButton by Xpath(".//div[@class='KxvlWc']//button[contains(string(), 'agree')]")
    private val searchBar by Xpath(".//input[@title='Search']")
    private val searchButton by Xpath(".//div[@class='UUbT9']//input[@value='Google Search']")

    fun `accept cookies`() = acceptButton.click()
    fun `type search phrase`(phrase: String) = searchBar.sendKeys(phrase)
    fun `press search buton`() = searchButton.click()
    fun `press enter on Search bar`() = searchBar.sendKeys(Keys.RETURN)
}