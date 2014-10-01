/**
 * User service, exposes user model to the rest of the app.
 */
define(['angular', 'common'], function(angular) {
  'use strict';

  var mod = angular.module('studies.services', ['biobank.common']);

  var setDescription = function (cmd, description) {
    if (description && (description.length > 0)) {
      cmd.description = description;
    }
  };

  /**
   * Service to acccess studies.
   */
  mod.factory('StudyService', [
    '$http', 'BbwebRestApi', function($http, BbwebRestApi) {

      var changeStatus = function(study, status) {
        var cmd = {
          id: study.id,
          expectedVersion: study.version
        };
        return BbwebRestApi.call('POST', '/studies/' + status, cmd);
      };

      return {
        list : function() {
          return BbwebRestApi.call('GET', '/studies');
        },
        query: function(id) {
          return BbwebRestApi.call('GET', '/studies/' + id);
        },
        addOrUpdate: function(study) {
          var cmd = {
            name: study.name
          };

          setDescription(cmd, study.description);

          if (study.id) {
            cmd.id = study.id;
            cmd.expectedVersion = study.version;

            return BbwebRestApi.call('PUT', '/studies/' + study.id, cmd);
          } else {
            return BbwebRestApi.call('POST', '/studies', cmd);
          }
        },
        enable: function(study) {
          return changeStatus(study, 'enable');
        },
        disable: function(study) {
          return changeStatus(study, 'disable');
        },
        retire: function(study) {
          return changeStatus(study, 'retire');
        },
        unretire: function(study) {
          return changeStatus(study, 'unretire');
        },
        dto: {
          processing: function(study) {
            return BbwebRestApi.call('GET', '/studies/dto/processing/' + study.id);
          }
        }
      };
    }
  ]);

  /**
   * Service to access study annotation types.
   */
  mod.factory('StudyAnnotTypeService', ['BbwebRestApi', function(BbwebRestApi) {
    return {
      getAll: function(baseUrl, studyId) {
        return BbwebRestApi.call('GET', baseUrl + '/' + studyId);
      },
      get: function(baseUrl, studyId, annotTypeId) {
        return BbwebRestApi.call('GET', baseUrl + '/'  + studyId + '?annotTypeId=' + annotTypeId);
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
          return BbwebRestApi.call('PUT', baseUrl + '/'  + annotType.id, cmd);
        } else {
          return BbwebRestApi.call('POST', baseUrl, cmd);
        }
      },
      remove: function(baseUrl, annotType) {
        return BbwebRestApi.call(
          'DELETE',
          baseUrl + '/' + annotType.studyId + '/' + annotType.id + '/' + annotType.version);
      },
      valueTypes : function() {
        return BbwebRestApi.call('GET', '/studies/valuetypes');
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
  mod.factory('SpecimenGroupService', ['BbwebRestApi', function(BbwebRestApi) {
    return {
      getAll: function(studyId) {
        return BbwebRestApi.call('GET', '/studies/sgroups/' + studyId);
      },
      get: function(studyId, specimenGroupId) {
        return BbwebRestApi.call('GET', '/studies/sgroups/' + studyId + '?sgId=' + specimenGroupId);
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
          return BbwebRestApi.call('PUT', '/studies/sgroups/' + specimenGroup.id, cmd);
        } else {
          return BbwebRestApi.call('POST', '/studies/sgroups', cmd);
        }
      },
      remove: function(specimenGroup) {
        return BbwebRestApi.call(
          'DELETE',
          '/studies/sgroups/' + specimenGroup.studyId + '/' + specimenGroup.id + '/' + specimenGroup.version);
      },
      anatomicalSourceTypes : function() {
        return BbwebRestApi.call('GET', '/studies/anatomicalsrctypes');
      },
      specimenTypes : function() {
        return BbwebRestApi.call('GET', '/studies/specimentypes');
      },
      preservTypes : function() {
        return BbwebRestApi.call('GET', '/studies/preservtypes');
      },
      preservTempTypes : function() {
        return BbwebRestApi.call('GET', '/studies/preservtemptypes');
      },
      specimenGroupValueTypes : function() {
        return BbwebRestApi.call('GET', '/studies/sgvaluetypes');
      }
    };
  }]);

  /**
   * Service to access Collection Event Types.
   */
  mod.factory('CeventTypeService', ['BbwebRestApi', function(BbwebRestApi) {
    return {
      getAll: function(studyId) {
        return BbwebRestApi.call('GET', '/studies/cetypes/' + studyId);
      },
      get: function(studyId, collectionEventTypeId) {
        return BbwebRestApi.call('GET', '/studies/cetypes/' + studyId + '?cetId=' + collectionEventTypeId);
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
          return BbwebRestApi.call('PUT', '/studies/cetypes/' + collectionEventType.id, cmd);
        } else {
          return BbwebRestApi.call('POST', '/studies/cetypes', cmd);
        }
      },
      remove: function(collectionEventType) {
        return BbwebRestApi.call(
          'DELETE',
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
   * Service to access Specimen Link Annotation Types.
   */
  mod.factory('SpcLinkAnnotTypeService', ['StudyAnnotTypeService', function(StudyAnnotTypeService) {
    var baseUrl = '/studies/slannottype';
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
   * Service to access Processing Types.
   */
  mod.factory('ProcessingTypeService', ['BbwebRestApi', function(BbwebRestApi) {
    return {
      getAll: function(studyId) {
        return BbwebRestApi.call('GET', '/studies/proctypes/' + studyId);
      },
      get: function(studyId, processingTypeId) {
        return BbwebRestApi.call('GET', '/studies/proctypes/' + studyId + '?procTypeId=' + processingTypeId);
      },
      addOrUpdate: function(processingType) {
        var cmd = {
          studyId:     processingType.studyId,
          name:        processingType.name,
          enabled:     processingType.enabled
        };

        setDescription(cmd, processingType.description);

        if (processingType.id) {
          cmd.id = processingType.id;
          cmd.expectedVersion = processingType.version;
          return BbwebRestApi.call('PUT', '/studies/proctypes/' + processingType.id, cmd);
        } else {
          return BbwebRestApi.call('POST', '/studies/proctypes', cmd);
        }
      },
      remove: function(processingType) {
        return BbwebRestApi.call(
          'DELETE',
          '/studies/proctypes/' + processingType.studyId +
            '/' + processingType.id +
            '/' + processingType.version);
      }
    };
  }]);

  /**
   * Service to access Spcecimen Link Types.
   */
  mod.factory('SpcLinkTypeService', ['BbwebRestApi', function(BbwebRestApi) {
    return {
      getAll: function(studyId) {
        return BbwebRestApi.call('GET', '/studies/sltypes/' + studyId);
      },
      get: function(studyId, spcLinkTypeId) {
        return BbwebRestApi.call('GET', '/studies/sltypes/' + studyId + '?slTypeId=' + spcLinkTypeId);
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
          return BbwebRestApi.call('PUT', '/studies/sltypes/' + spcLinkType.id, cmd);
        } else {
          return BbwebRestApi.call('POST', '/studies/sltypes', cmd);
        }
      },
      remove: function(spcLinkType) {
        return BbwebRestApi.call(
          'DELETE',
          '/studies/sltypes/' + spcLinkType.processingTypeId +
            '/' + spcLinkType.id +
            '/' + spcLinkType.version);
      }
    };
  }]);
});

