package tools

import pages.CreateNewDocumentPage
import pages.LoginPage
import pages.MyDocumentsPage

fun loginPage(func: LoginPage.() -> Unit){
    LoginPage().func()
}

fun myDocuments(func: MyDocumentsPage.() -> Unit){
    MyDocumentsPage().func()
}

fun createNewDocument(func: CreateNewDocumentPage.() -> Unit){
    CreateNewDocumentPage().func()
}