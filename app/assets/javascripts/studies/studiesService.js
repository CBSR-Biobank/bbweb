/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'jquery', 'underscore'], function(angular, $, _) {
  'use strict';

  studiesServiceFactory.$inject = ['biobankApi', 'funutils', 'queryStringService'];

  /**
   * Service to acccess studies.
   */
  function studiesServiceFactory(biobankApi, funutils, queryStringService) {
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

    /**
     * @deprecated use StudyCounts.
     */
    function getStudyCounts() {
      return biobankApi.get(uri() + '/counts');
    }

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

      if (arguments.length > 0) {
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

      angular.extend(cmd, funutils.pickOptional(study, 'description'));

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

    function valueTypes() {
      return biobankApi.get('/studies/valuetypes');
    }
  }

  return studiesServiceFactory;
});
