/**
 * User service, exposes user model to the rest of the app.
 */
define(["angular", "common"], function(angular) {
  "use strict";

  var mod = angular.module("study.services", ["biobank.common"]);
  mod.factory("studyService", ['$http', '$route', '$q', 'playRoutes', function($http, $route, $q, playRoutes) {
    var studies;
    return {
      list : function() {
        return playRoutes.controllers.study.StudyController.list().get();
      },
      query: function() {
        var id = $route.current.params.id;
        return playRoutes.controllers.study.StudyController.query(id).get();
      }
    };
  }]);
});

