define(['./module', 'angular'], function(module, angular) {
  'use strict';

  module.service('centresService', centresService);

  centresService.$inject = ['biobankXhrReqService', 'domainEntityService'];

  /**
   *
   */
  function centresService(biobankXhrReqService, domainEntityService) {
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

    function uri(centreId) {
      var result = '/centres';
      if (arguments.length > 0) {
        result += '/' + centreId;
      }
      return result;
    }

    function changeStatus(status, centre) {
      var cmd = { id: centre.id, expectedVersion: centre.version };
      return biobankXhrReqService.call('POST', uri(centre.id) + '/' + status, cmd);
    }

    function list() {
      return biobankXhrReqService.call('GET', uri());
    }

    function query(id) {
      return biobankXhrReqService.call('GET', uri(id));
    }

    function addOrUpdate(centre) {
      var cmd = {name: centre.name};

      angular.extend(cmd, domainEntityService.getOptionalAttribute(centre, 'description'));

      if (centre.id) {
        cmd.id = centre.id;
        cmd.expectedVersion = centre.version;

        return biobankXhrReqService.call('PUT', uri(centre.id), cmd);
      } else {
        return biobankXhrReqService.call('POST', uri(), cmd);
      }
    }

    function enable(centre) {
      return changeStatus('enable', centre);
    }

    function disable(centre) {
      return changeStatus('disable', centre);
    }

    function studies(centreId) {
      return biobankXhrReqService.call('GET', uri(centreId) + '/studies');
    }

    function addStudy(centreId, studyId) {
      var cmd = {centreId: centreId, studyId: studyId};
      return biobankXhrReqService.call('POST', uri(centreId) + '/study', cmd);
    }

    function removeStudy(centreId, studyId) {
      return biobankXhrReqService.call('DELETE', uri(centreId) + '/study/' + studyId);
    }
  }

});
