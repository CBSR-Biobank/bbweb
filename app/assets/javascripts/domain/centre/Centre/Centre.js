/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
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
   * Used for validation.
   */
  const SCHEMA = ConcurrencySafeEntity.createDerivedSchema({
    id: 'Centre',
    properties: {
      'slug':         { 'type': 'string' },
      'name':         { 'type': 'string'},
      'description':  { 'type': [ 'string', 'null' ] },
      'studyNames':   { 'type': 'array', 'items': { '$ref': 'StudyName' } },
      'locations':    { 'type': 'array', 'items': { '$ref': 'Location' } },
      'state':        { 'type': 'string'}
    },
    required: [ 'slug', 'name', 'state', 'studyNames', 'locations' ]
  });

  /**
   * A Centre can be one or a combination of the following:
   *
   *   - *Collection Centre*: where specimens are collected from patients (e.g. a clinic).
   *
   *   - *Processing Centre*: where collected specimens are processed.
   *
   *   - *Storage Centre*: where collected and processed specimens are stored. Usually, a storage centre is
   *     also a processing centre.
   *
   *   - *Request Centre*: where collected and processed specimens are sent for analysis. Usually the
   *     specimens are stored at a storage centre first for a period of time.
   *
   * Use this constructor to create new Centre to be persisted on the server. Use {@link
   * domain.centres.Centre.create create()} or {@link domain.centres.Centre.asyncCreate asyncCreate()} to
   * create objects returned by the server.
   *
   * @memberOf domain.centres
   * @extends domain.ConcurrencySafeEntity
   */
  class Centre extends ConcurrencySafeEntity {

    /**
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     */
    constructor(obj) {

      /**
       * A short identifying name.
       *
       * @name domain.centres.Centre#name
       * @type {string}
       */

      /**
       * A description that provides more details on the {@link domain.centres.Centre#name name}.
       *
       * @name domain.centres.Centre#description
       * @type {string}
       */

      /**
       * When a centre is {@link domain.centres.CentreState.ENABLED ENABLED} it is ready to collect / store
       * specimens.
       *
       * @name domain.centres.Centre#state
       * @type {domain.centres.CentreState}
       */

      /**
       * The studies associated with this centre.
       *
       * @name domain.centres.Centre#studyNames
       * @type {Array<string>}
       */

      /**
       * The locations for this centre. Some centres may have more than one location.
       *
       * @name domain.centres.Centre#locations
       * @type {Array<domain.Location>}
       */

      super(Object.assign(
        {
          name:        '',
          description: null,
          state:       CentreState.DISABLED,
          studyNames:  [],
          locations:   []
        },
        obj));
    }

    static url(...paths) {
      const args = [ 'centres' ].concat(paths);
      return super.url(...args);
    }

    /**
     * Adds a centre to the system.
     *
     * @returns {Promise} The added centre wrapped in a promise.
     */
    add() {
      var json = { name: this.name, description: this.description };
      // calling function this.url with ean empty string appends a trailing slash
      return biobankApi.post(Centre.url(''), json)
        .then((reply) => Centre.asyncCreate(reply));
    }

    update(url, additionalJson) {
      return super.update(url, additionalJson).then(Centre.asyncCreate);
    }

    /**
     * Updates the centre's name.
     *
     * @param {string} name - The new name to give to this centre.
     *
     * @returns {Promise} A copy of this centre, but with the new name.
     */
    updateName(name) {
      return this.update(Centre.url('name', this.id), { name: name });
    }

    /**
     * Updates the centre's description.
     *
     * @param {string=} description - The new description to give to this centre. If the value is undefined,
     * then the description is cleared.
     *
     * @returns {Promise} A copy of this centre, but with the new description.
     */
    updateDescription(description) {
      var url = Centre.url('description', this.id);
      if (description) {
        return this.update(url, { description: description });
      }
      return this.update(url);
    }

    /**
     * Adds a study to this centre. When a study is added, the centre can participate in collecting and / or
     * storing specimens for the study.
     *
     * @param {domain.studies.Study} study - The study to add to this centre.
     *
     * @returns {Promise} A copy of this centre, but with the study added to it.
     */
    addStudy(study) {
      return this.update(Centre.url('studies', this.id), { studyId: study.id });
    }

    /**
     * Removes a study from this centre. When a study is removed, the centre can no longer participate in
     * collecting and / or storing specimens for the study.
     *
     * @param {domain.studies.Study} study - The study to remove from this centre.
     *
     * @returns {Promise} A copy of this centre, but with the study removed from it.
     */
    removeStudy(study) {
      var url,
          found = _.find(this.studyNames,
                         (studyName) => studyName.id === study.id);

      if (!found) {
        throw new DomainError('study ID not present: ' + study.id);
      }

      url = Centre.url('studies', this.id, this.version, study.id);
      return biobankApi.del(url).then(Centre.asyncCreate);
    }

    /**
     * Adds a location to this centre.
     *
     * @param {domain.Location} location - The location to add.
     *
     * @returns {Promise} A copy of this centre, but with the location added to it.
     */
    addLocation(location) {
      return this.update(Centre.url('locations', this.id), _.omit(location, 'id'));
    }

    /**
     * Updates an existing location on this centre.
     *
     * @param {domain.Location} location - The location to update.
     *
     * @returns {Promise} A copy of this centre, but with the updated location.
     */
    updateLocation(location) {
      return this.update.call(this,
                              Centre.url('locations', this.id) + '/' + location.id,
                              location);
    }

    /**
     * Removes a location from this centre.
     *
     * @param {domain.Location} location - The location to remove.
     *
     * @returns {Promise} A copy of this centre, but with the removed from it.
     */
    removeLocation(location) {
      const existingLoc = _.find(this.locations, { id: location.id });
      if (_.isUndefined(existingLoc)) {
        throw new DomainError('location does not exist: ' + location.id);
      }

      const url = Centre.url('locations', this.id, this.version, location.id);
      return biobankApi.del(url).then(Centre.asyncCreate);
    }

    /**
     * @returns {boolean} True if the centre has at least one location.
     */
    hasLocations() {
      return this.locations.length > 0;
    }

    /**
     * @returns {boolean} True if the centre is disabled.
     */
    isDisabled() {
      return this.state === CentreState.DISABLED;
    }

    /**
     * @returns {boolean} True if the centre is enabled.
     */
    isEnabled() {
      return this.state === CentreState.ENABLED;
    }

    /**
     * Used to disable a centre.
     *
     * @throws an error if the centre is already disabled.
     *
     * @returns {Promise} A copy of this centre, but with state of disabled.
     */
    disable() {
      if (this.isDisabled()) {
        throw new DomainError('already disabled');
      }
      return this.changeState(this, 'disable');
    }

    /**
     * Used to enable a centre.
     *
     * @throws an error if the centre is already enabled.
     *
     * @returns {Promise} A copy of this centre, but with state of enabled.
     */
    enable() {
      if (this.isEnabled()) {
        throw new DomainError('already enabled');
      }
      return this.changeState(this, 'enable');
    }

    /**
     * @return {object} The JSON schema for this class.
     */
    static schema() {
      return SCHEMA;
    }

    /**
     * @private
     */
    static additionalSchemas() {
      return [ StudyName.schema(), Location.schema() ];
    }

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
    static create(obj) {
      var validation = this.isValid(obj);

      if (!validation.valid) {
        $log.error('invalid object from server: ' + validation.message);
        throw new DomainError('invalid object from server: ' + validation.message);
      }

      if (obj.studyNames) {
        obj.studyNames = obj.studyNames.map((name) => StudyName.create(name));
      }

      if (obj.locations) {
        obj.locations = obj.locations.map((location) => Location.create(location));
      }

      return new Centre(obj);
    }

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
    static asyncCreate(obj) {
      var result;

      try {
        result = Centre.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    }

    /**
     * Returns the names for all centres.
     *
     * @returns {Array.<domain.centres.CentreDto>} The name of all the centres.
     */
    static allNames() {
      return biobankApi.get('/centres/names');
    }

    /**
     * Returns centre locations names matching criteria.
     *
     * @param {string} filter - the string to match.
     *
     * @returns {Promise<Array<domain.centres.CentreLocationDto>>} A promise.
     */
    static locationsSearch(filter = '') {
      return biobankApi.post(this.url('locations'),
                             {
                               filter: filter,
                               limit: 10
                             });
    }

    /**
     * Concatenates the centre name and location name so that they can be selected from a
     * drop down list.
     *
     * @param {domain.centres.CentreLocationDto} centreLocations - the locations returned from the server.
     *
     * @returns {Promise<Array<domain.centres.CentreLocationName>>} A promise.
     */
    static centreLocationToNames(centreLocations) {
      return centreLocations
        .map(centreLocation =>
             _.extend({ name: centreLocation.centreName + ': ' + centreLocation.locationName },
                      _.pick(centreLocation, 'centreId', 'locationId')));
    }

    /**
     * Retrieves a centre from the server.
     *
     * @param {string} slug the slug of the centre to retrieve.
     *
     * @returns {Promise<domain.centres.Centre>} The centre wrapped in a promise.
     */
    static get(slug) {
      return biobankApi.get(this.url(slug))
        .then((reply) => Centre.asyncCreate(reply));
    }

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
    static list(options) {
      var params,
          validKeys = [
            'filter',
            'sort',
            'page',
            'limit'
          ];

      options = options || {};
      params = _.omitBy(_.pick(options, validKeys),
                        (value) => value === '');

      return biobankApi.get(this.url('search'), params).then((reply) => {
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
    }

    /**
     * @private
     */
    changeState(centre, state) {
      var json = { expectedVersion: centre.version };
      return biobankApi.post(Centre.url(state, centre.id), json)
        .then((reply) => Centre.asyncCreate(reply));
    }
  }

  return Centre;
}

export default ngModule => ngModule.factory('Centre', CentreFactory)
