package aspects

import model.TestStepReport
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Aspect
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import tools.driver
import tools.watcher
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


// Added to screenshot name to avoid screenshot name duplication
private var counter = 0L

/**
 * @param testName - name of the Test Set
 * @param stepName - name of currently finished step
 * Takes screenshot of the currently finished step
 * @return relative path to newly created screenshot
 */
private fun getScreenshot(testName: String, stepName: String): String {
    // 0 for current thread, 1 for getScreenshot, 2 for AspectJ call, 3 for ended method
    //val screenshotName = (Thread.currentThread().stackTrace[3].methodName?: "").replace(" ", "_")
    val dateName = SimpleDateFormat("yyyyMMddhhmmss").format(Date())
    val ts = driver as TakesScreenshot
    val source = ts.getScreenshotAs(OutputType.FILE)
    val screenshotName = "$stepName$dateName-$counter.png".also { counter += 1 }
    val destination = "./reports/$testName/$screenshotName"
    val finalDestination = File(destination)
    source.copyTo(finalDestination)
    return screenshotName
}

//list of <li> rows representing each step
val testCaseSteps = mutableListOf<TestStepReport>()

@Aspect
class PageAspect {

    /**
     * @param joinPoint - reference to a method for which the function is called
     */
    @After("execution(void pages.*.*(..))")
    fun collectTest(joinPoint: JoinPoint) {
        if (!isInvokedByTestCase()) return

        val stepName = joinPoint.signature.name
        val methodArgs = joinPoint.args
        val screenshotPath = getScreenshot(watcher.methodName, stepName)
        val timestamp = System.currentTimeMillis()

        testCaseSteps.add(TestStepReport(stepName, methodArgs.map { it.toString() }, screenshotPath, timestamp))
    }
}

//Check if object invoking Test Step was in 'tests' package
// 0 for this function, 1 for PageAspect check, 2 for a calling function (test step), 3 for a caller
fun isInvokedByTestCase(): Boolean {
    return "${Exception().stackTrace[3]}".startsWith("tests")
}