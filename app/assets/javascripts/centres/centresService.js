define(['./module'], function(module) {
  'use strict';

  module.service('centresService', centresService);

  centresService.$inject = ['biobankXhrReqService'];

  /**
   *
   */
  function centresService(biobankXhrReqService) {
    var service = {
      list:        list,
      query:       query,
      addOrUpdate: addOrUpdate,
      enable:      enable,
      disable:     disable,
      studies:     studies,
      addStudy:    addStudy,
      removeStudy: removeStudy
    };
    return service;

    //----

    function changeStatus(status, centre) {
      var cmd = { id: centre.id, expectedVersion: centre.version };
      return biobankXhrReqService.call('POST', '/centres/' + status, cmd);
    }

    function list() {
      return biobankXhrReqService.call('GET','/centres');
    }

    function query(id) {
      return biobankXhrReqService.call('GET','/centres/' + id);
    }

    function addOrUpdate(centre) {
      var cmd = {
        name: centre.name,
        description: centre.description
      };

      if (centre.id) {
        cmd.id = centre.id;
        cmd.expectedVersion = centre.version;

        return biobankXhrReqService.call('PUT', '/centres/' + centre.id, cmd);
      } else {
        return biobankXhrReqService.call('POST', '/centres', cmd);
      }
    }

    function enable(centre) {
      return changeStatus('enabled', centre);
    }

    function disable(centre) {
      return changeStatus('disabled', centre);
    }

    function studies(centreId) {
      return biobankXhrReqService.call('GET','/centres/centre/' + centreId + '/studies');
    }

    function addStudy(centreId, studyId) {
      var cmd = {centreId: centreId, studyId: studyId};
      return biobankXhrReqService.call('POST','/centres/centre/study', cmd);
    }

    function removeStudy(centreId, studyId) {
      return biobankXhrReqService.call('DELETE','/centres/centre/' + centreId + '/study/' + studyId);
    }
  }

});
