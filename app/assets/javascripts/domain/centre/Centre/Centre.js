/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/**
 * Factory for Centres.
 *
 * @param {$q} $q - AngularJS service for asynchronous functions.
 *
 * @param {biobankApi} biobankApi - service that communicates with the Biobank server.
 *
 * @param {ConcurrencySafeEntity} ConcurrencySafeEntity - Base class for domain objects.
 *
 * @param {CentreState} CenterState - The enumerated type for the state of a centre.
 *
 * @param {Location} Location -
 *
 * @returns {Factory} The AngularJS factory function.
 */
/* @ngInject */
function CentreFactory($q,
                       $log,
                       biobankApi,
                       DomainEntity,
                       ConcurrencySafeEntity,
                       DomainError,
                       CentreState,
                       StudyName,
                       Location) {

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
     * When a centre is {@link domain.centres.CentreState.ENABLED ENABLED} it is ready to collect / store
     * specimens.
     */
    this.state = CentreState.DISABLED;

    /** The studies associated with this centre. */
    this.studyNames = [];

    /** The locations for this centre. Some centres may have more than one location. */
    this.locations = [];

    ConcurrencySafeEntity.call(this, Centre.SCHEMA, obj);
  }

  Centre.prototype = Object.create(ConcurrencySafeEntity.prototype);
  Centre.prototype.constructor = Centre;

  Centre.url = function (/* pathItem1, pathItem2, ... pathItemN */) {
    const args = [ 'centres' ].concat(_.toArray(arguments));
    return DomainEntity.url.apply(null, args);
  };

  /**
   * Used for validation.
   */
  Centre.SCHEMA = {
    'id': 'Centre',
    'type': 'object',
    'properties': {
      'id':           { 'type': 'string'},
      'version':      { 'type': 'integer', 'minimum': 0},
      'timeAdded':    { 'type': 'string'},
      'timeModified': { 'type': [ 'string', 'null' ] },
      'name':         { 'type': 'string'},
      'description':  { 'type': [ 'string', 'null' ] },
      'studyNames':   { 'type': 'array', 'items': { '$ref': 'StudyName' } },
      'locations':    { 'type': 'array', 'items': { '$ref': 'Location' } },
      'state':        { 'type': 'string'}
    },
    'required': [ 'id', 'version', 'timeAdded', 'name', 'state', 'studyNames' ]
  };

  /*
   * @private
   */
  Centre.isValid = function(obj) {
    return ConcurrencySafeEntity.isValid(Centre.SCHEMA, [ StudyName.SCHEMA, Location.SCHEMA ], obj);
  };

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
    var validation = Centre.isValid(obj);

    if (!validation.valid) {
      $log.error('invalid object from server: ' + validation.message);
      throw new DomainError('invalid object from server: ' + validation.message);
    }

    if (obj.studyNames) {
      obj.studyNames = obj.studyNames.map(function (name) {
        return StudyName.create(name);
      });
    }

    if (obj.locations) {
      obj.locations = obj.locations.map(function (location) {
        return Location.create(location);
      });
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
    var result;

    try {
      result = Centre.create(obj);
      return $q.when(result);
    } catch (e) {
      return $q.reject(e);
    }
  };

  /**
   * Returns the names for all centres.
   *
   * @returns {Array.<domain.centres.CentreDto>} The name of all the centres.
   */
  Centre.allNames = function () {
    return biobankApi.get('/centres/names');
  };

  /**
   * Returns centre locations names matching criteria.
   *
   * @param {string} filter - the string to match.
   *
   * @returns {Promise<Array<domain.centres.CentreLocationDto>>} A promise.
   */
  Centre.locationsSearch = function (filter) {
    filter = filter || '';
    return biobankApi.post(Centre.url('locations'),
                           {
                             filter: filter,
                             limit: 10
                           });
  };

  /**
   * Concatenates the centre name and location name so that they can be selected from a
   * drop down list.
   *
   * @param {domain.centres.CentreLocationDto} centreLocations - the locations returned from the server.
   *
   * @returns {Promise<Array<domain.centres.CentreLocationName>>} A promise.
   */
  Centre.centreLocationToNames = function (centreLocations) {
    return centreLocations.map((centreLocation) =>
                               _.extend({ name: centreLocation.centreName + ': ' + centreLocation.locationName },
                                        _.pick(centreLocation, 'centreId', 'locationId')));
  };

  /**
   * Retrieves the centre with the given ID.
   *
   * @param {string} id - The ID associated with the centre.
   *
   * @returns {Promise} The centre wrapped in a promise.
   */
  Centre.get = function (id) {
    return biobankApi.get(Centre.url(id)).then(function(reply) {
      return Centre.asyncCreate(reply);
    });
  };

  /**
   * Used to list the centres stored in the system.
   *
   * <p>A paged API is used to list centres. See below for more details.</p>
   *
   * @param {object} options - The options to use to list centres.
   *
   * @param {string} options.filter The filter to use on centres. Default is empty string.
   *
   * @param {string} options.sort Centres can be sorted by <code>name</code> or by
   *                              <code>state</code>. Values other than these two yield an error.
   *                              Use a minus sign prefix to sort in descending order.
   *
   * @param {int} options.page If the total results are longer than limit, then page selects which centres
   *                           should be returned. If an invalid value is used then the response is an
   *                           error.
   *
   * @param {int} options.limit The total number of centres to return per page. The maximum page size is 10.
   *                            If a value larger than 10 is used then the response is an error.
   *
   * @return A promise. If the promise succeeds then a paged result is returned.
   */
  Centre.list = function (options) {
    var params,
        validKeys = [
          'filter',
          'sort',
          'page',
          'limit'
        ];

    options = options || {};
    params = _.omitBy(_.pick(options, validKeys), function (value) {
      return value === '';
    });

    return biobankApi.get(Centre.url('search'), params).then(function(reply) {
      var deferred = $q.defer();
      try {
        // reply is a paged result
        reply.items = reply.items.map((obj) => Centre.create(obj));
        deferred.resolve(reply);
      } catch (e) {
        deferred.reject('invalid centres from server');
      }
      return deferred.promise;
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
    return biobankApi.post(Centre.url(), json).then(function(reply) {
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
    return this.update.call(this, Centre.url('name', this.id), { name: name });
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
    var url = Centre.url('description', this.id);
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
    return this.update.call(this, Centre.url('studies', this.id), { studyId: study.id });
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
    var self = this,
        url,
        found = _.find(self.studyNames, function (studyName) {
          return studyName.id === study.id;
        });

    if (!found) {
      throw new DomainError('study ID not present: ' + study.id);
    }

    url = Centre.url('studies', self.id, self.version, study.id);
    return biobankApi.del(url).then(Centre.asyncCreate);
  };

  /**
   * Adds a location to this centre.
   *
   * @param {domain.Location} location - The location to add.
   *
   * @returns {Promise} A copy of this centre, but with the location added to it.
   */
  Centre.prototype.addLocation = function (location) {
    return this.update.call(this, Centre.url('locations', this.id), _.omit(location, 'id'));
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
                            Centre.url('locations', this.id) + '/' + location.id,
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

    existingLoc = _.find(self.locations, { id: location.id });
    if (_.isUndefined(existingLoc)) {
      throw new DomainError('location does not exist: ' + location.id);
    }

    url = Centre.url('locations', self.id, self.version, location.id);
    return biobankApi.del(url).then(Centre.asyncCreate);
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
    return this.state === CentreState.DISABLED;
  };

  /**
   * @returns {boolean} True if the centre is enabled.
   */
  Centre.prototype.isEnabled = function () {
    return this.state === CentreState.ENABLED;
  };

  /**
   * Used to disable a centre.
   *
   * @throws an error if the centre is already disabled.
   *
   * @returns {Promise} A copy of this centre, but with state of disabled.
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
   * @returns {Promise} A copy of this centre, but with state of enabled.
   */
  Centre.prototype.enable = function () {
    if (this.isEnabled()) {
      throw new DomainError('already enabled');
    }
    return changeState(this, 'enable');
  };

  function changeState(centre, state) {
    var json = { expectedVersion: centre.version };
    return biobankApi.post(Centre.url(state, centre.id), json).then(function (reply) {
      return Centre.asyncCreate(reply);
    });
  }

  return Centre;
}

export default ngModule => ngModule.factory('Centre', CentreFactory)
