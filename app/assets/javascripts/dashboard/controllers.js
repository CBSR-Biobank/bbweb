/**
 * Dashboard controllers.
 */
define(["angular"], function() {
  "use strict";

  /**
   * user is not a service, but stems from userResolve (Check ../user/services.js) object used by dashboard.routes.
   */
  var DashboardCtrl = function($scope, user) {
    $scope.user = user;
  };
  DashboardCtrl.$inject = ["$scope", "user"];

  var AdminDashboardCtrl = function($scope, user) {
    $scope.user = user;
  };
  AdminDashboardCtrl.$inject = ["$scope", "user"];

  return {
    DashboardCtrl: DashboardCtrl,
    AdminDashboardCtrl: AdminDashboardCtrl
  };

});
