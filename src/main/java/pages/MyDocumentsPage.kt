package pages

import tools.PageObjectIDM

class MyDocumentsPage: PageObjectIDM() {

    private val disclaimer by Xpath(".//div[contains(@class, 'disclaimer')]")
    private val disclaimerEnterAppButton by Xpath(".//div[contains(@class, 'disclaimer')]//button[contains(@class, 'enter-app')]")
    private val disclaimerLeaveAppButton by Xpath(".//div[contains(@class, 'disclaimer')]//button[contains(@class, 'leave-app')]")

    private val createButton by Xpath(".//a[@class='ui--button-primary js-navigate']/span[text()='Create Document']/parent::a")
    private val importButton by Xpath(".//a[@class='ui--button-primary js-navigate']/span[text()='Import Document']/parent::a")

    fun `press Create button`(){
        createButton.click()
    }

    fun `accept disclaimer`(){
        disclaimerEnterAppButton.click()
    }

    fun `cancel disclaimer`(){
        disclaimerLeaveAppButton.click()
    }
    fun `verify disclaimer is visible`(){
        `wait until page is loaded`()
        wait.until {disclaimer.isDisplayed}
    }
}