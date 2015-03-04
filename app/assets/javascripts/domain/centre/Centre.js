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

    Centre.list = function (options) {
      options = options || {};
      return centresService.list(options).then(function(reply) {
        return _.map(reply, function(obj){
          return new Centre(obj);
        });
      });
    };

    Centre.get = function (id) {
      return centresService.get(id).then(function(reply) {
        return new Centre(reply);
      });
    };

    Centre.prototype.getLocations = function () {
      var self = this;
      if (self.id === null) {
        throw new Error('id is null');
      }
      return centreLocationsService.list(self.id).then(function(reply){
        var locs = _.map(reply, function(loc) {
          return new Location(loc);
        });
        self.locations = _.union(self.locations, locs);
        return self;
      });
    };

    Centre.prototype.addLocation = function (location) {
      var self = this;
      if (self.id === null) {
        throw new Error('id is null');
      }
      if (_.contains(self.locations, location)) {
        throw new Error('location already present: ' + location.id);
      }
      return centreLocationsService.add(this, location).then(function(reply) {
        self.locations.push(new Location(location));
        return this;
      });
    };

    Centre.prototype.removeLocation = function (location) {
      var self = this;
      if (self.id === null) {
        throw new Error('id is null');
      }
      if (!_.contains(self.locations, location)) {
        throw new Error('location not present: ' + location.id);
      }
      return centreLocationsService.remove(this, location).then(function(reply) {
        self.locations = _.without(self.locations, location);
        return self;
      });
    };

    Centre.prototype.getStudyIds = function() {
      var self = this;
      if (self.id === null) {
        throw new Error('id is null');
      }
      return centresService.studies(this).then(function(reply) {
        self.studyIds = _.union(self.studyIds, reply);
        return self;
      });
    };

    Centre.prototype.addStudy = function (study) {
      var self = this;
      if (self.id === null) {
        throw new Error('id is null');
      }
      if (_.contains(self.studyIds, study.id)) {
        throw new Error('study ID already present: ' + study.id);
      }
      return centresService.addStudy(this, study.id).then(function(reply) {
        self.studyIds.push(study.id);
        return self;
      });
    };

    Centre.prototype.removeStudy = function (study) {
      var self = this;
      if (self.id === null) {
        throw new Error('id is null');
      }
      if (! _.contains(self.studyIds, study.id)) {
        throw new Error('study ID not present: ' + study.id);
      }
      return centresService.removeStudy(this, study.id).then(function(reply) {
        self.studyIds = _.without(self.studyIds, study.id);
        return self;
      });
    };

    return Centre;
  }

});
