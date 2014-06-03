/**
 * User controllers.
 */
define(["angular"], function(angular) {
  "use strict";

  /**
   * user is not a service, but stems from userResolve (Check ../user/services.js) object.
   */
  var StudiesCtrl = function($scope, $location, user, studyService) {
    studyService.list().then(function(response) {
      $scope.studies = response.data;
      $scope.user = user;
    });
  };

  return {
    StudiesCtrl: StudiesCtrl
  };

});
