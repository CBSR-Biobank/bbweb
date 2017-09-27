/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _       = require('lodash'),
      sprintf = require('sprintf-js').sprintf;

  MembershipFactory.$inject = [
    '$q',
    '$log',
    'biobankApi',
    'ConcurrencySafeEntity',
    'MembershipBase',
    'EntityInfo',
    'EntitySet',
    'DomainError'
  ];

  /*
   * Angular factory for Users.
   */
  function MembershipFactory($q,
                             $log,
                             biobankApi,
                             ConcurrencySafeEntity,
                             MembershipBase,
                             EntityInfo,
                             EntitySet,
                             DomainError) {

    /**
     * Use this contructor to create a new Membership to be persited on the server. Use {@link
     * domain.access.Membership.create|create()} or {@link domain.access.Membership.asyncCreate|asyncCreate()}
     * to create objects returned by the server.
     *
     * @class
     * @memberOf domain.access
     * @extends domain.access.MembershipBase
     *
     * @classdesc User membership information.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     */
    function Membership(obj) {
      /**
       * The users this membership is for.
       *
       * @name domain.users.Membership#userData
       * @type {Array<EntityInfo>}
       */
      this.userData = [];

      MembershipBase.call(this, obj);
      this.userData = _.get(obj, 'userData', []).map(function (info) {
        return new EntityInfo(info);
      });
    }

    Membership.prototype = Object.create(MembershipBase.prototype);
    Membership.prototype.constructor = Membership;

    Membership.REST_API_URL = '/access/memberships';

    Membership.SCHEMA = _.extend(
      _.clone(MembershipBase.SCHEMA),
      {
        'id': 'Membership',
        'type': 'object',
        'properties': _.extend(
          _.clone(MembershipBase.SCHEMA.properties),
          { 'userData':  { 'type': 'array', 'items': { '$ref': 'EntityInfo' } } }),
        'required': _.clone(MembershipBase.SCHEMA.required).concat('userData')
      });

    /**
     * Checks if <tt>obj</tt> has valid properties to construct a {@link domain.access.Membership|Membership}.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.Validation} The validation passes if <tt>obj</tt> has a valid schema.
     */
    Membership.isValid = function (obj) {
      return ConcurrencySafeEntity.isValid(Membership.SCHEMA,
                                           [
                                             EntityInfo.SCHEMA,
                                             EntitySet.SCHEMA
                                           ],
                                           obj);
    };

    /**
     * Creates a Membership, but first it validates <code>obj</code> to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.access.Membership} A user created from the given object.
     *
     * @see {@link domain.access.Membership.asyncCreate|asyncCreate()} when you need to create
     * a user within asynchronous code.
     */
    Membership.create = function (obj) {
      var validation = Membership.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      return new Membership(obj);
    };

    /**
     * Creates a Membership from a server reply, but first validates that <tt>obj</tt> has a valid schema.
     * <i>Meant to be called from within promise code.</i>
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Promise<domain.access.Membership>} A user wrapped in a promise.
     *
     * @see {@link domain.access.Membership.create|create()} when not creating a Membership within
     * asynchronous code.
     */
    Membership.asyncCreate = function (obj) {
      var result;

      try {
        result = Membership.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    };

    /**
     * Retrieves a Membership from the server.
     *
     * @param {string} id the ID of the user to retrieve.
     *
     * @returns {Promise<domain.access.Membership>} The user within a promise.
     */
    Membership.get = function(id) {
      return biobankApi.get(uri(id)).then(Membership.prototype.asyncCreate);
    };

    /**
     * Used to list Memberships.
     *
     * @param {object} options - The options to use.
     *
     * @param {string} options.filter The filter expression to use on user to refine the list.
     *
     * @param {int} options.page If the total results are longer than limit, then page selects which
     * users should be returned. If an invalid value is used then the response is an error.
     *
     * @param {int} options.limit The total number of users to return per page. The maximum page size is
     * 10. If a value larger than 10 is used then the response is an error.
     *
     * @returns {Promise} A promise of {@link biobank.domain.PagedResult} with items of type {@link
     * domain.access.Membership}.
     */
    Membership.list = function(options) {
      var validKeys = [ 'filter',
                        'page',
                        'limit'
                      ],
          params;

      options = options || {};
      params = _.omitBy(_.pick(options, validKeys), function (value) {
        return value === '';
      });

      return biobankApi.get(Membership.REST_API_URL, params).then(function(reply) {
        // reply is a paged result
        var deferred = $q.defer();
        try {
          reply.items = _.map(reply.items, function(obj){
            return Membership.create(obj);
          });
          deferred.resolve(reply);
        } catch (e) {
          deferred.reject('invalid memberships from server');
        }
        return deferred.promise;
      });
    };

    /**
     * Creates a Membership from a server reply but first validates that it has a valid schema.
     *
     * <i>A wrapper for {@link domain.access.Membership#asyncCreate}.</i>
     *
     * @see {@link domain.ConcurrencySafeEntity#update}
     */
    Membership.prototype.asyncCreate = function (obj) {
      return Membership.asyncCreate(obj);
    };

    /**
     * Adds a membership.
     *
     * @return {Promise<domain.access.Membership>} A promise containing the membership that was created.
     */
    Membership.prototype.add = function () {
      var json = _.pick(this, 'name', 'description');
      json.userIds    = this.userData.map(function (userInfo) { return userInfo.id; });
      json.allStudies = this.studyData.allEntities;
      json.allCentres = this.centreData.allEntities;
      json.studyIds   = this.studyData.getEntityIds();
      json.centreIds  = this.centreData.getEntityIds();
      return biobankApi.post(uri(), json).then(Membership.asyncCreate);
    };

    /**
     * Removes a membership.
     *
     * @return {Promise<boolean>} A promise with boolean TRUE if successful.
     */
    Membership.prototype.remove = function () {
      var url;
      if (_.isNil(this.id)) {
        throw new DomainError('membership has not been persisted');
      }
      url = sprintf('%s/%d', uri(this.id), this.version);
      return biobankApi.del(url);
    };

    /**
     * Updates the name.
     *
     * @param {String} name - The new name to give this membership.
     *
     * @returns {Promise<domain.access.Membership>} A promise containing the membershipo with the new name.
     */
    Membership.prototype.updateName = function (name) {
      return this.update.call(this, updateUri('name', this.id), { name: name });
    };

    /**
     * Updates the Membership's description.
     *
     * @param {String} description - The new description to give this membership. When description is
     * <code>falsy</code>, the description will be cleared.
     *
     * @returns {Promise<domain.access.Membership>} A promise containing the membership with the new description.
     */
    Membership.prototype.updateDescription = function (description) {
      return this.update.call(this,
                              updateUri('description', this.id),
                              description ? { description: description } : {});
    };

    Membership.prototype.addUser = function (id) {
      return this.update.call(this, updateUri('user', this.id), { userId: id });
    };

    Membership.prototype.removeUser = function (id) {
      var url;
      if (_.isNil(this.id)) {
        throw new DomainError('membership has not been persisted');
      }
      url = sprintf('%s/%d/%s', updateUri('user', this.id), this.version, id);
      return biobankApi.del(url).then(Membership.asyncCreate);
    };

    Membership.prototype.allStudies = function () {
      return this.update.call(this, updateUri('allStudies', this.id), {});
    };

    Membership.prototype.addStudy = function (id) {
      return this.update.call(this, updateUri('study', this.id), { studyId: id });
    };

    Membership.prototype.removeStudy = function (id) {
      var url;
      if (_.isNil(this.id)) {
        throw new DomainError('membership has not been persisted');
      }
      url = sprintf('%s/%d/%s', updateUri('study', this.id), this.version, id);
      return biobankApi.del(url).then(Membership.asyncCreate);
    };

    Membership.prototype.allCentres = function () {
      return this.update.call(this, updateUri('allCentres', this.id), {});
    };

    Membership.prototype.addCentre = function (id) {
      return this.update.call(this, updateUri('centre', this.id), { centreId: id });
    };

    Membership.prototype.removeCentre = function (id) {
      var url;
      if (_.isNil(this.id)) {
        throw new DomainError('membership has not been persisted');
      }
      url = sprintf('%s/%d/%s', updateUri('centre', this.id), this.version, id);
      return biobankApi.del(url).then(Membership.asyncCreate);
    };

    function uri(id) {
      var result = Membership.REST_API_URL + '/';
      if (arguments.length > 0) {
        result += id;
      }
      return result;
    }

    function updateUri(/* path, membershipId */) {
      var result = '/access/memberships/',
          args = _.toArray(arguments),
          path,
          membershipId;

      if (args.length > 0) {
        path = args.shift();
        result += path;
      }

      if (args.length > 0) {
        membershipId = args.shift();
        result += '/' + membershipId;
      }
      return result;
    }

    return Membership;
  }

  return MembershipFactory;
});
