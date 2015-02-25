define(['../module', 'angular'], function(module, angular) {
  'use strict';

  module.factory('Centre', CentreFactory);

  CentreFactory.$inject = ['ConcurrencySafeEntity'];

  /**  *
   */
  function CentreFactory(ConcurrencySafeEntity) {

    /**
     * Centre is a value object.
     */
    function Centre(obj) {
      obj =  obj || {};

      ConcurrencySafeEntity.call(this, obj);

      this.name        = obj.name || '';
      this.description = obj.description || null;
      this.status      = obj.status || 'Disabled';
      this.locations   = obj.locations || [];
      this.studyIds    = obj.studyIds || [];
    }

    Centre.prototype = Object.create(ConcurrencySafeEntity.prototype);

    Centre.prototype.addLocations = function (locations) {
      this.locations = _.union(this.locations, locations);
    };

    Centre.prototype.removeLocations = function (locations) {
      this.locations = _.difference(this.locations, locations);
    };

    Centre.prototype.addStudyIds = function (studyIds) {
      this.studyIds = _.union(this.studyIds, studyIds);
    };

    Centre.prototype.removeStudyIds = function (studyIds) {
      this.studyIds = _.difference(this.studyIds, studyIds);
    };

    Centre.prototype.getAddCommand = function () {
      var cmd = _.pick(this, ['name']);
      if (this.description) {
        cmd.description = this.description;
      }
      return cmd;
    };

    Centre.prototype.getUpdateCommand = function () {
      return _.extend(this.getAddCommand(), {
        id:              this.id,
        expectedVersion: this.version
      });
    };

    return Centre;
  }

});
