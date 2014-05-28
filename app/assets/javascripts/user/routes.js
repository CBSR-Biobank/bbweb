/**
 * Configure routes of user module.
 */
define(["angular", "./controllers", "common"], function(angular, controllers) {
  var mod = angular.module("user.routes", ["user.services", "yourprefix.common"]);
  mod.config(["$routeProvider", function($routeProvider) {
    $routeProvider
      .when("/login", {templateUrl:"/assets/templates/user/login.html", controller:controllers.LoginCtrl});
      //.when("/users", {templateUrl:"/assets/templates/user/users.html", controller:controllers.UserCtrl})
      //.when("/users/:id", {templateUrl:"/assets/templates/user/editUser.html", controller:controllers.UserCtrl});
  }]);
  return mod;
});
