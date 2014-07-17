/**
 * User service, exposes user model to the rest of the app.
 */
define(['angular', 'common'], function(angular) {
  'use strict';

  var mod = angular.module('studies.services', ['biobank.common']);

  /**
   * Service to acccess studies.
   */
  mod.factory('StudyService', ['$http', function($http) {
    var changeStatus = function(status, study) {
        var cmd = {
          id: study.id,
          expectedVersion: study.version
        };
        return $http.post('/studies/' + status, cmd);
    };

    return {
      list : function() {
        return $http.get('/studies');
      },
      query: function(id) {
        return $http.get('/studies/' + id);
      },
      addOrUpdate: function(study) {
        var cmd = {
          name: study.name,
          description: study.description
        };

        if (study.id) {
          cmd.id = study.id;
          cmd.expectedVersion = study.version;

          return $http.put('/studies/' + study.id, cmd);
        } else {
          return $http.post('/studies', cmd);
        }
      },
      enable: function(study) {
        return changeStatus('enabled', study);
      },
      disable: function(study) {
        return changeStatus('disabled', study);
      },
      retire: function(study) {
        return changeStatus('retire', study);
      },
      unretire: function(study) {
        return changeStatus('unretire', study);
      },
      dto: {
        processing: function(study) {
          return $http.get('/studies/dto/processing/' + study.id);
        }
      }
    };
  }]);

  /**
   * Service to access study annotation types.
   */
  mod.factory('StudyAnnotTypeService', ['$http', function($http) {
    return {
      getAll: function(baseUrl, studyId) {
        return $http.get(baseUrl + '/' + studyId);
      },
      get: function(baseUrl, studyId, annotTypeId) {
        return $http.get(baseUrl + '/'  + studyId + '?annotTypeId=' + annotTypeId);
      },
      addOrUpdate: function(baseUrl, annotType) {
        var cmd = {
          studyId:       annotType.studyId,
          name:          annotType.name,
          description:   annotType.description,
          valueType:     annotType.valueType,
          maxValueCount: annotType.maxValueCount,
          options:       annotType.options
        };

        if (typeof annotType.required !== 'undefined') {
          cmd.required = annotType.required;
        }

        if (annotType.id) {
          cmd.id = annotType.id;
          cmd.expectedVersion = annotType.version;
          return $http.put(baseUrl + '/'  + annotType.id, cmd);
        } else {
          return $http.post(baseUrl, cmd);
        }
      },
      remove: function(baseUrl, annotType) {
        return $http.delete(baseUrl + '/'  + annotType.studyId + '/' + annotType.id + '/' + annotType.version);
      },
      valueTypes : function() {
        return $http.get('/studies/valuetypes');
      }
    };
  }]);

  /**
   * Service to access participant annotation types.
   */
  mod.factory('ParticipantAnnotTypeService', ['StudyAnnotTypeService', function(StudyAnnotTypeService) {
    var baseUrl = '/studies/pannottype';
    return {
      getAll: function(studyId) {
        return StudyAnnotTypeService.getAll(baseUrl, studyId);
      },
      get: function(studyId, annotTypeId) {
        return StudyAnnotTypeService.get(baseUrl, studyId, annotTypeId);
      },
      addOrUpdate: function(annotType) {
        return StudyAnnotTypeService.addOrUpdate(baseUrl, annotType);
      },
      remove: function(annotType) {
        return StudyAnnotTypeService.remove(baseUrl, annotType);
      }
    };
  }]);

  /**
   * Service to access specimen groups.
   */
  mod.factory('SpecimenGroupService', ['$http', function($http) {
    return {
      getAll: function(studyId) {
        return $http.get('/studies/sgroups/' + studyId);
      },
      get: function(studyId, specimenGroupId) {
        return $http.get('/studies/sgroups/' + studyId + '?sgId=' + specimenGroupId);
      },
      addOrUpdate: function(specimenGroup) {
        var cmd = {
          studyId:                     specimenGroup.studyId,
          name:                        specimenGroup.name,
          description:                 specimenGroup.description,
          units:                       specimenGroup.units,
          anatomicalSourceType:        specimenGroup.anatomicalSourceType,
          preservationType:            specimenGroup.preservationType,
          preservationTemperatureType: specimenGroup.preservationTemperatureType,
          specimenType:                specimenGroup.specimenType
        };

        if (specimenGroup.id) {
          cmd.id = specimenGroup.id;
          cmd.expectedVersion = specimenGroup.version;
          return $http.put('/studies/sgroups/' + specimenGroup.id, cmd);
        } else {
          return $http.post('/studies/sgroups', cmd);
        }
      },
      remove: function(specimenGroup) {
        return $http.delete(
          '/studies/sgroups/' + specimenGroup.studyId +
            '/' + specimenGroup.id +
            '/' + specimenGroup.version);
      },
      anatomicalSourceTypes : function() {
        return $http.get('/studies/anatomicalsrctypes');
      },
      specimenTypes : function() {
        return $http.get('/studies/specimentypes');
      },
      preservTypes : function() {
        return $http.get('/studies/preservtypes');
      },
      preservTempTypes : function() {
        return $http.get('/studies/preservtemptypes');
      },
      specimenGroupValueTypes : function() {
        return $http.get('/studies/sgvaluetypes');
      }
    };
  }]);

  /**
   * Service to access Collection Event Types.
   */
  mod.factory('CeventTypeService', ['$http', function($http) {
    return {
      getAll: function(studyId) {
        return $http.get('/studies/cetypes/' + studyId);
      },
      get: function(studyId, collectionEventTypeId) {
        return $http.get('/studies/cetypes/' + studyId + '?cetId=' + collectionEventTypeId);
      },
      addOrUpdate: function(collectionEventType) {
        var cmd = {
          studyId:            collectionEventType.studyId,
          name:               collectionEventType.name,
          description:        collectionEventType.description,
          recurring:          collectionEventType.recurring,
          specimenGroupData:  collectionEventType.specimenGroupData,
          annotationTypeData: collectionEventType.annotationTypeData
        };

        if (collectionEventType.id) {
          cmd.id = collectionEventType.id;
          cmd.expectedVersion = collectionEventType.version;
          return $http.put('/studies/cetypes/' + collectionEventType.id, cmd);
        } else {
          return $http.post('/studies/cetypes', cmd);
        }
      },
      remove: function(collectionEventType) {
        return $http.delete(
          '/studies/cetypes/' + collectionEventType.studyId +
            '/' + collectionEventType.id +
            '/' + collectionEventType.version);
      }
    };
  }]);

  /**
   * Service to access Collection Event Annotation Types.
   */
  mod.factory('CeventAnnotTypeService', ['StudyAnnotTypeService', function(StudyAnnotTypeService) {
    var baseUrl = '/studies/ceannottype';
    return {
      getAll: function(studyId) {
        return StudyAnnotTypeService.getAll(baseUrl, studyId);
      },
      get: function(studyId, participantAnnotTypeId) {
        return StudyAnnotTypeService.get(baseUrl, studyId, participantAnnotTypeId);
      },
      addOrUpdate: function(annotType) {
        return StudyAnnotTypeService.addOrUpdate(baseUrl, annotType);
      },
      remove: function(annotType) {
        return StudyAnnotTypeService.remove(baseUrl, annotType);
      }
    };
  }]);

  /**
   * Service to access Specimen Link Annotation Types.
   */
  mod.factory('SpcLinkAnnotTypeService', ['StudyAnnotTypeService', function(StudyAnnotTypeService) {
    var baseUrl = '/studies/slannottype';
    return {
      getAll: function(studyId) {
        return StudyAnnotTypeService.getAll(baseUrl, studyId);
      },
      get: function(studyId, participantAnnotTypeId) {
        return StudyAnnotTypeService.get(baseUrl, studyId, participantAnnotTypeId);
      },
      addOrUpdate: function(annotType) {
        return StudyAnnotTypeService.addOrUpdate(baseUrl, annotType);
      },
      remove: function(annotType) {
        return StudyAnnotTypeService.remove(baseUrl, annotType);
      }
    };
  }]);

  /**
   * Service to access Processing Types.
   */
  mod.factory('ProcessingTypeService', ['$http', function($http) {
    return {
      getAll: function(studyId) {
        return $http.get('/studies/proctypes/' + studyId);
      },
      get: function(studyId, processingTypeId) {
        return $http.get('/studies/proctypes/' + studyId + '?procTypeId=' + processingTypeId);
      },
      addOrUpdate: function(processingType) {
        var cmd = {
          studyId:     processingType.studyId,
          name:        processingType.name,
          description: processingType.description,
          enabled:     processingType.enabled
        };

        if (processingType.id) {
          cmd.id = processingType.id;
          cmd.expectedVersion = processingType.version;
          return $http.put('/studies/proctypes/' + processingType.id, cmd);
        } else {
          return $http.post('/studies/proctypes', cmd);
        }
      },
      remove: function(processingType) {
        return $http.delete(
          '/studies/proctypes/' + processingType.studyId +
            '/' + processingType.id +
            '/' + processingType.version);
      }
    };
  }]);

  /**
   * Service to access Spcecimen Link Types.
   */
  mod.factory('SpcLinkTypeService', ['$http', function($http) {
    return {
      getAll: function(studyId) {
        return $http.get('/studies/sltypes/' + studyId);
      },
      get: function(studyId, spcLinkTypeId) {
        return $http.get('/studies/sltypes/' + studyId + '?slTypeId=' + spcLinkTypeId);
      },
      addOrUpdate: function(spcLinkType) {
        var cmd = {
          processingTypeId:      spcLinkType.processingTypeId,
          expectedInputChange:   spcLinkType.expectedInputChange,
          expectedOutputChange:  spcLinkType.expectedOutputChange,
          inputCount:            spcLinkType.inputCount,
          outputCount:           spcLinkType.outputCount,
          inputGroupId:          spcLinkType.inputGroupId,
          outputGroupId:         spcLinkType.outputGroupId,
          inputContainerTypeId:  spcLinkType.inputContainerTypeId,
          outputContainerTypeId: spcLinkType.outputContainerTypeId,
          annotationTypeData:    spcLinkType.annotationTypeData
        };

        if (spcLinkType.id) {
          cmd.id = spcLinkType.id;
          cmd.expectedVersion = spcLinkType.version;
          return $http.put('/studies/sltypes/' + spcLinkType.id, cmd);
        } else {
          return $http.post('/studies/sltypes', cmd);
        }
      },
      remove: function(spcLinkType) {
        return $http.delete(
          '/studies/sltypes/' + spcLinkType.processingTypeId +
            '/' + spcLinkType.id +
            '/' + spcLinkType.version);
      }
    };
  }]);
});

