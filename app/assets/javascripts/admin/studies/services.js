/**
 * User service, exposes user model to the rest of the app.
 */
define(['angular', 'common'], function(angular) {
  'use strict';

  var mod = angular.module('study.services', ['biobank.common']);
  mod.factory('studyService', ['$http', '$route', '$q', 'playRoutes', function($http, $route, $q, playRoutes) {
    var studies;
    return {
      list : function() {
        return playRoutes.controllers.study.StudyController.list().get();
      },
      query: function(id) {
        return playRoutes.controllers.study.StudyController.query(id).get();
      },
      addOrUpdate: function(study) {
        var cmd = {
          name: study.name,
          description: study.description
        };

        if (study.id) {
          cmd.id = study.id;
          cmd.expectedVersion = study.version;

          return playRoutes.controllers.study.StudyController.update(study.id).put(cmd);
        } else {
          return playRoutes.controllers.study.StudyController.add().post(cmd);
        }
      },
      valueTypes : function() {
        return playRoutes.controllers.study.StudyController.valueTypes().get();
      },
      anatomicalSourceTypes : function() {
        return playRoutes.controllers.study.StudyController.anatomicalSourceTypes().get();
      },
      specimenTypes : function() {
        return playRoutes.controllers.study.StudyController.specimenTypes().get();
      },
      preservTypes : function() {
        return playRoutes.controllers.study.StudyController.preservTypes().get();
      },
      preservTempTypes : function() {
        return playRoutes.controllers.study.StudyController.preservTempTypes().get();
      },
      participantInfo: function(studyId) {
        return playRoutes.controllers.study.ParticipantAnnotTypeController.get(studyId).get();
      },
      participantAnnotType: function(studyId, participantAnnotTypeId) {
        return playRoutes.controllers.study.ParticipantAnnotTypeController.get(
          studyId, participantAnnotTypeId).get();
      }
    };
  }]);
});

