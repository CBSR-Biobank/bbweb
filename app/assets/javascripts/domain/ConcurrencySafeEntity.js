define(['angular', 'underscore'], function(angular, _) {
  'use strict';

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

  return ConcurrencySafeEntityFactory;
});
