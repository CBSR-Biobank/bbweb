/**
 * User service, exposes user model to the rest of the app.
 */
define(["angular", "common"], function(angular) {
  "use strict";

  var mod = angular.module("study.services", ["biobank.common"]);
  mod.factory("studyService", ['$q', '$http', function($q, $http) {
    return {
      list : function(credentials) {
        return $http.get('/studies');
      }
    };
  }]);
});


