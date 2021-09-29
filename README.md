# Kotlinium - Guide

Kotlinium is a UI tests framework for Kotlin, built based on JUnit5 and Selenium functionalities.
Main goal of the framework is to enable writing tests in DSL-like style and generating customizable test reports.
![](https://imgur.com/KzxxCLK.png)

## 1. Wiring up
Currently, Kotlinium is not available as Gradle or Maven dependency.
In order to test the framework out, please fork this repository or download it as a standalone project.
## 2. Writing first test
All tests are defined in `tests` directory (`./src/test/java/tests`).
Each test must be a part of a **Test Set**. Test sets are defined by classes which extends `KotliniumTest` class (this class is responsible for monitoring and collecting test data).
Example of `DemoSet` Test Set and a simple test defined inside. 
```kotlin
@Tag("Demo")  
class DemoSet : KotliniumTest() {  
    @Jira("Fix-2021")  
    @Description("An example of test written using Kotlinium")  
    @Test
    fun `user can access website and log in`() {  
        mainPage {  
		`verify that header title is equal to`("Welcome to the-internet")  
   		`click on 'Sign in' button`()  
        }  
	
	loginPage {  
		`fill credentials for user`("tomsmith")  
 		`press 'Login' button`()  
		`verify that user is successfully logged in`()  
        }  
    }  
}
```
### 2.1. Robots
As you may have noticed by now, test steps are divided in scopes called *robots*.
Each robot holds a reference to a *PageObject* object for which set of functions is performed. The only available functions in each robot are functions that were defined inside *PageObject* class for which robot was created.

To put it in simple example:
*Dog* PageObject can bark and eat bone but has no access to parrots's functions.
```kotlin
parrot{
	fly()
	`eat seeds`()
}
dog{
	bark()
	`eat bone`()
	//cannot fly and eat seeds unless is taught how in its PageObject class.
}
``` 
#### 2.1.1. How robots are made
Robots are defined in `robots.kt` file under `tools` package.
Robots are basically a higher order functions. As a parameter, they accept **unit** functions defined in particular class (PageObject class).
In function's body, there is a new object created for which all provided functions are executed.
```kotlin
fun loginPage(func: LoginPage.() -> Unit){  
    LoginPage().func() 
}
```
As you can see, provided functions are invoked on PageObject object, so it must have access to them.
### 2.2. PageObject
Kotlinium provides a `PageObject` class which includes helpful tools for quicker test development.
Below `LoginPage` class represents all functionalities provided by Kotlinium framework.
Next subchapters will cover all of them.
```kotlin
class LoginPage : PageObject() {  
  
    private val usernameField by Id("username")  
    private val passwordField by Id("password")  
    private val loginButton by Xpath(".//button[@type='submit']") {  
	it.isDisplayed && usernameField.getAttribute("value").isNotEmpty() && passwordField.getAttribute("value").isNotEmpty()  
    }  
    @Static
    private val loginAlert by Xpath(".//div[@id='flash']")
    
    @Invisible
    private val fileInput by Xpath(".//input[@type='file']")  
  
    @DisplayName("user types %s to username field")  
    fun `fill username`(name: String) = usernameField.sendKeys(name)  
  
    @DisplayName("user types %s to password field")  
    fun `fill password`(password: String) = passwordField.sendKeys(password)  
  
    fun `fill credentials for user`(user: String) {  
        `fill username`(user)  
        `fill password`("SuperSecretPassword!")  
    }  
      
    fun `press 'Login' button`() = loginButton.click()  
}
```
#### 2.2.1. Locator delegate
WebElements assignment can be delegated to Locator classes. Currently supported locators are **ID**, **Xpath** and **CSS**.
Delegate accepts path as a mandatory parameter and optional access rule function:(`WebElement -> Boolean`).
By default, access rule is set to `webElement -> webElement.isDisplayed`.
Default access rule ensures that web elements are accessed only when they are visible on a screen so no implicit wait function inside test function is required.
Access rule is customizable as shown in `loginButton` example.
```kotlin
private val usernameField by Id("username")  
private val passwordField by Id("password")  
private val loginButton by Xpath(".//button[@type='submit']") {
	//wait until both username and password fields are filled in.   
	it.isDisplayed && usernameField.getAttribute("value").isNotEmpty() && passwordField.getAttribute("value").isNotEmpty()  
}  
```
#### 2.2.2. Static elements
Some elements may not change during whole test case execution. In such scenario, we do not need to find and evaluate them over and over again. Instead, we can cache and retrieve same reference to them at any time.
Using `@Static` annotation cause element to be cached in memory and be evaluated only once.
```kotlin
@Static
private val loginAlert by Xpath(".//div[@id='flash']")
```
**Note**: Be careful when using this, as dynamic elements annotated with this can cause [Stale Element Exceptions](https://www.selenium.dev/exceptions/#stale_element_reference).
#### 2.2.3. Invisible elements
Some elements are not visible by user and are hidden by default.
Good example of the invisible elements are input fields such as file input which is 0px wide and is triggered by some JS logic on a custom div element.
For such hidden elements, we do not necessarily want to wait until they show up (as described in *access rule* in chapter 2.2.1).
`@Invisible` annotation causes web element to drop its access rule and be returned immediately on demand.
It has same effect on web elements as if access rule was set to `webElement -> true` (always return).
>Should I use @Invisible annotation or change access rule of a web element?

*Use whichever you see fit. Me personaly, I do not like {true} access rules defined as they might be not intuitive for others.* 
#### 2.2.4. DisplayName
You may want present your reports to non-technical part of your team or company. Test case functions' names and syntax of a Kotlin language may not be clear to them. In such cases you may want to use `@DisplayName` annotation.
It takes a string which will replace a default name of your test function.
Let us take a peek at the following function:
```kotlin
fun `fill username`(name: String) = usernameField.sendKeys(name)  
 ```
 Had the function been invoked in some test case, it would have appeared as `Fill username: [your typed username]`.
 Let us modify the function by adding `@DisplayName` annotation:
 ```kotlin
@DisplayName("user types %s to username field")
fun `fill username`(name: String) = usernameField.sendKeys(name)    
```
 Now the output will be: *User types **your typed username** to username field*
 
 Each `%s` characters set (mark) will be replaced with each consecutive function's parameter.
 Parameters are of any type (`toString()` method is invoked for each)
 >What if I add too many %s marks or too little %s marks compared to amount of function's parameters?
 >
*If too many %s marks are provided, then each unpaired mark (starting from the left) will remain as %s in final output.*
*Too little marks will cause only first parameters to be included in the final output.* 

## 3. Generating reports
After each test run there is a report summary created.
Test reports are put in `./reports` directory which contains following structure:
- *json* directory which holds JSON file for each Test Set
- directories for all test cases runs.
- Report summary in HTML file
#### 3.1. JSON file
Kotlinium provides you with JSON file so you can craft your very own test summary based on information you can find in it.
```json
[  
  {  
  "jiraID": "Fix-2021",  
  "testCaseName": "user can search for words and press button to search - [tomsmith] [SuperSecretPassword!]",  
  "description": "An example of test written using Kotlinium",  
  "stepsList": [  
		  ...
		  {  
			"stepName": "click on example",  
			"stepParameters": [  
				"Form Authentication"  
			],  
			"screenshotPath": "click on example20210928062943-1.png",  
			"timestamp": 1632846583239,  
			"displayName": "user selects %s example from main page",  
			"testPassed": true  
			},
			...  
	],
	"stackTrace": ""  
  }  
]
```
#### 3.2. ReportBuilder
In case you do not want to create your own test summary, there is a basic report builder available.
Report builder is found in `tools.ReportBuilder` file.
It has all necessary logic to build a full report summary based on JSON file.
It is using `reportTemplate.html` and `summaryTemplate.html` files available in `resources` file to inject JSON data inside.
Feel free to tweak around and change some styles as you see fit.
![](https://imgur.com/KzxxCLK.png)
Simple example of test report generated for one of the test cases.
Each test case is a part of full Test Set summary.
## 4. Kotlinium properties
Kotlinium supports modifying test execution with a help of various properties.
#### 4.1. System properties
System properties can be provided directly from command line when running Maven plugin or by declaring them in`kotlinium.properties` file in `resources` directory.
There is a set of supported properties:
1. *webdriver.chrome.driver* - specifies path to web driver 
2.  *webdriver.type* - specifies which browser will be used. *Chrome* (default), *Firefox* and *Opera* are supported.
3. *environment* - specifies what URL will be opened before each test.
4. *screenshot.strategy* - specifies when screenshot should be taken. Following options are supported:
*always* - take screenshot after each step
*on-fail* - take screenshot only on failed step
*never* (any string) - do not take screenshots
5. *kotlinium.static* - specifies if all elements should be cached no matter the `@Static` annotation.
Value is either `true` or `false` (any string is equal to `false`).
#### 4.2. Driver properties
Specifying driver properties are simple as defining them in `driver.properties` file found in `resources` file.
Add each in new line and comment out unwanted ones using `#` character.
Example:
```properties
--headless  
--disable-gpu  
--window-size=1920,1200  
--incognito  
--lang=en-GB  
#--ignore-certificate-errors  
#--disable-extensions
```
Please look up properties on the internet for the specific web driver you wish to use.

## 5. JUnit 5 compatibility
As mentioned before, Kotlinium was written in mind to be fully compatible with JUnit framework.
Currently, it supports following JUnit features:
- running Test Sets by tag
- extending tests with a watcher
- using parameterized tests

Simple example of a parameterized test:
```kotlin
@ParameterizedTest  
@CsvSource("idmt07,user", "idmt08,admin", "idmt09,user")  
fun `simple log in`(username: String, role: String) {  
  loginPage {  
	`fill credentials for user`(username, role)  
        `press 'Login' button`()  
        `user is successfully logged in`()  
    }  
}
```
Note that parameters are provided as described in JUnit 5 documentation. Way in which parameters are provided to the test function does not affect its execution.

Currently, the biggest disadvantage I see is a lack of the parallel test execution support. It would require to modify the way driver is set in whole project. Current solution creates 1 global driver which is reset after each Test Case.
Running tests in parallel requires test functions to have unique web driver running only in that particular test case.

## 6. Contributing
I would love to see you enjoy the Kotlinium framework.
If there is anything you want to be changed, please create pull request where you explain missing features or bug you have found.
Feel free to fork the repository and contribute to it.
In case of any further questions please contact me:
E-mail: tomek[at]siemieni.uk

## License
[MIT](https://choosealicense.com/licenses/mit/)
