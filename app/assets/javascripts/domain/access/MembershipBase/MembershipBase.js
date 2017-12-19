/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

var _ = require('lodash');

/* @ngInject */
function MembershipBaseFactory($q,
                               $log,
                               biobankApi,
                               ConcurrencySafeEntity,
                               EntityInfo,
                               EntitySet) {

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
    this.studyData = [];

    /**
     * The centres this membership is for.
     *
     * @name domain.users.MembershipBase#centreData
     * @type {Array<string>}
     */
    this.centreData = [];

    ConcurrencySafeEntity.call(this, MembershipBase.SCHEMA, obj);
    this.studyData = new EntitySet(_.get(obj, 'studyData', {}));
    this.centreData = new EntitySet(_.get(obj, 'centreData', {}));
  }

  MembershipBase.prototype = Object.create(ConcurrencySafeEntity.prototype);
  MembershipBase.prototype.constructor = MembershipBase;

  MembershipBase.SCHEMA = ConcurrencySafeEntity.createDerivedSchema({
    id: 'MembershipBase',
    properties: {
      'slug':         { 'type': 'string' },
      'name':         { 'type': 'string' },
      'description':  { 'type': [ 'string', 'null' ] },
      'studyData':    { 'type': 'object', 'items': { '$ref': 'EntitySet' } },
      'centreData':   { 'type': 'object', 'items': { '$ref': 'EntitySet' } }
    },
    required: [ 'id', 'version', 'timeAdded', 'studyData', 'centreData' ]
  });

  MembershipBase.createDerivedSchema = function ({ id, type = 'object', properties = {}, required = [] } = {}) {
    return Object.assign(
      {},
      MembershipBase.SCHEMA,
      {
        id: id,
        type: type,
        properties: Object.assign(
          {},
          MembershipBase.SCHEMA.properties,
          properties
        ),
        required: MembershipBase.SCHEMA.required.slice().concat(required)
      }
    );
  }

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

export default ngModule => ngModule.factory('MembershipBase', MembershipBaseFactory)
