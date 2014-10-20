define(['./module'], function(module) {
  'use strict';

  module.service('StudyService', StudyService);

  StudyService.$inject = ['biobankXhrReqService', 'domainEntityService'];

  /**
   * Service to acccess studies.
   */
  function StudyService(biobankXhrReqService, domainEntityService) {
    var service = {
      list          : list,
      query         : query,
      addOrUpdate   : addOrUpdate,
      enable        : enable,
      disable       : disable,
      retire        : retire,
      unretire      : unretire,
      processingDto : processingDto
    };
    return service;

    //-------
    function changeStatus(study, status) {
      var cmd = {
        id: study.id,
        expectedVersion: study.version
      };
      return biobankXhrReqService.call('POST', '/studies/' + status, cmd);
    }

    function list() {
      return biobankXhrReqService.call('GET', '/studies');
    }

    function query(id) {
      return biobankXhrReqService.call('GET', '/studies/' + id);
    }

    function addOrUpdate(study) {
      var cmd = {
        name: study.name
      };

      domainEntityService.getOptionalAttribute(cmd, study.description);

      if (study.id) {
        cmd.id = study.id;
        cmd.expectedVersion = study.version;

        return biobankXhrReqService.call('PUT', '/studies/' + study.id, cmd);
      } else {
        return biobankXhrReqService.call('POST', '/studies', cmd);
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
      return biobankXhrReqService.call('GET', '/studies/dto/processing/' + studyId);
    }
  }

});
