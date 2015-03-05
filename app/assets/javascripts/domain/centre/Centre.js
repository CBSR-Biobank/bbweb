define(['../module', 'angular', 'underscore'], function(module, angular, _) {
  'use strict';

  module.factory('Centre', CentreFactory);

  CentreFactory.$inject = [
    'validationService',
    'ConcurrencySafeEntity',
    'CentreStatus',
    'Location',
    'centresService',
    'centreLocationsService'
  ];

  /**  *
   */
  function CentreFactory(validationService,
                         ConcurrencySafeEntity,
                         CentreStatus,
                         Location,
                         centresService,
                         centreLocationsService) {

    var addedEventKeys = ['id', 'name'];

    var updatedEventKeys = addedEventKeys.concat('version');

    var checkObject = validationService.checker(
      validationService.aMapValidator,
      validationService.hasKeys('id', 'version', 'timeAdded', 'name', 'status')
    );

    var checkAddedEvent = validationService.checker(
      validationService.aMapValidator,
      validationService.hasKeys.apply(null, addedEventKeys));

    var checkUpdatedEvent = validationService.checker(
      validationService.aMapValidator,
      validationService.hasKeys.apply(null, updatedEventKeys));

    function createCentre(obj) {
      var checks = checkObject(obj);
      if (checks.length) {
        throw new Error('invalid object from server: ' + checks.join(', '));
      }
      return new Centre(obj);
    }

    /**
     * Centre is a value object.
     */
    function Centre(obj) {
      obj =  obj || {};

      ConcurrencySafeEntity.call(this, obj);

      _.extend(this, _.defaults(obj, {
        name:        '',
        description: null,
        status:      CentreStatus.DISABLED(),
        locations :  [],
        studyIds:    []
      }));
    }

    Centre.prototype = Object.create(ConcurrencySafeEntity.prototype);

    Centre.list = function (options) {
      options = options || {};
      return centresService.list(options).then(function(reply) {
        // reply is a paged result
        reply.items = _.map(reply.items, function(obj){
          return createCentre(obj);
        });
        return reply;
      });
    };

    Centre.get = function (id) {
      return centresService.get(id).then(function(reply) {
        return createCentre(reply);
      });
    };

    Centre.prototype.addOrUpdate = function () {
      var self = this;
      return centresService.addOrUpdate(self).then(function(reply) {
        var checker = this.isNew() ? checkAddedEvent : checkUpdatedEvent;
        var checks = checker();

        if (checks.length) {
          throw new Error('invalid event from server: ' + checks.join(', '));
        }

        _.extend(self, _.defaults(reply, { version: 0, description: null }));
        return self;
      });
    };

    Centre.prototype.disable = function () {
      var self = this;
      return centresService.disable(self).then(function(reply) {
        self.status = CentreStatus.DISABLED();
        self.version = reply.version;
        return self;
      });
    };

    Centre.prototype.enable = function () {
      var self = this;
      return centresService.enable(self).then(function(reply) {
        self.status = CentreStatus.ENABLED();
        self.version = reply.version;
        return self;
      });
    };

    Centre.prototype.getLocations = function () {
      var self = this;
      if (self.id === null) {
        throw new Error('id is null');
      }
      return centreLocationsService.list(self.id).then(function(reply){
        var locs = _.map(reply, function(loc) {
          return Location.create(loc);
        });
        self.locations = _.union(self.locations, locs);
        return self;
      });
    };

    Centre.prototype.getLocation = function (locationId) {
      var self = this;
      if (self.id === null) {
        throw new Error('id is null');
      }
      return centreLocationsService.query(self.id, locationId).then(function(reply){
        self.locations.push(Location.create(reply));
        return self;
      });
    };

    Centre.prototype.addLocation = function (location) {
      var self = this, existingLoc;

      if (self.id === null) {
        throw new Error('id is null');
      }

      existingLoc = _.findWhere(self.locations, { id: location.id });
      if (existingLoc) {
        // remove this location first
        return self.removeLocation(location).then(addInternal);
      } else {
        return addInternal();
      }

      function addInternal() {
        return centreLocationsService.add(self, location).then(function(event) {
          self.locations.push(Location.create(event));
          return self;
        });
      }
    };

    Centre.prototype.removeLocation = function (location) {
      var self = this, existingLoc;

      if (self.id === null) {
        throw new Error('id is null');
      }

      existingLoc = _.findWhere(self.locations, { id: location.id });
      if (_.isUndefined(existingLoc)) {
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
