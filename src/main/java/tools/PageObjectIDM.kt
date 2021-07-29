package tools

import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.FluentWait
import java.time.Duration
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class PageObjectIDM {
    val wait = FluentWait(driver)
        .withTimeout(Duration.ofSeconds(10))
        .pollingEvery(Duration.ofMillis(200))
        .ignoring(NoSuchElementException::class.java)

    fun WebElement.waitForElement(by: By, rule: (WebElement) -> Boolean = {true}): WebElement{
        wait.until { rule(this.findElement(by)) }
        return this.findElement(by)
    }
    fun WebElement.waitForElements(by: By, rule: (List<WebElement>) -> Boolean = {true}): List<WebElement>{
        wait.until { rule(this.findElements(by)) }
        return this.findElements(by)
    }
    fun `wait until page is loaded`() {
        wait.until {
            with(driver.findElements(By.xpath(".//div[@class='nanobar']/child::div"))) {
                return@until size == 1 && this[0].getCssValue("width") == "0px"
            }
        }
    }

    abstract inner class Locator(
        private val locator: (String) -> By,
        private val path: String,
        private val accessRule: (WebElement) -> Boolean = { true }
    ) : ReadWriteProperty<Any?, WebElement> {
        private val element: WebElement by lazy {
            wait.until { driver.findElement(locator(path)) }
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): WebElement {
            wait.until { accessRule(element) }
            return element
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: WebElement) {
        }
    }

    inner class Xpath(
        path: String,
        accessRule: (WebElement) -> Boolean = { true }
    ) : Locator(By::xpath, path, accessRule)

    inner class CssSelector(
        path: String,
        accessRule: (WebElement) -> Boolean = { true }
    ) : Locator(By::cssSelector, path, accessRule)

    inner class Id(
        id: String,
        accessRule: (WebElement) -> Boolean = { true }
    ) : Locator(By::id, id, accessRule)
}