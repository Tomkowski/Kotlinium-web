package tools

import business.environmentURL
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.util.concurrent.ThreadLocalRandom
import java.util.logging.Logger
import kotlin.reflect.KClass
import kotlin.streams.asSequence

lateinit var driver: WebDriver
val logger: Logger = Logger.getLogger(KotliniumTest::class.simpleName)

fun Array<Annotation>.findValue(annotation: KClass<*>): Annotation? {
    return find { it.annotationClass == annotation }
}

fun openPage(website: String) {
    driver.get(website).also {
        WebDriverWait(driver, 60).until { driver.executeScript("return document.readyState") == "complete" }
    }
    logger.info("opened $environmentURL")
}

fun randomString(size: Int): String {
    val stringSize = size.coerceAtLeast(0)
    val characters = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return ThreadLocalRandom.current()
        .ints(stringSize.toLong(), 0, characters.size)
        .asSequence()
        .map(characters::get)
        .joinToString("")
}

fun File.mkSubDirs(){
    with(this.path){
        //file separator is either '/' or '\', but is received as String. [0] to get char.
        File(substring(0, indexOfLast { it == System.getProperty("file.separator")[0] })).mkdirs()
    }
}

fun WebDriver.executeScript(script: String, vararg args: Any?): Any{
    return (this as JavascriptExecutor).executeScript(script, args)
}

/**
 * Checks if element is displayed, otherwise scrolls to the element and checks again.
 * @return element is currently displayed.
 */
val WebElement.isCurrentlyDisplayed: Boolean
get() {
    return this.isDisplayed || let{ driver.executeScript("arguments[0].scrollIntoView(true);", it as WebElement); this.isDisplayed}
}