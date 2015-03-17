define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  centresService.$inject = [
    'biobankApi',
    'queryStringService'
  ];

  /**
   *
   */
  function centresService(biobankApi,
                          queryStringService) {
    var service = {
      list:            list,
      getCentreCounts: getCentreCounts,
      get:             get,
      addOrUpdate:     addOrUpdate,
      enable:          enable,
      disable:         disable,
      studies:         studies,
      addStudy:        addStudy,
      removeStudy:     removeStudy
    };
    return service;

    //----

    function getAddCommand(centre) {
      var cmd = _.pick(centre, ['name']);
      if (centre.description) {
        cmd.description = centre.description;
      }
      return cmd;
    }

    function getUpdateCommand (centre) {
      return _.extend(getAddCommand(centre), {
        id:              centre.id,
        expectedVersion: centre.version
      });
    }

    function uri(centreId) {
      var result = '/centres';
      if (arguments.length > 0) {
        result += '/' + centreId;
      }
      return result;
    }

    function changeStatus(status, centre) {
      var cmd = { id: centre.id, expectedVersion: centre.version };
      return biobankApi.post(uri(centre.id) + '/' + status, cmd);
    }

    function getCentreCounts() {
      return biobankApi.get(uri() + '/counts');
    }

    /**
     * @param {string} options.filter The filter to use on centre names. Default is empty string.
     *
     * @param {string} options.status Returns centres filtered by status. The following are valid: 'all' to
     * return all centres, 'disabled' to return only disabled centres, 'enabled' to reutrn only enable
     * centres, and 'retired' to return only retired centres. For any other values the response is an error.
     *
     * @param {string} options.sortField Centres can be sorted by 'name' or by 'status'. Values other than
     * these two yield an error.
     *
     * @param {int} options.page If the total results are longer than pageSize, then page selects which
     * centres should be returned. If an invalid value is used then the response is an error.
     *
     * @param {int} options.pageSize The total number of centres to return per page. The maximum page size is
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

      options = options || {};

      paramsStr = queryStringService.param(options, function (value, key) {
        return _.contains(validKeys, key);
      });

      if (paramsStr.length) {
        url += paramsStr;
      }

      return biobankApi.get(url);
    }

    function get(id) {
      return biobankApi.get(uri(id));
    }

    function addOrUpdate(centre) {
      if (centre.isNew()) {
        return biobankApi.post(uri(), getAddCommand(centre));
      } else {
        return biobankApi.put(uri(centre.id), getUpdateCommand(centre));
      }
    }

    function enable(centre) {
      return changeStatus('enable', centre);
    }

    function disable(centre) {
      return changeStatus('disable', centre);
    }

    function studies(centre) {
      return biobankApi.get(uri(centre.id) + '/studies');
    }

    function addStudy(centre, studyId) {
      var cmd = {centreId: centre.id, studyId: studyId};
      return biobankApi.post(uri(centre.id) + '/studies/' + studyId, cmd);
    }

    function removeStudy(centre, studyId) {
      return biobankApi.del(uri(centre.id) + '/studies/' + studyId);
    }
  }

  return centresService;
});
