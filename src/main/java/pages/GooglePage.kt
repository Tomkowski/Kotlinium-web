package pages

import org.openqa.selenium.Keys
import tools.PageObjectIDM

class GooglePage : PageObjectIDM() {
    private val acceptButton by Xpath(".//div[@class='KxvlWc']//button[contains(string(), 'Zgadzam')]")
    private val searchBar by Xpath(".//input[@title='Szukaj']")
    private val searchButton by Xpath(".//div[@class='UUbT9']//input[@value='Szukaj w Google']")

    fun `accept cookies`() = acceptButton.click()
    fun `type search phrase`(phrase: String) = searchBar.sendKeys(phrase)
    fun `press search buton`() = searchButton.click()
    fun `press enter on Search bar`() = searchBar.sendKeys(Keys.RETURN)
}