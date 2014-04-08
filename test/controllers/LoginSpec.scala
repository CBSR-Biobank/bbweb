package controllers

import fixture._

import scala.concurrent._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory

//@RunWith(classOf[JUnitRunner])
//class LoginSpec extends Specification {
//
//  private val log = LoggerFactory.getLogger(this.getClass)
//
//  val nameGenerator = new NameGenerator(classOf[LoginSpec].getName)
//
//  //  "Application" should {
//
//  // this compiles fine
//  //    "show the login page" in {
//  //running(FakeApplication()) {
//  //val result = route(FakeRequest(GET, "/")).get
//  //val result = controllers.Application.index process FakeRequest()
//
//  //status(result) must equalTo(SEE_OTHER)
//  //header("Location", result) must beSome(securesocial.controllers.LoginPage.login.toString)
//  //    }
//  //    }
//
//  //}
//}