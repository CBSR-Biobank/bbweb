/**
 * Dashboard routes.
 */
define(["angular", "./controllers", "common"], function(angular, controllers) {
  "use strict";

  var mod = angular.module("dashboard.routes", ["yourprefix.common"]);
  mod.config(["$routeProvider", "userResolve", function($routeProvider, userResolve) {
    $routeProvider
      .when("/dashboard",  {templateUrl: "/assets/templates/dashboard/dashboard.html",  controller:controllers.DashboardCtrl, resolve:userResolve});
      //.when("/admin/dashboard",  {templateUrl: "/assets/templates/dashboard/admin.html",  controller:controllers.AdminDashboardCtrl})
  }]);
  return mod;
});
