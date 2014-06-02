/**
 * User service, exposes user model to the rest of the app.
 */
define(["angular", "common"], function(angular) {
  "use strict";

  var mod = angular.module("study.services", ["biobank.common"]);
  mod.factory("studyService", ["$http", "$q", "playRoutes", function($http, $q, playRoutes) {
    return {
      list : function(credentials) {
        return playRoutes.controllers.StudyControler.list().post(credentials).then(function(response) {
          return response.data;
        });
      }
    };
  }]);
});


