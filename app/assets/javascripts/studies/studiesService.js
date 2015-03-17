define(['angular', 'jquery', 'underscore'], function(angular, $, _) {
  'use strict';

  studiesServiceFactory.$inject = ['biobankApi', 'domainEntityService', 'queryStringService'];

  /**
   * Service to acccess studies.
   */
  function studiesServiceFactory(biobankApi, domainEntityService, queryStringService) {
    var service = {
      list:           list,
      getStudyCounts: getStudyCounts,
      getStudyNames:  getStudyNames,
      get:            get,
      addOrUpdate:    addOrUpdate,
      enable:         enable,
      disable:        disable,
      retire:         retire,
      unretire:       unretire,
      collectionDto:  collectionDto,
      processingDto:  processingDto,
      valueTypes:     valueTypes
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
      return biobankApi.post(uri(study.id) + '/' + status, cmd);
    }

    function getStudyCounts() {
      return biobankApi.get(uri() + '/counts');
    }

    /**
     * @param {string} options.filter The filter to use on study names. Default is empty string.
     *
     * @param {string} options.status Returns studies filtered by status. The following are valid: 'all' to
     * return all studies, 'disabled' to return only disabled studies, 'enabled' to reutrn only enable
     * studies, and 'retired' to return only retired studies. For any other values the response is an error.
     *
     * @param {string} options.sortField Studies can be sorted by 'name' or by 'status'. Values other than
     * these two yield an error.
     *
     * @param {int} options.page If the total results are longer than pageSize, then page selects which
     * studies should be returned. If an invalid value is used then the response is an error.
     *
     * @param {int} options.pageSize The total number of studies to return per page. The maximum page size is
     * 10. If a value larger than 10 is used then the response is an error.
     *
     * @param {string} options.order One of 'asc' or 'desc'. If an invalid value is used then
     * the response is an error.
     *
     * @return A promise. If the promise succeeds then a paged result is returned.
     */
    function list(options) {
      var validKeys = [
        'filter',
        'status',
        'sort',
        'page',
        'pageSize',
        'order'
      ];
      var url = uri();
      var paramsStr = '';

      if (arguments.length) {
        paramsStr = queryStringService.param(options, function (value, key) {
          return _.contains(validKeys, key);
        });
      }

      if (paramsStr) {
        url += paramsStr;
      }

      return biobankApi.get(url);
    }

    function getStudyNames(options) {
      var validKeys = ['filter', 'order'];
      var url = uri();
      var paramsStr = '';

      if (arguments.length) {
        paramsStr = queryStringService.param(options, function (value, key) {
          return _.contains(validKeys, key);
        });
      }

      if (paramsStr) {
        url += paramsStr;
      }

      return biobankApi.get(url + '/names');
    }

    function get(id) {
      return biobankApi.get(uri(id));
    }

    function addOrUpdate(study) {
      var cmd = {name: study.name };

      angular.extend(cmd, domainEntityService.getOptionalAttribute(study, 'description'));

      if (study.id) {
        cmd.id = study.id;
        cmd.expectedVersion = study.version;

        return biobankApi.put(uri(study.id), cmd);
      } else {
        return biobankApi.post(uri(), cmd);
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
      return biobankApi.get(uri(studyId) + '/dto/collection');
    }

    function processingDto(studyId) {
      return biobankApi.get(uri(studyId) + '/dto/processing');
    }

    function valueTypes() {
      return biobankApi.get('/studies/valuetypes');
    }
  }

  return studiesServiceFactory;
});
