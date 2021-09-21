package tools

import model.Static
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.FluentWait
import java.time.Duration
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class PageObjectIDM {
    //create a FluentWait object with default timeout time of 10 seconds
    val wait = FluentWait(driver)
        .withTimeout(Duration.ofSeconds(System.getProperty("wait.timeout")?.toLong() ?: 10L))
        .pollingEvery(Duration.ofMillis(200))
        .ignoring(NoSuchElementException::class.java)

    /**
     * Wait for a rule to apply and then returns WebElement
     * @param by - Locator by which the WebElement will be found
     * @param rule - boolean function which specify when element should be returned.
     * @return WebElement under specific WebElement
     */
    fun WebElement.waitForElement(by: By, rule: (WebElement) -> Boolean = { true }): WebElement {
        wait.until { rule(this.findElement(by)) }
        return this.findElement(by)
    }

    fun WebElement.waitForElements(by: By, rule: (List<WebElement>) -> Boolean = { true }): List<WebElement> {
        wait.until { rule(this.findElements(by)) }
        return this.findElements(by)
    }

    fun `wait until page is loaded`() {}

    /**
     * Delegate which returns a WebElements
     * @param locator - function which takes path or and ID of a WebElement and returns its By form
     * @param path - path by which WebElement is found later
     * @param accessRule - rule which has to be fulfilled before WebElement is returned. True by default (always return)
     */
    abstract inner class Locator(
        private val locator: (String) -> By,
        private val path: String,
        private val accessRule: (WebElement) -> Boolean = { true }
    ) : ReadOnlyProperty<Any?, WebElement> {
        private val element: WebElement by lazy {
            wait.until { driver.findElement(locator(path)) }
        }

        /**
         * Evaluates webElement and returns it. Varying on kotlinium.static property or @Static annotation it can be evaluated lazily only once
         * or be evaluated over and over again after each call to the WebElement
         * @return webElement specified by a locator and path properties
         */
        override fun getValue(thisRef: Any?, property: KProperty<*>): WebElement {
            val isStaticAnnotationAvailable =
                property.annotations.find { it.annotationClass == Static::class} != null

            val webElement =
                if (isStaticAnnotationAvailable || System.getProperty("kotlinium.static") == "true") element else wait.until {
                    driver.findElement(locator(path))
                }
            wait.until { accessRule(webElement) }
            return webElement
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