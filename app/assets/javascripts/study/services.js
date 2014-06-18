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

        if (study.id === undefined) {
          return playRoutes.controllers.study.StudyController.add().post(cmd);
        } else {
          cmd.id = study.id;
          cmd.expectedVersion = study.version + 1;

          return playRoutes.controllers.study.StudyController.update(study.id).put(cmd);
        }
      },
      participantInfo: function(studyId) {
        return playRoutes.controllers.study.ParticipantAnnotTypeController.list(studyId).get();
      }
    };
  }]);
});

