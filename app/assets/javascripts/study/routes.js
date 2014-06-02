/**
 * Configure routes of user module.
 */
define(["angular", "./controllers", "common"], function(angular, controllers) {
  var mod = angular.module("study.routes", ["study.services", "biobank.common"]);
  mod.config(["$routeProvider", "userResolve", function($routeProvider, userResolve) {
    $routeProvider
      .when("/studies", {
        templateUrl:"/assets/templates/study/studies.html",
        controller:controllers.StudiesCtrl,
        resolve:userResolve});
  }]);
  return mod;
});
