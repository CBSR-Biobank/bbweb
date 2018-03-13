/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Angular factory for a AccessItem.
 */
/* @ngInject */
function AccessItemFactory($q,
                           $log,
                           biobankApi,
                           DomainEntity,
                           ConcurrencySafeEntity,
                           EntityInfo) {
  /**
   * A base class for User Access Management objects.
   * @memberOf domain.access
   */
  class AccessItem extends ConcurrencySafeEntity {

    constructor(schema = AccessItem.SCHEMA, obj = {}) {
      super(schema, obj)

      /**
       * A short identifying name that is unique.
       *
       * @name domain.access.AccessItem#name
       * @type {string}
       */

      /**
       * An optional description that can provide additional details on the name.
       *
       * @name domain.access.AccessItem#description
       * @type {string}
       * @default null
       */

      /**
       * This AccessItem's parents.
       *
       * @name domain.access.AccessItem#parentData
       * @type {Array<EntityInfo>}
       */
      this.parentData = []
      if (obj.parentData) {
        this.parentData = obj.parentData.map(info => new EntityInfo(info))
      }

      /**
       * This AccessItem's children.
       *
       * @name domain.access.AccessItem#userData
       * @type {Array<EntityInfo>}
       */
      this.childData = []
      if (obj.childData) {
        this.childData = obj.childData.map(info => new EntityInfo(info))
      }
    }

    static createDerivedSchema({ id, type = 'object', properties = {}, required = [] } = {}) {
      return Object.assign(
        {},
        AccessItem.SCHEMA,
        {
          id: id,
          type: type,
          properties: Object.assign(
            {},
            AccessItem.SCHEMA.properties,
            properties
          ),
          required: AccessItem.SCHEMA.required.slice().concat(required)
        }
      );
    }

  }

  AccessItem.SCHEMA = ConcurrencySafeEntity.createDerivedSchema({
    id: 'AccessItem',
    properties: {
      'slug':         { 'type': 'string' },
      'name':         { 'type': 'string' },
      'description':  { 'type': [ 'string', 'null' ] },
      'parentData':   { 'type': 'array', 'items': { '$ref': 'EntityInfo' } },
      'childData':    { 'type': 'array', 'items': { '$ref': 'EntityInfo' } }
    },
    required: [
      'slug',
      'name',
      'parentData',
      'childData'
    ]
  });

  return AccessItem;
}

export default ngModule => ngModule.factory('AccessItem', AccessItemFactory)
