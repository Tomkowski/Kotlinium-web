package tools

import pages.HerokuMainPage
import pages.LoginPage

fun herokuMainPage(func: HerokuMainPage.() -> Unit){
    HerokuMainPage().func()
}

fun loginPage(func: LoginPage.() -> Unit){
    LoginPage().func()
}