/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global define */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  StudyFactory.$inject = [
    'funutils',
    'validationService',
    'ConcurrencySafeEntity',
    'StudyStatus',
    'studiesService'
  ];

  /**
   *
   */
  function StudyFactory(funutils,
                        validationService,
                        ConcurrencySafeEntity,
                        StudyStatus,
                        studiesService) {

    var requiredKeys = ['id', 'version', 'timeAdded', 'name', 'status'];

    var validateObj = funutils.partial(
      validationService.condition1(
        validationService.validator('must be a map', _.isObject),
        validationService.validator('has the correct keys',
                                    validationService.hasKeys.apply(null, requiredKeys))),
      _.identity);

    /**
     *
     */
    function Study(obj) {
      var defaults = {
        name:        '',
        description: null,
        status:      StudyStatus.DISABLED()
      };

      obj =  obj || {};
      ConcurrencySafeEntity.call(this, obj);
      _.extend(this, defaults, _.pick(obj, _.keys(defaults)));
    }

    Study.prototype = Object.create(ConcurrencySafeEntity.prototype);

    /**
     * Used by promise code, so it must return an error rather than throw one.
     */
    Study.create = function (obj) {
      var validation = validateObj(obj);
      if (!_.isObject(validation)) {
        return new Error('invalid object from server: ' + validation);
      }
      return new Study(obj);
    };

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
    Study.list = function (options) {
      options = options || {};
      return studiesService.list(options).then(function(reply) {
        // reply is a paged result
        reply.items = _.map(reply.items, function(obj){
          return Study.create(obj);
        });
        return reply;
      });
    };

    Study.get = function (id) {
      return studiesService.get(id).then(function(reply) {
        return Study.create(reply);
      });
    };

    Study.prototype.addOrUpdate = function () {
      var self = this;
      return studiesService.addOrUpdate(self).then(function(reply) {
        return Study.create(reply);
      });
    };

    Study.prototype.disable = function () {
      return changeState(this, 'disable');
    };

    Study.prototype.enable = function () {
      return changeState(this, 'enable');
    };

    Study.prototype.retire = function () {
      return changeState(this, 'retire');
    };

    Study.prototype.unretire = function () {
      return changeState(this, 'unretire');
    };

    Study.prototype.isDisabled = function () {
      return (this.status === StudyStatus.DISABLED());
    };

    Study.prototype.isEnabled = function () {
      return (this.status === StudyStatus.ENABLED());
    };

    Study.prototype.isRetired = function () {
      return (this.status === StudyStatus.Retired());
    };

    function changeState(obj, method) {
      return studiesService[method](obj).then(function(reply) {
        return new Study.create(reply);
      });
    }

    return Study;
  }

  return StudyFactory;
});

/* Local Variables:  */
/* mode: js          */
/* End:              */

