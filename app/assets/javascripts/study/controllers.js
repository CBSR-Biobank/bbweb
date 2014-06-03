/**
 * User controllers.
 */
define(["angular"], function(angular) {
  "use strict";

  /**
   * user is not a service, but stems from userResolve (Check ../user/services.js) object.
   */
  var StudiesCtrl = function($scope, $location, user, studyService) {
    $scope.user = user;
    $scope.studies = studyService.list();
    for (var study in $scope.studies) {
      console.log("study: " + study.name);
    }
  };

  return {
    StudiesCtrl: StudiesCtrl
  };

});
