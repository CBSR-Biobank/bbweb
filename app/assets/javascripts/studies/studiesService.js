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
      getStudyCount : getStudyCount,
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

    /**
     * @param {Study} study the study to change the status on.
     *
     * @param {string} status One of 'disable', 'enable', 'retire', or 'unretire'. If an invalid
     * value is used then the response is an error.
     *
     * @return A promise.
     *
     */
    function changeStatus(study, status) {
      var cmd = {
        id: study.id,
        expectedVersion: study.version
      };
      return biobankXhrReqService.call('POST', uri(study.id) + '/' + status, cmd);
    }

    function getStudyCount() {
      return biobankXhrReqService.call('GET', uri() + '/count');
    }

    /**
     * @param {string} nameFilter Returns the studies that have a name that match this filter.
     *
     * @param {string} status Returns studies that have a matching status. For all studies use
     * 'all'. If a value larger than 10 is used then the response is an error.
     *
     * @param {int} page If the total results are longer than pageSize, then page selects which
     * studies should be returned. If an invalid value is used then the response is an error.
     *
     * @param {int} pageSize The total number of studies to return per page. If a value larger than
     * 10 is used then the response is an error.
     *
     * @param {string} sortField Studies can be sorted by 'name' or by 'status'. If an invalid value
     * is used then the response is an error.
     *
     * @param {string} order One of 'asc' or 'desc'. If an invalid value is used then
     * the response is an error.
     *
     * @return A promise.
     */
    function getStudies(nameFilter, status, page, pageSize, sortField, order) {
      var params = {};

      if (nameFilter) { params.filter = nameFilter; }
      if (status)     { params.status = status; }
      if (page)       { params.page = page; }
      if (pageSize)   { params.pageSize = pageSize; }
      if (sortField)  { params.sort = sortField; }

      if (order) {
        if (order === 'asc') {
          order = 'ascending';
        } else if (order === 'desc') {
          order = 'descending';
        }
        params.order = order;
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
