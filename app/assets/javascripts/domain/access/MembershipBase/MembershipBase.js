/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

var _ = require('lodash');

/* @ngInject */
function MembershipBaseFactory($q,
                               $log,
                               biobankApi,
                               ConcurrencySafeEntity,
                               EntityInfo,
                               EntitySet) {


  const SCHEMA = ConcurrencySafeEntity.createDerivedSchema({
    id: 'MembershipBase',
    properties: {
      'slug':         { 'type': 'string' },
      'name':         { 'type': 'string' },
      'description':  { 'type': [ 'string', 'null' ] },
      'studyData':    { '$ref': 'EntitySet' },
      'centreData':   { '$ref': 'EntitySet' }
    },
    required: [ 'id', 'version', 'timeAdded', 'studyData', 'centreData' ]
  });

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
  class MembershipBase extends ConcurrencySafeEntity {

    constructor(obj) {
      /**
       * A short identifying name that is unique.
       *
       * @name domain.access.MembershipBase#name
       * @type {string}
       */

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

      /**
       * The centres this membership is for.
       *
       * @name domain.users.MembershipBase#centreData
       * @type {Array<string>}
       */

      super(Object.assign(
        {
          name:  '',
          studyData:  [],
          centreData:  []
        },
        obj))

      this.studyData = new EntitySet(_.get(obj, 'studyData', {}));
      this.centreData = new EntitySet(_.get(obj, 'centreData', {}));
    }

    isForAllStudies() {
      return this.studyData.isForAllEntities();
    }

    isMemberOfStudy(name) {
      return this.studyData.isMemberOf(name);
    }

    isForAllCentres() {
      return this.centreData.isForAllEntities();
    }

    isMemberOfCentre(name) {
      return this.centreData.isMemberOf(name);
    }

    static createDerivedSchema({ id, type = 'object', properties = {}, required = [] } = {}) {
      return Object.assign(
        {},
        SCHEMA,
        {
          id: id,
          type: type,
          properties: Object.assign(
            {},
            SCHEMA.properties,
            properties
          ),
          required: SCHEMA.required.slice().concat(required)
        }
      );
    }
  }

  return MembershipBase;
}

export default ngModule => ngModule.factory('MembershipBase', MembershipBaseFactory)
