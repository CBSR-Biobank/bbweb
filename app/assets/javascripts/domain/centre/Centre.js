define(['../module', 'angular', 'underscore'], function(module, angular, _) {
  'use strict';

  module.factory('Centre', CentreFactory);

  CentreFactory.$inject = [
    'ConcurrencySafeEntity',
    'Location',
    'centresService',
    'centreLocationsService'
  ];

  /**  *
   */
  function CentreFactory(ConcurrencySafeEntity,
                         Location,
                         centresService,
                         centreLocationsService) {

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

    Centre.prototype.getLocations = function () {
      if (this.id === null) {
        throw new Error('id is null');
      }
      return centreLocationsService.list(this.id).then(function(reply){
        var locs = _.map(reply, function(loc) {
          return new Location(loc);
        });
        this.locations = _.union(this.locations, locs);
        return this;
      });
    };

    Centre.prototype.addLocation = function (location) {
      if (this.id === null) {
        throw new Error('id is null');
      }
      if (this.locations.contains(location)) {
        throw new Error('location already present: ' + location);
      }
      return centreLocationsService.add(this, location).then(function(reply) {
        this.locations.push(location);
        return this;
      });
    };

    Centre.prototype.removeLocation = function (location) {
      if (this.id === null) {
        throw new Error('id is null');
      }
      if (! this.studyIds.contains(location)) {
        throw new Error('location not present: ' + location);
      }
      return centreLocationsService.add(this, location).then(function(reply) {
        this.locations = _.without(this.locations, location);
        return this;
      });
    };

    Centre.prototype.getStudyIds = function() {
      if (this.id === null) {
        throw new Error('id is null');
      }
      return centresService.studies(this).then(function(reply) {
        this.studyIds = _.union(this.studyIds, reply);
        return this;
      });
    };

    Centre.prototype.addStudyId = function (studyId) {
      if (this.id === null) {
        throw new Error('id is null');
      }
      if (this.studyIds.contains(studyId)) {
        throw new Error('study ID already present: ' + studyId);
      }
      return centresService.addStudy(this, studyId).then(function(reply) {
        this.studyIds.push(studyId);
        return this;
      });
    };

    Centre.prototype.removeStudyId = function (studyId) {
      if (this.id === null) {
        throw new Error('id is null');
      }
      if (! this.studyIds.contains(studyId)) {
        throw new Error('study ID not present: ' + studyId);
      }
      return centresService.removeStudy(this, studyId).then(function(reply) {
        this.studyIds = _.without(this.studyIds, studyId);
        return this;
      });
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

    Centre.getCentres = function (options) {
      return centresService.getCentres(options).then(function(reply) {
        return _.map(reply, function(obj){
          return new Centre(obj);
        });
      });
    };

    Centre.getCentre = function (id) {
      return centresService.get(id).then(function(reply) {
        return new Centre(reply);
      });
    };

    return Centre;
  }

});
