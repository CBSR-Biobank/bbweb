/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  ConcurrencySafeEntityFactory.$inject = [ '$q', 'biobankApi' ];

  /**
   *
   */
  function ConcurrencySafeEntityFactory($q, biobankApi) {

    function ConcurrencySafeEntity(obj) {
      var defaults = {
        id:           null,
        version:      0,
        timeAdded:    null,
        timeModified: null
      };

      obj = obj || {};
      _.extend(this, defaults, _.pick(obj, _.keys(defaults)));
    }

    ConcurrencySafeEntity.prototype.isNew = function() {
      return (this.id === null);
    };

    ConcurrencySafeEntity.prototype.update = function (url, additionalJson) {
      var self = this, json = _.extend({ expectedVersion: self.version }, additionalJson || {});
      return biobankApi.post(url, json).then(function(reply) {
        return self.asyncCreate(reply);
      });
    };

    ConcurrencySafeEntity.prototype.asyncCreate = function (obj) {
      var deferred = $q.defer();
      deferred.reject('derived class should override this method');
      return deferred.promise;
    };

    return ConcurrencySafeEntity;
  }

  return ConcurrencySafeEntityFactory;
});
