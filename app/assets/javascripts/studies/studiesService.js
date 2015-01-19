define(['./module', 'angular', 'jquery'], function(module, angular, $) {
  'use strict';

  module.service('studiesService', StudiesService);

  StudiesService.$inject = ['biobankXhrReqService', 'domainEntityService'];

  /**
   * Service to acccess studies.
   */
  function StudiesService(biobankXhrReqService, domainEntityService) {
    var service = {
      getStudies    : getStudies,
      get           : get,
      addOrUpdate   : addOrUpdate,
      enable        : enable,
      disable       : disable,
      retire        : retire,
      unretire      : unretire,
      collectionDto : collectionDto,
      processingDto : processingDto,
      valueTypes    : valueTypes
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

    function getStudies(nameFilter, status, page, pageSize, sortField, order) {
      var params = {};

      if (nameFilter) {
        params.filter = nameFilter;
      }

      if (status) {
        params.status = status;
      }
      if (page) {
        params.page = page;
      }
      if (pageSize) {
        params.pageSize = pageSize;
      }
      if (sortField) {
        params.sort = sortField;
      }
      if (order) {
        params.order = (order === 'asc') ? 'ascending' : 'descending';
      }

      var paramsStr = $.param(params);
      var url = uri();

      if (paramsStr !== '') {
        url += '?' + paramsStr;
      }

      return biobankXhrReqService.call('GET', url);
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

    function collectionDto(studyId) {
      return biobankXhrReqService.call('GET', uri(studyId) + '/dto/collection');
    }

    function processingDto(studyId) {
      return biobankXhrReqService.call('GET', uri(studyId) + '/dto/processing');
    }

    function valueTypes() {
      return biobankXhrReqService.call('GET', '/studies/valuetypes');
    }
  }

});
