define(['./module', 'angular'], function(module, angular) {
  'use strict';

  module.service('studyAnnotTypesService', StudyAnnotTypesService);

  StudyAnnotTypesService.$inject = ['biobankXhrReqService', 'domainEntityService'];

  /**
   * Service to access study annotation types.
   */
  function StudyAnnotTypesService(biobankXhrReqService, domainEntityService) {
    var services = {
      getAll      : getAll,
      get         : get,
      addOrUpdate : addOrUpdate,
      remove      : remove,
      valueTypes  : valueTypes
    };
    return services;

    //-------

    function uri(annotTypeUri, studyId, annotTypeId, version) {
      var result = '/studies';
      if (arguments.length < 2) {
        throw new Error('annotTypeUri or study id not specified');
      } else {
        result += '/' + studyId + '/' + annotTypeUri;

        if (arguments.length > 2) {
          result += '/' + annotTypeId;
        }

        if (arguments.length > 3) {
          result += '/' + version;
        }
      }
      return result;
    }

    function getAll(annotTypeUri, studyId) {
      return biobankXhrReqService.call('GET', uri(annotTypeUri, studyId));
    }

    function get(annotTypeUri, studyId, annotTypeId) {
      return biobankXhrReqService.call('GET', uri(annotTypeUri, studyId) + '?annotTypeId=' + annotTypeId);
    }

    function addOrUpdate(annotTypeUri, annotType) {
      var cmd = {
        studyId:       annotType.studyId,
        name:          annotType.name,
        valueType:     annotType.valueType,
        maxValueCount: annotType.maxValueCount
      };

      angular.extend(cmd, domainEntityService.getOptionalAttribute(annotType, 'description'));
      angular.extend(cmd, domainEntityService.getOptionalAttribute(annotType, 'options'));

      if (typeof annotType.required !== 'undefined') {
        cmd.required = annotType.required;
      }

      if (annotType.id) {
        cmd.id = annotType.id;
        cmd.expectedVersion = annotType.version;
        return biobankXhrReqService.call('PUT', uri(annotTypeUri, annotType.studyId, annotType.id), cmd);
      } else {
        return biobankXhrReqService.call('POST', uri(annotTypeUri, annotType.studyId), cmd);
      }
    }

    function remove(annotTypeUri, annotType) {
      return biobankXhrReqService.call(
        'DELETE',
        uri(annotTypeUri, annotType.studyId, annotType.id, annotType.version));
    }

    function valueTypes() {
      return biobankXhrReqService.call('GET', '/studies/valuetypes');
    }


  }

});
