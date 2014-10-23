define(['./module', 'angular'], function(module, angular) {
  'use strict';

  module.service('studiesService', StudiesService);

  StudiesService.$inject = ['biobankXhrReqService', 'domainEntityService'];

  /**
   * Service to acccess studies.
   */
  function StudiesService(biobankXhrReqService, domainEntityService) {
    var service = {
      getAll        : getAll,
      get           : get,
      addOrUpdate   : addOrUpdate,
      enable        : enable,
      disable       : disable,
      retire        : retire,
      unretire      : unretire,
      processingDto : processingDto
    };
    return service;

    //-------

    function uri(studyId) {
      var result = '/studies';
      if (arguments.length > 0) {
        result += '/' + studyId;
      }
      return result;
    }

    function changeStatus(study, status) {
      var cmd = {
        id: study.id,
        expectedVersion: study.version
      };
      return biobankXhrReqService.call('POST', uri(study.id) + '/' + status, cmd);
    }

    function getAll() {
      return biobankXhrReqService.call('GET', uri());
    }

    function get(id) {
      return biobankXhrReqService.call('GET', uri(id));
    }

    function addOrUpdate(study) {
      var cmd = {name: study.name };

      angular.extend(cmd, domainEntityService.getOptionalAttribute(study, 'description'));

      if (study.id) {
        cmd.id = study.id;
        cmd.expectedVersion = study.version;

        return biobankXhrReqService.call('PUT', uri(study.id), cmd);
      } else {
        return biobankXhrReqService.call('POST', uri(), cmd);
      }
    }

    function enable(study) {
      return changeStatus(study, 'enable');
    }

    function disable(study) {
      return changeStatus(study, 'disable');
    }

    function retire(study) {
      return changeStatus(study, 'retire');
    }

    function unretire(study) {
      return changeStatus(study, 'unretire');
    }

    function processingDto(studyId) {
      return biobankXhrReqService.call('GET', uri(studyId) + '/dto/processing');
    }
  }

});
