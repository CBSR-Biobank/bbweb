define(['./module', 'angular'], function(module, angular) {
  'use strict';

  module.factory('ConcurrencySafeEntity', ConcurrencySafeEntityFactory);

  //ConcurrencySafeEntityFactory.$inject = [];

  /**
   *
   */
  function ConcurrencySafeEntityFactory() {

    function ConcurrencySafeEntity(obj) {
      obj = obj || {};

      this.id           = obj.id || null;
      this.version      = obj.version || 0;
      this.timeAdded    = obj.timeAdded || null;
      this.timeModified = obj.timeModified || null;
    }

    ConcurrencySafeEntity.prototype.isNew = function () {
      return (this.id === null);
    };

    return ConcurrencySafeEntity;
  }

});
