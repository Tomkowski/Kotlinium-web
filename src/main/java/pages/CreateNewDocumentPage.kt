package pages

import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebElement
import tools.PageObjectIDM
import tools.driver
import tools.randomString

class CreateNewDocumentPage : PageObjectIDM() {

    private val logoIDM by Xpath(".//h1[@class='idm-logo']")

    private val recentTemplates by Id("recent")
    private val allDocumentsTypes by Id("all")
    private val searchDocumentsTypes by Id("search")

    fun `verify template is visible on Recent tab`(templateName: String) {
        val template by Xpath(".//td[@class='h_name']/a[text()='$templateName']")
        assert(template.isDisplayed) { "$template template is not visible on Recently used templates!" }
    }

    fun `select recent template`(templateName: String) {
        val template by Xpath(".//td[@class='h_name']/a[text()='$templateName']")
        template.click()
    }

    fun `open Recent tab`() {
        recentTemplates.click()
    }

    fun `open All Document Types`() {
        allDocumentsTypes.click()
    }

    fun `open Search Document Types`() {
        searchDocumentsTypes.click()
    }

    fun `select domain`(domain: String) {
        val domainRow by Xpath(".//li[@class='row']//span[contains(text(), '$domain')]/ancestor::a")
        domainRow.click()
    }

    fun `select All Document Types template`(template: String) {
        `select domain`(template) // basically does the same no need to duplicate the code
        `wait until page is loaded`()
    }

    fun `fill mandatory properties`() {
        val allProperties = driver.findElements(By.xpath(".//section[@data-section-panel='mandatory']/child::div"))
        val mandatoryProperties = allProperties.filter {
            try {
                it.findElement(By.xpath("//label[contains(@class, 'mandatory')]")) != null
            } catch (exception: NoSuchElementException) {
                false
            }
        }
        mandatoryProperties.forEach {
            enterPropertyBasedOnType(it)
        }
    }

    fun `fill property`(propertyName: String, value: String) {
        val propertyField by Xpath(".//div[@class='form--label-wrap']/label[starts-with(normalize-space(),'$propertyName')]/ancestor::div[@class='form--group']")
        enterPropertyBasedOnType(propertyField, value)
    }

    private fun enterPropertyBasedOnType(propertyField: WebElement, value: String = "") {
        val propertyInputField =
            propertyField.findElement(By.xpath("./div[@class='form--field-wrap']/*[not(self::div) and not(self::ul)]"))

        with(propertyInputField) {
            //value already filled in
            if (getAttribute("valid") == "true") return

            when (tagName) {
                "input" -> {
                    handleInputProperty(this, value)
                }
                "select" -> {
                    handleSelectProperty(this, value)
                }
                "textarea" -> {
                    handleTextAreaProperty(this, value)
                }
            }
            logoIDM.click() // lose focus on input field so its status can be changed to valid
            wait.until { getAttribute("valid") == "true" }
        }
    }

    private fun handleInputProperty(element: WebElement, value: String = "") {

        with(element) {
            when {
                getAttribute("class").contains("ui-autocomplete-input") -> {
                    sendKeys(if (value.isEmpty()) "  " else value) // double space to show all results
                    `wait until page is loaded`()
                    val productLists = findElement(By.xpath("./../ul"))
                    val options = productLists.waitForElements(By.tagName("li")){ it.isNotEmpty() }
                    with(options) {
                        if (value.isEmpty()) {
                            random().click()
                        } else {
                            find { row -> row.text.contains(value) }?.click()
                        }
                    }
                }
                getAttribute("class").contains("datepicker") -> {
                    sendKeys(if (value.isEmpty()) "26 Jul 2023" else value)
                }
                else -> {
                    sendKeys(if(value.isEmpty()) randomString(10) else value)
                }
            }
        }
    }

    private fun handleTextAreaProperty(element: WebElement, value: String = "New text - UI Kotlinium") {
        element.sendKeys(value)
    }

    private fun handleSelectProperty(element: WebElement, value: String = "") {
        val selectableList = element.findElement(By.xpath("./../div"))
        with(selectableList) {
            click() //expand
            waitForElements(By.xpath(".//li")).random().click()
            //findElements(By.xpath(".//li")).random().click()
        }
    }
}