/* global define */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  CentreFactory.$inject = [
    'funutils',
    'validationService',
    'ConcurrencySafeEntity',
    'CentreStatus',
    'Location',
    'centresService',
    'centreLocationsService'
  ];

  /**  *
   */
  function CentreFactory(funutils,
                         validationService,
                         ConcurrencySafeEntity,
                         CentreStatus,
                         Location,
                         centresService,
                         centreLocationsService) {

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
    function Centre(obj) {
      var defaults = {
        name:        '',
        description: null,
        status:      CentreStatus.DISABLED(),
        locations:   [],
        studyIds:    []
      };

      ConcurrencySafeEntity.call(this, obj);
      obj =  obj || {};
      _.extend(this, defaults, _.pick(obj, _.keys(defaults)));
    }

    Centre.prototype = Object.create(ConcurrencySafeEntity.prototype);

    /**
     * Used by promise code, so it must return an error rather than throw one.
     */
    Centre.create = function (obj) {
      var validation = validateObj(obj);
      if (!_.isObject(validation)) {
        return new Error('invalid object from server: ' + validation);
      }
      return new Centre(obj);
    };

    Centre.list = function (options) {
      options = options || {};
      return centresService.list(options).then(function(reply) {
        // reply is a paged result
        reply.items = _.map(reply.items, function(obj){
          return Centre.create(obj);
        });
        return reply;
      });
    };

    Centre.get = function (id) {
      return centresService.get(id).then(function(reply) {
        return Centre.create(reply);
      });
    };

    Centre.prototype.addOrUpdate = function () {
      var self = this;
      return centresService.addOrUpdate(self).then(function(reply) {
        return new Centre.create(reply);
      });
    };

    Centre.prototype.isDisabled = function () {
      return this.status === CentreStatus.DISABLED();
    };

    Centre.prototype.isEnabled = function () {
      return this.status === CentreStatus.ENABLED();
    };

    Centre.prototype.disable = function () {
      return changeState(this, 'disable');
    };

    Centre.prototype.enable = function () {
      return changeState(this, 'enable');
    };

    Centre.prototype.getLocations = function () {
      var self = this;
      if (self.id === null) {
        throw new Error('id is null');
      }
      return centreLocationsService.list(self.id).then(function(reply){
        self.locations = _.map(reply, function(loc) {
          return Location.create(loc);
        });
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
      }
      return addInternal();

      function addInternal() {
        return centreLocationsService.add(self, location).then(function(event) {
          var newLoc = Location.create(_.extend(funutils.renameKeys(event, { locationId: 'id' }), location));
          self.locations.push(newLoc);
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

    function changeState(obj, method) {
      return centresService[method](obj).then(function(reply) {
        return new Centre.create(reply);
      });
    }

    return Centre;
  }

  return CentreFactory;
});

/* Local Variables:  */
/* mode: js          */
/* End:              */

