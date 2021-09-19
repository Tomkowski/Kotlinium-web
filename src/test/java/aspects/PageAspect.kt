package aspects

import model.TestStepReport
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import tools.driver
import tools.logger
import tools.testName
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


// Added to screenshot name to avoid screenshot name duplication
private var counter = 0L

/**
 * @param testName - name of the Test Set
 * @param stepName - name of currently finished step
 * Takes screenshot of the currently finished step
 * @return relative path to newly created screenshot
 */
private fun getScreenshot(testName: String, stepName: String): String {
    val dateName = SimpleDateFormat("yyyyMMddhhmmss").format(Date())
    val ts = driver as TakesScreenshot
    val source = ts.getScreenshotAs(OutputType.FILE)
    val screenshotName = normalizeScreenshotName("$stepName$dateName-$counter.png").also { counter += 1 }
    val destination = "./reports/$testName/$screenshotName"
    val finalDestination = File(destination)
    source.copyTo(finalDestination)
    return screenshotName
}

private fun normalizeScreenshotName(name: String): String {
    return name.replace("(['\"<>:])".toRegex(), "_")
        .replace("&#", "& #")
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
        val screenshotPath = getScreenshot(testName, stepName)
        val timestamp = System.currentTimeMillis()

        testCaseSteps.add(TestStepReport(stepName, methodArgs.map { it.toString() }, screenshotPath, timestamp))
    }

    @Before("execution(void tests.*.*(..))")
    fun createTestName(joinPoint: JoinPoint) {
        logger.info("${joinPoint.signature.name} test was run")
        testName =
            joinPoint.signature.name + if (joinPoint.args.isNotEmpty()) "- ${joinPoint.args.joinToString(" ") { "[$it]" }}" else ""
    }
}

//Check if object invoking Test Step was in 'tests' package
// 0 for this function, 1 for PageAspect check, 2 for a calling function (test step), 3 for a caller
fun isInvokedByTestCase(): Boolean {
    return "${Exception().stackTrace[3]}".startsWith("tests")
}