define(['./module', 'angular', 'underscore'], function(module, angular, _) {
  'use strict';

  module.factory('ConcurrencySafeEntity', ConcurrencySafeEntityFactory);

  //ConcurrencySafeEntityFactory.$inject = [];

  /**
   *
   */
  function ConcurrencySafeEntityFactory() {

    function ConcurrencySafeEntity(obj) {
      obj = obj || {};

      _.extend(this, _.defaults(obj, {
        id: null,
        version: 0,
        timeAdded: null,
        timeModified: null
      }));
    }

    ConcurrencySafeEntity.prototype.isNew = function () {
      return (this.id === null);
    };

    return ConcurrencySafeEntity;
  }

});
