define(['./module'], function(module) {
  'use strict';

  module.service('StudyAnnotTypeService', StudyAnnotTypeService);

  StudyAnnotTypeService.$inject = ['biobankXhrReqService', 'modelObjService'];

  /**
   * Service to access study annotation types.
   */
  function StudyAnnotTypeService(biobankXhrReqService, modelObjService) {
    var services = {
      getAll      : getAll,
      get         : get,
      addOrUpdate : addOrUpdate,
      remove      : remove,
      valueTypes  : valueTypes
    };
    return services;

    //-------

    function getAll(baseUrl, studyId) {
      return biobankXhrReqService.call('GET', baseUrl + '/' + studyId);
    }

    function get(baseUrl, studyId, annotTypeId) {
      return biobankXhrReqService.call('GET', baseUrl + '/'  + studyId + '?annotTypeId=' + annotTypeId);
    }

    function addOrUpdate(baseUrl, annotType) {
      var cmd = {
        studyId:       annotType.studyId,
        name:          annotType.name,
        valueType:     annotType.valueType,
        maxValueCount: annotType.maxValueCount,
        options:       annotType.options
      };

      modelObjService.setDescription(cmd, annotType.description);

      if (typeof annotType.required !== 'undefined') {
        cmd.required = annotType.required;
      }

      if (annotType.id) {
        cmd.id = annotType.id;
        cmd.expectedVersion = annotType.version;
        return biobankXhrReqService.call('PUT', baseUrl + '/'  + annotType.id, cmd);
      } else {
        return biobankXhrReqService.call('POST', baseUrl, cmd);
      }
    }

    function remove(baseUrl, annotType) {
      return biobankXhrReqService.call(
        'DELETE',
        baseUrl + '/' + annotType.studyId + '/' + annotType.id + '/' + annotType.version);
    }

    function valueTypes() {
      return biobankXhrReqService.call('GET', '/studies/valuetypes');
    }


  }

});
