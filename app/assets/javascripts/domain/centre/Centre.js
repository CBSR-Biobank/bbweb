/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'underscore', 'tv4', 'sprintf'], function(angular, _, tv4, sprintf) {
  'use strict';

  CentreFactory.$inject = [
    '$q',
    'funutils',
    'biobankApi',
    'ConcurrencySafeEntity',
    'CentreStatus',
    'Location',
    'queryStringService'
  ];

  /**  *
   */
  function CentreFactory($q,
                         funutils,
                         biobankApi,
                         ConcurrencySafeEntity,
                         CentreStatus,
                         Location,
                         queryStringService) {

    var schema = {
      'id': 'Centre',
      'type': 'object',
      'properties': {
        'id':           { 'type': 'string'},
        'version':      { 'type': 'integer', 'minimum': 0},
        'timeAdded':    { 'type': 'string'},
        'timeModified': { 'type': [ 'string', 'null' ] },
        'name':         { 'type': 'string'},
        'description':  { 'type': [ 'string', 'null' ] },
        'studyIds':     { 'type': 'array'},
        'locations':    { 'type': 'array'},
        'status':       { 'type': 'string'}
      },
      'required': [ 'id', 'version', 'timeAdded', 'name', 'status' ]
    };

    /**
     *
     */
    function Centre(obj) {
      var defaults = {
        name:        '',
        description: null,
        status:      CentreStatus.DISABLED(),
        studyIds:    [],
        locations:   []
      };

      ConcurrencySafeEntity.call(this, obj);
      obj =  obj || {};
      _.extend(this, defaults, _.pick(obj, _.keys(defaults)));
      this.statusLabel = CentreStatus.label(this.status);
    }

    Centre.prototype = Object.create(ConcurrencySafeEntity.prototype);

    Centre.prototype.constructor = Centre;

    /**
     * Used by promise code, so it must return an error rather than throw one.
     */
    Centre.create = function (obj) {
      if (!tv4.validate(obj, schema)) {
        throw new Error('invalid object from server: ' + tv4.error);
      }

      if (!validStudyIds(obj.studyIds)) {
        throw new Error('invalid object from server: bad study ids');
      }

      if (!validLocations(obj.locations)) {
        throw new Error('invalid object from server: bad locations');
      }

      return new Centre(obj);
    };

    /**
     * @param {string} options.filter The filter to use on centre names. Default is empty string.
     *
     * @param {string} options.status Returns centres filtered by status. The following are valid: 'all' to
     * return all centres, 'disabled' to return only disabled centres, 'enabled' to reutrn only enable
     * centres, and 'retired' to return only retired centres. For any other values the response is an error.
     *
     * @param {string} options.sortField Centres can be sorted by 'name' or by 'status'. Values other than
     * these two yield an error.
     *
     * @param {int} options.page If the total results are longer than pageSize, then page selects which
     * centres should be returned. If an invalid value is used then the response is an error.
     *
     * @param {int} options.pageSize The total number of centres to return per page. The maximum page size is
     * 10. If a value larger than 10 is used then the response is an error.
     *
     * @param {string} options.order One of 'asc' or 'desc'. If an invalid value is used then
     * the response is an error.
     *
     * @return A promise. If the promise succeeds then a paged result is returned.
     */
    Centre.list = function (options) {
      var validKeys = [
        'filter',
        'status',
        'sort',
        'page',
        'pageSize',
        'order'
      ];
      var url = uri();
      var paramsStr = '';

      options = options || {};

      paramsStr = queryStringService.param(options, function (value, key) {
        return _.contains(validKeys, key);
      });

      if (paramsStr.length) {
        url += paramsStr;
      }

      return biobankApi.get(url).then(function(reply) {
        var deferred = $q.defer();
        try {
          // reply is a paged result
          reply.items = _.map(reply.items, function(obj){
            return Centre.create(obj);
          });
          deferred.resolve(reply);
        } catch (e) {
          deferred.reject('invalid centres from server');
        }
        return deferred.promise;
      });
    };

    Centre.get = function (id) {
      return biobankApi.get(uri(id)).then(function(reply) {
        return Centre.prototype.asyncCreate(reply);
      });
    };

    Centre.prototype.asyncCreate = function (obj) {
      var deferred = $q.defer();

      if (!tv4.validate(obj, schema)) {
        deferred.reject('invalid object from server: ' + tv4.error);
      } else if (!validStudyIds(obj.studyIds)) {
        deferred.reject('invalid study IDs from server: ' + tv4.error);
      } else if (!validLocations(obj.locations)) {
        deferred.reject('invalid locations from server: ' + tv4.error);
      } else {
        deferred.resolve(new Centre(obj));
      }

      return deferred.promise;
    };

    Centre.prototype.add = function () {
      var self = this,
          json = { name: this.name };
      angular.extend(json, funutils.pickOptional(self, 'description'));
      return biobankApi.post(uri(), json).then(function(reply) {
        return self.asyncCreate(reply);
      });
    };

    Centre.prototype.updateName = function (name) {
      return update.call(this, 'name', { name: name });
    };

    Centre.prototype.updateDescription = function (description) {
      if (description) {
        return update.call(this, 'description', { description: description });
      }
      return update.call(this, 'description');
    };

    Centre.prototype.addStudy = function (study) {
      return update.call(this, 'studies', { studyId: study.id });
    };

    Centre.prototype.removeStudy = function (study) {
      var self = this, url;

      if (!_.contains(self.studyIds, study.id)) {
        throw new Error('study ID not present: ' + study.id);
      }

      url = sprintf.sprintf('%s/%d/%s', uri('studies', self.id), self.version, study.id);

      return biobankApi.del(url).then(function(reply) {
        return self.asyncCreate(
          _.extend(self, {
            version: self.version + 1,
            studyIds: _.without(self.studyIds, study.id)
          }));
      });
    };

    Centre.prototype.addLocation = function (location) {
      return update.call(this, 'locations', _.omit(location, 'uniqueId'));
    };

    Centre.prototype.removeLocation = function (location) {
      var self = this, existingLoc, url;

      existingLoc = _.findWhere(self.locations, { uniqueId: location.uniqueId });
      if (_.isUndefined(existingLoc)) {
        throw new Error('location does not exist: ' + location.id);
      }

      url = sprintf.sprintf('%s/%d/%s', uri('locations', self.id), self.version, location.uniqueId);

      return biobankApi.del(url).then(function(reply) {
        return self.asyncCreate(
          _.extend(self, {
            version: self.version + 1,
            locations: _.filter(self.locations, function(loc) {
              return loc.uniqueId !== location.uniqueId;
            })
          }));
      });
    };

    Centre.prototype.isDisabled = function () {
      return this.status === CentreStatus.DISABLED();
    };

    Centre.prototype.isEnabled = function () {
      return this.status === CentreStatus.ENABLED();
    };

    Centre.prototype.disable = function () {
      if (this.isDisabled()) {
        throw new Error('already disabled');
      }
      return changeState(this, 'disable');
    };

    Centre.prototype.enable = function () {
       if (this.isEnabled()) {
        throw new Error('already enabled');
      }
     return changeState(this, 'enable');
    };

    function changeState(centre, status) {
      var json = { expectedVersion: centre.version };
      return biobankApi.post(uri(status, centre.id), json).then(function (reply) {
        return Centre.prototype.asyncCreate(reply);
      });
    }

    function update(path, additionalJson) {
      /* jshint validthis:true */
      var self = this,
          json = { expectedVersion: self.version };

      if (additionalJson) {
        // in the case of description, the value could be undefined
        _.extend(json, additionalJson);
      }
      return biobankApi.post(uri(path, self.id), json).then(function(reply) {
        return self.asyncCreate(reply);
      });
    }

    /**
     * Ensures that all studyIds are valid.
     */
    function validStudyIds(studyIds) {
      var result;

      if (_.isUndefined(studyIds) || (studyIds.length <= 0)) {
        // there are no study IDs, nothing to validate
        return true;
      }
      result = _.find(studyIds, function (studyId) {
        return (studyId === null) || (studyId === '');
      });

      return _.isUndefined(result);
    }

    /**
     * Ensures that all locations are valid.
     */
    function validLocations(locations) {
      var result;

      if (_.isUndefined(locations) || (locations.length <= 0)) {
        // there are no study IDs, nothing to validate
        return true;
      }
      result = _.find(locations, function (location) {
        return !Location.valid(location);
      });

      return _.isUndefined(result);
    }

    function uri(/* path, centreId */) {
      var args = _.toArray(arguments),
          centreId,
          path;

      var result = '/centres';

      if (args.length > 0) {
        path = args.shift();
        result += '/' + path;
      }

      if (args.length > 0) {
        centreId = args.shift();
        result += '/' + centreId;
      }

      return result;
    }

    return Centre;
  }

  return CentreFactory;
});

/* Local Variables:  */
/* mode: js          */
/* End:              */
