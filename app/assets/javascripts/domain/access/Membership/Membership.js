/*/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Angular factory for Users.
 */
/* @ngInject */
function MembershipFactory($q,
                           $log,
                           biobankApi,
                           DomainEntity,
                           ConcurrencySafeEntity,
                           MembershipBase,
                           EntityInfo,
                           EntitySet,
                           DomainError) {

  const SCHEMA = MembershipBase.createDerivedSchema({
    id: 'Membership',
    properties: {
      'userData':  { 'type': 'array', 'items': { '$ref': 'EntityInfo' } }
    },
    required: [ 'userData' ]
  });

  /**
   * User membership information
   *
   * Use this contructor to create a new Membership to be persited on the server. Use {@link
   * domain.access.Membership.create|create()} or {@link domain.access.Membership.asyncCreate|asyncCreate()}
   * to create objects returned by the server.
   *
   * @memberOf domain.access
   * @extends domain.access.MembershipBase
   *
   */
  class Membership extends MembershipBase {

    /**
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     */
    constructor(obj) {
      /**
       * The users this membership is for.
       *
       * @name domain.users.Membership#userData
       * @type {Array<EntityInfo>}
       */

      super(obj);
      this.userData = _.get(obj, 'userData', []).map((info) => new EntityInfo(info));
    }

    /**
     * Adds a membership.
     *
     * @return {Promise<domain.access.Membership>} A promise containing the membership that was created.
     */
    add() {
      var json = _.pick(this, 'name', 'description');
      json.userIds    = this.userData.map(function (userInfo) { return userInfo.id; });
      json.allStudies = this.studyData.allEntities;
      json.allCentres = this.centreData.allEntities;
      json.studyIds   = this.studyData.getEntityIds();
      json.centreIds  = this.centreData.getEntityIds();
      return biobankApi.post(Membership.url(), json).then(Membership.asyncCreate);
    }

    /**
     * Removes a membership.
     *
     * @return {Promise<boolean>} A promise with boolean TRUE if successful.
     */
    remove() {
      var url;
      if (_.isNil(this.id)) {
        throw new DomainError('membership has not been persisted');
      }
      url = Membership.url(this.id, this.version);
      return biobankApi.del(url);
    }

    /** @protected */
    update(url, additionalJson) {
      return super.update(url, additionalJson).then(Membership.asyncCreate);
    }

    /**
     * Updates the name.
     *
     * @param {String} name - The new name to give this membership.
     *
     * @returns {Promise<domain.access.Membership>} A promise containing the membership with the new name.
     */
    updateName(name) {
      return this.update(Membership.url('name', this.id), { name: name });
    }

    /**
     * Updates the Membership's description.
     *
     * @param {String} description - The new description to give this membership. When description is
     * <code>falsy</code>, the description will be cleared.
     *
     * @returns {Promise<domain.access.Membership>} A promise containing the membership with the new description.
     */
    updateDescription(description) {
      return this.update(Membership.url('description', this.id),
                         description ? { description: description } : {});
    }

    addUser(id) {
      return this.update(Membership.url('user', this.id), { userId: id });
    }

    removeUser(id) {
      var url;
      if (_.isNil(this.id)) {
        throw new DomainError('membership has not been persisted');
      }
      url = Membership.url('user', this.id, this.version, id);
      return biobankApi.del(url).then(Membership.asyncCreate);
    }

    updateStudyData(studyData) {
      return this.update(Membership.url('studyData', this.id),
                         {
                           allStudies: studyData.allEntities,
                           studyIds:   studyData.entityData.map(ed => ed.id)
                         });
    }

    allStudies() {
      return this.update(Membership.url('allStudies', this.id), {});
    }

    addStudy(id) {
      return this.update(Membership.url('study', this.id), { studyId: id });
    }

    removeStudy(id) {
      var url;
      if (_.isNil(this.id)) {
        throw new DomainError('membership has not been persisted');
      }
      url = Membership.url('study', this.id, this.version, id);
      return biobankApi.del(url).then(Membership.asyncCreate);
    }

    updateCentreData(centreData) {
      return this.update(Membership.url('centreData', this.id),
                         {
                           allCentres: centreData.allEntities,
                           centreIds:  centreData.entityData.map(ed => ed.id)
                         });
    }

    allCentres() {
      return this.update(Membership.url('allCentres', this.id), {});
    }

    addCentre(id) {
      return this.update(Membership.url('centre', this.id), { centreId: id });
    }

    removeCentre(id) {
      var url;
      if (_.isNil(this.id)) {
        throw new DomainError('membership has not been persisted');
      }
      url = Membership.url('centre', this.id, this.version, id);
      return biobankApi.del(url).then(Membership.asyncCreate);
    }

    static url(...pathItems) {
      const allPaths = [ 'access/memberships' ].concat(pathItems);
      return super.url(...allPaths);
    }

    /** @private */
    static schema() {
      return SCHEMA;
    }

    /** @private */
    static additionalSchemas() {
      return [ EntityInfo.schema(), EntitySet.schema() ];
    }

    /**
     * Creates a Membership, but first it validates <code>obj</code> to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.access.Membership} A Membership created from the given object.
     *
     * @see {@link domain.access.Membership.asyncCreate|asyncCreate()} when you need to create
     * a membership within asynchronous code.
     */
    static create(obj) {
      var validation = Membership.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      return new Membership(obj);
    }

    /**
     * Creates a Membership from a server reply, but first validates that <tt>obj</tt> has a valid schema.
     * <i>Meant to be called from within promise code.</i>
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Promise<domain.access.Membership>} A Membership wrapped in a promise.
     *
     * @see {@link domain.access.Membership.create|create()} when not creating a Membership within
     * asynchronous code.
     */
    static asyncCreate(obj) {
      var result;

      try {
        result = Membership.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    }

    /**
     * Retrieves a Membership from the server.
     *
     * @param {string} slug the slug of the membership to retrieve.
     *
     * @returns {Promise<domain.access.Membership>} The membership within a promise.
     */
    static get(slug) {
      return biobankApi.get(this.url(slug)).then(this.asyncCreate);
    }

    /**
     * Used to list Memberships.
     *
     * @param {object} options - The options to use.
     *
     * @param {string} options.filter The filter expression to use on membership to refine the list.
     *
     * @param {int} options.page If the total results are longer than limit, then page selects which
     * memberships should be returned. If an invalid value is used then the response is an error.
     *
     * @param {int} options.limit The total number of memberships to return per page. The maximum page size is
     * 10. If a value larger than 10 is used then the response is an error.
     *
     * @returns {Promise<common.controllers.PagedListController.PagedResult>} with items of type {@link
     * domain.access.Membership}.
     */
    static list(options = {}) {
      var validKeys = [ 'filter', 'page', 'limit' ],
          params;

      params = _.omitBy(_.pick(options, validKeys),
                        (value) => value === '');

      return biobankApi.get(this.url(), params).then((reply) => {
        // reply is a paged result
        var deferred = $q.defer();
        try {
          reply.items = reply.items.map((obj) => Membership.create(obj));
          deferred.resolve(reply);
        } catch (e) {
          deferred.reject('invalid memberships from server');
        }
        return deferred.promise;
      });
    }
  }

  return Membership;
}

export default ngModule => ngModule.factory('Membership', MembershipFactory)
