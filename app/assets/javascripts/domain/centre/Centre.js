/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'lodash', 'tv4', 'sprintf'], function(angular, _, tv4, sprintf) {
  'use strict';

  CentreFactory.$inject = [
    '$q',
    'biobankApi',
    'ConcurrencySafeEntity',
    'DomainError',
    'CentreStatus',
    'centreStatusLabel',
    'Location'
  ];

  /**
   * Factory for Centres.
   *
   * @param {$q} $q - AngularJS service for asynchronous functions.
   *
   * @param {biobankApi} biobankApi - service that communicates with the Biobank server.
   *
   * @param {ConcurrencySafeEntity} ConcurrencySafeEntity - Base class for domain objects.
   *
   * @param {CentreStatus} CenterStatus - The enumerated type for the status of a centre.
   *
   * @param {Service} centreStatusLabel - The service that converts a center's status to a label that can be
   * shown in the UI.
   *
   * @param {Location} Location -
   *
   * @returns {Factory} The AngularJS factory function.
   */
  function CentreFactory($q,
                         biobankApi,
                         ConcurrencySafeEntity,
                         DomainError,
                         CentreStatus,
                         centreStatusLabel,
                         Location) {

    /**
     * Used for validation.
     */
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
     * Use this constructor to create new Centre to be persisted on the server. Use
     * [create()]{@link domain.centres.Centre.create} or [asyncCreate()]{@link
     * domain.centres.Centre.asyncCreate} to create objects returned by the server.
     *
     * @classdesc A Centre can be one or a combination of the following:
     * <dl>
     *   <dt>Collection Centre</dt>
     *
     *   <dd>where specimens are collected from patients (e.g. a clinic).</dd>
     *
     *   <dt>Processing Centre</dt>
     *
     *   <dd>where collected specimens are processed.</dd>
     *
     *   <dt>Storage Centre</dt>
     *
     *   <dd>where collected and processed specimens are stored. Usually, a storage centre is also a
     *   processing centre.</dd>
     *
     *   <dt>Request Centre</dt>
     *
     *   <dd>where collected and processed specimens are sent for analysis. Usually the specimens are stored
     *   first at a storage centre for a period of time.</dd>
     *
     * </dl>
     *
     * @class
     * @memberOf domain.centres
     * @extends domain.ConcurrencySafeEntity
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     */
    function Centre(obj) {
      /** A short identifying name. */
      this.name = '';

      /** A description that provides more details on the {@link domain.centres.Centre#name name}. */
      this.description = null;

      /**
       * When a centre is {@link domain.centres.CentreStatus.ENABLED ENABLED} it is ready to collect / store
       * specimens.
       */
      this.status = CentreStatus.DISABLED;

      /** The studies associated with this centre. */
      this.studyIds = [];

      /** The locations for this centre. Some centres may have more than one location. */
      this.locations = [];

      ConcurrencySafeEntity.call(this, obj);
      obj =  obj || {};
      _.extend(this, _.pick(obj, _.keys(this)));
      this.statusLabel = centreStatusLabel.statusToLabel(this.status);
    }

    Centre.prototype = Object.create(ConcurrencySafeEntity.prototype);
    Centre.prototype.constructor = Centre;

    /**
     * Creates a Centre, but first it validates <code>obj</code> to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @throws Throws an error if <code>obj</code> does not have the required properties.
     *
     * @returns {domain.centres.Centre} A new centre.
     *
     * @see [asyncCreate()]{@link domain.centres.Centre.asyncCreate} when you need to create
     * a centre within asynchronous code.
     */
    Centre.create = function (obj) {
      if (!tv4.validate(obj, schema)) {
        console.error('invalid object from server: ' + tv4.error);
        throw new DomainError('invalid object from server: ' + tv4.error);
      }

      if (!validStudyIds(obj.studyIds)) {
        console.error('invalid object from server: bad study ids');
        throw new DomainError('invalid object from server: bad study ids');
      }

      if (!validLocations(obj.locations)) {
        console.error('invalid object from server: bad locations');
        throw new DomainError('invalid object from server: bad locations');
      }

      return new Centre(obj);
    };

    /**
     * Creates a Centre from a server reply but first validates that it has a valid schema.
     *
     * <p>Meant to be called from within promise code.</p>
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Promise} A new centre wrapped in a promise.
     *
     * @see [create()]{@link domain.centres.Centre.create} when not creating a Centre within
     * asynchronous code.
     */
    Centre.asyncCreate = function (obj) {
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

    /**
     * @typedef domain.centres.CentreDto
     * @type object
     * @property {string} id - the ID that identifies this centre.
     * @property {string} name - the centre's name.
     * @property {domain.centre.CentreStatus} status - the centre's status.
     */

    /**
     * Returns the names for all centres.
     *
     * @returns {Array.<domain.centres.CentreDto>} The name of all the centres.
     */
    Centre.allNames = function () {
      return biobankApi.get('/centres/names');
    };

    /**
     * @typedef domain.centres.CentreLocationDto
     * @type object
     * @property {string} centreId - the ID that identifies the centre.
     * @property {string} locationId - the ID that identifies the location.
     * @property {string} centreName - the centre's name.
     * @property {string} locationName - the location's name.
     */

    /**
     * Returns all locations for all centres.
     *
     * @returns {Promise<Array<domain.centres.CentreLocationDto>>} A promise.
     */
    Centre.allLocations = function () {
      return biobankApi.get('/centres/locations');
    };

    /**
     * @typedef domain.centres.CentreLocationName
     * @type object
     * @property {string} centreId - the ID that identifies the centre.
     * @property {string} locationId - the ID that identifies the location.
     * @property {string} name - the centre's name concatenated with the location name.
     */

    /**
     * Concatenates the centre name and location name so that they can be selected from a
     * drop down list.
     *
     * @param {domain.centres.CentreLocationDto} - the locations returned from the server.
     *
     * @returns {Promise<Array<domain.centres.CentreLocationName>>} A promise.
     */
    Centre.centreLocationToNames = function (centreLocations) {
      return _.map(centreLocations, function (centreLocation) {
        return _.extend({ name: centreLocation.centreName + ': ' + centreLocation.locationName },
                        _.pick(centreLocation, 'centreId', 'locationId'));
      });
    };

    /**
     * Used to list the centres stored in the system.
     *
     * <p>A paged API is used to list centres. See below for more details.</p>
     *
     * @param {object} options - The options to use to list centres.
     *
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
      var url = uri(),
          params,
          validKeys = [
            'filter',
            'status',
            'sort',
            'page',
            'pageSize',
            'order'
          ];

      options = options || {};
      params = _.pick(options, validKeys);

      return biobankApi.get(url, params).then(function(reply) {
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

    /**
     * Retrieves the centre with the given ID.
     *
     * @param {string} id - The ID associated with the centre.
     *
     * @returns {Promise} The centre wrapped in a promise.
     */
    Centre.get = function (id) {
      return biobankApi.get(uri(id)).then(function(reply) {
        return Centre.asyncCreate(reply);
      });
    };

    /**
     * Creates a Centre from a server reply but first validates that it has a valid schema.
     *
     * <p>A wrapper for {@link domian.centres.Centre#asyncCreate}.</p>
     *
     * @see domain.ConcurrencySafeEntity.update
     */
    Centre.prototype.asyncCreate = function (obj) {
      return Centre.asyncCreate(obj);
    };

    /**
     * Adds a centre to the system.
     *
     * @returns {Promise} The added centre wrapped in a promise.
     */
    Centre.prototype.add = function () {
      var json = { name: this.name, description: this.description };
      return biobankApi.post(uri(), json).then(function(reply) {
        return Centre.asyncCreate(reply);
      });
    };

    /**
     * Updates the centre's name.
     *
     * @param {string} name - The new name to give to this centre.
     *
     * @returns {Promise} A copy of this centre, but with the new name.
     */
    Centre.prototype.updateName = function (name) {
      return this.update.call(this, uri('name', this.id), { name: name });
    };

    /**
     * Updates the centre's description.
     *
     * @param {string=} description - The new description to give to this centre. If the value is undefined,
     * then the description is cleared.
     *
     * @returns {Promise} A copy of this centre, but with the new description.
     */
    Centre.prototype.updateDescription = function (description) {
      var url = uri('description', this.id);
      if (description) {
        return this.update.call(this, url, { description: description });
      }
      return this.update.call(this, url);
    };

    /**
     * Adds a study to this centre. When a study is added, the centre can participate in collecting and / or
     * storing specimens for the study.
     *
     * @param {domain.studies.Study} study - The study to add to this centre.
     *
     * @returns {Promise} A copy of this centre, but with the study added to it.
     */
    Centre.prototype.addStudy = function (study) {
      return this.update.call(this, uri('studies', this.id), { studyId: study.id });
    };

    /**
     * Removes a study from this centre. When a study is removed, the centre can no longer participate in
     * collecting and / or storing specimens for the study.
     *
     * @param {domain.studies.Study} study - The study to remove from this centre.
     *
     * @returns {Promise} A copy of this centre, but with the study removed from it.
     */
    Centre.prototype.removeStudy = function (study) {
      var self = this, url;

      if (!_.includes(self.studyIds, study.id)) {
        throw new DomainError('study ID not present: ' + study.id);
      }

      url = sprintf.sprintf('%s/%d/%s', uri('studies', self.id), self.version, study.id);

      return biobankApi.del(url).then(function(reply) {
        return Centre.asyncCreate(
          _.extend(self, {
            version: self.version + 1,
            studyIds: _.without(self.studyIds, study.id)
          }));
      });
    };

    /**
     * Adds a location to this centre.
     *
     * @param {domain.Location} location - The location to add.
     *
     * @returns {Promise} A copy of this centre, but with the location added to it.
     */
    Centre.prototype.addLocation = function (location) {
      return this.update.call(this, uri('locations', this.id), _.omit(location, 'uniqueId'));
    };

    /**
     * Updates an existing location on this centre.
     *
     * @param {domain.Location} location - The location to update.
     *
     * @returns {Promise} A copy of this centre, but with the updated location.
     */
    Centre.prototype.updateLocation = function (location) {
      return this.update.call(this,
                              uri('locations', this.id) + '/' + location.uniqueId,
                              location);
    };

    /**
     * Removes a location from this centre.
     *
     * @param {domain.Location} location - The location to remove.
     *
     * @returns {Promise} A copy of this centre, but with the removed from it.
     */
    Centre.prototype.removeLocation = function (location) {
      var self = this, existingLoc, url;

      existingLoc = _.find(self.locations, { uniqueId: location.uniqueId });
      if (_.isUndefined(existingLoc)) {
        throw new DomainError('location does not exist: ' + location.id);
      }

      url = sprintf.sprintf('%s/%d/%s', uri('locations', self.id), self.version, location.uniqueId);

      return biobankApi.del(url).then(function(reply) {
        return Centre.asyncCreate(
          _.extend(self, {
            version: self.version + 1,
            locations: _.filter(self.locations, function(loc) {
              return loc.uniqueId !== location.uniqueId;
            })
          }));
      });
    };

    /**
     * @returns {boolean} True if the centre has at least one location.
     */
    Centre.prototype.hasLocations = function () {
      return this.locations.length > 0;
    };

    /**
     * @returns {boolean} True if the centre is disabled.
     */
    Centre.prototype.isDisabled = function () {
      return this.status === CentreStatus.DISABLED;
    };

    /**
     * @returns {boolean} True if the centre is enabled.
     */
    Centre.prototype.isEnabled = function () {
      return this.status === CentreStatus.ENABLED;
    };

    /**
     * Used to disable a centre.
     *
     * @throws an error if the centre is already disabled.
     *
     * @returns {Promise} A copy of this centre, but with status of disabled.
     */
    Centre.prototype.disable = function () {
      if (this.isDisabled()) {
        throw new DomainError('already disabled');
      }
      return changeState(this, 'disable');
    };

    /**
     * Used to enable a centre.
     *
     * @throws an error if the centre is already enabled.
     *
     * @returns {Promise} A copy of this centre, but with status of enabled.
     */
    Centre.prototype.enable = function () {
       if (this.isEnabled()) {
        throw new DomainError('already enabled');
      }
     return changeState(this, 'enable');
    };

    function changeState(centre, status) {
      var json = { expectedVersion: centre.version };
      return biobankApi.post(uri(status, centre.id), json).then(function (reply) {
        return Centre.asyncCreate(reply);
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
