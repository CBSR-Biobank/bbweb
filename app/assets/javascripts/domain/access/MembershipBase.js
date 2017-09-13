/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function(require) {
  'use strict';

  var _ = require('lodash');

  MembershipBaseFactory.$inject = [
    '$q',
    '$log',
    'biobankApi',
    'ConcurrencySafeEntity',
    'EntityInfo',
    'EntitySet',
    'DomainError'
  ];

  /*
   * Angular factory for Users.
   */
  function MembershipBaseFactory($q,
                                 $log,
                                 biobankApi,
                                 ConcurrencySafeEntity,
                                 EntityInfo,
                                 EntitySet,
                                 DomainError) {

    /**
     * Do not use this constructor. Use {@link domain.access.Membership} or {@link
     * domain.access.UserMembership} instead.
     *
     * @class
     * @memberOf domain.access
     * @extends domain.ConcurrencySafeEntity
     *
     * @classdesc User membership information.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     */
    function MembershipBase(obj) {
      /**
       * A short identifying name that is unique.
       *
       * @name domain.access.MembershipBase#name
       * @type {string}
       */
      this.name = '';

      /**
       * An optional description that can provide additional details on the name.
       *
       * @name domain.access.MembershipBase#description
       * @type {string}
       * @default null
       */

      /**
       * The studies this membership is for.
       *
       * @name domain.access.MembershipBase#studyData
       * @type {Array<string>}
       */
      this.studyData = null;

      /**
       * The centres this membership is for.
       *
       * @name domain.users.MembershipBase#centreData
       * @type {Array<string>}
       */
      this.centreData = null;

      ConcurrencySafeEntity.call(this, MembershipBase.SCHEMA, obj);
      this.studyData = new EntitySet(_.get(obj, 'studyData', {}));
      this.centreData = new EntitySet(_.get(obj, 'centreData', {}));
    }

    MembershipBase.prototype = Object.create(ConcurrencySafeEntity.prototype);
    MembershipBase.prototype.constructor = MembershipBase;

    MembershipBase.SCHEMA = {
      'id': 'MembershipBase',
      'type': 'object',
      properties: {
        'id':           { 'type': 'string' },
        'version':      { 'type': 'integer', 'minimum': 0 },
        'timeAdded':    { 'type': 'string' },
        'timeModified': { 'type': [ 'string', 'null' ] },
        'name':         { 'type': 'string' },
        'description':  { 'type': [ 'string', 'null' ] },
        'studyData':    { 'type': 'object', 'items': { '$ref': 'EntitySet' } },
        'centreData':   { 'type': 'object', 'items': { '$ref': 'EntitySet' } }
      },
      'required': [ 'id', 'version', 'timeAdded', 'studyData', 'centreData' ]
    };

    /**
     * Checks if <tt>obj</tt> has valid properties to construct a {@link
     * domain.access.MembershipBase|MembershipBase}.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.Validation} The validation passes if <tt>obj</tt> has a valid schema.
     */
    MembershipBase.isValid = function (obj) {
       return ConcurrencySafeEntity.isValid(MembershipBase.SCHEMA,
                                            [
                                               EntityInfo.SCHEMA,
                                               EntitySet.SCHEMA
                                            ],
                                            obj);
    };

    /**
     * Creates a MembershipBase, but first it validates <code>obj</code> to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.access.MembershipBase} A MembershipBase created from the given object.
     *
     * @see {@link domain.access.MembershipBase.asyncCreate|asyncCreate()} when you need to create
     * a MembershipBase within asynchronous code.
     */
    MembershipBase.create = function (obj) {
      var validation = MembershipBase.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      return new MembershipBase(obj);
    };

    /**
     * Creates a MembershipBase from a server reply, but first validates that <tt>obj</tt> has a valid schema.
     * <i>Meant to be called from within promise code.</i>
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Promise<domain.access.MembershipBase>} A MembershipBase wrapped in a promise.
     *
     * @see {@link domain.access.MembershipBase.create|create()} when not creating a MembershipBase within
     * asynchronous code.
     */
    MembershipBase.asyncCreate = function (obj) {
      var result;

      try {
        result = MembershipBase.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    };

    MembershipBase.prototype.isForAllStudies = function () {
      return this.studyData.isForAllEntities();
    };

    MembershipBase.prototype.isMemberOfStudy = function (name) {
      return this.studyData.isMemberOf(name);
    };

    MembershipBase.prototype.isForAllCentres = function () {
      return this.centreData.isForAllEntities();
    };

    MembershipBase.prototype.isMemberOfCentre = function (name) {
      return this.centreData.isMemberOf(name);
    };

    return MembershipBase;
  }

  return MembershipBaseFactory;
});
